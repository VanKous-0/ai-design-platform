-- Consolidated immutable snapshot of the 26 pre-Flyway bootstrap scripts.
-- Existing populated databases are baselined at version 26 and skip this migration.
-- Empty databases apply this migration before V27 and later migrations.

-- Legacy source: sql/schema.sql
-- Database: ai_design_platform
-- Execute this file first in DataGrip after selecting the ai_design_platform schema.

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码密文或 Spring Security PasswordEncoder 格式密码',
    `nickname` VARCHAR(50) NOT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER 普通用户，ADMIN 管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`),
    KEY `idx_sys_user_role` (`role`),
    KEY `idx_sys_user_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '系统用户表';

-- Legacy source: sql/init_data.sql
-- Database: ai_design_platform
-- Execute this file after schema.sql.
--
-- No fixed administrator password is stored in source control.
-- Configure ADMIN_BOOTSTRAP_USERNAME and ADMIN_BOOTSTRAP_PASSWORD on first startup.

-- Legacy source: sql/workflow_schema.sql
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

-- Legacy source: sql/workflow_init_data.sql
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

-- Legacy source: sql/ai_tool_schema.sql
-- AI tool evaluation module schema.
-- Execute after sql/workflow_schema.sql because ai_tool_stage_rel references workflow_stage.

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

-- Legacy source: sql/ai_tool_init_data.sql
-- AI tool evaluation module test data.
-- Execute after sql/ai_tool_schema.sql and sql/workflow_init_data.sql.
-- These records are only for development/demo. They are not final evaluation conclusions.

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

-- Legacy source: sql/prompt_schema.sql
-- Prompt library module schema.
-- Execute after workflow_schema.sql and ai_tool_schema.sql.

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
    `source_desc` VARCHAR(500) DEFAULT NULL COMMENT '提示词来源与证据说明',
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

-- Legacy source: sql/prompt_init_data.sql
-- Prompt library module test data.
-- Execute after prompt_schema.sql, workflow_init_data.sql and ai_tool_init_data.sql.
-- These records are only for development/demo. They are not final prompt content.

