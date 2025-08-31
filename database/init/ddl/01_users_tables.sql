-- =====================================================
-- 用户管理相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.4 (增加多租户支持)
-- =====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS yibang_erp_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yibang_erp_dev;

-- 1. 公司表 (companies)
CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '公司ID',
    name VARCHAR(100) NOT NULL COMMENT '公司名称',
    type ENUM('SUPPLIER', 'SALES') NOT NULL COMMENT '公司类型：SUPPLIER-供应链公司，SALES-销售公司',
    parent_supplier_id BIGINT COMMENT '上级供应链公司ID（可为空，表示顶级供应链公司）',
    supplier_level INT DEFAULT 1 COMMENT '供应链层级：1-顶级，2-二级，3-三级',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃，SUSPENDED-暂停',
    business_license VARCHAR(100) COMMENT '营业执照号',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    address TEXT COMMENT '公司地址',
    description TEXT COMMENT '公司描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_company_type (type),
    INDEX idx_company_status (status),
    INDEX idx_company_name (name),
    INDEX idx_parent_supplier (parent_supplier_id),
    INDEX idx_supplier_level (supplier_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司信息表';

-- 2. 角色表 (roles)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    permissions JSON COMMENT '权限配置JSON',
    supplier_scope ENUM('ALL', 'OWN_SUPPLIER', 'OWN_SALES') DEFAULT 'OWN_SALES' COMMENT '权限范围：ALL-全部，OWN_SUPPLIER-仅自己供应链，OWN_SALES-仅自己销售',
    is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统角色：0-否，1-是',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_role_name (name),
    INDEX idx_role_status (status),
    INDEX idx_supplier_scope (supplier_scope)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 3. 用户表 (users)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(255) COMMENT '头像URL',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    supplier_company_id BIGINT COMMENT '所属供应链公司ID（可为空，表示个人销售）',
    sales_company_id BIGINT COMMENT '所属销售公司ID（可为空，表示个人销售）',
    sales_type ENUM('COMPANY_SALES', 'INDIVIDUAL_SALES') DEFAULT 'INDIVIDUAL_SALES' COMMENT '销售类型：COMPANY_SALES-公司销售，INDIVIDUAL_SALES-个人销售',
    price_tier_id BIGINT COMMENT '价格分层等级ID',
    department VARCHAR(50) COMMENT '部门',
    position VARCHAR(50) COMMENT '职位',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED', 'PENDING') NOT NULL DEFAULT 'PENDING' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃，LOCKED-锁定，PENDING-待激活',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(45) COMMENT '最后登录IP',
    login_attempts INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
    locked_until TIMESTAMP NULL COMMENT '锁定截止时间',
    password_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '密码最后修改时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (supplier_company_id) REFERENCES companies(id),
    FOREIGN KEY (sales_company_id) REFERENCES companies(id),
    INDEX idx_user_username (username),
    INDEX idx_user_email (email),
    INDEX idx_user_company (company_id),
    INDEX idx_user_role (role_id),
    INDEX idx_user_status (status),
    INDEX idx_supplier_company (supplier_company_id),
    INDEX idx_sales_company (sales_company_id),
    INDEX idx_sales_type (sales_type),
    INDEX idx_user_supplier_sales (supplier_company_id, sales_company_id, sales_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 4. 用户会话表 (user_sessions)
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_token VARCHAR(255) NOT NULL UNIQUE COMMENT '会话令牌',
    refresh_token VARCHAR(255) COMMENT '刷新令牌',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_session_token (session_token),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 5. 用户权限表 (user_permissions)
CREATE TABLE user_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    permission_key VARCHAR(100) NOT NULL COMMENT '权限键',
    permission_value JSON COMMENT '权限值',
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    granted_by BIGINT COMMENT '授权人ID',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (granted_by) REFERENCES users(id),
    UNIQUE KEY uk_user_permission (user_id, permission_key),
    INDEX idx_user_id (user_id),
    INDEX idx_permission_key (permission_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限表';

-- 6. 供应链公司与销售公司关系表
CREATE TABLE company_supplier_relationships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
    supplier_company_id BIGINT NOT NULL COMMENT '供应链公司ID',
    sales_company_id BIGINT NOT NULL COMMENT '销售公司ID',
    relationship_type ENUM('DIRECT', 'PARTNER', 'FRANCHISE') NOT NULL COMMENT '关系类型：DIRECT-直营，PARTNER-合作伙伴，FRANCHISE-加盟',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE' COMMENT '关系状态',
    commission_rate DECIMAL(5,2) COMMENT '佣金比例',
    start_date DATE COMMENT '合作开始日期',
    end_date DATE COMMENT '合作结束日期',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    
    FOREIGN KEY (supplier_company_id) REFERENCES companies(id),
    FOREIGN KEY (sales_company_id) REFERENCES companies(id),
    UNIQUE KEY uk_supplier_sales (supplier_company_id, sales_company_id),
    INDEX idx_supplier (supplier_company_id),
    INDEX idx_sales (sales_company_id),
    INDEX idx_status (status),
    INDEX idx_relationship_type (relationship_type),
    INDEX idx_relationship_supplier_status (supplier_company_id, status),
    INDEX idx_relationship_sales_status (sales_company_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应链公司与销售公司关系表';

-- 插入基础角色数据
INSERT INTO roles (name, description, permissions, supplier_scope, is_system, status) VALUES
('SYSTEM_ADMIN', '系统管理员', '["*"]', 'ALL', 1, 'ACTIVE'),
('SUPPLIER_ADMIN', '供应链管理员', '["SUPPLIER_MANAGE", "PRODUCT_MANAGE", "ORDER_MANAGE", "PRICE_MANAGE", "MESSAGE_MANAGE"]', 'OWN_SUPPLIER', 1, 'ACTIVE'),
('SUPPLIER_OPERATOR', '供应链操作员', '["PRODUCT_VIEW", "ORDER_PROCESS", "MESSAGE_VIEW"]', 'OWN_SALES', 1, 'ACTIVE'),
('SALES', '销售', '["ORDER_CREATE", "PRODUCT_VIEW", "MESSAGE_VIEW"]', 'OWN_SALES', 1, 'ACTIVE');

-- 插入基础公司数据
INSERT INTO companies (name, type, parent_supplier_id, supplier_level, status, description) VALUES
('系统管理公司', 'SUPPLIER', NULL, 1, 'ACTIVE', '系统管理专用公司'),
('示例供应链公司', 'SUPPLIER', NULL, 1, 'ACTIVE', '示例供应链公司'),
('示例销售公司', 'SALES', 2, 2, 'ACTIVE', '示例销售公司');

-- 插入系统管理员用户（密码：admin123）
INSERT INTO users (username, password_hash, email, real_name, role_id, company_id, supplier_company_id, sales_company_id, sales_type, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@yibang.com', '系统管理员', 1, 1, 1, 1, 'COMPANY_SALES', 'ACTIVE');

-- 插入示例供应链关系数据
INSERT INTO company_supplier_relationships (
    supplier_company_id, 
    sales_company_id, 
    relationship_type, 
    status, 
    commission_rate, 
    start_date, 
    created_by
) VALUES 
(2, 3, 'PARTNER', 'ACTIVE', 15.00, CURDATE(), 1);

-- 创建视图：用户供应链关系视图
CREATE VIEW v_user_supplier_relationships AS
SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    u.email,
    u.phone,
    u.status as user_status,
    u.sales_type,
    r.name as role_name,
    r.supplier_scope,
    c.id as company_id,
    c.name as company_name,
    c.type as company_type,
    c.status as company_status,
    u.supplier_company_id,
    sc.name as supplier_company_name,
    u.sales_company_id,
    ssc.name as sales_company_name,
    csr.relationship_type,
    csr.commission_rate,
    csr.status as relationship_status
FROM users u
LEFT JOIN roles r ON u.role_id = r.id
LEFT JOIN companies c ON u.company_id = c.id
LEFT JOIN companies sc ON u.supplier_company_id = sc.id
LEFT JOIN companies ssc ON u.sales_company_id = ssc.id
LEFT JOIN company_supplier_relationships csr ON (u.supplier_company_id = csr.supplier_company_id AND u.sales_company_id = csr.sales_company_id)
WHERE u.deleted = 0;
