CREATE TABLE IF NOT EXISTS `workflow_step_iteration` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `instance_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow instance ID',
    `node_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template node ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Owner user ID',
    `iteration_no` INT NOT NULL COMMENT 'Iteration number within the node',
    `tool_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'AI tool used externally',
    `prompt_content` TEXT DEFAULT NULL COMMENT 'Prompt used for this iteration',
    `output_content` MEDIUMTEXT DEFAULT NULL COMMENT 'External AI output or result summary',
    `result_url` VARCHAR(500) DEFAULT NULL COMMENT 'External or static result URL',
    `effect_score` TINYINT DEFAULT NULL COMMENT 'Visual or task effect score, 1-10',
    `accuracy_score` TINYINT DEFAULT NULL COMMENT 'Requirement accuracy score, 1-10',
    `controllability_score` TINYINT DEFAULT NULL COMMENT 'Output controllability score, 1-10',
    `usability_score` TINYINT DEFAULT NULL COMMENT 'Downstream usability score, 1-10',
    `improvement_note` TEXT DEFAULT NULL COMMENT 'Problems and next-round improvement plan',
    `selected` TINYINT NOT NULL DEFAULT 0 COMMENT 'Whether this is the selected result',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_step_iteration_no` (`instance_id`, `node_id`, `iteration_no`),
    KEY `idx_workflow_step_iteration_user` (`user_id`),
    KEY `idx_workflow_step_iteration_tool` (`tool_id`),
    KEY `idx_workflow_step_iteration_selected` (`instance_id`, `node_id`, `selected`),
    CONSTRAINT `chk_workflow_step_iteration_effect`
        CHECK (`effect_score` IS NULL OR (`effect_score` BETWEEN 1 AND 10)),
    CONSTRAINT `chk_workflow_step_iteration_accuracy`
        CHECK (`accuracy_score` IS NULL OR (`accuracy_score` BETWEEN 1 AND 10)),
    CONSTRAINT `chk_workflow_step_iteration_controllability`
        CHECK (`controllability_score` IS NULL OR (`controllability_score` BETWEEN 1 AND 10)),
    CONSTRAINT `chk_workflow_step_iteration_usability`
        CHECK (`usability_score` IS NULL OR (`usability_score` BETWEEN 1 AND 10))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow step external AI result iterations';
