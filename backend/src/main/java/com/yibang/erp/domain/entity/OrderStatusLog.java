package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 订单状态日志实体 - 基于现有数据库表结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_status_logs")
public class OrderStatusLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 变更前状态
     */
    @TableField("from_status")
    private String fromStatus;

    /**
     * 变更后状态
     */
    @TableField("to_status")
    private String toStatus;

    /**
     * 变更原因
     */
    @TableField("change_reason")
    private String changeReason;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作类型
     */
    @TableField("operator_type")
    private String operatorType; // SYSTEM, USER, AI

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
    private String operatorName; // 操作人姓名，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String operatorRole; // 操作人角色，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private LocalDateTime operatedAt; // 操作时间，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private Boolean isSystemOperation; // 是否系统操作，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String operationSource; // 操作来源，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String remarks; // 备注，用于前端显示
}
