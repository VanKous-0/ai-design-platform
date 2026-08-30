-- User profile and parameter memory schema.
-- Execute after sql/schema.sql.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `user_profile` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT 'Real name',
    `school` VARCHAR(100) DEFAULT NULL COMMENT 'School',
    `major` VARCHAR(100) DEFAULT NULL COMMENT 'Major',
    `grade` VARCHAR(50) DEFAULT NULL COMMENT 'Grade',
    `phone` VARCHAR(30) DEFAULT NULL COMMENT 'Phone number',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT 'Bio',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT 'Avatar URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_profile_user` (`user_id`),
    KEY `idx_user_profile_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'User profile table';

CREATE TABLE IF NOT EXISTS `user_design_preference` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `preferred_project_type` VARCHAR(100) DEFAULT NULL COMMENT 'Preferred project type',
    `preferred_style` VARCHAR(100) DEFAULT NULL COMMENT 'Preferred design style',
    `preferred_site_scale` VARCHAR(100) DEFAULT NULL COMMENT 'Preferred site scale',
    `preferred_target_user` VARCHAR(100) DEFAULT NULL COMMENT 'Preferred target user',
    `default_tool_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Default AI tool ID',
    `extra_json` TEXT DEFAULT NULL COMMENT 'Reserved JSON string',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_design_preference_user` (`user_id`),
    KEY `idx_user_design_preference_tool` (`default_tool_id`),
    KEY `idx_user_design_preference_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'User design preference table';

CREATE TABLE IF NOT EXISTS `user_recent_parameter` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `parameter_type` VARCHAR(50) NOT NULL COMMENT 'Parameter type',
    `parameter_key` VARCHAR(100) NOT NULL COMMENT 'Parameter key',
    `parameter_value` VARCHAR(500) NOT NULL COMMENT 'Parameter value',
    `source` VARCHAR(50) DEFAULT NULL COMMENT 'Source scene',
    `use_count` INT NOT NULL DEFAULT 1 COMMENT 'Use count',
    `last_used_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last used time',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_recent_parameter_value` (`user_id`, `parameter_key`, `parameter_value`),
    KEY `idx_user_recent_parameter_user_time` (`user_id`, `last_used_time`),
    KEY `idx_user_recent_parameter_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'User recent parameter table';
