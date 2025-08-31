package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 商品库存实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product_inventory")
public class ProductInventory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 可用库存
     */
    @TableField("available_quantity")
    private Integer availableQuantity;

    /**
     * 预留库存
     */
    @TableField("reserved_quantity")
    private Integer reservedQuantity;

    /**
     * 损坏库存
     */
    @TableField("damaged_quantity")
    private Integer damagedQuantity;

    /**
     * 最低库存预警线
     */
    @TableField("min_stock_level")
    private Integer minStockLevel;

    /**
     * 最高库存上限
     */
    @TableField("max_stock_level")
    private Integer maxStockLevel;

    /**
     * 补货点
     */
    @TableField("reorder_point")
    private Integer reorderPoint;

    /**
     * 最后入库时间
     */
    @TableField("last_stock_in")
    private LocalDateTime lastStockIn;

    /**
     * 最后出库时间
     */
    @TableField("last_stock_out")
    private LocalDateTime lastStockOut;

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
     * 计算总库存
     */
    public Integer getTotalQuantity() {
        return (availableQuantity != null ? availableQuantity : 0) +
               (reservedQuantity != null ? reservedQuantity : 0) +
               (damagedQuantity != null ? damagedQuantity : 0);
    }

    /**
     * 检查是否需要补货
     */
    public boolean needsReorder() {
        return reorderPoint != null && availableQuantity != null && availableQuantity <= reorderPoint;
    }

    /**
     * 检查是否库存不足
     */
    public boolean isLowStock() {
        return minStockLevel != null && availableQuantity != null && availableQuantity <= minStockLevel;
    }

    /**
     * 检查是否库存过高
     */
    public boolean isOverStock() {
        return maxStockLevel != null && availableQuantity != null && availableQuantity >= maxStockLevel;
    }
}
