package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI Excel处理任务实体
 * 完全独立于现有的Excel导入日志
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ai_excel_process_tasks")
public class AIExcelProcessTask {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID（业务唯一标识）
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件哈希值
     */
    @TableField("file_hash")
    private String fileHash;

    /**
     * 任务状态
     */
    @TableField("task_status")
    private String taskStatus; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED

    /**
     * 销售用户ID
     */
    @TableField("sales_user_id")
    private Long salesUserId;

    /**
     * 销售公司ID
     */
    @TableField("sales_company_id")
    private Long salesCompanyId;

    /**
     * 处理优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 总行数
     */
    @TableField("total_rows")
    private Integer totalRows;

    /**
     * 已处理行数
     */
    @TableField("processed_rows")
    private Integer processedRows;

    /**
     * 成功处理行数
     */
    @TableField("success_rows")
    private Integer successRows;

    /**
     * 失败处理行数
     */
    @TableField("failed_rows")
    private Integer failedRows;

    /**
     * 需要人工处理行数
     */
    @TableField("manual_process_rows")
    private Integer manualProcessRows;

    /**
     * 平均置信度
     */
    @TableField("average_confidence")
    private Double averageConfidence;

    /**
     * 当前处理步骤
     */
    @TableField("current_step")
    private String currentStep;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 处理参数（JSON格式）
     */
    @TableField("process_params")
    private String processParams;

    /**
     * 处理结果（JSON格式）
     */
    @TableField("process_result")
    private String processResult;

    /**
     * 任务创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 任务开始时间
     */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /**
     * 任务完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /**
     * 任务更新时间
     */
    @TableField("updated_at")
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
     * 是否删除
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
