-- 设置字符集
SET NAMES utf8mb4;

-- 错误订单信息表
CREATE TABLE ai_excel_error_orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  task_id VARCHAR(64) NOT NULL COMMENT '处理任务ID',
  excel_row_number BIGINT COMMENT 'Excel行号',
  raw_data TEXT COMMENT '原始Excel行数据',
  error_type VARCHAR(50) NOT NULL COMMENT '错误类型',
  error_message TEXT COMMENT '详细错误信息',
  error_level VARCHAR(20) DEFAULT 'ERROR' COMMENT '错误级别',
  suggested_action TEXT COMMENT '建议处理方式',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待处理, PROCESSED-已处理, IGNORED-已忽略',
  processed_by BIGINT COMMENT '处理人ID',
  processed_at TIMESTAMP NULL DEFAULT NULL COMMENT '处理时间',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  INDEX idx_task_id (task_id),
  INDEX idx_error_type (error_type),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Excel错误订单信息表';


-- 错误类型枚举表
CREATE TABLE ai_excel_error_types (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  error_code VARCHAR(50) NOT NULL UNIQUE COMMENT '错误代码',
  error_name VARCHAR(100) NOT NULL COMMENT '错误名称',
  error_description TEXT COMMENT '错误描述',
  suggested_solution TEXT COMMENT '建议解决方案',
  is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Excel错误类型表';

-- 插入基础错误类型数据
INSERT INTO ai_excel_error_types (error_code, error_name, error_description, suggested_solution) VALUES
('PARSE_ERROR', '解析错误', 'Excel数据解析失败', '检查Excel文件格式，确保数据完整性'),
('VALIDATION_ERROR', '验证错误', '数据验证失败', '检查数据格式和业务规则'),
('PRODUCT_NOT_FOUND', '商品未找到', '无法匹配到系统中的商品', '检查商品SKU或名称，或联系管理员添加商品'),
('CUSTOMER_NOT_FOUND', '客户未找到', '无法匹配到系统中的客户', '检查客户信息，或联系管理员添加客户'),
('DATA_FORMAT_ERROR', '数据格式错误', '数据格式不符合要求', '检查数据格式，确保符合系统要求'),
('BUSINESS_RULE_ERROR', '业务规则错误', '违反业务规则', '检查业务逻辑，确保符合系统规则'),
('SYSTEM_ERROR', '系统错误', '系统内部错误', '联系系统管理员处理'),
('INSUFFICIENT_STOCK', '库存不足', '商品库存不足', '检查库存情况，或联系供应商补货'),
('PRICE_ERROR', '价格错误', '价格信息异常', '检查价格信息，确保价格合理'),
('QUANTITY_ERROR', '数量错误', '数量信息异常', '检查数量信息，确保数量合理');

-- 错误订单处理记录表
CREATE TABLE ai_excel_error_order_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  error_order_id BIGINT NOT NULL COMMENT '错误订单ID',
  action VARCHAR(50) NOT NULL COMMENT '操作类型：VIEW-查看, PROCESS-处理, IGNORE-忽略, CREATE_ORDER-创建订单',
  operator_id BIGINT COMMENT '操作人ID',
  operator_name VARCHAR(100) COMMENT '操作人姓名',
  action_details TEXT COMMENT '操作详情',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  
  INDEX idx_error_order_id (error_order_id),
  INDEX idx_operator_id (operator_id),
  INDEX idx_created_at (created_at),
  
  FOREIGN KEY (error_order_id) REFERENCES ai_excel_error_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Excel错误订单处理日志表';
