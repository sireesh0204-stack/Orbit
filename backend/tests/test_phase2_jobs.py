import pytest
from app.models.job import JobPosting, PlatformUrl
from app.models.profile import UserProfile, SkillsGroup
from app.services.deduplicator import deduplicate_job_postings, are_duplicate_jobs
from app.services.fit_scorer import calculate_fit_score, rank_and_filter_jobs

def test_are_duplicate_jobs_detects_near_match():
    job1 = JobPosting(
        job_id="li_1",
        title="Staff Backend Engineer",
        company="Stripe",
        location="Bengaluru",
        primary_platform="LinkedIn",
        apply_url="https://linkedin.com/1",
        posted_date="2026-10-24",
        expiry_date="2026-11-24"
    )
    job2 = JobPosting(
        job_id="nk_1",
        title="Staff Backend Engineer - Infrastructure",
        company="Stripe India Private Ltd",
        location="Bengaluru, Karnataka",
        primary_platform="Naukri",
        apply_url="https://naukri.com/1",
        posted_date="2026-10-24",
        expiry_date="2026-11-24"
    )
    assert are_duplicate_jobs(job1, job2) is True

def test_are_duplicate_jobs_rejects_different_companies():
    job1 = JobPosting(
        job_id="li_1",
        title="Senior Android Engineer",
        company="Uber",
        location="Remote",
        primary_platform="LinkedIn",
        apply_url="https://linkedin.com/1",
        posted_date="2026-10-24",
        expiry_date="2026-11-24"
    )
    job2 = JobPosting(
        job_id="nk_2",
        title="Senior Android Engineer",
        company="Swiggy",
        location="Remote",
        primary_platform="Naukri",
        apply_url="https://naukri.com/2",
        posted_date="2026-10-24",
        expiry_date="2026-11-24"
    )
    assert are_duplicate_jobs(job1, job2) is False

def test_deduplicate_job_postings_merges_source_urls():
    job1 = JobPosting(
        job_id="j1",
        title="Lead Platform Architect",
        company="CRED",
        location="Bengaluru",
        primary_platform="LinkedIn",
        apply_url="https://linkedin.com/cred1",
        source_urls=[PlatformUrl(platform="LinkedIn", url="https://linkedin.com/cred1")],
        posted_date="2026-10-24",
        expiry_date="2026-11-24"
    )
    job2 = JobPosting(
        job_id="j2",
        title="Lead Platform Architect",
        company="CRED India",
        location="Bengaluru",
        primary_platform="Naukri",
        apply_url="https://naukri.com/cred1",
        source_urls=[PlatformUrl(platform="Naukri", url="https://naukri.com/cred1")],
        posted_date="2026-10-24",
        expiry_date="2026-11-24"
    )

    deduped = deduplicate_job_postings([job1, job2])
    assert len(deduped) == 1
    merged = deduped[0]
    platforms = {u.platform for u in merged.source_urls}
    assert "LinkedIn" in platforms
    assert "Naukri" in platforms

def test_calculate_fit_score_high_match():
    profile = UserProfile(
        user_id="u1",
        target_roles=["Staff Backend Engineer"],
        total_years_experience=8.0,
        preferred_cities=["Bengaluru"],
        is_remote_preferred=True,
        skills=SkillsGroup(
            technical=["Go", "Kafka", "Distributed Systems", "PostgreSQL"],
            soft=["Leadership"],
            tools_and_platforms=["Docker", "AWS"]
        )
    )
    job = JobPosting(
        job_id="j1",
        title="Staff Backend Engineer",
        company="Stripe",
        location="Bengaluru",
        primary_platform="LinkedIn",
        apply_url="https://example.com",
        posted_date="2026-10-24",
        expiry_date="2026-11-24",
        skills_required=["Go", "Kafka", "Distributed Systems"]
    )
    score, why = calculate_fit_score(job, profile)
    assert score >= 90
    assert "overlap in core stack" in why

def test_sync_and_rank_endpoint_returns_ranked_jobs(client, auth_headers_user1):
    response = client.post("/api/v1/jobs/sync", headers=auth_headers_user1)
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) >= 1
    assert "match_percentage" in data[0]
    assert "why_it_matches" in data[0]

def test_favorite_toggle_endpoint(client, auth_headers_user1):
    # Ensure jobs exist
    sync_resp = client.post("/api/v1/jobs/sync", headers=auth_headers_user1)
    first_job = sync_resp.json()[0]
    job_id = first_job["job_id"]

    # Toggle favorite
    fav_resp = client.post(f"/api/v1/jobs/{job_id}/favorite", headers=auth_headers_user1)
    assert fav_resp.status_code == 200
    assert fav_resp.json()["is_favorite"] is True

    # Toggle back
    fav_resp2 = client.post(f"/api/v1/jobs/{job_id}/favorite", headers=auth_headers_user1)
    assert fav_resp2.status_code == 200
    assert fav_resp2.json()["is_favorite"] is False

