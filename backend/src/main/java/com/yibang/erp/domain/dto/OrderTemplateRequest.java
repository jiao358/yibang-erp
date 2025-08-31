package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单模板请求DTO
 */
@Data
public class OrderTemplateRequest {

    private Long id;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /**
     * 模板版本
     */
    @NotBlank(message = "模板版本不能为空")
    private String version;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 是否启用
     */
    @NotNull(message = "是否启用不能为空")
    private Boolean isActive;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveDate;

    /**
     * 废弃时间
     */
    private LocalDateTime deprecatedDate;

    /**
     * 核心字段配置
     */
    private Map<String, Object> coreFieldsConfig;

    /**
     * 扩展字段配置
     */
    private Map<String, Object> extendedFieldsConfig;

    /**
     * 业务规则配置
     */
    private Map<String, Object> businessRulesConfig;

    /**
     * 字段映射配置
     */
    private Map<String, Object> fieldMappingConfig;
}
