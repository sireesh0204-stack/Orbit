import pytest
from app.services.google_docs_importer import extract_google_file_id, fetch_google_doc_bytes

def test_extract_google_file_id_doc_url():
    url = "https://docs.google.com/document/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit?usp=sharing"
    fid, dtype = extract_google_file_id(url)
    assert fid == "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
    assert dtype == "google_doc"

def test_extract_google_file_id_drive_url():
    url = "https://drive.google.com/file/d/1A2b3C4d5E6f7G8h9I0jKlMnOp12345/view?usp=sharing"
    fid, dtype = extract_google_file_id(url)
    assert fid == "1A2b3C4d5E6f7G8h9I0jKlMnOp12345"
    assert dtype == "google_drive"

def test_extract_google_file_id_id_param():
    url = "https://drive.google.com/open?id=1A2b3C4d5E6f7G8h9I0jKlMnOp12345"
    fid, dtype = extract_google_file_id(url)
    assert fid == "1A2b3C4d5E6f7G8h9I0jKlMnOp12345"
    assert dtype == "google_drive"

def test_extract_google_file_id_invalid_url_raises_error():
    with pytest.raises(ValueError, match="Invalid Google Docs or Google Drive URL"):
        extract_google_file_id("https://randomwebsite.com/mypage")

def test_upload_google_doc_endpoint_success(client, auth_headers_user1):
    doc_url = "https://docs.google.com/document/d/mock_1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit"
    response = client.post(
        "/api/v1/profile/upload-google-doc",
        json={"url": doc_url},
        headers=auth_headers_user1
    )
    assert response.status_code == 200
    data = response.json()
    assert data["user_id"] == "firebase_uid_user_1"
    assert len(data["skills"]["technical"]) > 0

def test_upload_google_doc_endpoint_invalid_url_returns_400(client, auth_headers_user1):
    response = client.post(
        "/api/v1/profile/upload-google-doc",
        json={"url": "not_a_valid_google_doc_url"},
        headers=auth_headers_user1
    )
    assert response.status_code == 400
    assert "Invalid Google Docs" in response.json()["detail"]

def test_upload_google_doc_endpoint_unauthorized_returns_403(client, auth_headers_unauthorized):
    response = client.post(
        "/api/v1/profile/upload-google-doc",
        json={"url": "https://docs.google.com/document/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit"},
        headers=auth_headers_unauthorized
    )
    assert response.status_code == 403

