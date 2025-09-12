package com.yibang.erp.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量供应商确认订单响应DTO
 */
@Data
public class OrderBatchConfirmResponse {

    /**
     * 处理是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 总订单数
     */
    private int totalCount;

    /**
     * 成功确认数量
     */
    private int successCount;

    /**
     * 跳过数量
     */
    private int skippedCount;

    /**
     * 失败数量
     */
    private int failedCount;

    /**
     * 成功确认的订单ID列表
     */
    private List<Long> successOrderIds;

    /**
     * 跳过的订单详情
     */
    private List<OrderSkipDetail> skippedOrders;

    /**
     * 失败的订单详情
     */
    private List<OrderErrorDetail> failedOrders;

    /**
     * 跳过的订单详情
     */
    @Data
    public static class OrderSkipDetail {
        private Long orderId;
        private String platformOrderNo;
        private String currentStatus;
        private String reason;
    }

    /**
     * 失败的订单详情
     */
    @Data
    public static class OrderErrorDetail {
        private Long orderId;
        private String platformOrderNo;
        private String errorMessage;
    }
}
