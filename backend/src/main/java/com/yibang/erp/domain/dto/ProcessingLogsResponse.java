package com.yibang.erp.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 处理日志查询接口响应DTO
 */
@Data
public class ProcessingLogsResponse {
    
    /**
     * 日志列表
     */
    private List<LogInfo> content;
    
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
     * 日志信息内部类
     */
    @Data
    public static class LogInfo {
        /**
         * 日志ID
         */
        private Long id;
        
        /**
         * 任务ID
         */
        private String taskId;
        
        /**
         * 时间戳
         */
        private String timestamp;
        
        /**
         * 日志级别
         */
        private String level; // INFO, WARN, ERROR, DEBUG
        
        /**
         * 日志消息
         */
        private String message;
        
        /**
         * 详细信息
         */
        private String details;
        
        /**
         * Excel行号（如果相关）
         */
        private Integer excelRowNumber;
        
        /**
         * 处理步骤
         */
        private String step;
        
        /**
         * 执行时间（毫秒）
         */
        private Long executionTime;
        
        /**
         * 创建时间
         */
        private String createdAt;
    }
    
    /**
     * 创建成功响应
     */
    public static ProcessingLogsResponse success() {
        ProcessingLogsResponse response = new ProcessingLogsResponse();
        response.setStatus("SUCCESS");
        response.setMessage("查询成功");
        return response;
    }
    
    /**
     * 创建错误响应
     */
    public static ProcessingLogsResponse error(String message) {
        ProcessingLogsResponse response = new ProcessingLogsResponse();
        response.setStatus("ERROR");
        response.setMessage(message);
        return response;
    }
}
