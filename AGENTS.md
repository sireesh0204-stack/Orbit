# Operating Architecture: Antigravity & Claude Code

## 1. Dual-Agent Role Division & Responsibilities
- **Antigravity (This Agent)**:
  - Single agent for ALL backend/system logic, state management, lifecycle, build configurations, and device testing.
  - Full repository context: reads all files and guards shared state against regressions.
  - Runs builds, tests on device, commits and pushes.
- **Claude Code**:
  - Used for deep algorithms, AST/scoring mechanics, prompt audits, and complex Jetpack Compose UI refinement.
  - Receives strictly decoupled logic/UI components.
  - Antigravity owns all wiring, tests, and invariant protection.

---

## 2. Mandatory Rules Before Touching Code
1. **Pre-Flight Context**: Before reading or editing anything, read `BUGS.md`, `LOOPS.md`, and `harness/docs/UI_HARNESS.md`.
2. **Invariant Protection**: Check `BUGS.md` "Must-not-break" fields for any code touching shared state.
3. **One Bug Per Commit**: Commit each fix atomically with its Bug ID.
4. **No Weakening Tests**: Never delete or weaken a test or harness check.
5. **App Icon Architecture**:
   - NEVER use default Android system icons (`sym_def_app_icon`).
   - All app icons must be modern `<adaptive-icon>` assets:
     * Minimal, mature neomorphic or glassmorphic aesthetic (no flat, bold, or childish graphics).
     * Background: Delicate tactile dot-grid canvas or matte obsidian texture.
     * Foreground: Clean, sleek geometric vector that blends harmoniously on home screens with premium apps.
     * Brand color tokens: Rusty Copper & Antique Bronze gradient mix (`#D96B43`, `#E29555`, `#A85A2E`, `#CD7F32`) or OLED Cyberpunk cyan/amber accents.
