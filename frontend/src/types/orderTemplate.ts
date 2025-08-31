// 订单模板请求类型
export interface OrderTemplateRequest {
  id?: number
  templateName: string
  version: string
  description?: string
  isActive: boolean
  effectiveDate?: string
  deprecatedDate?: string
  coreFieldsConfig?: Record<string, any>
  extendedFieldsConfig?: Record<string, any>
  businessRulesConfig?: Record<string, any>
  fieldMappingConfig?: Record<string, any>
}

// 订单模板响应类型
export interface OrderTemplateResponse {
  id: number
  templateName: string
  version: string
  description?: string
  isActive: boolean
  effectiveDate?: string
  deprecatedDate?: string
  coreFieldsConfig?: Record<string, any>
  extendedFieldsConfig?: Record<string, any>
  businessRulesConfig?: Record<string, any>
  fieldMappingConfig?: Record<string, any>
  createdAt: string
  updatedAt: string
  createdBy?: number
  updatedBy?: number
  createdByName?: string
  updatedByName?: string
}

// 订单模板字段配置类型
export interface FieldConfig {
  fieldName: string
  fieldType: string
  required: boolean
  defaultValue?: any
  validationRules?: string[]
  displayName: string
  description?: string
  order: number
}

// 业务规则配置类型
export interface BusinessRuleConfig {
  ruleName: string
  ruleType: 'VALIDATION' | 'TRANSFORMATION' | 'BUSINESS_LOGIC'
  ruleExpression: string
  errorMessage?: string
  priority: number
  enabled: boolean
}

// 字段映射配置类型
export interface FieldMappingConfig {
  sourceField: string
  targetField: string
  transformationRule?: string
  required: boolean
  defaultValue?: any
}
