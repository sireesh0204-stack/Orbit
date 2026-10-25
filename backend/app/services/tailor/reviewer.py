from typing import List, Dict, Any, Tuple
from app.models.profile import UserProfile
from app.models.job import JobPosting

def audit_and_review_draft(
    draft: Dict[str, Any],
    master_profile: UserProfile,
    job: JobPosting
) -> Tuple[bool, int, List[str], List[str], List[str]]:
    """
    Pass 2: Adversarial Reviewer & Auditor Agent.
    Returns:
    - is_valid_no_hallucination: bool
    - ats_keyword_coverage_score: int (0-100)
    - missing_critical_keywords: List[str]
    - suggested_talking_points: List[str]
    - hallucination_flags: List[str]
    """
    hallucination_flags: List[str] = []

    # 1. Fact Verification: Check every company and date in draft against master profile
    master_companies = {w.company.lower() for w in master_profile.work_history}
    master_titles = {w.title.lower() for w in master_profile.work_history}

    for work in draft.get("work_history", []):
        draft_comp = work.get("company", "").lower()
        if draft_comp not in master_companies:
            hallucination_flags.append(f"Unverified employer in draft: '{work.get('company')}'")

        draft_title = work.get("title", "").lower()
        if draft_title not in master_titles:
            hallucination_flags.append(f"Unverified job title in draft: '{work.get('title')}'")

    # 2. ATS Keyword Coverage
    job_skills = {s.lower() for s in job.skills_required}
    draft_skills = {s.lower() for s in draft.get("skills", {}).get("technical", [])}

    # Also check skills mentioned across bullet points
    all_draft_text = " ".join([
        bp for w in draft.get("work_history", []) for bp in w.get("bullet_points", [])
    ]).lower()

    integrated_keywords = []
    missing_keywords = []

    for skill in job.skills_required:
        s_lower = skill.lower()
        if s_lower in draft_skills or s_lower in all_draft_text:
            integrated_keywords.append(skill)
        else:
            missing_keywords.append(skill)

    if job.skills_required:
        ats_score = int((len(integrated_keywords) / len(job.skills_required)) * 100)
    else:
        ats_score = 90

    # 3. Suggested Interview Talking Points for missing keywords
    talking_points = []
    for missing in missing_keywords[:3]:
        talking_points.append(
            f"Discuss transferable skills from your {master_profile.skills.technical[:2]} background to rapidly adopt {missing} in production."
        )

    is_valid = len(hallucination_flags) == 0
    return is_valid, ats_score, missing_keywords, talking_points, hallucination_flags

