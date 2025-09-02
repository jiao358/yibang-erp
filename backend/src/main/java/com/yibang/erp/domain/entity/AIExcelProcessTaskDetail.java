package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * AI Excel处理任务详情实体
 * 记录每行的处理结果
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ai_excel_process_task_details")
public class AIExcelProcessTaskDetail {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 行号
     */
    @TableField("excel_row_number")
    private Integer ExcelRowNumber;

    /**
     * 处理状态
     */
    @TableField("process_status")
    private String processStatus; // SUCCESS, FAILED, MANUAL_PROCESS, SKIPPED

    /**
     * 原始数据（JSON格式）
     */
    @TableField("raw_data")
    private String rawData;

    /**
     * 提取的数据（JSON格式）
     */
    @TableField("extracted_data")
    private String extractedData;

    /**
     * 处理结果（JSON格式）
     */
    @TableField("process_result")
    private String processResult;

    /**
     * 置信度
     */
    @TableField("confidence")
    private Double confidence;

    /**
     * 错误类型
     */
    @TableField("error_type")
    private String errorType; // VALIDATION, PRODUCT_MATCH, CUSTOMER_MATCH, SYSTEM

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 处理建议
     */
    @TableField("suggestion")
    private String suggestion;

    /**
     * 处理备注
     */
    @TableField("processing_notes")
    private String processingNotes;

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
}
