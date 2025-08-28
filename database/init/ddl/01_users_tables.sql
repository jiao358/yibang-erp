-- =====================================================
-- 用户管理相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.3
-- =====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS yibang_erp_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yibang_erp_dev;

-- 1. 公司表 (companies)
CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '公司ID',
    name VARCHAR(100) NOT NULL COMMENT '公司名称',
    type ENUM('SUPPLIER', 'SALES') NOT NULL COMMENT '公司类型：SUPPLIER-供应链公司，SALES-销售公司',
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
    INDEX idx_company_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司信息表';

-- 2. 角色表 (roles)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    permissions JSON COMMENT '权限配置JSON',
    is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统角色：0-否，1-是',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_role_name (name),
    INDEX idx_role_status (status)
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
    INDEX idx_user_username (username),
    INDEX idx_user_email (email),
    INDEX idx_user_company (company_id),
    INDEX idx_user_role (role_id),
    INDEX idx_user_status (status)
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

-- 插入基础角色数据
INSERT INTO roles (name, description, permissions, is_system, status) VALUES
('SYSTEM_ADMIN', '系统管理员', '["*"]', 1, 'ACTIVE'),
('SUPPLIER_ADMIN', '供应链管理员', '["SUPPLIER_MANAGE", "PRODUCT_MANAGE", "ORDER_MANAGE", "PRICE_MANAGE", "MESSAGE_MANAGE"]', 1, 'ACTIVE'),
('SUPPLIER_OPERATOR', '供应链操作员', '["PRODUCT_VIEW", "ORDER_PROCESS", "MESSAGE_VIEW"]', 1, 'ACTIVE'),
('SALES', '销售', '["ORDER_CREATE", "PRODUCT_VIEW", "MESSAGE_VIEW"]', 1, 'ACTIVE');

-- 插入基础公司数据
INSERT INTO companies (name, type, status, description) VALUES
('系统管理公司', 'SUPPLIER', 'ACTIVE', '系统管理专用公司'),
('示例供应链公司', 'SUPPLIER', 'ACTIVE', '示例供应链公司'),
('示例销售公司', 'SALES', 'ACTIVE', '示例销售公司');

-- 插入系统管理员用户（密码：admin123）
INSERT INTO users (username, password_hash, email, real_name, role_id, company_id, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@yibang.com', '系统管理员', 1, 1, 'ACTIVE');
