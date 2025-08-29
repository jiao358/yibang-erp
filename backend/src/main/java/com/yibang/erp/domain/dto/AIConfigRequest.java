package com.yibang.erp.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * AI配置请求DTO
 */
@Data
public class AIConfigRequest {
    
    /**
     * DeepSeek API基础URL
     */
    @Size(max = 500, message = "API基础URL长度不能超过500字符")
    private String baseUrl;
    
    /**
     * DeepSeek API密钥
     */
    @NotNull(message = "API密钥不能为空")
    @Size(min = 10, max = 1000, message = "API密钥长度必须在10到1000字符之间")
    private String apiKey;
    
    /**
     * 默认模型名称
     */
    @Size(max = 100, message = "模型名称长度不能超过100字符")
    private String defaultModel;
    
    /**
     * 最大token数
     */
    @Min(value = 100, message = "最大token数不能少于100")
    @Max(value = 100000, message = "最大token数不能超过100000")
    private Integer maxTokens;
    
    /**
     * 温度参数
     */
    @Min(value = 0, message = "温度参数不能小于0")
    @Max(value = 2, message = "温度参数不能大于2")
    private Double temperature;
    
    /**
     * 是否启用AI功能
     */
    private Boolean enabled;
    
    /**
     * 请求超时时间（毫秒）
     */
    @Min(value = 5000, message = "超时时间不能少于5000毫秒")
    @Max(value = 300000, message = "超时时间不能超过300000毫秒")
    private Integer timeout;
    
    /**
     * 配置用户ID（只有系统管理员可以配置）
     */
    @NotNull(message = "配置用户ID不能为空")
    private Long configUserId;
}
