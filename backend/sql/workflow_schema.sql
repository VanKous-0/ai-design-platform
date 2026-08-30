-- Workflow module schema.
-- Execute after sql/schema.sql if this is a fresh database.

CREATE TABLE IF NOT EXISTS `workflow_stage` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '阶段名称',
    `code` VARCHAR(50) NOT NULL COMMENT '阶段编码',
    `description` TEXT DEFAULT NULL COMMENT '阶段说明',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_stage_code` (`code`),
    KEY `idx_workflow_stage_status_sort` (`status`, `sort_order`),
    KEY `idx_workflow_stage_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '工作流设计阶段表';

CREATE TABLE IF NOT EXISTS `workflow_step` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stage_id` BIGINT UNSIGNED NOT NULL COMMENT '所属设计阶段ID',
    `title` VARCHAR(150) NOT NULL COMMENT '步骤标题',
    `content` TEXT NOT NULL COMMENT '步骤内容',
    `input_desc` TEXT DEFAULT NULL COMMENT '输入说明',
    `output_desc` TEXT DEFAULT NULL COMMENT '输出说明',
    `tips` TEXT DEFAULT NULL COMMENT '提示与注意事项',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_workflow_step_stage_sort` (`stage_id`, `sort_order`),
    KEY `idx_workflow_step_status_sort` (`status`, `sort_order`),
    KEY `idx_workflow_step_deleted` (`is_deleted`),
    CONSTRAINT `fk_workflow_step_stage`
        FOREIGN KEY (`stage_id`) REFERENCES `workflow_stage` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '工作流步骤表';
