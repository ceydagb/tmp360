# Temporal360 Implementation Plan

## Phase-1 (Now)
- Replace dashboard demo chart with live DB aggregates.
- Surface real 0..10 risk score and weekly insight preview.
- Add high-risk notification gate in periodic worker.
- Keep notification deep links consistent with route names.
- Verify CI build on push.

## Phase-2
- Build seizure-day vs non-seizure-day comparator table.
- Add top-3 trigger combination scorer.
- Expand analytics charts (water, macros, smoking, adherence).
- Improve module-level setting effects and validation.

## Phase-3
- Extend home-screen widget actions for all critical modules.
- Add custom module schema and generic logging pipeline.
- Strengthen PDF report sections for physician workflows.
- Add regression test coverage for DAO aggregates and risk engine.

## Workstream Mapping
- Data: DAO aggregate queries, entities, migrations.
- Domain: risk engine, correlation engine, recommendation rules.
- UI: dashboard, analysis, quick-entry UX, dark theme consistency.
- Notifications: scheduling, throttling, deep links.
- CI/CD: build reliability, artifact publishing, release workflow.
