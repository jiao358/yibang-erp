package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单人工处理表实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("order_manual_processing")
public class OrderManualProcessing {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("source_order_id")
    private String sourceOrderId;

    @TableField("processing_type")
    private String processingType;

    @TableField("processing_reason")
    private String processingReason;

    @TableField("platform_order_id")
    private String platformOrderId;

    @TableField("supplier_company_id")
    private Long supplierCompanyId;

    @TableField("original_data")
    private String originalData;

    @TableField("request_data")
    private String requestData;

    @TableField("status")
    private String status;

    @TableField("priority")
    private String priority;

    @TableField("assigned_to")
    private Long assignedTo;

    @TableField("processed_by")
    private Long processedBy;

    @TableField("processed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;

    @TableField("processing_notes")
    private String processingNotes;

    @TableField("rejection_reason")
    private String rejectionReason;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    // 处理类型常量
    public static final String TYPE_ORDER_CLOSE = "ORDER_CLOSE";
    public static final String TYPE_ADDRESS_CHANGE = "ADDRESS_CHANGE";
    public static final String TYPE_REFUND = "REFUND";
    public static final String TYPE_CANCEL = "CANCEL";

    // 状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_REJECTED = "REJECTED";

    // 优先级常量
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";
}
