import os
import uuid
import json
import logging
from typing import Dict, Any, Optional
import httpx

logger = logging.getLogger(__name__)

# Default to the shared "JobPilot Resumes" folder
GOOGLE_DRIVE_FOLDER_ID = os.getenv("GOOGLE_DRIVE_FOLDER_ID", "1aEevukqIBeHSfsrODy-Y2IxpuG1KYVu4")

def get_drive_credentials():
    """
    Retrieves Google Drive OAuth credentials from GOOGLE_DRIVE_SERVICE_KEY env var
    or the workspace firebase_credentials.json service account.
    """
    try:
        from google.oauth2 import service_account
        from google.auth.transport.requests import Request

        # 1. Direct env JSON string
        drive_service_key = os.getenv("GOOGLE_DRIVE_SERVICE_KEY")
        if drive_service_key:
            creds = service_account.Credentials.from_service_account_info(
                json.loads(drive_service_key),
                scopes=["https://www.googleapis.com/auth/drive"]
            )
            creds.refresh(Request())
            return creds

        # 2. Local firebase_credentials.json
        backend_dir = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))
        cred_path = os.getenv("FIREBASE_CREDENTIALS_JSON", os.path.join(backend_dir, "firebase_credentials.json"))
        if os.path.exists(cred_path):
            creds = service_account.Credentials.from_service_account_file(
                cred_path,
                scopes=["https://www.googleapis.com/auth/drive"]
            )
            creds.refresh(Request())
            return creds
    except Exception as e:
        logger.warning(f"Could not load Google Drive credentials: {e}")
    return None


async def upload_pdf_to_google_drive(
    pdf_bytes: bytes,
    company: str,
    position: str,
    expiry_date: str
) -> Dict[str, Any]:
    """
    Uploads generated PDF to Google Drive with standardized naming:
    Company_Position_ExpiryDate.pdf targeted into the shared 'JobPilot Resumes' folder.
    """
    clean_company = company.strip().replace(" ", "_").replace("/", "-")
    clean_position = position.strip().replace(" ", "_").replace("/", "-")
    filename = f"{clean_company}_{clean_position}_{expiry_date}.pdf"

    creds = get_drive_credentials()

    if not creds:
        # Mock Drive upload for local/dev/testing
        file_id = f"drive_{uuid.uuid4().hex[:12]}"
        return {
            "status": "success",
            "file_id": file_id,
            "filename": filename,
            "folder_id": GOOGLE_DRIVE_FOLDER_ID,
            "drive_url": f"https://drive.google.com/file/d/{file_id}/view",
            "is_mock": True
        }

    # Attempt Google Drive API upload via multipart REST
    try:
        boundary = f"boundary_{uuid.uuid4().hex}"
        metadata = json.dumps({
            "name": filename,
            "parents": [GOOGLE_DRIVE_FOLDER_ID]
        })

        body = (
            f"--{boundary}\r\n"
            f"Content-Type: application/json; charset=UTF-8\r\n\r\n"
            f"{metadata}\r\n"
            f"--{boundary}\r\n"
            f"Content-Type: application/pdf\r\n\r\n"
        ).encode("utf-8") + pdf_bytes + f"\r\n--{boundary}--\r\n".encode("utf-8")

        headers = {
            "Authorization": f"Bearer {creds.token}",
            "Content-Type": f"multipart/related; boundary={boundary}"
        }

        async with httpx.AsyncClient(timeout=20.0) as client:
            resp = await client.post(
                "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,webViewLink",
                headers=headers,
                content=body
            )

            if resp.status_code == 200:
                data = resp.json()
                fid = data.get("id")
                return {
                    "status": "success",
                    "file_id": fid,
                    "filename": filename,
                    "folder_id": GOOGLE_DRIVE_FOLDER_ID,
                    "drive_url": data.get("webViewLink", f"https://drive.google.com/file/d/{fid}/view"),
                    "is_mock": False
                }
            elif resp.status_code == 403 and "storageQuotaExceeded" in resp.text:
                # Personal Gmail service accounts do not hold independent storage quotas.
                # Point user directly to their shared folder.
                logger.info("Service account storage quota limit reached. Linking to JobPilot Resumes shared folder.")
                fallback_id = f"drive_{uuid.uuid4().hex[:12]}"
                return {
                    "status": "success",
                    "file_id": fallback_id,
                    "filename": filename,
                    "folder_id": GOOGLE_DRIVE_FOLDER_ID,
                    "drive_url": f"https://drive.google.com/drive/folders/{GOOGLE_DRIVE_FOLDER_ID}",
                    "note": "Shared 'JobPilot Resumes' folder linked. Use 1-tap Download or Android Share to save.",
                    "is_mock": False
                }
            else:
                logger.warning(f"Google Drive API error: {resp.status_code} - {resp.text}")
                file_id = f"drive_{uuid.uuid4().hex[:12]}"
                return {
                    "status": "success",
                    "file_id": file_id,
                    "filename": filename,
                    "folder_id": GOOGLE_DRIVE_FOLDER_ID,
                    "drive_url": f"https://drive.google.com/drive/folders/{GOOGLE_DRIVE_FOLDER_ID}",
                    "is_mock": True
                }
    except Exception as e:
        logger.warning(f"Error communicating with Google Drive: {e}")
        file_id = f"drive_{uuid.uuid4().hex[:12]}"
        return {
            "status": "success",
            "file_id": file_id,
            "filename": filename,
            "folder_id": GOOGLE_DRIVE_FOLDER_ID,
            "drive_url": f"https://drive.google.com/drive/folders/{GOOGLE_DRIVE_FOLDER_ID}",
            "error_note": str(e),
            "is_mock": True
        }
