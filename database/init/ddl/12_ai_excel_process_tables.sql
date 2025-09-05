-- AI Excel处理任务表
CREATE TABLE IF NOT EXISTS `ai_excel_process_tasks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务ID（业务唯一标识）',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小(字节)',
  `file_hash` varchar(128) NOT NULL COMMENT '文件哈希值',
  `task_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED',
  `sales_user_id` bigint(20) NOT NULL COMMENT '销售用户ID',
  `sales_company_id` bigint(20) NOT NULL COMMENT '销售公司ID',
  `priority` int(11) NOT NULL DEFAULT 3 COMMENT '处理优先级：1-5，1最高',
  `total_rows` int(11) DEFAULT 0 COMMENT '总行数',
  `processed_rows` int(11) DEFAULT 0 COMMENT '已处理行数',
  `success_rows` int(11) DEFAULT 0 COMMENT '成功处理行数',
  `failed_rows` int(11) DEFAULT 0 COMMENT '失败处理行数',
  `manual_process_rows` int(11) DEFAULT 0 COMMENT '需要人工处理行数',
  `average_confidence` decimal(5,4) DEFAULT 0.0000 COMMENT '平均置信度',
  `current_step` varchar(255) DEFAULT NULL COMMENT '当前处理步骤',
  `error_message` text COMMENT '错误信息',
  `process_params` TEXT DEFAULT NULL COMMENT '处理参数（JSON格式）',
  `process_result` TEXT DEFAULT NULL COMMENT '处理结果（JSON格式）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `started_at` datetime DEFAULT NULL COMMENT '开始时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint(20) NOT NULL COMMENT '创建人ID',
  `updated_by` bigint(20) NOT NULL COMMENT '更新人ID',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_sales_user_id` (`sales_user_id`),
  KEY `idx_sales_company_id` (`sales_company_id`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Excel处理任务表';

-- AI Excel处理任务详情表
CREATE TABLE IF NOT EXISTS `ai_excel_process_task_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务ID',
  `excel_row_number` int(11) NOT NULL COMMENT '行号',
  `process_status` varchar(32) NOT NULL COMMENT '处理状态：SUCCESS, FAILED, MANUAL_PROCESS, SKIPPED',
  `raw_data` TEXT DEFAULT NULL COMMENT '原始数据（JSON格式）',
  `extracted_data` TEXT DEFAULT NULL COMMENT '提取的数据（JSON格式）',
  `process_result` TEXT DEFAULT NULL COMMENT '处理结果（JSON格式）',
  `confidence` decimal(5,4) DEFAULT 0.0000 COMMENT '置信度',
  `error_type` varchar(32) DEFAULT NULL COMMENT '错误类型：VALIDATION, PRODUCT_MATCH, CUSTOMER_MATCH, SYSTEM',
  `error_message` text COMMENT '错误信息',
  `suggestion` text COMMENT '处理建议',
  `processing_notes` text COMMENT '处理备注',
  `order_id` VARCHAR(255) COMMENT '平台生成的订单id',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_row_number` (`excel_row_number`),
  KEY `idx_process_status` (`process_status`),
  KEY `idx_confidence` (`confidence`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Excel处理任务详情表';

-- 添加外键约束（可选）
-- ALTER TABLE `ai_excel_process_task_details` 
-- ADD CONSTRAINT `fk_task_details_task_id` 
-- FOREIGN KEY (`task_id`) REFERENCES `ai_excel_process_tasks` (`task_id`) ON DELETE CASCADE;

-- 创建索引优化查询性能
CREATE INDEX `idx_tasks_composite` ON `ai_excel_process_tasks` (`sales_user_id`, `task_status`, `created_at`);
CREATE INDEX `idx_details_composite` ON `ai_excel_process_task_details` (`task_id`, `process_status`, `confidence`);
