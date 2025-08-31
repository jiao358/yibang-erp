package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单创建请求DTO
 */
@Data
public class OrderCreateRequest {

    /**
     * 销售订单ID（客户提供的订单号，仅作参考）
     */
    @NotBlank(message = "销售订单ID不能为空")
    private String salesOrderId;

    /**
     * 销售人ID
     */
    @NotNull(message = "销售人ID不能为空")
    private Long salesUserId;

    /**
     * 销售公司ID
     */
    @NotNull(message = "销售公司ID不能为空")
    private Long salesCompanyId;

    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /**
     * 订单来源
     */
    @NotBlank(message = "订单来源不能为空")
    private String source; // MANUAL, EXCEL, IMAGE, API, PDF, WORD, EMAIL, VOICE

    /**
     * 模板版本
     */
    private String templateVersion = "1.0";

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
    @NotNull(message = "订单商品列表不能为空")
    private List<OrderItemRequest> orderItems;

    /**
     * 订单商品请求DTO
     */
    @Data
    public static class OrderItemRequest {
        
        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /**
         * 商品数量
         */
        @NotNull(message = "商品数量不能为空")
        @Min(value = 1, message = "商品数量必须大于0")
        private Integer quantity;

        /**
         * 商品单价
         */
        @NotNull(message = "商品单价不能为空")
        @DecimalMin(value = "0.01", message = "商品单价必须大于0")
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
