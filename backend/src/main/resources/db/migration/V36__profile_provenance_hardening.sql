-- Phase 1.1: explicit Prompt preference evidence and server-captured event provenance.

CREATE TABLE `prompt_preference_hint` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `prompt_id` BIGINT UNSIGNED NOT NULL COMMENT 'Prompt whose deliberate render carries this evidence',
    `preference_key` VARCHAR(80) NOT NULL COMMENT 'Stable preference dimension key',
    `preference_value` VARCHAR(1000) NOT NULL COMMENT 'Explicit administrator-declared preference value',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_preference_hint_dimension`
        (`prompt_id`, `preference_key`, `is_deleted`),
    KEY `idx_prompt_preference_hint_prompt` (`prompt_id`, `is_deleted`),
    CONSTRAINT `fk_prompt_preference_hint_prompt`
        FOREIGN KEY (`prompt_id`) REFERENCES `prompt_template` (`id`) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Explicit Prompt-to-preference evidence mapping';

ALTER TABLE `usage_event`
    ADD COLUMN `preference_evidence_json` JSON DEFAULT NULL
        COMMENT 'Server-resolved preference evidence captured with the immutable event' AFTER `extra_json`,
    ADD CONSTRAINT `chk_usage_event_preference_evidence`
        CHECK (`preference_evidence_json` IS NULL OR JSON_TYPE(`preference_evidence_json`) = 'ARRAY');
