from datetime import datetime
from typing import Dict, Tuple, List
from app.models.job import JobPosting

def evaluate_and_purge_expired_jobs(
    jobs_cache: Dict[str, JobPosting],
    today_iso: str = None
) -> Tuple[int, int, List[str]]:
    """
    Evaluates jobs against current ISO date.
    Purges stale expired jobs from the registry and removes expired items from favorites.
    Returns (purged_total_count, expired_favorites_count, purged_job_ids).
    """
    if not today_iso:
        today_iso = datetime.now().strftime("%Y-%m-%d")

    purged_ids: List[str] = []
    expired_favs = 0

    job_ids = list(jobs_cache.keys())
    for jid in job_ids:
        job = jobs_cache[jid]
        if job.expiry_date < today_iso:
            if job.is_favorite:
                expired_favs += 1
            purged_ids.append(jid)
            del jobs_cache[jid]

    return len(purged_ids), expired_favs, purged_ids