INSERT INTO `prompt_template` (
    `id`,
    `stage_id`,
    `title`,
    `code`,
    `category`,
    `content`,
    `input_desc`,
    `output_desc`,
    `tips`,
    `example_input`,
    `example_output`,
    `sort_order`,
    `copy_count`,
    `status`,
    `is_deleted`
)
VALUES
    (1, 1, '场地条件分析提示词', 'RESEARCH_SITE_ANALYSIS', '场地分析',
     '请基于我提供的场地资料，从区位交通、周边功能、自然条件、历史文脉、限制条件和潜在机会六个方面进行建筑设计前期场地分析，并用条目化结构输出。',
     '场地位置、周边照片、任务书、规划条件、基地红线等资料。',
     '场地分析结论、问题清单、机会点清单。',
     '输入资料越具体，输出越适合用于设计依据整理。',
     '基地位于城市滨水区域，周边有商业、居住和公园，任务为社区文化中心。',
     '可输出区位优势、交通接驳、景观资源、噪声干扰、人群需求和设计机会点。',
     10, 0, 1, 0),
    (2, 1, '案例调研总结提示词', 'RESEARCH_CASE_SUMMARY', '分析总结',
     '请对以下建筑案例资料进行归纳，重点总结项目背景、功能组织、空间亮点、立面策略、技术特点和可借鉴经验，并给出适用于本项目的启发。',
     '案例名称、图片说明、建筑面积、功能构成、设计说明等。',
     '案例分析表、可借鉴策略、风险提醒。',
     '不要只总结优点，也要指出该案例不适合直接套用的地方。',
     '案例为某城市图书馆，强调开放公共空间和屋顶花园。',
     '可输出功能复合、公共界面、屋顶利用、开放流线等分析结论。',
     20, 0, 1, 0),
    (3, 1, '关键词扩展提示词', 'RESEARCH_KEYWORD_EXPAND', '关键词扩展',
     '请围绕以下设计主题扩展关键词，分别从场地、用户、空间、材料、技术、情绪体验和可持续策略角度输出关键词，并按优先级排序。',
     '设计主题、项目类型、场地特征和目标用户。',
     '关键词列表、关键词解释、可转化为设计概念的方向。',
     '适合在概念生成前使用，帮助打开设计思路。',
     '主题：面向青年社区的开放共享文化空间。',
     '可输出共享、渗透、复合、弹性、街角客厅、开放界面等关键词。',
     30, 0, 1, 0),
    (4, 2, '设计概念生成提示词', 'CONCEPT_IDEA_GENERATION', '概念生成',
     '请基于项目背景、场地问题和目标用户，生成 3 个不同方向的建筑设计概念。每个概念需要包含核心关键词、空间策略、体量设想、用户体验和适合表达的图像氛围。',
     '项目背景、场地分析结论、用户需求、设计限制。',
     '多个概念方向、概念关键词、空间策略说明。',
     '生成结果用于启发，不应直接作为最终方案。',
     '项目为滨水社区文化中心，希望强化公共开放性和城市界面。',
     '可输出“城市客厅”“滨水廊桥”“社区织补”等概念方向。',
     10, 0, 1, 0),
    (5, 2, '方案比选提示词', 'CONCEPT_SCHEME_COMPARE', '方案比选',
     '请对以下多个建筑方案进行比较，从功能效率、空间体验、场地回应、建造可行性、视觉表达和后续深化潜力六个维度进行评价，并给出推荐方案和理由。',
     '多个方案的文字说明、草图描述、优缺点、面积指标。',
     '方案对比表、推荐结论、优化建议。',
     '适合方案讨论阶段使用，不能替代专业评审。',
     '方案 A 强调开放中庭，方案 B 强调沿街界面，方案 C 强调屋顶活动。',
     '可输出各方案评分、主要风险、综合推荐和深化方向。',
     20, 0, 1, 0),
    (6, 2, '图像生成描述优化提示词', 'CONCEPT_IMAGE_PROMPT_OPTIMIZE', '图像生成描述优化',
     '请将以下建筑设计概念转化为适合图像生成工具使用的视觉描述，包含建筑类型、空间氛围、材料、光线、视角、环境背景和构图重点。',
     '设计概念、建筑类型、希望呈现的空间效果。',
     '结构化图像生成描述，可用于后续工具输入。',
     '本平台不直接调用图像生成工具，只提供描述优化。',
     '概念：滨水社区客厅，强调开放、透明、木结构和公共活动。',
     '可输出滨水文化建筑、通透玻璃界面、木质结构、傍晚暖光、人群活动等描述。',
     30, 0, 1, 0),
    (7, 3, '平面功能优化提示词', 'DEVELOPMENT_PLAN_OPTIMIZE', '平立剖表达优化',
     '请基于以下平面功能描述，检查功能分区、流线组织、公共与后勤关系、动静分区和使用效率，并提出可操作的优化建议。',
     '平面功能描述、面积指标、主要流线、问题反馈。',
     '问题清单、优化建议、深化注意事项。',
     '适合配合设计讨论，不替代规范审查。',
     '一层包含展厅、咖啡、报告厅和后勤，入口位于东侧。',
     '可输出入口识别、流线交叉、后勤独立、公共空间连续性等建议。',
     10, 0, 1, 0),
    (8, 3, '立面表达优化提示词', 'DEVELOPMENT_FACADE_OPTIMIZE', '平立剖表达优化',
     '请根据建筑概念和功能逻辑，提出立面表达优化建议，包括开窗节奏、虚实关系、材料选择、入口强调、夜景效果和与场地环境的关系。',
     '建筑概念、立面草图描述、材料倾向、周边环境。',
     '立面优化建议、材料策略、表达重点。',
     '避免只追求形式，应回应功能和场地。',
     '概念为城市客厅，立面希望透明开放但需要遮阳。',
     '可输出竖向遮阳、首层通透、二层半透明界面、夜间公共性表达等建议。',
     20, 0, 1, 0),
    (9, 3, '剖面空间叙事提示词', 'DEVELOPMENT_SECTION_NARRATIVE', '表达优化',
     '请根据以下剖面关系，帮助我组织空间叙事，说明不同楼层、公共空间、采光通风、视线关系和结构策略如何共同支撑设计概念。',
     '剖面草图描述、楼层关系、中庭或坡道等空间节点。',
     '剖面说明文字、空间叙事逻辑、表达图重点。',
     '适合用于方案文本和汇报讲解。',
     '建筑有通高中庭、屋顶平台和沿河阶梯空间。',
     '可输出垂直公共空间、自然采光、视线连接、活动延展等叙事。',
     30, 0, 1, 0),
    (10, 4, '汇报文案整理提示词', 'PRESENTATION_REPORT_COPY', '汇报文案整理',
     '请将以下设计过程和方案要点整理为适合汇报使用的表达文本，要求逻辑清晰、层次分明，包含项目背景、问题判断、设计策略、空间组织和成果亮点。',
     '设计说明草稿、分析图要点、方案亮点。',
     '汇报文本、章节标题、讲述逻辑。',
     '适合答辩和中期汇报前整理语言。',
     '项目希望通过开放首层和屋顶活动平台连接社区与滨水空间。',
     '可输出从场地问题到策略回应再到空间成果的完整叙事。',
     10, 0, 1, 0),
    (11, 4, '展板标题优化提示词', 'PRESENTATION_BOARD_TITLE', '表达优化',
     '请根据以下设计内容，为建筑设计展板生成主标题、副标题和分区标题，要求准确、简洁、有设计感，但不要过度夸张。',
     '项目类型、设计概念、主要空间亮点、展板章节。',
     '主标题、副标题、章节标题备选。',
     '标题要服务内容，不要为了新奇牺牲准确性。',
     '项目为滨水社区文化中心，概念是城市客厅和共享廊桥。',
     '可输出“滨水共享客厅”“连接城市与社区的开放廊桥”等标题。',
     20, 0, 1, 0),
    (12, 4, '成果说明压缩提示词', 'PRESENTATION_SUMMARY_COMPRESS', '汇报文案整理',
     '请将以下较长的设计说明压缩为 300 字以内的成果简介，保留项目背景、核心策略、空间亮点和最终价值。',
     '完整设计说明或汇报文案。',
     '简短成果简介。',
     '适合用于首页展示、案例摘要或答辩材料简介。',
     '输入一段 1000 字左右的设计说明。',
     '输出 300 字以内的项目摘要。',
     30, 0, 1, 0)
