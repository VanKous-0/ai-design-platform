-- Workflow runtime schema.
-- Execute after sql/schema.sql, sql/workflow_schema.sql.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `workflow_template` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(100) NOT NULL COMMENT 'Template name',
    `code` VARCHAR(80) NOT NULL COMMENT 'Stable template code',
    `description` TEXT DEFAULT NULL COMMENT 'Template description',
    `scene_type` VARCHAR(80) DEFAULT NULL COMMENT 'Typical scenario type',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT 'Cover image URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1 enabled, 0 disabled',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_template_code` (`code`),
    KEY `idx_workflow_template_status_sort` (`status`, `sort_order`),
    KEY `idx_workflow_template_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow template table';

CREATE TABLE IF NOT EXISTS `workflow_template_node` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `template_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template ID',
    `stage_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Optional existing workflow_stage ID',
    `step_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Optional existing workflow_step ID',
    `node_name` VARCHAR(150) NOT NULL COMMENT 'Node name',
    `node_code` VARCHAR(80) NOT NULL COMMENT 'Node code in template',
    `node_type` VARCHAR(50) DEFAULT NULL COMMENT 'Node type',
    `input_desc` TEXT DEFAULT NULL COMMENT 'Input description',
    `output_desc` TEXT DEFAULT NULL COMMENT 'Output description',
    `next_tip` TEXT DEFAULT NULL COMMENT 'Tip after this node is completed',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Node order',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1 enabled, 0 disabled',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_template_node_code` (`template_id`, `node_code`),
    KEY `idx_workflow_template_node_template_sort` (`template_id`, `sort_order`),
    KEY `idx_workflow_template_node_stage` (`stage_id`),
    KEY `idx_workflow_template_node_step` (`step_id`),
    KEY `idx_workflow_template_node_status` (`status`),
    KEY `idx_workflow_template_node_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow template node table';

CREATE TABLE IF NOT EXISTS `workflow_instance` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `template_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Owner user ID',
    `title` VARCHAR(150) DEFAULT NULL COMMENT 'Instance title',
    `current_node_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Current node ID',
    `status` VARCHAR(30) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING / FINISHED',
    `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Start time',
    `finish_time` DATETIME DEFAULT NULL COMMENT 'Finish time',
    `progress` DECIMAL(5, 2) NOT NULL DEFAULT 0.00 COMMENT 'Progress percent',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    KEY `idx_workflow_instance_user` (`user_id`),
    KEY `idx_workflow_instance_template` (`template_id`),
    KEY `idx_workflow_instance_status` (`status`),
    KEY `idx_workflow_instance_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow execution instance table';

CREATE TABLE IF NOT EXISTS `workflow_step_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `instance_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow instance ID',
    `node_id` BIGINT UNSIGNED NOT NULL COMMENT 'Template node ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Operator user ID',
    `input_content` TEXT DEFAULT NULL COMMENT 'User input',
    `output_content` TEXT DEFAULT NULL COMMENT 'User output',
    `status` VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' COMMENT 'Step status',
    `duration_seconds` INT DEFAULT NULL COMMENT 'Duration in seconds',
    `started_at` DATETIME DEFAULT NULL COMMENT 'Step start time',
    `completed_at` DATETIME DEFAULT NULL COMMENT 'Step completed time',
    `next_suggestion` TEXT DEFAULT NULL COMMENT 'Next step suggestion',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_step_record_instance_node` (`instance_id`, `node_id`),
    KEY `idx_workflow_step_record_user` (`user_id`),
    KEY `idx_workflow_step_record_node` (`node_id`),
    KEY `idx_workflow_step_record_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow step execution record table';
