package com.yibang.erp.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单批量处理响应DTO
 */
@Data
public class OrderBatchProcessResponse {

    /**
     * 处理批次ID
     */
    private String batchId;

    /**
     * 处理类型
     */
    private String processType;

    /**
     * 处理状态
     */
    private String status; // PROCESSING, COMPLETED, FAILED, PARTIALLY_COMPLETED

    /**
     * 总行数
     */
    private Integer totalRows;

    /**
     * 已处理行数
     */
    private Integer processedRows;

    /**
     * 成功处理行数
     */
    private Integer successRows;

    /**
     * 失败处理行数
     */
    private Integer failedRows;

    /**
     * 总订单数量
     */
    private Integer totalCount;

    /**
     * 成功处理数量
     */
    private Integer successCount;

    /**
     * 失败处理数量
     */
    private Integer failureCount;

    /**
     * 跳过处理数量
     */
    private Integer skippedCount;

    /**
     * 处理开始时间
     */
    private LocalDateTime startTime;

    /**
     * 处理完成时间
     */
    private LocalDateTime endTime;

    /**
     * 处理时间
     */
    private LocalDateTime processedAt;

    /**
     * 处理耗时（毫秒）
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 处理结果详情
     */
    private List<ProcessResult> results;

    /**
     * 错误信息汇总
     */
    private List<String> errorMessages;

    /**
     * 处理备注
     */
    private String remarks;

    /**
     * 操作人信息
     */
    private String operatorName;

    /**
     * 处理结果详情
     */
    @Data
    public static class ProcessResult {
        
        /**
         * 订单ID
         */
        private Long orderId;

        /**
         * 平台订单号
         */
        private String platformOrderNo;

        /**
         * 处理状态
         */
        private String status; // SUCCESS, FAILED, SKIPPED

        /**
         * 处理结果消息
         */
        private String message;

        /**
         * 错误详情
         */
        private String errorDetails;

        /**
         * 处理时间
         */
        private LocalDateTime processTime;

        /**
         * 处理前状态
         */
        private String beforeStatus;

        /**
         * 处理后状态
         */
        private String afterStatus;
    }

    /**
     * 处理状态枚举
     */
    public enum ProcessStatus {
        /**
         * 处理中
         */
        PROCESSING("PROCESSING", "处理中"),

        /**
         * 处理完成
         */
        COMPLETED("COMPLETED", "处理完成"),

        /**
         * 处理失败
         */
        FAILED("FAILED", "处理失败"),

        /**
         * 部分完成
         */
        PARTIALLY_COMPLETED("PARTIALLY_COMPLETED", "部分完成");

        private final String code;
        private final String name;

        ProcessStatus(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static ProcessStatus fromCode(String code) {
            for (ProcessStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown process status code: " + code);
        }
    }

    /**
     * 计算处理进度百分比
     */
    public double getProgressPercentage() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) (successCount + failureCount + skippedCount) / totalCount * 100;
    }

    /**
     * 检查是否全部成功
     */
    public boolean isAllSuccess() {
        return successCount != null && successCount.equals(totalCount) && failureCount != null && failureCount == 0;
    }

    /**
     * 检查是否有失败
     */
    public boolean hasFailures() {
        return failureCount != null && failureCount > 0;
    }
}
