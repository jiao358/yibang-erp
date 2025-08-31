package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 订单审核日志实体 - 基于现有数据库表结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_approval_logs")
public class OrderApprovalLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 操作类型
     */
    @TableField("action")
    private String action; // SUBMIT, APPROVE, REJECT, MODIFY

    /**
     * 审核状态
     */
    @TableField("status")
    private String status; // PENDING, APPROVED, REJECTED

    /**
     * 审核意见
     */
    @TableField("comment")
    private String comment;

    /**
     * 审核人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审核时间
     */
    @TableField("approval_at")
    private LocalDateTime approvalAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String platformOrderId; // 平台订单号，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String approverName; // 审核人姓名，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String approverRole; // 审核人角色，用于前端显示
}
