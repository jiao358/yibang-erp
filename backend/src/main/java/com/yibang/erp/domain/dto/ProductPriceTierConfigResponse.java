package com.yibang.erp.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品价格分层配置响应DTO
 */
@Data
public class ProductPriceTierConfigResponse {

    private Long id;
    private Long productId;
    private Long priceTierId;
    private String priceTierName;
    private String priceTierType;
    private BigDecimal dropshippingPrice;
    private BigDecimal retailLimitPrice;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
