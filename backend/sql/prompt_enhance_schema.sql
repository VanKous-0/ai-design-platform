-- Prompt enhancement schema.
-- Execute after sql/prompt_schema.sql and sql/workflow_runtime_schema.sql.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `workflow_node_prompt_rel` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `node_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template node ID',
    `prompt_id` BIGINT UNSIGNED NOT NULL COMMENT 'Prompt template ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_node_prompt` (`node_id`, `prompt_id`),
    KEY `idx_workflow_node_prompt_node` (`node_id`),
    KEY `idx_workflow_node_prompt_prompt` (`prompt_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow node prompt relation table';

CREATE TABLE IF NOT EXISTS `prompt_parameter` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `prompt_id` BIGINT UNSIGNED NOT NULL COMMENT 'Prompt template ID',
    `param_key` VARCHAR(80) NOT NULL COMMENT 'Placeholder key without braces',
    `param_name` VARCHAR(100) NOT NULL COMMENT 'Parameter display name',
    `param_type` VARCHAR(30) NOT NULL DEFAULT 'text' COMMENT 'Parameter type',
    `required` TINYINT NOT NULL DEFAULT 0 COMMENT 'Required: 1 yes, 0 no',
    `default_value` VARCHAR(500) DEFAULT NULL COMMENT 'Default value',
    `placeholder` VARCHAR(255) DEFAULT NULL COMMENT 'Frontend placeholder',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_parameter_key` (`prompt_id`, `param_key`),
    KEY `idx_prompt_parameter_prompt_sort` (`prompt_id`, `sort_order`),
    KEY `idx_prompt_parameter_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Prompt parameter definition table';
