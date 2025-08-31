// 价格分层类型定义
export interface PriceTier {
  id?: number
  companyId: number
  companyName?: string
  tierName: string
  tierCode: string
  tierType: string
  tierLevel: number
  description?: string
  discountRate?: number
  markupRate?: number
  minOrderAmount?: number
  maxOrderAmount?: number
  minOrderQuantity?: number
  maxOrderQuantity?: number
  customerLevelRequirement?: string
  paymentTerms?: string
  creditLimit?: number
  effectiveStart?: string
  effectiveEnd?: string
  isActive: boolean
  priority: number
  createdAt?: string
  updatedAt?: string
  createdBy: number
  createdByName?: string
  updatedBy?: number
  updatedByName?: string
}

export interface PriceTierResponse {
  id: number
  companyId: number
  companyName?: string
  tierName: string
  tierCode: string
  tierType: string
  tierLevel: number
  description?: string
  discountRate?: number
  markupRate?: number
  minOrderAmount?: number
  maxOrderAmount?: number
  minOrderQuantity?: number
  maxOrderQuantity?: number
  customerLevelRequirement?: string
  paymentTerms?: string
  creditLimit?: number
  effectiveStart?: string
  effectiveEnd?: string
  isActive: boolean
  priority: number
  createdAt: string
  updatedAt?: string
  createdBy: number
  createdByName?: string
  updatedBy?: number
  updatedByName?: string
}

export interface PriceTierQueryRequest {
  tierName?: string
  tierCode?: string
  tierType?: string
  tierLevel?: number
  isActive?: boolean
  customerLevelRequirement?: string
  discountRateFrom?: number
  discountRateTo?: number
  markupRateFrom?: number
  markupRateTo?: number
  priorityFrom?: number
  priorityTo?: number
  effectiveStartFrom?: string
  effectiveStartTo?: string
  createdAtFrom?: string
  createdAtTo?: string
  companyId: number
  page: number
  size: number
}

export interface PriceTierForm {
  tierName: string
  tierCode: string
  tierType: string
  tierLevel: number
  description: string
  discountRate?: number
  markupRate?: number
  minOrderAmount?: number
  maxOrderAmount?: number
  minOrderQuantity?: number
  maxOrderQuantity?: number
  customerLevelRequirement?: string
  paymentTerms?: string
  creditLimit?: number
  effectiveStart?: string
  effectiveEnd?: string
  isActive: boolean
  priority: number
  companyId: number
  createdBy: number
}

export interface PriceTierTypeOption {
  value: string
  label: string
  description: string
}

export interface PriceCalculationRequest {
  priceTierId: number
  originalPrice: number
}

export interface PriceCalculationResponse {
  finalPrice: number
  discountAmount: number
  discountRate: number
}
