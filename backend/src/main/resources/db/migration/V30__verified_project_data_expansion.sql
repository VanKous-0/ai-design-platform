ALTER TABLE `prompt_template`
    ADD COLUMN `source_type` VARCHAR(30) NOT NULL DEFAULT 'DEMO' COMMENT 'ORIGINAL, RECONSTRUCTED or DEMO' AFTER `source_desc`,
    ADD COLUMN `source_file` VARCHAR(255) DEFAULT NULL COMMENT 'Evidence file name' AFTER `source_type`,
    ADD COLUMN `source_page` VARCHAR(50) DEFAULT NULL COMMENT 'Evidence page or section' AFTER `source_file`,
    ADD KEY `idx_prompt_source_type` (`source_type`);

ALTER TABLE `ai_tool`
    ADD COLUMN `data_status` VARCHAR(30) NOT NULL DEFAULT 'MANUAL' COMMENT 'VERIFIED_USE, RESEARCH_CANDIDATE or MANUAL' AFTER `version_desc`,
    ADD COLUMN `source_desc` VARCHAR(500) DEFAULT NULL COMMENT 'Evidence or maintenance note' AFTER `data_status`,
    ADD KEY `idx_ai_tool_data_status` (`data_status`);

ALTER TABLE `ai_tool_evaluation`
    ADD COLUMN `data_status` VARCHAR(30) NOT NULL DEFAULT 'DEMO' COMMENT 'VERIFIED, MANUAL or DEMO' AFTER `comment`,
    ADD COLUMN `source_desc` VARCHAR(500) DEFAULT NULL COMMENT 'Evaluation evidence' AFTER `data_status`,
    ADD KEY `idx_ai_tool_evaluation_status` (`data_status`);

ALTER TABLE `evaluation_dimension`
    ADD COLUMN `weight_percent` DECIMAL(5, 2) DEFAULT NULL COMMENT 'Research weight percentage' AFTER `description`,
    ADD COLUMN `source_desc` VARCHAR(500) DEFAULT NULL COMMENT 'Dimension source' AFTER `weight_percent`;

ALTER TABLE `review_record`
    MODIFY COLUMN `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Optional submitter; null for evidence-derived project review',
    ADD COLUMN `case_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Related verified case' AFTER `tool_id`,
    ADD COLUMN `source_type` VARCHAR(30) NOT NULL DEFAULT 'DEMO' COMMENT 'VERIFIED or DEMO' AFTER `review_date`,
    ADD COLUMN `source_file` VARCHAR(255) DEFAULT NULL COMMENT 'Evidence file name' AFTER `source_type`,
    ADD COLUMN `source_page` VARCHAR(50) DEFAULT NULL COMMENT 'Evidence page or section' AFTER `source_file`,
    ADD COLUMN `source_desc` VARCHAR(500) DEFAULT NULL COMMENT 'Evidence explanation' AFTER `source_page`,
    ADD KEY `idx_review_case` (`case_id`),
    ADD KEY `idx_review_source_type` (`source_type`);

CREATE TABLE IF NOT EXISTS `project_achievement` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(80) NOT NULL,
    `achievement_type` VARCHAR(30) NOT NULL COMMENT 'AWARD, COMPETITION_ENTRY, DESIGN_WORK or BUSINESS_PLAN',
    `title` VARCHAR(200) NOT NULL,
    `project_name` VARCHAR(200) DEFAULT NULL,
    `competition_name` VARCHAR(255) DEFAULT NULL,
    `issuer` VARCHAR(200) DEFAULT NULL,
    `award_level` VARCHAR(100) DEFAULT NULL,
    `achievement_date` DATE DEFAULT NULL,
    `participants` VARCHAR(500) DEFAULT NULL,
    `summary` VARCHAR(1000) DEFAULT NULL,
    `evidence_url` VARCHAR(500) DEFAULT NULL,
    `source_file` VARCHAR(255) DEFAULT NULL,
    `source_desc` VARCHAR(500) DEFAULT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_achievement_code` (`code`),
    KEY `idx_project_achievement_type_sort` (`achievement_type`, `sort_order`),
    KEY `idx_project_achievement_status` (`status`, `is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Verified project awards, entries and works';

UPDATE `prompt_template`
SET `source_type` = 'ORIGINAL',
    `source_file` = CASE
        WHEN `code` = 'ORIGINAL_STATION_RENDER_KEYWORDS' THEN '车站展板生成过程.pdf'
        ELSE '游客中心生成过程.pdf'
    END,
    `source_page` = CASE
        WHEN `code` = 'ORIGINAL_VISITOR_SITE_PLAN' THEN '第7页'
        WHEN `code` = 'ORIGINAL_VISITOR_BOARD_HTML' THEN '第11页'
        WHEN `code` = 'ORIGINAL_VISITOR_RENDER_KEYWORDS' THEN '第13页'
        WHEN `code` = 'ORIGINAL_STATION_RENDER_KEYWORDS' THEN '第7页'
        ELSE NULL
    END
WHERE `code` IN (
    'ORIGINAL_VISITOR_SITE_PLAN',
    'ORIGINAL_VISITOR_BOARD_HTML',
    'ORIGINAL_VISITOR_RENDER_KEYWORDS',
    'ORIGINAL_STATION_RENDER_KEYWORDS'
);

UPDATE `prompt_template`
SET `source_type` = 'DEMO'
WHERE `source_type` IS NULL OR `source_type` = '';

UPDATE `ai_tool_evaluation`
SET `data_status` = 'DEMO',
    `source_desc` = '开发期主观示例评分，未经过正式实验，不作为公开研究结论。';

UPDATE `ai_tool`
SET `data_status` = CASE
        WHEN `code` IN (
            'DEEPSEEK', 'KIMI', 'TENCENT_YUANBAO', 'MAKET_AI',
            'STABLE_DIFFUSION', 'LIBLIB_AI', 'TRIPO_AI', 'MIDJOURNEY'
        ) THEN 'VERIFIED_USE'
        ELSE 'RESEARCH_CANDIDATE'
    END,
    `source_desc` = CASE
        WHEN `code` IN (
            'DEEPSEEK', 'KIMI', 'TENCENT_YUANBAO', 'MAKET_AI',
            'STABLE_DIFFUSION', 'LIBLIB_AI', 'TRIPO_AI', 'MIDJOURNEY'
        ) THEN '两份项目生成过程材料记录了实际使用。'
        ELSE '商业计划书或前期工具库列为研究候选，现有材料未证明用于最终项目流程。'
    END;

