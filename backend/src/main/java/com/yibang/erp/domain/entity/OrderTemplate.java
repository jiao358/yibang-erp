package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单模板实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_templates")
public class OrderTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板版本
     */
    @TableField("version")
    private String version;

    /**
     * 模板描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 生效时间
     */
    @TableField("effective_date")
    private LocalDateTime effectiveDate;

    /**
     * 废弃时间
     */
    @TableField("deprecated_date")
    private LocalDateTime deprecatedDate;

    /**
     * 核心字段配置（JSON格式）
     */
    @TableField("core_fields_config")
    private String coreFieldsConfig;

    /**
     * 扩展字段配置（JSON格式）
     */
    @TableField("extended_fields_config")
    private String extendedFieldsConfig;

    /**
     * 业务规则配置（JSON格式）
     */
    @TableField("business_rules_config")
    private String businessRulesConfig;

    /**
     * 字段映射配置（JSON格式）
     */
    @TableField("field_mapping_config")
    private String fieldMappingConfig;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取核心字段配置
     */
    public List<TemplateField> getCoreFields() {
        if (coreFieldsConfig == null || coreFieldsConfig.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(coreFieldsConfig, new TypeReference<List<TemplateField>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    /**
     * 设置核心字段配置
     */
    public void setCoreFields(List<TemplateField> fields) {
        try {
            this.coreFieldsConfig = objectMapper.writeValueAsString(fields);
        } catch (JsonProcessingException e) {
            this.coreFieldsConfig = "[]";
        }
    }

    /**
     * 获取扩展字段配置
     */
    public List<TemplateField> getExtendedFields() {
        if (extendedFieldsConfig == null || extendedFieldsConfig.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(extendedFieldsConfig, new TypeReference<List<TemplateField>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    /**
     * 设置扩展字段配置
     */
    public void setExtendedFields(List<TemplateField> fields) {
        try {
            this.extendedFieldsConfig = objectMapper.writeValueAsString(fields);
        } catch (JsonProcessingException e) {
            this.extendedFieldsConfig = "[]";
        }
    }

    /**
     * 获取业务规则配置
     */
    public List<BusinessRule> getBusinessRules() {
        if (businessRulesConfig == null || businessRulesConfig.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(businessRulesConfig, new TypeReference<List<BusinessRule>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    /**
     * 设置业务规则配置
     */
    public void setBusinessRules(List<BusinessRule> rules) {
        try {
            this.businessRulesConfig = objectMapper.writeValueAsString(rules);
        } catch (JsonProcessingException e) {
            this.businessRulesConfig = "[]";
        }
    }

    /**
     * 获取字段映射配置
     */
    public Map<String, String> getFieldMapping() {
        if (fieldMappingConfig == null || fieldMappingConfig.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(fieldMappingConfig, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    /**
     * 设置字段映射配置
     */
    public void setFieldMapping(Map<String, String> mapping) {
        try {
            this.fieldMappingConfig = objectMapper.writeValueAsString(mapping);
        } catch (JsonProcessingException e) {
            this.fieldMappingConfig = "{}";
        }
    }

    /**
     * 模板字段内部类
     */
    @Data
    public static class TemplateField {
        private String fieldName;
        private String displayName;
        private String fieldType; // STRING, NUMBER, DATE, BOOLEAN, OBJECT, ARRAY
        private Boolean isRequired;
        private Object defaultValue;
        private String validationRule;
        private String description;
        private Integer displayOrder;
        private Boolean isEditable;
        private String groupName; // 字段分组
    }

    /**
     * 业务规则内部类
     */
    @Data
    public static class BusinessRule {
        private String ruleName;
        private String ruleType; // VALIDATION, CALCULATION, TRANSITION
        private String targetField;
        private String ruleExpression;
        private String errorMessage;
        private Integer priority;
        private Boolean isActive;
    }
}
