package com.yibang.erp.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 成功订单查询接口响应DTO
 */
@Data
public class SuccessOrdersResponse {
    
    /**
     * 成功订单列表
     */
    private List<SuccessOrderInfo> content;
    
    /**
     * 总元素数
     */
    private Long totalElements;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 当前页码
     */
    private Integer currentPage;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 响应状态
     */
    private String status;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 成功订单信息内部类
     */
    @Data
    public static class SuccessOrderInfo {
        /**
         * 订单ID
         */
        private Long id;
        
        /**
         * 订单编号
         */
        private String orderId;
        
        /**
         * 任务ID
         */
        private String taskId;
        
        /**
         * Excel行号
         */
        private Integer excelRowNumber;
        
        /**
         * 客户名称
         */
        private String customerName;
        
        /**
         * 客户ID
         */
        private Long customerId;
        
        /**
         * 产品名称
         */
        private String productName;
        
        /**
         * 产品ID
         */
        private Long productId;
        
        /**
         * 数量
         */
        private Integer quantity;
        
        /**
         * 单价
         */
        private Double unitPrice;
        
        /**
         * 总金额
         */
        private Double amount;
        
        /**
         * 订单状态
         */
        private String status;
        
        /**
         * 创建时间
         */
        private String createdAt;
        
        /**
         * 更新时间
         */
        private String updatedAt;
        
        /**
         * 备注
         */
        private String remark;
    }
    
    /**
     * 创建成功响应
     */
    public static SuccessOrdersResponse success() {
        SuccessOrdersResponse response = new SuccessOrdersResponse();
        response.setStatus("SUCCESS");
        response.setMessage("查询成功");
        return response;
    }
    
    /**
     * 创建错误响应
     */
    public static SuccessOrdersResponse error(String message) {
        SuccessOrdersResponse response = new SuccessOrdersResponse();
        response.setStatus("ERROR");
        response.setMessage(message);
        return response;
    }
}
