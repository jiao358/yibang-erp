package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息处理日志实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("message_processing_log")
public class MessageProcessingLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息ID
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 源订单号
     */
    @TableField("source_order_id")
    private String sourceOrderId;

    /**
     * 销售订单ID
     */
    @TableField("sales_order_id")
    private String salesOrderId;

    /**
     * 幂等性键（来自电商平台的Idempotency-Key）
     */
    @TableField("idempotency_key")
    private String idempotencyKey;

    /**
     * 业务类型：orders.create.q, orders.logistics.q, orders.address.q, orders.status.q, orders.dlq
     */
    @TableField("bus_type")
    private String busType;

    /**
     * 处理状态：PROCESSING, SUCCESS, FAILED, DUPLICATE, DEAD_LETTER
     */
    @TableField("status")
    private String status;

    /**
     * 处理结果消息
     */
    @TableField("result_message")
    private String resultMessage;

    /**
     * 全部信息
     */
    @TableField("total_message")
    private String totalMessage;



    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

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
     * 处理完成时间
     */
    @TableField("processed_at")
    private LocalDateTime processedAt;

    /**
     * 是否删除
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
