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
     * zsdxx 校园员工id
     */
    private String userId;
    
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
     * 下单时间（电商平台格式：yyyyMMdd hh:mm:ss）
     */
    private String createDate;
    
    /**
     * 用户昵称
     */
    private String userNickName;
    
    /**
     * 省份
     */
    private String provinceName;
    
    /**
     * 城市
     */
    private String cityName;
    
    /**
     * 区域
     */
    private String districtName;
    
    /**
     * 收件人姓名
     */
    private String deliveryContact;
    
    /**
     * 收件人电话
     */
    private String deliveryPhone;
    
    /**
     * 详细地址
     */
    private String deliveryAddress;
    
    /**
     * 买家留言
     */
    private String buyerNote;
    
    /**
     * 幂等性键（来自电商平台的Idempotency-Key）
     */
    private String idempotencyKey;
    
    /**
     * 订单类型（来自电商平台）
     */
    private String orderType;
    
    /**
     * 订单ID（来自电商平台）
     */
    private String orderId;
    
    /**
     * 订单状态（来自电商平台）
     */
    private String status;
    
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
        
        /**
         * 商品名称（电商平台字段）
         */
        private String offerName;
        
        /**
         * 商品SKU（电商平台字段）
         */
        private String offerId;
        
        /**
         * 分佣金额（电商平台字段）
         */
        private String commission;
    }
}
