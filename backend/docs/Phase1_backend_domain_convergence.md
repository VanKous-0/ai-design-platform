# Phase 1 — Backend Domain Convergence

## Current-state findings

- `prompt_template` previously mixed logical identity and mutable content. Both
  prompt content and `prompt_parameter` rows were updated in place, so an old
  render could not be reconstructed after an administrator edit.
- `workflow_step_iteration.prompt_content` already served as the rendered prompt
  snapshot, but it did not record the prompt identity or exact revision.
- `user_design_preference` is a useful backward-compatible current summary, but
  its one-row-per-user shape cannot retain source, confidence, observation count,
  or recent versus long-term semantics. `user_recent_parameter` records recent
  parameter reuse and is not a substitute for preference provenance.
- `ai_tool` is external product/catalog metadata. It is referenced by cases,
  reviews, ratings, prompt recommendations, and workflow iteration records; it is
  not an executable Agent tool and remains unchanged in this phase.
- The Java workflow runtime is a sequential legacy runtime. This phase adds only
  immutable history references and does not add graph, routing, or checkpoint
  behavior.
- Flyway started at V27 while Docker and documentation still required 26 ordered
  scripts under `sql/`, so an empty database could not be created by Flyway alone.

## Implemented boundaries

`prompt_template` remains the logical identity and backward-compatible current
projection. `prompt_revision` is the immutable source for content and parameter
schema. Every content change or parameter create/update/delete creates a new
revision in the same transaction; revisions have no update API. Current and
historical rendering both read the selected revision snapshot. Unique and
composite foreign keys protect revision numbers and prompt/revision ownership
under concurrency without requiring elevated trigger privileges for migrations.

`workflow_step_iteration.prompt_content` remains the rendered prompt snapshot.
When a library prompt is used, `prompt_id` and `prompt_revision_id` must be
provided together and the revision must belong to that prompt. An optional JSON
object stores the profile context used for the iteration.

`user_design_preference` and its API remain available. A new
`user_preference_signal` model stores source, scope, sentiment, confidence,
evidence count, provenance summary, and last observation time. User declarations
always have confidence 1.0 and outrank Agent and behavior inference during
consolidation. One behavior observation starts at 0.3; repeated matching evidence
increases confidence by 0.1 up to 0.8. The profile-owner endpoint accepts only
`USER_DECLARED`; trusted inferred updates use the existing admin boundary at
`POST /api/admin/users/{userId}/preference-signals`, so clients cannot relabel
their own input as Agent or behavior evidence.

Flyway now owns both paths: V1 is the immutable consolidated pre-Flyway baseline
for empty databases, while populated legacy databases baseline at version 26 and
apply V27 onward. The old scripts remain for history but are not mounted or
executed by Docker Compose.
