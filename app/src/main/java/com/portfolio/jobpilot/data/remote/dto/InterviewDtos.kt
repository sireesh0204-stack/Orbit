package com.portfolio.jobpilot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CompanyIntelligenceDto(
    @SerializedName("company") val company: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("target_role") val targetRole: String,
    @SerializedName("tech_stack") val techStack: List<String> = emptyList(),
    @SerializedName("architecture_focus") val architectureFocus: List<String> = emptyList(),
    @SerializedName("key_challenges") val keyChallenges: List<String> = emptyList()
)

data class SystemDesignScenarioDto(
    @SerializedName("title") val title: String,
    @SerializedName("architecture_style") val architectureStyle: String,
    @SerializedName("problem_statement") val problemStatement: String,
    @SerializedName("key_components") val keyComponents: List<String> = emptyList(),
    @SerializedName("tradeoffs_and_bottlenecks") val tradeoffsAndBottlenecks: List<String> = emptyList(),
    @SerializedName("candidate_talking_points") val candidateTalkingPoints: List<String> = emptyList()
)

data class TechnicalConceptPrepDto(
    @SerializedName("topic") val topic: String,
    @SerializedName("question") val question: String,
    @SerializedName("concise_answer") val conciseAnswer: String,
    @SerializedName("deep_dive_points") val deepDivePoints: List<String> = emptyList(),
    @SerializedName("pitfalls_to_avoid") val pitfallsToAvoid: List<String> = emptyList()
)

data class BehavioralStarStoryDto(
    @SerializedName("competency") val competency: String,
    @SerializedName("situation") val situation: String,
    @SerializedName("task") val task: String,
    @SerializedName("action") val action: String,
    @SerializedName("result") val result: String,
    @SerializedName("grounded_experience") val groundedExperience: String
)

data class ReverseInterviewQuestionDto(
    @SerializedName("category") val category: String,
    @SerializedName("question") val question: String,
    @SerializedName("strategic_intent") val strategicIntent: String
)

data class NotebookLmDossierDto(
    @SerializedName("notebook_title") val notebookTitle: String,
    @SerializedName("markdown_content") val markdownContent: String,
    @SerializedName("audio_deep_dive_prompt") val audioDeepDivePrompt: String
)

data class InterviewPrepDossierDto(
    @SerializedName("id") val id: String,
    @SerializedName("job_id") val jobId: String,
    @SerializedName("candidate_user_id") val candidateUserId: String,
    @SerializedName("company") val company: String,
    @SerializedName("position") val position: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("company_intel") val companyIntel: CompanyIntelligenceDto,
    @SerializedName("system_design") val systemDesign: List<SystemDesignScenarioDto> = emptyList(),
    @SerializedName("technical_concepts") val technicalConcepts: List<TechnicalConceptPrepDto> = emptyList(),
    @SerializedName("behavioral_star_stories") val behavioralStarStories: List<BehavioralStarStoryDto> = emptyList(),
    @SerializedName("reverse_interview_questions") val reverseInterviewQuestions: List<ReverseInterviewQuestionDto> = emptyList(),
    @SerializedName("notebooklm_dossier") val notebookLmDossier: NotebookLmDossierDto
)

