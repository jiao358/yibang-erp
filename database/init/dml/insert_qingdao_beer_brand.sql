-- =====================================================
-- 插入青岛啤酒品牌数据
-- 创建时间: 2025-08-30
-- 版本: V1.0
-- =====================================================

USE yibang_erp_dev;

-- 插入青岛啤酒品牌
INSERT INTO brands (
    name, 
    logo, 
    description, 
    website, 
    status, 
    created_by
) VALUES (
    '青岛啤酒', 
    'https://example.com/logos/qingdao-beer.png', 
    '青岛啤酒是中国著名的啤酒品牌，创立于1903年，以其独特的酿造工艺和优质的口感闻名于世。青岛啤酒采用优质大麦、啤酒花和崂山泉水酿造，具有清爽、醇厚的特点。', 
    'https://www.tsingtao.com.cn', 
    'ACTIVE', 
    1
);

-- 验证插入结果
SELECT 
    id,
    name,
    description,
    status,
    created_at
FROM brands 
WHERE name = '青岛啤酒';

-- 显示所有品牌
SELECT 
    id,
    name,
    status,
    created_at
FROM brands 
ORDER BY created_at DESC;
