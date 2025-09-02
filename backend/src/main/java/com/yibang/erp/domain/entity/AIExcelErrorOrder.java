package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI Excel错误订单实体
 * 用于存储Excel导入过程中出现错误的订单信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_excel_error_orders")
public class AIExcelErrorOrder {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 处理任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * Excel行号
     */
    @TableField("excel_row_number")
    private Integer excelRowNumber;

    /**
     * 原始Excel行数据（JSON格式）
     */
    @TableField("raw_data")
    private String rawData;

    /**
     * 错误类型
     */
    @TableField("error_type")
    private String errorType;

    /**
     * 详细错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 错误级别
     */
    @TableField("error_level")
    private String errorLevel;

    /**
     * 建议处理方式
     */
    @TableField("suggested_action")
    private String suggestedAction;

    /**
     * 处理状态：PENDING-待处理, PROCESSED-已处理, IGNORED-已忽略
     */
    @TableField("status")
    private String status;

    /**
     * 处理人ID
     */
    @TableField("processed_by")
    private Long processedBy;

    /**
     * 处理时间
     */
    @TableField("processed_at")
    private LocalDateTime processedAt;

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
     * 获取原始数据的Map形式
     */
    public Map<String, Object> getRawDataMap() {
        // 这里可以使用Jackson或其他JSON工具解析rawData
        // 暂时返回null，具体实现可以在Service层处理
        return null;
    }

    /**
     * 设置原始数据
     */
    public void setRawDataMap(Map<String, Object> rawDataMap) {
        // 这里可以将Map转换为JSON字符串
        // 暂时留空，具体实现可以在Service层处理
    }
}