INSERT INTO `ai_tool`
(`name`, `code`, `official_url`, `description`, `price_desc`, `version_desc`,
 `data_status`, `source_desc`, `status`, `is_deleted`)
VALUES
('Hypar', 'HYPAR', 'https://hypar.io/',
 '项目过程材料记录用于家具摆放、空间布局和CAD图纸生成基础。',
 NULL, 'Web service',
 'VERIFIED_USE', '来源：游客中心生成过程.pdf第4页、车站展板生成过程.pdf第3页。', 1, 0)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`official_url` = VALUES(`official_url`),
`description` = VALUES(`description`),
`data_status` = VALUES(`data_status`),
`source_desc` = VALUES(`source_desc`),
`status` = 1,
`is_deleted` = 0;

INSERT INTO `evaluation_dimension`
(`name`, `code`, `description`, `weight_percent`, `source_desc`, `sort_order`, `status`)
VALUES
('专业性', 'RESEARCH_PROFESSIONALISM', '是否针对建筑设计或景观设计任务优化。', 30.00,
 '智绘绿境——商业计划书.pdf，第15页“AI工具筛选标准”。', 110, 1),
('输出质量', 'RESEARCH_OUTPUT_QUALITY', '生成结果的可用性与美观度。', 25.00,
 '智绘绿境——商业计划书.pdf，第15页“AI工具筛选标准”。', 120, 1),
('易用性', 'RESEARCH_USABILITY', '学习成本和界面友好度。', 20.00,
 '智绘绿境——商业计划书.pdf，第15页“AI工具筛选标准”。', 130, 1),
('集成性', 'RESEARCH_INTEGRATION', '能否与其他工具形成连续工作流。', 15.00,
 '智绘绿境——商业计划书.pdf，第15页“AI工具筛选标准”。', 140, 1),
('成本', 'RESEARCH_COST', '是否免费或学生可负担。', 10.00,
 '智绘绿境——商业计划书.pdf，第15页“AI工具筛选标准”。', 150, 1)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`description` = VALUES(`description`),
`weight_percent` = VALUES(`weight_percent`),
`source_desc` = VALUES(`source_desc`),
`sort_order` = VALUES(`sort_order`),
`status` = 1;

