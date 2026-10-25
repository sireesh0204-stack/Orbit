from typing import List, Dict, Any
from app.models.profile import UserProfile, WorkExperience
from app.models.job import JobPosting

DRAFTER_SYSTEM_PROMPT = """You are an elite Resume Drafter.
Your objective: Align the candidate's master resume with the target job description.

HARD ZERO-HALLUCINATION INVARIANTS:
1. You may ONLY reorder, rephrase, and emphasize VERIFIED facts present in the master profile.
2. NEVER invent past employers, job titles, start/end dates, certifications, tools, metrics, or achievements.
3. If the candidate does not have a skill required by the job, DO NOT add it. Gaps stay as gaps.
4. Echo the job description's terminology only when describing candidate's actual verified projects.
"""

def draft_tailored_resume(profile: UserProfile, job: JobPosting) -> Dict[str, Any]:
    """
    Pass 1: Drafter Agent.
    Reorders bullet points and aligns technical keywords with job requirements
    while preserving 100% factual accuracy.
    """
    job_skills_lower = {s.lower() for s in job.skills_required}
    tailored_history: List[Dict[str, Any]] = []

    for work in profile.work_history:
        # Prioritize bullet points that mention skills matching the target job
        prioritized_bullets = []
        other_bullets = []

        for bp in work.bullet_points:
            bp_lower = bp.lower()
            if any(skill in bp_lower for skill in job_skills_lower):
                prioritized_bullets.append(bp)
            else:
                other_bullets.append(bp)

        ordered_bullets = prioritized_bullets + other_bullets

        tailored_history.append({
            "company": work.company,
            "title": work.title,
            "start_date": work.start_date,
            "end_date": work.end_date,
            "bullet_points": ordered_bullets,
            "technologies_used": work.technologies_used
        })

    # Prioritize candidate's technical skills that match job requirements
    matched_skills = [s for s in profile.skills.technical if s.lower() in job_skills_lower]
    remaining_skills = [s for s in profile.skills.technical if s.lower() not in job_skills_lower]
    aligned_technical_skills = matched_skills + remaining_skills

    return {
        "candidate_title": job.title if any(r.lower() in job.title.lower() for r in profile.target_roles) else (profile.target_roles[0] if profile.target_roles else "Senior Software Engineer"),
        "target_company": job.company,
        "skills": {
            "technical": aligned_technical_skills,
            "soft": profile.skills.soft,
            "tools_and_platforms": profile.skills.tools_and_platforms
        },
        "work_history": tailored_history,
        "education": [e.model_dump() for e in profile.education]
    }

