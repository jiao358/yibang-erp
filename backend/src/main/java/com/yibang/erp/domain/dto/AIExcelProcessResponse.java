package com.yibang.erp.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI Excel订单处理响应DTO
 */
@Data
public class AIExcelProcessResponse {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务状态
     */
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED

    /**
     * 处理进度信息
     */
    private ProgressInfo progress;

    /**
     * 处理结果统计
     */
    private ProcessStatistics statistics;

    /**
     * 错误信息列表
     */
    private List<ProcessError> errors;

    /**
     * 需要人工处理的订单列表
     */
    private List<ManualProcessOrder> manualProcessOrders;

    /**
     * 成功处理的订单列表
     */
    private List<ProcessedOrder> processedOrders;

    /**
     * 任务创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 任务开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 任务完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 处理耗时（毫秒）
     */
    private Long duration;

    /**
     * 任务消息
     */
    private String message;

    /**
     * 扩展信息
     */
    private Map<String, Object> extendedInfo;

    /**
     * 进度信息内部类
     */
    @Data
    public static class ProgressInfo {
        private int totalRows;
        private int processedRows;
        private int successRows;
        private int failedRows;
        private int manualProcessRows;
        private int currentRow;
        private String currentStep;
        private double percentage;
        private String estimatedTimeRemaining;
    }

    /**
     * 处理统计信息内部类
     */
    @Data
    public static class ProcessStatistics {
        private int totalOrders;
        private int successOrders;
        private int failedOrders;
        private int manualProcessOrders;
        private int skippedOrders;
        private double averageConfidence;
        private int highConfidenceOrders; // 置信度 > 0.8
        private int mediumConfidenceOrders; // 置信度 0.6-0.8
        private int lowConfidenceOrders; // 置信度 < 0.6
    }

    /**
     * 处理错误内部类
     */
    @Data
    public static class ProcessError {
        private int rowNumber;
        private String errorType; // VALIDATION, PRODUCT_MATCH, CUSTOMER_MATCH, SYSTEM
        private String errorMessage;
        private String suggestion;
        private String rawData;
        private double confidence;
    }

    /**
     * 需要人工处理的订单内部类
     */
    @Data
    public static class ManualProcessOrder {
        private int rowNumber;
        private String reason;
        private String rawData;
        private Map<String, Object> extractedData;
        private List<String> suggestions;
        private double confidence;
    }

    /**
     * 已处理的订单内部类
     */
    @Data
    public static class ProcessedOrder {
        private int rowNumber;
        private Long orderId;
        private String platformOrderNo;
        private String customerName;
        private String productName;
        private int quantity;
        private double unitPrice;
        private double totalAmount;
        private double confidence;
        private String processingNotes;
    }
}
