package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售目标实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sales_targets")
public class SalesTarget {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目标名称
     */
    @TableField("target_name")
    private String targetName;

    /**
     * 目标代码
     */
    @TableField("target_code")
    private String targetCode;

    /**
     * 目标描述
     */
    @TableField("description")
    private String description;

    /**
     * 目标类型：REVENUE(营收), QUANTITY(数量), CUSTOMER(客户数), PRODUCT(产品)
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标值
     */
    @TableField("target_value")
    private BigDecimal targetValue;

    /**
     * 当前值
     */
    @TableField("current_value")
    private BigDecimal currentValue;

    /**
     * 完成率
     */
    @TableField("completion_rate")
    private BigDecimal completionRate;

    /**
     * 目标单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 目标周期：DAILY(日), WEEKLY(周), MONTHLY(月), QUARTERLY(季), YEARLY(年)
     */
    @TableField("period")
    private String period;

    /**
     * 周期开始时间
     */
    @TableField("period_start")
    private LocalDateTime periodStart;

    /**
     * 周期结束时间
     */
    @TableField("period_end")
    private LocalDateTime periodEnd;

    /**
     * 目标状态：DRAFT(草稿), ACTIVE(激活), COMPLETED(完成), CANCELLED(取消)
     */
    @TableField("status")
    private String status;

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
     * 负责人ID
     */
    @TableField("owner_id")
    private Long ownerId;

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
