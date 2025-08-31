-- =====================================================
-- 商品价格分层配置表
-- 创建时间: 2025-08-29
-- 版本: V1.0
-- =====================================================

USE yibang_erp_dev;

-- 先插入基础商品数据（如果不存在）
INSERT IGNORE INTO products (id, sku, name, category_id, brand_id, company_id, description, short_description, unit, cost_price, selling_price, market_price, retail_limit_price, weight, dimensions, is_featured, is_hot, is_new, status, approval_status, created_by, updated_by) VALUES
(1, 'MI001', '小米智能手环', 5, 3, 1, '小米智能手环，支持心率监测、运动追踪等功能', '智能手环', '个', 60.00, 89.00, 120.00, 120.00, 0.05, '{"length": 40, "width": 20, "height": 10}', 1, 1, 1, 'ACTIVE', 'APPROVED', 1, 1),
(2, 'AP001', 'iPhone 15 Pro', 5, 1, 1, '苹果iPhone 15 Pro，最新旗舰手机', 'iPhone 15 Pro', '台', 450.00, 588.00, 800.00, 800.00, 0.18, '{"length": 150, "width": 75, "height": 8}', 1, 1, 1, 'ACTIVE', 'APPROVED', 1, 1);

-- 创建商品价格分层配置表
CREATE TABLE IF NOT EXISTS product_price_tier_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    price_tier_id BIGINT NOT NULL COMMENT '价格分层ID',
    dropshipping_price DECIMAL(10,2) NOT NULL COMMENT '一件代发价格',
    retail_limit_price DECIMAL(10,2) NOT NULL COMMENT '零售限价',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (price_tier_id) REFERENCES price_tiers(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE KEY uk_product_price_tier (product_id, price_tier_id),
    INDEX idx_product_id (product_id),
    INDEX idx_price_tier_id (price_tier_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品价格分层配置表';

-- 插入示例数据
INSERT INTO product_price_tier_configs (product_id, price_tier_id, dropshipping_price, retail_limit_price, is_active, created_by) VALUES
(1, 1, 89.00, 120.00, 1, 1),
(1, 2, 95.00, 130.00, 1, 1),
(1, 3, 100.00, 140.00, 1, 1),
(1, 4, 105.00, 150.00, 1, 1),
(2, 1, 588.00, 800.00, 1, 1),
(2, 2, 620.00, 850.00, 1, 1),
(2, 3, 650.00, 900.00, 1, 1),
(2, 4, 680.00, 950.00, 1, 1);

-- 验证表结构
DESCRIBE product_price_tier_configs;

-- 验证数据
SELECT 
    pc.id,
    p.name as product_name,
    pt.tier_name,
    pt.tier_type,
    pc.dropshipping_price,
    pc.retail_limit_price,
    pc.is_active
FROM product_price_tier_configs pc
JOIN products p ON pc.product_id = p.id
JOIN price_tiers pt ON pc.price_tier_id = pt.id
WHERE pc.deleted = 0;
