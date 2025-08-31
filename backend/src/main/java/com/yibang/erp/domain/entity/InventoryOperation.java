package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存操作记录实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("inventory_operations")
public class InventoryOperation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作单号
     */
    @TableField("operation_no")
    private String operationNo;

    /**
     * 操作类型：STOCK_IN-入库，STOCK_OUT-出库，ADJUSTMENT-调整，TRANSFER-调拨，CHECK-盘点
     */
    @TableField("operation_type")
    private String operationType;

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
     * 操作数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 操作前可用库存
     */
    @TableField("before_available_qty")
    private Integer beforeAvailableQuantity;

    /**
     * 操作后可用库存
     */
    @TableField("after_available_qty")
    private Integer afterAvailableQuantity;

    /**
     * 操作前预留库存
     */
    @TableField("before_reserved_qty")
    private Integer beforeReservedQuantity;

    /**
     * 操作后预留库存
     */
    @TableField("after_reserved_qty")
    private Integer afterReservedQuantity;

    /**
     * 操作前损坏库存
     */
    @TableField("before_damaged_qty")
    private Integer beforeDamagedQuantity;

    /**
     * 操作后损坏库存
     */
    @TableField("after_damaged_qty")
    private Integer afterDamagedQuantity;

    /**
     * 单价
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 关联订单ID（如果是订单相关操作）
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 关联订单项ID
     */
    @TableField("order_item_id")
    private Long orderItemId;

    /**
     * 操作原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

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
}
