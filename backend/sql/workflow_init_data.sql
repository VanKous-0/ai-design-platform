-- Workflow module test data.
-- Execute after sql/workflow_schema.sql.

INSERT INTO `workflow_stage` (`id`, `name`, `code`, `description`, `sort_order`, `status`, `is_deleted`)
VALUES
    (1, '前期调研', 'RESEARCH', '收集场地、用户、政策、案例等基础信息，形成设计依据。', 10, 1, 0),
    (2, '概念设计', 'CONCEPT', '围绕设计目标生成概念方向、空间策略和初步方案。', 20, 1, 0),
    (3, '方案深化', 'DEVELOPMENT', '深化平面、立面、剖面、流线、功能与表达方式。', 30, 1, 0),
    (4, '成果表达', 'PRESENTATION', '整理图纸、文本、模型、汇报材料与成果展示内容。', 40, 1, 0)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `is_deleted` = 0;

INSERT INTO `workflow_step` (
    `id`,
    `stage_id`,
    `title`,
    `content`,
    `input_desc`,
    `output_desc`,
    `tips`,
    `sort_order`,
    `status`,
    `is_deleted`
)
VALUES
    (1, 1, '场地信息整理', '整理基地位置、周边交通、自然条件、历史文脉和限制条件。', '场地照片、测绘资料、规划条件、任务书', '场地分析要点清单', '注意区分客观条件和主观判断。', 10, 1, 0),
    (2, 1, '案例与规范调研', '收集同类型建筑案例，梳理功能组织、空间体验、技术策略和相关规范。', '案例资料、规范条文、竞品项目', '案例分析表与规范约束摘要', '案例不是照搬，重点提炼可迁移策略。', 20, 1, 0),
    (3, 2, '设计概念生成', '基于任务目标和调研结论，提出多个可比较的概念方向。', '设计目标、调研结论、用户画像', '概念关键词、草图方向、设计叙事', '保留多个方向，避免过早收敛。', 10, 1, 0),
    (4, 2, '空间策略推演', '推演功能分区、空间序列、流线组织和体量关系。', '概念方向、功能需求、面积指标', '空间策略图与体量草案', '检查策略是否能回应前期问题。', 20, 1, 0),
    (5, 3, '平立剖深化', '深化平面布局、立面语言、剖面关系和关键节点。', '概念方案、空间策略、反馈意见', '深化图纸和节点说明', '同步检查功能、结构、消防和表达一致性。', 10, 1, 0),
    (6, 4, '成果汇报整理', '整理设计说明、图纸、模型截图、分析图和展示文本。', '最终方案、过程材料、图纸模型', '汇报文件与展示成果', '表达逻辑应服务于设计主线。', 10, 1, 0)
ON DUPLICATE KEY UPDATE
    `stage_id` = VALUES(`stage_id`),
    `title` = VALUES(`title`),
    `content` = VALUES(`content`),
    `input_desc` = VALUES(`input_desc`),
    `output_desc` = VALUES(`output_desc`),
    `tips` = VALUES(`tips`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `is_deleted` = 0;
