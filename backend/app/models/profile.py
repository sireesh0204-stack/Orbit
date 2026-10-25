from typing import List, Optional
from pydantic import BaseModel, Field

class WorkExperience(BaseModel):
    company: str
    title: str
    start_date: str = Field(description="Format YYYY-MM")
    end_date: str = Field(description="Format YYYY-MM or Present")
    bullet_points: List[str] = Field(default_factory=list)
    technologies_used: List[str] = Field(default_factory=list)

class Education(BaseModel):
    institution: str
    degree: str
    year: str = Field(description="Format YYYY")

class SkillsGroup(BaseModel):
    technical: List[str] = Field(default_factory=list)
    soft: List[str] = Field(default_factory=list)
    tools_and_platforms: List[str] = Field(default_factory=list)

class UserProfile(BaseModel):
    user_id: str
    target_roles: List[str] = Field(default_factory=list)
    total_years_experience: float = 0.0
    preferred_cities: List[str] = Field(default_factory=list)
    is_remote_preferred: bool = True
    skills: SkillsGroup = Field(default_factory=SkillsGroup)
    domains: List[str] = Field(default_factory=list)
    work_history: List[WorkExperience] = Field(default_factory=list)
    education: List[Education] = Field(default_factory=list)

class UserProfilePatch(BaseModel):
    target_roles: Optional[List[str]] = None
    total_years_experience: Optional[float] = None
    preferred_cities: Optional[List[str]] = None
    is_remote_preferred: Optional[bool] = None
    skills: Optional[SkillsGroup] = None
    domains: Optional[List[str]] = None
    work_history: Optional[List[WorkExperience]] = None
    education: Optional[List[Education]] = None

