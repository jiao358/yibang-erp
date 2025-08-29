// 价格策略类型定义
export interface PriceStrategy {
  id?: number
  strategyName: string
  strategyCode: string
  description?: string
  strategyType: string
  basePrice?: number
  adjustmentRate?: number
  adjustmentAmount?: number
  minPrice?: number
  maxPrice?: number
  conditions?: string
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

export interface PriceStrategyResponse {
  id: number
  strategyName: string
  strategyCode: string
  description?: string
  strategyType: string
  strategyTypeDescription: string
  basePrice?: number
  adjustmentRate?: number
  adjustmentAmount?: number
  minPrice?: number
  maxPrice?: number
  conditions?: string
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

export interface PriceStrategyQueryRequest {
  strategyName?: string
  strategyCode?: string
  strategyType?: string
  isActive?: boolean
  categoryId?: number
  customerType?: string
  basePriceFrom?: number
  basePriceTo?: number
  adjustmentRateFrom?: number
  adjustmentRateTo?: number
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

export interface PriceStrategyForm {
  strategyName: string
  strategyCode: string
  description: string
  strategyType: string
  basePrice: number | null
  adjustmentRate: number | null
  adjustmentAmount: number | null
  minPrice: number | null
  maxPrice: number | null
  conditions: string
  priority: number
  isActive: boolean
  categoryId: number | null
  customerType: string
  effectiveStart: string
  effectiveEnd: string
}

export interface PriceStrategyTypeOption {
  value: string
  label: string
  description: string
}

export interface StrategyApplicationRequest {
  strategyId: number
  originalPrice: number
}

export interface StrategyApplicationResponse {
  finalPrice: number
  adjustmentAmount: number
  adjustmentRate: number
}

export interface StrategyStatistics {
  totalCount: number
  typeDistribution: Record<string, number>
  statusDistribution: Record<string, number>
}
