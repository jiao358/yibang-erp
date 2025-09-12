 -- 消息处理日志表
CREATE TABLE IF NOT EXISTS `message_processing_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息唯一ID',
  `source_order_id` varchar(100) DEFAULT NULL COMMENT '源订单号',
  `sales_order_id` varchar(100) DEFAULT NULL COMMENT '销售订单ID',
	`idempotency_key` VARCHAR(200) DEFAULT NULL COMMENT '幂等键，来自电商平台的Idempotency-Key',
	`bus_type` varchar(40)  NOT NULL COMMENT 'orders.create.q,orders.logistics.q,orders.address.q,	
orders.status.q,orders.dlq',
	
  `status` varchar(20) NOT NULL COMMENT '处理状态：PROCESSING, SUCCESS, FAILED, DUPLICATE, DEAD_LETTER',
  `result_message` text COMMENT '处理结果消息',
  `error_message` text COMMENT '错误信息',
	`total_message` text COMMENT '全部消息',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `processed_at` datetime DEFAULT NULL COMMENT '处理完成时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_source_sales_order` (`source_order_id`, `sales_order_id`),
  KEY `idx_status` (`status`),
	KEY `idx_bus_type` (`bus_type`),
  KEY `idx_idempotency_key` (`idempotency_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息处理日志表';