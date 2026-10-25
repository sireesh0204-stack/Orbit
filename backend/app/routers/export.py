from fastapi import APIRouter, Depends, HTTPException, status, Query, Response
from fastapi.responses import Response
from app.auth import AuthenticatedUser, verify_whitelisted_user
from app.routers.jobs import _JOBS_CACHE
from app.routers.tailor import _TAILORED_METADATA_DB, _PDF_BINARIES
from app.services.export.drive_uploader import upload_pdf_to_google_drive
from app.services.export.job_exporter import export_jobs_to_csv, export_jobs_to_html

router = APIRouter(prefix="/api/v1/export", tags=["Export"])

@router.post("/drive-upload")
async def upload_tailored_resume_to_drive(
    resume_id: str,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    metadata = _TAILORED_METADATA_DB.get(resume_id)
    if not metadata:
        raise HTTPException(status_code=404, detail="Tailored resume not found.")

    pdf_bytes = _PDF_BINARIES.get(resume_id)
    if not pdf_bytes:
        raise HTTPException(status_code=404, detail="Compiled PDF binary not found.")

    result = await upload_pdf_to_google_drive(
        pdf_bytes=pdf_bytes,
        company=metadata.company,
        position=metadata.position,
        expiry_date=metadata.expiry_date
    )

    # Save drive file id to metadata
    metadata.google_drive_file_id = result.get("file_id")
    _TAILORED_METADATA_DB[resume_id] = metadata

    return result

@router.get("/jobs-csv")
async def download_jobs_csv(
    only_favorites: bool = Query(default=False),
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    jobs = list(_JOBS_CACHE.values())
    if only_favorites:
        jobs = [j for j in jobs if j.is_favorite]

    csv_bytes, filename = export_jobs_to_csv(jobs)
    return Response(
        content=csv_bytes,
        media_type="text/csv",
        headers={"Content-Disposition": f'attachment; filename="{filename}"'}
    )

@router.get("/jobs-html")
async def download_jobs_html(
    only_favorites: bool = Query(default=False),
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    jobs = list(_JOBS_CACHE.values())
    if only_favorites:
        jobs = [j for j in jobs if j.is_favorite]

    html_bytes, filename = export_jobs_to_html(jobs)
    return Response(
        content=html_bytes,
        media_type="text/html",
        headers={"Content-Disposition": f'attachment; filename="{filename}"'}
    )

