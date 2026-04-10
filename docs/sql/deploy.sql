-- =====================================================
-- 签名验签API系统 - 数据库部署脚本
-- 数据库: MySQL 8.0+
-- 创建时间: 2026-04-10
-- 说明: 系统初始化和表结构创建
-- =====================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `signature_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `signature_db`;

-- 2. 创建签名记录表
DROP TABLE IF EXISTS `signature_record`;
CREATE TABLE `signature_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `request_id` VARCHAR(64) NOT NULL COMMENT '请求ID,唯一标识',
    `signer` VARCHAR(128) NOT NULL COMMENT '签名方标识',
    `content` TEXT NOT NULL COMMENT '待签名的原始内容',
    `signature_value` VARCHAR(512) NOT NULL COMMENT '签名结果值',
    `algorithm` VARCHAR(32) DEFAULT 'HMAC-SHA256' COMMENT '签名算法:HMAC-SHA256,SHA-256',
    `timestamp` DATETIME NOT NULL COMMENT '签名时间',
    `expire_time` DATETIME COMMENT '过期时间',
    `verified_count` INT DEFAULT 0 COMMENT '验签次数统计',
    `status` VARCHAR(16) DEFAULT 'VALID' COMMENT '状态:VALID有效,EXPIRED过期,INVALID作废',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_request_id` (`request_id`),
    UNIQUE KEY `uk_signature_value` (`signature_value`),
    KEY `idx_signer` (`signer`),
    KEY `idx_timestamp` (`timestamp`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签名记录表';

-- 3. 创建索引优化脚本
-- ALTER TABLE `signature_record` ADD INDEX idx_signer_status (`signer`, `status`);

-- 4. 插入测试数据
INSERT INTO `signature_record` (`request_id`, `signer`, `content`, `signature_value`, `algorithm`, `timestamp`, `expire_time`, `verified_count`, `status`)
VALUES 
('test00120260410000000001', 'demo-client', 'orderId=DEMO001&amount=999.00', 'demo-signature-hash-placeholder', 'HMAC-SHA256', NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), 0, 'VALID');

-- 5. 创建用户表(可选扩展)
/*
DROP TABLE IF EXISTS `signature_client`;
CREATE TABLE `signature_client` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `client_id` VARCHAR(64) NOT NULL COMMENT '客户端ID',
    `client_secret` VARCHAR(256) NOT NULL COMMENT '客户端密钥',
    `client_name` VARCHAR(128) COMMENT '客户端名称',
    `status` TINYINT DEFAULT 1 COMMENT '状态:1启用,0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签名客户端表';
*/

-- =====================================================
-- 部署完成验证
-- =====================================================
SELECT '数据库部署完成!' AS message;
SELECT COUNT(*) AS table_count FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'signature_db';
