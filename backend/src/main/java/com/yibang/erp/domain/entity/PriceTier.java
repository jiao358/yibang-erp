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
     * 公司ID
     */
    @TableField("company_id")
    private Long companyId;

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
     * 分层类型：DEALER_LEVEL_1, DEALER_LEVEL_2, VIP_CUSTOMER等
     */
    @TableField("tier_type")
    private String tierType;

    /**
     * 分层级别（数字越小级别越高）
     */
    @TableField("tier_level")
    private Integer tierLevel;

    /**
     * 分层描述
     */
    @TableField("description")
    private String description;

    /**
     * 折扣率（1.00表示无折扣）
     */
    @TableField("discount_rate")
    private BigDecimal discountRate;

    /**
     * 加价率（1.00表示无加价）
     */
    @TableField("markup_rate")
    private BigDecimal markupRate;

    /**
     * 最小订单金额
     */
    @TableField("min_order_amount")
    private BigDecimal minOrderAmount;

    /**
     * 最大订单金额
     */
    @TableField("max_order_amount")
    private BigDecimal maxOrderAmount;

    /**
     * 最小订单数量
     */
    @TableField("min_order_quantity")
    private Integer minOrderQuantity;

    /**
     * 最大订单数量
     */
    @TableField("max_order_quantity")
    private Integer maxOrderQuantity;

    /**
     * 客户等级要求
     */
    @TableField("customer_level_requirement")
    private String customerLevelRequirement;

    /**
     * 付款条件
     */
    @TableField("payment_terms")
    private String paymentTerms;

    /**
     * 信用额度
     */
    @TableField("credit_limit")
    private BigDecimal creditLimit;

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
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 优先级（数字越大优先级越高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 是否删除
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
