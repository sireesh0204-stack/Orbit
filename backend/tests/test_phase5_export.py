import asyncio
from app.models.job import JobPosting
from app.services.export.job_exporter import export_jobs_to_csv, export_jobs_to_html
from app.services.export.drive_uploader import upload_pdf_to_google_drive

def test_export_jobs_to_csv():
    jobs = [
        JobPosting(
            job_id="j1",
            title="Senior Engineer",
            company="Stripe",
            location="Bengaluru",
            primary_platform="LinkedIn",
            apply_url="https://example.com/1",
            posted_date="2026-10-24",
            expiry_date="2026-11-24"
        )
    ]
    csv_bytes, filename = export_jobs_to_csv(jobs)
    assert filename.startswith("jobs_")
    assert filename.endswith(".csv")
    content = csv_bytes.decode("utf-8")
    assert "Senior Engineer" in content
    assert "Stripe" in content

def test_export_jobs_to_html():
    jobs = [
        JobPosting(
            job_id="j1",
            title="Lead Architect",
            company="CRED",
            location="Remote",
            primary_platform="Naukri",
            apply_url="https://example.com/cred",
            posted_date="2026-10-24",
            expiry_date="2026-11-24"
        )
    ]
    html_bytes, filename = export_jobs_to_html(jobs)
    assert filename.startswith("jobs_")
    assert filename.endswith(".html")
    content = html_bytes.decode("utf-8")
    assert "JobPilot Verified Job Archive" in content
    assert "CRED" in content

def test_drive_uploader_standardized_filename():
    result = asyncio.run(upload_pdf_to_google_drive(
        pdf_bytes=b"%PDF-1.4 dummy",
        company="Stripe Payments",
        position="Staff Backend Engineer",
        expiry_date="2026-11-24"
    ))
    assert result["status"] == "success"
    assert result["filename"] == "Stripe_Payments_Staff_Backend_Engineer_2026-11-24.pdf"
    assert "file_id" in result

def test_export_endpoints_csv_and_html(client, auth_headers_user1):
    # Ensure sync
    client.post("/api/v1/jobs/sync", headers=auth_headers_user1)

    csv_resp = client.get("/api/v1/export/jobs-csv", headers=auth_headers_user1)
    assert csv_resp.status_code == 200
    assert "text/csv" in csv_resp.headers["content-type"]
    assert len(csv_resp.content) > 50

    html_resp = client.get("/api/v1/export/jobs-html", headers=auth_headers_user1)
    assert html_resp.status_code == 200
    assert "text/html" in html_resp.headers["content-type"]
    assert len(html_resp.content) > 50

def test_drive_upload_endpoint(client, auth_headers_user1):
    sync_resp = client.post("/api/v1/jobs/sync", headers=auth_headers_user1)
    job_id = sync_resp.json()[0]["job_id"]

    tailor_resp = client.post(f"/api/v1/tailor?job_id={job_id}", headers=auth_headers_user1)
    resume_id = tailor_resp.json()["resume_id"]

    drive_resp = client.post(f"/api/v1/export/drive-upload?resume_id={resume_id}", headers=auth_headers_user1)
    assert drive_resp.status_code == 200
    data = drive_resp.json()
    assert data["status"] == "success"
    assert "file_id" in data
    assert ".pdf" in data["filename"]
