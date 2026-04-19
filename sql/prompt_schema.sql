-- Prompt library module schema.
-- Execute after workflow_schema.sql and ai_tool_schema.sql.

CREATE DATABASE IF NOT EXISTS `ai_design_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `ai_design_platform`;

CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stage_id` BIGINT UNSIGNED NOT NULL COMMENT '所属工作流阶段ID，逻辑关联 workflow_stage.id',
    `title` VARCHAR(150) NOT NULL COMMENT '提示词标题',
    `code` VARCHAR(80) NOT NULL COMMENT '提示词稳定编码',
    `category` VARCHAR(50) NOT NULL COMMENT '提示词分类，例如概念生成、分析总结、表达优化',
    `content` TEXT NOT NULL COMMENT '提示词正文',
    `input_desc` TEXT DEFAULT NULL COMMENT '输入说明',
    `output_desc` TEXT DEFAULT NULL COMMENT '输出说明',
    `tips` TEXT DEFAULT NULL COMMENT '使用提示与注意事项',
    `example_input` TEXT DEFAULT NULL COMMENT '示例输入',
    `example_output` TEXT DEFAULT NULL COMMENT '示例输出',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    `copy_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '复制次数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_template_code` (`code`),
    KEY `idx_prompt_template_stage_sort` (`stage_id`, `sort_order`),
    KEY `idx_prompt_template_category` (`category`),
    KEY `idx_prompt_template_status_sort` (`status`, `sort_order`),
    KEY `idx_prompt_template_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '提示词模板表，stage_id 逻辑关联 workflow_stage.id';

CREATE TABLE IF NOT EXISTS `prompt_tool_rel` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `prompt_id` BIGINT UNSIGNED NOT NULL COMMENT '提示词ID，逻辑关联 prompt_template.id',
    `tool_id` BIGINT UNSIGNED NOT NULL COMMENT 'AI工具ID，逻辑关联 ai_tool.id',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_tool` (`prompt_id`, `tool_id`),
    KEY `idx_prompt_tool_rel_prompt` (`prompt_id`),
    KEY `idx_prompt_tool_rel_tool` (`tool_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '提示词推荐工具关系表';
