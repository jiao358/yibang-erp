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
    private Long companyId;
    private String companyName;
    private String tierName;
    private String tierCode;
    private String tierType;
    private Integer tierLevel;
    private String description;
    private BigDecimal discountRate;
    private BigDecimal markupRate;
    private BigDecimal minOrderAmount;
    private BigDecimal maxOrderAmount;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;
    private String customerLevelRequirement;
    private String paymentTerms;
    private BigDecimal creditLimit;
    private LocalDateTime effectiveStart;
    private LocalDateTime effectiveEnd;
    private Boolean isActive;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String createdByName;
    private Long updatedBy;
    private String updatedByName;
}