INSERT IGNORE INTO `ai_tool_stage_rel` (`tool_id`, `stage_id`)
SELECT t.id, s.id
FROM `ai_tool` t
JOIN `workflow_stage` s ON s.code IN (
    CASE
        WHEN t.code = 'DEEPSEEK' THEN 'RESEARCH'
        WHEN t.code IN ('KIMI', 'TENCENT_YUANBAO', 'STABLE_DIFFUSION', 'LIBLIB_AI', 'MAKET_AI') THEN 'CONCEPT'
        WHEN t.code IN ('TRIPO_AI', 'HYPAR') THEN 'DEVELOPMENT'
        ELSE 'PRESENTATION'
    END
)
WHERE t.code IN (
    'DEEPSEEK', 'KIMI', 'TENCENT_YUANBAO', 'STABLE_DIFFUSION',
    'LIBLIB_AI', 'MAKET_AI', 'TRIPO_AI', 'HYPAR', 'MIDJOURNEY'
);

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '游客中心调研数据整理与问题提炼', 'RECON_VISITOR_RESEARCH_SYNTHESIS', '调研分析',
'请根据以下游客中心场地调研材料进行归纳整理：{siteData}。请按区位与交通、周边环境、使用人群、问卷结论、场地问题和设计机会六部分输出。不要只罗列数据，要指出各项数据对游客中心功能、流线和公共空间设计的影响，并将无法从材料确认的内容明确标记为“待补充”。',
'输入实地测量、区位观察、人群记录和问卷数据。', '结构化调研结论和设计机会点。',
'基于原材料中“利用DeepSeek归纳、整合并分析人群与问卷”的真实步骤重构，不是逐字原始Prompt。',
'根据真实过程重构，保留原文直接、任务导向的表达风格。', 'RECONSTRUCTED',
'游客中心生成过程.pdf', '第1-2页', 110, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'RESEARCH'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '游客中心调研图表HTML生成', 'RECON_VISITOR_RESEARCH_CHART_HTML', '数据可视化',
'请把以下游客中心调研数据生成高度直观化的HTML图表：{researchData}。至少包括人群构成、到访目的、时段分布和需求优先级。请使用清晰的中文标题、图例和数据标签，输出一个可以直接在浏览器打开的完整HTML文件，并在图表下方写出不超过三条设计结论。不得编造输入中没有的数值。',
'输入整理后的真实调研数据。', '可直接打开并转换为图片的HTML图表。',
'材料明确记录使用DeepSeek生成HTML后转换为图片；图表类型根据材料中的可视化目标补全。',
'根据真实过程重构，非原始完整Prompt。', 'RECONSTRUCTED',
'游客中心生成过程.pdf', '第2页', 120, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'RESEARCH'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '游客中心三种空间组合比选', 'RECON_VISITOR_LAYOUT_OPTIONS', '空间布局',
'请根据游客中心任务书生成三种不同的空间组合方式。基础条件如下：{designBrief}。每种方案必须说明入口、门厅、服务空间、休息空间、管理空间与湖岸景观的关系，并分别列出人群适配、流线效率、景观视线和后续立面塑造的优缺点。最后给出推荐方案，但保留人工选择空间。',
'输入游客中心任务书、场地条件和功能需求。', '三种空间布局方案及比选结论。',
'材料明确记录“让DeepSeek提供三种不同空间组合方式，再人工评估选择”。',
'根据真实过程重构，非原始完整Prompt。', 'RECONSTRUCTED',
'游客中心生成过程.pdf', '第3页', 210, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'CONCEPT'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '游客中心效果图提示词生成与筛选', 'RECON_VISITOR_RENDER_PROMPT', '效果图提示词',
'请根据以下游客中心基础要求生成三组效果图提示词：{buildingRequirements}。共同关键词包括公园、游客、现代化、绿色低碳、人行流线、公共设施、长方体建筑和真实风格。三组提示词分别突出自然融合、公共互动和智慧服务。每组都要说明建筑形态、材料、光线、视角、人物活动和周边景观，避免产生不符合建筑尺度的内容。',
'输入游客中心基础要求和选定的展板渲染风格。', '三组可筛选、可继续迭代的效果图提示词。',
'以材料保存的关键词串为主体扩写，用于复现“多次生成、筛选并继续训练”的过程。',
'根据真实关键词和步骤重构，非原始完整Prompt。', 'RECONSTRUCTED',
'游客中心生成过程.pdf', '第10页、第13页', 220, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'CONCEPT'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '游客中心展板精确版式描述', 'RECON_VISITOR_BOARD_LAYOUT', '展板排版',
'请为建筑设计展板提出三种排版方案，展板内容包括效果图、总平面图、平面图、立面图、剖面图、形态推演图、设计说明和元素提取。版式约束为：{layoutConstraint}。请明确每个板块在画面中的占比、位置、阅读顺序、留白和标题层级，并保证效果图为视觉中心。输出可用于继续生成参考版式的详细描述。',
'输入展板尺寸、板块清单和类似“上1/3的右2/3为效果图”的位置约束。', '三套可执行的展板版式描述。',
'材料记录了向Midjourney描述精确占比并输入低权重参考图的过程。',
'根据真实过程重构，非逐字原始Prompt。', 'RECONSTRUCTED',
'游客中心生成过程.pdf', '第11-13页', 410, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'PRESENTATION'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '模块化车站调研框架与需求拆解', 'RECON_STATION_RESEARCH_FRAMEWORK', '调研分析',
'请围绕“模块化多功能公交站”建立调研框架，目标是同时回应全人群需求与立体停车系统。请按人群行为、停车技术、北京案例、模块化可行性、环境适配性五个方向列出：需要查找的数据、可核验的信息来源、关键技术指标和对应设计影响。不要先给方案，先形成可执行的调研提纲。',
'输入项目目标、场地和调研范围。', '五个方向的调研提纲和证据清单。',
'材料第1页明确记录了五个调研方向和使用DeepSeek形成提纲的过程。',
'根据真实过程重构，非原始完整Prompt。', 'RECONSTRUCTED',
'车站展板生成过程.pdf', '第1页', 130, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'RESEARCH'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '车站政策案例与技术参数提取', 'RECON_STATION_POLICY_PARAMETERS', '资料提取',
'请从以下政策、公开报告和案例材料中提取与模块化公交站相关的可核验参数：{sourceMaterials}。按停车配比、模块尺寸、能源效率、空间布局、设施使用率五类整理。每条必须保留来源名称和原始表述；不同来源冲突时并列展示，不得自行合并为一个确定数值。',
'输入政策名称、报告摘录或案例资料。', '带来源的技术参数表和冲突说明。',
'对应材料中对北京交通委、规划院报告和设计导则的提取步骤。',
'根据真实过程重构，非原始完整Prompt。', 'RECONSTRUCTED',
'车站展板生成过程.pdf', '第1页', 140, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'RESEARCH'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '车站设计切口生成与筛选', 'RECON_STATION_CONCEPT_FOCUS', '概念生成',
'已知元素包括车站、服务、多功能、模块化、多种人群和满足需求。请先提出五个可以落到空间和互动设施上的设计切口，每个切口说明解决的问题、目标人群、核心行为和模块化表达方式。请避免空泛口号。初稿输出后，根据我的反馈指出你理解正确和需要修改的部分，再生成第二轮方案。',
'输入基础元素和每轮人工反馈。', '可筛选的设计切口及迭代后的概念。',
'保留原材料强调的“人工筛选、提供反馈、逐步驯化”方法，不把情绪反馈包装为技术指标。',
'根据真实过程重构，非原始完整Prompt。', 'RECONSTRUCTED',
'车站展板生成过程.pdf', '第1-3页', 230, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'CONCEPT'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '模块化车站构件尺寸与布局组合', 'RECON_STATION_COMPONENT_LAYOUT', '空间布局',
'请根据以下模块化车站概念和构件清单，整理各构件的建议尺寸范围，并提出三种布局组合：{componentList}。每种布局说明候车流线、互动设施使用、无障碍通行、立体停车衔接和维护空间。尺寸必须标明是输入数据、规范数据还是待验证建议，不能把建议值写成已确认规范。',
'输入车站概念、构件清单和已核验尺寸。', '尺寸表和三种布局组合。',
'对应材料中使用DeepSeek提供物件尺寸与多种布局，再进入Hypar摆放的步骤。',
'根据真实过程重构，非原始完整Prompt。', 'RECONSTRUCTED',
'车站展板生成过程.pdf', '第3页', 310, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'DEVELOPMENT'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '模块化车站效果图提示词扩写', 'RECON_STATION_RENDER_PROMPT', '效果图提示词',
'请以这些关键词为主体扩写模块化车站效果图提示词：车站、私密性、现实风格、现代科技、LED屏幕、智能、模块化、组合、城市、小尺度、繁华。项目目标是解决低头族问题，并具有功能性和艺术性的双重墙面。请生成三组提示词，分别突出互动唤醒、智能服务和模块组合，并补充材料、光线、视角、人物行为和城市背景。',
'输入选定概念、立面特点和展板渲染风格。', '三组用于文生图迭代的提示词。',
'以材料保存的关键词和创意说明为主体扩写。',
'根据真实关键词重构，非原始完整Prompt。', 'RECONSTRUCTED',
'车站展板生成过程.pdf', '第4页、第7页', 240, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'CONCEPT'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '模块化车站A1展板HTML排版', 'RECON_STATION_A1_BOARD_HTML', '展板排版',
'请生成一个可直接在浏览器打开的A1建筑设计展板HTML。内容必须包括：大标题、总平面图1张、平面图1张、立面图2张、剖面图2张、流线分析1张、区位分析板块、较大面积的效果图、设计说明和人群使用分析。请设置清晰的网格、图框占位、中文标题层级和打印尺寸，效果图为主要视觉中心，输出完整HTML和CSS。',
'输入展板标题、图片路径和各板块文字。', '可继续调整并转换为图片的A1展板HTML。',
'板块清单和A1尺寸来自原材料，HTML结构为可执行化整理。',
'根据真实过程重构，非原始完整Prompt。', 'RECONSTRUCTED',
'车站展板生成过程.pdf', '第5-6页', 420, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'PRESENTATION'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `source_type`, `source_file`, `source_page`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, 'AI工具组合记录与阶段复盘', 'RECON_WORKFLOW_TOOL_REVIEW', '过程复盘',
'请根据以下设计过程记录整理工具链：{processLog}。按前期分析、概念生成、深化设计、表达呈现四阶段列出每个工具的输入、输出、选择理由、需要人工判断的环节和可替代工具。只记录过程材料中实际出现的使用，不要把研究候选工具写成已使用工具。',
'输入真实过程日志或工具使用清单。', '可追溯的阶段工具链和复盘。',
'对应商业计划书的全流程公式和两份项目过程记录，用于形成证据化复盘。',
'根据真实过程重构，非原始完整Prompt。', 'RECONSTRUCTED',
'智绘绿境——商业计划书.pdf；游客中心生成过程.pdf；车站展板生成过程.pdf',
'商业计划书第16-17页；过程材料全篇', 430, 0, 1, 0
FROM workflow_stage s WHERE s.code = 'PRESENTATION'
ON DUPLICATE KEY UPDATE `content`=VALUES(`content`), `source_type`='RECONSTRUCTED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`), `status`=1, `is_deleted`=0;

