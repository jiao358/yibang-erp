package com.yibang.erp.domain.dto;

import com.yibang.erp.domain.entity.TaskHistoryItem;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务列表响应DTO
 */
@Data
public class TaskListResponse {
    
    /**
     * 任务列表
     */
    private List<TaskInfo> content;
    
    /**
     * 总元素数
     */
    private Long totalElements;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 当前页码
     */
    private Integer currentPage;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 响应状态
     */
    private String status;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 任务信息内部类
     */
    @Data
    public static class TaskInfo {
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
        private String createdAt;
        
        /**
         * 完成时间
         */
        private String completedAt;
        
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
        private String startedAt;
        
        /**
         * 需手动处理的行数
         */
        private Integer manualProcessRows;
    }
    
    /**
     * 创建成功响应
     */
    public static TaskListResponse success() {
        TaskListResponse response = new TaskListResponse();
        response.setStatus("SUCCESS");
        response.setMessage("获取任务列表成功");
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static TaskListResponse error(String message) {
        TaskListResponse response = new TaskListResponse();
        response.setStatus("ERROR");
        response.setMessage(message);
        return response;
    }
}
