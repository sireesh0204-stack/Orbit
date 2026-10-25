import pytest
from pydantic import ValidationError
from app.models.profile import UserProfile, WorkExperience, Education, SkillsGroup
from app.models.job import JobPosting, RankedJob, PlatformUrl

def test_user_profile_schema():
    profile = UserProfile(
        user_id="user_123",
        target_roles=["Staff Backend Engineer"],
        total_years_experience=7.5,
        preferred_cities=["Bengaluru"],
        is_remote_preferred=True,
        skills=SkillsGroup(
            technical=["Kotlin", "Python", "Kafka"],
            soft=["Leadership"],
            tools_and_platforms=["GCP", "Kubernetes"]
        ),
        domains=["FinTech", "Distributed Systems"],
        work_history=[
            WorkExperience(
                company="Acme Corp",
                title="Lead Engineer",
                start_date="2021-01",
                end_date="Present",
                bullet_points=["Architected real-time stream processing platform"],
                technologies_used=["Kafka", "Python"]
            )
        ],
        education=[
            Education(
                institution="National Tech Institute",
                degree="B.Tech Computer Science",
                year="2017"
            )
        ]
    )
    assert profile.user_id == "user_123"
    assert profile.total_years_experience == 7.5
    assert len(profile.work_history) == 1
    assert profile.work_history[0].end_date == "Present"

def test_ranked_job_schema():
    job = RankedJob(
        job_id="job_abc_1",
        title="Senior Android Engineer",
        company="Stripe",
        location="Bengaluru, India",
        primary_platform="LinkedIn",
        apply_url="https://linkedin.com/jobs/view/123",
        source_urls=[
            PlatformUrl(platform="LinkedIn", url="https://linkedin.com/jobs/view/123"),
            PlatformUrl(platform="Naukri", url="https://naukri.com/job/456")
        ],
        posted_date="2026-10-24",
        expiry_date="2026-11-23",
        is_estimated_expiry=True,
        description="Build high-performance payments UI",
        skills_required=["Kotlin", "Jetpack Compose", "Coroutines"],
        match_percentage=94,
        why_it_matches="Your 7+ years of Android and high-refresh Compose work matches Stripe mobile requirements."
    )
    assert job.match_percentage == 94
    assert len(job.source_urls) == 2

def test_ranked_job_match_percentage_bounds():
    with pytest.raises(ValidationError):
        RankedJob(
            job_id="j1",
            title="Dev",
            company="Co",
            location="Remote",
            primary_platform="Indeed",
            apply_url="https://example.com",
            posted_date="2026-10-24",
            expiry_date="2026-11-24",
            match_percentage=105, # Out of range > 100
            why_it_matches="Too high"
        )

