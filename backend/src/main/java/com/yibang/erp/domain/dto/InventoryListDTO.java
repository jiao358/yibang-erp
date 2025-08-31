package com.yibang.erp.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 库存列表DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@Accessors(chain = true)
public class InventoryListDTO {

    /**
     * 库存ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编码
     */
    private String productSku;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品单位
     */
    private String productUnit;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 可用库存
     */
    private Integer availableQuantity;

    /**
     * 预留库存
     */
    private Integer reservedQuantity;

    /**
     * 损坏库存
     */
    private Integer damagedQuantity;

    /**
     * 最低库存预警线
     */
    private Integer minStockLevel;

    /**
     * 最高库存上限
     */
    private Integer maxStockLevel;

    /**
     * 补货点
     */
    private Integer reorderPoint;

    /**
     * 最后入库时间
     */
    private LocalDateTime lastStockIn;

    /**
     * 最后出库时间
     */
    private LocalDateTime lastStockOut;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
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
