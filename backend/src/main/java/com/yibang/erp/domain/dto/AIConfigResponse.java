package com.yibang.erp.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI配置响应DTO
 */
@Data
public class AIConfigResponse {
    
    /**
     * 配置ID
     */
    private Long configId;
    
    /**
     * DeepSeek API基础URL
     */
    private String baseUrl;
    
    /**
     * DeepSeek API密钥（脱敏显示）
     */
    private String apiKeyMasked;
    
    /**
     * 默认模型名称
     */
    private String defaultModel;
    
    /**
     * 最大token数
     */
    private Integer maxTokens;
    
    /**
     * 温度参数
     */
    private Double temperature;
    
    /**
     * 是否启用AI功能
     */
    private Boolean enabled;
    
    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout;
    
    /**
     * 配置状态
     */
    private String configStatus;
    
    /**
     * 最后测试时间
     */
    private LocalDateTime lastTestTime;
    
    /**
     * 最后测试结果
     */
    private Boolean lastTestResult;
    
    /**
     * 配置用户ID
     */
    private Long configUserId;
    
    /**
     * 配置用户名
     */
    private String configUserName;
    
    /**
     * 配置时间
     */
    private LocalDateTime configTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 是否配置完整
     */
    private boolean isConfigured;
    
    /**
     * 配置说明
     */
    private String configDescription;
}
