-- Prompt enhancement demo data.
-- Execute after prompt_enhance_schema.sql and workflow_runtime_init_data.sql.
-- The relation inserts use existing prompt_template rows by sort order because old prompt codes may vary.
-- If your local prompts are different, adjust the selected prompt IDs.

USE `ai_design_platform`;

INSERT INTO `workflow_node_prompt_rel` (`node_id`, `prompt_id`, `sort_order`)
SELECT n.id, p.id, 10
FROM workflow_template_node n
JOIN workflow_template t ON t.id = n.template_id
JOIN prompt_template p ON p.status = 1 AND p.is_deleted = 0
WHERE t.code = 'building_concept_generation'
  AND n.node_code IN ('concept_keywords', 'concept_strategy')
ORDER BY p.sort_order, p.id
LIMIT 4
ON DUPLICATE KEY UPDATE `sort_order` = VALUES(`sort_order`);

INSERT INTO `workflow_node_prompt_rel` (`node_id`, `prompt_id`, `sort_order`)
SELECT n.id, p.id, 10
FROM workflow_template_node n
JOIN workflow_template t ON t.id = n.template_id
JOIN prompt_template p ON p.status = 1 AND p.is_deleted = 0
WHERE t.code = 'landscape_site_analysis'
  AND n.node_code IN ('site_issue_opportunity', 'landscape_strategy')
ORDER BY p.sort_order, p.id
LIMIT 4
ON DUPLICATE KEY UPDATE `sort_order` = VALUES(`sort_order`);

INSERT INTO `prompt_parameter`
(`prompt_id`, `param_key`, `param_name`, `param_type`, `required`, `default_value`, `placeholder`, `sort_order`)
SELECT p.id, 'projectType', '项目类型', 'text', 1, NULL, '例如：校园景观、社区图书馆', 10
FROM prompt_template p WHERE p.status = 1 AND p.is_deleted = 0 ORDER BY p.sort_order, p.id LIMIT 1
ON DUPLICATE KEY UPDATE `param_name` = VALUES(`param_name`), `required` = VALUES(`required`), `placeholder` = VALUES(`placeholder`);

INSERT INTO `prompt_parameter`
(`prompt_id`, `param_key`, `param_name`, `param_type`, `required`, `default_value`, `placeholder`, `sort_order`)
SELECT p.id, 'area', '项目面积', 'text', 0, '未明确', '例如：3000平方米', 20
FROM prompt_template p WHERE p.status = 1 AND p.is_deleted = 0 ORDER BY p.sort_order, p.id LIMIT 1
ON DUPLICATE KEY UPDATE `param_name` = VALUES(`param_name`), `default_value` = VALUES(`default_value`), `placeholder` = VALUES(`placeholder`);

INSERT INTO `prompt_parameter`
(`prompt_id`, `param_key`, `param_name`, `param_type`, `required`, `default_value`, `placeholder`, `sort_order`)
SELECT p.id, 'style', '设计风格', 'text', 0, '现代简洁', '例如：生态自然', 30
FROM prompt_template p WHERE p.status = 1 AND p.is_deleted = 0 ORDER BY p.sort_order, p.id LIMIT 1
ON DUPLICATE KEY UPDATE `param_name` = VALUES(`param_name`), `default_value` = VALUES(`default_value`), `placeholder` = VALUES(`placeholder`);

INSERT INTO `prompt_parameter`
(`prompt_id`, `param_key`, `param_name`, `param_type`, `required`, `default_value`, `placeholder`, `sort_order`)
SELECT p.id, 'targetUser', '目标用户', 'text', 0, '普通公众', '例如：大学生、社区居民', 40
FROM prompt_template p WHERE p.status = 1 AND p.is_deleted = 0 ORDER BY p.sort_order, p.id LIMIT 1
ON DUPLICATE KEY UPDATE `param_name` = VALUES(`param_name`), `default_value` = VALUES(`default_value`), `placeholder` = VALUES(`placeholder`);
