package com.yibang.erp.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * AI Excel订单处理请求DTO
 */
@Data
public class AIExcelProcessRequest {

    /**
     * 销售用户ID
     */
    @NotNull(message = "销售用户ID不能为空")
    private Long salesUserId;

    /**
     * 销售公司ID
     */
    @NotNull(message = "销售公司ID不能为空")
    private Long salesCompanyId;

    /**
     * 处理优先级 (1-5, 1最高)
     */
    private Integer priority = 3;

    /**
     * 是否启用AI商品匹配
     */
    private Boolean enableAIProductMatching = true;

    /**
     * 最小置信度阈值 (0.0-1.0)
     */
    private Double minConfidenceThreshold = 0.7;

    /**
     * 是否自动创建不存在的客户
     */
    private Boolean autoCreateCustomer = false;

    /**
     * 是否自动创建不存在的商品
     */
    private Boolean autoCreateProduct = false;

    /**
     * 处理备注
     */
    private String remarks;

    /**
     * 扩展参数 (JSON格式)
     */
    private String extendedParams;

    private String operatorName;
}
