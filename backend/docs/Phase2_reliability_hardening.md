# Phase 2 — Backend Reliability Hardening

## Scope

Phase 2 hardens the existing Java Workflow runtime and critical write APIs. It
does not add an Agent runtime, graph engine, provider integration, or a new
workflow product model.

## Workflow transition contract

`workflow_instance.lock_version` is a compare-and-swap version. Completing a
step updates the expected current node, running status, version, next node, and
progress in one conditional statement. The canonical `workflow_step_record` is
inserted afterward in the same transaction. If record persistence fails, the
state transition rolls back. Concurrent losers receive HTTP 409 with
`WORKFLOW_STATE_CONFLICT`; raw duplicate-key errors are not exposed.

The existing unique `(instance_id, node_id)` step-record constraint remains the
database guarantee that a node has at most one canonical completion record.

## Iteration and selection contract

`workflow_node_runtime` is the per-instance/per-node concurrency row:

- `next_iteration_no` is read under `SELECT ... FOR UPDATE` and advanced in the
  same transaction as the iteration insert.
- `selected_iteration_id` is the canonical selected result.
- the selected ID has a composite foreign key to an iteration belonging to the
  same instance and node.
- a generated-column unique key independently guarantees at most one active
  `selected = 1` compatibility projection.

This deliberately uses a short database row lock for local allocation and
selection. It does not introduce Redis or a distributed lock.

## Idempotency

The following endpoints accept an optional `Idempotency-Key` (maximum 100
characters):

- `POST /api/usage-events`
- `POST /api/workflow-instances/{id}/steps/{nodeId}/complete`
- `POST /api/workflow-instances/{id}/steps/{nodeId}/iterations`

The server stores a SHA-256 fingerprint of normalized business input. An exact
retry returns the original committed resource or response. Key reuse with
different input returns HTTP 409 and `RESOURCE_CONFLICT`. Requests without a key
are intentional new operations. Anonymous usage events must include
`anonymousId` when using an idempotency key.

Because the usage event and profile observation remain one transaction, replay
does not increment `BEHAVIOR_INFERRED.evidence_count` a second time.

## Error response

The existing JSON fields remain available:

```json
{
  "code": 409,
  "message": "Workflow state changed concurrently",
  "data": null,
  "errorCode": "WORKFLOW_STATE_CONFLICT"
}
```

`errorCode` is the stable machine-readable field. Bean-validation failures keep
the legacy numeric code `40001` while returning HTTP 400 and
`VALIDATION_ERROR`. Authentication, authorization, missing resources, and
conflicts use HTTP 401, 403, 404, and 409 respectively.

## Verification

The real MySQL 8.4 suite verifies both baseline-26-to-V37 and empty-V1-to-V37
migrations, two-way concurrent step completion, ten-way iteration allocation,
concurrent canonical selection, exact idempotent replay, single profile evidence
contribution, and rollback after a deliberately forced mid-transaction record
conflict.

GitHub Actions runs Java 21 `mvn clean verify`; Testcontainers uses the hosted
runner's Docker service to exercise MySQL rather than replacing it with H2.

## Deliberate limits

- Java Workflow remains the compatibility runtime and is expected to be retired
  by a future Agent runtime; this phase only makes current writes safe.
- Prompt preference hints remain current-Prompt metadata rather than immutable
  PromptRevision metadata. That semantic change belongs to a later phase.
- Idempotency is intentionally limited to the three highest-risk retryable write
  paths, not every CRUD endpoint.
- No Redis lock, event bus, automatic retry framework, or distributed workflow
  engine is introduced.
