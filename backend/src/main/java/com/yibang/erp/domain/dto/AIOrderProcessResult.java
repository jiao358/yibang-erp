package com.yibang.erp.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI订单处理结果DTO
 */
@Data
public class AIOrderProcessResult {
    
    /**
     * 处理ID
     */
    private Long processId;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 公司ID
     */
    private Long companyId;
    
    /**
     * 处理类型
     */
    private String processType;
    
    /**
     * 处理状态
     */
    private String status;
    
    /**
     * 处理结果
     */
    private String result;
    
    /**
     * 处理描述
     */
    private String description;
    
    /**
     * AI模型名称
     */
    private String modelName;
    
    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 处理开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 处理完成时间
     */
    private LocalDateTime endTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 自定义参数
     */
    private String customParams;
}
