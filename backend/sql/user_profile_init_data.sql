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
    '{bcrypt}$2b$12$exkzoQQH1NLV9NDe.UI5b.OTypMdSNAN.MvUQH2I3hc5Go334A3q2',
    '测试用户',
    NULL,
    'USER',
    1,
    0
) ON DUPLICATE KEY UPDATE
    `password_hash` = '{bcrypt}$2b$12$exkzoQQH1NLV9NDe.UI5b.OTypMdSNAN.MvUQH2I3hc5Go334A3q2',
    `nickname` = '测试用户',
    `role` = 'USER',
    `status` = 1,
    `is_deleted` = 0;
