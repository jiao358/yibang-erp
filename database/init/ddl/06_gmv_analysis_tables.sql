-- =====================================================
-- GMV分析相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.3
-- =====================================================

USE yibang_erp_dev;

-- 1. GMV统计表 (gmv_statistics)
CREATE TABLE gmv_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    sales_company_id BIGINT COMMENT '销售公司ID（NULL表示所有销售公司）',
    statistic_date DATE NOT NULL COMMENT '统计日期',
    period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') NOT NULL COMMENT '统计周期类型',
    period_start_date DATE NOT NULL COMMENT '周期开始日期',
    period_end_date DATE NOT NULL COMMENT '周期结束日期',
    total_orders INT NOT NULL DEFAULT 0 COMMENT '订单总数',
    total_order_amount DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '订单总金额',
    total_gmv DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'GMV总额',
    total_revenue DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '实际收入',
    total_cost DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '总成本',
    gross_profit DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '毛利润',
    gross_profit_margin DECIMAL(5,4) COMMENT '毛利率',
    average_order_value DECIMAL(10,2) COMMENT '平均订单价值',
    customer_count INT NOT NULL DEFAULT 0 COMMENT '客户数量',
    new_customer_count INT NOT NULL DEFAULT 0 COMMENT '新客户数量',
    repeat_customer_count INT NOT NULL DEFAULT 0 COMMENT '复购客户数量',
    product_count INT NOT NULL DEFAULT 0 COMMENT '商品种类数',
    top_product_categories TEXT COMMENT '热销商品分类JSON',
    top_sales_regions TEXT COMMENT '热销地区JSON',
    growth_rate DECIMAL(8,4) COMMENT '增长率',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (sales_company_id) REFERENCES companies(id),
    UNIQUE KEY uk_company_period (company_id, sales_company_id, statistic_date, period_type),
    INDEX idx_company_id (company_id),
    INDEX idx_sales_company (sales_company_id),
    INDEX idx_statistic_date (statistic_date),
    INDEX idx_period_type (period_type),
    INDEX idx_period_dates (period_start_date, period_end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GMV统计表';

-- 2. GMV目标表 (gmv_targets)
CREATE TABLE gmv_targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '目标ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    sales_company_id BIGINT COMMENT '销售公司ID（NULL表示所有销售公司）',
    target_year INT NOT NULL COMMENT '目标年份',
    target_period ENUM('Q1', 'Q2', 'Q3', 'Q4', 'H1', 'H2', 'YEARLY') NOT NULL COMMENT '目标周期',
    period_start_date DATE NOT NULL COMMENT '周期开始日期',
    period_end_date DATE NOT NULL COMMENT '周期结束日期',
    gmv_target DECIMAL(15,2) NOT NULL COMMENT 'GMV目标',
    order_count_target INT COMMENT '订单数量目标',
    customer_count_target INT COMMENT '客户数量目标',
    revenue_target DECIMAL(15,2) COMMENT '收入目标',
    profit_target DECIMAL(15,2) COMMENT '利润目标',
    growth_target DECIMAL(8,4) COMMENT '增长目标',
    description TEXT COMMENT '目标描述',
    status ENUM('ACTIVE', 'INACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (sales_company_id) REFERENCES companies(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE KEY uk_company_period_target (company_id, sales_company_id, target_year, target_period),
    INDEX idx_company_id (company_id),
    INDEX idx_sales_company (sales_company_id),
    INDEX idx_target_year (target_year),
    INDEX idx_target_period (target_period),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GMV目标表';

-- 3. GMV趋势分析表 (gmv_trends)
CREATE TABLE gmv_trends (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '趋势ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    sales_company_id BIGINT COMMENT '销售公司ID',
    trend_date DATE NOT NULL COMMENT '趋势日期',
    trend_type ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL COMMENT '趋势类型',
    gmv_value DECIMAL(15,2) NOT NULL COMMENT 'GMV值',
    order_count INT NOT NULL DEFAULT 0 COMMENT '订单数量',
    customer_count INT NOT NULL DEFAULT 0 COMMENT '客户数量',
    revenue_value DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '收入值',
    cost_value DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '成本值',
    profit_value DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '利润值',
    moving_average_7d DECIMAL(15,2) COMMENT '7日移动平均',
    moving_average_30d DECIMAL(15,2) COMMENT '30日移动平均',
    trend_direction ENUM('UP', 'DOWN', 'STABLE') COMMENT '趋势方向',
    trend_strength DECIMAL(5,4) COMMENT '趋势强度',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (sales_company_id) REFERENCES companies(id),
    UNIQUE KEY uk_company_trend_date (company_id, sales_company_id, trend_date, trend_type),
    INDEX idx_company_id (company_id),
    INDEX idx_sales_company (sales_company_id),
    INDEX idx_trend_date (trend_date),
    INDEX idx_trend_type (trend_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GMV趋势分析表';

-- 4. 销售业绩表 (sales_performance)
CREATE TABLE sales_performance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '业绩ID',
    sales_user_id BIGINT NOT NULL COMMENT '销售用户ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    performance_date DATE NOT NULL COMMENT '业绩日期',
    period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') NOT NULL COMMENT '业绩周期',
    period_start_date DATE NOT NULL COMMENT '周期开始日期',
    period_end_date DATE NOT NULL COMMENT '周期结束日期',
    total_orders INT NOT NULL DEFAULT 0 COMMENT '订单总数',
    total_gmv DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'GMV总额',
    total_revenue DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '实际收入',
    total_commission DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '佣金总额',
    commission_rate DECIMAL(5,4) COMMENT '佣金率',
    customer_count INT NOT NULL DEFAULT 0 COMMENT '客户数量',
    new_customer_count INT NOT NULL DEFAULT 0 COMMENT '新客户数量',
    repeat_customer_count INT NOT NULL DEFAULT 0 COMMENT '复购客户数量',
    average_order_value DECIMAL(10,2) COMMENT '平均订单价值',
    conversion_rate DECIMAL(5,4) COMMENT '转化率',
    target_achievement_rate DECIMAL(5,4) COMMENT '目标达成率',
    ranking_position INT COMMENT '排名位置',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (sales_user_id) REFERENCES users(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    UNIQUE KEY uk_sales_period (sales_user_id, company_id, performance_date, period_type),
    INDEX idx_sales_user (sales_user_id),
    INDEX idx_company_id (company_id),
    INDEX idx_performance_date (performance_date),
    INDEX idx_period_type (period_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售业绩表';

-- 5. 地区销售统计表 (regional_sales_stats)
CREATE TABLE regional_sales_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地区统计ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    region_code VARCHAR(20) NOT NULL COMMENT '地区代码',
    region_name VARCHAR(100) NOT NULL COMMENT '地区名称',
    region_level ENUM('PROVINCE', 'CITY', 'DISTRICT') NOT NULL COMMENT '地区级别',
    parent_region_code VARCHAR(20) COMMENT '上级地区代码',
    statistic_date DATE NOT NULL COMMENT '统计日期',
    period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') NOT NULL COMMENT '统计周期',
    total_orders INT NOT NULL DEFAULT 0 COMMENT '订单总数',
    total_gmv DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'GMV总额',
    total_revenue DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '实际收入',
    customer_count INT NOT NULL DEFAULT 0 COMMENT '客户数量',
    average_order_value DECIMAL(10,2) COMMENT '平均订单价值',
    growth_rate DECIMAL(8,4) COMMENT '增长率',
    market_share DECIMAL(5,4) COMMENT '市场份额',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    UNIQUE KEY uk_region_period (company_id, region_code, statistic_date, period_type),
    INDEX idx_company_id (company_id),
    INDEX idx_region_code (region_code),
    INDEX idx_parent_region (parent_region_code),
    INDEX idx_statistic_date (statistic_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地区销售统计表';

-- 6. 商品销售统计表 (product_sales_stats)
CREATE TABLE product_sales_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品统计ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    statistic_date DATE NOT NULL COMMENT '统计日期',
    period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') NOT NULL COMMENT '统计周期',
    total_quantity INT NOT NULL DEFAULT 0 COMMENT '销售数量',
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '销售金额',
    total_gmv DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'GMV总额',
    order_count INT NOT NULL DEFAULT 0 COMMENT '订单数量',
    customer_count INT NOT NULL DEFAULT 0 COMMENT '客户数量',
    average_price DECIMAL(10,2) COMMENT '平均价格',
    growth_rate DECIMAL(8,4) COMMENT '增长率',
    ranking_position INT COMMENT '排名位置',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    UNIQUE KEY uk_product_period (company_id, product_id, statistic_date, period_type),
    INDEX idx_company_id (company_id),
    INDEX idx_product_id (product_id),
    INDEX idx_category_id (category_id),
    INDEX idx_statistic_date (statistic_date),
    INDEX idx_period_type (period_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品销售统计表';

-- 插入示例GMV目标数据
INSERT INTO gmv_targets (company_id, target_year, target_period, period_start_date, period_end_date, gmv_target, order_count_target, customer_count_target, revenue_target, created_by) VALUES
(2, 2024, 'Q1', '2024-01-01', '2024-03-31', 1000000.00, 1000, 200, 800000.00, 1),
(2, 2024, 'Q2', '2024-04-01', '2024-06-30', 1200000.00, 1200, 250, 960000.00, 1),
(2, 2024, 'H1', '2024-01-01', '2024-06-30', 2200000.00, 2200, 450, 1760000.00, 1),
(2, 2024, 'YEARLY', '2024-01-01', '2024-12-31', 5000000.00, 5000, 1000, 4000000.00, 1);

-- 插入示例地区数据
INSERT INTO regional_sales_stats (company_id, region_code, region_name, region_level, statistic_date, period_type, total_orders, total_gmv, total_revenue, customer_count) VALUES
(2, '110000', '北京市', 'PROVINCE', '2024-01-14', 'DAILY', 50, 50000.00, 40000.00, 45),
(2, '310000', '上海市', 'PROVINCE', '2024-01-14', 'DAILY', 45, 45000.00, 36000.00, 40),
(2, '440100', '广州市', 'CITY', '2024-01-14', 'DAILY', 30, 30000.00, 24000.00, 28);
