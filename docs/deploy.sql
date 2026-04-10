-- ========================================================
-- 签名和验签接口项目 - 数据库部署脚本
-- 版本: 1.0.0
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- ========================================================

-- --------------------------------------------------------
-- 1. 创建数据库
-- --------------------------------------------------------

-- 如果数据库已存在则删除（谨慎使用）
-- DROP DATABASE IF EXISTS signature_db;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS signature_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE signature_db;

-- --------------------------------------------------------
-- 2. 创建签名记录表
-- --------------------------------------------------------

DROP TABLE IF EXISTS `signature_record`;

CREATE TABLE `signature_record` (
    -- 主键
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 业务字段
    `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID，标识不同应用系统',
    `timestamp` VARCHAR(20) NOT NULL COMMENT '时间戳（毫秒），用于防重放攻击',
    `nonce` VARCHAR(32) NOT NULL COMMENT '随机字符串，用于防重放攻击',
    `data_content` TEXT COMMENT '业务数据内容（JSON格式）',
    `signature` VARCHAR(256) NOT NULL COMMENT 'SHA256签名结果（64位大写十六进制）',
    `sign_type` VARCHAR(10) DEFAULT 'SHA256' COMMENT '签名算法类型',

    -- 时间戳字段
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 主键约束
    PRIMARY KEY (`id`),

    -- 索引
    KEY `idx_signature` (`signature`) COMMENT '签名索引，用于快速查询',
    KEY `idx_app_timestamp_nonce` (`app_id`, `timestamp`, `nonce`) COMMENT '组合索引，用于防重放检查',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引，用于按时间查询'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签名记录表';

-- --------------------------------------------------------
-- 3. 插入测试数据（可选）
-- --------------------------------------------------------

-- 插入示例数据（用于开发和测试环境）
-- 生产环境请删除或注释掉以下 INSERT 语句

/*
INSERT INTO `signature_record` (`app_id`, `timestamp`, `nonce`, `data_content`, `signature`, `sign_type`)
VALUES
    ('APP001', '1704067200000', 'test001', '{"orderNo":"202401010001","amount":100.00}', 'A1B2C3D4E5F6789012345678901234567890ABCDEF1234567890ABCDEF123456', 'SHA256'),
    ('APP001', '1704067260000', 'test002', '{"orderNo":"202401010002","amount":200.00}', 'B2C3D4E5F6A1789012345678901234567890ABCDEF1234567890ABCDEF123451', 'SHA256'),
    ('APP002', '1704067320000', 'test003', '{"orderNo":"202401010003","amount":300.00}', 'C3D4E5F6A1B2789012345678901234567890ABCDEF1234567890ABCDEF123452', 'SHA256');
*/

-- --------------------------------------------------------
-- 4. 创建数据库用户（可选）
-- --------------------------------------------------------

-- 创建专用数据库用户（建议生产环境使用）
-- 请根据实际环境修改用户名和密码

/*
-- 创建用户
CREATE USER IF NOT EXISTS 'signature_user'@'%' IDENTIFIED BY 'YourStrongPassword123!';

-- 授予权限
GRANT SELECT, INSERT, UPDATE, DELETE ON signature_db.* TO 'signature_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
*/

-- --------------------------------------------------------
-- 5. 验证脚本
-- --------------------------------------------------------

-- 查看表结构
DESCRIBE `signature_record`;

-- 查看索引
SHOW INDEX FROM `signature_record`;

-- 查看表注释
SHOW TABLE STATUS WHERE Name = 'signature_record';

-- 查看当前数据库字符集
SHOW VARIABLES LIKE 'character_set_database';
SHOW VARIABLES LIKE 'collation_database';

-- --------------------------------------------------------
-- 6. 常用查询语句（参考）
-- --------------------------------------------------------

-- 按签名查询记录
-- SELECT * FROM `signature_record` WHERE `signature` = 'your-signature';

-- 按应用ID查询记录
-- SELECT * FROM `signature_record` WHERE `app_id` = 'APP001' ORDER BY `create_time` DESC LIMIT 10;

-- 查询最近24小时的记录
-- SELECT * FROM `signature_record` WHERE `create_time` >= DATE_SUB(NOW(), INTERVAL 24 HOUR);

-- 统计各应用的签名数量
-- SELECT `app_id`, COUNT(*) as count FROM `signature_record` GROUP BY `app_id`;

-- --------------------------------------------------------
-- 7. 维护脚本（参考）
-- --------------------------------------------------------

-- 清理过期数据（保留最近90天）
-- DELETE FROM `signature_record` WHERE `create_time` < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- 优化表（定期执行以回收空间）
-- OPTIMIZE TABLE `signature_record`;

-- 分析表（更新统计信息）
-- ANALYZE TABLE `signature_record`;

-- --------------------------------------------------------
-- 脚本执行完成
-- --------------------------------------------------------

SELECT '数据库部署脚本执行完成！' AS message;
SELECT CONCAT('数据库: ', DATABASE()) AS database_name;
SELECT CONCAT('表: signature_record 创建成功') AS table_status;