def test_hyderabad_onsite_job_gets_full_location_points():
    profile = UserProfile(
        user_id="u_hyd",
        target_roles=["Staff Backend Engineer"],
        total_years_experience=8.0,
        preferred_cities=["Hyderabad"],
        is_remote_preferred=True,
        skills=SkillsGroup(technical=["Go", "Kafka"])
    )
    job_onsite = JobPosting(
        job_id="hyd_onsite",
        title="Staff Backend Engineer",
        company="Microsoft",
        location="Hyderabad (On-site / HITEC City)",
        primary_platform="LinkedIn",
        apply_url="https://example.com",
        posted_date="2026-10-25",
        expiry_date="2026-11-25",
        skills_required=["Go", "Kafka"]
    )
    score, why = calculate_fit_score(job_onsite, profile)
    assert score == 100
    assert "In-person office reachability in Hyderabad" in why

def test_hyderabad_hybrid_job_gets_full_location_points():
    profile = UserProfile(
        user_id="u_hyd",
        target_roles=["Staff Backend Engineer"],
        total_years_experience=8.0,
        preferred_cities=["Hyderabad"],
        is_remote_preferred=True,
        skills=SkillsGroup(technical=["Go", "Kafka"])
    )
    job_hybrid = JobPosting(
        job_id="hyd_hybrid",
        title="Staff Backend Engineer",
        company="Google",
        location="Hyderabad (Hybrid)",
        primary_platform="LinkedIn",
        apply_url="https://example.com",
        posted_date="2026-10-25",
        expiry_date="2026-11-25",
        skills_required=["Go", "Kafka"]
    )
    score, why = calculate_fit_score(job_hybrid, profile)
    assert score == 100
    assert "Hybrid office flexibility in Hyderabad" in why

def test_other_city_remote_job_gets_full_location_points():
    profile = UserProfile(
        user_id="u_hyd",
        target_roles=["Staff Backend Engineer"],
        total_years_experience=8.0,
        preferred_cities=["Hyderabad"],
        is_remote_preferred=True,
        skills=SkillsGroup(technical=["Go", "Kafka"])
    )
    job_remote = JobPosting(
        job_id="blr_remote",
        title="Staff Backend Engineer",
        company="Stripe",
        location="Bengaluru (Remote)",
        primary_platform="LinkedIn",
        apply_url="https://example.com",
        posted_date="2026-10-25",
        expiry_date="2026-11-25",
        skills_required=["Go", "Kafka"]
    )
    score, why = calculate_fit_score(job_remote, profile)
    assert score == 100
    assert "Remote flexibility" in why

def test_other_city_onsite_job_is_penalized_and_filtered():
    profile = UserProfile(
        user_id="u_hyd",
        target_roles=["Staff Backend Engineer"],
        total_years_experience=8.0,
        preferred_cities=["Hyderabad"],
        is_remote_preferred=True,
        skills=SkillsGroup(technical=["Go", "Kafka"])
    )
    job_pune_onsite = JobPosting(
        job_id="pune_onsite",
        title="Staff Backend Engineer",
        company="Barclays",
        location="Pune (On-site)",
        primary_platform="LinkedIn",
        apply_url="https://example.com",
        posted_date="2026-10-25",
        expiry_date="2026-11-25",
        skills_required=["Go", "Kafka"]
    )
    score, why = calculate_fit_score(job_pune_onsite, profile)
    # Lost 20 location points because it is onsite outside Hyderabad
    assert score == 80
    assert "outside commuting zone" in why

    # And in ranking, it gets filtered out when eligible jobs exist
    ranked = rank_and_filter_jobs([job_pune_onsite], profile)
    # If only ineligible jobs exist, it falls back to them; but when a Hyderabad job exists, Hyderabad is prioritized
    job_hyd = JobPosting(
        job_id="hyd_1",
        title="Staff Backend Engineer",
        company="Microsoft",
        location="Hyderabad (On-site)",
        primary_platform="LinkedIn",
        apply_url="https://example.com",
        posted_date="2026-10-25",
        expiry_date="2026-11-25",
        skills_required=["Go", "Kafka"]
    )
    ranked_both = rank_and_filter_jobs([job_pune_onsite, job_hyd], profile)
    assert len(ranked_both) == 1
    assert ranked_both[0].job_id == "hyd_1"


