package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Excel导入记录实体 - 基于现有数据库表结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("excel_import_logs")
public class ExcelImportLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 导入状态
     */
    @TableField("import_status")
    private String importStatus; // PENDING, PROCESSING, COMPLETED, FAILED

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
     * 成功行数
     */
    @TableField("success_rows")
    private Integer successRows;

    /**
     * 失败行数
     */
    @TableField("failed_rows")
    private Integer failedRows;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * AI处理状态
     */
    @TableField("ai_processing_status")
    private String aiProcessingStatus; // PENDING, PROCESSING, COMPLETED, FAILED

    /**
     * AI处理结果
     */
    @TableField("ai_processing_result")
    private String aiProcessingResult;

    /**
     * 导入用户ID
     */
    @TableField("import_user_id")
    private Long importUserId;

    /**
     * 开始时间
     */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

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

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private Map<String, Object> extendedFields;

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String importUserName; // 导入用户姓名，用于前端显示

    /**
     * 获取扩展字段Map
     */
    public Map<String, Object> getExtendedFieldsMap() {
        if (extendedFields == null) {
            return Map.of();
        }
        return extendedFields;
    }

    /**
     * 设置扩展字段Map
     */
    public void setExtendedFieldsMap(Map<String, Object> fields) {
        this.extendedFields = fields;
    }
}
