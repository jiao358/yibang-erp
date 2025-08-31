-- 库存管理模块数据库表结构
-- 创建时间：2025-08-31

-- 1. 仓库表
CREATE TABLE IF NOT EXISTS `warehouses` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_code` varchar(50) NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(100) NOT NULL COMMENT '仓库名称',
  `warehouse_type` varchar(20) NOT NULL COMMENT '仓库类型：MAIN-主仓库，BRANCH-分仓库，TEMPORARY-临时仓库',
  `company_id` bigint NOT NULL COMMENT '所属公司ID',
  `address` varchar(500) DEFAULT NULL COMMENT '仓库地址',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `area` decimal(10,2) DEFAULT NULL COMMENT '仓库面积(平方米)',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-停用，MAINTENANCE-维护中',
  `description` text COMMENT '仓库描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- 2. 商品库存表
CREATE TABLE IF NOT EXISTS `product_inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `available_quantity` int NOT NULL DEFAULT '0' COMMENT '可用库存数量',
  `reserved_quantity` int NOT NULL DEFAULT '0' COMMENT '预留库存数量',
  `damaged_quantity` int NOT NULL DEFAULT '0' COMMENT '损坏库存数量',
  `min_stock_level` int DEFAULT NULL COMMENT '最低库存水平',
  `max_stock_level` int DEFAULT NULL COMMENT '最高库存水平',
  `reorder_point` int DEFAULT NULL COMMENT '补货点',
  `last_stock_in` datetime DEFAULT NULL COMMENT '最后入库时间',
  `last_stock_out` datetime DEFAULT NULL COMMENT '最后出库时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_warehouse` (`product_id`,`warehouse_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品库存表';

-- 3. 库存操作记录表
CREATE TABLE IF NOT EXISTS `inventory_operations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operation_no` varchar(50) NOT NULL COMMENT '操作单号',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型：STOCK_IN-入库，STOCK_OUT-出库，ADJUSTMENT-调整，TRANSFER-转移',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `quantity` int NOT NULL COMMENT '操作数量',
  `before_available_qty` int NOT NULL COMMENT '操作前可用库存',
  `before_reserved_qty` int NOT NULL COMMENT '操作前预留库存',
  `before_damaged_qty` int NOT NULL COMMENT '操作前损坏库存',
  `after_available_qty` int NOT NULL COMMENT '操作后可用库存',
  `after_reserved_qty` int NOT NULL COMMENT '操作后预留库存',
  `after_damaged_qty` int NOT NULL COMMENT '操作后损坏库存',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `total_amount` decimal(12,2) DEFAULT NULL COMMENT '总金额',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `order_item_id` bigint DEFAULT NULL COMMENT '关联订单项ID',
  `reason` varchar(200) DEFAULT NULL COMMENT '操作原因',
  `remark` text COMMENT '备注',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_operation_no` (`operation_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operation_time` (`operation_time`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存操作记录表';

-- 4. 库存预警表
CREATE TABLE IF NOT EXISTS `inventory_alerts` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alert_no` varchar(50) NOT NULL COMMENT '预警编号',
  `alert_type` varchar(20) NOT NULL COMMENT '预警类型：LOW_STOCK-库存不足，OVERSTOCK-库存过多，EXPIRY-即将过期',
  `alert_level` varchar(20) NOT NULL COMMENT '预警级别：LOW-低，MEDIUM-中，HIGH-高，CRITICAL-紧急',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `current_quantity` int NOT NULL COMMENT '当前库存数量',
  `threshold_quantity` int NOT NULL COMMENT '预警阈值数量',
  `alert_content` text COMMENT '预警内容',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，HANDLED-已处理，IGNORED-已忽略',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `handling_result` varchar(200) DEFAULT NULL COMMENT '处理结果',
  `handling_remark` text COMMENT '处理备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alert_no` (`alert_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_alert_level` (`alert_level`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存预警表';

-- 5. 库存盘点表
CREATE TABLE IF NOT EXISTS `inventory_checks` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `check_no` varchar(50) NOT NULL COMMENT '盘点单号',
  `check_type` varchar(20) NOT NULL COMMENT '盘点类型：FULL-全面盘点，PARTIAL-部分盘点，RANDOM-随机盘点',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `status` varchar(20) NOT NULL DEFAULT 'PLANNED' COMMENT '盘点状态：PLANNED-计划中，IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消',
  `planned_start_time` datetime NOT NULL COMMENT '计划开始时间',
  `planned_end_time` datetime NOT NULL COMMENT '计划结束时间',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `checker_id` bigint NOT NULL COMMENT '盘点人ID',
  `reviewer_id` bigint NOT NULL COMMENT '审核人ID',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `review_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
  `review_comment` text COMMENT '审核意见',
  `description` text COMMENT '盘点描述',
  `remark` text COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`),
  KEY `idx_check_type` (`check_type`),
  KEY `idx_checker_id` (`checker_id`),
  KEY `idx_review_status` (`review_status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存盘点表';

-- 6. 库存盘点项目表
CREATE TABLE IF NOT EXISTS `inventory_check_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `check_id` bigint NOT NULL COMMENT '盘点ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `system_quantity` int NOT NULL COMMENT '系统库存数量',
  `actual_quantity` int NOT NULL COMMENT '实际库存数量',
  `difference_quantity` int NOT NULL DEFAULT '0' COMMENT '差异数量',
  `difference_amount` decimal(12,2) DEFAULT NULL COMMENT '差异金额',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待盘点，CHECKED-已盘点，VERIFIED-已验证',
  `checker_id` bigint NOT NULL COMMENT '盘点人ID',
  `checked_at` datetime DEFAULT NULL COMMENT '盘点时间',
  `verifier_id` bigint DEFAULT NULL COMMENT '验证人ID',
  `verified_at` datetime DEFAULT NULL COMMENT '验证时间',
  `difference_reason` varchar(200) DEFAULT NULL COMMENT '差异原因',
  `handling_method` varchar(100) DEFAULT NULL COMMENT '处理方法',
  `handling_remark` text COMMENT '处理备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_check_id` (`check_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存盘点项目表';

-- 插入示例数据
INSERT INTO `warehouses` (`warehouse_code`, `warehouse_name`, `warehouse_type`, `company_id`, `address`, `contact_person`, `contact_phone`, `area`, `status`, `description`) VALUES
('WH001', '主仓库', 'MAIN', 1, '北京市朝阳区xxx街道xxx号', '张三', '13800138001', 1000.00, 'ACTIVE', '公司主仓库，存储主要商品'),
('WH002', '分仓库A', 'BRANCH', 1, '上海市浦东新区xxx路xxx号', '李四', '13800138002', 500.00, 'ACTIVE', '上海分仓库，服务华东地区'),
('WH003', '临时仓库', 'TEMPORARY', 1, '广州市天河区xxx路xxx号', '王五', '13800138003', 200.00, 'ACTIVE', '临时仓库，用于促销活动');

-- 插入示例库存数据
INSERT INTO `product_inventory` (`product_id`, `warehouse_id`, `available_quantity`, `reserved_quantity`, `damaged_quantity`, `min_stock_level`, `max_stock_level`, `reorder_point`) VALUES
(1, 1, 100, 10, 0, 20, 200, 50),
(2, 1, 50, 5, 0, 10, 100, 25),
(3, 2, 80, 8, 0, 15, 150, 40);
