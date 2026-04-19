-- Site home content module schema.
-- Execute after schema.sql.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `site_content` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `section_key` VARCHAR(80) NOT NULL COMMENT 'Home section key, for example hero, intro, workflow_entry',
    `title` VARCHAR(150) NOT NULL COMMENT 'Section title',
    `subtitle` VARCHAR(255) DEFAULT NULL COMMENT 'Section subtitle',
    `content` TEXT DEFAULT NULL COMMENT 'Section body content',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT 'Image URL or local path string',
    `link_url` VARCHAR(500) DEFAULT NULL COMMENT 'Jump link URL or route path',
    `extra_json` TEXT DEFAULT NULL COMMENT 'Reserved structured extension data in JSON string format',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Display order within the same section_key',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1 enabled, 0 disabled',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    KEY `idx_site_content_section_sort` (`section_key`, `sort_order`),
    KEY `idx_site_content_status_sort` (`status`, `sort_order`),
    KEY `idx_site_content_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Home page content configuration table';

CREATE TABLE IF NOT EXISTS `award_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `title` VARCHAR(150) NOT NULL COMMENT 'Award or achievement title',
    `award_level` VARCHAR(80) DEFAULT NULL COMMENT 'Award level, for example school, city, national, other',
    `issuer` VARCHAR(150) DEFAULT NULL COMMENT 'Issuer organization',
    `award_date` DATE DEFAULT NULL COMMENT 'Award or achievement date',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT 'Summary',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT 'Award image URL or local path string',
    `link_url` VARCHAR(500) DEFAULT NULL COMMENT 'Detail link URL or route path',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1 enabled, 0 disabled',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    KEY `idx_award_record_date` (`award_date`),
    KEY `idx_award_record_status_sort` (`status`, `sort_order`),
    KEY `idx_award_record_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Award and achievement record table';
