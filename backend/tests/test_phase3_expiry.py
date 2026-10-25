from app.models.job import JobPosting
from app.services.expiry_evaluator import evaluate_and_purge_expired_jobs

def test_evaluate_and_purge_expired_jobs():
    cache = {
        "active_job": JobPosting(
            job_id="active_job",
            title="Senior Engineer",
            company="Google",
            location="Remote",
            primary_platform="LinkedIn",
            apply_url="https://example.com/active",
            posted_date="2026-10-24",
            expiry_date="2026-11-24",
            is_favorite=True
        ),
        "expired_job": JobPosting(
            job_id="expired_job",
            title="Old Engineer",
            company="Yahoo",
            location="Remote",
            primary_platform="Indeed",
            apply_url="https://example.com/old",
            posted_date="2026-09-01",
            expiry_date="2026-10-01",
            is_favorite=True
        )
    }

    purged, expired_favs, ids = evaluate_and_purge_expired_jobs(cache, today_iso="2026-10-24")
    assert purged == 1
    assert expired_favs == 1
    assert ids == ["expired_job"]
    assert "active_job" in cache
    assert "expired_job" not in cache

def test_purge_expired_endpoint(client, auth_headers_user1):
    # Sync first
    client.post("/api/v1/jobs/sync", headers=auth_headers_user1)

    # Call purge with future override date to simulate passing time
    response = client.post(
        "/api/v1/jobs/purge-expired?today_override=2026-12-31",
        headers=auth_headers_user1
    )
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"
    assert data["purged_total_jobs"] >= 1

