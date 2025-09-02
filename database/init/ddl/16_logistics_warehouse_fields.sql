-- 为物流信息表添加仓库字段
-- 创建时间：2025-01-14

-- 为logistics_info表添加仓库相关字段
ALTER TABLE logistics_info 
ADD COLUMN warehouse_id BIGINT COMMENT '发货仓库ID' AFTER delivery_notes,
ADD COLUMN warehouse_name VARCHAR(100) COMMENT '发货仓库名称' AFTER warehouse_id;

-- 添加索引
ALTER TABLE logistics_info 
ADD INDEX idx_warehouse_id (warehouse_id);

-- 添加外键约束（可选，如果warehouses表存在）
-- ALTER TABLE logistics_info 
-- ADD CONSTRAINT fk_logistics_warehouse 
-- FOREIGN KEY (warehouse_id) REFERENCES warehouses(id);
