-- Database: ai_design_platform
-- Execute this file first in DataGrip after selecting the ai_design_platform schema.

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码密文或 Spring Security PasswordEncoder 格式密码',
    `nickname` VARCHAR(50) NOT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER 普通用户，ADMIN 管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`),
    KEY `idx_sys_user_role` (`role`),
    KEY `idx_sys_user_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '系统用户表';