INSERT INTO `prompt_parameter`
(`prompt_id`, `param_key`, `param_name`, `param_type`, `required`, `placeholder`, `sort_order`, `is_deleted`)
SELECT p.id, x.param_key, x.param_name, 'textarea', 1, x.placeholder, 10, 0
FROM prompt_template p
JOIN (
    SELECT 'RECON_VISITOR_RESEARCH_SYNTHESIS' code, 'siteData' param_key, '场地调研数据' param_name, '粘贴实地调研、问卷和人群数据' placeholder
    UNION ALL SELECT 'RECON_VISITOR_RESEARCH_CHART_HTML', 'researchData', '调研数据', '粘贴已核验的数据'
    UNION ALL SELECT 'RECON_VISITOR_LAYOUT_OPTIONS', 'designBrief', '设计任务书', '输入场地与功能要求'
    UNION ALL SELECT 'RECON_VISITOR_RENDER_PROMPT', 'buildingRequirements', '建筑基础要求', '输入选定方案与渲染要求'
    UNION ALL SELECT 'RECON_VISITOR_BOARD_LAYOUT', 'layoutConstraint', '版式约束', '例如上1/3的右2/3为效果图'
    UNION ALL SELECT 'RECON_STATION_RESEARCH_FRAMEWORK', 'projectContext', '项目背景', '输入车站场地和目标'
    UNION ALL SELECT 'RECON_STATION_POLICY_PARAMETERS', 'sourceMaterials', '来源材料', '粘贴政策、报告或案例摘录'
    UNION ALL SELECT 'RECON_STATION_CONCEPT_FOCUS', 'conceptFeedback', '概念反馈', '输入基础元素与人工反馈'
    UNION ALL SELECT 'RECON_STATION_COMPONENT_LAYOUT', 'componentList', '构件清单', '输入构件、尺寸和约束'
    UNION ALL SELECT 'RECON_STATION_RENDER_PROMPT', 'stationConcept', '车站概念', '输入概念和立面特点'
    UNION ALL SELECT 'RECON_STATION_A1_BOARD_HTML', 'boardContent', '展板内容', '输入标题、图片和说明'
    UNION ALL SELECT 'RECON_WORKFLOW_TOOL_REVIEW', 'processLog', '过程记录', '粘贴真实过程日志'
) x ON x.code = p.code
ON DUPLICATE KEY UPDATE
`param_name`=VALUES(`param_name`), `param_type`=VALUES(`param_type`),
`required`=VALUES(`required`), `placeholder`=VALUES(`placeholder`), `is_deleted`=0;

INSERT IGNORE INTO `prompt_tool_rel` (`prompt_id`, `tool_id`)
SELECT p.id, t.id
FROM prompt_template p
JOIN ai_tool t ON (
    (p.code IN (
        'RECON_VISITOR_RESEARCH_SYNTHESIS', 'RECON_VISITOR_RESEARCH_CHART_HTML',
        'RECON_VISITOR_LAYOUT_OPTIONS', 'RECON_STATION_RESEARCH_FRAMEWORK',
        'RECON_STATION_POLICY_PARAMETERS', 'RECON_STATION_CONCEPT_FOCUS',
        'RECON_STATION_COMPONENT_LAYOUT', 'RECON_STATION_A1_BOARD_HTML',
        'RECON_WORKFLOW_TOOL_REVIEW'
    ) AND t.code = 'DEEPSEEK')
    OR (p.code IN ('RECON_VISITOR_RENDER_PROMPT', 'RECON_STATION_RENDER_PROMPT') AND t.code IN ('KIMI', 'TENCENT_YUANBAO'))
    OR (p.code = 'RECON_VISITOR_BOARD_LAYOUT' AND t.code = 'MIDJOURNEY')
);

INSERT INTO `workflow_template`
(`name`, `code`, `description`, `scene_type`, `sort_order`, `status`, `is_deleted`)
VALUES
('智驿游客中心完整设计流程', 'verified_visitor_center_workflow',
 '根据游客中心生成过程材料整理，覆盖调研、布局、图纸、景观、效果图、模型、渲染和展板。',
 'verified_project', 30, 1, 0),
('抬头能量站完整设计流程', 'verified_energy_station_workflow',
 '根据模块化车站生成过程材料整理，覆盖调研、概念、布局、图纸、效果图、模型、渲染和展板。',
 'verified_project', 40, 1, 0),
('景观建筑AI设计公式', 'verified_ai_design_formula',
 '根据商业计划书中的六条设计公式整理，作为通用方法论模板，不代表平台内自动调用AI。',
 'research_method', 50, 1, 0)
ON DUPLICATE KEY UPDATE
`name`=VALUES(`name`), `description`=VALUES(`description`), `scene_type`=VALUES(`scene_type`),
`sort_order`=VALUES(`sort_order`), `status`=1, `is_deleted`=0;

