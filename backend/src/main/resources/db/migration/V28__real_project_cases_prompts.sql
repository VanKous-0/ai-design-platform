SET @add_prompt_source_desc = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE `prompt_template` ADD COLUMN `source_desc` VARCHAR(500) DEFAULT NULL COMMENT ''Prompt source and evidence''',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'prompt_template'
      AND column_name = 'source_desc'
);
PREPARE add_prompt_source_desc_stmt FROM @add_prompt_source_desc;
EXECUTE add_prompt_source_desc_stmt;
DEALLOCATE PREPARE add_prompt_source_desc_stmt;

CREATE TABLE IF NOT EXISTS `case_tool_usage` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `case_id` BIGINT UNSIGNED NOT NULL,
    `tool_id` BIGINT UNSIGNED DEFAULT NULL,
    `tool_name` VARCHAR(100) NOT NULL,
    `tool_code` VARCHAR(80) NOT NULL,
    `tool_type` VARCHAR(30) NOT NULL,
    `usage_stage` VARCHAR(50) NOT NULL,
    `usage_desc` VARCHAR(500) NOT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_case_tool_usage` (`case_id`, `tool_code`, `sort_order`),
    KEY `idx_case_tool_usage_case_sort` (`case_id`, `sort_order`),
    KEY `idx_case_tool_usage_tool` (`tool_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

UPDATE `workflow_template_node` n
JOIN `workflow_template` t ON t.id = n.template_id
JOIN `workflow_stage` s ON s.code = CASE
    WHEN t.code = 'landscape_site_analysis'
         AND n.node_code IN ('site_basic_analysis', 'site_issue_opportunity') THEN 'RESEARCH'
    ELSE 'CONCEPT'
END
SET n.stage_id = s.id
WHERE t.code IN ('building_concept_generation', 'landscape_site_analysis');

INSERT INTO `ai_tool`
(`name`, `code`, `official_url`, `description`, `status`, `is_deleted`)
VALUES
('腾讯元宝', 'TENCENT_YUANBAO', 'https://yuanbao.tencent.com/', '项目材料记录用于建筑效果图文生图尝试。', 1, 0),
('Tripo AI', 'TRIPO_AI', 'https://www.tripo3d.ai/', '项目材料记录用于三维模型生成与UV贴图。', 1, 0),
('Stable Diffusion', 'STABLE_DIFFUSION', NULL, '项目材料记录用于景观细节和图像生成。', 1, 0),
('LiblibAI', 'LIBLIB_AI', 'https://www.liblib.art/', '项目材料记录使用LoRA控制景观平面图、鸟瞰图和效果图。', 1, 0),
('Midjourney', 'MIDJOURNEY', 'https://www.midjourney.com/', '项目材料记录用于建筑展板排版参考探索。', 1, 0),
('Maket', 'MAKET_AI', 'https://www.maket.ai/', '项目材料记录用于建筑空间布局组合尝试。', 1, 0)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`official_url` = VALUES(`official_url`),
`description` = VALUES(`description`),
`status` = 1,
`is_deleted` = 0;

DELETE FROM `case_tool_usage`
WHERE `case_id` IN (
    SELECT `id` FROM `case_project`
    WHERE `code` IN (
        'CASE_RESEARCH_WATERFRONT_SITE',
        'CASE_RESEARCH_YOUTH_COMMUNITY',
        'CASE_CONCEPT_CITY_LIVING_ROOM',
        'CASE_CONCEPT_BRIDGE_COMPARE',
        'CASE_DEVELOPMENT_PLAN_FACADE',
        'CASE_DEVELOPMENT_ROOF_PLATFORM',
        'CASE_PRESENTATION_BOARD',
        'CASE_PRESENTATION_REPORT_COPY'
    )
);

DELETE FROM `case_asset`
WHERE `case_id` IN (
    SELECT `id` FROM `case_project`
    WHERE `code` IN (
        'CASE_RESEARCH_WATERFRONT_SITE',
        'CASE_RESEARCH_YOUTH_COMMUNITY',
        'CASE_CONCEPT_CITY_LIVING_ROOM',
        'CASE_CONCEPT_BRIDGE_COMPARE',
        'CASE_DEVELOPMENT_PLAN_FACADE',
        'CASE_DEVELOPMENT_ROOF_PLATFORM',
        'CASE_PRESENTATION_BOARD',
        'CASE_PRESENTATION_REPORT_COPY'
    )
);

DELETE FROM `case_project`
WHERE `code` IN (
    'CASE_RESEARCH_WATERFRONT_SITE',
    'CASE_RESEARCH_YOUTH_COMMUNITY',
    'CASE_CONCEPT_CITY_LIVING_ROOM',
    'CASE_CONCEPT_BRIDGE_COMPARE',
    'CASE_DEVELOPMENT_PLAN_FACADE',
    'CASE_DEVELOPMENT_ROOF_PLATFORM',
    'CASE_PRESENTATION_BOARD',
    'CASE_PRESENTATION_REPORT_COPY'
);

DELETE FROM `workflow_node_prompt_rel`
WHERE `prompt_id` IN (
    SELECT `id` FROM `prompt_template`
    WHERE `code` IN (
        'RESEARCH_SITE_ANALYSIS',
        'RESEARCH_CASE_SUMMARY',
        'RESEARCH_KEYWORD_EXPAND',
        'CONCEPT_IDEA_GENERATION',
        'CONCEPT_SCHEME_COMPARE',
        'CONCEPT_IMAGE_PROMPT_OPTIMIZE',
        'DEVELOPMENT_PLAN_OPTIMIZE',
        'DEVELOPMENT_FACADE_OPTIMIZE',
        'DEVELOPMENT_SECTION_NARRATIVE',
        'PRESENTATION_REPORT_COPY',
        'PRESENTATION_BOARD_TITLE',
        'PRESENTATION_SUMMARY_COMPRESS'
    )
);

DELETE FROM `prompt_parameter`
WHERE `prompt_id` IN (
    SELECT `id` FROM `prompt_template`
    WHERE `code` IN (
        'RESEARCH_SITE_ANALYSIS',
        'RESEARCH_CASE_SUMMARY',
        'RESEARCH_KEYWORD_EXPAND',
        'CONCEPT_IDEA_GENERATION',
        'CONCEPT_SCHEME_COMPARE',
        'CONCEPT_IMAGE_PROMPT_OPTIMIZE',
        'DEVELOPMENT_PLAN_OPTIMIZE',
        'DEVELOPMENT_FACADE_OPTIMIZE',
        'DEVELOPMENT_SECTION_NARRATIVE',
        'PRESENTATION_REPORT_COPY',
        'PRESENTATION_BOARD_TITLE',
        'PRESENTATION_SUMMARY_COMPRESS'
    )
);

DELETE FROM `prompt_tool_rel`
WHERE `prompt_id` IN (
    SELECT `id` FROM `prompt_template`
    WHERE `code` IN (
        'RESEARCH_SITE_ANALYSIS',
        'RESEARCH_CASE_SUMMARY',
        'RESEARCH_KEYWORD_EXPAND',
        'CONCEPT_IDEA_GENERATION',
        'CONCEPT_SCHEME_COMPARE',
        'CONCEPT_IMAGE_PROMPT_OPTIMIZE',
        'DEVELOPMENT_PLAN_OPTIMIZE',
        'DEVELOPMENT_FACADE_OPTIMIZE',
        'DEVELOPMENT_SECTION_NARRATIVE',
        'PRESENTATION_REPORT_COPY',
        'PRESENTATION_BOARD_TITLE',
        'PRESENTATION_SUMMARY_COMPRESS'
    )
);

DELETE FROM `prompt_template`
WHERE `code` IN (
    'RESEARCH_SITE_ANALYSIS',
    'RESEARCH_CASE_SUMMARY',
    'RESEARCH_KEYWORD_EXPAND',
    'CONCEPT_IDEA_GENERATION',
    'CONCEPT_SCHEME_COMPARE',
    'CONCEPT_IMAGE_PROMPT_OPTIMIZE',
    'DEVELOPMENT_PLAN_OPTIMIZE',
    'DEVELOPMENT_FACADE_OPTIMIZE',
    'DEVELOPMENT_SECTION_NARRATIVE',
    'PRESENTATION_REPORT_COPY',
    'PRESENTATION_BOARD_TITLE',
    'PRESENTATION_SUMMARY_COMPRESS'
);

INSERT INTO `case_project`
(`title`, `code`, `stage_id`, `tool_id`, `cover_url`, `summary`, `content`,
 `source_desc`, `author_name`, `sort_order`, `status`, `audit_status`, `is_deleted`)
SELECT
    '智驿：奥林匹克森林公园环岛改造游客中心',
    'REAL_VISITOR_CENTER_ZHIYI',
    s.id,
    t.id,
    '/assets/cases/visitor-center/cover.jpg',
    '基于奥林匹克森林公园南门环岛场地调研，结合生成式AI完成游客中心的分析、布局、图纸、模型和展板表达。',
    '项目从实地测量、区位与人群分析出发，使用DeepSeek整理调研数据并生成可视化图表；通过Maket和Hypar辅助空间布局与图纸产出，结合Stable Diffusion、LiblibAI、腾讯元宝和Tripo AI完成景观、效果图与模型探索，最终形成“自然融合与生态共生、通透开放与空间互动、现代简约与地域特色、功能复合与智慧服务、节能与可持续设计”的游客中心方案。',
    '来源：游客中心展板、游客中心生成过程.pdf、项目成果统计表。',
    '田婧辰',
    10,
    1,
    'APPROVED',
    0
FROM `workflow_stage` s
LEFT JOIN `ai_tool` t ON t.code = 'DEEPSEEK'
WHERE s.code = 'PRESENTATION'
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`), `stage_id` = VALUES(`stage_id`), `tool_id` = VALUES(`tool_id`),
`cover_url` = VALUES(`cover_url`), `summary` = VALUES(`summary`), `content` = VALUES(`content`),
`source_desc` = VALUES(`source_desc`), `author_name` = VALUES(`author_name`),
`sort_order` = VALUES(`sort_order`), `status` = 1, `audit_status` = 'APPROVED', `is_deleted` = 0;

INSERT INTO `case_project`
(`title`, `code`, `stage_id`, `tool_id`, `cover_url`, `summary`, `content`,
 `source_desc`, `author_name`, `sort_order`, `status`, `audit_status`, `is_deleted`)
SELECT
    '抬头能量站：面向低头族的模块化车站',
    'REAL_LOOK_UP_ENERGY_STATION',
    s.id,
    t.id,
    '/assets/cases/energy-station/cover.jpg',
    '围绕城市低头族问题，将公交站转化为兼具基础服务、互动体验和公益运营的模块化公共空间。',
    '项目以全人群需求、立体停车、模块化可行性和环境适配为调研框架，利用DeepSeek梳理政策、案例和技术参数。方案设置双人发电单车、声控谜语柜、彩虹伞架、手影充电墙和旋转拼图树等互动模块，并通过动态激励、智能场景适配和公益运营增强人与环境的连接。',
    '来源：未来设计师展板、车站展板生成过程.pdf、项目成果统计表。',
    '田婧辰',
    20,
    1,
    'APPROVED',
    0
FROM `workflow_stage` s
LEFT JOIN `ai_tool` t ON t.code = 'DEEPSEEK'
WHERE s.code = 'PRESENTATION'
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`), `stage_id` = VALUES(`stage_id`), `tool_id` = VALUES(`tool_id`),
`cover_url` = VALUES(`cover_url`), `summary` = VALUES(`summary`), `content` = VALUES(`content`),
`source_desc` = VALUES(`source_desc`), `author_name` = VALUES(`author_name`),
`sort_order` = VALUES(`sort_order`), `status` = 1, `audit_status` = 'APPROVED', `is_deleted` = 0;

INSERT INTO `case_project`
(`title`, `code`, `stage_id`, `tool_id`, `cover_url`, `summary`, `content`,
 `source_desc`, `author_name`, `sort_order`, `status`, `audit_status`, `is_deleted`)
SELECT
    '邻聚驿：AIGC生成式模块化居民服务中心',
    'REAL_NEIGHBORHOOD_SERVICE_CENTER',
    s.id,
    NULL,
    '/assets/cases/neighborhood-center/cover.jpg',
    '连接两栋居民楼的双层社区驿站，回应老人、儿童和青年群体的日常服务与交流需求。',
    '“邻聚驿”以约100平方米双层实用型社区驿站为原型，设置室内花卉种植、物品置换、纠纷调解、儿童托管和屋顶花园等功能。方案利用屋顶花园实现童宠分离和人群分流，通过清晰的功能布局与流线组织形成面向社区居民的共享空间。',
    '来源：展板一、展板二、社区服务中心系列展板、项目成果统计表。现有材料未提供可核验的完整AI工具过程。',
    '田婧辰',
    30,
    1,
    'APPROVED',
    0
FROM `workflow_stage` s
WHERE s.code = 'PRESENTATION'
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`), `stage_id` = VALUES(`stage_id`), `tool_id` = NULL,
`cover_url` = VALUES(`cover_url`), `summary` = VALUES(`summary`), `content` = VALUES(`content`),
`source_desc` = VALUES(`source_desc`), `author_name` = VALUES(`author_name`),
`sort_order` = VALUES(`sort_order`), `status` = 1, `audit_status` = 'APPROVED', `is_deleted` = 0;

INSERT INTO `case_project`
(`title`, `code`, `stage_id`, `tool_id`, `cover_url`, `summary`, `content`,
 `source_desc`, `author_name`, `sort_order`, `status`, `audit_status`, `is_deleted`)
SELECT
    '无界视界：示范性微花园设计',
    'REAL_BOUNDLESS_GARDEN',
    s.id,
    NULL,
    '/assets/cases/boundless-garden/cover.jpg',
    '以全感官包容性生态美学为核心，融合疗愈花园、智慧技术和生物友好设计的示范性微花园。',
    '项目通过色彩识别、芳香与触觉植物、景观小品和无障碍游线构建可观、可感、可触的疗愈花园，同时兼顾生物多样性、低碳生活与循环理念。作品“无界视界”获得“青创北京”2025年“挑战杯”首都大学生课外学术科技作品竞赛省级一等奖。',
    '来源：微花园展板、微花园获奖证书、成果统计表及竞赛获奖名单。',
    '田婧辰',
    40,
    1,
    'APPROVED',
    0
FROM `workflow_stage` s
WHERE s.code = 'PRESENTATION'
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`), `stage_id` = VALUES(`stage_id`), `tool_id` = NULL,
`cover_url` = VALUES(`cover_url`), `summary` = VALUES(`summary`), `content` = VALUES(`content`),
`source_desc` = VALUES(`source_desc`), `author_name` = VALUES(`author_name`),
`sort_order` = VALUES(`sort_order`), `status` = 1, `audit_status` = 'APPROVED', `is_deleted` = 0;

INSERT INTO `case_asset`
(`case_id`, `asset_type`, `asset_url`, `title`, `description`, `sort_order`, `is_deleted`)
SELECT id, 'image', '/assets/cases/visitor-center/board.jpg', '智驿完整展板', '游客中心最终设计展板。', 10, 0
FROM `case_project` WHERE code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT id, 'file', '/assets/cases/visitor-center/process.pdf', '游客中心生成过程', '记录调研、布局、出图、模型和展板生成过程。', 20, 0
FROM `case_project` WHERE code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT id, 'image', '/assets/cases/energy-station/board.jpg', '抬头能量站完整展板', '模块化车站最终设计展板。', 10, 0
FROM `case_project` WHERE code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT id, 'file', '/assets/cases/energy-station/process.pdf', '车站展板生成过程', '记录调研、概念、图纸、模型和排版过程。', 20, 0
FROM `case_project` WHERE code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT id, 'image', '/assets/cases/neighborhood-center/board-overview.jpg', '邻聚驿汇总展板', '社区服务中心上下两版汇总展示。', 10, 0
FROM `case_project` WHERE code = 'REAL_NEIGHBORHOOD_SERVICE_CENTER'
UNION ALL
SELECT id, 'image', '/assets/cases/neighborhood-center/board-detail.jpg', '邻聚驿功能与活动展板', '材料、功能、平面和社区活动策划。', 20, 0
FROM `case_project` WHERE code = 'REAL_NEIGHBORHOOD_SERVICE_CENTER'
UNION ALL
SELECT id, 'image', '/assets/cases/boundless-garden/board.jpg', '无界视界完整展板', '微花园总平面、植物配置、分析和效果图。', 10, 0
FROM `case_project` WHERE code = 'REAL_BOUNDLESS_GARDEN'
UNION ALL
SELECT id, 'image', '/assets/cases/boundless-garden/award.jpg', '挑战杯获奖证书', '省级一等奖支撑材料。', 20, 0
FROM `case_project` WHERE code = 'REAL_BOUNDLESS_GARDEN';

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '游客中心景观总平面生成指令', 'ORIGINAL_VISITOR_SITE_PLAN', '图像生成',
'请帮我生成一个该建筑的总平面，该建筑是游客中心，在湖岸边上，生成包含建筑在正中其周围的景观，提供一份详细的PNG，添加具体现代化元素（如户外景观小品、广场等），一定是手绘风格平面俯视图，生成平面俯视图。',
'游客中心建筑功能、湖岸场地和景观组合要求。', '手绘风格景观总平面图。',
'项目原文仅记录该指令及后续筛选、Stable Diffusion和LiblibAI细化过程，不补写未保存的参数。',
'原始文件：游客中心生成过程.pdf，“景观平面/总平面生成过程”。', 10, 0, 1, 0
FROM `workflow_stage` s WHERE s.code = 'CONCEPT'
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`), `stage_id` = VALUES(`stage_id`), `category` = VALUES(`category`),
`content` = VALUES(`content`), `input_desc` = VALUES(`input_desc`), `output_desc` = VALUES(`output_desc`),
`tips` = VALUES(`tips`), `source_desc` = VALUES(`source_desc`), `status` = 1, `is_deleted` = 0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '建筑设计展板HTML排版指令', 'ORIGINAL_VISITOR_BOARD_HTML', '展板排版',
'请给我以HTML的形式生成一系列风格类型不同的建筑设计展板排版。其中要包括：效果图、总平面图、平面图、立面图、剖面图、形态推演图、设计说明、元素提取等板块。',
'展板板块构成和期望的版式风格。', '可转换为图片的HTML展板排版方案。',
'原项目使用DeepSeek生成HTML，并通过多轮调整和参考图筛选版式。',
'原始文件：游客中心生成过程.pdf，“展板生成过程”。', 20, 0, 1, 0
FROM `workflow_stage` s WHERE s.code = 'PRESENTATION'
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`), `stage_id` = VALUES(`stage_id`), `category` = VALUES(`category`),
`content` = VALUES(`content`), `input_desc` = VALUES(`input_desc`), `output_desc` = VALUES(`output_desc`),
`tips` = VALUES(`tips`), `source_desc` = VALUES(`source_desc`), `status` = 1, `is_deleted` = 0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '智驿效果图关键词', 'ORIGINAL_VISITOR_RENDER_KEYWORDS', '效果图关键词',
'公园，游客，现代化，绿色低碳，人行流线，便利，公共设施，城市公园，长方体建筑，真实',
'模块化游客中心，以门厅为中心的环绕式结构。', '游客中心效果图生成关键词。',
'这是过程文档保存的关键词串，不扩写为未被记录的完整Prompt。',
'原始文件：游客中心生成过程.pdf，“创意说明/提示词”。', 30, 0, 1, 0
FROM `workflow_stage` s WHERE s.code = 'CONCEPT'
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`), `stage_id` = VALUES(`stage_id`), `category` = VALUES(`category`),
`content` = VALUES(`content`), `input_desc` = VALUES(`input_desc`), `output_desc` = VALUES(`output_desc`),
`tips` = VALUES(`tips`), `source_desc` = VALUES(`source_desc`), `status` = 1, `is_deleted` = 0;

INSERT INTO `prompt_template`
(`stage_id`, `title`, `code`, `category`, `content`, `input_desc`, `output_desc`, `tips`,
 `source_desc`, `sort_order`, `copy_count`, `status`, `is_deleted`)
SELECT s.id, '抬头能量站效果图关键词', 'ORIGINAL_STATION_RENDER_KEYWORDS', '效果图关键词',
'车站，私密性，现实风格，现代科技，LED屏幕，智能，模块化，组合，城市，小尺度，繁华，城市',
'符合新时代需求的模块化、智能化车站，兼具功能性和艺术性的双重墙面搭配。', '模块化车站效果图生成关键词。',
'这是过程文档保存的关键词串，不扩写为未被记录的完整Prompt。',
'原始文件：车站展板生成过程.pdf，“创意说明/提示词”。', 40, 0, 1, 0
FROM `workflow_stage` s WHERE s.code = 'CONCEPT'
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`), `stage_id` = VALUES(`stage_id`), `category` = VALUES(`category`),
`content` = VALUES(`content`), `input_desc` = VALUES(`input_desc`), `output_desc` = VALUES(`output_desc`),
`tips` = VALUES(`tips`), `source_desc` = VALUES(`source_desc`), `status` = 1, `is_deleted` = 0;

INSERT IGNORE INTO `prompt_tool_rel` (`prompt_id`, `tool_id`)
SELECT p.id, t.id
FROM `prompt_template` p
JOIN `ai_tool` t ON t.code IN ('STABLE_DIFFUSION', 'LIBLIB_AI')
WHERE p.code = 'ORIGINAL_VISITOR_SITE_PLAN';

INSERT IGNORE INTO `prompt_tool_rel` (`prompt_id`, `tool_id`)
SELECT p.id, t.id
FROM `prompt_template` p
JOIN `ai_tool` t ON t.code = 'DEEPSEEK'
WHERE p.code = 'ORIGINAL_VISITOR_BOARD_HTML';

INSERT IGNORE INTO `prompt_tool_rel` (`prompt_id`, `tool_id`)
SELECT p.id, t.id
FROM `prompt_template` p
JOIN `ai_tool` t ON t.code IN ('KIMI', 'TENCENT_YUANBAO')
WHERE p.code IN ('ORIGINAL_VISITOR_RENDER_KEYWORDS', 'ORIGINAL_STATION_RENDER_KEYWORDS');

INSERT INTO `case_tool_usage`
(`case_id`, `tool_id`, `tool_name`, `tool_code`, `tool_type`, `usage_stage`, `usage_desc`, `sort_order`)
SELECT c.id, t.id, 'DeepSeek', 'DEEPSEEK', 'AI', '前期调研', '整理区位、人群与问卷数据，并生成HTML可视化图表。', 10
FROM case_project c JOIN ai_tool t ON t.code = 'DEEPSEEK' WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, t.id, 'Maket', 'MAKET_AI', 'AI', '平面布局', '尝试初步空间布局组合并与立面预期双向优化。', 20
FROM case_project c JOIN ai_tool t ON t.code = 'MAKET_AI' WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, NULL, 'Hypar', 'HYPAR', 'DESIGN', '图纸产出', '进行家具摆放并作为CAD图纸生成基础。', 30
FROM case_project c WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, NULL, 'CAD', 'CAD', 'DRAWING', '图纸产出', '承接平面布局并形成建筑图纸。', 40
FROM case_project c WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, t.id, 'Stable Diffusion', 'STABLE_DIFFUSION', 'AI', '景观生成', '尝试生成景观细节。', 50
FROM case_project c JOIN ai_tool t ON t.code = 'STABLE_DIFFUSION' WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, t.id, 'LiblibAI', 'LIBLIB_AI', 'AI', '景观生成', '使用景观LoRA控制线稿并调整渲染风格。', 60
FROM case_project c JOIN ai_tool t ON t.code = 'LIBLIB_AI' WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, t.id, 'Kimi', 'KIMI', 'AI', '效果图提示词', '根据游客中心基础要求筛选并生成效果图提示词。', 70
FROM case_project c JOIN ai_tool t ON t.code = 'KIMI' WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, t.id, '腾讯元宝', 'TENCENT_YUANBAO', 'AI', '效果图生成', '尝试文生图并多轮调整建筑立面。', 80
FROM case_project c JOIN ai_tool t ON t.code = 'TENCENT_YUANBAO' WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, t.id, 'Tripo AI', 'TRIPO_AI', 'AI', '模型生成', '生成三维模型并完成UV贴图。', 90
FROM case_project c JOIN ai_tool t ON t.code = 'TRIPO_AI' WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, NULL, '知末', 'ZHIMO', 'POST_PROCESSING', '渲染出图', '承接模型视角并进行后续渲染。', 100
FROM case_project c WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, NULL, '建筑学长', 'JIANZHU_XUEZHANG', 'POST_PROCESSING', '图纸与效果图', '进行图纸初步渲染和场景尺度调整。', 110
FROM case_project c WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, NULL, 'Photoshop', 'PHOTOSHOP', 'POST_PROCESSING', '成果表达', '精细化图纸和效果图表现。', 120
FROM case_project c WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, t.id, 'Midjourney', 'MIDJOURNEY', 'AI', '展板排版', '结合参考图尝试不同展板版式。', 130
FROM case_project c JOIN ai_tool t ON t.code = 'MIDJOURNEY' WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI'
UNION ALL
SELECT c.id, NULL, 'Convertio', 'CONVERTIO', 'POST_PROCESSING', '文件转换', '将HTML等中间成果转换为可用图像或文件。', 140
FROM case_project c WHERE c.code = 'REAL_VISITOR_CENTER_ZHIYI';

INSERT INTO `case_tool_usage`
(`case_id`, `tool_id`, `tool_name`, `tool_code`, `tool_type`, `usage_stage`, `usage_desc`, `sort_order`)
SELECT c.id, t.id, 'DeepSeek', 'DEEPSEEK', 'AI', '调研与概念', '梳理政策、案例、技术参数和低头族设计概念。', 10
FROM case_project c JOIN ai_tool t ON t.code = 'DEEPSEEK' WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, NULL, 'Godel', 'GODEL', 'DESIGN', '数据验证', '验证立体车库空间效率公式。', 20
FROM case_project c WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, NULL, 'Hypar', 'HYPAR', 'DESIGN', '平面布局', '进行家具摆放并作为CAD图纸基础。', 30
FROM case_project c WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, NULL, 'CAD', 'CAD', 'DRAWING', '图纸产出', '形成建筑平面与立面图纸。', 40
FROM case_project c WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, t.id, 'Kimi', 'KIMI', 'AI', '效果图提示词', '根据车站基础要求筛选并生成提示词。', 50
FROM case_project c JOIN ai_tool t ON t.code = 'KIMI' WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, t.id, '腾讯元宝', 'TENCENT_YUANBAO', 'AI', '效果图生成', '尝试文生图并多轮调整建筑立面。', 60
FROM case_project c JOIN ai_tool t ON t.code = 'TENCENT_YUANBAO' WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, t.id, 'Tripo AI', 'TRIPO_AI', 'AI', '模型生成', '生成三维模型并完成UV贴图。', 70
FROM case_project c JOIN ai_tool t ON t.code = 'TRIPO_AI' WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, NULL, '知末', 'ZHIMO', 'POST_PROCESSING', '渲染出图', '承接模型视角并进行后续渲染。', 80
FROM case_project c WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, NULL, '建筑学长', 'JIANZHU_XUEZHANG', 'POST_PROCESSING', '图纸与效果图', '进行图纸初步渲染和场景尺度调整。', 90
FROM case_project c WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, NULL, 'Photoshop', 'PHOTOSHOP', 'POST_PROCESSING', '成果表达', '微调效果图并精细化图纸表现。', 100
FROM case_project c WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION'
UNION ALL
SELECT c.id, NULL, 'Convertio', 'CONVERTIO', 'POST_PROCESSING', '文件转换', '转换HTML和图像中间成果。', 110
FROM case_project c WHERE c.code = 'REAL_LOOK_UP_ENERGY_STATION';
