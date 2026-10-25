from fastapi import APIRouter, Depends, Query, HTTPException, status
from typing import List, Dict
from app.auth import AuthenticatedUser, verify_whitelisted_user
from app.models.job import RankedJob, JobPosting
from app.routers.profile import _PROFILES_DB
from app.models.profile import UserProfile, SkillsGroup
from app.services.scrapers.apify_client import fetch_jobs_from_scrapers
from app.services.deduplicator import deduplicate_job_postings
from app.services.link_verifier import filter_live_jobs
from app.services.fit_scorer import rank_and_filter_jobs
from app.services.expiry_evaluator import evaluate_and_purge_expired_jobs

router = APIRouter(prefix="/api/v1/jobs", tags=["Jobs"])

# In-memory storage for jobs cache
_JOBS_CACHE: Dict[str, JobPosting] = {}

def get_or_create_default_profile(user_id: str) -> UserProfile:
    profile = _PROFILES_DB.get(user_id)
    if not profile:
        profile = UserProfile(
            user_id=user_id,
            target_roles=["Staff Backend Engineer", "Senior Android Engineer"],
            total_years_experience=7.0,
            preferred_cities=["Bengaluru"],
            is_remote_preferred=True,
            skills=SkillsGroup(
                technical=["Kotlin", "Go", "Python", "Kafka", "Jetpack Compose", "Distributed Systems"],
                soft=["Leadership", "Architecture"],
                tools_and_platforms=["Docker", "Linux", "Git", "Kubernetes"]
            ),
            domains=["FinTech", "Distributed Systems"]
        )
        _PROFILES_DB[user_id] = profile
    return profile

@router.post("/sync", response_model=List[RankedJob])
async def sync_and_rank_jobs(
    user: AuthenticatedUser = Depends(verify_whitelisted_user),
    timeframe: str = Query(default="7d", pattern="^(5m|15m|1h|24h|7d|14d)$")
):
    """
    Triggers scraper ingestion -> fuzzy deduplication -> link verification -> fit scoring.
    Supports real-time URL manipulation tricks:
    - timeframe="5m" -> LinkedIn f_TPR=r300 & sortBy=DD (jobs posted 5 minutes ago)
    - timeframe="15m" -> LinkedIn f_TPR=r900
    - timeframe="1h" -> LinkedIn f_TPR=r3600
    """
    raw_jobs = await fetch_jobs_from_scrapers(timeframe=timeframe)
    deduped_jobs = deduplicate_job_postings(raw_jobs)
    verified_jobs = await filter_live_jobs(deduped_jobs)

    # Cache jobs in memory
    for job in verified_jobs:
        _JOBS_CACHE[job.job_id] = job

    profile = get_or_create_default_profile(user.uid)
    ranked = rank_and_filter_jobs(verified_jobs, profile)
    return ranked

@router.get("", response_model=List[JobPosting])
async def list_jobs(
    user: AuthenticatedUser = Depends(verify_whitelisted_user),
    limit: int = Query(default=10, ge=1, le=50)
):
    if not _JOBS_CACHE:
        # Initial cold seed
        raw_jobs = await fetch_jobs_from_scrapers()
        deduped = deduplicate_job_postings(raw_jobs)
        for job in deduped:
            _JOBS_CACHE[job.job_id] = job

    return list(_JOBS_CACHE.values())[:limit]

@router.get("/rank", response_model=List[RankedJob])
async def get_ranked_jobs(
    user: AuthenticatedUser = Depends(verify_whitelisted_user),
    timeframe: str = Query(default="7d", pattern="^(24h|7d|14d)$"),
    min_match: int = Query(default=90, ge=0, le=100)
):
    if not _JOBS_CACHE:
        # Trigger fresh sync
        return await sync_and_rank_jobs(user=user, timeframe=timeframe)

    profile = get_or_create_default_profile(user.uid)
    jobs_list = list(_JOBS_CACHE.values())
    ranked = rank_and_filter_jobs(jobs_list, profile)
    return ranked

@router.post("/purge-expired")
async def purge_expired_jobs(
    user: AuthenticatedUser = Depends(verify_whitelisted_user),
    today_override: str = Query(default=None, pattern="^\\d{4}-\\d{2}-\\d{2}$")
):
    """
    Daily expiration evaluator: marks expired jobs, removes expired jobs from Favorites,
    and purges stale listings.
    """
    purged_count, expired_favs, purged_ids = evaluate_and_purge_expired_jobs(
        _JOBS_CACHE, today_iso=today_override
    )
    return {
        "status": "success",
        "purged_total_jobs": purged_count,
        "expired_favorites_cleared": expired_favs,
        "purged_job_ids": purged_ids
    }

@router.post("/{job_id}/favorite", response_model=JobPosting)
async def toggle_favorite(
    job_id: str,
    user: AuthenticatedUser = Depends(verify_whitelisted_user)
):
    job = _JOBS_CACHE.get(job_id)
    if not job:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Job with ID '{job_id}' not found."
        )
    job.is_favorite = not job.is_favorite
    _JOBS_CACHE[job_id] = job
    return job
