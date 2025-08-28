-- =====================================================
-- 商品管理相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.3
-- =====================================================

USE yibang_erp_dev;

-- 1. 商品分类表 (categories)
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT NULL COMMENT '父分类ID',
    level INT NOT NULL DEFAULT 1 COMMENT '分类层级：1-一级分类，2-二级分类，3-三级分类',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    description TEXT COMMENT '分类描述',
    icon VARCHAR(255) COMMENT '分类图标',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (parent_id) REFERENCES categories(id),
    INDEX idx_category_parent (parent_id),
    INDEX idx_category_level (level),
    INDEX idx_category_status (status),
    INDEX idx_category_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 2. 商品品牌表 (brands)
CREATE TABLE brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '品牌ID',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '品牌名称',
    logo VARCHAR(255) COMMENT '品牌Logo',
    description TEXT COMMENT '品牌描述',
    website VARCHAR(255) COMMENT '品牌官网',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_brand_name (name),
    INDEX idx_brand_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品品牌表';

-- 3. 商品表 (products)
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    sku VARCHAR(100) NOT NULL UNIQUE COMMENT '商品编码',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    brand_id BIGINT COMMENT '品牌ID',
    description TEXT COMMENT '商品描述',
    short_description VARCHAR(500) COMMENT '商品简介',
    images JSON COMMENT '商品图片JSON数组',
    specifications JSON COMMENT '商品规格JSON',
    unit VARCHAR(20) NOT NULL COMMENT '单位',
    cost_price DECIMAL(10,2) NOT NULL COMMENT '成本价',
    selling_price DECIMAL(10,2) NOT NULL COMMENT '销售价',
    market_price DECIMAL(10,2) COMMENT '市场价',
    weight DECIMAL(8,3) COMMENT '重量(kg)',
    dimensions JSON COMMENT '尺寸信息JSON',
    barcode VARCHAR(100) COMMENT '条形码',
    hs_code VARCHAR(20) COMMENT '海关编码',
    origin_country VARCHAR(50) COMMENT '原产国',
    material VARCHAR(200) COMMENT '材质',
    color VARCHAR(50) COMMENT '颜色',
    size VARCHAR(50) COMMENT '尺寸',
    tags JSON COMMENT '商品标签JSON数组',
    status ENUM('DRAFT', 'PENDING', 'ACTIVE', 'INACTIVE', 'DISCONTINUED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审核，ACTIVE-已上架，INACTIVE-已下架，DISCONTINUED-已停售',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
    approval_comment TEXT COMMENT '审核意见',
    approval_at TIMESTAMP NULL COMMENT '审核时间',
    approval_by BIGINT COMMENT '审核人ID',
    company_id BIGINT NOT NULL COMMENT '所属公司ID',
    is_featured TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
    is_hot TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否热销：0-否，1-是',
    is_new TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否新品：0-否，1-是',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (brand_id) REFERENCES brands(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (approval_by) REFERENCES users(id),
    INDEX idx_product_sku (sku),
    INDEX idx_product_name (name),
    INDEX idx_product_category (category_id),
    INDEX idx_product_brand (brand_id),
    INDEX idx_product_company (company_id),
    INDEX idx_product_status (status),
    INDEX idx_product_approval (approval_status),
    INDEX idx_product_featured (is_featured),
    INDEX idx_product_hot (is_hot),
    INDEX idx_product_new (is_new)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 4. 商品库存表 (product_inventory)
CREATE TABLE product_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '库存ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    available_quantity INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    reserved_quantity INT NOT NULL DEFAULT 0 COMMENT '预留库存',
    damaged_quantity INT NOT NULL DEFAULT 0 COMMENT '损坏库存',
    min_stock_level INT NOT NULL DEFAULT 0 COMMENT '最低库存预警线',
    max_stock_level INT COMMENT '最高库存上限',
    reorder_point INT COMMENT '补货点',
    last_stock_in TIMESTAMP NULL COMMENT '最后入库时间',
    last_stock_out TIMESTAMP NULL COMMENT '最后出库时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_product_warehouse (product_id, warehouse_id),
    INDEX idx_product_id (product_id),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_available_quantity (available_quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品库存表';

-- 5. 商品价格表 (product_prices)
CREATE TABLE product_prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '价格ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    price_type ENUM('BASE', 'SALES', 'WHOLESALE', 'SPECIAL') NOT NULL DEFAULT 'BASE' COMMENT '价格类型：BASE-基础价，SALES-销售价，WHOLESALE-批发价，SPECIAL-特价',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    currency VARCHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '货币代码',
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    effective_to TIMESTAMP NULL COMMENT '失效时间',
    min_quantity INT COMMENT '最小数量（用于阶梯定价）',
    max_quantity INT COMMENT '最大数量（用于阶梯定价）',
    customer_group VARCHAR(50) COMMENT '客户群体',
    company_id BIGINT COMMENT '适用公司ID（NULL表示所有公司）',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    INDEX idx_product_price (product_id, price_type),
    INDEX idx_effective_time (effective_from, effective_to),
    INDEX idx_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品价格表';

-- 6. 商品审核记录表 (product_approval_logs)
CREATE TABLE product_approval_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审核记录ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    action ENUM('SUBMIT', 'APPROVE', 'REJECT', 'MODIFY') NOT NULL COMMENT '操作类型：SUBMIT-提交审核，APPROVE-通过，REJECT-拒绝，MODIFY-修改',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL COMMENT '审核状态',
    comment TEXT COMMENT '审核意见',
    approver_id BIGINT COMMENT '审核人ID',
    approval_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (approver_id) REFERENCES users(id),
    INDEX idx_product_id (product_id),
    INDEX idx_approver_id (approver_id),
    INDEX idx_approval_time (approval_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品审核记录表';

-- 插入基础分类数据
INSERT INTO categories (name, parent_id, level, sort_order, description) VALUES
('电子产品', NULL, 1, 1, '各类电子产品'),
('服装鞋帽', NULL, 1, 2, '服装鞋帽类商品'),
('食品饮料', NULL, 1, 3, '食品饮料类商品'),
('家居用品', NULL, 1, 4, '家居用品类商品'),
('手机数码', 1, 2, 1, '手机数码产品'),
('电脑办公', 1, 2, 2, '电脑办公产品'),
('男装', 2, 2, 1, '男士服装'),
('女装', 2, 2, 2, '女士服装');

-- 插入基础品牌数据
INSERT INTO brands (name, description, status) VALUES
('苹果', 'Apple Inc.', 'ACTIVE'),
('华为', '华为技术有限公司', 'ACTIVE'),
('小米', '小米科技有限责任公司', 'ACTIVE'),
('耐克', 'Nike Inc.', 'ACTIVE'),
('阿迪达斯', 'Adidas AG', 'ACTIVE');
