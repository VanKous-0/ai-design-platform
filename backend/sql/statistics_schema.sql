-- Statistics and feedback schema.
-- Execute after sql/schema.sql.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `usage_event` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'User ID when logged in',
    `anonymous_id` VARCHAR(100) DEFAULT NULL COMMENT 'Anonymous visitor ID',
    `event_type` VARCHAR(50) NOT NULL COMMENT 'Event type',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT 'Target type',
    `target_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Target ID',
    `page_url` VARCHAR(500) DEFAULT NULL COMMENT 'Page URL',
    `stay_duration` INT DEFAULT NULL COMMENT 'Stay duration in seconds',
    `input_summary` VARCHAR(500) DEFAULT NULL COMMENT 'Short input summary',
    `extra_json` TEXT DEFAULT NULL COMMENT 'Extra JSON string, not parsed by backend',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    KEY `idx_usage_event_user_id` (`user_id`),
    KEY `idx_usage_event_anonymous_id` (`anonymous_id`),
    KEY `idx_usage_event_event_type` (`event_type`),
    KEY `idx_usage_event_target` (`target_type`, `target_id`),
    KEY `idx_usage_event_create_time` (`create_time`),
    KEY `idx_usage_event_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'User behavior event table';

CREATE TABLE IF NOT EXISTS `survey_feedback` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'User ID when logged in',
    `anonymous_id` VARCHAR(100) DEFAULT NULL COMMENT 'Anonymous visitor ID',
    `scene` VARCHAR(100) NOT NULL COMMENT 'Feedback scene',
    `score` DECIMAL(3, 1) NOT NULL COMMENT 'Score, 0-10',
    `content` TEXT DEFAULT NULL COMMENT 'Feedback content',
    `contact` VARCHAR(100) DEFAULT NULL COMMENT 'Optional contact info',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    KEY `idx_survey_feedback_user_id` (`user_id`),
    KEY `idx_survey_feedback_scene` (`scene`),
    KEY `idx_survey_feedback_create_time` (`create_time`),
    KEY `idx_survey_feedback_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Survey feedback table';
