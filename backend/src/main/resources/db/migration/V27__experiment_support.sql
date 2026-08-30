ALTER TABLE `sys_user`
    ADD COLUMN `experiment_code` VARCHAR(50) DEFAULT NULL COMMENT 'Anonymous experiment participant code' AFTER `status`,
    ADD COLUMN `experiment_group` VARCHAR(50) DEFAULT NULL COMMENT 'Experiment group' AFTER `experiment_code`,
    ADD COLUMN `experiment_batch` VARCHAR(50) DEFAULT NULL COMMENT 'Experiment batch' AFTER `experiment_group`,
    ADD UNIQUE KEY `uk_sys_user_experiment_code` (`experiment_code`),
    ADD KEY `idx_sys_user_experiment_batch_group` (`experiment_batch`, `experiment_group`);
