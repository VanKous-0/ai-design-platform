-- Review module schema.
-- Execute after schema.sql, workflow_schema.sql and ai_tool_schema.sql.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `review_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '提交用户ID，逻辑关联 sys_user.id',
    `title` VARCHAR(150) NOT NULL COMMENT '复盘标题',
    `code` VARCHAR(80) NOT NULL COMMENT '复盘稳定编码',
    `stage_id` BIGINT UNSIGNED NOT NULL COMMENT '所属工作流阶段ID，逻辑关联 workflow_stage.id',
    `tool_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '主要使用的AI工具ID，可为空，逻辑关联 ai_tool.id',
    `project_name` VARCHAR(150) DEFAULT NULL COMMENT '复盘对应项目名',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '复盘摘要',
    `problem_desc` TEXT DEFAULT NULL COMMENT '问题描述',
    `solution_desc` TEXT DEFAULT NULL COMMENT '解决方案描述',
    `reflection` TEXT DEFAULT NULL COMMENT '经验总结与反思',
    `score` DECIMAL(4, 1) DEFAULT NULL COMMENT '本次复盘自评分',
    `review_date` DATE DEFAULT NULL COMMENT '复盘日期',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_record_code` (`code`),
    KEY `idx_review_record_user` (`user_id`),
    KEY `idx_review_record_stage_sort` (`stage_id`, `sort_order`),
    KEY `idx_review_record_tool` (`tool_id`),
    KEY `idx_review_record_status_sort` (`status`, `sort_order`),
    KEY `idx_review_record_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户复盘记录表';

CREATE TABLE IF NOT EXISTS `review_asset` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `review_id` BIGINT UNSIGNED NOT NULL COMMENT '所属复盘ID，逻辑关联 review_record.id',
    `asset_type` VARCHAR(20) NOT NULL COMMENT '附件类型：image / file',
    `asset_url` VARCHAR(500) NOT NULL COMMENT '附件地址，第一版仅保存URL或路径',
    `title` VARCHAR(150) DEFAULT NULL COMMENT '附件标题',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '附件说明',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_review_asset_review_sort` (`review_id`, `sort_order`),
    KEY `idx_review_asset_type` (`asset_type`),
    KEY `idx_review_asset_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户复盘附件表';
