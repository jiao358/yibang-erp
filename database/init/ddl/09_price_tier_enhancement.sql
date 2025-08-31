-- =====================================================
-- 价格分层功能增强 - 补充缺失字段
-- 创建时间: 2025-08-29
-- 版本: V1.0
-- =====================================================

USE yibang_erp_dev;

-- 1. 修改价格分层表 (price_tiers) - 补充缺失字段
ALTER TABLE price_tiers 
ADD COLUMN tier_type VARCHAR(50) COMMENT '分层类型：DEALER_LEVEL_1, DEALER_LEVEL_2, VIP_CUSTOMER等' AFTER tier_code,
ADD COLUMN effective_start TIMESTAMP NULL COMMENT '生效开始时间' AFTER credit_limit,
ADD COLUMN effective_end TIMESTAMP NULL COMMENT '生效结束时间' AFTER effective_start;

-- 2. 修改用户表 (users) - 添加价格分层等级关联
ALTER TABLE users 
ADD COLUMN price_tier_id BIGINT COMMENT '价格分层等级ID' AFTER sales_type,
ADD FOREIGN KEY (price_tier_id) REFERENCES price_tiers(id);

-- 3. 修改商品表 (products) - 添加零售限价字段
ALTER TABLE products 
ADD COLUMN retail_limit_price DECIMAL(10,2) COMMENT '零售端限价' AFTER market_price;

-- 4. 为价格分层表添加索引
ALTER TABLE price_tiers 
ADD INDEX idx_tier_type (tier_type),
ADD INDEX idx_effective_time (effective_start, effective_end);

-- 5. 为用户表添加价格分层索引
ALTER TABLE users 
ADD INDEX idx_price_tier (price_tier_id);

-- 6. 为商品表添加零售限价索引
ALTER TABLE products 
ADD INDEX idx_retail_limit_price (retail_limit_price);

-- 7. 更新现有数据 - 为现有的价格分层添加默认的分层类型
UPDATE price_tiers 
SET tier_type = CASE 
    WHEN tier_level = 1 THEN 'DEALER_LEVEL_1'
    WHEN tier_level = 2 THEN 'DEALER_LEVEL_2'
    WHEN tier_level = 3 THEN 'DEALER_LEVEL_3'
    WHEN customer_level_requirement = 'VIP' THEN 'VIP_CUSTOMER'
    WHEN customer_level_requirement = 'PREMIUM' THEN 'VIP_CUSTOMER'
    ELSE 'RETAIL'
END
WHERE tier_type IS NULL;

-- 8. 更新现有数据 - 为现有的价格分层设置默认的生效时间
UPDATE price_tiers 
SET effective_start = created_at,
    effective_end = DATE_ADD(created_at, INTERVAL 10 YEAR)
WHERE effective_start IS NULL;

-- 9. 为现有商品设置默认的零售限价（基于销售价的1.2倍）
UPDATE products 
SET retail_limit_price = ROUND(selling_price * 1.2, 2)
WHERE retail_limit_price IS NULL AND selling_price > 0;

-- 10. 创建价格分层类型枚举约束（通过触发器实现）
DELIMITER $$

CREATE TRIGGER tr_price_tiers_tier_type_check 
BEFORE INSERT ON price_tiers
FOR EACH ROW
BEGIN
    IF NEW.tier_type NOT IN ('DEALER_LEVEL_1', 'DEALER_LEVEL_2', 'DEALER_LEVEL_3', 'VIP_CUSTOMER', 'WHOLESALE', 'RETAIL') THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Invalid tier_type value. Allowed values: DEALER_LEVEL_1, DEALER_LEVEL_2, DEALER_LEVEL_3, VIP_CUSTOMER, WHOLESALE, RETAIL';
    END IF;
END$$

CREATE TRIGGER tr_price_tiers_tier_type_check_update 
BEFORE UPDATE ON price_tiers
FOR EACH ROW
BEGIN
    IF NEW.tier_type NOT IN ('DEALER_LEVEL_1', 'DEALER_LEVEL_2', 'DEALER_LEVEL_3', 'VIP_CUSTOMER', 'WHOLESALE', 'RETAIL') THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Invalid tier_type value. Allowed values: DEALER_LEVEL_1, DEALER_LEVEL_2, DEALER_LEVEL_3, VIP_CUSTOMER, WHOLESALE, RETAIL';
    END IF;
END$$

DELIMITER ;

-- 11. 验证修改结果
SELECT 
    'price_tiers' as table_name,
    COUNT(*) as total_records,
    COUNT(tier_type) as records_with_tier_type,
    COUNT(effective_start) as records_with_effective_start,
    COUNT(effective_end) as records_with_effective_end
FROM price_tiers
UNION ALL
SELECT 
    'users' as table_name,
    COUNT(*) as total_records,
    COUNT(price_tier_id) as records_with_price_tier,
    NULL as records_with_effective_start,
    NULL as records_with_effective_end
FROM users
UNION ALL
SELECT 
    'products' as table_name,
    COUNT(*) as total_records,
    COUNT(retail_limit_price) as records_with_retail_limit,
    NULL as records_with_effective_start,
    NULL as records_with_effective_end
FROM products;

-- 12. 显示新增字段的详细信息
DESCRIBE price_tiers;
DESCRIBE users;
DESCRIBE products;
