-- =====================================================
-- 客户表微信字段扩展
-- 创建时间: 2024-01-14
-- 版本: V1.0
-- =====================================================

USE yibang_erp_dev;

-- 为customers表添加微信相关字段
ALTER TABLE customers 
ADD COLUMN open_id VARCHAR(100) COMMENT '微信open_id',
ADD COLUMN union_id VARCHAR(100) COMMENT '微信union_id',
ADD COLUMN province VARCHAR(50) COMMENT '省份',
ADD COLUMN city VARCHAR(50) COMMENT '城市',
ADD COLUMN gender VARCHAR(10) COMMENT '性别',
ADD COLUMN country VARCHAR(50) COMMENT '国家';

-- 添加索引
ALTER TABLE customers 
ADD INDEX idx_open_id (open_id),
ADD INDEX idx_union_id (union_id),
ADD INDEX idx_province (province),
ADD INDEX idx_city (city);

-- 添加唯一约束（可选，根据业务需求）
-- ALTER TABLE customers ADD UNIQUE KEY uk_open_id (open_id);
-- ALTER TABLE customers ADD UNIQUE KEY uk_union_id (union_id);
