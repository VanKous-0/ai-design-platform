-- Phase 2: database-backed workflow concurrency and critical-write idempotency.

ALTER TABLE `workflow_instance`
    ADD COLUMN `lock_version` BIGINT UNSIGNED NOT NULL DEFAULT 0
        COMMENT 'CAS version for workflow state transitions' AFTER `progress`;

ALTER TABLE `workflow_step_record`
    ADD COLUMN `completion_request_id` VARCHAR(100) DEFAULT NULL
        COMMENT 'Client Idempotency-Key for step completion' AFTER `next_suggestion`,
    ADD COLUMN `completion_request_hash` CHAR(64) DEFAULT NULL
        COMMENT 'SHA-256 of normalized completion input' AFTER `completion_request_id`,
    ADD COLUMN `completion_response_json` JSON DEFAULT NULL
        COMMENT 'Original successful response for exact retry replay' AFTER `completion_request_hash`,
    ADD UNIQUE KEY `uk_workflow_step_completion_request` (`instance_id`, `completion_request_id`),
    ADD CONSTRAINT `chk_workflow_step_completion_idempotency`
        CHECK ((`completion_request_id` IS NULL AND `completion_request_hash` IS NULL)
            OR (`completion_request_id` IS NOT NULL AND `completion_request_hash` IS NOT NULL)),
    ADD CONSTRAINT `chk_workflow_step_completion_response`
        CHECK (`completion_response_json` IS NULL OR JSON_TYPE(`completion_response_json`) = 'OBJECT');

ALTER TABLE `workflow_step_iteration`
    ADD COLUMN `creation_request_id` VARCHAR(100) DEFAULT NULL
        COMMENT 'Client Idempotency-Key for iteration creation' AFTER `selected`,
    ADD COLUMN `creation_request_hash` CHAR(64) DEFAULT NULL
        COMMENT 'SHA-256 of normalized iteration input' AFTER `creation_request_id`,
    ADD COLUMN `selected_node_guard` VARCHAR(64)
        GENERATED ALWAYS AS (
            CASE WHEN `selected` = 1 AND `is_deleted` = 0
                THEN CONCAT(`instance_id`, ':', `node_id`)
                ELSE NULL
            END
        ) STORED,
    ADD UNIQUE KEY `uk_workflow_iteration_identity` (`instance_id`, `node_id`, `id`),
    ADD UNIQUE KEY `uk_workflow_iteration_creation_request` (`instance_id`, `creation_request_id`),
    ADD CONSTRAINT `chk_workflow_iteration_idempotency`
        CHECK ((`creation_request_id` IS NULL AND `creation_request_hash` IS NULL)
            OR (`creation_request_id` IS NOT NULL AND `creation_request_hash` IS NOT NULL)),
    ADD CONSTRAINT `chk_workflow_iteration_selected`
        CHECK (`selected` IN (0, 1));

CREATE TABLE `workflow_node_runtime` (
    `instance_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow instance ID',
    `node_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template node ID',
    `next_iteration_no` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Next number allocated under row lock',
    `selected_iteration_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Canonical selected iteration',
    `lock_version` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Selection version',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`instance_id`, `node_id`),
    KEY `idx_workflow_node_runtime_selected` (`instance_id`, `node_id`, `selected_iteration_id`),
    CONSTRAINT `chk_workflow_node_runtime_counter` CHECK (`next_iteration_no` >= 1),
    CONSTRAINT `fk_workflow_node_runtime_instance`
        FOREIGN KEY (`instance_id`) REFERENCES `workflow_instance` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_workflow_node_runtime_node`
        FOREIGN KEY (`node_id`) REFERENCES `workflow_template_node` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_workflow_node_runtime_selected`
        FOREIGN KEY (`instance_id`, `node_id`, `selected_iteration_id`)
        REFERENCES `workflow_step_iteration` (`instance_id`, `node_id`, `id`) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Canonical per-node workflow concurrency state';

INSERT IGNORE INTO `workflow_node_runtime` (`instance_id`, `node_id`)
SELECT i.`id`, n.`id`
FROM `workflow_instance` i
JOIN `workflow_template_node` n ON n.`template_id` = i.`template_id`
UNION
SELECT `instance_id`, `node_id` FROM `workflow_step_iteration`
UNION
SELECT `instance_id`, `node_id` FROM `workflow_step_record`;

UPDATE `workflow_node_runtime` r
LEFT JOIN (
    SELECT `instance_id`, `node_id`, COALESCE(MAX(`iteration_no`), 0) + 1 AS `next_no`
    FROM `workflow_step_iteration`
    GROUP BY `instance_id`, `node_id`
) allocated ON allocated.`instance_id` = r.`instance_id` AND allocated.`node_id` = r.`node_id`
SET r.`next_iteration_no` = COALESCE(allocated.`next_no`, 1);

UPDATE `workflow_node_runtime` r
JOIN (
    SELECT `instance_id`, `node_id`, MAX(`id`) AS `selected_id`
    FROM `workflow_step_iteration`
    WHERE `selected` = 1 AND `is_deleted` = 0
    GROUP BY `instance_id`, `node_id`
) chosen ON chosen.`instance_id` = r.`instance_id` AND chosen.`node_id` = r.`node_id`
SET r.`selected_iteration_id` = chosen.`selected_id`;

UPDATE `workflow_step_iteration` SET `selected` = 0 WHERE `selected` = 1;

UPDATE `workflow_step_iteration` i
JOIN `workflow_node_runtime` r ON r.`selected_iteration_id` = i.`id`
SET i.`selected` = 1;

ALTER TABLE `workflow_step_iteration`
    ADD UNIQUE KEY `uk_workflow_iteration_single_selected` (`selected_node_guard`),
    ADD CONSTRAINT `fk_workflow_iteration_runtime`
        FOREIGN KEY (`instance_id`, `node_id`)
        REFERENCES `workflow_node_runtime` (`instance_id`, `node_id`) ON DELETE RESTRICT;

ALTER TABLE `usage_event`
    ADD COLUMN `idempotency_actor` VARCHAR(120) DEFAULT NULL
        COMMENT 'Server-derived authenticated or anonymous actor scope' AFTER `preference_evidence_json`,
    ADD COLUMN `idempotency_key` VARCHAR(100) DEFAULT NULL
        COMMENT 'Client Idempotency-Key' AFTER `idempotency_actor`,
    ADD COLUMN `idempotency_request_hash` CHAR(64) DEFAULT NULL
        COMMENT 'SHA-256 of normalized event input' AFTER `idempotency_key`,
    ADD UNIQUE KEY `uk_usage_event_idempotency` (`idempotency_actor`, `idempotency_key`),
    ADD CONSTRAINT `chk_usage_event_idempotency`
        CHECK ((`idempotency_actor` IS NULL AND `idempotency_key` IS NULL AND `idempotency_request_hash` IS NULL)
            OR (`idempotency_actor` IS NOT NULL AND `idempotency_key` IS NOT NULL
                AND `idempotency_request_hash` IS NOT NULL));
