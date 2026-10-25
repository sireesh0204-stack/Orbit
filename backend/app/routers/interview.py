from typing import Dict
from fastapi import APIRouter, Depends, HTTPException, status, Response
from app.auth import AuthenticatedUser, verify_whitelisted_user
from app.models.interview import InterviewPrepDossier
from app.routers.jobs import _JOBS_CACHE, get_or_create_default_profile
from app.services.interview_prep import generate_interview_prep_dossier

router = APIRouter(prefix="/api/v1/interview", tags=["Interview Prep"])

# In-memory dossier cache: key = f"{user_uid}_{job_id}"
_INTERVIEW_DOSSIER_CACHE: Dict[str, InterviewPrepDossier] = {}

@router.post("/{job_id}/generate", response_model=InterviewPrepDossier)
async def generate_interview_dossier(
    job_id: str,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    """
    Generates a full interview preparation dossier including:
    - Company Intelligence & Domain Breakdown
    - System Design Scenarios with CAP/latency tradeoffs
    - High-frequency technical concepts & common traps
    - Zero-hallucination behavioral STAR stories grounded in candidate's work history
    - High-leverage reverse interview questions
    - Markdown export for 1-click Google NotebookLM Audio Deep Dive podcasting
    """
    job = _JOBS_CACHE.get(job_id)
    if not job:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Target job with ID '{job_id}' not found."
        )

    profile = get_or_create_default_profile(user.uid)
    dossier = generate_interview_prep_dossier(
        job=job,
        profile=profile,
        user_email=user.email
    )

    cache_key = f"{user.uid}_{job_id}"
    _INTERVIEW_DOSSIER_CACHE[cache_key] = dossier
    return dossier

@router.get("/{job_id}", response_model=InterviewPrepDossier)
async def get_interview_dossier(
    job_id: str,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    """
    Retrieves the interview preparation dossier for the specified job.
    If not yet generated, generates it on the fly.
    """
    cache_key = f"{user.uid}_{job_id}"
    if cache_key in _INTERVIEW_DOSSIER_CACHE:
        return _INTERVIEW_DOSSIER_CACHE[cache_key]

    return await generate_interview_dossier(job_id=job_id, user=user)

@router.get("/{job_id}/notebooklm", response_class=Response)
async def export_notebooklm_markdown(
    job_id: str,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    """
    Exports the complete Markdown briefing file ready for 1-click import into Google NotebookLM.
    """
    cache_key = f"{user.uid}_{job_id}"
    dossier = _INTERVIEW_DOSSIER_CACHE.get(cache_key)
    if not dossier:
        dossier = await generate_interview_dossier(job_id=job_id, user=user)

    filename = f"{dossier.company}_{dossier.position}_NotebookLM_Prep.md".replace(" ", "_")
    return Response(
        content=dossier.notebooklm_dossier.markdown_content,
        media_type="text/markdown; charset=utf-8",
        headers={
            "Content-Disposition": f'attachment; filename="{filename}"'
        }
    )

