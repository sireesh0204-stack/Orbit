from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from typing import Dict, Any
from app.auth import AuthenticatedUser, verify_whitelisted_user
from app.models.profile import UserProfile, UserProfilePatch
from app.services.text_extractor import extract_resume_text
from app.services.resume_parser import parse_resume_to_profile
from app.services.google_docs_importer import fetch_google_doc_bytes
from pydantic import BaseModel, Field

class GoogleDocImportRequest(BaseModel):
    url: str = Field(description="Google Docs or Google Drive share URL / File ID")

router = APIRouter(prefix="/api/v1/profile", tags=["Profile"])

# In-memory storage for user profiles (per user_id)
_PROFILES_DB: Dict[str, UserProfile] = {}

@router.get("", response_model=UserProfile)
async def get_profile(
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    profile = _PROFILES_DB.get(user.uid)
    if not profile:
        return UserProfile(
            user_id=user.uid,
            target_roles=[],
            total_years_experience=0.0,
            preferred_cities=[],
            is_remote_preferred=True
        )
    return profile

@router.patch("", response_model=UserProfile)
async def patch_profile(
    patch: UserProfilePatch,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    current = _PROFILES_DB.get(user.uid) or UserProfile(
        user_id=user.uid,
        target_roles=[],
        total_years_experience=0.0,
        preferred_cities=[],
        is_remote_preferred=True
    )
    
    update_data = patch.model_dump(exclude_unset=True)
    updated_profile = current.model_copy(update=update_data)
    _PROFILES_DB[user.uid] = updated_profile
    return updated_profile

ALLOWED_RESUME_EXTENSIONS = (".pdf", ".docx", ".txt", ".png", ".jpg", ".jpeg")

@router.post("/upload-resume", response_model=UserProfile)
async def upload_resume(
    file: UploadFile = File(...),
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    filename = file.filename or ""
    lower = filename.lower()
    if not any(lower.endswith(ext) for ext in ALLOWED_RESUME_EXTENSIONS):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid file format. Supported formats: PDF, DOCX, TXT, PNG, JPEG."
        )

    file_bytes = await file.read()
    if not file_bytes:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Uploaded file is empty."
        )

    try:
        raw_text, _ = extract_resume_text(filename, file_bytes)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )

    if not raw_text.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Could not extract any readable text from the uploaded document."
        )

    profile = await parse_resume_to_profile(raw_text, user.uid)
    _PROFILES_DB[user.uid] = profile
    return profile

@router.post("/upload-google-doc", response_model=UserProfile)
async def upload_from_google_docs(
    payload: GoogleDocImportRequest,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    """
    Imports and structures a resume directly from a Google Docs or Google Drive share link.
    """
    try:
        file_bytes, filename = await fetch_google_doc_bytes(payload.url)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )

    try:
        raw_text, _ = extract_resume_text(filename, file_bytes)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )

    profile = await parse_resume_to_profile(raw_text, user.uid)
    _PROFILES_DB[user.uid] = profile
    return profile
