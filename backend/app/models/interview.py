from typing import List, Optional
from pydantic import BaseModel, Field

class CompanyIntelligence(BaseModel):
    company: str
    domain: str
    target_role: str
    tech_stack: List[str] = Field(default_factory=list)
    architecture_focus: List[str] = Field(default_factory=list)
    key_challenges: List[str] = Field(default_factory=list)

class SystemDesignScenario(BaseModel):
    title: str
    architecture_style: str
    problem_statement: str
    key_components: List[str] = Field(default_factory=list)
    tradeoffs_and_bottlenecks: List[str] = Field(default_factory=list)
    candidate_talking_points: List[str] = Field(default_factory=list)

class TechnicalConceptPrep(BaseModel):
    topic: str
    question: str
    concise_answer: str
    deep_dive_points: List[str] = Field(default_factory=list)
    pitfalls_to_avoid: List[str] = Field(default_factory=list)

class BehavioralStarStory(BaseModel):
    competency: str
    situation: str
    task: str
    action: str
    result: str
    grounded_experience: str

class ReverseInterviewQuestion(BaseModel):
    category: str
    question: str
    strategic_intent: str

class NotebookLmDossier(BaseModel):
    notebook_title: str
    markdown_content: str
    audio_deep_dive_prompt: str

class InterviewPrepDossier(BaseModel):
    id: str
    job_id: str
    candidate_user_id: str
    company: str
    position: str
    created_at: str
    company_intel: CompanyIntelligence
    system_design: List[SystemDesignScenario] = Field(default_factory=list)
    technical_concepts: List[TechnicalConceptPrep] = Field(default_factory=list)
    behavioral_star_stories: List[BehavioralStarStory] = Field(default_factory=list)
    reverse_interview_questions: List[ReverseInterviewQuestion] = Field(default_factory=list)
    notebooklm_dossier: NotebookLmDossier

