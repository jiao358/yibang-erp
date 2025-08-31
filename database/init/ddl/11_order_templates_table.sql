-- =====================================================
-- 订单模板表结构
-- 基于现有OrderTemplate实体类创建
-- 创建时间: 2025-08-31
-- 版本: V1.0
-- =====================================================

USE yibang_erp_dev;

-- 创建订单模板表
CREATE TABLE IF NOT EXISTS `order_templates` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `version` varchar(20) NOT NULL COMMENT '模板版本',
  `description` text COMMENT '模板描述',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-未启用',
  `effective_date` datetime NOT NULL COMMENT '生效时间',
  `deprecated_date` datetime DEFAULT NULL COMMENT '废弃时间',
  `core_fields_config` json COMMENT '核心字段配置（JSON格式）',
  `extended_fields_config` json COMMENT '扩展字段配置（JSON格式）',
  `business_rules_config` json COMMENT '业务规则配置（JSON格式）',
  `field_mapping_config` json COMMENT '字段映射配置（JSON格式）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_name_version` (`template_name`, `version`),
  KEY `idx_template_name` (`template_name`),
  KEY `idx_version` (`version`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_effective_date` (`effective_date`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单模板表';

-- 插入默认订单模板数据
INSERT INTO `order_templates` (
  `template_name`, 
  `version`, 
  `description`, 
  `is_active`, 
  `effective_date`, 
  `core_fields_config`, 
  `extended_fields_config`, 
  `business_rules_config`, 
  `field_mapping_config`
) VALUES (
  '标准订单模板',
  '1.0.0',
  '适用于大多数标准订单的默认模板',
  1,
  '2024-01-01 00:00:00',
  '[
    {"fieldName": "customer_id", "displayName": "客户ID", "fieldType": "NUMBER", "isRequired": true, "displayOrder": 1, "isEditable": true, "groupName": "基本信息"},
    {"fieldName": "order_type", "displayName": "订单类型", "fieldType": "STRING", "isRequired": true, "defaultValue": "NORMAL", "displayOrder": 2, "isEditable": true, "groupName": "基本信息"},
    {"fieldName": "expected_delivery_date", "displayName": "预计交货日期", "fieldType": "DATE", "isRequired": true, "displayOrder": 3, "isEditable": true, "groupName": "基本信息"},
    {"fieldName": "delivery_address", "displayName": "收货地址", "fieldType": "STRING", "isRequired": true, "displayOrder": 4, "isEditable": true, "groupName": "配送信息"},
    {"fieldName": "delivery_contact", "displayName": "收货联系人", "fieldType": "STRING", "isRequired": true, "displayOrder": 5, "isEditable": true, "groupName": "配送信息"},
    {"fieldName": "delivery_phone", "displayName": "联系电话", "fieldType": "STRING", "isRequired": true, "displayOrder": 6, "isEditable": true, "groupName": "配送信息"}
  ]',
  '[
    {"fieldName": "special_requirements", "displayName": "特殊要求", "fieldType": "STRING", "isRequired": false, "displayOrder": 7, "isEditable": true, "groupName": "扩展信息"},
    {"fieldName": "remarks", "displayName": "备注", "fieldType": "STRING", "isRequired": false, "displayOrder": 8, "isEditable": true, "groupName": "扩展信息"}
  ]',
  '[
    {"ruleName": "订单号唯一性", "ruleType": "VALIDATION", "targetField": "platform_order_id", "ruleExpression": "unique", "errorMessage": "订单号必须唯一", "priority": 1, "isActive": true},
    {"ruleName": "交货日期验证", "ruleType": "VALIDATION", "targetField": "expected_delivery_date", "ruleExpression": "future_date", "errorMessage": "交货日期必须是未来日期", "priority": 2, "isActive": true}
  ]',
  '{
    "customer_id": "customer.id",
    "order_type": "order.type", 
    "expected_delivery_date": "delivery.date",
    "delivery_address": "delivery.address",
    "delivery_contact": "delivery.contact",
    "delivery_phone": "delivery.phone"
  }'
);

-- 插入Excel导入模板
INSERT INTO `order_templates` (
  `template_name`, 
  `version`, 
  `description`, 
  `is_active`, 
  `effective_date`, 
  `core_fields_config`, 
  `extended_fields_config`, 
  `business_rules_config`, 
  `field_mapping_config`
) VALUES (
  'Excel导入模板',
  '1.0.0',
  '适用于Excel批量导入订单的模板',
  1,
  '2024-01-01 00:00:00',
  '[
    {"fieldName": "customer_code", "displayName": "客户编码", "fieldType": "STRING", "isRequired": true, "displayOrder": 1, "isEditable": true, "groupName": "客户信息"},
    {"fieldName": "product_sku", "displayName": "商品SKU", "fieldType": "STRING", "isRequired": true, "displayOrder": 2, "isEditable": true, "groupName": "商品信息"},
    {"fieldName": "quantity", "displayName": "数量", "fieldType": "NUMBER", "isRequired": true, "displayOrder": 3, "isEditable": true, "groupName": "商品信息"},
    {"fieldName": "unit_price", "displayName": "单价", "fieldType": "NUMBER", "isRequired": true, "displayOrder": 4, "isEditable": true, "groupName": "商品信息"},
    {"fieldName": "expected_delivery_date", "displayName": "预计交货日期", "fieldType": "DATE", "isRequired": true, "displayOrder": 5, "isEditable": true, "groupName": "配送信息"}
  ]',
  '[
    {"fieldName": "delivery_address", "displayName": "收货地址", "fieldType": "STRING", "isRequired": false, "displayOrder": 6, "isEditable": true, "groupName": "配送信息"},
    {"fieldName": "special_requirements", "displayName": "特殊要求", "fieldType": "STRING", "isRequired": false, "displayOrder": 7, "isEditable": true, "groupName": "扩展信息"}
  ]',
  '[
    {"ruleName": "数量验证", "ruleType": "VALIDATION", "targetField": "quantity", "ruleExpression": "positive_number", "errorMessage": "数量必须大于0", "priority": 1, "isActive": true},
    {"ruleName": "单价验证", "ruleType": "VALIDATION", "targetField": "unit_price", "ruleExpression": "positive_number", "errorMessage": "单价必须大于0", "priority": 2, "isActive": true}
  ]',
  '{
    "customer_code": "customer.code",
    "product_sku": "product.sku",
    "quantity": "item.quantity",
    "unit_price": "item.unit_price",
    "expected_delivery_date": "delivery.date"
  }'
);
