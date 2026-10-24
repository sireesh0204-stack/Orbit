# Operating Architecture: Antigravity & Argon 4

## 1. Role Division & Responsibilities
- **Antigravity (This Agent)**:
  - Single agent for ALL backend/system logic, state management, lifecycle, build configurations, and device testing.
  - Full repository context: reads all files and guards shared state against regressions.
  - Runs builds, tests on device, commits and pushes.
- **Argon 4**:
  - Used EXCLUSIVELY for visual art, UI design, layout XML/Composables, colors, spacing, typography.
  - Receives strictly STATELESS UI prompts (receives data properties, emits callback lambdas).
  - NEVER write business logic, lifecycle, or services in Argon 4 prompts.

---

## 2. Mandatory Rules Before Touching Code
1. **Pre-Flight Context**: Before reading or editing anything, read `BUGS.md`, `LOOPS.md`, and `harness/docs/UI_HARNESS.md`.
2. **Invariant Protection**: Check `BUGS.md` "Must-not-break" fields for any code touching shared state.
3. **One Bug Per Commit**: Commit each fix atomically with its Bug ID.
4. **No Weakening Tests**: Never delete or weaken a test or harness check.
