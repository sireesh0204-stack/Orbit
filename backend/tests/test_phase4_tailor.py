from app.models.profile import UserProfile, SkillsGroup, WorkExperience, Education
from app.models.job import JobPosting
from app.services.tailor.drafter import draft_tailored_resume
from app.services.tailor.reviewer import audit_and_review_draft
from app.services.tailor.doc_generator import generate_ats_docx, generate_ats_pdf, verify_ats_parsability

def test_drafter_retains_verified_work_history():
    profile = UserProfile(
        user_id="u1",
        target_roles=["Staff Backend Engineer"],
        skills=SkillsGroup(technical=["Go", "Kafka", "PostgreSQL"]),
        work_history=[
            WorkExperience(
                company="Real Employer Corp",
                title="Backend Lead",
                start_date="2020-01",
                end_date="Present",
                bullet_points=["Built streaming pipeline with Kafka", "Managed team"]
            )
        ]
    )
    job = JobPosting(
        job_id="j1",
        title="Staff Engineer",
        company="Stripe",
        location="Remote",
        primary_platform="LinkedIn",
        apply_url="https://example.com",
        posted_date="2026-10-24",
        expiry_date="2026-11-24",
        skills_required=["Kafka"]
    )

    draft = draft_tailored_resume(profile, job)
    assert draft["target_company"] == "Stripe"
    assert draft["work_history"][0]["company"] == "Real Employer Corp"
    # Bullet mentioning Kafka should be prioritized first
    assert "Kafka" in draft["work_history"][0]["bullet_points"][0]

def test_reviewer_flags_invented_employer():
    profile = UserProfile(
        user_id="u1",
        target_roles=["Staff Backend Engineer"],
        work_history=[
            WorkExperience(
                company="Real Employer Corp",
                title="Lead",
                start_date="2020-01",
                end_date="Present"
            )
        ]
    )
    job = JobPosting(
        job_id="j1",
        title="Staff Engineer",
        company="Google",
        location="Remote",
        primary_platform="LinkedIn",
        apply_url="https://example.com",
        posted_date="2026-10-24",
        expiry_date="2026-11-24"
    )

    # Corrupted draft with hallucinated employer
    fake_draft = {
        "work_history": [
            {
                "company": "Fictional Aerospace Inc", # HALLUCINATION
                "title": "Lead",
                "start_date": "2020-01",
                "end_date": "Present",
                "bullet_points": []
            }
        ],
        "skills": {"technical": []}
    }

    is_valid, ats_score, missing, talking, flags = audit_and_review_draft(fake_draft, profile, job)
    assert is_valid is False
    assert any("Unverified employer" in f for f in flags)

def test_doc_generator_produces_parsable_artifacts():
    draft = {
        "candidate_title": "Senior Distributed Systems Engineer",
        "skills": {
            "technical": ["Go", "Kotlin", "Kafka", "PostgreSQL"],
            "tools_and_platforms": ["Docker", "Linux"]
        },
        "work_history": [
            {
                "company": "Stripe",
                "title": "Staff Backend Engineer",
                "start_date": "2021-01",
                "end_date": "Present",
                "bullet_points": ["Architected distributed ledger processing 100K TPS."]
            }
        ],
        "education": [
            {"degree": "B.Tech Computer Science", "institution": "Tech University", "year": "2018"}
        ]
    }

    docx_bytes = generate_ats_docx(draft, candidate_name="Jane Doe")
    pdf_bytes = generate_ats_pdf(draft, candidate_name="Jane Doe")

    assert len(docx_bytes) > 500
    assert len(pdf_bytes) > 500

    ok, reason = verify_ats_parsability(docx_bytes, pdf_bytes)
    assert ok is True

def test_tailor_endpoint_success(client, auth_headers_user1):
    # Sync jobs first
    sync_resp = client.post("/api/v1/jobs/sync", headers=auth_headers_user1)
    job_id = sync_resp.json()[0]["job_id"]

    tailor_resp = client.post(f"/api/v1/tailor?job_id={job_id}", headers=auth_headers_user1)
    assert tailor_resp.status_code == 200
    meta = tailor_resp.json()
    assert "resume_id" in meta
    assert meta["ats_keyword_coverage_score"] >= 0

    resume_id = meta["resume_id"]

    # Test DOCX download
    docx_resp = client.get(f"/api/v1/tailor/{resume_id}/docx", headers=auth_headers_user1)
    assert docx_resp.status_code == 200
    assert len(docx_resp.content) > 500

    # Test PDF download
    pdf_resp = client.get(f"/api/v1/tailor/{resume_id}/pdf", headers=auth_headers_user1)
    assert pdf_resp.status_code == 200
    assert len(pdf_resp.content) > 500

