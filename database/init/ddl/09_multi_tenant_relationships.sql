-- =====================================================
-- 多租户关系字段增强 DDL
-- 文件：09_multi_tenant_relationships.sql
-- 描述：为多租户隔离增加必要的字段和表结构
-- 创建时间：2025-08-29
-- =====================================================

-- 1. 创建供应链公司与销售公司关系表
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
    INDEX idx_relationship_type (relationship_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应链公司与销售公司关系表';

-- 2. 为角色表增加权限范围字段
ALTER TABLE roles 
ADD COLUMN supplier_scope ENUM('ALL', 'OWN_SUPPLIER', 'OWN_SALES') DEFAULT 'OWN_SALES' COMMENT '权限范围：ALL-全部，OWN_SUPPLIER-仅自己供应链，OWN_SALES-仅自己销售' AFTER permissions;

-- 3. 为用户表增加多租户关联字段
ALTER TABLE users 
ADD COLUMN supplier_company_id BIGINT COMMENT '所属供应链公司ID（可为空，表示个人销售）' AFTER company_id,
ADD COLUMN sales_company_id BIGINT COMMENT '所属销售公司ID（可为空，表示个人销售）' AFTER supplier_company_id,
ADD COLUMN sales_type ENUM('COMPANY_SALES', 'INDIVIDUAL_SALES') DEFAULT 'INDIVIDUAL_SALES' COMMENT '销售类型：COMPANY_SALES-公司销售，INDIVIDUAL_SALES-个人销售' AFTER sales_company_id;

-- 4. 为用户表增加外键约束和索引
ALTER TABLE users 
ADD CONSTRAINT fk_user_supplier_company FOREIGN KEY (supplier_company_id) REFERENCES companies(id),
ADD CONSTRAINT fk_user_sales_company FOREIGN KEY (sales_company_id) REFERENCES companies(id),
ADD INDEX idx_supplier_company (supplier_company_id),
ADD INDEX idx_sales_company (sales_company_id),
ADD INDEX idx_sales_type (sales_type);

-- 5. 为公司表增加供应链标识字段
ALTER TABLE companies 
ADD COLUMN parent_supplier_id BIGINT COMMENT '上级供应链公司ID（可为空，表示顶级供应链公司）' AFTER type,
ADD COLUMN supplier_level INT DEFAULT 1 COMMENT '供应链层级：1-顶级，2-二级，3-三级' AFTER parent_supplier_id;

-- 6. 为公司表增加外键约束和索引
ALTER TABLE companies 
ADD CONSTRAINT fk_company_parent_supplier FOREIGN KEY (parent_supplier_id) REFERENCES companies(id),
ADD INDEX idx_parent_supplier (parent_supplier_id),
ADD INDEX idx_supplier_level (supplier_level);

-- 7. 插入示例供应链关系数据
INSERT INTO company_supplier_relationships (
    supplier_company_id, 
    sales_company_id, 
    relationship_type, 
    status, 
    commission_rate, 
    start_date, 
    created_by
) VALUES 
(2, 3, 'PARTNER', 'ACTIVE', 15.00, CURDATE(), 1),
(2, 4, 'FRANCHISE', 'ACTIVE', 20.00, CURDATE(), 1);

-- 8. 更新现有用户数据，建立供应链关联关系
-- 系统管理员用户
UPDATE users SET 
    supplier_company_id = 1,
    sales_company_id = 1,
    sales_type = 'COMPANY_SALES'
WHERE username = 'admin';

-- 9. 更新角色权限范围
UPDATE roles SET supplier_scope = 'ALL' WHERE name = 'SYSTEM_ADMIN';
UPDATE roles SET supplier_scope = 'OWN_SUPPLIER' WHERE name = 'SUPPLIER_ADMIN';
UPDATE roles SET supplier_scope = 'OWN_SALES' WHERE name = 'SUPPLIER_OPERATOR';
UPDATE roles SET supplier_scope = 'OWN_SALES' WHERE name = 'SALES';

-- 10. 创建视图：用户供应链关系视图
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

-- 11. 创建索引优化查询性能
CREATE INDEX idx_user_supplier_sales ON users(supplier_company_id, sales_company_id, sales_type);
CREATE INDEX idx_company_type_status ON companies(type, status);
CREATE INDEX idx_relationship_supplier_status ON company_supplier_relationships(supplier_company_id, status);
CREATE INDEX idx_relationship_sales_status ON company_supplier_relationships(sales_company_id, status);

-- 12. 添加注释说明
COMMENT ON TABLE company_supplier_relationships IS '供应链公司与销售公司关系表，支持一对多、多对多的复杂关系';
COMMENT ON COLUMN users.supplier_company_id IS '用户所属的供应链公司，用于权限隔离和数据范围控制';
COMMENT ON COLUMN users.sales_company_id IS '用户所属的销售公司，支持公司销售模式';
COMMENT ON COLUMN users.sales_type IS '销售类型，区分个人销售和公司销售';
COMMENT ON COLUMN roles.supplier_scope IS '角色权限范围，控制用户可访问的数据范围';
COMMENT ON COLUMN companies.parent_supplier_id IS '上级供应链公司，支持多级供应链结构';
COMMENT ON COLUMN companies.supplier_level IS '供应链层级，用于权限继承和数据范围控制';
