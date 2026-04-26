-- Optional normal user bootstrap data.
-- Execute after sql/schema.sql.

USE `ai_design_platform`;

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
    '{noop}test123',
    '测试用户',
    NULL,
    'USER',
    1,
    0
) ON DUPLICATE KEY UPDATE
    `password_hash` = '{noop}test123',
    `nickname` = '测试用户',
    `role` = 'USER',
    `status` = 1,
    `is_deleted` = 0;
