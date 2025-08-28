-- =====================================================
-- 站内消息系统相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.3
-- =====================================================

USE yibang_erp_dev;

-- 1. 消息模板表 (message_templates)
CREATE TABLE message_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    template_code VARCHAR(100) NOT NULL UNIQUE COMMENT '模板代码',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type ENUM('SYSTEM', 'BUSINESS', 'NOTIFICATION', 'MARKETING') NOT NULL COMMENT '模板类型',
    title_template VARCHAR(200) NOT NULL COMMENT '标题模板',
    content_template TEXT NOT NULL COMMENT '内容模板',
    variables JSON COMMENT '变量定义JSON',
    category VARCHAR(50) NOT NULL COMMENT '消息分类',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统模板：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_template_code (template_code),
    INDEX idx_template_type (template_type),
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息模板表';

-- 2. 消息表 (messages)
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    message_code VARCHAR(100) NOT NULL UNIQUE COMMENT '消息编码',
    template_id BIGINT COMMENT '模板ID（NULL表示自定义消息）',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type ENUM('SYSTEM', 'BUSINESS', 'NOTIFICATION', 'MARKETING', 'PERSONAL') NOT NULL COMMENT '消息类型',
    category VARCHAR(50) NOT NULL COMMENT '消息分类',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
    sender_id BIGINT NOT NULL COMMENT '发送人ID',
    sender_type ENUM('USER', 'SYSTEM', 'AI') NOT NULL DEFAULT 'USER' COMMENT '发送人类型',
    company_id BIGINT NOT NULL COMMENT '发送人公司ID',
    is_broadcast TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否广播消息：0-否，1-是',
    broadcast_scope ENUM('ALL', 'COMPANY', 'ROLE', 'DEPARTMENT', 'CUSTOM') COMMENT '广播范围',
    broadcast_targets JSON COMMENT '广播目标JSON',
    scheduled_send_at TIMESTAMP NULL COMMENT '计划发送时间',
    sent_at TIMESTAMP NULL COMMENT '实际发送时间',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    status ENUM('DRAFT', 'SCHEDULED', 'SENDING', 'SENT', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (template_id) REFERENCES message_templates(id),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    INDEX idx_message_code (message_code),
    INDEX idx_template_id (template_id),
    INDEX idx_message_type (message_type),
    INDEX idx_category (category),
    INDEX idx_sender_id (sender_id),
    INDEX idx_company_id (company_id),
    INDEX idx_priority (priority),
    INDEX idx_status (status),
    INDEX idx_scheduled_send (scheduled_send_at),
    INDEX idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 3. 消息接收者表 (message_recipients)
CREATE TABLE message_recipients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '接收者ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    recipient_id BIGINT NOT NULL COMMENT '接收者ID',
    recipient_type ENUM('USER', 'ROLE', 'DEPARTMENT', 'COMPANY') NOT NULL DEFAULT 'USER' COMMENT '接收者类型',
    company_id BIGINT NOT NULL COMMENT '接收者公司ID',
    read_at TIMESTAMP NULL COMMENT '阅读时间',
    read_ip VARCHAR(45) COMMENT '阅读IP地址',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-否，1-是',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除：0-否，1-是',
    deleted_at TIMESTAMP NULL COMMENT '删除时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (message_id) REFERENCES messages(id),
    FOREIGN KEY (recipient_id) REFERENCES users(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    UNIQUE KEY uk_message_recipient (message_id, recipient_id),
    INDEX idx_message_id (message_id),
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_company_id (company_id),
    INDEX idx_is_read (is_read),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息接收者表';

-- 4. 消息附件表 (message_attachments)
CREATE TABLE message_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '附件ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    file_type VARCHAR(100) COMMENT '文件类型',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    file_hash VARCHAR(64) COMMENT '文件哈希值',
    download_count INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (message_id) REFERENCES messages(id),
    INDEX idx_message_id (message_id),
    INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息附件表';

-- 5. 消息日志表 (message_logs)
CREATE TABLE message_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    action ENUM('CREATE', 'SEND', 'READ', 'DELETE', 'UPDATE', 'FAIL') NOT NULL COMMENT '操作类型',
    user_id BIGINT COMMENT '操作用户ID',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    details JSON COMMENT '操作详情JSON',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (message_id) REFERENCES messages(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_message_id (message_id),
    INDEX idx_action (action),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息日志表';

-- 6. 消息设置表 (message_settings)
CREATE TABLE message_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    notification_channels JSON NOT NULL COMMENT '通知渠道JSON',
    email_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '邮件通知：0-禁用，1-启用',
    sms_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '短信通知：0-禁用，1-启用',
    wechat_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '微信通知：0-禁用，1-启用',
    app_push_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'APP推送：0-禁用，1-启用',
    in_site_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '站内消息：0-禁用，1-启用',
    quiet_hours_start TIME COMMENT '免打扰开始时间',
    quiet_hours_end TIME COMMENT '免打扰结束时间',
    message_categories JSON COMMENT '消息分类设置JSON',
    auto_read_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '自动已读：0-禁用，1-启用',
    auto_delete_days INT COMMENT '自动删除天数',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    UNIQUE KEY uk_user_company (user_id, company_id),
    INDEX idx_user_id (user_id),
    INDEX idx_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息设置表';

-- 7. 消息队列表 (message_queue)
CREATE TABLE message_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '队列ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    recipient_id BIGINT NOT NULL COMMENT '接收者ID',
    channel ENUM('EMAIL', 'SMS', 'WECHAT', 'APP_PUSH', 'IN_SITE') NOT NULL COMMENT '发送渠道',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retries INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_at TIMESTAMP NULL COMMENT '下次重试时间',
    status ENUM('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    error_message TEXT COMMENT '错误信息',
    sent_at TIMESTAMP NULL COMMENT '发送时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (message_id) REFERENCES messages(id),
    FOREIGN KEY (recipient_id) REFERENCES users(id),
    INDEX idx_message_id (message_id),
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_channel (channel),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_next_retry (next_retry_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息队列表';

-- 8. 消息统计表 (message_statistics)
CREATE TABLE message_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    user_id BIGINT COMMENT '用户ID（NULL表示公司统计）',
    statistic_date DATE NOT NULL COMMENT '统计日期',
    message_type ENUM('SYSTEM', 'BUSINESS', 'NOTIFICATION', 'MARKETING', 'PERSONAL') NOT NULL COMMENT '消息类型',
    channel ENUM('EMAIL', 'SMS', 'WECHAT', 'APP_PUSH', 'IN_SITE') NOT NULL COMMENT '发送渠道',
    total_sent INT NOT NULL DEFAULT 0 COMMENT '发送总数',
    total_delivered INT NOT NULL DEFAULT 0 COMMENT '送达总数',
    total_read INT NOT NULL DEFAULT 0 COMMENT '阅读总数',
    total_failed INT NOT NULL DEFAULT 0 COMMENT '失败总数',
    delivery_rate DECIMAL(5,4) COMMENT '送达率',
    read_rate DECIMAL(5,4) COMMENT '阅读率',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_statistic_record (company_id, user_id, statistic_date, message_type, channel),
    INDEX idx_company_id (company_id),
    INDEX idx_user_id (user_id),
    INDEX idx_statistic_date (statistic_date),
    INDEX idx_message_type (message_type),
    INDEX idx_channel (channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息统计表';

-- 插入基础消息模板数据
INSERT INTO message_templates (template_code, template_name, template_type, title_template, content_template, category, priority, is_system, created_by) VALUES
('ORDER_CREATED', '订单创建通知', 'BUSINESS', '新订单通知 - {order_id}', '您有一个新的订单 {order_id}，客户：{customer_name}，金额：{amount}，请及时处理。', 'ORDER', 'HIGH', 1, 1),
('ORDER_STATUS_CHANGED', '订单状态变更通知', 'BUSINESS', '订单状态更新 - {order_id}', '订单 {order_id} 状态已更新为 {new_status}，请关注处理进度。', 'ORDER', 'NORMAL', 1, 1),
('SYSTEM_MAINTENANCE', '系统维护通知', 'SYSTEM', '系统维护通知', '系统将于 {start_time} 进行维护，预计持续 {duration}，期间可能影响正常使用。', 'SYSTEM', 'HIGH', 1, 1),
('INVENTORY_ALERT', '库存预警通知', 'BUSINESS', '库存预警 - {product_name}', '商品 {product_name} 当前库存 {current_stock}，低于预警线 {alert_threshold}，请及时补货。', 'INVENTORY', 'URGENT', 1, 1),
('WELCOME_MESSAGE', '欢迎消息', 'MARKETING', '欢迎加入 {company_name}', '欢迎您加入 {company_name}！我们致力于为您提供优质的服务和产品。', 'WELCOME', 'LOW', 1, 1);

-- 插入示例消息设置数据
INSERT INTO message_settings (user_id, company_id, notification_channels, email_enabled, sms_enabled, wechat_enabled, app_push_enabled, in_site_enabled, created_by) VALUES
(1, 1, '["EMAIL", "IN_SITE"]', 1, 0, 0, 1, 1, 1),
(1, 2, '["EMAIL", "IN_SITE", "APP_PUSH"]', 1, 0, 0, 1, 1, 1);
