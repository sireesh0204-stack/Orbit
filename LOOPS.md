# Orbit Regression & Loop Detection Matrix (`LOOPS.md`)

> **Purpose**: Tracks commit history to detect and prevent "ping-pong" patterns where an assistant reverts previous fixes.

---

## 1. Commit History & Loop Analysis

| Commit Hash | Intent / Change | Files Modified | Loop Signal / Regression Check |
| :--- | :--- | :--- | :--- |
| *(Initial)* | Initial Project Architecture & Harness Setup | `README.md`, `BUGS.md`, `LOOPS.md` | Baseline established. |
| `PHASE-00` | Phase 0: Modular Android Compose App, Cloud Run FastAPI backend, Whitelist gate & Test suites | `app/*`, `backend/*`, `README.md` | Verified `./gradlew test` (Pass) & `pytest` (12/12 Pass). |
| `PHASE-01` | Phase 1: Resume Upload (PDF/DOCX), Text Extraction, Structured Profile Schema & Compose Editor Form | `backend/app/services/*`, `backend/app/routers/profile.py`, `backend/tests/*`, `app/src/main/*`, `app/src/test/*` | Verified `./gradlew test` (Pass) & `pytest` (19/19 Pass). |
| `PHASE-01B` | Expanded Multi-Format Ingestion: Added TXT, PNG, JPEG/JPG with OCR and Multimodal Vision support | `text_extractor.py`, `resume_parser.py`, `ProfileScreen.kt`, `JobRepository.kt`, `test_profile_upload.py` | Verified `./gradlew test` (Pass) & `pytest` (24/24 Pass). |
| `PHASE-02` | Phase 2: Ingestion & Deduplication (rapidfuzz), Fit Scoring (0-100), Link Verification, PlatformLinksBottomSheet | `deduplicator.py`, `fit_scorer.py`, `link_verifier.py`, `apify_client.py`, `PlatformLinksBottomSheet.kt`, `test_phase2_jobs.py` | Verified `./gradlew test` (Pass) & `pytest` (30/30 Pass). |
| `PHASE-03` | Phase 3: Auto-Expiry Evaluator Service, Stale Favorite Purge, Expiry Warning Badges in JobCard | `expiry_evaluator.py`, `JobDao.kt`, `JobRepository.kt`, `JobCard.kt`, `test_phase3_expiry.py` | Verified `./gradlew test` (Pass) & `pytest` (32/32 Pass). |
| `PHASE-04` | Phase 4: Dual-Pass Drafter-Reviewer Resume Pipeline (DOCX/PDF Gen, ATS Audit), MyResumesScreen | `drafter.py`, `reviewer.py`, `doc_generator.py`, `tailor.py`, `MyResumesScreen.kt`, `test_phase4_tailor.py` | Verified `./gradlew test` (Pass) & `pytest` (36/36 Pass). |
| `PHASE-05` | Phase 5: Google Drive Standardized Naming Upload (`Company_Position_ExpiryDate.pdf`), CSV & HTML Job Exporters | `drive_uploader.py`, `job_exporter.py`, `export.py`, `test_phase5_export.py` | Verified `./gradlew test` (Pass) & `pytest` (41/41 Pass). |
| `FEATURE-GDOC` | Google Docs & Google Drive Direct Ingestion: Added link parser, HTTP & Drive API download, Dialog in ProfileScreen | `google_docs_importer.py`, `profile.py`, `ProfileScreen.kt`, `JobApiService.kt`, `JobRepository.kt`, `test_google_docs_import.py` | Verified `./gradlew test` (Pass) & `pytest` (48/48 Pass). |
| `THEME-ADAPTIVE` | Executive Bento Cockpit: Titanium Slate tokens & Adaptive Performance Tier (S24/Redmi 120Hz vs 60Hz) | `AdaptivePerformanceTier.kt`, `Color.kt`, `MainActivity.kt`, `ProfileScreen.kt`, `JobCard.kt` | Verified `./gradlew test` (Pass) & `pytest` (48/48 Pass). |
| `POLICY-LOC-SORT` | Location Policy (Hyderabad Office/Hybrid/Remote, others Remote) & Recency-First Sorting (posted_date DESC) | `fit_scorer.py`, `apify_client.py`, `JobDao.kt`, `MainActivity.kt`, `BUGS.md` | Verified `./gradlew test` (Pass) & `pytest` (48/48 Pass). |
| `FEATURE-REALTIME-URL` | LinkedIn Sub-Hour Manipulation (`f_TPR=r300`, `sortBy=DD`), Naukri date filters & RealTimeJobUrlBuilder | `url_builder.py`, `jobs.py`, `test_realtime_urls.py`, `BUGS.md` | Verified `./gradlew test` (Pass) & `pytest` (53/53 Pass). |
| `FEATURE-INTERVIEW-PREP` | In-App Interview Prep Cockpit: Zero-Cost System Design, Behavioral STAR grounding, Tech Cheat-Sheet & NotebookLM Dossier | `interview.py`, `interview_prep.py`, `InterviewDtos.kt`, `JobApiService.kt`, `JobRepository.kt`, `test_interview_prep.py` | Verified `./gradlew test` (Pass) & `pytest` (62/62 Pass). |

