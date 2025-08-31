package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存盘点明细实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("inventory_check_items")
public class InventoryCheckItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 盘点单ID
     */
    @TableField("check_id")
    private Long checkId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 仓库ID
     */
    @TableField("warehouse_id")
    private Long warehouseId;

    /**
     * 系统库存数量
     */
    @TableField("system_quantity")
    private Integer systemQuantity;

    /**
     * 实际盘点数量
     */
    @TableField("actual_quantity")
    private Integer actualQuantity;

    /**
     * 差异数量（实际 - 系统）
     */
    @TableField("difference_quantity")
    private Integer differenceQuantity;

    /**
     * 差异金额
     */
    @TableField("difference_amount")
    private BigDecimal differenceAmount;

    /**
     * 盘点状态：PENDING-待盘点，CHECKED-已盘点，VERIFIED-已验证
     */
    @TableField("status")
    private String status;

    /**
     * 盘点人ID
     */
    @TableField("checker_id")
    private Long checkerId;

    /**
     * 盘点时间
     */
    @TableField("checked_at")
    private LocalDateTime checkedAt;

    /**
     * 验证人ID
     */
    @TableField("verifier_id")
    private Long verifierId;

    /**
     * 验证时间
     */
    @TableField("verified_at")
    private LocalDateTime verifiedAt;

    /**
     * 差异原因
     */
    @TableField("difference_reason")
    private String differenceReason;

    /**
     * 处理方式：ADJUST-调整库存，INVESTIGATE-调查原因，IGNORE-忽略差异
     */
    @TableField("handling_method")
    private String handlingMethod;

    /**
     * 处理备注
     */
    @TableField("handling_remark")
    private String handlingRemark;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
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
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 计算差异数量
     */
    public void calculateDifference() {
        if (systemQuantity != null && actualQuantity != null) {
            this.differenceQuantity = actualQuantity - systemQuantity;
        }
    }

    /**
     * 检查是否有差异
     */
    public boolean hasDifference() {
        return differenceQuantity != null && differenceQuantity != 0;
    }
}
