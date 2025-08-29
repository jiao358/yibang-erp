package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格分层实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("price_tiers")
public class PriceTier {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分层名称
     */
    @TableField("tier_name")
    private String tierName;

    /**
     * 分层代码
     */
    @TableField("tier_code")
    private String tierCode;

    /**
     * 分层描述
     */
    @TableField("description")
    private String description;

    /**
     * 最低价格
     */
    @TableField("min_price")
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    @TableField("max_price")
    private BigDecimal maxPrice;

    /**
     * 折扣率
     */
    @TableField("discount_rate")
    private BigDecimal discountRate;

    /**
     * 固定折扣金额
     */
    @TableField("fixed_discount")
    private BigDecimal fixedDiscount;

    /**
     * 分层类型：BASIC(基础), VIP(VIP), WHOLESALE(批发), CUSTOM(自定义)
     */
    @TableField("tier_type")
    private String tierType;

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
