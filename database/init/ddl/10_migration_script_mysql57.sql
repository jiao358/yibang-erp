-- =====================================================
-- 数据迁移脚本 - MySQL 5.7兼容版本
-- 文件：10_migration_script_mysql57.sql
-- 描述：将现有数据迁移到新的多租户结构
-- 创建时间：2025-08-29
-- =====================================================

-- 注意：请在执行此脚本前备份数据库！

-- 1. 为现有表添加新字段（MySQL 5.7兼容版本）
-- 为roles表添加supplier_scope字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'roles' 
     AND COLUMN_NAME = 'supplier_scope') = 0,
    'ALTER TABLE roles ADD COLUMN supplier_scope ENUM(\'ALL\', \'OWN_SUPPLIER\', \'OWN_SALES\') DEFAULT \'OWN_SALES\' COMMENT \'权限范围：ALL-全部，OWN_SUPPLIER-仅自己供应链，OWN_SALES-仅自己销售\' AFTER permissions',
    'SELECT \'supplier_scope column already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为companies表添加供应链相关字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'companies' 
     AND COLUMN_NAME = 'parent_supplier_id') = 0,
    'ALTER TABLE companies ADD COLUMN parent_supplier_id BIGINT COMMENT \'上级供应链公司ID（可为空，表示顶级供应链公司）\' AFTER type',
    'SELECT \'parent_supplier_id column already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'companies' 
     AND COLUMN_NAME = 'supplier_level') = 0,
    'ALTER TABLE companies ADD COLUMN supplier_level INT DEFAULT 1 COMMENT \'供应链层级：1-顶级，2-二级，3-三级\' AFTER parent_supplier_id',
    'SELECT \'supplier_level column already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为users表添加多租户关联字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND COLUMN_NAME = 'supplier_company_id') = 0,
    'ALTER TABLE users ADD COLUMN supplier_company_id BIGINT COMMENT \'所属供应链公司ID（可为空，表示个人销售）\' AFTER company_id',
    'SELECT \'supplier_company_id column already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND COLUMN_NAME = 'sales_company_id') = 0,
    'ALTER TABLE users ADD COLUMN sales_company_id BIGINT COMMENT \'所属销售公司ID（可为空，表示个人销售）\' AFTER supplier_company_id',
    'SELECT \'sales_company_id column already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND COLUMN_NAME = 'sales_type') = 0,
    'ALTER TABLE users ADD COLUMN sales_type ENUM(\'COMPANY_SALES\', \'INDIVIDUAL_SALES\') DEFAULT \'INDIVIDUAL_SALES\' COMMENT \'销售类型：COMPANY_SALES-公司销售，INDIVIDUAL_SALES-个人销售\' AFTER sales_company_id',
    'SELECT \'sales_type column already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 创建供应链关系表（如果不存在）
CREATE TABLE IF NOT EXISTS company_supplier_relationships (
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

-- 3. 数据迁移：更新现有角色数据
UPDATE roles SET supplier_scope = 'ALL' WHERE name = 'SYSTEM_ADMIN';
UPDATE roles SET supplier_scope = 'OWN_SUPPLIER' WHERE name = 'SUPPLIER_ADMIN';
UPDATE roles SET supplier_scope = 'OWN_SALES' WHERE name = 'SUPPLIER_OPERATOR';
UPDATE roles SET supplier_scope = 'OWN_SALES' WHERE name = 'SALES';

-- 4. 数据迁移：更新现有公司数据
-- 假设ID为1的是系统管理公司，ID为2的是示例供应链公司，ID为3的是示例销售公司
UPDATE companies SET 
    parent_supplier_id = NULL, 
    supplier_level = 1 
WHERE id = 1 AND type = 'SUPPLIER';

UPDATE companies SET 
    parent_supplier_id = NULL, 
    supplier_level = 1 
WHERE id = 2 AND type = 'SUPPLIER';

UPDATE companies SET 
    parent_supplier_id = 2, 
    supplier_level = 2 
WHERE id = 3 AND type = 'SALES';

-- 5. 数据迁移：更新现有用户数据
-- 系统管理员用户
UPDATE users SET 
    supplier_company_id = 1,
    sales_company_id = 1,
    sales_type = 'COMPANY_SALES'
WHERE username = 'admin' AND role_id = (SELECT id FROM roles WHERE name = 'SYSTEM_ADMIN');

-- 其他用户根据其公司类型设置默认值
UPDATE users u
JOIN companies c ON u.company_id = c.id
SET 
    u.supplier_company_id = CASE 
        WHEN c.type = 'SUPPLIER' THEN c.id
        ELSE NULL
    END,
    u.sales_company_id = CASE 
        WHEN c.type = 'SALES' THEN c.id
        ELSE NULL
    END,
    u.sales_type = CASE 
        WHEN c.type = 'SALES' THEN 'COMPANY_SALES'
        ELSE 'INDIVIDUAL_SALES'
    END
WHERE u.username != 'admin';

-- 6. 创建供应链关系数据
-- 基于现有的公司数据创建关系
INSERT IGNORE INTO company_supplier_relationships (
    supplier_company_id, 
    sales_company_id, 
    relationship_type, 
    status, 
    commission_rate, 
    start_date, 
    created_by
) 
SELECT 
    c1.id as supplier_company_id,
    c2.id as sales_company_id,
    'PARTNER' as relationship_type,
    'ACTIVE' as status,
    15.00 as commission_rate,
    CURDATE() as start_date,
    1 as created_by
FROM companies c1
JOIN companies c2 ON c2.parent_supplier_id = c1.id
WHERE c1.type = 'SUPPLIER' AND c2.type = 'SALES'
  AND c2.parent_supplier_id IS NOT NULL;

-- 7. 添加外键约束（如果不存在）
-- 注意：在执行前请确保数据完整性

-- 为users表添加外键约束
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND CONSTRAINT_NAME = 'fk_user_supplier_company') = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_user_supplier_company FOREIGN KEY (supplier_company_id) REFERENCES companies(id)',
    'SELECT \'fk_user_supplier_company constraint already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND CONSTRAINT_NAME = 'fk_user_sales_company') = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_user_sales_company FOREIGN KEY (sales_company_id) REFERENCES companies(id)',
    'SELECT \'fk_user_sales_company constraint already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为companies表添加外键约束
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'companies' 
     AND CONSTRAINT_NAME = 'fk_company_parent_supplier') = 0,
    'ALTER TABLE companies ADD CONSTRAINT fk_company_parent_supplier FOREIGN KEY (parent_supplier_id) REFERENCES companies(id)',
    'SELECT \'fk_company_parent_supplier constraint already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8. 创建索引（如果不存在）
