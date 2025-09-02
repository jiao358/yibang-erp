package com.yibang.erp.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 失败订单查询响应DTO
 */
@Data
public class FailedOrdersResponse {
    private List<FailedOrderInfo> content;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer size;
    private String status;
    private String message;

    public static FailedOrdersResponse success() {
        FailedOrdersResponse response = new FailedOrdersResponse();
        response.setStatus("SUCCESS");
        response.setMessage("获取失败订单列表成功");
        return response;
    }

    public static FailedOrdersResponse error(String message) {
        FailedOrdersResponse response = new FailedOrdersResponse();
        response.setStatus("ERROR");
        response.setMessage(message);
        return response;
    }

    /**
     * 失败订单信息
     */
    @Data
    public static class FailedOrderInfo {
        private Long id;
        private String taskId;
        private Integer excelRowNumber;
        private String rawData;
        private String errorType;
        private String errorMessage;
        private String suggestedAction;
        private String status;
        private String createdAt;
        private String updatedAt;
    }
}
