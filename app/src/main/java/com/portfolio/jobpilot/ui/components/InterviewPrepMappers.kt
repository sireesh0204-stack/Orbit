package com.portfolio.jobpilot.ui.components

import com.portfolio.jobpilot.data.remote.dto.InterviewPrepDossierDto

fun InterviewPrepDossierDto.toUiModel(): InterviewPrepDossierUiModel {
    val mappedSystemDesigns = this.systemDesign.mapIndexed { idx, sd ->
        val componentNodes = sd.keyComponents.map { compStr ->
            val parts = compStr.split(":", limit = 2)
            if (parts.size == 2) {
                SystemDesignComponentNode(
                    name = parts[0].trim(),
                    role = parts[1].trim(),
                    slaMetric = "< 10ms"
                )
            } else {
                SystemDesignComponentNode(
                    name = compStr.trim(),
                    role = "Core architecture component",
                    slaMetric = "P99 < 50ms"
                )
            }
        }

        SystemDesignBlueprintUiModel(
            id = "sd_$idx",
            title = sd.title,
            architectureStyle = sd.architectureStyle,
            targetThroughput = "100k TPS",
            problemStatement = sd.problemStatement,
            keyComponents = componentNodes,
            capAndLatencyTradeoffs = sd.tradeoffsAndBottlenecks,
            candidateTalkingPoints = sd.candidateTalkingPoints
        )
    }

    val mappedTechConcepts = this.technicalConcepts.mapIndexed { idx, tc ->
        TechConceptCardUiModel(
            id = "tc_$idx",
            conceptTitle = tc.topic,
            frequencyTag = "P0 TOPIC",
            questionPrompt = tc.question,
            elevatorPitch30s = tc.conciseAnswer,
            deepDivePoints = tc.deepDivePoints,
            trapsToAvoid = tc.pitfallsToAvoid
        )
    }

    val mappedStarStories = this.behavioralStarStories.mapIndexed { idx, ss ->
        StarStoryUiModel(
            id = "star_$idx",
            headline = ss.competency,
            competencyTag = ss.competency,
            verificationBadge = ss.groundedExperience,
            impactMetric = "HIGH IMPACT",
            situation = ss.situation,
            task = ss.task,
            action = ss.action,
            result = ss.result
        )
    }

    val mappedReverseQuestions = this.reverseInterviewQuestions.mapIndexed { idx, rq ->
        ReverseQuestionUiModel(
            id = "rq_$idx",
            questionText = rq.question,
            strategicIntent = rq.strategicIntent,
            targetPersona = rq.category,
            followUpAngle = "Probe if engineering refactoring is prioritized in sprint planning."
        )
    }

    val mappedPodcastDossier = PodcastDossierUiModel(
        promptTitle = this.notebookLmDossier.notebookTitle,
        estimatedReadMinutes = 8,
        audioPromptInstructions = this.notebookLmDossier.audioDeepDivePrompt,
        rawNotebookLmMarkdown = this.notebookLmDossier.markdownContent
    )

    return InterviewPrepDossierUiModel(
        jobId = this.jobId,
        companyName = this.company,
        targetPosition = this.position,
        domainBadge = this.companyIntel.domain,
        readinessScorePercent = 94,
        systemDesigns = mappedSystemDesigns,
        techConcepts = mappedTechConcepts,
        starStories = mappedStarStories,
        reverseQuestions = mappedReverseQuestions,
        podcastDossier = mappedPodcastDossier
    )
}

