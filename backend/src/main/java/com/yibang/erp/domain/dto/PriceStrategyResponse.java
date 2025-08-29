package com.yibang.erp.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格策略响应DTO
 */
@Data
public class PriceStrategyResponse {

    private Long id;
    private String strategyName;
    private String strategyCode;
    private String description;
    private String strategyType;
    private String strategyTypeDescription;
    private BigDecimal basePrice;
    private BigDecimal adjustmentRate;
    private BigDecimal adjustmentAmount;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String conditions;
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
