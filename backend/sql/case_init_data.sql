-- Case showcase module test data.
-- Execute after case_schema.sql, workflow_init_data.sql and ai_tool_init_data.sql.
-- These records are only for development/demo. They are not final case content.

USE `ai_design_platform`;

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