fun sampleStripeDossierUiModel(): InterviewPrepDossierUiModel {
    return InterviewPrepDossierUiModel(
        jobId = "sample_stripe_01",
        companyName = "Stripe",
        targetPosition = "Staff Software Engineer",
        domainBadge = "Fintech & Payments Infrastructure",
        readinessScorePercent = 96,
        systemDesigns = listOf(
            SystemDesignBlueprintUiModel(
                id = "sd_sample_1",
                title = "Scalable Global Settlement & Ledger Pipeline",
                architectureStyle = "Decoupled Event-Driven Pipeline with Distributed Cache",
                targetThroughput = "120k TPS",
                problemStatement = "Design an idempotent, high-concurrency payment processing pipeline with sub-50ms P99 latency and zero double-charge guarantee.",
                keyComponents = listOf(
                    SystemDesignComponentNode("Edge API Gateway", "Rate limiting via Token Bucket", "< 5ms"),
                    SystemDesignComponentNode("Distributed Kafka Broker", "Partition-keyed event ordering", "< 15ms"),
                    SystemDesignComponentNode("Two-tier Storage Engine", "Redis L1 + Sharded PostgreSQL L2", "< 25ms")
                ),
                capAndLatencyTradeoffs = listOf(
                    "Prioritizing AP availability on transient failure with monotonic Lamport timestamps for eventual consistency.",
                    "Mitigating thundering herd spikes via probabilistic early expiration."
                ),
                candidateTalkingPoints = listOf(
                    "Lead with unique Idempotency-Keys on write endpoints to isolate retry bursts.",
                    "Demonstrate Circuit Breaker trip thresholds (Resilience4j) to prevent cascading third-party banking timeout spikes."
                )
            )
        ),
        techConcepts = listOf(
            TechConceptCardUiModel(
                id = "tc_sample_1",
                conceptTitle = "Coroutines & Structured Concurrency",
                frequencyTag = "P0 CRITICAL",
                questionPrompt = "How do lightweight threads prevent starvation, and how does structured cancellation behave under partial failure?",
                elevatorPitch30s = "Coroutines run cooperative multitasking over OS thread pools. Suspending saves the continuation state machine and frees threads. Structured concurrency ensures parent scopes cancel all child jobs if any failure occurs.",
                deepDivePoints = listOf(
                    "Dispatchers.Default (CPU-bound) vs Dispatchers.IO (elastic up to 64 blocking threads).",
                    "SupervisorJob allows sibling coroutines to survive single child failures."
                ),
                trapsToAvoid = listOf(
                    "Never swallow CancellationException with a generic catch (Exception e) block.",
                    "Avoid blocking synchronous I/O operations inside Dispatchers.Main."
                )
            )
        ),
        starStories = listOf(
            StarStoryUiModel(
                id = "star_sample_1",
                headline = "Zero-Downtime Settlement Engine Migration",
                competencyTag = "Technical Leadership",
                verificationBadge = "Verified from PhonePe (2021-2024)",
                impactMetric = "80k TPS",
                situation = "While serving as Lead Architect, the legacy settlement engine suffered transaction deadlocks under peak holiday load.",
                task = "Architect a resilient, deadlock-free pipeline without downtime.",
                action = "Engineered real-time settlement engine processing 80,000 TPS with monotonic version vectors and Kafka event streaming.",
                result = "Eliminated 100% of transaction deadlocks and delivered zero downtime during 3 consecutive festive peak events."
            )
        ),
        reverseQuestions = listOf(
            ReverseQuestionUiModel(
                id = "rq_sample_1",
                questionText = "How does your engineering team balance immediate merchant feature delivery with retirement of legacy distributed tech debt?",
                strategicIntent="Probes engineering culture, technical autonomy, and whether refactoring is treated as a first-class citizen.",
                targetPersona = "Engineering Leadership",
                followUpAngle = "Probe if dedicated debt-reduction cycles are scheduled every quarter."
            )
        ),
        podcastDossier = PodcastDossierUiModel(
            promptTitle = "Stripe - Staff Software Engineer Interview Prep",
            estimatedReadMinutes = 6,
            audioPromptInstructions = "Generate a deep-dive podcast between two senior tech leads breaking down this candidate's preparation for Stripe as a Staff Software Engineer. Focus on distributed settlement, idempotency, and candidate's verified 80k TPS achievements.",
            rawNotebookLmMarkdown = """
# INTERVIEW INTELLIGENCE DOSSIER: Stripe — Staff Software Engineer
> Generated via Orbit JobPilot | Zero-Hallucination Grounding

## 1. Executive Summary & Company Intelligence
- Target Organization: Stripe
- Domain: Fintech & Payments Infrastructure
- Tech Stack: Kotlin, Go, Kafka, PostgreSQL, Distributed Systems

## 2. High-Yield System Design
### Global Settlement & Ledger Pipeline
- Architecture: Decoupled Event-Driven Pipeline with Distributed Cache
- Target Throughput: 120,000 TPS | Latency Budget: P99 < 50ms
- Components: Token Bucket Edge Gateway, Partitioned Kafka Broker, Sharded DB

## 3. High-Frequency Technical Concepts
- Topic: Coroutines & Structured Concurrency
- Elevator Pitch: Cooperative multitasking on top of thread pools with state machine continuations.

## 4. Grounded Behavioral STAR Story
- Verified from PhonePe (2021-2024)
- Situation: Legacy settlement deadlocks under peak load.
- Action: Implemented monotonic version vectors with Kafka event stream.
- Result: Handled 80k TPS with 0 deadlocks across 3 festive peaks.
            """.trimIndent()
        )
    )
}

