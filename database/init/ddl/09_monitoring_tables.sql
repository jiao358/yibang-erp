-- =====================================================
-- 系统监控相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.3
-- =====================================================

USE yibang_erp_dev;

-- 1. 系统指标表 (system_metrics)
CREATE TABLE system_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '指标ID',
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    metric_key VARCHAR(100) NOT NULL COMMENT '指标键',
    metric_type ENUM('COUNTER', 'GAUGE', 'HISTOGRAM', 'SUMMARY') NOT NULL COMMENT '指标类型',
    metric_value DECIMAL(20,6) NOT NULL COMMENT '指标值',
    metric_unit VARCHAR(20) COMMENT '指标单位',
    tags JSON COMMENT '标签JSON',
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    host_name VARCHAR(100) COMMENT '主机名',
    service_name VARCHAR(100) COMMENT '服务名称',
    instance_id VARCHAR(100) COMMENT '实例ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_metric_key (metric_key),
    INDEX idx_metric_name (metric_name),
    INDEX idx_metric_type (metric_type),
    INDEX idx_timestamp (timestamp),
    INDEX idx_host_service (host_name, service_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统指标表';

-- 2. 系统告警表 (system_alerts)
CREATE TABLE system_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '告警ID',
    alert_name VARCHAR(100) NOT NULL COMMENT '告警名称',
    alert_key VARCHAR(100) NOT NULL COMMENT '告警键',
    alert_type ENUM('SYSTEM', 'BUSINESS', 'PERFORMANCE', 'SECURITY', 'AVAILABILITY') NOT NULL COMMENT '告警类型',
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL COMMENT '严重程度',
    status ENUM('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    source VARCHAR(100) NOT NULL COMMENT '告警源',
    metric_name VARCHAR(100) COMMENT '相关指标名称',
    threshold_value DECIMAL(20,6) COMMENT '阈值',
    current_value DECIMAL(20,6) COMMENT '当前值',
    alert_message TEXT NOT NULL COMMENT '告警消息',
    alert_details JSON COMMENT '告警详情JSON',
    first_occurrence TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次发生时间',
    last_occurrence TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后发生时间',
    acknowledged_at TIMESTAMP NULL COMMENT '确认时间',
    acknowledged_by BIGINT COMMENT '确认人ID',
    resolved_at TIMESTAMP NULL COMMENT '解决时间',
    resolved_by BIGINT COMMENT '解决人ID',
    resolution_notes TEXT COMMENT '解决说明',
    escalation_level INT NOT NULL DEFAULT 1 COMMENT '升级级别',
    max_escalation_level INT NOT NULL DEFAULT 3 COMMENT '最大升级级别',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (acknowledged_by) REFERENCES users(id),
    FOREIGN KEY (resolved_by) REFERENCES users(id),
    INDEX idx_alert_key (alert_key),
    INDEX idx_alert_type (alert_type),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_source (source),
    INDEX idx_first_occurrence (first_occurrence),
    INDEX idx_escalation_level (escalation_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统告警表';

-- 3. 监控配置表 (monitor_configs)
CREATE TABLE monitor_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_type ENUM('THRESHOLD', 'INTERVAL', 'RULE', 'NOTIFICATION') NOT NULL COMMENT '配置类型',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_description TEXT COMMENT '配置描述',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    effective_to TIMESTAMP NULL COMMENT '失效时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_config_key (config_key),
    INDEX idx_config_type (config_type),
    INDEX idx_is_active (is_active),
    INDEX idx_effective_time (effective_from, effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控配置表';

-- 4. 系统日志表 (system_logs)
CREATE TABLE system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    log_level ENUM('TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL') NOT NULL COMMENT '日志级别',
    logger_name VARCHAR(200) NOT NULL COMMENT '日志器名称',
    message TEXT NOT NULL COMMENT '日志消息',
    thread_name VARCHAR(100) COMMENT '线程名',
    stack_trace TEXT COMMENT '堆栈跟踪',
    mdc_data JSON COMMENT 'MDC数据JSON',
    host_name VARCHAR(100) COMMENT '主机名',
    service_name VARCHAR(100) COMMENT '服务名称',
    instance_id VARCHAR(100) COMMENT '实例ID',
    user_id BIGINT COMMENT '用户ID',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    request_id VARCHAR(100) COMMENT '请求ID',
    session_id VARCHAR(100) COMMENT '会话ID',
    log_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间戳',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_log_level (log_level),
    INDEX idx_logger_name (logger_name),
    INDEX idx_log_timestamp (log_timestamp),
    INDEX idx_host_service (host_name, service_name),
    INDEX idx_user_id (user_id),
    INDEX idx_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 5. 性能监控表 (performance_metrics)
CREATE TABLE performance_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '性能指标ID',
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    metric_category ENUM('CPU', 'MEMORY', 'DISK', 'NETWORK', 'DATABASE', 'APPLICATION', 'BUSINESS') NOT NULL COMMENT '指标分类',
    metric_value DECIMAL(20,6) NOT NULL COMMENT '指标值',
    metric_unit VARCHAR(20) COMMENT '指标单位',
    percentile_50 DECIMAL(20,6) COMMENT '50分位数',
    percentile_95 DECIMAL(20,6) COMMENT '95分位数',
    percentile_99 DECIMAL(20,6) COMMENT '99分位数',
    min_value DECIMAL(20,6) COMMENT '最小值',
    max_value DECIMAL(20,6) COMMENT '最大值',
    sample_count INT NOT NULL DEFAULT 1 COMMENT '样本数量',
    tags JSON COMMENT '标签JSON',
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    host_name VARCHAR(100) COMMENT '主机名',
    service_name VARCHAR(100) COMMENT '服务名称',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_metric_name (metric_name),
    INDEX idx_metric_category (metric_category),
    INDEX idx_timestamp (timestamp),
    INDEX idx_host_service (host_name, service_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='性能监控表';

-- 6. 业务指标表 (business_metrics)
CREATE TABLE business_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '业务指标ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    metric_category ENUM('SALES', 'INVENTORY', 'CUSTOMER', 'FINANCIAL', 'OPERATIONAL') NOT NULL COMMENT '指标分类',
    metric_value DECIMAL(20,6) NOT NULL COMMENT '指标值',
    metric_unit VARCHAR(20) COMMENT '指标单位',
    metric_period ENUM('REAL_TIME', 'HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY') NOT NULL COMMENT '指标周期',
    period_start TIMESTAMP NOT NULL COMMENT '周期开始时间',
    period_end TIMESTAMP NOT NULL COMMENT '周期结束时间',
    target_value DECIMAL(20,6) COMMENT '目标值',
    threshold_warning DECIMAL(20,6) COMMENT '警告阈值',
    threshold_critical DECIMAL(20,6) COMMENT '严重阈值',
    trend_direction ENUM('UP', 'DOWN', 'STABLE') COMMENT '趋势方向',
    trend_percentage DECIMAL(8,4) COMMENT '趋势百分比',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (company_id) REFERENCES companies(id),
    INDEX idx_company_id (company_id),
    INDEX idx_metric_name (metric_name),
    INDEX idx_metric_category (metric_category),
    INDEX idx_metric_period (metric_period),
    INDEX idx_period_time (period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务指标表';

-- 7. 告警规则表 (alert_rules)
CREATE TABLE alert_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_key VARCHAR(100) NOT NULL UNIQUE COMMENT '规则键',
    rule_type ENUM('THRESHOLD', 'TREND', 'ANOMALY', 'COMPOSITE') NOT NULL COMMENT '规则类型',
    metric_name VARCHAR(100) NOT NULL COMMENT '监控指标名称',
    condition_operator ENUM('GT', 'GTE', 'LT', 'LTE', 'EQ', 'NE', 'CONTAINS', 'NOT_CONTAINS') NOT NULL COMMENT '条件操作符',
    threshold_value DECIMAL(20,6) COMMENT '阈值',
    time_window INT COMMENT '时间窗口(秒)',
    evaluation_interval INT NOT NULL DEFAULT 60 COMMENT '评估间隔(秒)',
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL COMMENT '严重程度',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    description TEXT COMMENT '规则描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_rule_key (rule_key),
    INDEX idx_rule_type (rule_type),
    INDEX idx_metric_name (metric_name),
    INDEX idx_severity (severity),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

-- 8. 告警通知配置表 (alert_notifications)
CREATE TABLE alert_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知配置ID',
    alert_rule_id BIGINT NOT NULL COMMENT '告警规则ID',
    notification_type ENUM('EMAIL', 'SMS', 'WECHAT', 'WEBHOOK', 'SLACK', 'DINGTALK') NOT NULL COMMENT '通知类型',
    notification_config JSON NOT NULL COMMENT '通知配置JSON',
    recipients JSON NOT NULL COMMENT '接收者JSON',
    escalation_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用升级：0-否，1-是',
    escalation_delay INT COMMENT '升级延迟(分钟)',
    escalation_recipients JSON COMMENT '升级接收者JSON',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (alert_rule_id) REFERENCES alert_rules(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_alert_rule_id (alert_rule_id),
    INDEX idx_notification_type (notification_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警通知配置表';

-- 9. 系统健康检查表 (health_checks)
CREATE TABLE health_checks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '健康检查ID',
    check_name VARCHAR(100) NOT NULL COMMENT '检查名称',
    check_type ENUM('LIVENESS', 'READINESS', 'STARTUP') NOT NULL COMMENT '检查类型',
    status ENUM('HEALTHY', 'UNHEALTHY', 'DEGRADED', 'UNKNOWN') NOT NULL COMMENT '状态',
    response_time DECIMAL(8,3) COMMENT '响应时间(毫秒)',
    last_check_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后检查时间',
    next_check_at TIMESTAMP NOT NULL COMMENT '下次检查时间',
    check_interval INT NOT NULL DEFAULT 30 COMMENT '检查间隔(秒)',
    failure_count INT NOT NULL DEFAULT 0 COMMENT '失败次数',
    success_count INT NOT NULL DEFAULT 0 COMMENT '成功次数',
    details JSON COMMENT '检查详情JSON',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_check_name (check_name),
    INDEX idx_check_type (check_type),
    INDEX idx_status (status),
    INDEX idx_last_check (last_check_at),
    INDEX idx_next_check (next_check_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统健康检查表';

-- 插入基础监控配置数据
INSERT INTO monitor_configs (config_name, config_key, config_type, config_value, config_description, created_by) VALUES
('CPU使用率阈值', 'cpu.usage.threshold', 'THRESHOLD', '80.0', 'CPU使用率告警阈值，超过此值将触发告警', 1),
('内存使用率阈值', 'memory.usage.threshold', 'THRESHOLD', '85.0', '内存使用率告警阈值，超过此值将触发告警', 1),
('磁盘使用率阈值', 'disk.usage.threshold', 'THRESHOLD', '90.0', '磁盘使用率告警阈值，超过此值将触发告警', 1),
('响应时间阈值', 'response.time.threshold', 'THRESHOLD', '5000', 'API响应时间告警阈值(毫秒)，超过此值将触发告警', 1),
('指标收集间隔', 'metrics.collection.interval', 'INTERVAL', '60', '系统指标收集间隔(秒)', 1),
('日志保留天数', 'logs.retention.days', 'RULE', '30', '系统日志保留天数', 1);

-- 插入基础告警规则数据
INSERT INTO alert_rules (rule_name, rule_key, rule_type, metric_name, condition_operator, threshold_value, severity, description, created_by) VALUES
('CPU使用率告警', 'cpu.usage.alert', 'THRESHOLD', 'cpu.usage.percent', 'GTE', 80.0, 'HIGH', 'CPU使用率超过80%时触发告警', 1),
('内存使用率告警', 'memory.usage.alert', 'THRESHOLD', 'memory.usage.percent', 'GTE', 85.0, 'HIGH', '内存使用率超过85%时触发告警', 1),
('磁盘使用率告警', 'disk.usage.alert', 'THRESHOLD', 'disk.usage.percent', 'GTE', 90.0, 'CRITICAL', '磁盘使用率超过90%时触发告警', 1),
('响应时间告警', 'response.time.alert', 'THRESHOLD', 'api.response.time', 'GTE', 5000, 'MEDIUM', 'API响应时间超过5秒时触发告警', 1);

-- 插入基础健康检查数据
INSERT INTO health_checks (check_name, check_type, status, check_interval, next_check_at) VALUES
('数据库连接检查', 'LIVENESS', 'HEALTHY', 30, DATE_ADD(NOW(), INTERVAL 30 SECOND)),
('Redis连接检查', 'LIVENESS', 'HEALTHY', 30, DATE_ADD(NOW(), INTERVAL 30 SECOND)),
('应用服务检查', 'READINESS', 'HEALTHY', 60, DATE_ADD(NOW(), INTERVAL 60 SECOND));
