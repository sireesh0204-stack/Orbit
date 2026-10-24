# Orbit Master Bug Invariant Registry (`BUGS.md`)

> **Project**: Orbit (Job)  
> **Rule of Invariance**: Before editing any code, the AI agent MUST read this file. Any code modification touching shared state or rendering loops must strictly adhere to the "Must-not-break" invariants.

---

## 1. Operating Rules for AI Assistants
1. **Pre-Flight Invariant Check**: Read this file and `LOOPS.md` before touching code.
2. **One Bug Per Commit**: Commit fixes atomically with their Bug ID (`BUG-00X`).
3. **No Weakening Tests**: Never delete or weaken a unit test or smoke test to make a fix fit.
4. **No Regressive Reverts**: You cannot fix a new bug by reverting an existing bug guard.

---

## 2. Active Bug Registry

*(No bugs registered yet — project in initial architecture phase).*