INSERT INTO `workflow_template_node`
(`template_id`, `stage_id`, `node_name`, `node_code`, `node_type`, `input_desc`, `output_desc`, `next_tip`, `sort_order`, `status`, `is_deleted`)
SELECT t.id, s.id, x.node_name, x.node_code, x.node_type, x.input_desc, x.output_desc, x.next_tip, x.sort_order, 1, 0
FROM workflow_template t
JOIN (
    SELECT 'verified_visitor_center_workflow' template_code, 'RESEARCH' stage_code, '实地调研与数据整理' node_name, 'visitor_research' node_code, 'input' node_type, '实地测量、区位、人群和问卷数据。' input_desc, '结构化调研结论和可视化图表。' output_desc, '进入空间布局比选。' next_tip, 10 sort_order
    UNION ALL SELECT 'verified_visitor_center_workflow','CONCEPT','空间组合与方案筛选','visitor_layout','ai_prompt','任务书、功能需求和场地条件。','三种空间组合与人工选定方案。','进入图纸和景观深化。',20
    UNION ALL SELECT 'verified_visitor_center_workflow','DEVELOPMENT','图纸布局与CAD产出','visitor_drawings','external_tool','选定布局和家具需求。','Hypar布局基础、CAD图纸和细化成果。','进入景观总平面。',30
    UNION ALL SELECT 'verified_visitor_center_workflow','DEVELOPMENT','景观总平面生成','visitor_site_plan','ai_prompt','建筑位置、湖岸场地和景观功能。','筛选后的总平面和LoRA细化结果。','进入效果图和模型。',40
    UNION ALL SELECT 'verified_visitor_center_workflow','DEVELOPMENT','效果图与模型迭代','visitor_render_model','external_tool','效果图提示词和建筑立面要求。','文生图结果、Tripo模型和UV贴图。','进入后期渲染。',50
    UNION ALL SELECT 'verified_visitor_center_workflow','PRESENTATION','渲染与展板表达','visitor_board','ai_prompt','图纸、效果图、模型视角和说明。','最终展板和成果表达。','流程完成并形成复盘。',60
    UNION ALL SELECT 'verified_energy_station_workflow','RESEARCH','五方向调研与参数验证','station_research','ai_prompt','政策、案例、人群和停车数据。','调研框架、参数表和可视化。','进入设计切口筛选。',10
    UNION ALL SELECT 'verified_energy_station_workflow','CONCEPT','低头族问题与概念迭代','station_concept','ai_prompt','车站基础元素和人工反馈。','选定的设计切口与概念。','进入构件布局。',20
    UNION ALL SELECT 'verified_energy_station_workflow','DEVELOPMENT','构件尺寸与布局生成','station_layout','ai_prompt','模块构件、尺寸和功能需求。','多种布局、Hypar摆放与CAD基础。','进入效果图和模型。',30
    UNION ALL SELECT 'verified_energy_station_workflow','DEVELOPMENT','效果图与模型迭代','station_render_model','external_tool','选定概念、提示词和立面要求。','腾讯元宝图像、PS调整和Tripo模型。','进入后期渲染。',40
    UNION ALL SELECT 'verified_energy_station_workflow','PRESENTATION','渲染与A1展板表达','station_board','ai_prompt','图纸、效果图、分析图和说明。','A1展板HTML及最终图像。','流程完成并形成复盘。',50
    UNION ALL SELECT 'verified_ai_design_formula','RESEARCH','场地分析公式','formula_site_analysis','method','实地调研数据。','DeepSeek整理、HTML图表和可视化结果。','进入概念与总平面。',10
    UNION ALL SELECT 'verified_ai_design_formula','CONCEPT','总平面图生成公式','formula_site_plan','method','概念草图。','Stable Diffusion平面生成和Photoshop细化。','进入平面图。',20
    UNION ALL SELECT 'verified_ai_design_formula','DEVELOPMENT','平面生成公式','formula_floor_plan','method','空间布局需求。','Maket布局、Hypar线稿和渲染成果。','进入剖面和模型。',30
    UNION ALL SELECT 'verified_ai_design_formula','DEVELOPMENT','剖面生成公式','formula_section','method','模型截面和关键词。','迭代渲染及Photoshop细化。','进入模型构建。',40
    UNION ALL SELECT 'verified_ai_design_formula','DEVELOPMENT','模型构建公式','formula_model','method','概念草图。','Tripo模型和参数化调整结果。','进入图像表达。',50
    UNION ALL SELECT 'verified_ai_design_formula','PRESENTATION','图像表达公式','formula_presentation','method','3D模型和表达需求。','建筑学长、知末、Midjourney组合渲染结果。','流程完成。',60
) x ON x.template_code = t.code
JOIN workflow_stage s ON s.code = x.stage_code
ON DUPLICATE KEY UPDATE
`node_name`=VALUES(`node_name`), `stage_id`=VALUES(`stage_id`), `node_type`=VALUES(`node_type`),
`input_desc`=VALUES(`input_desc`), `output_desc`=VALUES(`output_desc`),
`next_tip`=VALUES(`next_tip`), `sort_order`=VALUES(`sort_order`), `status`=1, `is_deleted`=0;

INSERT IGNORE INTO `workflow_node_prompt_rel` (`node_id`, `prompt_id`, `sort_order`)
SELECT n.id, p.id, 10
FROM workflow_template_node n
JOIN prompt_template p ON (
    (n.node_code = 'visitor_research' AND p.code IN ('RECON_VISITOR_RESEARCH_SYNTHESIS','RECON_VISITOR_RESEARCH_CHART_HTML'))
    OR (n.node_code = 'visitor_layout' AND p.code = 'RECON_VISITOR_LAYOUT_OPTIONS')
    OR (n.node_code = 'visitor_site_plan' AND p.code = 'ORIGINAL_VISITOR_SITE_PLAN')
    OR (n.node_code = 'visitor_render_model' AND p.code IN ('ORIGINAL_VISITOR_RENDER_KEYWORDS','RECON_VISITOR_RENDER_PROMPT'))
    OR (n.node_code = 'visitor_board' AND p.code IN ('ORIGINAL_VISITOR_BOARD_HTML','RECON_VISITOR_BOARD_LAYOUT'))
    OR (n.node_code = 'station_research' AND p.code IN ('RECON_STATION_RESEARCH_FRAMEWORK','RECON_STATION_POLICY_PARAMETERS'))
    OR (n.node_code = 'station_concept' AND p.code = 'RECON_STATION_CONCEPT_FOCUS')
    OR (n.node_code = 'station_layout' AND p.code = 'RECON_STATION_COMPONENT_LAYOUT')
    OR (n.node_code = 'station_render_model' AND p.code IN ('ORIGINAL_STATION_RENDER_KEYWORDS','RECON_STATION_RENDER_PROMPT'))
    OR (n.node_code = 'station_board' AND p.code = 'RECON_STATION_A1_BOARD_HTML')
);

