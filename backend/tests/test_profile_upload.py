import io
import pytest
from docx import Document
from pypdf import PdfWriter
from PIL import Image
from app.services.text_extractor import (
    extract_text_from_pdf,
    extract_text_from_docx,
    extract_text_from_txt,
    extract_text_from_image,
    extract_resume_text
)

def make_sample_docx_bytes(text_lines: list[str]) -> bytes:
    doc = Document()
    for line in text_lines:
        doc.add_paragraph(line)
    buf = io.BytesIO()
    doc.save(buf)
    return buf.getvalue()

def make_sample_image_bytes(fmt: str = "PNG") -> bytes:
    img = Image.new("RGB", (200, 100), color="white")
    buf = io.BytesIO()
    img.save(buf, format=fmt)
    return buf.getvalue()

def test_extract_text_from_docx():
    lines = [
        "Jane Doe - Staff Backend Engineer",
        "Company: Stripe",
        "Skills: Python, Go, Kafka, Distributed Systems",
        "Education: Bachelor of Engineering 2017"
    ]
    docx_bytes = make_sample_docx_bytes(lines)
    extracted = extract_text_from_docx(docx_bytes)
    assert "Staff Backend Engineer" in extracted
    assert "Stripe" in extracted
    assert "Kafka" in extracted

def test_extract_text_from_txt():
    txt_content = "Bob - Senior Android Engineer\nSkills: Kotlin, Jetpack Compose, Room\nCompany: Uber"
    extracted = extract_text_from_txt(txt_content.encode("utf-8"))
    assert "Senior Android Engineer" in extracted
    assert "Kotlin" in extracted

def test_extract_text_from_image():
    png_bytes = make_sample_image_bytes("PNG")
    extracted, has_ocr = extract_text_from_image(png_bytes)
    assert extracted is not None
    assert len(extracted) > 0

def test_unsupported_file_extension_raises_error():
    with pytest.raises(ValueError, match="Unsupported resume format"):
        extract_resume_text("resume.mp3", b"fake_mp3_data")

def test_upload_resume_unsupported_format_returns_400(client, auth_headers_user1):
    files = {"file": ("resume.csv", b"col1,col2", "text/csv")}
    response = client.post("/api/v1/profile/upload-resume", files=files, headers=auth_headers_user1)
    assert response.status_code == 400
    assert "Supported formats: PDF, DOCX, TXT, PNG, JPEG" in response.json()["detail"]

def test_upload_resume_empty_file_returns_400(client, auth_headers_user1):
    files = {"file": ("empty.docx", b"", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")}
    response = client.post("/api/v1/profile/upload-resume", files=files, headers=auth_headers_user1)
    assert response.status_code == 400
    assert "empty" in response.json()["detail"]

def test_upload_resume_valid_docx_success(client, auth_headers_user1):
    lines = [
        "Alex Smith - Senior Android Engineer",
        "Location: Bengaluru, India",
        "Company: Acme Payments",
        "Skills: Kotlin, Jetpack Compose, Room, Coroutines, CI/CD",
        "Built real-time audio telemetry engine scaling to 5M users",
        "Education: B.Tech Computer Science 2018"
    ]
    docx_bytes = make_sample_docx_bytes(lines)
    files = {
        "file": (
            "Alex_Smith_Resume.docx",
            docx_bytes,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
    }
    response = client.post("/api/v1/profile/upload-resume", files=files, headers=auth_headers_user1)
    assert response.status_code == 200
    data = response.json()
    assert data["user_id"] == "firebase_uid_user_1"
    assert "Kotlin" in data["skills"]["technical"]
    assert "Bengaluru" in data["preferred_cities"]
    assert len(data["work_history"]) >= 1

def test_upload_resume_valid_txt_success(client, auth_headers_user1):
    txt_content = (
        "Sarah Connor - Staff Backend Engineer\n"
        "Location: Bengaluru\n"
        "Company: Cyberdyne\n"
        "Skills: Python, Go, Kafka, Distributed Systems\n"
        "Education: Master of Science 2016"
    )
    files = {
        "file": ("Sarah_Resume.txt", txt_content.encode("utf-8"), "text/plain")
    }
    response = client.post("/api/v1/profile/upload-resume", files=files, headers=auth_headers_user1)
    assert response.status_code == 200
    data = response.json()
    assert "Staff Backend Engineer" in data["target_roles"]

def test_upload_resume_valid_png_image_success(client, auth_headers_user1):
    png_bytes = make_sample_image_bytes("PNG")
    files = {
        "file": ("scanned_resume.png", png_bytes, "image/png")
    }
    response = client.post("/api/v1/profile/upload-resume", files=files, headers=auth_headers_user1)
    assert response.status_code == 200
    data = response.json()
    assert data["user_id"] == "firebase_uid_user_1"
    assert len(data["skills"]["technical"]) > 0

def test_upload_resume_valid_jpeg_image_success(client, auth_headers_user1):
    jpeg_bytes = make_sample_image_bytes("JPEG")
    files = {
        "file": ("photo_resume.jpg", jpeg_bytes, "image/jpeg")
    }
    response = client.post("/api/v1/profile/upload-resume", files=files, headers=auth_headers_user1)
    assert response.status_code == 200
    data = response.json()
    assert data["user_id"] == "firebase_uid_user_1"

def test_patch_profile_updates_fields(client, auth_headers_user1):
    patch_payload = {
        "preferred_cities": ["Bengaluru", "Hyderabad"],
        "is_remote_preferred": False,
        "total_years_experience": 8.0
    }
    response = client.patch("/api/v1/profile", json=patch_payload, headers=auth_headers_user1)
    assert response.status_code == 200
    data = response.json()
    assert data["preferred_cities"] == ["Bengaluru", "Hyderabad"]
    assert data["is_remote_preferred"] is False
    assert data["total_years_experience"] == 8.0

def test_upload_resume_unauthorized_user_returns_403(client, auth_headers_unauthorized):
    docx_bytes = make_sample_docx_bytes(["Some resume"])
    files = {
        "file": (
            "resume.docx",
            docx_bytes,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
    }
    response = client.post("/api/v1/profile/upload-resume", files=files, headers=auth_headers_unauthorized)
    assert response.status_code == 403
