-- 订单人工处理表
-- 用于记录需要人工介入处理的订单，如订单关闭、地址修改、退款等场景


CREATE TABLE IF NOT EXISTS  `order_manual_processing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
	`platform_order_id` varchar(100) NOT NULL COMMENT '订单编号',
  `source_order_id` varchar(100) NOT NULL COMMENT '源订单ID（外部系统订单号）',
  `processing_type` varchar(50) NOT NULL COMMENT '处理类型：ORDER_CLOSE, ADDRESS_CHANGE, REFUND, CANCEL等',
  `processing_reason` varchar(500) DEFAULT NULL COMMENT '处理原因',
  `original_data` text DEFAULT NULL COMMENT '原始数据（变更前的数据）',
  `request_data` text DEFAULT NULL COMMENT '请求数据（变更后的数据）',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING, PROCESSING, COMPLETED, REJECTED',
  `priority` varchar(20) DEFAULT 'NORMAL' COMMENT '优先级：LOW, NORMAL, HIGH, URGENT',
  `assigned_to` bigint(20) DEFAULT NULL COMMENT '分配给谁处理（用户ID）',
  `processed_by` bigint(20) DEFAULT NULL COMMENT '实际处理人（用户ID）',
  `processed_at` datetime DEFAULT NULL COMMENT '处理完成时间',
  `processing_notes` text DEFAULT NULL COMMENT '处理备注',
	`supplier_company_id` bigint(20) NOT NULL COMMENT '供应链所属公司ID',
  `rejection_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_source_order_id` (`source_order_id`),
  KEY `idx_processing_type` (`processing_type`),
  KEY `idx_status` (`status`),
  KEY `idx_assigned_to` (`assigned_to`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单人工处理表';

-- 插入处理类型枚举数据（可选）
-- INSERT INTO `order_manual_processing` (`processing_type`, `status`) VALUES 
-- ('ORDER_CLOSE', 'PENDING'),
-- ('ADDRESS_CHANGE', 'PENDING'),
-- ('REFUND', 'PENDING'),
-- ('CANCEL', 'PENDING');
