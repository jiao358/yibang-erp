-- =====================================================
-- 数字大屏相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.3
-- =====================================================

USE yibang_erp_dev;

-- 1. 大屏配置表 (dashboard_configs)
CREATE TABLE dashboard_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    name VARCHAR(100) NOT NULL COMMENT '大屏名称',
    type ENUM('SALES', 'SUPPLY_CHAIN', 'FINANCIAL', 'OPERATIONS', 'CUSTOM') NOT NULL COMMENT '大屏类型',
    layout_config JSON NOT NULL COMMENT '布局配置JSON',
    theme_config JSON COMMENT '主题配置JSON',
    refresh_interval INT NOT NULL DEFAULT 30 COMMENT '刷新间隔(秒)',
    auto_refresh TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否自动刷新：0-否，1-是',
    is_public TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否公开：0-否，1-是',
    company_id BIGINT COMMENT '所属公司ID（NULL表示所有公司）',
    status ENUM('ACTIVE', 'INACTIVE', 'DRAFT') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-禁用，DRAFT-草稿',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_dashboard_type (type),
    INDEX idx_company_id (company_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大屏配置表';

-- 2. 大屏组件表 (dashboard_components)
CREATE TABLE dashboard_components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '组件ID',
    dashboard_id BIGINT NOT NULL COMMENT '大屏ID',
    component_type ENUM('CHART', 'TABLE', 'METRIC', 'TEXT', 'IMAGE', 'VIDEO', 'IFRAME') NOT NULL COMMENT '组件类型',
    name VARCHAR(100) NOT NULL COMMENT '组件名称',
    position_x INT NOT NULL COMMENT 'X坐标位置',
    position_y INT NOT NULL COMMENT 'Y坐标位置',
    width INT NOT NULL COMMENT '组件宽度',
    height INT NOT NULL COMMENT '组件高度',
    config JSON NOT NULL COMMENT '组件配置JSON',
    data_source VARCHAR(200) COMMENT '数据源配置',
    refresh_interval INT COMMENT '刷新间隔(秒)',
    is_visible TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可见：0-否，1-是',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (dashboard_id) REFERENCES dashboard_configs(id),
    INDEX idx_dashboard_id (dashboard_id),
    INDEX idx_component_type (component_type),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大屏组件表';

-- 3. 大屏数据表 (dashboard_data)
CREATE TABLE dashboard_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '数据ID',
    dashboard_id BIGINT NOT NULL COMMENT '大屏ID',
    component_id BIGINT NOT NULL COMMENT '组件ID',
    data_type ENUM('REAL_TIME', 'HISTORICAL', 'AGGREGATED') NOT NULL COMMENT '数据类型',
    data_key VARCHAR(100) NOT NULL COMMENT '数据键',
    data_value JSON NOT NULL COMMENT '数据值JSON',
    data_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据时间戳',
    expiry_time TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (dashboard_id) REFERENCES dashboard_configs(id),
    FOREIGN KEY (component_id) REFERENCES dashboard_components(id),
    INDEX idx_dashboard_component (dashboard_id, component_id),
    INDEX idx_data_type (data_type),
    INDEX idx_data_timestamp (data_timestamp),
    INDEX idx_expiry_time (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大屏数据表';

-- 4. 大屏访问记录表 (dashboard_access_logs)
CREATE TABLE dashboard_access_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '访问记录ID',
    dashboard_id BIGINT NOT NULL COMMENT '大屏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    access_type ENUM('VIEW', 'EDIT', 'CONFIGURE') NOT NULL COMMENT '访问类型',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    session_duration INT COMMENT '会话时长(秒)',
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    FOREIGN KEY (dashboard_id) REFERENCES dashboard_configs(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    INDEX idx_dashboard_id (dashboard_id),
    INDEX idx_user_id (user_id),
    INDEX idx_company_id (company_id),
    INDEX idx_access_type (access_type),
    INDEX idx_accessed_at (accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大屏访问记录表';

-- 5. 大屏模板表 (dashboard_templates)
CREATE TABLE dashboard_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    description TEXT COMMENT '模板描述',
    category VARCHAR(50) NOT NULL COMMENT '模板分类',
    thumbnail_url VARCHAR(255) COMMENT '缩略图URL',
    template_config JSON NOT NULL COMMENT '模板配置JSON',
    is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统模板：0-否，1-是',
    usage_count INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_template_category (category),
    INDEX idx_is_system (is_system),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大屏模板表';

-- 6. 实时数据流表 (real_time_data_streams)
CREATE TABLE real_time_data_streams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '数据流ID',
    stream_name VARCHAR(100) NOT NULL UNIQUE COMMENT '数据流名称',
    data_type ENUM('ORDERS', 'SALES', 'INVENTORY', 'FINANCIAL', 'CUSTOM') NOT NULL COMMENT '数据类型',
    source_table VARCHAR(100) COMMENT '源数据表',
    source_query TEXT COMMENT '源数据查询SQL',
    update_frequency INT NOT NULL DEFAULT 60 COMMENT '更新频率(秒)',
    last_update_at TIMESTAMP NULL COMMENT '最后更新时间',
    next_update_at TIMESTAMP NULL COMMENT '下次更新时间',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_stream_name (stream_name),
    INDEX idx_data_type (data_type),
    INDEX idx_is_active (is_active),
    INDEX idx_next_update (next_update_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实时数据流表';

-- 插入基础大屏模板数据
INSERT INTO dashboard_templates (name, description, category, template_config, is_system, created_by) VALUES
('销售大屏模板', '标准销售数据展示模板，包含订单量、销售额、客户分析等', 'SALES', '{"layout": "grid", "components": ["sales_metrics", "order_chart", "customer_analysis"]}', 1, 1),
('供应链大屏模板', '供应链运营数据展示模板，包含库存、订单处理、物流等', 'SUPPLY_CHAIN', '{"layout": "grid", "components": ["inventory_metrics", "order_processing", "logistics_status"]}', 1, 1),
('财务大屏模板', '财务数据展示模板，包含收入、支出、利润等', 'FINANCIAL', '{"layout": "grid", "components": ["revenue_metrics", "expense_chart", "profit_analysis"]}', 1, 1),
('运营大屏模板', '综合运营数据展示模板，包含多维度数据分析', 'OPERATIONS', '{"layout": "grid", "components": ["kpi_metrics", "trend_analysis", "performance_chart"]}', 1, 1);

-- 插入示例大屏配置
INSERT INTO dashboard_configs (name, type, layout_config, theme_config, company_id, created_by) VALUES
('销售数据大屏', 'SALES', '{"layout": "grid", "columns": 4, "rows": 3}', '{"theme": "dark", "primary_color": "#1890ff"}', 3, 1),
('供应链监控大屏', 'SUPPLY_CHAIN', '{"layout": "grid", "columns": 3, "rows": 4}', '{"theme": "light", "primary_color": "#52c41a"}', 2, 1);

-- 插入示例实时数据流
INSERT INTO real_time_data_streams (stream_name, data_type, source_table, update_frequency, created_by) VALUES
('订单实时数据', 'ORDERS', 'orders', 30, 1),
('销售实时数据', 'SALES', 'orders', 60, 1),
('库存实时数据', 'INVENTORY', 'product_inventory', 120, 1);
