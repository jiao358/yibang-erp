package com.yibang.erp.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单模板响应DTO
 */
@Data
public class OrderTemplateResponse {

    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板版本
     */
    private String version;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 是否启用
     */
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

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 更新人姓名
     */
    private String updatedByName;
}
