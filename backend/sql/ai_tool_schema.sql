-- AI tool evaluation module schema.
-- Execute after sql/workflow_schema.sql because ai_tool_stage_rel references workflow_stage.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `ai_tool` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '工具展示名称',
    `code` VARCHAR(50) NOT NULL COMMENT '工具稳定编码',
    `official_url` VARCHAR(500) DEFAULT NULL COMMENT '工具官网或使用入口',
    `logo_url` VARCHAR(500) DEFAULT NULL COMMENT '工具Logo地址',
    `description` TEXT DEFAULT NULL COMMENT '工具说明',
    `price_desc` VARCHAR(255) DEFAULT NULL COMMENT '价格说明',
    `version_desc` VARCHAR(255) DEFAULT NULL COMMENT '版本说明',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_tool_code` (`code`),
    KEY `idx_ai_tool_name` (`name`),
    KEY `idx_ai_tool_status` (`status`),
    KEY `idx_ai_tool_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'AI工具基础信息表';

CREATE TABLE IF NOT EXISTS `ai_tool_stage_rel` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tool_id` BIGINT UNSIGNED NOT NULL COMMENT 'AI工具ID',
    `stage_id` BIGINT UNSIGNED NOT NULL COMMENT '适用工作流阶段ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_tool_stage` (`tool_id`, `stage_id`),
    KEY `idx_ai_tool_stage_rel_tool` (`tool_id`),
    KEY `idx_ai_tool_stage_rel_stage` (`stage_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'AI工具适用阶段关系表，tool_id 逻辑关联 ai_tool.id，stage_id 逻辑关联 workflow_stage.id';

CREATE TABLE IF NOT EXISTS `evaluation_dimension` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '测评维度名称',
    `code` VARCHAR(50) NOT NULL COMMENT '测评维度编码',
    `description` TEXT DEFAULT NULL COMMENT '测评维度说明',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_evaluation_dimension_code` (`code`),
    KEY `idx_evaluation_dimension_status_sort` (`status`, `sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'AI工具测评维度表';

CREATE TABLE IF NOT EXISTS `ai_tool_evaluation` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tool_id` BIGINT UNSIGNED NOT NULL COMMENT 'AI工具ID',
    `dimension_id` BIGINT UNSIGNED NOT NULL COMMENT '测评维度ID',
    `score` DECIMAL(4, 1) NOT NULL COMMENT '评分，支持一位小数',
    `comment` TEXT DEFAULT NULL COMMENT '评分说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_tool_evaluation` (`tool_id`, `dimension_id`),
    KEY `idx_ai_tool_evaluation_tool` (`tool_id`),
    KEY `idx_ai_tool_evaluation_dimension` (`dimension_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'AI工具维度评分表，tool_id 逻辑关联 ai_tool.id，dimension_id 逻辑关联 evaluation_dimension.id';
