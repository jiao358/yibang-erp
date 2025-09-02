package com.yibang.erp.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 错误订单信息DTO
 * 用于前端展示错误订单信息
 */
@Data
public class ErrorOrderInfo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 处理任务ID
     */
    private String taskId;

    /**
     * Excel行号
     */
    private Integer excelRowNumber;

    /**
     * 原始Excel行数据
     */
    private Map<String, Object> rawData;

    /**
     * 错误类型
     */
    private String errorType;

    /**
     * 错误类型标签
     */
    private String errorTypeLabel;

    /**
     * 详细错误信息
     */
    private String errorMessage;

    /**
     * 错误级别
     */
    private String errorLevel;

    /**
     * 建议处理方式
     */
    private String suggestedAction;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 处理状态标签
     */
    private String statusLabel;

    /**
     * 处理人ID
     */
    private Long processedBy;

    /**
     * 处理人姓名
     */
    private String processedByName;

    /**
     * 处理时间
     */
    private LocalDateTime processedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 获取错误类型标签
     */
    public String getErrorTypeLabel() {
        if (errorTypeLabel != null) {
            return errorTypeLabel;
        }
        
        // 根据错误类型返回中文标签
        switch (errorType) {
            case "PARSE_ERROR":
                return "解析错误";
            case "VALIDATION_ERROR":
                return "验证错误";
            case "PRODUCT_NOT_FOUND":
                return "商品未找到";
            case "CUSTOMER_NOT_FOUND":
                return "客户未找到";
            case "DATA_FORMAT_ERROR":
                return "数据格式错误";
            case "BUSINESS_RULE_ERROR":
                return "业务规则错误";
            case "SYSTEM_ERROR":
                return "系统错误";
            case "INSUFFICIENT_STOCK":
                return "库存不足";
            case "PRICE_ERROR":
                return "价格错误";
            case "QUANTITY_ERROR":
                return "数量错误";
            default:
                return errorType;
        }
    }

    /**
     * 获取状态标签
     */
    public String getStatusLabel() {
        if (statusLabel != null) {
            return statusLabel;
        }
        
        // 根据状态返回中文标签
        switch (status) {
            case "PENDING":
                return "待处理";
            case "PROCESSED":
                return "已处理";
            case "IGNORED":
                return "已忽略";
            default:
                return status;
        }
    }
}