UPDATE `review_record`
SET `status` = 0,
    `source_type` = 'DEMO',
    `source_desc` = '开发期演示复盘，已由有材料来源的项目复盘替代。'
WHERE `code` IN (
    'REVIEW_RESEARCH_DATA_ORGANIZE',
    'REVIEW_CONCEPT_IDEA',
    'REVIEW_DEVELOPMENT_COMPARE',
    'REVIEW_PRESENTATION_COPY'
);

INSERT INTO `review_record`
(`user_id`, `title`, `code`, `stage_id`, `tool_id`, `case_id`, `project_name`,
 `summary`, `problem_desc`, `solution_desc`, `reflection`, `score`, `review_date`,
 `source_type`, `source_file`, `source_page`, `source_desc`, `sort_order`, `status`, `is_deleted`)
SELECT NULL, x.title, x.code, s.id, t.id, c.id, x.project_name,
       x.summary, x.problem_desc, x.solution_desc, x.reflection, NULL, NULL,
       'VERIFIED', x.source_file, x.source_page, x.source_desc, x.sort_order, 1, 0
FROM (
    SELECT '游客中心调研与数据可视化复盘' title, 'VERIFIED_VISITOR_RESEARCH' code, 'RESEARCH' stage_code, 'DEEPSEEK' tool_code, 'REAL_VISITOR_CENTER_ZHIYI' case_code, '智驿游客中心' project_name,
           '实地测量和问卷数据经AI归纳，并通过HTML转换形成可视化图表。' summary,
           '原始数据分散，人群和问卷结论不够直观。' problem_desc,
           '使用DeepSeek归纳数据并生成HTML图表，再转换为图片。' solution_desc,
           'AI承担整理与表达，数据真实性和设计判断仍由团队负责。' reflection,
           '游客中心生成过程.pdf' source_file, '第1-2页' source_page, '依据项目过程文档整理。' source_desc, 10 sort_order
    UNION ALL SELECT '游客中心空间布局比选复盘','VERIFIED_VISITOR_LAYOUT','CONCEPT','DEEPSEEK','REAL_VISITOR_CENTER_ZHIYI','智驿游客中心','先生成三种空间组合，再结合场地人群需求人工筛选。','单一布局难以同时回应功能、场地和立面预期。','DeepSeek生成备选组合，Maket辅助布局并与立面预期双向优化。','多方案生成必须配合人工评估，不能以生成结果代替设计决策。','游客中心生成过程.pdf','第3页','依据项目过程文档整理。',20
    UNION ALL SELECT '游客中心图纸产出复盘','VERIFIED_VISITOR_DRAWINGS','DEVELOPMENT','HYPAR','REAL_VISITOR_CENTER_ZHIYI','智驿游客中心','通过Hypar、CAD、建筑学长和Photoshop形成图纸成果。','AI和在线工具生成结果需要转换为可继续编辑的标准图纸。','Hypar完成家具摆放并作为CAD基础，后续人工渲染和精细化。','工具链价值在于减少重复操作，但专业制图仍需要人工校核。','游客中心生成过程.pdf','第4页','依据项目过程文档整理。',30
    UNION ALL SELECT '游客中心景观总平面复盘','VERIFIED_VISITOR_SITE_PLAN','DEVELOPMENT','STABLE_DIFFUSION','REAL_VISITOR_CENTER_ZHIYI','智驿游客中心','原始总平面指令经筛选后，再通过Stable Diffusion和LiblibAI细化。','初始生成难以同时满足平面逻辑和展板风格。','先筛选符合任务书的方案，再使用LoRA控制线稿和渲染风格。','图像生成需要分阶段控制，不能一次生成后直接作为最终图纸。','游客中心生成过程.pdf','第7页','依据项目过程文档整理。',40
    UNION ALL SELECT '游客中心效果图模型与展板复盘','VERIFIED_VISITOR_PRESENTATION','PRESENTATION','MIDJOURNEY','REAL_VISITOR_CENTER_ZHIYI','智驿游客中心','Kimi、腾讯元宝、Tripo AI与后期工具共同完成效果图、模型和展板。','生成图片、三维模型和展板版式之间风格与尺度容易不一致。','多轮筛选图像，生成模型并选取角度渲染，再通过HTML和参考图探索展板版式。','最终成果来自多工具协同和人工筛选，不是单一模型一次完成。','游客中心生成过程.pdf','第10-13页','依据项目过程文档整理。',50
    UNION ALL SELECT '模块化车站调研复盘','VERIFIED_STATION_RESEARCH','RESEARCH','DEEPSEEK','REAL_LOOK_UP_ENERGY_STATION','抬头能量站','围绕人群、停车、案例、模块化和环境五方向建立调研框架。','政策、案例和技术参数跨来源分散，难以直接支持设计。','使用DeepSeek提取公开材料并结构化参数，使用Godel验证空间效率公式。','参数必须保留来源并单独验证，不能把AI搜索结果直接当作事实。','车站展板生成过程.pdf','第1页','依据项目过程文档整理。',60
    UNION ALL SELECT '模块化车站概念迭代复盘','VERIFIED_STATION_CONCEPT','CONCEPT','DEEPSEEK','REAL_LOOK_UP_ENERGY_STATION','抬头能量站','从多功能车站元素出发，经人工筛选确定解决低头族问题的设计切口。','AI初稿内容杂乱、缺乏重点。','团队持续反馈正确与错误方向，逐步收敛到可执行概念。','反馈能够改善输出，但核心设计切口仍来自人工讨论和选择。','车站展板生成过程.pdf','第1-3页','依据项目过程文档整理。',70
    UNION ALL SELECT '模块化车站图纸与模型复盘','VERIFIED_STATION_DEVELOPMENT','DEVELOPMENT','HYPAR','REAL_LOOK_UP_ENERGY_STATION','抬头能量站','利用尺寸建议、布局组合、Hypar、CAD、腾讯元宝和Tripo AI推进方案。','模块构件、功能活动和尺度控制需要在二维图纸与三维效果之间协调。','先生成尺寸和布局备选，再完成人工选择、制图、文生图和模型贴图。','跨工具转换必须保存中间版本，便于回退和比较。','车站展板生成过程.pdf','第3-4页','依据项目过程文档整理。',80
    UNION ALL SELECT '模块化车站A1展板复盘','VERIFIED_STATION_BOARD','PRESENTATION','DEEPSEEK','REAL_LOOK_UP_ENERGY_STATION','抬头能量站','明确A1展板板块后，通过HTML多轮调整形成最终排版。','首次生成的板块比例和视觉层级通常不满足展示要求。','明确图纸数量、分析板块和效果图占比，反复调整HTML后转换为图像。','排版生成适合从严格内容清单开始，并通过多轮人工审查收敛。','车站展板生成过程.pdf','第5-7页','依据项目过程文档整理。',90
) x
JOIN workflow_stage s ON s.code = x.stage_code
LEFT JOIN ai_tool t ON t.code = x.tool_code
LEFT JOIN case_project c ON c.code = x.case_code
ON DUPLICATE KEY UPDATE
`stage_id`=VALUES(`stage_id`), `tool_id`=VALUES(`tool_id`), `case_id`=VALUES(`case_id`),
`summary`=VALUES(`summary`), `problem_desc`=VALUES(`problem_desc`),
`solution_desc`=VALUES(`solution_desc`), `reflection`=VALUES(`reflection`),
`score`=NULL, `review_date`=NULL, `source_type`='VERIFIED',
`source_file`=VALUES(`source_file`), `source_page`=VALUES(`source_page`),
`source_desc`=VALUES(`source_desc`), `status`=1, `is_deleted`=0;

