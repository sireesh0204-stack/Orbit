from typing import List, Dict
from rapidfuzz import fuzz
from app.models.job import JobPosting, PlatformUrl

def normalize_string(s: str) -> str:
    return s.strip().lower()

def is_location_match(loc1: str, loc2: str) -> bool:
    l1 = normalize_string(loc1)
    l2 = normalize_string(loc2)
    if "remote" in l1 and "remote" in l2:
        return True
    # City matching
    for city in ["bengaluru", "bangalore", "hyderabad", "pune", "mumbai", "delhi", "gurgaon", "noida"]:
        if city in l1 and city in l2:
            return True
        if (city == "bengaluru" and "bangalore" in l2) or (city == "bangalore" and "bengaluru" in l2):
            return True
    return fuzz.token_set_ratio(l1, l2) >= 80

def are_duplicate_jobs(job_a: JobPosting, job_b: JobPosting) -> bool:
    """
    Deduplication rules:
    - Company similarity >= 85% (using token_set_ratio to handle corporate suffixes like 'India', 'Pvt Ltd')
    - Title similarity >= 80% (using token_set_ratio to handle team appendages like '- Infrastructure')
    - Location match
    """
    comp_score = max(
        fuzz.token_set_ratio(normalize_string(job_a.company), normalize_string(job_b.company)),
        fuzz.token_sort_ratio(normalize_string(job_a.company), normalize_string(job_b.company))
    )
    if comp_score < 85:
        return False

    title_score = max(
        fuzz.token_set_ratio(normalize_string(job_a.title), normalize_string(job_b.title)),
        fuzz.token_sort_ratio(normalize_string(job_a.title), normalize_string(job_b.title))
    )
    if title_score < 80:
        return False

    return is_location_match(job_a.location, job_b.location)

def deduplicate_job_postings(raw_jobs: List[JobPosting]) -> List[JobPosting]:
    """
    Fuzzy deduplication across LinkedIn, Naukri, and Indeed.
    Combines duplicates into one card, merging their source URLs.
    """
    deduped: List[JobPosting] = []

    for job in raw_jobs:
        matched = False
        for existing in deduped:
            if are_duplicate_jobs(job, existing):
                # Merge source URLs
                existing_urls = {u.url: u for u in existing.source_urls}
                for u in job.source_urls:
                    if u.url not in existing_urls:
                        existing.source_urls.append(u)
                        existing_urls[u.url] = u

                # Also add the job's primary apply_url if missing
                if job.apply_url not in existing_urls:
                    existing.source_urls.append(PlatformUrl(platform=job.primary_platform, url=job.apply_url))

                # Merge skills
                all_skills = list(set(existing.skills_required + job.skills_required))
                existing.skills_required = all_skills
                matched = True
                break

        if not matched:
            # Ensure primary source url is in source_urls
            if not any(u.url == job.apply_url for u in job.source_urls):
                job.source_urls.append(PlatformUrl(platform=job.primary_platform, url=job.apply_url))
            deduped.append(job)

    return deduped
