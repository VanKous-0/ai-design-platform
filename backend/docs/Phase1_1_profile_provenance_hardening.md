# Phase 1.1 — Profile Provenance and Reproducibility Hardening

## Provenance contract

The three persisted concepts have intentionally different responsibilities:

```text
UsageEvent              -> raw, append-oriented behavioral fact
UserPreferenceSignal   -> current aggregated profile state
ProfileContextSnapshot -> immutable execution-time effective context
```

`UserPreferenceSignal` does not retain every observation. Repeated or changed
behavior updates one aggregate row per user, preference dimension, sentiment,
scope, and source. The raw sequence remains in `usage_event`.

## Explicit behavior evidence

Administrators can attach `preferenceHints` to a Prompt through the existing
Prompt create/update API. Each hint is one explicit `preferenceKey` and
`preferenceValue`; no title, content, category, tool name, keyword, regular
expression, or LLM inference is used.

Only this path updates the long-term behavior aggregate:

```text
authenticated render_prompt event targeting a Prompt
  -> enabled Prompt preference hint exists
  -> UsageEvent is inserted with server-resolved preference_evidence_json
  -> BEHAVIOR_INFERRED / LONG_TERM / PREFER signal is updated once
```

The insert and aggregate update share one transaction. If profile observation
fails, the event insert rolls back. Anonymous events remain valid statistics
facts and may contain resolved evidence, but never update a user's profile.
`copy_prompt`, login, views, duration, and every event without an explicit
mapping do not update the profile.

Behavior confidence remains deliberately simple: the first matching observation
is `0.300`, a repeated value adds `0.100` up to `0.800`, and a changed value
resets the aggregate to one observation at `0.300`. `USER_DECLARED` continues to
outrank `AGENT_INFERRED`, which outranks `BEHAVIOR_INFERRED`.

## Authoritative execution snapshot

`UserPreferenceContextService` selects only effective preferences and sorts them
by scope, sentiment, key, source, and signal ID. Workflow iteration creation
serializes this stable versioned structure:

```json
{
  "schemaVersion": 1,
  "preferences": [
    {
      "signalId": 1,
      "preferenceKey": "style",
      "preferenceValue": "new_chinese",
      "sentiment": "PREFER",
      "scope": "LONG_TERM",
      "source": "USER_DECLARED",
      "confidence": 1.0,
      "lastObservedAt": "2026-09-04T12:00:00"
    }
  ]
}
```

`WorkflowStepIterationCreateRequest.profileContextSnapshot` remains accepted for
request compatibility but is deprecated and ignored. The response and database
row contain only the server-built snapshot. Later profile updates create new
snapshots for new iterations and do not change existing rows.

## Deliberate limits

This phase adds no event bus, second behavior log, rules engine, LLM analyzer,
recommendation system, Agent runtime, or LangGraph contract. Cross-request event
idempotency and existing Workflow concurrency gaps remain Phase 2 work.
