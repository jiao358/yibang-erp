-- =====================================================
-- 订单管理相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.3
-- =====================================================

USE yibang_erp_dev;

-- 1. 客户表 (customers)
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户ID',
    customer_code VARCHAR(50) NOT NULL UNIQUE COMMENT '客户编码',
    name VARCHAR(100) NOT NULL COMMENT '客户名称',
    company_id BIGINT NOT NULL COMMENT '所属销售公司ID',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    address TEXT COMMENT '客户地址',
    customer_type ENUM('INDIVIDUAL', 'ENTERPRISE', 'WHOLESALE') NOT NULL DEFAULT 'ENTERPRISE' COMMENT '客户类型：INDIVIDUAL-个人，ENTERPRISE-企业，WHOLESALE-批发',
    customer_level ENUM('REGULAR', 'VIP', 'PREMIUM') NOT NULL DEFAULT 'REGULAR' COMMENT '客户等级：REGULAR-普通，VIP-重要，PREMIUM-至尊',
    credit_limit DECIMAL(12,2) DEFAULT 0 COMMENT '信用额度',
    payment_terms VARCHAR(100) COMMENT '付款条件',
    tax_number VARCHAR(50) COMMENT '税号',
    bank_account VARCHAR(100) COMMENT '银行账户',
    status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃，BLOCKED-已封禁',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    INDEX idx_customer_code (customer_code),
    INDEX idx_customer_company (company_id),
    INDEX idx_customer_type (customer_type),
    INDEX idx_customer_level (customer_level),
    INDEX idx_customer_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- 2. 订单表 (orders)


CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    platform_order_id VARCHAR(100) NOT NULL UNIQUE COMMENT '平台订单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    sales_id BIGINT NOT NULL COMMENT '销售ID',
    supplier_company_id BIGINT NOT NULL COMMENT '供应链公司ID',
    order_type ENUM('NORMAL', 'WHOLESALE', 'RUSH', 'SAMPLE') NOT NULL DEFAULT 'NORMAL' COMMENT '订单类型：NORMAL-普通，WHOLESALE-批发，RUSH-加急，SAMPLE-样品',
    order_source ENUM('MANUAL', 'EXCEL_IMPORT', 'API', 'WEBSITE') NOT NULL DEFAULT 'MANUAL' COMMENT '订单来源：MANUAL-手动创建，EXCEL_IMPORT-Excel导入，API-API接口，WEBSITE-网站',
    order_status ENUM('DRAFT', 'SUBMITTED', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT' COMMENT '订单状态',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
    approval_comment TEXT COMMENT '审核意见',
    approval_at TIMESTAMP NULL COMMENT '审核时间',
    approval_by BIGINT COMMENT '审核人ID',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '折扣金额',
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '税额',
    shipping_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '运费',
    final_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '最终金额',
    currency VARCHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '货币代码',
    payment_status ENUM('UNPAID', 'PARTIALLY_PAID', 'PAID', 'REFUNDED') NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
    payment_method VARCHAR(50) COMMENT '支付方式',
    payment_at TIMESTAMP NULL COMMENT '支付时间',
    delivery_address TEXT COMMENT '收货地址',
    delivery_contact VARCHAR(50) COMMENT '收货联系人',
    delivery_phone VARCHAR(20) COMMENT '收货联系电话',
    expected_delivery_date DATE COMMENT '预计交货日期',
    special_requirements TEXT COMMENT '特殊要求',
    ai_confidence DECIMAL(5,4) COMMENT 'AI处理置信度',
    ai_processed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否经过AI处理：0-否，1-是',
    source_order_id VARCHAR(255) COMMENT '来源订单ID（存储其他系统的订单ID）',
    logistics_order_number VARCHAR(255) COMMENT '物流订单号',
    logistics_company VARCHAR(255) COMMENT '物流公司',
    province_code VARCHAR(20) COMMENT '省份代码',
    province_name VARCHAR(50) COMMENT '省份名称',
    city_code VARCHAR(20) COMMENT '城市代码',
    city_name VARCHAR(50) COMMENT '城市名称',
    district_code VARCHAR(20) COMMENT '区域代码',
    district_name VARCHAR(50) COMMENT '区域名称',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    
    
    FOREIGN KEY (sales_id) REFERENCES users(id),
    FOREIGN KEY (supplier_company_id) REFERENCES companies(id),
    FOREIGN KEY (approval_by) REFERENCES users(id),

    INDEX idx_logistics_order_number (logistics_order_number),
    INDEX idx_platform_order_id (platform_order_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_sales_id (sales_id),
    INDEX idx_supplier_company (supplier_company_id),
    INDEX idx_order_status (order_status),
    INDEX idx_approval_status (approval_status),
    INDEX idx_payment_status (payment_status),
    INDEX idx_created_at (created_at),
    INDEX idx_expected_delivery (expected_delivery_date),
    INDEX idx_source_order_id (source_order_id),
    -- 新增的地区相关索引
    INDEX idx_province_code (province_code),
    INDEX idx_city_code (city_code),
    INDEX idx_district_code (district_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';


-- 3. 订单明细表 (order_items)
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku VARCHAR(100) NOT NULL COMMENT '商品SKU',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_specifications JSON COMMENT '商品规格JSON',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    unit VARCHAR(20) NOT NULL COMMENT '单位',
    discount_rate DECIMAL(5,4) NOT NULL DEFAULT 1 COMMENT '折扣率',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '折扣金额',
    tax_rate DECIMAL(5,4) NOT NULL DEFAULT 0 COMMENT '税率',
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '税额',
    subtotal DECIMAL(12,2) NOT NULL COMMENT '小计金额',
    ai_mapped_product_id BIGINT COMMENT 'AI映射后的商品ID',
    ai_confidence DECIMAL(5,4) COMMENT 'AI映射置信度',
    ai_processing_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT 'AI处理状态',
    ai_processing_result JSON COMMENT 'AI处理结果JSON',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (ai_mapped_product_id) REFERENCES products(id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    INDEX idx_ai_status (ai_processing_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 4. 订单状态变更记录表 (order_status_logs)
CREATE TABLE order_status_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '状态记录ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    from_status ENUM('DRAFT', 'SUBMITTED', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED') COMMENT '原状态',
    to_status ENUM('DRAFT', 'SUBMITTED', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED') NOT NULL COMMENT '新状态',
    change_reason VARCHAR(200) COMMENT '变更原因',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_type ENUM('SYSTEM', 'USER', 'AI') NOT NULL DEFAULT 'USER' COMMENT '操作类型：SYSTEM-系统，USER-用户，AI-AI系统',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (operator_id) REFERENCES users(id),
    INDEX idx_order_id (order_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态变更记录表';

-- 5. 订单审核记录表 (order_approval_logs)
CREATE TABLE order_approval_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审核记录ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    action ENUM('SUBMIT', 'APPROVE', 'REJECT', 'MODIFY') NOT NULL COMMENT '操作类型：SUBMIT-提交审核，APPROVE-通过，REJECT-拒绝，MODIFY-修改',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL COMMENT '审核状态',
    comment TEXT COMMENT '审核意见',
    approver_id BIGINT COMMENT '审核人ID',
    approval_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (approver_id) REFERENCES users(id),
    INDEX idx_order_id (order_id),
    INDEX idx_approver_id (approver_id),
    INDEX idx_approval_time (approval_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单审核记录表';

-- 6. 物流信息表 (logistics_info)
CREATE TABLE logistics_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '物流信息ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    tracking_number VARCHAR(100) COMMENT '物流单号',
    carrier VARCHAR(50) COMMENT '物流公司',
    carrier_code VARCHAR(20) COMMENT '物流公司代码',
    shipping_method VARCHAR(50) COMMENT '运输方式',
    shipping_date TIMESTAMP NULL COMMENT '发货时间',
    estimated_delivery_date DATE COMMENT '预计到达日期',
    actual_delivery_date TIMESTAMP NULL COMMENT '实际到达日期',
    shipping_address TEXT COMMENT '发货地址',
    delivery_address TEXT COMMENT '收货地址',
    shipping_contact VARCHAR(50) COMMENT '发货联系人',
    delivery_contact VARCHAR(50) COMMENT '收货联系人',
    shipping_phone VARCHAR(20) COMMENT '发货联系电话',
    delivery_phone VARCHAR(20) COMMENT '收货联系电话',
    shipping_notes TEXT COMMENT '发货备注',
    delivery_notes TEXT COMMENT '收货备注',
    status ENUM('PENDING', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '物流状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    UNIQUE KEY uk_order_logistics (order_id),
    INDEX idx_tracking_number (tracking_number),
    INDEX idx_carrier (carrier),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流信息表';

-- 7. Excel导入记录表 (excel_import_logs)
CREATE TABLE excel_import_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '导入记录ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    file_hash VARCHAR(64) NOT NULL COMMENT '文件哈希值',
    import_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '导入状态',
    total_rows INT NOT NULL DEFAULT 0 COMMENT '总行数',
    processed_rows INT NOT NULL DEFAULT 0 COMMENT '已处理行数',
    success_rows INT NOT NULL DEFAULT 0 COMMENT '成功行数',
    failed_rows INT NOT NULL DEFAULT 0 COMMENT '失败行数',
    error_message TEXT COMMENT '错误信息',
    ai_processing_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT 'AI处理状态',
    ai_processing_result JSON COMMENT 'AI处理结果',
    import_user_id BIGINT NOT NULL COMMENT '导入用户ID',
    started_at TIMESTAMP NULL COMMENT '开始时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (import_user_id) REFERENCES users(id),
    INDEX idx_file_hash (file_hash),
    INDEX idx_import_status (import_status),
    INDEX idx_ai_status (ai_processing_status),
    INDEX idx_import_user (import_user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Excel导入记录表';

-- 插入示例客户数据
INSERT INTO customers (customer_code, name, company_id, contact_person, contact_phone, contact_email, customer_type, customer_level, status) VALUES
('CUST001', '示例企业客户', 3, '张经理', '13800138001', 'zhang@example.com', 'ENTERPRISE', 'VIP', 'ACTIVE'),
('CUST002', '示例个人客户', 3, '李女士', '13800138002', 'li@example.com', 'INDIVIDUAL', 'REGULAR', 'ACTIVE'),
('CUST003', '示例批发客户', 3, '王总', '13800138003', 'wang@example.com', 'WHOLESALE', 'PREMIUM', 'ACTIVE');
