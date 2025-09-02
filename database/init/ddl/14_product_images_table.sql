-- 商品图片表
-- 用于存储商品的图片信息

CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    image_name VARCHAR(255) NOT NULL COMMENT '图片文件名',
    image_url VARCHAR(500) NOT NULL COMMENT '图片访问URL',
    image_path VARCHAR(500) NOT NULL COMMENT '图片存储路径',
    image_size BIGINT COMMENT '图片文件大小（字节）',
    image_type VARCHAR(50) COMMENT '图片类型（jpg, png, gif等）',
    image_width INT COMMENT '图片宽度',
    image_height INT COMMENT '图片高度',
    is_primary BOOLEAN DEFAULT FALSE COMMENT '是否为主图',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    
    INDEX idx_product_id (product_id),
    INDEX idx_status (status),
    INDEX idx_is_primary (is_primary),
    INDEX idx_sort_order (sort_order),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- 添加商品表的外键约束（如果不存在）
-- ALTER TABLE products ADD CONSTRAINT fk_products_category_id FOREIGN KEY (category_id) REFERENCES categories(id);
-- ALTER TABLE products ADD CONSTRAINT fk_products_brand_id FOREIGN KEY (brand_id) REFERENCES brands(id);
