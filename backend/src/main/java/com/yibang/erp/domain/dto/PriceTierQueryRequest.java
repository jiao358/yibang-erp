package com.yibang.erp.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格分层查询DTO
 */
@Data
public class PriceTierQueryRequest {

    /**
     * 分层名称（模糊查询）
     */
    private String tierName;

    /**
     * 分层代码（精确查询）
     */
    private String tierCode;

    /**
     * 分层类型
     */
    private String tierType;

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
     * 最低价格范围
     */
    private BigDecimal minPriceFrom;
    private BigDecimal minPriceTo;

    /**
     * 最高价格范围
     */
    private BigDecimal maxPriceFrom;
    private BigDecimal maxPriceTo;

    /**
     * 折扣率范围
     */
    private BigDecimal discountRateFrom;
    private BigDecimal discountRateTo;

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
