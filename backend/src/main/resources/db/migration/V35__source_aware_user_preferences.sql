-- Source-aware, confidence-bearing long-term and recent preference signals.

CREATE TABLE `user_preference_signal` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Profile owner',
    `preference_key` VARCHAR(80) NOT NULL COMMENT 'Stable preference dimension key',
    `preference_value` VARCHAR(1000) NOT NULL COMMENT 'Preference value or free text',
    `sentiment` VARCHAR(20) NOT NULL DEFAULT 'PREFER' COMMENT 'PREFER or AVOID',
    `scope` VARCHAR(20) NOT NULL COMMENT 'LONG_TERM or RECENT',
    `source` VARCHAR(30) NOT NULL COMMENT 'USER_DECLARED, BEHAVIOR_INFERRED, or AGENT_INFERRED',
    `confidence` DECIMAL(4, 3) NOT NULL COMMENT 'Confidence from 0.000 to 1.000',
    `evidence_count` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Number of supporting observations',
    `evidence_summary` VARCHAR(1000) DEFAULT NULL COMMENT 'Short provenance summary without raw conversation payloads',
    `last_observed_at` DATETIME NOT NULL COMMENT 'Most recent supporting observation time',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_preference_signal_source`
        (`user_id`, `preference_key`, `sentiment`, `scope`, `source`, `is_deleted`),
    KEY `idx_user_preference_signal_context`
        (`user_id`, `scope`, `preference_key`, `is_deleted`),
    KEY `idx_user_preference_signal_observed`
        (`user_id`, `last_observed_at`),
    CONSTRAINT `chk_user_preference_signal_sentiment`
        CHECK (`sentiment` IN ('PREFER', 'AVOID')),
    CONSTRAINT `chk_user_preference_signal_scope`
        CHECK (`scope` IN ('LONG_TERM', 'RECENT')),
    CONSTRAINT `chk_user_preference_signal_source`
        CHECK (`source` IN ('USER_DECLARED', 'BEHAVIOR_INFERRED', 'AGENT_INFERRED')),
    CONSTRAINT `chk_user_preference_signal_confidence`
        CHECK (`confidence` >= 0.000 AND `confidence` <= 1.000),
    CONSTRAINT `chk_user_preference_signal_evidence_count`
        CHECK (`evidence_count` >= 1),
    CONSTRAINT `fk_user_preference_signal_user`
        FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Source-aware long-term and recent user preference signals';

INSERT INTO `user_preference_signal` (
    `user_id`, `preference_key`, `preference_value`, `sentiment`, `scope`, `source`,
    `confidence`, `evidence_count`, `evidence_summary`, `last_observed_at`,
    `create_time`, `update_time`, `is_deleted`
)
SELECT
    legacy.`user_id`, legacy.`preference_key`, legacy.`preference_value`,
    'PREFER', 'LONG_TERM', 'USER_DECLARED', 1.000, 1,
    'Backfilled from user_design_preference', legacy.`update_time`,
    legacy.`create_time`, legacy.`update_time`, 0
FROM (
    SELECT `user_id`, 'project_type' AS `preference_key`, `preferred_project_type` AS `preference_value`,
           `create_time`, `update_time`
    FROM `user_design_preference`
    WHERE `preferred_project_type` IS NOT NULL AND `preferred_project_type` <> '' AND `is_deleted` = 0
    UNION ALL
    SELECT `user_id`, 'style', `preferred_style`, `create_time`, `update_time`
    FROM `user_design_preference`
    WHERE `preferred_style` IS NOT NULL AND `preferred_style` <> '' AND `is_deleted` = 0
    UNION ALL
    SELECT `user_id`, 'site_scale', `preferred_site_scale`, `create_time`, `update_time`
    FROM `user_design_preference`
    WHERE `preferred_site_scale` IS NOT NULL AND `preferred_site_scale` <> '' AND `is_deleted` = 0
    UNION ALL
    SELECT `user_id`, 'target_user', `preferred_target_user`, `create_time`, `update_time`
    FROM `user_design_preference`
    WHERE `preferred_target_user` IS NOT NULL AND `preferred_target_user` <> '' AND `is_deleted` = 0
    UNION ALL
    SELECT `user_id`, 'default_tool_id', CAST(`default_tool_id` AS CHAR), `create_time`, `update_time`
    FROM `user_design_preference`
    WHERE `default_tool_id` IS NOT NULL AND `is_deleted` = 0
) legacy;