ON DUPLICATE KEY UPDATE
    `stage_id` = VALUES(`stage_id`),
    `title` = VALUES(`title`),
    `category` = VALUES(`category`),
    `content` = VALUES(`content`),
    `input_desc` = VALUES(`input_desc`),
    `output_desc` = VALUES(`output_desc`),
    `tips` = VALUES(`tips`),
    `example_input` = VALUES(`example_input`),
    `example_output` = VALUES(`example_output`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `is_deleted` = 0;

INSERT INTO `prompt_tool_rel` (`prompt_id`, `tool_id`)
VALUES
    (1, 1), (1, 2), (1, 6),
    (2, 2), (2, 4), (2, 6),
    (3, 1), (3, 3), (3, 5),
    (4, 1), (4, 2), (4, 6),
    (5, 2), (5, 4), (5, 6),
    (6, 1), (6, 3), (6, 6),
    (7, 2), (7, 4), (7, 6),
    (8, 1), (8, 3), (8, 6),
    (9, 2), (9, 4), (9, 6),
    (10, 1), (10, 3), (10, 6),
    (11, 1), (11, 5), (11, 6),
    (12, 2), (12, 4), (12, 6)
ON DUPLICATE KEY UPDATE
    `tool_id` = VALUES(`tool_id`);

-- Legacy source: sql/case_schema.sql
-- Case showcase module schema.
-- Execute after workflow_schema.sql and ai_tool_schema.sql.

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

CREATE TABLE IF NOT EXISTS `case_tool_usage` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `case_id` BIGINT UNSIGNED NOT NULL COMMENT '所属案例ID',
    `tool_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'AI工具ID，非AI工具为空',
    `tool_name` VARCHAR(100) NOT NULL COMMENT '工具展示名称',
    `tool_code` VARCHAR(80) NOT NULL COMMENT '案例内稳定工具编码',
    `tool_type` VARCHAR(30) NOT NULL COMMENT 'AI/DESIGN/DRAWING/POST_PROCESSING',
    `usage_stage` VARCHAR(50) NOT NULL COMMENT '实际使用阶段',
    `usage_desc` VARCHAR(500) NOT NULL COMMENT '有材料依据的用途说明',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '流程排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_case_tool_usage` (`case_id`, `tool_code`, `sort_order`),
    KEY `idx_case_tool_usage_case_sort` (`case_id`, `sort_order`),
    KEY `idx_case_tool_usage_tool` (`tool_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '案例实际工具使用链路';

-- Legacy source: sql/case_init_data.sql
-- Case showcase module test data.
-- Execute after case_schema.sql, workflow_init_data.sql and ai_tool_init_data.sql.
-- These records are only for development/demo. They are not final case content.

INSERT INTO `case_project` (
    `id`,
    `title`,
    `code`,
    `stage_id`,
    `tool_id`,
    `cover_url`,
    `summary`,
    `content`,
    `source_desc`,
    `author_name`,
    `sort_order`,
    `status`,
    `is_deleted`
)
VALUES
    (1, '滨水社区文化中心场地分析案例', 'CASE_RESEARCH_WATERFRONT_SITE', 1, 2, '/assets/cases/research-waterfront-cover.jpg',
     '围绕滨水社区文化中心任务，对区位交通、周边功能、景观资源和场地限制进行结构化分析。',
     '该案例展示如何利用 AI 辅助整理前期场地资料，并将零散信息转化为场地问题、机会点和后续设计策略。内容包含区位判断、公共界面分析、滨水资源利用和人群活动需求整理。',
     '开发测试数据，非最终案例内容。', '项目组', 10, 1, 0),
    (2, '青年社区共享空间调研案例', 'CASE_RESEARCH_YOUTH_COMMUNITY', 1, 4, '/assets/cases/research-youth-cover.jpg',
     '通过案例调研和关键词扩展，梳理青年社区共享空间的功能需求和空间氛围。',
     '该案例用于展示前期调研阶段如何将用户画像、社区活动和空间关键词结合，形成后续概念设计的依据。',
     '开发测试数据，非最终案例内容。', '项目组', 20, 1, 0),
    (3, '城市客厅概念生成案例', 'CASE_CONCEPT_CITY_LIVING_ROOM', 2, 1, '/assets/cases/concept-living-room-cover.jpg',
     '基于场地问题生成“城市客厅”概念，并推导空间策略、体量关系和视觉氛围。',
     '该案例展示概念设计阶段如何从调研结论出发，生成多个概念方向，并将关键词转化为空间组织策略。',
     '开发测试数据，非最终案例内容。', '项目组', 10, 1, 0),
    (4, '滨水廊桥方案比选案例', 'CASE_CONCEPT_BRIDGE_COMPARE', 2, 6, '/assets/cases/concept-bridge-cover.jpg',
     '对多个滨水公共建筑概念方案进行比较，形成推荐方案和深化方向。',
     '该案例展示如何借助 AI 辅助梳理方案优缺点，从功能效率、空间体验、场地回应和表达潜力等角度进行比选。',
     '开发测试数据，非最终案例内容。', '项目组', 20, 1, 0),
    (5, '文化中心平立剖深化案例', 'CASE_DEVELOPMENT_PLAN_FACADE', 3, 2, '/assets/cases/development-plan-cover.jpg',
     '围绕平面功能、立面表达和剖面空间关系，对文化中心方案进行深化优化。',
     '该案例展示方案深化阶段如何检查功能分区、流线组织、立面虚实关系和剖面公共空间叙事。',
     '开发测试数据，非最终案例内容。', '项目组', 10, 1, 0),
    (6, '屋顶公共活动平台深化案例', 'CASE_DEVELOPMENT_ROOF_PLATFORM', 3, 3, '/assets/cases/development-roof-cover.jpg',
     '针对屋顶活动平台和垂直公共空间进行深化表达，强化空间连续性。',
     '该案例展示如何通过图文说明整理屋顶平台、坡道、中庭和城市界面的关系。',
     '开发测试数据，非最终案例内容。', '项目组', 20, 1, 0),
    (7, '建筑设计展板表达优化案例', 'CASE_PRESENTATION_BOARD', 4, 6, '/assets/cases/presentation-board-cover.jpg',
     '围绕最终展板进行标题、图纸组织、汇报逻辑和视觉层级优化。',
     '该案例展示成果表达阶段如何将设计过程、分析图、总平面、平立剖和效果图组织为清晰的汇报展板。',
     '开发测试数据，非最终案例内容。', '项目组', 10, 1, 0),
    (8, '方案汇报文案整理案例', 'CASE_PRESENTATION_REPORT_COPY', 4, 1, '/assets/cases/presentation-copy-cover.jpg',
     '将较长的设计说明整理成适合答辩汇报的结构化文案。',
     '该案例展示如何把项目背景、问题判断、设计策略、空间组织和成果亮点串联成完整汇报叙事。',
     '开发测试数据，非最终案例内容。', '项目组', 20, 1, 0)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `stage_id` = VALUES(`stage_id`),
    `tool_id` = VALUES(`tool_id`),
    `cover_url` = VALUES(`cover_url`),
    `summary` = VALUES(`summary`),
    `content` = VALUES(`content`),
    `source_desc` = VALUES(`source_desc`),
    `author_name` = VALUES(`author_name`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `is_deleted` = 0;

INSERT INTO `case_asset` (
    `id`,
    `case_id`,
    `asset_type`,
    `asset_url`,
    `title`,
    `description`,
    `sort_order`,
    `is_deleted`
)
VALUES
    (1, 1, 'image', '/assets/cases/research-waterfront-site-analysis.jpg', '场地分析图', '展示区位、交通、景观和限制条件。', 10, 0),
    (2, 1, 'file', '/assets/cases/research-waterfront-report.pdf', '调研报告占位文件', '用于后续替换为正式 PDF。', 20, 0),
    (3, 2, 'image', '/assets/cases/research-youth-keywords.jpg', '关键词扩展图', '展示青年社区共享空间关键词。', 10, 0),
    (4, 2, 'image', '/assets/cases/research-youth-case-board.jpg', '案例调研板', '展示同类型案例分析。', 20, 0),
    (5, 3, 'image', '/assets/cases/concept-living-room-diagram.jpg', '概念策略图', '展示城市客厅概念转化。', 10, 0),
    (6, 3, 'image', '/assets/cases/concept-living-room-render.jpg', '概念氛围图', '概念阶段视觉表达占位图。', 20, 0),
    (7, 4, 'image', '/assets/cases/concept-bridge-compare.jpg', '方案比选图', '展示多个方案的对比维度。', 10, 0),
    (8, 4, 'file', '/assets/cases/concept-bridge-notes.pdf', '方案比选说明文档', '用于保存方案比较过程。', 20, 0),
    (9, 5, 'image', '/assets/cases/development-plan.jpg', '深化平面图', '展示功能分区和流线优化。', 10, 0),
    (10, 5, 'image', '/assets/cases/development-facade.jpg', '立面表达图', '展示材料与开窗节奏。', 20, 0),
    (11, 6, 'image', '/assets/cases/development-roof-section.jpg', '剖面空间图', '展示屋顶平台和中庭关系。', 10, 0),
    (12, 6, 'image', '/assets/cases/development-roof-view.jpg', '屋顶活动视角图', '展示公共活动平台效果。', 20, 0),
    (13, 7, 'image', '/assets/cases/presentation-board-layout.jpg', '展板排版图', '展示最终展板版式结构。', 10, 0),
    (14, 7, 'file', '/assets/cases/presentation-board.pdf', '展板 PDF 占位文件', '用于后续替换正式展板。', 20, 0),
    (15, 8, 'image', '/assets/cases/presentation-copy-outline.jpg', '汇报逻辑图', '展示汇报文案结构。', 10, 0),
    (16, 8, 'file', '/assets/cases/presentation-script.docx', '汇报稿占位文件', '用于后续替换正式文档。', 20, 0)
ON DUPLICATE KEY UPDATE
    `case_id` = VALUES(`case_id`),
    `asset_type` = VALUES(`asset_type`),
    `asset_url` = VALUES(`asset_url`),
    `title` = VALUES(`title`),
    `description` = VALUES(`description`),
    `sort_order` = VALUES(`sort_order`),
    `is_deleted` = 0;

-- Legacy source: sql/review_schema.sql
-- Review module schema.
-- Execute after schema.sql, workflow_schema.sql and ai_tool_schema.sql.

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

-- Legacy source: sql/review_init_data.sql
-- Review module test data.
-- Execute after review_schema.sql, workflow_init_data.sql and ai_tool_init_data.sql.
-- These records are only for development/demo. They are not final review content.

INSERT INTO `review_record` (
    `id`,
    `user_id`,
    `title`,
    `code`,
    `stage_id`,
    `tool_id`,
    `project_name`,
    `summary`,
    `problem_desc`,
    `solution_desc`,
    `reflection`,
    `score`,
    `review_date`,
    `sort_order`,
    `status`,
    `is_deleted`
)
VALUES
    (1, 1, '前期调研资料整理复盘', 'REVIEW_RESEARCH_DATA_ORGANIZE', 1, 2, '滨水社区文化中心',
     '复盘使用 AI 辅助整理场地资料和案例信息的过程。',
     '前期资料来源分散，场地照片、规划条件、案例资料之间缺少统一结构，直接进入概念阶段容易依据不足。',
     '使用 DeepSeek 先整理资料清单，再按区位交通、周边功能、自然条件、限制条件和机会点重组内容。',
     '前期调研阶段最重要的是把资料转化为问题和机会，而不是只堆材料。AI 适合做结构化整理，但结论仍需人工判断。',
     8.0, '2026-04-01', 10, 1, 0),
    (2, 1, '概念方向生成复盘', 'REVIEW_CONCEPT_IDEA', 2, 1, '城市客厅概念方案',
     '复盘从场地问题到概念关键词和空间策略的生成过程。',
     '概念阶段容易出现口号化表达，关键词看起来丰富，但难以转化为平面、体量和空间体验。',
     '使用豆包生成多个概念方向后，对每个方向补充空间策略、用户体验和可视化表达建议。',
     'AI 生成概念时需要给出明确的场地问题和用户需求，否则输出会比较泛化。概念必须能落到空间组织上。',
     7.5, '2026-04-05', 10, 1, 0),
    (3, 1, '方案比选与深化复盘', 'REVIEW_DEVELOPMENT_COMPARE', 3, 6, '滨水廊桥方案深化',
     '复盘使用 AI 辅助比较多个方案并整理深化建议的过程。',
     '多个方案各有优势，人工讨论时容易只关注形式效果，忽略功能效率、建造可行性和后续深化风险。',
     '使用 ChatGPT 按功能效率、空间体验、场地回应、表达潜力和风险点建立比选框架，再结合人工判断确定深化方向。',
     '方案比选适合让 AI 提供结构化框架，但最终取舍必须结合设计目标和专业判断。',
     8.5, '2026-04-10', 10, 1, 0),
    (4, 1, '成果表达与汇报文案复盘', 'REVIEW_PRESENTATION_COPY', 4, 1, '社区文化中心成果表达',
     '复盘最终展板和汇报文案整理过程。',
     '成果阶段材料多，图纸、分析图、效果图和文字说明之间缺少清晰讲述顺序，汇报容易显得散。',
     '使用豆包先整理汇报结构，再压缩长文本，形成项目背景、问题判断、设计策略、空间成果和价值总结的讲述线索。',
     '成果表达不是把所有内容都放上去，而是围绕主线筛选材料。AI 可以辅助压缩和组织语言。',
     8.0, '2026-04-15', 10, 1, 0)
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `title` = VALUES(`title`),
    `stage_id` = VALUES(`stage_id`),
    `tool_id` = VALUES(`tool_id`),
    `project_name` = VALUES(`project_name`),
    `summary` = VALUES(`summary`),
    `problem_desc` = VALUES(`problem_desc`),
    `solution_desc` = VALUES(`solution_desc`),
    `reflection` = VALUES(`reflection`),
    `score` = VALUES(`score`),
    `review_date` = VALUES(`review_date`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `is_deleted` = 0;

INSERT INTO `review_asset` (
    `id`,
    `review_id`,
    `asset_type`,
    `asset_url`,
    `title`,
    `description`,
    `sort_order`,
    `is_deleted`
)
VALUES
    (1, 1, 'image', '/assets/reviews/research-data-board.jpg', '调研资料整理图', '展示前期资料分类和问题机会点。', 10, 0),
    (2, 1, 'file', '/assets/reviews/research-data-notes.pdf', '调研复盘 PDF 占位文件', '用于后续替换正式复盘文档。', 20, 0),
    (3, 2, 'image', '/assets/reviews/concept-keywords.jpg', '概念关键词图', '展示概念关键词到空间策略的转化。', 10, 0),
    (4, 2, 'image', '/assets/reviews/concept-options.jpg', '概念方向对比图', '展示多个概念方向。', 20, 0),
    (5, 3, 'image', '/assets/reviews/development-compare.jpg', '方案比选图', '展示多方案比较框架。', 10, 0),
    (6, 3, 'file', '/assets/reviews/development-feedback.docx', '深化反馈文档占位', '用于后续替换正式反馈文档。', 20, 0),
    (7, 4, 'image', '/assets/reviews/presentation-outline.jpg', '汇报逻辑图', '展示最终汇报结构。', 10, 0),
    (8, 4, 'file', '/assets/reviews/presentation-script.pdf', '汇报稿 PDF 占位文件', '用于后续替换正式汇报稿。', 20, 0)
ON DUPLICATE KEY UPDATE
    `review_id` = VALUES(`review_id`),
    `asset_type` = VALUES(`asset_type`),
    `asset_url` = VALUES(`asset_url`),
    `title` = VALUES(`title`),
    `description` = VALUES(`description`),
    `sort_order` = VALUES(`sort_order`),
    `is_deleted` = 0;

-- Legacy source: sql/site_schema.sql
-- Site home content module schema.
-- Execute after schema.sql.

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

-- Legacy source: sql/site_init_data.sql
-- Site home content module test data.
-- Execute after site_schema.sql.
-- These records are only for development/demo and are not final homepage copy.

INSERT INTO `site_content` (
    `id`,
    `section_key`,
    `title`,
    `subtitle`,
    `content`,
    `image_url`,
    `link_url`,
    `extra_json`,
    `sort_order`,
    `status`,
    `is_deleted`
)
VALUES
    (1, 'hero', 'AI Design Platform', 'A platform for architectural design workflow, tool evaluation, prompt templates, cases and reviews.', 'The first version focuses on content management and display. It does not call external AI APIs inside the platform.', '/assets/site/hero-cover.jpg', '/api/stages', '{"primaryButtonText":"View Workflow","secondaryButtonText":"Browse Cases"}', 10, 1, 0),
    (2, 'intro', 'Project Introduction', 'AI-assisted knowledge platform for architectural design education and practice.', 'The platform organizes design stages, AI tool evaluation, prompt templates, case projects and review records in one backend.', '/assets/site/intro.jpg', NULL, '{"tags":["Architecture","AI","Workflow"]}', 10, 1, 0),
    (3, 'workflow_entry', 'Design Workflow', 'Understand tasks, inputs, outputs and tips by design stage.', 'The workflow module shows standard stages and steps from research to concept, development and presentation.', '/assets/site/workflow-entry.jpg', '/api/stages', '{"buttonText":"Open Workflow"}', 10, 1, 0),
    (4, 'tool_recommend_entry', 'AI Tool Evaluation Center', 'View tool information, applicable stages and evaluation scores.', 'The tool module is used for tool evaluation, recommendation and navigation. Tool data is maintained manually by admins.', '/assets/site/tool-entry.jpg', '/api/tools', '{"buttonText":"View Tools"}', 20, 1, 0),
    (5, 'prompt_entry', 'Prompt Library', 'Search prompt templates by stage, category and keyword.', 'The prompt module stores reusable prompt templates for architectural design scenarios and recommended tools.', '/assets/site/prompt-entry.jpg', '/api/prompts', '{"buttonText":"Browse Prompts"}', 30, 1, 0),
    (6, 'case_entry', 'Case Showcase', 'Display case background, result description and resource list.', 'The case module presents research, concept generation, scheme development and presentation cases.', '/assets/site/case-entry.jpg', '/api/cases', '{"buttonText":"View Cases"}', 40, 1, 0),
    (7, 'review_entry', 'Review Records', 'Record problems, solutions and reflections for later reuse.', 'The review module stores project review records, attachments, related stages and related tools.', '/assets/site/review-entry.jpg', '/api/reviews', '{"buttonText":"View Reviews"}', 50, 1, 0),
    (8, 'contact', 'Project Notes', 'Current homepage content is demo data.', 'Before final presentation, admins can replace copywriting, image paths, links and award records.', '/assets/site/contact.jpg', NULL, '{"email":"demo@example.com","team":"AI Design Platform Team"}', 10, 1, 0)
ON DUPLICATE KEY UPDATE
    `section_key` = VALUES(`section_key`),
    `title` = VALUES(`title`),
    `subtitle` = VALUES(`subtitle`),
    `content` = VALUES(`content`),
    `image_url` = VALUES(`image_url`),
    `link_url` = VALUES(`link_url`),
    `extra_json` = VALUES(`extra_json`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `is_deleted` = 0;

INSERT INTO `award_record` (
    `id`,
    `title`,
    `award_level`,
    `issuer`,
    `award_date`,
    `summary`,
    `image_url`,
    `link_url`,
    `sort_order`,
    `status`,
    `is_deleted`
)
VALUES
    (1, 'Innovation Project Approval', 'School Level', 'Innovation and Entrepreneurship Office', '2026-03-15', 'Demo data for project approval display.', '/assets/awards/dachuang-approval.jpg', NULL, 10, 1, 0),
    (2, 'Midterm Review Passed', 'School Level', 'Project Review Committee', '2026-04-10', 'Demo data for midterm review display.', '/assets/awards/midterm-review.jpg', NULL, 20, 1, 0),
    (3, 'Campus Showcase', 'School Level', 'College Showcase Event', '2026-05-20', 'Demo data for campus achievement showcase.', '/assets/awards/campus-showcase.jpg', NULL, 30, 1, 0),
    (4, 'Innovation Competition Award', 'Other', 'Competition Committee', '2026-06-01', 'Demo data for award list display. Replace it with final information later.', '/assets/awards/competition-award.jpg', NULL, 40, 1, 0),
    (5, 'College Course Achievement Showcase', 'College Level', 'School of Architecture', '2026-06-15', 'Demo data for course and college achievement display.', '/assets/awards/course-showcase.jpg', NULL, 50, 1, 0)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `award_level` = VALUES(`award_level`),
    `issuer` = VALUES(`issuer`),
    `award_date` = VALUES(`award_date`),
    `summary` = VALUES(`summary`),
    `image_url` = VALUES(`image_url`),
    `link_url` = VALUES(`link_url`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `is_deleted` = 0;

-- Legacy source: sql/workflow_runtime_schema.sql
-- Workflow runtime schema.
-- Execute after sql/schema.sql, sql/workflow_schema.sql.

CREATE TABLE IF NOT EXISTS `workflow_template` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(100) NOT NULL COMMENT 'Template name',
    `code` VARCHAR(80) NOT NULL COMMENT 'Stable template code',
    `description` TEXT DEFAULT NULL COMMENT 'Template description',
    `scene_type` VARCHAR(80) DEFAULT NULL COMMENT 'Typical scenario type',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT 'Cover image URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1 enabled, 0 disabled',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_template_code` (`code`),
    KEY `idx_workflow_template_status_sort` (`status`, `sort_order`),
    KEY `idx_workflow_template_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow template table';

CREATE TABLE IF NOT EXISTS `workflow_template_node` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `template_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template ID',
    `stage_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Optional existing workflow_stage ID',
    `step_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Optional existing workflow_step ID',
    `node_name` VARCHAR(150) NOT NULL COMMENT 'Node name',
    `node_code` VARCHAR(80) NOT NULL COMMENT 'Node code in template',
    `node_type` VARCHAR(50) DEFAULT NULL COMMENT 'Node type',
    `input_desc` TEXT DEFAULT NULL COMMENT 'Input description',
    `output_desc` TEXT DEFAULT NULL COMMENT 'Output description',
    `next_tip` TEXT DEFAULT NULL COMMENT 'Tip after this node is completed',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Node order',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1 enabled, 0 disabled',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_template_node_code` (`template_id`, `node_code`),
    KEY `idx_workflow_template_node_template_sort` (`template_id`, `sort_order`),
    KEY `idx_workflow_template_node_stage` (`stage_id`),
    KEY `idx_workflow_template_node_step` (`step_id`),
    KEY `idx_workflow_template_node_status` (`status`),
    KEY `idx_workflow_template_node_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow template node table';

CREATE TABLE IF NOT EXISTS `workflow_instance` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `template_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Owner user ID',
    `title` VARCHAR(150) DEFAULT NULL COMMENT 'Instance title',
    `current_node_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Current node ID',
    `status` VARCHAR(30) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING / FINISHED',
    `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Start time',
    `finish_time` DATETIME DEFAULT NULL COMMENT 'Finish time',
    `progress` DECIMAL(5, 2) NOT NULL DEFAULT 0.00 COMMENT 'Progress percent',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    KEY `idx_workflow_instance_user` (`user_id`),
    KEY `idx_workflow_instance_template` (`template_id`),
    KEY `idx_workflow_instance_status` (`status`),
    KEY `idx_workflow_instance_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow execution instance table';

CREATE TABLE IF NOT EXISTS `workflow_step_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `instance_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow instance ID',
    `node_id` BIGINT UNSIGNED NOT NULL COMMENT 'Template node ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Operator user ID',
    `input_content` TEXT DEFAULT NULL COMMENT 'User input',
    `output_content` TEXT DEFAULT NULL COMMENT 'User output',
    `status` VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' COMMENT 'Step status',
    `duration_seconds` INT DEFAULT NULL COMMENT 'Duration in seconds',
    `started_at` DATETIME DEFAULT NULL COMMENT 'Step start time',
    `completed_at` DATETIME DEFAULT NULL COMMENT 'Step completed time',
    `next_suggestion` TEXT DEFAULT NULL COMMENT 'Next step suggestion',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_step_record_instance_node` (`instance_id`, `node_id`),
    KEY `idx_workflow_step_record_user` (`user_id`),
    KEY `idx_workflow_step_record_node` (`node_id`),
    KEY `idx_workflow_step_record_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow step execution record table';

CREATE TABLE IF NOT EXISTS `workflow_step_iteration` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `instance_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow instance ID',
    `node_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template node ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Owner user ID',
    `iteration_no` INT NOT NULL COMMENT 'Iteration number within the node',
    `tool_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'AI tool used externally',
    `prompt_content` TEXT DEFAULT NULL COMMENT 'Prompt used for this iteration',
    `output_content` MEDIUMTEXT DEFAULT NULL COMMENT 'External AI output or result summary',
    `result_url` VARCHAR(500) DEFAULT NULL COMMENT 'External or static result URL',
    `effect_score` TINYINT DEFAULT NULL COMMENT 'Visual or task effect score, 1-10',
    `accuracy_score` TINYINT DEFAULT NULL COMMENT 'Requirement accuracy score, 1-10',
    `controllability_score` TINYINT DEFAULT NULL COMMENT 'Output controllability score, 1-10',
    `usability_score` TINYINT DEFAULT NULL COMMENT 'Downstream usability score, 1-10',
    `improvement_note` TEXT DEFAULT NULL COMMENT 'Problems and next-round improvement plan',
    `selected` TINYINT NOT NULL DEFAULT 0 COMMENT 'Whether this is the selected result',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_step_iteration_no` (`instance_id`, `node_id`, `iteration_no`),
    KEY `idx_workflow_step_iteration_user` (`user_id`),
    KEY `idx_workflow_step_iteration_tool` (`tool_id`),
    KEY `idx_workflow_step_iteration_selected` (`instance_id`, `node_id`, `selected`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow step external AI result iterations';

-- Legacy source: sql/workflow_runtime_init_data.sql
-- Workflow runtime demo data.
-- Execute after workflow_runtime_schema.sql.
-- This script uses codes from existing init scripts instead of fixed IDs where possible.
-- If your local workflow_stage/workflow_step codes differ, adjust the subqueries.

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
    (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'CONCEPT' LIMIT 1),
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
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'CONCEPT' LIMIT 1), NULL,
       '概念关键词生成', 'concept_keywords', 'ai_prompt',
       '输入任务书和设计偏好。', '输出概念关键词与设计主题。',
       '下一步可以把关键词扩展为完整方案叙事。', 20, 1
FROM workflow_template t WHERE t.code = 'building_concept_generation'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'CONCEPT' LIMIT 1), NULL,
       '方案叙事与空间策略', 'concept_strategy', 'ai_prompt',
       '输入概念关键词和功能需求。', '输出空间策略、动线和设计说明。',
       '下一步可以整理输出并进入成果表达。', 30, 1
FROM workflow_template t WHERE t.code = 'building_concept_generation'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'RESEARCH' LIMIT 1), NULL,
       '场地基础条件梳理', 'site_basic_analysis', 'input',
       '输入区位、面积、周边环境、交通和人群信息。', '输出场地条件摘要。',
       '下一步可以分析问题与机会点。', 10, 1
