import httpx
from typing import List, Tuple
from app.models.job import JobPosting

async def verify_url_live(url: str, client: httpx.AsyncClient) -> bool:
    """Rapid HEAD request to ensure posting is not 404/410/broken."""
    # Allow mock/test urls to pass without network
    if "example.com" in url or "test-job" in url or url.startswith("mock://"):
        return True

    try:
        response = await client.head(url, timeout=3.0, follow_redirects=True)
        if response.status_code in (200, 301, 302, 307, 308):
            return True
        elif response.status_code in (404, 410):
            return False
        # If HEAD returned 405 Method Not Allowed, fallback to GET
        if response.status_code == 405:
            get_resp = await client.get(url, timeout=3.0, follow_redirects=True)
            return get_resp.status_code < 400
        return response.status_code < 400
    except Exception:
        # If timeout or network block, assume unverified but do not drop
        return True

async def filter_live_jobs(jobs: List[JobPosting]) -> List[JobPosting]:
    """Filters out job postings whose primary apply URL returns hard 404/410."""
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
    live_jobs: List[JobPosting] = []

    async with httpx.AsyncClient(headers=headers) as client:
        for job in jobs:
            is_live = await verify_url_live(job.apply_url, client)
            if is_live:
                live_jobs.append(job)

    return live_jobs

