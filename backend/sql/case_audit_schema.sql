-- Case audit schema extension.
-- Execute after sql/case_schema.sql.
--
-- Note:
-- Some MySQL versions do not support "ADD COLUMN IF NOT EXISTS".
-- If re-running this script reports duplicate column/index errors,
-- confirm these columns/indexes already exist and skip the ALTER/CREATE INDEX statements.

USE `ai_design_platform`;

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
