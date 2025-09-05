-- =====================================================
-- 价格分层管理相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.3
-- =====================================================

USE yibang_erp_dev;

-- 1. 价格分层表 (price_tiers)
CREATE TABLE price_tiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '价格分层ID',
    company_id BIGINT NOT NULL COMMENT '供应链公司ID',
    tier_name VARCHAR(100) NOT NULL COMMENT '分层名称',
    tier_code VARCHAR(50) NOT NULL COMMENT '分层代码',
    tier_type VARCHAR(50) NOT NULL COMMENT '分层类型：DEALER_LEVEL_1, DEALER_LEVEL_2, VIP_CUSTOMER等',
    tier_level INT NOT NULL COMMENT '分层级别（数字越小级别越高）',
    description TEXT COMMENT '分层描述',
    discount_rate DECIMAL(5,4) NOT NULL DEFAULT 1.00 COMMENT '折扣率（1.00表示无折扣）',
    markup_rate DECIMAL(5,4) NOT NULL DEFAULT 1.00 COMMENT '加价率（1.00表示无加价）',
    min_order_amount DECIMAL(12,2) COMMENT '最小订单金额',
    max_order_amount DECIMAL(12,2) COMMENT '最大订单金额',
    min_order_quantity INT COMMENT '最小订单数量',
    max_order_quantity INT COMMENT '最大订单数量',
    customer_level_requirement ENUM('REGULAR', 'VIP', 'PREMIUM', 'ALL') NOT NULL DEFAULT 'ALL' COMMENT '客户等级要求',
    payment_terms VARCHAR(100) COMMENT '付款条件',
    credit_limit DECIMAL(12,2) COMMENT '信用额度',
    effective_start TIMESTAMP NULL COMMENT '生效开始时间',
    effective_end TIMESTAMP NULL COMMENT '生效结束时间',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE KEY uk_company_tier_code (company_id, tier_code),
    INDEX idx_company_id (company_id),
    INDEX idx_tier_type (tier_type),
    INDEX idx_tier_level (tier_level),
    INDEX idx_is_active (is_active),
    INDEX idx_priority (priority),
    INDEX idx_effective_time (effective_start, effective_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格分层表';

-- 2. 价格策略表 (price_strategies)
CREATE TABLE price_strategies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '价格策略ID',
    company_id BIGINT NOT NULL COMMENT '供应链公司ID',
    strategy_name VARCHAR(100) NOT NULL COMMENT '策略名称',
    strategy_type ENUM('FIXED', 'PERCENTAGE', 'TIERED', 'DYNAMIC', 'SEASONAL') NOT NULL COMMENT '策略类型',
    description TEXT COMMENT '策略描述',
    base_price_type ENUM('COST', 'MARKET', 'CUSTOM') NOT NULL DEFAULT 'COST' COMMENT '基础价格类型',
    base_price_multiplier DECIMAL(5,4) NOT NULL DEFAULT 1.00 COMMENT '基础价格倍数',
    min_margin_rate DECIMAL(5,4) COMMENT '最小利润率',
    max_margin_rate DECIMAL(5,4) COMMENT '最大利润率',
    competitor_price_weight DECIMAL(3,2) COMMENT '竞争对手价格权重',
    market_demand_weight DECIMAL(3,2) COMMENT '市场需求权重',
    seasonality_factors TEXT COMMENT '季节性因素JSON',
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    effective_to TIMESTAMP NULL COMMENT '失效时间',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_company_id (company_id),
    INDEX idx_strategy_type (strategy_type),
    INDEX idx_effective_time (effective_from, effective_to),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格策略表';

-- 3. 商品价格分层表 (product_price_tiers)
CREATE TABLE product_price_tiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品价格分层ID',
    company_id BIGINT NOT NULL COMMENT '供应链公司ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    price_tier_id BIGINT NOT NULL COMMENT '价格分层ID',
    base_price DECIMAL(10,2) NOT NULL COMMENT '基础价格',
    tier_price DECIMAL(10,2) NOT NULL COMMENT '分层价格',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '折扣金额',
    discount_percentage DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '折扣百分比',
    min_quantity INT COMMENT '最小数量',
    max_quantity INT COMMENT '最大数量',
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    effective_to TIMESTAMP NULL COMMENT '失效时间',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (price_tier_id) REFERENCES price_tiers(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE KEY uk_product_tier (company_id, product_id, price_tier_id),
    INDEX idx_company_id (company_id),
    INDEX idx_product_id (product_id),
    INDEX idx_price_tier (price_tier_id),
    INDEX idx_effective_time (effective_from, effective_to),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品价格分层表';

-- 4. 销售目标表 (sales_targets)
CREATE TABLE sales_targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '销售目标ID',
    company_id BIGINT NOT NULL COMMENT '供应链公司ID',
    sales_company_id BIGINT NOT NULL COMMENT '销售公司ID',
    target_year INT NOT NULL COMMENT '目标年份',
    target_period ENUM('Q1', 'Q2', 'Q3', 'Q4', 'H1', 'H2', 'YEARLY') NOT NULL COMMENT '目标周期',
    period_start_date DATE NOT NULL COMMENT '周期开始日期',
    period_end_date DATE NOT NULL COMMENT '周期结束日期',
    gmv_target DECIMAL(15,2) NOT NULL COMMENT 'GMV目标',
    revenue_target DECIMAL(15,2) NOT NULL COMMENT '收入目标',
    order_count_target INT NOT NULL COMMENT '订单数量目标',
    customer_count_target INT NOT NULL COMMENT '客户数量目标',
    product_category_targets TEXT COMMENT '商品分类目标JSON',
    regional_targets TEXT COMMENT '地区目标JSON',
    commission_rate DECIMAL(5,4) COMMENT '佣金率',
    bonus_structure TEXT COMMENT '奖金结构JSON',
    kpi_metrics TEXT COMMENT 'KPI指标JSON',
    status ENUM('DRAFT', 'ACTIVE', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    progress_percentage DECIMAL(5,2) DEFAULT 0 COMMENT '进度百分比',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (sales_company_id) REFERENCES companies(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE KEY uk_company_sales_period (company_id, sales_company_id, target_year, target_period),
    INDEX idx_company_id (company_id),
    INDEX idx_sales_company (sales_company_id),
    INDEX idx_target_year (target_year),
    INDEX idx_target_period (target_period),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售目标表';

-- 5. 销售目标进度表 (sales_target_progress)
CREATE TABLE sales_target_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '进度ID',
    sales_target_id BIGINT NOT NULL COMMENT '销售目标ID',
    progress_date DATE NOT NULL COMMENT '进度日期',
    gmv_achieved DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已达成GMV',
    revenue_achieved DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已达成收入',
    order_count_achieved INT NOT NULL DEFAULT 0 COMMENT '已达成订单数量',
    customer_count_achieved INT NOT NULL DEFAULT 0 COMMENT '已达成客户数量',
    gmv_progress_percentage DECIMAL(5,2) COMMENT 'GMV进度百分比',
    revenue_progress_percentage DECIMAL(5,2) COMMENT '收入进度百分比',
    order_progress_percentage DECIMAL(5,2) COMMENT '订单进度百分比',
    customer_progress_percentage DECIMAL(5,2) COMMENT '客户进度百分比',
    overall_progress_percentage DECIMAL(5,2) COMMENT '整体进度百分比',
    gap_analysis TEXT COMMENT '差距分析',
    action_plan TEXT COMMENT '行动计划',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (sales_target_id) REFERENCES sales_targets(id),
    UNIQUE KEY uk_target_progress_date (sales_target_id, progress_date),
    INDEX idx_sales_target_id (sales_target_id),
    INDEX idx_progress_date (progress_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售目标进度表';

-- 6. 价格变更记录表 (price_change_logs)
CREATE TABLE price_change_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '变更记录ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    product_id BIGINT COMMENT '商品ID（NULL表示批量变更）',
    price_tier_id BIGINT COMMENT '价格分层ID',
    change_type ENUM('CREATE', 'UPDATE', 'DELETE', 'ACTIVATE', 'DEACTIVATE') NOT NULL COMMENT '变更类型',
    old_price DECIMAL(10,2) COMMENT '原价格',
    new_price DECIMAL(10,2) COMMENT '新价格',
    old_discount_rate DECIMAL(5,4) COMMENT '原折扣率',
    new_discount_rate DECIMAL(5,4) COMMENT '新折扣率',
    change_reason VARCHAR(200) COMMENT '变更原因',
    effective_from TIMESTAMP NULL COMMENT '生效时间',
    effective_to TIMESTAMP NULL COMMENT '失效时间',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '审批状态',
    approval_comment TEXT COMMENT '审批意见',
    approval_by BIGINT COMMENT '审批人ID',
    approval_at TIMESTAMP NULL COMMENT '审批时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (price_tier_id) REFERENCES price_tiers(id),
    FOREIGN KEY (approval_by) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_company_id (company_id),
    INDEX idx_product_id (product_id),
    INDEX idx_price_tier (price_tier_id),
    INDEX idx_change_type (change_type),
    INDEX idx_approval_status (approval_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格变更记录表';

-- 7. 客户价格等级表 (customer_price_levels)
CREATE TABLE customer_price_levels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户价格等级ID',
    company_id BIGINT NOT NULL COMMENT '供应链公司ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    price_tier_id BIGINT NOT NULL COMMENT '价格分层ID',
    custom_discount_rate DECIMAL(5,4) COMMENT '自定义折扣率',
    custom_markup_rate DECIMAL(5,4) COMMENT '自定义加价率',
    special_terms TEXT COMMENT '特殊条款',
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    effective_to TIMESTAMP NULL COMMENT '失效时间',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (price_tier_id) REFERENCES price_tiers(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE KEY uk_customer_tier (company_id, customer_id, price_tier_id),
    INDEX idx_company_id (company_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_price_tier (price_tier_id),
    INDEX idx_effective_time (effective_from, effective_to),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户价格等级表';

-- 插入示例价格分层数据
INSERT INTO price_tiers (company_id, tier_name, tier_code, tier_type, tier_level, description, discount_rate, markup_rate, customer_level_requirement, effective_start, effective_end, created_by) VALUES
(2, '1级经销商', 'DEALER_LEVEL_1', 'DEALER_LEVEL_1', 1, '一级经销商，享受最大折扣', 0.80, 1.00, 'PREMIUM', NOW(), DATE_ADD(NOW(), INTERVAL 10 YEAR), 1),
(2, '2级经销商', 'DEALER_LEVEL_2', 'DEALER_LEVEL_2', 2, '二级经销商，享受较大折扣', 0.85, 1.00, 'VIP', NOW(), DATE_ADD(NOW(), INTERVAL 10 YEAR), 1),
(2, '3级经销商', 'DEALER_LEVEL_3', 'DEALER_LEVEL_3', 3, '三级经销商，享受标准折扣', 0.90, 1.00, 'REGULAR', NOW(), DATE_ADD(NOW(), INTERVAL 10 YEAR), 1),
(2, 'VIP客户', 'VIP_CUSTOMER', 'VIP_CUSTOMER', 4, 'VIP客户，享受特殊优惠', 0.95, 1.00, 'VIP', NOW(), DATE_ADD(NOW(), INTERVAL 10 YEAR), 1),
(2, '批发客户', 'WHOLESALE', 'WHOLESALE', 5, '批发客户，享受批发价格', 0.88, 1.00, 'REGULAR', NOW(), DATE_ADD(NOW(), INTERVAL 10 YEAR), 1),
(2, '零售客户', 'RETAIL', 'RETAIL', 6, '零售客户，标准价格', 1.00, 1.00, 'ALL', NOW(), DATE_ADD(NOW(), INTERVAL 10 YEAR), 1);

-- 插入示例价格策略数据
INSERT INTO price_strategies (company_id, strategy_name, strategy_type, description, base_price_type, base_price_multiplier, min_margin_rate, created_by) VALUES
(2, '成本加成策略', 'FIXED', '基于成本价格加固定利润率的定价策略', 'COST', 1.30, 0.20, 1),
(2, '市场导向策略', 'DYNAMIC', '根据市场价格动态调整的定价策略', 'MARKET', 0.95, 0.15, 1),
(2, '分层定价策略', 'TIERED', '根据客户等级和订单量分层的定价策略', 'COST', 1.25, 0.18, 1);

-- 插入示例销售目标数据
INSERT INTO sales_targets (company_id, sales_company_id, target_year, target_period, period_start_date, period_end_date, gmv_target, revenue_target, order_count_target, customer_count_target, status, created_by) VALUES
(2, 3, 2024, 'Q1', '2024-01-01', '2024-03-31', 500000.00, 400000.00, 500, 100, 'ACTIVE', 1),
(2, 3, 2024, 'Q2', '2024-04-01', '2024-06-30', 600000.00, 480000.00, 600, 120, 'ACTIVE', 1),
(2, 3, 2024, 'H1', '2024-01-01', '2024-06-30', 1100000.00, 880000.00, 1100, 220, 'ACTIVE', 1);
