-- 创建数据库
CREATE DATABASE IF NOT EXISTS signature_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE signature_db;

-- 创建签名记录表
CREATE TABLE IF NOT EXISTS signature_record (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    signature_id VARCHAR(64) NOT NULL UNIQUE COMMENT '签名ID',
    original_data TEXT NOT NULL COMMENT '原始数据',
    signature_result VARCHAR(512) NOT NULL COMMENT '签名结果',
    algorithm VARCHAR(32) NOT NULL COMMENT '签名算法',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    expire_time DATETIME COMMENT '过期时间',
    status VARCHAR(16) COMMENT '状态',
    verification_count INT COMMENT '验证次数',
    last_verification_time DATETIME COMMENT '最后验证时间',
    INDEX idx_signature_id (signature_id),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签名记录表';
