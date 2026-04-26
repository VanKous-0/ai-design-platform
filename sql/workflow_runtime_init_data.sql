-- Workflow runtime demo data.
-- Execute after workflow_runtime_schema.sql.
-- This script uses codes from existing init scripts instead of fixed IDs where possible.
-- If your local workflow_stage/workflow_step codes differ, adjust the subqueries.

USE `ai_design_platform`;

INSERT INTO `workflow_template`
(`name`, `code`, `description`, `scene_type`, `cover_url`, `sort_order`, `status`)
VALUES
('建筑概念方案生成流程', 'building_concept_generation', '从场地理解到概念表达的典型建筑方案生成流程。', 'building_concept', NULL, 10, 1),
('景观场地分析流程', 'landscape_site_analysis', '面向景观设计的场地条件分析、策略生成和表达流程。', 'landscape_analysis', NULL, 20, 1)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`description` = VALUES(`description`),
`scene_type` = VALUES(`scene_type`),
`sort_order` = VALUES(`sort_order`),
`status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT
    t.id,
    (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'concept_design' LIMIT 1),
    NULL,
    '需求与场地信息整理',
    'brief_site_collect',
    'input',
    '输入项目类型、面积、基地条件、目标用户等基础信息。',
    '形成结构化设计任务书。',
    '下一步可以基于任务书生成概念方向。',
    10,
    1
FROM workflow_template t WHERE t.code = 'building_concept_generation'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'concept_design' LIMIT 1), NULL,
       '概念关键词生成', 'concept_keywords', 'ai_prompt',
       '输入任务书和设计偏好。', '输出概念关键词与设计主题。',
       '下一步可以把关键词扩展为完整方案叙事。', 20, 1
FROM workflow_template t WHERE t.code = 'building_concept_generation'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'concept_design' LIMIT 1), NULL,
       '方案叙事与空间策略', 'concept_strategy', 'ai_prompt',
       '输入概念关键词和功能需求。', '输出空间策略、动线和设计说明。',
       '下一步可以整理输出并进入成果表达。', 30, 1
FROM workflow_template t WHERE t.code = 'building_concept_generation'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'site_analysis' LIMIT 1), NULL,
       '场地基础条件梳理', 'site_basic_analysis', 'input',
       '输入区位、面积、周边环境、交通和人群信息。', '输出场地条件摘要。',
       '下一步可以分析问题与机会点。', 10, 1
FROM workflow_template t WHERE t.code = 'landscape_site_analysis'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'site_analysis' LIMIT 1), NULL,
       '问题机会点提炼', 'site_issue_opportunity', 'ai_prompt',
       '输入场地条件摘要。', '输出问题、机会与设计突破口。',
       '下一步可以生成景观设计策略。', 20, 1
FROM workflow_template t WHERE t.code = 'landscape_site_analysis'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'concept_design' LIMIT 1), NULL,
       '景观策略生成', 'landscape_strategy', 'ai_prompt',
       '输入问题机会点和设计目标。', '输出空间结构、生态策略和活动策划。',
       '流程已完成，可以整理为设计说明或图纸表达。', 30, 1
FROM workflow_template t WHERE t.code = 'landscape_site_analysis'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);
