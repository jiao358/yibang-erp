package com.yibang.erp.domain.dto.microservice;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建请求DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class OrderCreateRequest {
    
    /**
     * 源订单号
     */
    @NotBlank(message = "源订单号不能为空")
    private String sourceOrderId;
    
    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    
    /**
     * 订单项列表
     */
    @NotEmpty(message = "订单项不能为空")
    @Valid
    private List<OrderItemRequest> orderItems;
    
    /**
     * 收货地址
     */
    @NotNull(message = "收货地址不能为空")
    @Valid
    private OrderAddressRequest address;
    
    /**
     * 备注
     */
    private String remarks;
    
    /**
     * 订单项请求
     */
    @Data
    public static class OrderItemRequest {
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        
        @NotNull(message = "数量不能为空")
        private Integer quantity;
        
        @NotNull(message = "单价不能为空")
        private BigDecimal unitPrice;
    }
    
    /**
     * 订单地址请求
     */
    @Data
    public static class OrderAddressRequest {
        @NotBlank(message = "收货人姓名不能为空")
        private String receiverName;
        
        @NotBlank(message = "收货人电话不能为空")
        private String receiverPhone;
        
        @NotBlank(message = "省份不能为空")
        private String province;
        
        @NotBlank(message = "城市不能为空")
        private String city;
        
        @NotBlank(message = "区县不能为空")
        private String district;
        
        @NotBlank(message = "详细地址不能为空")
        private String address;
        
        private String postalCode;
    }
}
