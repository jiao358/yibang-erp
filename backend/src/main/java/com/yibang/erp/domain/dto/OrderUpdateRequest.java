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
