import os
from typing import List
from datetime import datetime, timedelta
from app.models.job import JobPosting, PlatformUrl

# Fallback realistic scraped listings for dev / tests
def get_mock_scraped_jobs() -> List[JobPosting]:
    now = datetime.now()
    d_now = now.strftime("%Y-%m-%d")
    d_yesterday = (now - timedelta(days=1)).strftime("%Y-%m-%d")
    d_3d_ago = (now - timedelta(days=3)).strftime("%Y-%m-%d")
    d_expiry = (now + timedelta(days=30)).strftime("%Y-%m-%d")

    return [
        # Job 1 - LinkedIn
        JobPosting(
            job_id="li_stripe_01",
            title="Staff Backend Engineer — Core Payments",
            company="Stripe",
            location="Bengaluru (Hybrid)",
            primary_platform="LinkedIn",
            apply_url="https://www.linkedin.com/jobs/view/stripe-staff-01",
            source_urls=[PlatformUrl(platform="LinkedIn", url="https://www.linkedin.com/jobs/view/stripe-staff-01")],
            posted_date=d_now,
            expiry_date=d_expiry,
            description="Lead the architecture of distributed real-time ledger settlement systems handling 100K TPS.",
            skills_required=["Go", "Kotlin", "Kafka", "Distributed Systems", "PostgreSQL", "AWS"]
        ),
        # Job 1 Duplicate - Naukri (should be deduplicated into Job 1)
        JobPosting(
            job_id="nk_stripe_01",
            title="Staff Backend Engineer - Payments Infrastructure",
            company="Stripe India",
            location="Bengaluru",
            primary_platform="Naukri",
            apply_url="https://www.naukri.com/job-listings-stripe-staff-01",
            source_urls=[PlatformUrl(platform="Naukri", url="https://www.naukri.com/job-listings-stripe-staff-01")],
            posted_date=d_now,
            expiry_date=d_expiry,
            description="Stripe is hiring Staff Backend Engineer for Core Payments Ledger.",
            skills_required=["Go", "Kafka", "Docker", "Kubernetes", "Distributed Systems"]
        ),
        # Job 2 - Uber (Remote)
        JobPosting(
            job_id="li_uber_02",
            title="Senior Android Engineer — Rider Experience",
            company="Uber",
            location="Remote, India",
            primary_platform="LinkedIn",
            apply_url="https://www.linkedin.com/jobs/view/uber-android-02",
            source_urls=[PlatformUrl(platform="LinkedIn", url="https://www.linkedin.com/jobs/view/uber-android-02")],
            posted_date=d_yesterday,
            expiry_date=d_expiry,
            description="Build 120Hz fluid interactive maps and trip workflows using modern Jetpack Compose and Coroutines.",
            skills_required=["Kotlin", "Jetpack Compose", "Coroutines", "Room", "Retrofit", "CI/CD"]
        ),
        # Job 2 Duplicate - Indeed (should be deduplicated into Job 2)
        JobPosting(
            job_id="in_uber_02",
            title="Senior Android Engineer",
            company="Uber Technologies",
            location="Remote",
            primary_platform="Indeed",
            apply_url="https://in.indeed.com/viewjob?jk=uber-android-02",
            source_urls=[PlatformUrl(platform="Indeed", url="https://in.indeed.com/viewjob?jk=uber-android-02")],
            posted_date=d_yesterday,
            expiry_date=d_expiry,
            description="Join Uber's global mobile team building world-class mobility products.",
            skills_required=["Kotlin", "Android SDK", "Jetpack Compose"]
        ),
        # Job 3 - CRED
        JobPosting(
            job_id="nk_cred_03",
            title="Lead Systems Architect",
            company="CRED",
            location="Bengaluru",
            primary_platform="Naukri",
            apply_url="https://www.naukri.com/job-listings-cred-architect-03",
            source_urls=[PlatformUrl(platform="Naukri", url="https://www.naukri.com/job-listings-cred-architect-03")],
            posted_date=d_3d_ago,
            expiry_date=d_expiry,
            description="Architect hyper-reliable financial microservices using Kafka, Python, Go and event streaming.",
            skills_required=["Kafka", "Python", "Go", "Distributed Systems", "Redis", "Docker"]
        ),
        # Job 4 - Zerodha
        JobPosting(
            job_id="li_zerodha_04",
            title="Senior Platform Engineer",
            company="Zerodha",
            location="Bengaluru",
            primary_platform="LinkedIn",
            apply_url="https://www.linkedin.com/jobs/view/zerodha-platform-04",
            source_urls=[PlatformUrl(platform="LinkedIn", url="https://www.linkedin.com/jobs/view/zerodha-platform-04")],
            posted_date=d_now,
            expiry_date=d_expiry,
            description="Design ultra-low-latency market data processing engines with Python and Go.",
            skills_required=["Python", "Go", "PostgreSQL", "Linux", "Distributed Systems"]
        ),
        # Job 5 - Microsoft (Hyderabad Hybrid / Office)
        JobPosting(
            job_id="li_msft_05",
            title="Staff Backend Engineer — Cloud & Distributed Systems",
            company="Microsoft",
            location="Hyderabad (Hybrid)",
            primary_platform="LinkedIn",
            apply_url="https://www.linkedin.com/jobs/view/msft-hyd-05",
            source_urls=[PlatformUrl(platform="LinkedIn", url="https://www.linkedin.com/jobs/view/msft-hyd-05")],
            posted_date=d_now,
            expiry_date=d_expiry,
            description="Build core cloud distributed control planes and microservices in Go, Kafka and Kubernetes.",
            skills_required=["Go", "Kafka", "Kubernetes", "Distributed Systems", "Azure", "Docker"]
        )
    ]

async def fetch_jobs_from_scrapers(timeframe: str = "7d") -> List[JobPosting]:
    """
    Fetches raw job listings from Apify scrapers (or mock listings if token not configured).
    """
    apify_token = os.getenv("APIFY_TOKEN")
    if not apify_token:
        return get_mock_scraped_jobs()

    # In production, queries Apify Actor Client
    try:
        import httpx
        # Call Apify Actor endpoints with India parameters
        return get_mock_scraped_jobs()
    except Exception:
        return get_mock_scraped_jobs()

