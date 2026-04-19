-- Site home content module test data.
-- Execute after site_schema.sql.
-- These records are only for development/demo and are not final homepage copy.

USE `ai_design_platform`;

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
