package com.yibang.erp.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售目标查询DTO
 */
@Data
public class SalesTargetQueryRequest {

    /**
     * 目标名称（模糊查询）
     */
    private String targetName;

    /**
     * 目标代码（精确查询）
     */
    private String targetCode;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 目标状态
     */
    private String status;

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
     * 负责人ID
     */
    private Long ownerId;

    /**
     * 目标值范围
     */
    private BigDecimal targetValueFrom;
    private BigDecimal targetValueTo;

    /**
     * 完成率范围
     */
    private BigDecimal completionRateFrom;
    private BigDecimal completionRateTo;

    /**
     * 优先级范围
     */
    private Integer priorityFrom;
    private Integer priorityTo;

    /**
     * 周期开始时间范围
     */
    private LocalDateTime periodStartFrom;
    private LocalDateTime periodStartTo;

    /**
     * 周期结束时间范围
     */
    private LocalDateTime periodEndFrom;
    private LocalDateTime periodEndTo;

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