UPDATE `site_content`
SET `status` = 0
WHERE `id` BETWEEN 1 AND 8;

INSERT INTO `site_content`
(`section_key`, `title`, `subtitle`, `content`, `image_url`, `link_url`, `extra_json`, `sort_order`, `status`, `is_deleted`)
VALUES
('hero', '智绘绿境', '面向景观建筑全周期的生成式AI辅助决策服务平台',
 '平台围绕前期分析、概念生成、深化设计和成果表达组织工作流、工具、提示词、真实案例与过程复盘。当前版本负责流程与数据闭环，不直接调用外部AI模型。',
 '/assets/cases/boundless-garden/cover.jpg', '/api/workflow-templates',
 '{"primaryButtonText":"查看真实工作流","secondaryButtonText":"浏览项目案例"}', 10, 1, 0),
('intro', '项目研究内容', '真实项目方法沉淀与后端数据支撑',
 '项目研究AI设计流程、工具筛选、提示词方法和跨工具组合，并使用游客中心、模块化车站、社区服务中心和微花园项目进行实践验证。',
 '/assets/cases/visitor-center/cover.jpg', '/api/cases',
 '{"source":"项目成果统计表与生成过程材料"}', 10, 1, 0),
('workflow_entry', '四阶段设计工作流', '前期分析—概念生成—深化设计—成果表达',
 '后端已提供真实项目工作流模板、节点提示词、多轮结果记录、评分、选优和实验数据导出。',
 '/assets/cases/energy-station/cover.jpg', '/api/workflow-templates',
 '{"buttonText":"查看工作流"}', 20, 1, 0),
('tool_recommend_entry', '工具使用与筛选', '区分实际使用、研究候选与演示评分',
 '工具库记录项目材料中实际出现的工具链；未经过正式实验的主观示例评分不作为公开研究结论。',
 NULL, '/api/tools', '{"buttonText":"查看工具"}', 30, 1, 0),
('prompt_entry', '可追溯提示词库', '原始摘录与重构模板明确区分',
 'ORIGINAL为材料保存的原文，RECONSTRUCTED为依据真实步骤和原文风格整理的可复用模板。',
 NULL, '/api/prompts', '{"buttonText":"查看提示词"}', 40, 1, 0),
('case_entry', '真实案例成果', '四个项目案例及过程材料',
 '案例库展示智驿游客中心、抬头能量站、邻聚驿社区服务中心和无界视界微花园。',
 '/assets/cases/neighborhood-center/cover.jpg', '/api/cases',
 '{"buttonText":"查看案例"}', 50, 1, 0),
('review_entry', '项目过程复盘', '从真实生成过程整理问题、方法和反思',
 '复盘数据不填写无法证明的分数，保留来源文件与页码，支持答辩和后续实验。',
 NULL, '/api/reviews', '{"buttonText":"查看复盘"}', 60, 1, 0),
('contact', '项目说明', '北京林业大学园林学院大学生创新创业训练项目',
 '项目名称：智绘绿境：生成式AI驱动的景观建筑全流程智能化设计研究。',
 NULL, NULL, '{"projectNumber":"65","college":"园林学院"}', 70, 1, 0);

UPDATE `award_record`
SET `status` = 0
WHERE `summary` LIKE '%Demo%' OR `title` IN (
    'Innovation Project Approval',
    'Midterm Review Passed',
    'Campus Showcase',
    'Innovation Competition Award',
    'College Course Achievement Showcase'
);

INSERT INTO `award_record`
(`title`, `award_level`, `issuer`, `award_date`, `summary`, `image_url`, `sort_order`, `status`, `is_deleted`)
VALUES
('“无界视界”——示范性花园设计', '省级一等奖',
 '“青创北京”2025“挑战杯”首都大学生课外学术科技作品竞赛组委会',
 '2025-05-19', '获“青砺基层”专项赛一等奖。来源为成果统计表及获奖证明。',
 '/assets/cases/boundless-garden/award.jpg', 10, 1, 0);

INSERT INTO `project_achievement`
(`code`, `achievement_type`, `title`, `project_name`, `competition_name`, `issuer`,
 `award_level`, `achievement_date`, `participants`, `summary`, `evidence_url`,
 `source_file`, `source_desc`, `sort_order`, `status`, `is_deleted`)
