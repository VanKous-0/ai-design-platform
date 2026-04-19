-- Database: ai_design_platform
-- Execute this file after schema.sql.
-- Default admin account:
--   username: admin
--   password: admin123
--
-- The initial password uses Spring Security's {noop} encoder for local bootstrap.
-- After the project starts, replace it with a BCrypt value before real deployment.

INSERT INTO `sys_user` (
    `username`,
    `password_hash`,
    `nickname`,
    `avatar`,
    `role`,
    `status`,
    `is_deleted`
) VALUES (
    'admin',
    '{noop}admin123',
    '系统管理员',
    NULL,
    'ADMIN',
    1,
    0
) ON DUPLICATE KEY UPDATE
    `password_hash` = '{noop}admin123',
    `nickname` = '系统管理员',
    `role` = 'ADMIN',
    `status` = 1,
    `is_deleted` = 0;
