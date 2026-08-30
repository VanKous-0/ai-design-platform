-- User rating schema.
-- Execute after sql/schema.sql, sql/ai_tool_schema.sql, and sql/workflow_runtime_schema.sql.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `user_tool_rating` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `tool_id` BIGINT UNSIGNED NOT NULL COMMENT 'AI tool ID',
    `effect_score` DECIMAL(3, 1) NOT NULL COMMENT 'Effect score, 0-10',
    `ease_score` DECIMAL(3, 1) NOT NULL COMMENT 'Ease score, 0-10',
    `stability_score` DECIMAL(3, 1) NOT NULL COMMENT 'Stability score, 0-10',
    `recommend_score` DECIMAL(3, 1) NOT NULL COMMENT 'Recommend score, 0-10',
    `comment` TEXT DEFAULT NULL COMMENT 'User comment',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tool_rating` (`user_id`, `tool_id`),
    KEY `idx_user_tool_rating_tool` (`tool_id`),
    KEY `idx_user_tool_rating_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Normal user AI tool rating table';

CREATE TABLE IF NOT EXISTS `user_workflow_rating` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `template_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template ID',
    `instance_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow instance ID',
    `effect_score` DECIMAL(3, 1) NOT NULL COMMENT 'Effect score, 0-10',
    `ease_score` DECIMAL(3, 1) NOT NULL COMMENT 'Ease score, 0-10',
    `stability_score` DECIMAL(3, 1) NOT NULL COMMENT 'Stability score, 0-10',
    `recommend_score` DECIMAL(3, 1) NOT NULL COMMENT 'Recommend score, 0-10',
    `comment` TEXT DEFAULT NULL COMMENT 'User comment',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_workflow_rating` (`user_id`, `template_id`, `instance_id`),
    KEY `idx_user_workflow_rating_template` (`template_id`),
    KEY `idx_user_workflow_rating_instance` (`instance_id`),
    KEY `idx_user_workflow_rating_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Normal user workflow rating table';