VALUES
('ACH_AWARD_BOUNDLESS_GARDEN', 'AWARD', '“无界视界”——示范性花园设计省级一等奖',
 '“无界视界”——示范性花园设计',
 '“青创北京”2025“挑战杯”首都大学生课外学术科技作品竞赛——揭榜挂帅专项',
 '“青创北京”2025“挑战杯”首都大学生课外学术科技作品竞赛组委会',
 '省级一等奖', '2025-05-19', '田婧辰，赵佳佳，冷姿颖',
 '成果统计表记录为“青砺基层”专项赛一等奖。',
 '/assets/cases/boundless-garden/award.jpg',
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx；2025北京林业大学挑战杯获奖证明.pdf',
 '获奖信息有成果统计表和证书材料支撑。', 10, 1, 0),
('ACH_WORK_VISITOR_CENTER', 'DESIGN_WORK', '智驿',
 'AIGC生成式奥林匹克森林公园环岛改造游客中心',
 NULL, NULL, NULL, NULL, '田婧辰，赵佳佳，冷姿颖，张舒雅，张家宇',
 '奥森游客中心设计图及生成过程。',
 '/assets/cases/visitor-center/board.jpg',
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx；游客中心生成过程.pdf',
 '成果统计表“实物或设计作品”第1项。', 20, 1, 0),
('ACH_ENTRY_VISITOR_CENTER_NCDA', 'COMPETITION_ENTRY', '智驿游客中心参赛记录',
 'AIGC生成式奥林匹克森林公园环岛改造游客中心',
 '未来设计师比赛', NULL, NULL, NULL, '田婧辰，赵佳佳，冷姿颖，张舒雅，张家宇',
 '成果统计表记录为参与未来设计师比赛，不表述为获奖。',
 '/assets/cases/visitor-center/board.jpg',
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx；未来设计师参赛证明.png',
 '参赛信息有成果统计表和参赛证明支撑。', 30, 1, 0),
('ACH_WORK_ENERGY_STATION', 'DESIGN_WORK', '抬头能量站',
 'AIGC生成式模块化车站设计',
 NULL, NULL, NULL, NULL, '田婧辰，赵佳佳，冷姿颖，张舒雅，张家宇',
 '针对低头族问题的模块化智能车站设计图。',
 '/assets/cases/energy-station/board.jpg',
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx；车站展板生成过程.pdf',
 '成果统计表“实物或设计作品”第2项。', 40, 1, 0),
('ACH_ENTRY_ENERGY_STATION_NCDA', 'COMPETITION_ENTRY', '抬头能量站参赛记录',
 'AIGC生成式模块化车站设计',
 '未来设计师比赛', NULL, NULL, NULL, '田婧辰，赵佳佳，冷姿颖，张舒雅，张家宇',
 '成果统计表记录为参与未来设计师比赛，不表述为获奖。',
 '/assets/cases/energy-station/board.jpg',
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx；未来设计师参赛证明2.png',
 '参赛信息有成果统计表和参赛证明支撑。', 50, 1, 0),
('ACH_WORK_NEIGHBORHOOD', 'DESIGN_WORK', '邻聚驿',
 'AIGC生成式模块化居民服务中心',
 NULL, NULL, NULL, NULL, '田婧辰，赵佳佳，冷姿颖，张舒雅，张家宇',
 '根据北京社区普遍情况设计的模块化居民服务中心。',
 '/assets/cases/neighborhood-center/board-overview.jpg',
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx；社区服务中心系列展板',
 '成果统计表“实物或设计作品”第3项和第5项指向同一项目成果。', 60, 1, 0),
('ACH_ENTRY_NEIGHBORHOOD_CREATIVE', 'COMPETITION_ENTRY', '邻聚驿好创意参赛记录',
 'AIGC生成式模块化居民服务中心',
 '好创意比赛', NULL, NULL, NULL, '田婧辰，赵佳佳，冷姿颖，张舒雅，张家宇',
 '成果统计表记录为参与好创意比赛，不表述为获奖。',
 '/assets/cases/neighborhood-center/board-overview.jpg',
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx；好创意存证.png',
 '参赛信息有成果统计表和存证材料支撑。', 70, 1, 0),
('ACH_ENTRY_NEIGHBORHOOD_HUACAN', 'COMPETITION_ENTRY', '邻聚驿华灿杯参赛记录',
 '邻聚驿：根据北京社区普遍情况结合AI设计的社区服务中心',
 '华灿杯', NULL, NULL, NULL, '田婧辰，赵佳佳，冷姿颖，张舒雅，张家宇',
 '成果统计表记录为参加华灿杯比赛，不表述为获奖。',
 '/assets/cases/neighborhood-center/board-detail.jpg',
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx',
 '参赛信息来自成果统计表。', 80, 1, 0),
('ACH_WORK_BOUNDLESS_GARDEN', 'DESIGN_WORK', '无界视界',
 '“无界视界”——示范性花园设计',
 NULL, NULL, NULL, NULL, '田婧辰，赵佳佳，冷姿颖',
 '融合疗愈花园、包容性生态美学和智慧技术的微花园设计。',
 '/assets/cases/boundless-garden/board.jpg',
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx；微花园展板.jpg',
 '成果统计表“实物或设计作品”第4项。', 90, 1, 0),
('ACH_BUSINESS_PLAN', 'BUSINESS_PLAN', '智绘绿境商业计划书',
 '智绘绿境：生成式AI驱动的景观建筑全流程智能化设计研究',
 '未来设计师——AIGC专项比赛', NULL, NULL, NULL,
 '田婧辰，赵佳佳，冷姿颖，张舒雅，张家宇',
 '项目商业计划书，包含行业分析、工具筛选标准、六条AI设计公式和运营规划。',
 NULL,
 '北京林业大学大学生创新创业训练项目成果统计表.xlsx；智绘绿境——商业计划书.pdf',
 '成果统计表“实物或设计作品”第6项。', 100, 1, 0)
ON DUPLICATE KEY UPDATE
`achievement_type`=VALUES(`achievement_type`), `title`=VALUES(`title`),
`project_name`=VALUES(`project_name`), `competition_name`=VALUES(`competition_name`),
`issuer`=VALUES(`issuer`), `award_level`=VALUES(`award_level`),
`achievement_date`=VALUES(`achievement_date`), `participants`=VALUES(`participants`),
`summary`=VALUES(`summary`), `evidence_url`=VALUES(`evidence_url`),
`source_file`=VALUES(`source_file`), `source_desc`=VALUES(`source_desc`),
`sort_order`=VALUES(`sort_order`), `status`=1, `is_deleted`=0;