-- 为users表添加索引
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND INDEX_NAME = 'idx_supplier_company') = 0,
    'CREATE INDEX idx_supplier_company ON users(supplier_company_id)',
    'SELECT \'idx_supplier_company index already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND INDEX_NAME = 'idx_sales_company') = 0,
    'CREATE INDEX idx_sales_company ON users(sales_company_id)',
    'SELECT \'idx_sales_company index already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND INDEX_NAME = 'idx_sales_type') = 0,
    'CREATE INDEX idx_sales_type ON users(sales_type)',
    'SELECT \'idx_sales_type index already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND INDEX_NAME = 'idx_user_supplier_sales') = 0,
    'CREATE INDEX idx_user_supplier_sales ON users(supplier_company_id, sales_company_id, sales_type)',
    'SELECT \'idx_user_supplier_sales index already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为companies表添加索引
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'companies' 
     AND INDEX_NAME = 'idx_parent_supplier') = 0,
    'CREATE INDEX idx_parent_supplier ON companies(parent_supplier_id)',
    'SELECT \'idx_parent_supplier index already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'companies' 
     AND INDEX_NAME = 'idx_supplier_level') = 0,
    'CREATE INDEX idx_supplier_level ON companies(supplier_level)',
    'SELECT \'idx_supplier_level index already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为roles表添加索引
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'roles' 
     AND INDEX_NAME = 'idx_supplier_scope') = 0,
    'CREATE INDEX idx_supplier_scope ON roles(supplier_scope)',
    'SELECT \'idx_supplier_scope index already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为company_supplier_relationships表添加索引
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'company_supplier_relationships' 
     AND INDEX_NAME = 'idx_relationship_supplier_status') = 0,
    'CREATE INDEX idx_relationship_supplier_status ON company_supplier_relationships(supplier_company_id, status)',
    'SELECT \'idx_relationship_supplier_status index already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'company_supplier_relationships' 
     AND INDEX_NAME = 'idx_relationship_sales_status') = 0,
    'CREATE INDEX idx_relationship_sales_status ON company_supplier_relationships(sales_company_id, status)',
    'SELECT \'idx_relationship_sales_status index already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 9. 创建或更新视图
DROP VIEW IF EXISTS v_user_supplier_relationships;

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

-- 10. 验证迁移结果
-- 检查数据完整性
SELECT '用户表数据验证' as check_type, COUNT(*) as total_users FROM users WHERE deleted = 0
UNION ALL
SELECT '公司表数据验证', COUNT(*) FROM companies WHERE deleted = 0
UNION ALL
SELECT '角色表数据验证', COUNT(*) FROM roles WHERE deleted = 0
UNION ALL
SELECT '供应链关系表数据验证', COUNT(*) FROM company_supplier_relationships WHERE deleted = 0;

-- 检查用户供应链关联情况
SELECT 
    '用户供应链关联验证' as check_type,
    COUNT(*) as total_users,
    COUNT(supplier_company_id) as users_with_supplier,
    COUNT(sales_company_id) as users_with_sales,
    COUNT(CASE WHEN sales_type = 'COMPANY_SALES' THEN 1 END) as company_sales_users,
    COUNT(CASE WHEN sales_type = 'INDIVIDUAL_SALES' THEN 1 END) as individual_sales_users
FROM users 
WHERE deleted = 0;

-- 检查公司层级关系
SELECT 
    '公司层级关系验证' as check_type,
    COUNT(*) as total_companies,
    COUNT(parent_supplier_id) as companies_with_parent,
    COUNT(CASE WHEN supplier_level = 1 THEN 1 END) as level1_companies,
    COUNT(CASE WHEN supplier_level = 2 THEN 1 END) as level2_companies
FROM companies 
WHERE deleted = 0;
