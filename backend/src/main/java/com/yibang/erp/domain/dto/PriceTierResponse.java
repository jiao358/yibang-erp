package com.yibang.erp.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格分层响应DTO
 */
@Data
public class PriceTierResponse {

    private Long id;
    private String tierName;
    private String tierCode;
    private String description;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal discountRate;
    private BigDecimal fixedDiscount;
    private String tierType;
    private String tierTypeDescription;
    private Integer priority;
    private Boolean isActive;
    private Long categoryId;
    private String categoryName;
    private String customerType;
    private LocalDateTime effectiveStart;
    private LocalDateTime effectiveEnd;
    private Long companyId;
    private String companyName;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
