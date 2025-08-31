package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存盘点实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("inventory_checks")
public class InventoryCheck {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 盘点单号
     */
    @TableField("check_no")
    private String checkNo;

    /**
     * 盘点类型：FULL-全面盘点，PARTIAL-部分盘点，CYCLE-周期盘点，RANDOM-随机盘点
     */
    @TableField("check_type")
    private String checkType;

    /**
     * 仓库ID
     */
    @TableField("warehouse_id")
    private Long warehouseId;

    /**
     * 盘点状态：PLANNED-已计划，IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消
     */
    @TableField("status")
    private String status;

    /**
     * 计划开始时间
     */
    @TableField("planned_start_time")
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间
     */
    @TableField("planned_end_time")
    private LocalDateTime plannedEndTime;

    /**
     * 实际开始时间
     */
    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 盘点人ID
     */
    @TableField("checker_id")
    private Long checkerId;

    /**
     * 审核人ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝
     */
    @TableField("review_status")
    private String reviewStatus;

    /**
     * 审核意见
     */
    @TableField("review_comment")
    private String reviewComment;

    /**
     * 盘点说明
     */
    @TableField("description")
    private String description;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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
