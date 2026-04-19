-- Case showcase module schema.
-- Execute after workflow_schema.sql and ai_tool_schema.sql.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `case_project` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(150) NOT NULL COMMENT '案例标题',
    `code` VARCHAR(80) NOT NULL COMMENT '案例稳定编码',
    `stage_id` BIGINT UNSIGNED NOT NULL COMMENT '所属工作流阶段ID，逻辑关联 workflow_stage.id',
    `tool_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '主要使用的AI工具ID，可为空，逻辑关联 ai_tool.id',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '案例封面图地址',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '案例列表摘要',
    `content` TEXT DEFAULT NULL COMMENT '案例详细说明',
    `source_desc` VARCHAR(255) DEFAULT NULL COMMENT '案例来源或备注说明',
    `author_name` VARCHAR(100) DEFAULT NULL COMMENT '作者或整理人名称',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_case_project_code` (`code`),
    KEY `idx_case_project_stage_sort` (`stage_id`, `sort_order`),
    KEY `idx_case_project_tool` (`tool_id`),
    KEY `idx_case_project_status_sort` (`status`, `sort_order`),
    KEY `idx_case_project_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '案例成果项目表';

CREATE TABLE IF NOT EXISTS `case_asset` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `case_id` BIGINT UNSIGNED NOT NULL COMMENT '所属案例ID，逻辑关联 case_project.id',
    `asset_type` VARCHAR(20) NOT NULL COMMENT '资源类型：image / file',
    `asset_url` VARCHAR(500) NOT NULL COMMENT '资源地址，第一版仅保存URL或路径',
    `title` VARCHAR(150) DEFAULT NULL COMMENT '资源标题',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '资源说明',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_case_asset_case_sort` (`case_id`, `sort_order`),
    KEY `idx_case_asset_type` (`asset_type`),
    KEY `idx_case_asset_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '案例成果资源表';