FROM workflow_template t WHERE t.code = 'landscape_site_analysis'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'RESEARCH' LIMIT 1), NULL,
       '问题机会点提炼', 'site_issue_opportunity', 'ai_prompt',
       '输入场地条件摘要。', '输出问题、机会与设计突破口。',
       '下一步可以生成景观设计策略。', 20, 1
FROM workflow_template t WHERE t.code = 'landscape_site_analysis'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `step_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`)
SELECT t.id, (SELECT ws.id FROM workflow_stage ws WHERE ws.code = 'CONCEPT' LIMIT 1), NULL,
       '景观策略生成', 'landscape_strategy', 'ai_prompt',
       '输入问题机会点和设计目标。', '输出空间结构、生态策略和活动策划。',
       '流程已完成，可以整理为设计说明或图纸表达。', 30, 1
FROM workflow_template t WHERE t.code = 'landscape_site_analysis'
ON DUPLICATE KEY UPDATE `node_name` = VALUES(`node_name`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

-- Legacy source: sql/prompt_enhance_schema.sql
-- Prompt enhancement schema.
-- Execute after sql/prompt_schema.sql and sql/workflow_runtime_schema.sql.

CREATE TABLE IF NOT EXISTS `workflow_node_prompt_rel` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `node_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template node ID',
    `prompt_id` BIGINT UNSIGNED NOT NULL COMMENT 'Prompt template ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_node_prompt` (`node_id`, `prompt_id`),
    KEY `idx_workflow_node_prompt_node` (`node_id`),
    KEY `idx_workflow_node_prompt_prompt` (`prompt_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Workflow node prompt relation table';

CREATE TABLE IF NOT EXISTS `prompt_parameter` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `prompt_id` BIGINT UNSIGNED NOT NULL COMMENT 'Prompt template ID',
    `param_key` VARCHAR(80) NOT NULL COMMENT 'Placeholder key without braces',
    `param_name` VARCHAR(100) NOT NULL COMMENT 'Parameter display name',
    `param_type` VARCHAR(30) NOT NULL DEFAULT 'text' COMMENT 'Parameter type',
    `required` TINYINT NOT NULL DEFAULT 0 COMMENT 'Required: 1 yes, 0 no',
    `default_value` VARCHAR(500) DEFAULT NULL COMMENT 'Default value',
    `placeholder` VARCHAR(255) DEFAULT NULL COMMENT 'Frontend placeholder',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_parameter_key` (`prompt_id`, `param_key`),
    KEY `idx_prompt_parameter_prompt_sort` (`prompt_id`, `sort_order`),
    KEY `idx_prompt_parameter_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Prompt parameter definition table';

-- Legacy source: sql/prompt_enhance_init_data.sql
-- Prompt enhancement demo data.
-- Execute after prompt_enhance_schema.sql and workflow_runtime_init_data.sql.
-- The relation inserts use existing prompt_template rows by sort order because old prompt codes may vary.
-- If your local prompts are different, adjust the selected prompt IDs.

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

-- Legacy source: sql/user_profile_schema.sql
-- User profile and parameter memory schema.
-- Execute after sql/schema.sql.

CREATE TABLE IF NOT EXISTS `user_profile` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT 'Real name',
    `school` VARCHAR(100) DEFAULT NULL COMMENT 'School',
    `major` VARCHAR(100) DEFAULT NULL COMMENT 'Major',
    `grade` VARCHAR(50) DEFAULT NULL COMMENT 'Grade',
    `phone` VARCHAR(30) DEFAULT NULL COMMENT 'Phone number',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT 'Bio',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT 'Avatar URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_profile_user` (`user_id`),
    KEY `idx_user_profile_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'User profile table';

CREATE TABLE IF NOT EXISTS `user_design_preference` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `preferred_project_type` VARCHAR(100) DEFAULT NULL COMMENT 'Preferred project type',
    `preferred_style` VARCHAR(100) DEFAULT NULL COMMENT 'Preferred design style',
    `preferred_site_scale` VARCHAR(100) DEFAULT NULL COMMENT 'Preferred site scale',
    `preferred_target_user` VARCHAR(100) DEFAULT NULL COMMENT 'Preferred target user',
    `default_tool_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Default AI tool ID',
    `extra_json` TEXT DEFAULT NULL COMMENT 'Reserved JSON string',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_design_preference_user` (`user_id`),
    KEY `idx_user_design_preference_tool` (`default_tool_id`),
    KEY `idx_user_design_preference_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'User design preference table';

CREATE TABLE IF NOT EXISTS `user_recent_parameter` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `parameter_type` VARCHAR(50) NOT NULL COMMENT 'Parameter type',
    `parameter_key` VARCHAR(100) NOT NULL COMMENT 'Parameter key',
    `parameter_value` VARCHAR(500) NOT NULL COMMENT 'Parameter value',
    `source` VARCHAR(50) DEFAULT NULL COMMENT 'Source scene',
    `use_count` INT NOT NULL DEFAULT 1 COMMENT 'Use count',
    `last_used_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last used time',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_recent_parameter_value` (`user_id`, `parameter_key`, `parameter_value`),
    KEY `idx_user_recent_parameter_user_time` (`user_id`, `last_used_time`),
    KEY `idx_user_recent_parameter_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'User recent parameter table';

-- Legacy source: sql/user_profile_init_data.sql
-- Optional normal user bootstrap data.
-- Execute after sql/schema.sql.

INSERT INTO `sys_user` (
    `username`,
    `password_hash`,
    `nickname`,
    `avatar`,
    `role`,
    `status`,
    `is_deleted`
) VALUES (
    'testuser',
    '{bcrypt}$2b$12$exkzoQQH1NLV9NDe.UI5b.OTypMdSNAN.MvUQH2I3hc5Go334A3q2',
    '测试用户',
    NULL,
    'USER',
    1,
    0
) ON DUPLICATE KEY UPDATE
    `password_hash` = '{bcrypt}$2b$12$exkzoQQH1NLV9NDe.UI5b.OTypMdSNAN.MvUQH2I3hc5Go334A3q2',
    `nickname` = '测试用户',
    `role` = 'USER',
    `status` = 1,
    `is_deleted` = 0;

-- Legacy source: sql/rating_schema.sql
-- User rating schema.
-- Execute after sql/schema.sql, sql/ai_tool_schema.sql, and sql/workflow_runtime_schema.sql.

CREATE TABLE IF NOT EXISTS `user_tool_rating` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `tool_id` BIGINT UNSIGNED NOT NULL COMMENT 'AI tool ID',
    `effect_score` DECIMAL(3, 1) NOT NULL COMMENT 'Effect score, 0-10',
    `ease_score` DECIMAL(3, 1) NOT NULL COMMENT 'Ease score, 0-10',
    `stability_score` DECIMAL(3, 1) NOT NULL COMMENT 'Stability score, 0-10',
    `recommend_score` DECIMAL(3, 1) NOT NULL COMMENT 'Recommend score, 0-10',
    `comment` TEXT DEFAULT NULL COMMENT 'User comment',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tool_rating` (`user_id`, `tool_id`),
    KEY `idx_user_tool_rating_tool` (`tool_id`),
    KEY `idx_user_tool_rating_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Normal user AI tool rating table';

CREATE TABLE IF NOT EXISTS `user_workflow_rating` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `template_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow template ID',
    `instance_id` BIGINT UNSIGNED NOT NULL COMMENT 'Workflow instance ID',
    `effect_score` DECIMAL(3, 1) NOT NULL COMMENT 'Effect score, 0-10',
    `ease_score` DECIMAL(3, 1) NOT NULL COMMENT 'Ease score, 0-10',
    `stability_score` DECIMAL(3, 1) NOT NULL COMMENT 'Stability score, 0-10',
    `recommend_score` DECIMAL(3, 1) NOT NULL COMMENT 'Recommend score, 0-10',
    `comment` TEXT DEFAULT NULL COMMENT 'User comment',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_workflow_rating` (`user_id`, `template_id`, `instance_id`),
    KEY `idx_user_workflow_rating_template` (`template_id`),
    KEY `idx_user_workflow_rating_instance` (`instance_id`),
    KEY `idx_user_workflow_rating_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Normal user workflow rating table';

-- Legacy source: sql/rating_init_data.sql
-- Optional rating init data.
-- This stage does not require seed rating records.

-- Legacy source: sql/statistics_schema.sql
-- Statistics and feedback schema.
-- Execute after sql/schema.sql.

CREATE TABLE IF NOT EXISTS `usage_event` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'User ID when logged in',
    `anonymous_id` VARCHAR(100) DEFAULT NULL COMMENT 'Anonymous visitor ID',
    `event_type` VARCHAR(50) NOT NULL COMMENT 'Event type',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT 'Target type',
    `target_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Target ID',
    `page_url` VARCHAR(500) DEFAULT NULL COMMENT 'Page URL',
    `stay_duration` INT DEFAULT NULL COMMENT 'Stay duration in seconds',
    `input_summary` VARCHAR(500) DEFAULT NULL COMMENT 'Short input summary',
    `extra_json` TEXT DEFAULT NULL COMMENT 'Extra JSON string, not parsed by backend',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    KEY `idx_usage_event_user_id` (`user_id`),
    KEY `idx_usage_event_anonymous_id` (`anonymous_id`),
    KEY `idx_usage_event_event_type` (`event_type`),
    KEY `idx_usage_event_target` (`target_type`, `target_id`),
    KEY `idx_usage_event_create_time` (`create_time`),
    KEY `idx_usage_event_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'User behavior event table';

CREATE TABLE IF NOT EXISTS `survey_feedback` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'User ID when logged in',
    `anonymous_id` VARCHAR(100) DEFAULT NULL COMMENT 'Anonymous visitor ID',
    `scene` VARCHAR(100) NOT NULL COMMENT 'Feedback scene',
    `score` DECIMAL(3, 1) NOT NULL COMMENT 'Score, 0-10',
    `content` TEXT DEFAULT NULL COMMENT 'Feedback content',
    `contact` VARCHAR(100) DEFAULT NULL COMMENT 'Optional contact info',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 normal, 1 deleted',
    PRIMARY KEY (`id`),
    KEY `idx_survey_feedback_user_id` (`user_id`),
    KEY `idx_survey_feedback_scene` (`scene`),
    KEY `idx_survey_feedback_create_time` (`create_time`),
    KEY `idx_survey_feedback_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Survey feedback table';

-- Legacy source: sql/statistics_init_data.sql
-- Optional statistics init data.
-- This stage does not require seed statistics records.

SELECT * FROM survey_feedback ORDER BY id DESC LIMIT 5;

-- Legacy source: sql/case_audit_schema.sql
-- Case audit schema extension.
-- Execute after sql/case_schema.sql.
--
-- Note:
-- Some MySQL versions do not support "ADD COLUMN IF NOT EXISTS".
-- If re-running this script reports duplicate column/index errors,
-- confirm these columns/indexes already exist and skip the ALTER/CREATE INDEX statements.

ALTER TABLE `case_project`
    ADD COLUMN `submit_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '提交用户ID',
    ADD COLUMN `audit_status` VARCHAR(30) NOT NULL DEFAULT 'APPROVED' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
    ADD COLUMN `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    ADD COLUMN `auditor_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核管理员ID';

CREATE INDEX `idx_case_project_audit_status` ON `case_project` (`audit_status`);
CREATE INDEX `idx_case_project_submit_user` ON `case_project` (`submit_user_id`);

UPDATE `case_project`
SET `audit_status` = 'APPROVED'
WHERE `audit_status` IS NULL OR `audit_status` = '';

-- Legacy source: sql/case_audit_init_data.sql
-- Optional case audit init data.
-- Existing cases are handled by case_audit_schema.sql and default to APPROVED.
