package com.yibang.erp.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单消息DTO - 用于RabbitMQ消息传递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMessage {
    
    /**
     * 消息唯一ID
     */
    private String messageId;
    
    /**
     * 源订单号（客户提供的订单号）
     */
    private String sourceOrderId;
    
    /**
     * 销售订单ID
     */
    private String salesOrderId;
    
    /**
     * 销售用户ID
     */
    private Long salesUserId;
    
    /**
     * 销售公司ID
     */
    private Long salesCompanyId;
    
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 订单来源
     */
    private String source;
    
    /**
     * 模板版本
     */
    private String templateVersion;
    
    /**
     * 订单备注
     */
    private String remarks;
    
    /**
     * 扩展字段
     */
    private Map<String, Object> extendedFields;
    
    /**
     * 订单商品列表
     */
    private List<OrderItemMessage> orderItems;
    
    /**
     * 消息创建时间（ISO格式字符串）
     */
    private String messageTime;
    
    /**
     * 消息来源系统
     */
    private String sourceSystem;
    
    /**
     * 订单商品消息DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemMessage {
        
        /**
         * 商品ID
         */
        private Long productId;
        
        /**
         * 商品数量
         */
        private Integer quantity;
        
        /**
         * 商品单价
         */
        private BigDecimal unitPrice;
        
        /**
         * 商品备注
         */
        private String remarks;
        
        /**
         * 扩展字段
         */
        private Map<String, Object> extendedFields;
    }
}
