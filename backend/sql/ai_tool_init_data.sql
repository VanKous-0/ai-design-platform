-- AI tool evaluation module test data.
-- Execute after sql/ai_tool_schema.sql and sql/workflow_init_data.sql.
-- These records are only for development/demo. They are not final evaluation conclusions.

USE `ai_design_platform`;

INSERT INTO `ai_tool` (
    `id`,
    `name`,
    `code`,
    `official_url`,
    `logo_url`,
    `description`,
    `price_desc`,
    `version_desc`,
    `status`,
    `is_deleted`
)
VALUES
    (1, '豆包', 'DOUBAO', 'https://www.doubao.com/', NULL, '面向通用问答、文本生成、资料整理和多模态使用场景的 AI 工具。', '提供免费额度，具体价格以官网为准。', 'Web / App 版本持续更新', 1, 0),
    (2, 'DeepSeek', 'DEEPSEEK', 'https://www.deepseek.com/', NULL, '适合文本推理、方案分析、代码辅助和长文本处理的 AI 工具。', '提供免费使用入口，API 价格以官网为准。', '模型版本持续更新', 1, 0),
    (3, '通义', 'TONGYI', 'https://tongyi.aliyun.com/', NULL, '阿里云通义系列 AI 产品，适合文本生成、知识问答和办公辅助。', '免费与商业能力并存，具体以官网为准。', 'Web / App / 云服务版本', 1, 0),
    (4, 'Kimi', 'KIMI', 'https://kimi.moonshot.cn/', NULL, '适合长文本阅读、资料总结、方案梳理和文档问答的 AI 工具。', '提供免费额度，具体价格以官网为准。', 'Web 版本持续更新', 1, 0),
    (5, '智谱清言', 'ZHIPU_QINGYAN', 'https://chatglm.cn/', NULL, '适合中文问答、文案生成、知识整理和多轮对话的 AI 工具。', '提供免费使用入口，具体价格以官网为准。', 'Web / App 版本持续更新', 1, 0),
    (6, 'ChatGPT', 'CHATGPT', 'https://chatgpt.com/', NULL, '适合通用对话、文本生成、方案分析、图像理解和多模态辅助。', '免费与 Plus / Team 等订阅版本并存，具体以官网为准。', 'Web / App 版本持续更新', 1, 0)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `official_url` = VALUES(`official_url`),
    `logo_url` = VALUES(`logo_url`),
    `description` = VALUES(`description`),
    `price_desc` = VALUES(`price_desc`),
    `version_desc` = VALUES(`version_desc`),
    `status` = VALUES(`status`),
    `is_deleted` = 0;

INSERT INTO `evaluation_dimension` (`id`, `name`, `code`, `description`, `sort_order`, `status`)
VALUES
    (1, '出图质量', 'IMAGE_QUALITY', '评估工具在图像生成、视觉表达或图像辅助方面的综合质量。', 10, 1),
    (2, '控制精度', 'CONTROL_ACCURACY', '评估工具对用户意图、格式、约束和细节要求的执行能力。', 20, 1),
    (3, '建筑适配度', 'ARCHITECTURE_FIT', '评估工具对建筑设计语境、空间逻辑和专业表达的适配程度。', 30, 1),
    (4, '易用性', 'EASE_OF_USE', '评估工具上手难度、交互清晰度和日常使用便利性。', 40, 1),
    (5, '响应速度', 'RESPONSE_SPEED', '评估工具在文本、图片或复杂任务下的响应效率。', 50, 1),
    (6, '学习成本', 'LEARNING_COST', '评估用户掌握工具核心能力所需的时间和理解成本。', 60, 1),
    (7, '成本/价格', 'COST_PRICE', '评估免费额度、订阅价格、API 成本和性价比。', 70, 1),
    (8, '稳定性', 'STABILITY', '评估工具可用性、输出一致性、访问稳定性和任务连续性。', 80, 1)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`);

INSERT INTO `ai_tool_stage_rel` (`tool_id`, `stage_id`)
VALUES
    (1, 1), (1, 2), (1, 4),
    (2, 1), (2, 2), (2, 3),
    (3, 1), (3, 2), (3, 4),
    (4, 1), (4, 3), (4, 4),
    (5, 1), (5, 2),
    (6, 1), (6, 2), (6, 3), (6, 4)
ON DUPLICATE KEY UPDATE
    `stage_id` = VALUES(`stage_id`);

INSERT INTO `ai_tool_evaluation` (`tool_id`, `dimension_id`, `score`, `comment`)
VALUES
    (1, 1, 7.5, '多模态能力持续增强，适合辅助视觉灵感和文本整理，但专业建筑图像仍需人工筛选。'),
    (1, 2, 7.0, '对常规文本约束响应较好，复杂建筑约束需要更明确的提示。'),
    (1, 3, 7.0, '适合前期资料整理和概念讨论，建筑专业深度依赖提示词质量。'),
    (1, 4, 8.5, '上手门槛较低，适合团队成员快速使用。'),
    (1, 5, 8.0, '常规任务响应速度较快。'),
    (2, 2, 8.5, '推理和结构化分析能力较好，适合方案逻辑梳理。'),
    (2, 3, 8.0, '适合建筑设计说明、任务拆解和技术问题分析。'),
    (2, 4, 7.5, '对提示词质量有一定要求。'),
    (2, 5, 8.0, '多数文本任务响应较快。'),
    (2, 8, 7.5, '开发期表现稳定，具体以实际使用情况为准。'),
    (3, 3, 7.5, '适合中文场景下的资料整理和方案说明。'),
    (3, 4, 8.0, '国内用户访问和使用较方便。'),
    (3, 7, 7.5, '适合结合阿里生态和办公场景使用。'),
    (4, 2, 8.0, '长文本处理和资料总结能力较突出。'),
    (4, 3, 7.5, '适合调研资料阅读和案例分析整理。'),
    (4, 6, 8.0, '使用方式直接，学习成本较低。'),
    (5, 3, 7.0, '适合中文建筑语境下的基础问答和文本辅助。'),
    (5, 4, 7.5, '界面清晰，适合轻量使用。'),
    (6, 1, 8.0, '多模态能力较强，适合灵感图像理解和表达辅助。'),
    (6, 2, 8.5, '对复杂约束和多轮细化支持较好。'),
    (6, 3, 8.0, '适合方案分析、英文资料阅读和设计表达优化。'),
    (6, 8, 8.0, '整体可用性较好，但访问和订阅情况需结合实际环境。')
ON DUPLICATE KEY UPDATE
    `score` = VALUES(`score`),
    `comment` = VALUES(`comment`);
