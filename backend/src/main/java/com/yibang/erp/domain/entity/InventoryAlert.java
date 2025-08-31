package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 库存预警实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("inventory_alerts")
public class InventoryAlert {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 预警编号
     */
    @TableField("alert_no")
    private String alertNo;

    /**
     * 预警类型：LOW_STOCK-库存不足，OVER_STOCK-库存过高，EXPIRY-即将过期，DAMAGED-损坏库存
     */
    @TableField("alert_type")
    private String alertType;

    /**
     * 预警级别：LOW-低，MEDIUM-中，HIGH-高，CRITICAL-紧急
     */
    @TableField("alert_level")
    private String alertLevel;

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
     * 当前库存数量
     */
    @TableField("current_quantity")
    private Integer currentQuantity;

    /**
     * 预警阈值
     */
    @TableField("threshold_quantity")
    private Integer thresholdQuantity;

    /**
     * 预警内容
     */
    @TableField("alert_content")
    private String alertContent;

    /**
     * 预警状态：PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决，IGNORED-已忽略
     */
    @TableField("status")
    private String status;

    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理时间
     */
    @TableField("handled_at")
    private LocalDateTime handledAt;

    /**
     * 处理结果
     */
    @TableField("handling_result")
    private String handlingResult;

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
}
