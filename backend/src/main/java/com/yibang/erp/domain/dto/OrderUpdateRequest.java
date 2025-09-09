package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单更新请求DTO
 */
@Data
public class OrderUpdateRequest {

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 订单类型
     */
    private String orderType;

    /**
     * 预期交货日期
     */
    private String expectedDeliveryDate;

    /**
     * 货币
     */
    private String currency;

    /**
     * 特殊要求
     */
    private String specialRequirements;

    /**
     * 送货地址
     */
    private String deliveryAddress;

    /**
     * 送货联系人
     */
    private String deliveryContact;

    /**
     * 送货电话
     */
    private String deliveryPhone;

    /**
     * 源订单号
     */
    private String sourceOrderId;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 运费
     */
    private BigDecimal shippingAmount;

    /**
     * 税费
     */
    private BigDecimal taxAmount;

    /**
     * 最终金额
     */
    private BigDecimal finalAmount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 订单备注
     */
    private String remarks;

    /**
     * 扩展字段（动态字段）
     */
    private Map<String, Object> extendedFields;

    /**
     * 订单商品列表
     */
    private List<OrderItemUpdateRequest> orderItems;

    /**
     * 订单商品更新请求DTO
     */
    @Data
    public static class OrderItemUpdateRequest {
        
        /**
         * 订单商品ID（新增时为null）
         */
        private Long id;

        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /**
         * 商品数量
         */
        @NotNull(message = "商品数量不能为空")
        private Integer quantity;

        /**
         * 商品单价
         */
        @NotNull(message = "商品单价不能为空")
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
