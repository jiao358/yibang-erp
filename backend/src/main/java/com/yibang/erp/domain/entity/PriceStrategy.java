package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格策略实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("price_strategies")
public class PriceStrategy {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 策略名称
     */
    @TableField("strategy_name")
    private String strategyName;

    /**
     * 策略代码
     */
    @TableField("strategy_code")
    private String strategyCode;

    /**
     * 策略描述
     */
    @TableField("description")
    private String description;

    /**
     * 策略类型：DISCOUNT(折扣), MARKUP(加价), FIXED(固定价格), DYNAMIC(动态定价)
     */
    @TableField("strategy_type")
    private String strategyType;

    /**
     * 基础价格
     */
    @TableField("base_price")
    private BigDecimal basePrice;

    /**
     * 调整比例
     */
    @TableField("adjustment_rate")
    private BigDecimal adjustmentRate;

    /**
     * 调整金额
     */
    @TableField("adjustment_amount")
    private BigDecimal adjustmentAmount;

    /**
     * 最小价格
     */
    @TableField("min_price")
    private BigDecimal minPrice;

    /**
     * 最大价格
     */
    @TableField("max_price")
    private BigDecimal maxPrice;

    /**
     * 适用条件（JSON格式）
     */
    @TableField("conditions")
    private String conditions;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 适用商品分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 适用客户类型
     */
    @TableField("customer_type")
    private String customerType;

    /**
     * 生效开始时间
     */
    @TableField("effective_start")
    private LocalDateTime effectiveStart;

    /**
     * 生效结束时间
     */
    @TableField("effective_end")
    private LocalDateTime effectiveEnd;

    /**
     * 公司ID
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;
}
