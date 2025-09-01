-- =====================================================
-- AI管理相关表结构
-- 创建时间: 2024-01-14
-- 版本: V1.3
-- =====================================================

USE yibang_erp_dev;

-- 1. AI模型表 (ai_models)
CREATE TABLE ai_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模型ID',
    name VARCHAR(100) NOT NULL COMMENT '模型名称',
    type VARCHAR(50) NOT NULL COMMENT '模型类型：PRODUCT_MAPPING, ORDER_PARSING, PRICE_PREDICTION, INVENTORY_PREDICTION, CUSTOMER_ANALYSIS, ADDRESS_PARSING等',
    version VARCHAR(20) NOT NULL COMMENT '版本号',
    description TEXT COMMENT '模型描述',
    provider VARCHAR(50) NOT NULL COMMENT '服务提供商：OPENAI, ALIYUN, TENCENT, CUSTOM',
    model_config JSON COMMENT '模型配置JSON',
    performance_metrics JSON COMMENT '性能指标JSON',
    training_data_info JSON COMMENT '训练数据信息',
    accuracy_rate DECIMAL(5,4) COMMENT '准确率',
    response_time_avg DECIMAL(8,3) COMMENT '平均响应时间(毫秒)',
    cost_per_request DECIMAL(10,6) COMMENT '每次请求成本',
    max_requests_per_minute INT COMMENT '每分钟最大请求数',
    status ENUM('ACTIVE', 'INACTIVE', 'TRAINING', 'DEPRECATED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-禁用，TRAINING-训练中，DEPRECATED-已废弃',
    is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认模型：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE KEY uk_model_version (name, version),
    INDEX idx_model_type (type),
    INDEX idx_model_provider (provider),
    INDEX idx_model_status (status),
    INDEX idx_model_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型表';

-- 2. API密钥表 (api_keys)
CREATE TABLE api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '密钥ID',
    name VARCHAR(100) NOT NULL COMMENT '密钥名称',
    provider VARCHAR(50) NOT NULL COMMENT '服务提供商：OPENAI, ALIYUN, TENCENT, CUSTOM',
    api_key VARCHAR(500) NOT NULL COMMENT '加密后的API密钥',
    api_secret VARCHAR(500) COMMENT '加密后的API密钥',
    base_url VARCHAR(255) COMMENT 'API基础URL',
    permissions JSON COMMENT '权限配置JSON',
    usage_limit BIGINT COMMENT '使用限制（次数）',
    usage_limit_period ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') COMMENT '使用限制周期',
    current_usage BIGINT NOT NULL DEFAULT 0 COMMENT '当前使用量',
    cost_per_request DECIMAL(10,6) COMMENT '每次请求成本',
    monthly_budget DECIMAL(10,2) COMMENT '月度预算',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    last_used_at TIMESTAMP NULL COMMENT '最后使用时间',
    status ENUM('ACTIVE', 'INACTIVE', 'EXPIRED', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-禁用，EXPIRED-已过期，SUSPENDED-已暂停',
    is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认密钥：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_provider (provider),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at),
    INDEX idx_default_key (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API密钥表';

-- 3. Token使用记录表 (token_usage)
CREATE TABLE token_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '使用记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    api_key_id BIGINT NOT NULL COMMENT 'API密钥ID',
    request_id VARCHAR(100) UNIQUE COMMENT '请求ID',
    request_type VARCHAR(50) NOT NULL COMMENT '请求类型：PRODUCT_MAPPING, ORDER_PARSING, PRICE_PREDICTION, INVENTORY_PREDICTION, CUSTOMER_ANALYSIS, ADDRESS_PARSING等',
    input_tokens INT NOT NULL DEFAULT 0 COMMENT '输入Token数量',
    output_tokens INT NOT NULL DEFAULT 0 COMMENT '输出Token数量',
    total_tokens INT NOT NULL DEFAULT 0 COMMENT '总Token数量',
    cost DECIMAL(10,6) NOT NULL DEFAULT 0 COMMENT '成本',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD' COMMENT '货币代码',
    request_content JSON COMMENT '请求内容',
    response_content JSON COMMENT '响应内容',
    processing_time DECIMAL(8,3) COMMENT '处理时间(毫秒)',
    success TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否成功：0-失败，1-成功',
    error_message TEXT COMMENT '错误信息',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (model_id) REFERENCES ai_models(id),
    FOREIGN KEY (api_key_id) REFERENCES api_keys(id),
    INDEX idx_user_id (user_id),
    INDEX idx_model_id (model_id),
    INDEX idx_api_key_id (api_key_id),
    INDEX idx_request_type (request_type),
    INDEX idx_created_at (created_at),
    INDEX idx_success (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token使用记录表';

-- 4. AI配置表 (ai_configs)
CREATE TABLE ai_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'ENUM') NOT NULL DEFAULT 'STRING' COMMENT '配置值类型',
    description VARCHAR(500) COMMENT '配置描述',
    category VARCHAR(50) NOT NULL COMMENT '配置分类：GLOBAL, MODEL, SECURITY, PERFORMANCE',
    is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统配置：0-否，1-是',
    is_encrypted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否加密：0-否，1-是',
    validation_rules JSON COMMENT '验证规则JSON',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    updated_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_config_key (config_key),
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';

-- 5. 模型性能表 (model_performance)
CREATE TABLE model_performance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '性能记录ID',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    date DATE NOT NULL COMMENT '统计日期',
    total_requests BIGINT NOT NULL DEFAULT 0 COMMENT '总请求数',
    success_requests BIGINT NOT NULL DEFAULT 0 COMMENT '成功请求数',
    failed_requests BIGINT NOT NULL DEFAULT 0 COMMENT '失败请求数',
    total_tokens BIGINT NOT NULL DEFAULT 0 COMMENT '总Token数量',
    avg_response_time DECIMAL(8,3) COMMENT '平均响应时间(毫秒)',
    max_response_time DECIMAL(8,3) COMMENT '最大响应时间(毫秒)',
    min_response_time DECIMAL(8,3) COMMENT '最小响应时间(毫秒)',
    total_cost DECIMAL(12,6) NOT NULL DEFAULT 0 COMMENT '总成本',
    accuracy_rate DECIMAL(5,4) COMMENT '准确率',
    error_rate DECIMAL(5,4) COMMENT '错误率',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (model_id) REFERENCES ai_models(id),
    UNIQUE KEY uk_model_date (model_id, date),
    INDEX idx_model_id (model_id),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型性能表';

-- 6. AI处理任务表 (ai_processing_tasks)
CREATE TABLE ai_processing_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型：PRODUCT_MAPPING, ORDER_PARSING, PRICE_PREDICTION, INVENTORY_PREDICTION, CUSTOMER_ANALYSIS, ADDRESS_PARSING等',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    api_key_id BIGINT NOT NULL COMMENT 'API密钥ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
    input_data JSON COMMENT '输入数据',
    output_data JSON COMMENT '输出数据',
    error_message TEXT COMMENT '错误信息',
    progress_percentage DECIMAL(5,2) DEFAULT 0 COMMENT '进度百分比',
    estimated_completion_time TIMESTAMP NULL COMMENT '预计完成时间',
    started_at TIMESTAMP NULL COMMENT '开始时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    processing_time DECIMAL(8,3) COMMENT '处理时间(毫秒)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retries INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (model_id) REFERENCES ai_models(id),
    FOREIGN KEY (api_key_id) REFERENCES api_keys(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_task_type (task_type),
    INDEX idx_task_status (task_status),
    INDEX idx_user_id (user_id),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI处理任务表';

-- 插入基础AI配置数据
INSERT INTO ai_configs (config_key, config_value, config_type, description, category, is_system) VALUES
('ai.global.enabled', 'true', 'BOOLEAN', 'AI功能全局开关', 'GLOBAL', 1),
('ai.confidence.threshold', '0.8', 'NUMBER', 'AI处理置信度阈值', 'MODEL', 1),
('ai.request.timeout', '30000', 'NUMBER', 'AI请求超时时间(毫秒)', 'PERFORMANCE', 1),
('ai.max.retries', '3', 'NUMBER', 'AI请求最大重试次数', 'PERFORMANCE', 1),
('ai.cost.alert.threshold', '100.00', 'NUMBER', 'AI成本告警阈值', 'SECURITY', 1),
('ai.rate.limit.enabled', 'true', 'BOOLEAN', 'AI请求频率限制开关', 'SECURITY', 1),
('ai.rate.limit.requests_per_minute', '60', 'NUMBER', '每分钟最大请求数', 'SECURITY', 1);

-- 插入示例AI模型数据
INSERT INTO ai_models (name, type, version, description, provider, model_config, status, is_default) VALUES
('商品映射模型V1.0', 'PRODUCT_MAPPING', '1.0.0', '用于Excel订单与标准商品的智能匹配', 'OPENAI', '{"model": "gpt-4", "temperature": 0.1, "max_tokens": 1000}', 'ACTIVE', 1),
('订单解析模型V1.0', 'ORDER_PARSING', '1.0.0', '用于非结构化订单数据的智能解析', 'OPENAI', '{"model": "gpt-4", "temperature": 0.1, "max_tokens": 2000}', 'ACTIVE', 1),
('价格预测模型V1.0', 'PRICE_PREDICTION', '1.0.0', '用于商品价格趋势分析和建议', 'ALIYUN', '{"model": "qwen-turbo", "temperature": 0.2, "max_tokens": 500}', 'ACTIVE', 1),
('地址解析模型V1.0', 'ADDRESS_PARSING', '1.0.0', '用于订单地址的智能解析和标准化', 'DEEPSEEK', '{"model": "deepseek-chat", "temperature": 0.1, "max_tokens": 500}', 'ACTIVE', 1);

-- 插入示例API密钥数据（注意：实际使用时需要加密存储）
INSERT INTO api_keys (name, provider, api_key, permissions, status, is_default, created_by) VALUES
('OpenAI GPT-4 Key', 'OPENAI', 'encrypted_key_placeholder', '["PRODUCT_MAPPING", "ORDER_PARSING"]', 'ACTIVE', 1, 1),
('阿里云通义千问 Key', 'ALIYUN', 'encrypted_key_placeholder', '["PRICE_PREDICTION", "INVENTORY_PREDICTION"]', 'ACTIVE', 1, 1),
('DeepSeek Chat Key', 'DEEPSEEK', 'encrypted_key_placeholder', '["ADDRESS_PARSING", "ORDER_PARSING"]', 'ACTIVE', 1, 1);
