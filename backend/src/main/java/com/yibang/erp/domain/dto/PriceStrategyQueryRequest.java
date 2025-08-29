package com.yibang.erp.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格策略查询DTO
 */
@Data
public class PriceStrategyQueryRequest {

    /**
     * 策略名称（模糊查询）
     */
    private String strategyName;

    /**
     * 策略代码（精确查询）
     */
    private String strategyCode;

    /**
     * 策略类型
     */
    private String strategyType;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 基础价格范围
     */
    private BigDecimal basePriceFrom;
    private BigDecimal basePriceTo;

    /**
     * 调整比例范围
     */
    private BigDecimal adjustmentRateFrom;
    private BigDecimal adjustmentRateTo;

    /**
     * 优先级范围
     */
    private Integer priorityFrom;
    private Integer priorityTo;

    /**
     * 生效时间范围
     */
    private LocalDateTime effectiveStartFrom;
    private LocalDateTime effectiveStartTo;

    /**
     * 创建时间范围
     */
    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 分页参数
     */
    private Integer page = 1;
    private Integer size = 10;
}
