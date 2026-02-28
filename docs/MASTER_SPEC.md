# Temporal360 Master Spec (Execution Baseline)

## Product Goal
Temporal360 is a 360-degree logging and seizure trigger analysis app.
All modules must write real data, feed risk analytics, and produce actionable insights.
No placeholder-only screens are acceptable for production.

## Non-Negotiable Requirements
- Android 10+ stability: no startup crash, no module switch crash.
- App opens directly to dashboard.
- No forced PIN flow.
- All timestamps are persisted with real event time.
- Settings are active and affect behavior.
- Notifications deep-link to target screen.
- Widgets support quick logging.

## Core Modules
- Water: bottle-based logging, daily target, dehydration checks.
- Meals: macros (carb/protein/fat), calories, portions, custom foods.
- Fasting: derived from meal timestamps.
- Smoking: interval planning + reminders + recovery progression.
- Seizure: structured detailed form, supports past entries.
- Aura: scheduled prompts and direct log flow.
- Emotion: mood + reason + note.
- Physical activity: categorized + custom + duration.
- Medication and vitamins: mg-based, multi-item, intake logs.
- Sleep: previous-night log and quality contribution.
- Health changes: categorized + custom.
- Location checks: periodic asks with log entry.
- Custom module extensibility path.

## Risk Engine Acceptance Criteria
- Daily risk score in range 0..10.
- Uses multi-factor signals (water, carbs, sleep, aura/activity/seizure recency).
- Weekly insights compare seizure vs non-seizure conditions.
- High risk notification throttled to prevent spam.
- Top correlations presented from real DB data.

## Dashboard Acceptance Criteria
- Never empty state after launch.
- Shows live KPIs from DB (not hardcoded demo values).
- Includes risk visualization and quick log actions.
- Reflects meal/water/sleep/seizure changes without app restart.

## Analytics & Export
- Analysis screen shows computed insights from stored logs.
- PDF export contains medical timeline and module summaries.
- Charts are driven by DB aggregates.

## Delivery Definition (Current Phase)
- Phase-1: Stability + real dashboard data + risk notification baseline.
- Phase-2: deeper correlation engine + richer charts + doctor PDF refinements.
- Phase-3: widget expansion and final Play Store hardening.
