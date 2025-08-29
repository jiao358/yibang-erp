package com.yibang.erp.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * DeepSeek聊天请求DTO
 */
@Data
public class DeepSeekChatRequest {
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 消息列表
     */
    private String[] messages;
    
    /**
     * 最大token数
     */
    private Integer maxTokens;
    
    /**
     * 温度参数
     */
    private Double temperature;
    
    /**
     * 是否流式响应
     */
    private Boolean stream = false;
    
    /**
     * 停止词
     */
    private List<String> stop;
    
    /**
     * 存在惩罚
     */
    private Double presencePenalty;
    
    /**
     * 频率惩罚
     */
    private Double frequencyPenalty;
    
    /**
     * 用户标识
     */
    private String user;
}
