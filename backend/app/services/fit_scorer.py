from typing import List, Tuple
from app.models.profile import UserProfile
from app.models.job import JobPosting, RankedJob

def is_location_eligible(location: str, allowed_office_cities: List[str] = None) -> bool:
    """
    User Location Policy:
    - Hyderabad can be Office (On-site), Hybrid, or Remote.
    - All other locations must strictly be Remote.
    """
    if allowed_office_cities is None:
        allowed_office_cities = ["hyderabad", "secunderabad", "cyberabad", "hitec city"]
    
    loc = location.lower()
    # 1. Any Remote position is eligible regardless of city
    if any(r in loc for r in ["remote", "work from home", "wfh", "anywhere"]):
        return True
    
    # 2. Hyderabad (or explicitly allowed office cities) can be office, hybrid, or remote
    if any(city in loc for city in allowed_office_cities):
        return True
        
    return False

def calculate_fit_score(job: JobPosting, profile: UserProfile) -> Tuple[int, str]:
    """
    Objective match scoring against verified candidate profile.
    Returns (match_percentage: int 0-100, why_it_matches: str).
    """
    score = 0
    rationales: List[str] = []

    # 1. Technical Skills overlap (up to 50 pts)
    candidate_skills = {s.lower() for s in profile.skills.technical + profile.skills.tools_and_platforms}
    job_skills = {s.lower() for s in job.skills_required}
    matched_skills = [s for s in job.skills_required if s.lower() in candidate_skills]

    if job_skills:
        skill_ratio = len(matched_skills) / len(job_skills)
        skill_points = int(skill_ratio * 50)
        score += skill_points
        if matched_skills:
            rationales.append(f"Strong overlap in core stack ({', '.join(matched_skills[:3])}).")
    else:
        score += 35

    # 2. Title & Role relevance (up to 30 pts)
    job_title_lower = job.title.lower()
    role_matched = any(role.lower() in job_title_lower or job_title_lower in role.lower() for role in profile.target_roles)
    if role_matched:
        score += 30
        rationales.append(f"Target role alignment with your {profile.target_roles[0] if profile.target_roles else 'senior'} profile.")
    else:
        # Check domain or partial match
        domain_matched = any(dom.lower() in job.description.lower() for dom in profile.domains)
        if domain_matched:
            score += 20
            rationales.append("Domain match in distributed systems.")
        else:
            score += 15

    # 3. Location & Work Mode match (up to 20 pts)
    # Policy: Hyderabad can be Office, Hybrid, or Remote. All other cities must be Remote.
    job_loc_lower = job.location.lower()
    is_remote = any(r in job_loc_lower for r in ["remote", "work from home", "wfh", "anywhere"])
    is_hyderabad = any(h in job_loc_lower for h in ["hyderabad", "secunderabad", "cyberabad", "hitec city"])
    
    # Check if city matches candidate profile preferred cities
    user_preferred_cities = [c.lower() for c in profile.preferred_cities if "remote" not in c.lower()]
    is_preferred_city = any(c in job_loc_lower for c in user_preferred_cities)

    if is_remote:
        score += 20
        rationales.append("100% Remote flexibility matching your preferences.")
    elif is_hyderabad or is_preferred_city:
        score += 20
        if "hybrid" in job_loc_lower:
            rationales.append(f"Hybrid office flexibility in {job.location}.")
        else:
            rationales.append(f"In-person office reachability in {job.location}.")
    else:
        # Non-remote role outside Hyderabad
        score += 0
        rationales.append(f"Non-remote role outside commuting zone ({job.location}).")

    # Cap score at 100
    final_score = min(max(score, 0), 100)

    # Compile up to 3 punchy rationale sentences (stack, role, location)
    if not rationales:
        why = f"Solid alignment with your {profile.total_years_experience} years of software engineering expertise."
    else:
        why = " ".join(rationales[:3])

    return final_score, why

def rank_and_filter_jobs(jobs: List[JobPosting], profile: UserProfile) -> List[RankedJob]:
    """
    Ranks jobs prioritizing eligible locations (Hyderabad office/hybrid/remote, or other remote),
    and sorting with the most recently posted jobs on top.
    """
    ranked_list: List[RankedJob] = []

    for job in jobs:
        score, why = calculate_fit_score(job, profile)
        ranked_list.append(
            RankedJob(
                **job.model_dump(),
                match_percentage=score,
                why_it_matches=why
            )
        )

    # Filter for eligible location: Hyderabad (Office/Hybrid/Remote) OR other locations (Remote only)
    eligible_jobs = [j for j in ranked_list if is_location_eligible(j.location)]
    pool = eligible_jobs if eligible_jobs else ranked_list

    # Sort strictly with recently posted jobs on top (posted_date DESC, then match_percentage DESC)
    pool.sort(key=lambda j: (j.posted_date, j.match_percentage), reverse=True)

    return pool[:10]
