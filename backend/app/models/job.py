from typing import List, Optional
from pydantic import BaseModel, Field

class PlatformUrl(BaseModel):
    platform: str = Field(description="e.g. LinkedIn, Naukri, Indeed")
    url: str

class JobPosting(BaseModel):
    job_id: str
    title: str
    company: str
    location: str
    primary_platform: str
    apply_url: str
    source_urls: List[PlatformUrl] = Field(default_factory=list)
    posted_date: str = Field(description="ISO Date YYYY-MM-DD")
    expiry_date: str = Field(description="ISO Date YYYY-MM-DD")
    is_estimated_expiry: bool = True
    description: str = ""
    skills_required: List[str] = Field(default_factory=list)
    is_favorite: bool = False

class RankedJob(JobPosting):
    match_percentage: int = Field(ge=0, le=100)
    why_it_matches: str

class TailoredResumeMetadata(BaseModel):
    resume_id: str
    user_id: str
    job_id: str
    company: str
    position: str
    created_at: str
    expiry_date: str
    ats_keyword_coverage_score: int = Field(ge=0, le=100)
    docx_storage_path: Optional[str] = None
    pdf_storage_path: Optional[str] = None
    google_drive_file_id: Optional[str] = None

