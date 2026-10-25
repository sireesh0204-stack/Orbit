import uuid
from datetime import datetime
from typing import List, Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Response
from fastapi.responses import Response
from app.auth import AuthenticatedUser, verify_whitelisted_user
from app.models.job import TailoredResumeMetadata
from app.routers.profile import _PROFILES_DB
from app.routers.jobs import _JOBS_CACHE, get_or_create_default_profile
from app.services.tailor.drafter import draft_tailored_resume
from app.services.tailor.reviewer import audit_and_review_draft
from app.services.tailor.doc_generator import (
    generate_ats_docx,
    generate_ats_pdf,
    verify_ats_parsability
)

router = APIRouter(prefix="/api/v1/tailor", tags=["Tailor"])

# In-memory artifact storage for generated resumes
_TAILORED_METADATA_DB: Dict[str, TailoredResumeMetadata] = {}
_DOCX_BINARIES: Dict[str, bytes] = {}
_PDF_BINARIES: Dict[str, bytes] = {}

class TailorRequest:
    def __init__(self, job_id: str):
        self.job_id = job_id

@router.post("", response_model=TailoredResumeMetadata)
async def tailor_resume_for_job(
    job_id: str,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    job = _JOBS_CACHE.get(job_id)
    if not job:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Target job with ID '{job_id}' not found."
        )

    profile = get_or_create_default_profile(user.uid)

    # Pass 1: Drafter Agent
    draft = draft_tailored_resume(profile, job)

    # Pass 2: Reviewer & Auditor Agent
    is_valid, ats_score, missing_kw, talking_points, flags = audit_and_review_draft(
        draft, profile, job
    )

    if not is_valid:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=f"Adversarial audit failed hallucination check: {', '.join(flags)}"
        )

    # Generate ATS Artifacts
    docx_bytes = generate_ats_docx(draft, candidate_name=user.name)
    pdf_bytes = generate_ats_pdf(draft, candidate_name=user.name)

    # Verify ATS Parsability
    ats_ok, reason = verify_ats_parsability(docx_bytes, pdf_bytes)
    if not ats_ok:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"ATS Parsing Verification failed: {reason}"
        )

    resume_id = f"res_{uuid.uuid4().hex[:12]}"
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M")

    metadata = TailoredResumeMetadata(
        resume_id=resume_id,
        user_id=user.uid,
        job_id=job.job_id,
        company=job.company,
        position=job.title,
        created_at=now_str,
        expiry_date=job.expiry_date,
        ats_keyword_coverage_score=ats_score,
        docx_storage_path=f"/api/v1/tailor/{resume_id}/docx",
        pdf_storage_path=f"/api/v1/tailor/{resume_id}/pdf"
    )

    _TAILORED_METADATA_DB[resume_id] = metadata
    _DOCX_BINARIES[resume_id] = docx_bytes
    _PDF_BINARIES[resume_id] = pdf_bytes

    return metadata

@router.get("/history", response_model=List[TailoredResumeMetadata])
async def list_tailored_history(
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    return [
        m for m in _TAILORED_METADATA_DB.values()
        if m.user_id == user.uid
    ]

@router.get("/{resume_id}/docx")
async def download_docx(
    resume_id: str,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    data = _DOCX_BINARIES.get(resume_id)
    if not data:
        raise HTTPException(status_code=404, detail="DOCX file not found.")
    meta = _TAILORED_METADATA_DB.get(resume_id)
    filename = f"{meta.company.replace(' ', '_')}_{meta.position.replace(' ', '_')}_{meta.expiry_date}.docx" if meta else "resume.docx"

    return Response(
        content=data,
        media_type="application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        headers={"Content-Disposition": f'attachment; filename="{filename}"'}
    )

@router.get("/{resume_id}/pdf")
async def download_pdf(
    resume_id: str,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    data = _PDF_BINARIES.get(resume_id)
    if not data:
        raise HTTPException(status_code=404, detail="PDF file not found.")
    meta = _TAILORED_METADATA_DB.get(resume_id)
    filename = f"{meta.company.replace(' ', '_')}_{meta.position.replace(' ', '_')}_{meta.expiry_date}.pdf" if meta else "resume.pdf"

    return Response(
        content=data,
        media_type="application/pdf",
        headers={"Content-Disposition": f'attachment; filename="{filename}"'}
    )

