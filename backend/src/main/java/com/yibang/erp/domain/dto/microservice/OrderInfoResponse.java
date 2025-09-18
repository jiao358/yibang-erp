package com.yibang.erp.domain.dto.microservice;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单信息响应DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class OrderInfoResponse {
    
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 平台订单号
     */
    private String platformOrderId;
    
    /**
     * 源订单号
     */
    private String sourceOrderId;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 订单项列表
     */
    private List<OrderItemInfo> orderItems;
    
    /**
     * 收货地址
     */
    private OrderAddressInfo address;
    
    /**
     * 物流信息
     */
    private LogisticsInfo logistics;
    
    /**
     * 客户信息
     */
    private CustomerInfo customer;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 订单项信息
     */
    @Data
    public static class OrderItemInfo {
        private Long id;
        private Long productId;
        private String productSku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
    
    /**
     * 订单地址信息
     */
    @Data
    public static class OrderAddressInfo {
        private String receiverName;
        private String receiverPhone;
        private String province;
        private String city;
        private String district;
        private String address;
        private String postalCode;
    }
    
    /**
     * 物流信息
     */
    @Data
    public static class LogisticsInfo {
        private String trackingNumber;
        private String carrier;
        private String status;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
    }
    
    /**
     * 客户信息
     */
    @Data
    public static class CustomerInfo {
        private Long id;
        private String name;
        private String phone;
        private String email;
    }
}
