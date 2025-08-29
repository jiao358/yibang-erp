// 价格分层类型定义
export interface PriceTier {
  id?: number
  tierName: string
  tierCode: string
  description?: string
  minPrice?: number
  maxPrice?: number
  discountRate?: number
  fixedDiscount?: number
  tierType: string
  priority: number
  isActive: boolean
  categoryId?: number
  customerType?: string
  effectiveStart?: string
  effectiveEnd?: string
  companyId: number
  createdBy: number
  createdAt?: string
  updatedBy?: number
  updatedAt?: string
}

export interface PriceTierResponse {
  id: number
  tierName: string
  tierCode: string
  description?: string
  minPrice?: number
  maxPrice?: number
  discountRate?: number
  fixedDiscount?: number
  tierType: string
  tierTypeDescription: string
  priority: number
  isActive: boolean
  categoryId?: number
  categoryName?: string
  customerType?: string
  effectiveStart?: string
  effectiveEnd?: string
  companyId: number
  companyName?: string
  createdBy: number
  createdByName?: string
  createdAt: string
  updatedBy?: number
  updatedByName?: string
  updatedAt?: string
}

export interface PriceTierQueryRequest {
  tierName?: string
  tierCode?: string
  tierType?: string
  isActive?: boolean
  categoryId?: number
  customerType?: string
  minPriceFrom?: number
  minPriceTo?: number
  maxPriceFrom?: number
  maxPriceTo?: number
  discountRateFrom?: number
  discountRateTo?: number
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
  description: string
  minPrice: number | null
  maxPrice: number | null
  discountRate: number | null
  fixedDiscount: number | null
  tierType: string
  priority: number
  isActive: boolean
  categoryId: number | null
  customerType: string
  effectiveStart: string
  effectiveEnd: string
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
