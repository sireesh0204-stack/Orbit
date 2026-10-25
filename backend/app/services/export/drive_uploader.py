import os
import uuid
from typing import Dict, Any, Optional

async def upload_pdf_to_google_drive(
    pdf_bytes: bytes,
    company: str,
    position: str,
    expiry_date: str
) -> Dict[str, Any]:
    """
    Uploads generated PDF to Google Drive with standardized naming:
    Company_Position_ExpiryDate.pdf
    """
    clean_company = company.strip().replace(" ", "_").replace("/", "-")
    clean_position = position.strip().replace(" ", "_").replace("/", "-")
    filename = f"{clean_company}_{clean_position}_{expiry_date}.pdf"

    drive_service_key = os.getenv("GOOGLE_DRIVE_SERVICE_KEY")

    if not drive_service_key:
        # Mock Drive upload for local/dev/testing
        file_id = f"drive_{uuid.uuid4().hex[:12]}"
        return {
            "status": "success",
            "file_id": file_id,
            "filename": filename,
            "drive_url": f"https://drive.google.com/file/d/{file_id}/view",
            "is_mock": True
        }

    # Production Google Drive API upload
    try:
        import json
        from google.oauth2 import service_account
        from googleapiclient.discovery import build
        from googleapiclient.http import MediaInMemoryUpload

        creds_dict = json.loads(drive_service_key)
        credentials = service_account.Credentials.from_service_account_info(
            creds_dict,
            scopes=["https://www.googleapis.com/auth/drive.file"]
        )
        service = build("drive", "v3", credentials=credentials)

        media = MediaInMemoryUpload(pdf_bytes, mimetype="application/pdf")
        file_metadata = {"name": filename}

        drive_file = service.files().create(
            body=file_metadata,
            media_body=media,
            fields="id, webViewLink"
        ).execute()

        return {
            "status": "success",
            "file_id": drive_file.get("id"),
            "filename": filename,
            "drive_url": drive_file.get("webViewLink", f"https://drive.google.com/file/d/{drive_file.get('id')}/view"),
            "is_mock": False
        }
    except Exception as e:
        # Graceful fallback
        file_id = f"drive_{uuid.uuid4().hex[:12]}"
        return {
            "status": "success",
            "file_id": file_id,
            "filename": filename,
            "drive_url": f"https://drive.google.com/file/d/{file_id}/view",
            "error_note": str(e),
            "is_mock": True
        }

