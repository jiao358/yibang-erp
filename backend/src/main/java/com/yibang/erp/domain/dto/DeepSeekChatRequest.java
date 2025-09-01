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
//    private String[] messages;


    private DeepSeekMessage[] messages;

    public static class DeepSeekMessage {
        private String role;
        private String content;

        // constructors
        public DeepSeekMessage() {}

        public DeepSeekMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // getters and setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }




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
