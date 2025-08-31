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
     * 分层级别
     */
    private Integer tierLevel;

    /**
     * 是否激活
     */
    private Boolean isActive;

    /**
     * 客户等级要求
     */
    private String customerLevelRequirement;

    /**
     * 折扣率范围
     */
    private BigDecimal discountRateFrom;
    private BigDecimal discountRateTo;

    /**
     * 加价率范围
     */
    private BigDecimal markupRateFrom;
    private BigDecimal markupRateTo;

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
