package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品价格分层配置实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product_price_tier_configs")
public class ProductPriceTierConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 价格分层ID
     */
    @TableField("price_tier_id")
    private Long priceTierId;

    /**
     * 一件代发价格
     */
    @TableField("dropshipping_price")
    private BigDecimal dropshippingPrice;

    /**
     * 零售限价
     */
    @TableField("retail_limit_price")
    private BigDecimal retailLimitPrice;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

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
    private Boolean isDeleted;
}
