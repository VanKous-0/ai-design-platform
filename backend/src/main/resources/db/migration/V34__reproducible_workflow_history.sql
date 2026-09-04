-- Add only the immutable context required to reproduce a legacy workflow iteration.

ALTER TABLE `workflow_step_iteration`
    ADD COLUMN `prompt_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Logical prompt identity when a library prompt was used' AFTER `tool_id`,
    ADD COLUMN `prompt_revision_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Exact immutable prompt revision used' AFTER `prompt_id`,
    ADD COLUMN `profile_context_snapshot` JSON DEFAULT NULL COMMENT 'Immutable user preference context used for this iteration' AFTER `prompt_content`,
    ADD KEY `idx_workflow_step_iteration_prompt_revision` (`prompt_id`, `prompt_revision_id`),
    ADD CONSTRAINT `chk_workflow_step_iteration_prompt_reference`
        CHECK ((`prompt_id` IS NULL AND `prompt_revision_id` IS NULL)
            OR (`prompt_id` IS NOT NULL AND `prompt_revision_id` IS NOT NULL)),
    ADD CONSTRAINT `chk_workflow_step_iteration_profile_snapshot`
        CHECK (`profile_context_snapshot` IS NULL OR JSON_TYPE(`profile_context_snapshot`) = 'OBJECT'),
    ADD CONSTRAINT `fk_workflow_step_iteration_prompt_revision`
        FOREIGN KEY (`prompt_id`, `prompt_revision_id`)
        REFERENCES `prompt_revision` (`prompt_id`, `id`) ON DELETE RESTRICT;
