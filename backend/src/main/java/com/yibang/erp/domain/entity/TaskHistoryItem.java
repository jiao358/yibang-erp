package com.yibang.erp.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务历史项实体类
 */
@Data
public class TaskHistoryItem {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 任务状态
     */
    private String status;
    
    /**
     * 总行数
     */
    private Integer totalRows;
    
    /**
     * 成功行数
     */
    private Integer successRows;
    
    /**
     * 失败行数
     */
    private Integer failedRows;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
    
    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;
    
    /**
     * 供应商
     */
    private String supplier;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 上传用户
     */
    private String uploadUser;
    
    /**
     * 开始处理时间
     */
    private LocalDateTime startedAt;
    
    /**
     * 需手动处理的行数
     */
    private Integer manualProcessRows;
}
