package com.yibang.erp.domain.dto;

import lombok.Data;

/**
 * 任务统计响应DTO
 */
@Data
public class TaskStatisticsResponse {
    
    /**
     * 总任务数
     */
    private Long totalTasks;
    
    /**
     * 处理中的任务数
     */
    private Long processingTasks;
    
    /**
     * 已完成的任务数
     */
    private Long completedTasks;
    
    /**
     * 失败的任务数
     */
    private Long failedTasks;
    
    /**
     * 今天的任务数
     */
    private Long todayTasks;
    
    /**
     * 本周的任务数
     */
    private Long thisWeekTasks;
    
    /**
     * 本月的任务数
     */
    private Long thisMonthTasks;
    
    /**
     * 响应状态
     */
    private String status;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 创建成功响应
     */
    public static TaskStatisticsResponse success() {
        TaskStatisticsResponse response = new TaskStatisticsResponse();
        response.setStatus("SUCCESS");
        response.setMessage("获取统计信息成功");
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static TaskStatisticsResponse error(String message) {
        TaskStatisticsResponse response = new TaskStatisticsResponse();
        response.setStatus("ERROR");
        response.setMessage(message);
        return response;
    }
}
