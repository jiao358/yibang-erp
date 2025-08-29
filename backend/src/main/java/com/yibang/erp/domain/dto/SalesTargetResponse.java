package com.yibang.erp.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售目标响应DTO
 */
@Data
public class SalesTargetResponse {

    private Long id;
    private String targetName;
    private String targetCode;
    private String description;
    private String targetType;
    private String targetTypeDescription;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private BigDecimal completionRate;
    private String unit;
    private String period;
    private String periodDescription;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String status;
    private String statusDescription;
    private Integer priority;
    private Boolean isActive;
    private Long categoryId;
    private String categoryName;
    private String customerType;
    private Long ownerId;
    private String ownerName;
    private Long companyId;
    private String companyName;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
