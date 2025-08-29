package com.yibang.erp.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * AI订单处理请求DTO
 */
@Data
public class AIOrderProcessRequest {
    
    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    
    /**
     * 公司ID
     */
    @NotNull(message = "公司ID不能为空")
    private Long companyId;
    
    /**
     * 处理类型
     */
    @NotNull(message = "处理类型不能为空")
    private String processType;
    
    /**
     * 处理描述
     */
    @Size(max = 1000, message = "处理描述长度不能超过1000字符")
    private String description;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 优先级
     */
    private Integer priority = 1;
    
    /**
     * 自定义参数
     */
    private String customParams;
}
