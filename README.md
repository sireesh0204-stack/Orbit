<p align="center">
  <img src="assets/logo/aszsd_monogram.svg" alt="ASZSD Monogram" width="300" />
</p>

# JobPilot: High-Precision Private Career Orchestration Engine

> **Architecture Target**: Private Dual-User Mobile Client & Serverless Backend  
> **Mobile Client**: Modern Android 14+ / Xiaomi Redmi Turbo 5 (120Hz VRR AMOLED, 20:9 Aspect Ratio)  
> **Backend Service**: Google Cloud Run (Python 3.11, FastAPI, Pydantic v2)  
> **Security Protocol**: Strict 2-User Whitelist Invariant Gate via Firebase ID Tokens  

---

## 🚀 Quick Start & Executable Test Verification

### 1. Android Test Suite
Runs all Room DAO, Repository, Auth Interceptor, and ViewModel tests:
```bash
./gradlew test
# Windows PowerShell
.\gradlew.bat test
```
**Status**: All unit tests compiling and passing.

### 2. Backend Test Suite
Runs all 62 FastAPI endpoint, Whitelist Invariant Gate, Drafter-Reviewer, Deduplication, Exporter, Real-Time Sub-Hour Scraper, and Interview Prep tests:
```bash
# Activate virtual environment
.\backend\.venv\Scripts\Activate.ps1
# Run pytest
pytest backend/tests -v
```
**Status**: **62 passed in ~9s**.

---

## 📱 Android APK Package & GitHub Actions

JobPilot includes a fully automated CI/CD pipeline (`.github/workflows/build-apk.yml`) that compiles and signs the Android APK on every push or manual workflow dispatch.

### How to Download the APK from GitHub:
1. Navigate to the GitHub repository: **`https://github.com/sireesh0204-stack/Orbit`**
2. Click on the **Actions** tab at the top.
3. Select the latest workflow run: **`Build & Release JobPilot APK`**.
4. Scroll down to the **Artifacts** section at the bottom.
5. Click **`JobPilot-Debug-APK`** to download `app-debug.apk` directly to your phone or PC.
6. Install on your Android device (Xiaomi Redmi Turbo 5 / Samsung Galaxy S24).

*(You can also find the locally compiled APK at `app/build/outputs/apk/debug/app-debug.apk`)*.

---

## 🏛 System Topology & End-to-End Pipeline Summary

```
Orbit/
├── app/                              # Android Modular Jetpack Compose Client
│   ├── src/main/java/com/portfolio/jobpilot/
│   │   ├── data/
│   │   │   ├── auth/                 # FirebaseAuthManager (Whitelist verification)
│   │   │   ├── local/                # Room DB, JobDao, JobEntity (sourceUrlsJson)
│   │   │   ├── remote/               # Retrofit, AuthInterceptor, DTOs
│   │   │   └── repository/           # JobRepository
│   │   ├── ui/
│   │   │   ├── theme/                # Matte Obsidian tokens, Type, Colors
│   │   │   ├── components/           # 120Hz Stateless UI (JobCard, TelemetryHud, PlatformLinksBottomSheet)
│   │   │   └── screens/              # ProfileScreen, MyResumesScreen
│   │   ├── viewmodel/                # Unidirectional MVVM Flow (MainViewModel, ProfileViewModel)
│   │   └── MainActivity.kt           # 4-Tab Single-Activity Navigation Scaffold
│   └── src/test/                     # Complete JUnit test suite
├── backend/                          # FastAPI Backend on Google Cloud Run
│   ├── app/
│   │   ├── auth/                     # Firebase ID Token verification & Whitelist gate
│   │   ├── models/                   # Strict Pydantic v2 schemas (Profile, Job, Tailor)
│   │   ├── services/
│   │   │   ├── text_extractor.py     # PDF, DOCX, TXT, PNG, JPEG text & vision extractors
│   │   │   ├── resume_parser.py      # LLM + heuristic zero-hallucination profile parser
│   │   │   ├── deduplicator.py       # rapidfuzz multi-platform deduplication engine
│   │   │   ├── fit_scorer.py         # 0-100 match scoring & rationale generator
│   │   │   ├── link_verifier.py      # Async HEAD link verification
│   │   │   ├── expiry_evaluator.py   # Daily expiration evaluator & stale favorite purger
│   │   │   ├── tailor/
│   │   │   │   ├── drafter.py        # Pass 1: Zero-hallucination resume drafter
│   │   │   │   ├── reviewer.py       # Pass 2: Adversarial auditor & ATS coverage scorer
│   │   │   │   └── doc_generator.py  # Single-column ATS DOCX & PDF generators
│   │   │   └── export/
│   │   │       ├── drive_uploader.py # Google Drive standardized upload
│   │   │       └── job_exporter.py   # jobs_YYYY-MM-DD_HHmm.csv / .html generator
│   │   ├── routers/                  # /health, /profile, /jobs, /tailor, /export
│   │   └── main.py                   # FastAPI Application Entrypoint
│   ├── tests/                        # 41 Pytest unit & integration tests
│   ├── Dockerfile                    # Multi-stage Cloud Run container
│   ├── requirements.txt              # Production dependencies
│   └── pytest.ini                    # Pytest configuration
├── harness/                          # Master Portfolio UI & Motion Harness
├── BUGS.md                           # Master Bug Invariant Registry
└── LOOPS.md                          # Loop & Ping-Pong Detection Matrix
```

---

## 🔑 Backend API Endpoints

| Endpoint | Method | Protected | Description |
| :--- | :---: | :---: | :--- |
| `/health` | `GET` | No | System health and version |
| `/api/v1/profile` | `GET` | Yes | Retrieves user's verified candidate profile |
| `/api/v1/profile` | `PATCH` | Yes | Edits profile fields (roles, skills, cities, remote) |
| `/api/v1/profile/upload-resume` | `POST` | Yes | Ingests PDF, DOCX, TXT, PNG, JPEG with zero-hallucination extraction |
| `/api/v1/profile/upload-google-doc` | `POST` | Yes | Imports and structures directly from Google Docs / Google Drive share links |
| `/api/v1/jobs` | `GET` | Yes | Returns cached normalized job postings |
| `/api/v1/jobs/sync` | `POST` | Yes | Triggers scraper ingestion, deduplication, link verification & ranking |
| `/api/v1/jobs/rank` | `GET` | Yes | Returns top ranked jobs with match % and punchy rationale |
| `/api/v1/jobs/{job_id}/favorite` | `POST` | Yes | Toggles favorite status |
| `/api/v1/jobs/purge-expired` | `POST` | Yes | Purges stale jobs and removes expired favorites |
| `/api/v1/tailor?job_id=...` | `POST` | Yes | Runs Dual-Pass Drafter–Reviewer pipeline and generates ATS DOCX/PDF |
| `/api/v1/tailor/history` | `GET` | Yes | Lists all tailored resumes for user |
| `/api/v1/tailor/{resume_id}/docx`| `GET` | Yes | Downloads single-column ATS DOCX |
| `/api/v1/tailor/{resume_id}/pdf` | `GET` | Yes | Downloads clean ATS PDF |
| `/api/v1/export/drive-upload` | `POST` | Yes | Uploads to Google Drive as `Company_Position_ExpiryDate.pdf` |
| `/api/v1/export/jobs-csv` | `GET` | Yes | Exports active/saved jobs as `jobs_YYYY-MM-DD_HHmm.csv` |
| `/api/v1/export/jobs-html` | `GET` | Yes | Exports standalone styled HTML job catalog |
