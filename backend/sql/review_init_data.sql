-- Review module test data.
-- Execute after review_schema.sql, workflow_init_data.sql and ai_tool_init_data.sql.
-- These records are only for development/demo. They are not final review content.

USE `ai_design_platform`;

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
