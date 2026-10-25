import pytest
from fastapi.testclient import TestClient
from app.models.profile import UserProfile, SkillsGroup, WorkExperience, Education
from app.models.job import JobPosting
from app.routers.jobs import _JOBS_CACHE
from app.services.interview_prep import (
    generate_interview_prep_dossier,
    infer_domain,
    build_deterministic_company_intel,
    build_deterministic_system_design,
    build_deterministic_star_stories
)
from tests.conftest import make_test_token

def test_interview_prep_domain_inference():
    assert infer_domain("Stripe", "payment gateway processing") == "Fintech & Payments Infrastructure"
    assert infer_domain("Microsoft Azure", "distributed cloud platform") == "Enterprise Cloud & Distributed Systems"
    assert infer_domain("Amazon", "retail ecommerce catalog cart") == "E-Commerce & High-Scale Marketplaces"
    assert infer_domain("Unknown Corp", "general enterprise application") == "Scalable Software Engineering & Product Platforms"

def test_generate_interview_prep_dossier_full_structure():
    profile = UserProfile(
        user_id="user_test_1",
        target_roles=["Lead Systems Architect"],
        skills=SkillsGroup(technical=["Kotlin", "Coroutines", "System Design", "Kafka", "PostgreSQL"]),
        work_history=[
            WorkExperience(
                company="PhonePe",
                title="Lead Architect",
                start_date="2021-03",
                end_date="Present",
                bullet_points=[
                    "Engineered real-time settlement engine processing 80,000 TPS",
                    "Eliminated transaction deadlocks via monotonic version vectors"
                ],
                technologies_used=["Kotlin", "Kafka", "PostgreSQL"]
            )
        ]
    )

    job = JobPosting(
        job_id="job_stripe_99",
        title="Staff Software Engineer",
        company="Stripe",
        location="Hyderabad",
        primary_platform="LinkedIn",
        apply_url="https://stripe.com/careers/99",
        posted_date="2026-10-25",
        expiry_date="2026-11-25",
        description="Build high-throughput global payment processing infrastructure.",
        skills_required=["Distributed Systems", "PostgreSQL", "Kafka", "High Concurrency"]
    )

    dossier = generate_interview_prep_dossier(
        job=job,
        profile=profile,
        user_email="sireesh0204@gmail.com"
    )

    # 1. Dossier Metadata
    assert dossier.job_id == "job_stripe_99"
    assert dossier.company == "Stripe"
    assert dossier.position == "Staff Software Engineer"

    # 2. Company Intel
    assert dossier.company_intel.company == "Stripe"
    assert dossier.company_intel.domain == "Fintech & Payments Infrastructure"
    assert len(dossier.company_intel.architecture_focus) > 0

    # 3. System Design Scenarios
    assert len(dossier.system_design) >= 2
    assert "Architecture" in dossier.system_design[0].title
    assert len(dossier.system_design[0].key_components) > 0
    assert len(dossier.system_design[0].tradeoffs_and_bottlenecks) > 0

    # 4. Technical Concepts
    assert len(dossier.technical_concepts) >= 4
    topics = [tc.topic for tc in dossier.technical_concepts]
    assert any("Concurrency" in t for t in topics)
    assert any("Caching" in t for t in topics)
    assert any("Database" in t for t in topics)
    assert any("Idempotency" in t for t in topics)

    # 5. Behavioral STAR Stories Grounding (INVARIANT-02: Zero Hallucination)
    assert len(dossier.behavioral_star_stories) >= 1
    star = dossier.behavioral_star_stories[0]
    assert "PhonePe" in star.situation or "PhonePe" in star.action
    assert "80,000 TPS" in star.action or "80,000 TPS" in star.situation
    assert "PhonePe" in star.grounded_experience

    # 6. Reverse Interview Questions
    assert len(dossier.reverse_interview_questions) >= 4

    # 7. NotebookLM Markdown & Podcast Prompt
    assert "Stripe" in dossier.notebooklm_dossier.notebook_title
    assert "# INTERVIEW INTELLIGENCE DOSSIER: Stripe" in dossier.notebooklm_dossier.markdown_content
    assert "Google NotebookLM Audio Deep Dive Instructions" in dossier.notebooklm_dossier.markdown_content
    assert "podcast" in dossier.notebooklm_dossier.audio_deep_dive_prompt.lower()

def test_api_generate_interview_dossier_and_export(client: TestClient, auth_headers_user1: dict):
    # Setup test job in cache
    test_job = JobPosting(
        job_id="test_job_prep_1",
        title="Senior Android Engineer",
        company="Microsoft",
        location="Hyderabad",
        primary_platform="LinkedIn",
        apply_url="https://microsoft.com/careers/1",
        posted_date="2026-10-25",
        expiry_date="2026-11-25",
        description="Build fluent mobile experiences on Android with Kotlin and Jetpack Compose.",
        skills_required=["Kotlin", "Jetpack Compose", "Coroutines", "Room"]
    )
    _JOBS_CACHE[test_job.job_id] = test_job

    # 1. POST generate
    resp = client.post(
        f"/api/v1/interview/{test_job.job_id}/generate",
        headers=auth_headers_user1
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["job_id"] == "test_job_prep_1"
    assert data["company"] == "Microsoft"
    assert "system_design" in data
    assert "technical_concepts" in data
    assert "behavioral_star_stories" in data
    assert "notebooklm_dossier" in data

    # 2. GET dossier
    get_resp = client.get(
        f"/api/v1/interview/{test_job.job_id}",
        headers=auth_headers_user1
    )
    assert get_resp.status_code == 200
    assert get_resp.json()["id"] == data["id"]

    # 3. GET notebooklm markdown export
    md_resp = client.get(
        f"/api/v1/interview/{test_job.job_id}/notebooklm",
        headers=auth_headers_user1
    )
    assert md_resp.status_code == 200
    assert "text/markdown" in md_resp.headers["content-type"]
    assert "attachment; filename=" in md_resp.headers["content-disposition"]
    assert "# INTERVIEW INTELLIGENCE DOSSIER: Microsoft" in md_resp.text

def test_api_interview_dossier_unauthorized_user(client: TestClient):
    unauth_token = make_test_token("hacker_uid", "unauthorized@example.com", "Intruder")
    resp = client.post(
        "/api/v1/interview/any_job_id/generate",
        headers={"Authorization": f"Bearer {unauth_token}"}
    )
    # INVARIANT-01: Must reject non-whitelisted users
    assert resp.status_code == 403

def test_api_interview_dossier_job_not_found(client: TestClient, auth_headers_user1: dict):
    resp = client.post(
        "/api/v1/interview/non_existent_job_9999/generate",
        headers=auth_headers_user1
    )
    assert resp.status_code == 404

