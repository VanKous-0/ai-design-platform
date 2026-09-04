-- Phase 1: immutable prompt revisions and current-prompt compatibility projection.

ALTER TABLE `prompt_template`
    ADD COLUMN `owner_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Owner for future user-authored prompts' AFTER `stage_id`,
    ADD COLUMN `ownership_type` VARCHAR(20) NOT NULL DEFAULT 'SYSTEM' COMMENT 'SYSTEM or USER' AFTER `owner_user_id`,
    ADD COLUMN `current_revision_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Current immutable prompt revision' AFTER `ownership_type`,
    ADD KEY `idx_prompt_template_owner` (`owner_user_id`),
    ADD CONSTRAINT `chk_prompt_template_ownership`
        CHECK ((`ownership_type` = 'SYSTEM' AND `owner_user_id` IS NULL)
            OR (`ownership_type` = 'USER' AND `owner_user_id` IS NOT NULL)),
    ADD CONSTRAINT `fk_prompt_template_owner`
        FOREIGN KEY (`owner_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT;

CREATE TABLE `prompt_revision` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `prompt_id` BIGINT UNSIGNED NOT NULL COMMENT 'Logical prompt identity',
    `revision_no` INT UNSIGNED NOT NULL COMMENT 'Monotonic revision number within a prompt',
    `content` TEXT NOT NULL COMMENT 'Immutable prompt content',
    `input_desc` TEXT DEFAULT NULL COMMENT 'Immutable input description',
    `output_desc` TEXT DEFAULT NULL COMMENT 'Immutable output description',
    `tips` TEXT DEFAULT NULL COMMENT 'Immutable usage tips',
    `example_input` TEXT DEFAULT NULL COMMENT 'Immutable example input',
    `example_output` TEXT DEFAULT NULL COMMENT 'Immutable example output',
    `parameter_schema_json` JSON NOT NULL COMMENT 'Immutable parameter definition snapshot',
    `created_by` BIGINT UNSIGNED DEFAULT NULL COMMENT 'User who created this revision',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED or RETIRED',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_revision_no` (`prompt_id`, `revision_no`),
    UNIQUE KEY `uk_prompt_revision_prompt_id` (`prompt_id`, `id`),
    KEY `idx_prompt_revision_created_by` (`created_by`),
    CONSTRAINT `chk_prompt_revision_no` CHECK (`revision_no` >= 1),
    CONSTRAINT `chk_prompt_revision_status` CHECK (`status` IN ('PUBLISHED', 'RETIRED')),
    CONSTRAINT `chk_prompt_revision_parameter_schema`
        CHECK (JSON_TYPE(`parameter_schema_json`) = 'ARRAY'),
    CONSTRAINT `fk_prompt_revision_prompt`
        FOREIGN KEY (`prompt_id`) REFERENCES `prompt_template` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_prompt_revision_created_by`
        FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Immutable prompt revision history';

INSERT INTO `prompt_revision` (
    `prompt_id`, `revision_no`, `content`, `input_desc`, `output_desc`, `tips`,
    `example_input`, `example_output`, `parameter_schema_json`, `created_by`, `status`, `create_time`
)
SELECT
    p.`id`,
    1,
    p.`content`,
    p.`input_desc`,
    p.`output_desc`,
    p.`tips`,
    p.`example_input`,
    p.`example_output`,
    COALESCE(
        (
            SELECT JSON_ARRAYAGG(JSON_OBJECT(
                'paramKey', pp.`param_key`,
                'paramName', pp.`param_name`,
                'paramType', pp.`param_type`,
                'required', pp.`required`,
                'defaultValue', pp.`default_value`,
                'placeholder', pp.`placeholder`,
                'sortOrder', pp.`sort_order`
            ))
            FROM `prompt_parameter` pp
            WHERE pp.`prompt_id` = p.`id`
              AND pp.`is_deleted` = 0
        ),
        JSON_ARRAY()
    ),
    NULL,
    'PUBLISHED',
    p.`create_time`
FROM `prompt_template` p;

UPDATE `prompt_template` p
JOIN `prompt_revision` r
  ON r.`prompt_id` = p.`id`
 AND r.`revision_no` = 1
SET p.`current_revision_id` = r.`id`;

ALTER TABLE `prompt_template`
    ADD UNIQUE KEY `uk_prompt_template_current_revision` (`id`, `current_revision_id`),
    ADD CONSTRAINT `fk_prompt_template_current_revision`
        FOREIGN KEY (`id`, `current_revision_id`)
        REFERENCES `prompt_revision` (`prompt_id`, `id`) ON DELETE RESTRICT;
