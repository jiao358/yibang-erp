package com.yibang.erp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类
 * 只有系统管理员可以配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.deepseek")
public class AIConfig {
    
    /**
     * DeepSeek API基础URL
     */
    private String baseUrl = "https://api.deepseek.com";
    
    /**
     * DeepSeek API密钥
     */
    private String apiKey = "sk-3f69c84d8322422498438badd98cdac3";
    
    /**
     * 默认模型名称
     */
    private String defaultModel = "deepseek-chat";
    
    /**
     * 最大token数
     */
    private Integer maxTokens = 4096;
    
    /**
     * 温度参数
     */
    private Double temperature = 0.7;
    
    /**
     * 是否启用AI功能
     */
    private Boolean enabled = true;
    
    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;
}
