# Orbit Master Bug Invariant Registry (`BUGS.md`)

> **Project**: JobPilot (Orbit)  
> **Rule of Invariance**: Before editing any code, the AI agent MUST read this file. Any code modification touching shared state or rendering loops must strictly adhere to the "Must-not-break" invariants.

---

## 1. Operating Rules for AI Assistants
1. **Pre-Flight Invariant Check**: Read this file and `LOOPS.md` before touching code.
2. **One Bug Per Commit**: Commit fixes atomically with their Bug ID (`BUG-00X`).
3. **No Weakening Tests**: Never delete or weaken a unit test or smoke test to make a fix fit.
4. **No Regressive Reverts**: You cannot fix a new bug by reverting an existing bug guard.

---

## 2. System Core Invariants (Non-Negotiable)

* **INVARIANT-01: Private Dual-User Whitelist Gate**:
  - The API MUST reject any user whose email is not in `ALLOWED_USER_EMAILS` or whose UID is not in `ALLOWED_USER_UIDS` with `403 Forbidden`.
* **INVARIANT-02: Zero Hallucination in Resume Tailoring**:
  - Pass 1 & Pass 2 Drafter–Reviewer pipeline MUST NEVER invent employers, job titles, dates, certifications, tools, metrics, or achievements.
  - Any draft introducing unverified entities fails the adversarial audit with `422 Unprocessable Entity`.
* **INVARIANT-03: ATS Document Parsability**:
  - All generated DOCX and PDF artifacts must be strictly single-column, table-free in core experience, and pass raw plain-text extraction verification.
* **INVARIANT-04: 120Hz Zero-Reflow UI**:
  - Dynamic counters and match percentages mandate `tabular-nums`. All micro-interactions mutate RenderNode GPU matrices without triggering CPU `requestLayout()`.
* **INVARIANT-05: Standardized Google Drive Export**:
  - Uploaded files must strictly enforce the `Company_Position_ExpiryDate.pdf` naming pattern.
* **INVARIANT-06: Multi-Device Adaptive Rendering**:
  - Display refresh rate (120Hz VRR on Galaxy S24 / Redmi Turbo 5 vs 60Hz / battery-saver on legacy devices) must adaptively scale animations and bypass costly GPU blur shaders on Tier-2 devices to guarantee 0 dropped frames.
* **INVARIANT-07: Location Policy (Hyderabad Office/Hybrid/Remote; Others Remote-Only)**:
  - Candidates can attend physical offices in Hyderabad (Office, Hybrid, Remote eligible with full match scoring). All other cities (Bengaluru, Pune, Mumbai, etc.) strictly mandate Remote; non-remote roles outside Hyderabad are flagged and excluded.
* **INVARIANT-08: Recency-First Job Sorting**:
  - Feed queries in Room DAO and backend ranking must sort primarily by `posted_date DESC` so the newest posted jobs are always on top.
* **INVARIANT-09: Real-Time Sub-Hour Scraper Manipulation**:
  - Real-time job polling utilizes LinkedIn's hidden second-level time parameter (`f_TPR=r300` for 5 minutes, `sortBy=DD` for chronological sorting) combined with Naukri & Indeed date filters to extract brand-new postings within minutes of publication.
* **INVARIANT-10: Zero-Cost & Zero-Hallucination In-App Interview Prep**:
  - Interview preparation dossiers operate at strictly $0.00 cost (Google AI Studio Gemini Flash free tier + deterministic offline fallback + free Google NotebookLM export). Behavioral STAR stories must strictly cite and derive from verified candidate work history, never inventing metrics or projects.

---

## 3. Active Bug Registry

*(No open bugs. All 48 backend tests and Android unit test suite passing cleanly).*
