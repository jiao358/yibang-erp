// 销售目标类型定义
export interface SalesTarget {
  id?: number
  targetName: string
  targetCode: string
  description?: string
  targetType: string
  targetValue: number
  currentValue?: number
  completionRate?: number
  unit: string
  period: string
  periodStart: string
  periodEnd: string
  status: string
  priority: number
  isActive: boolean
  categoryId?: number
  customerType?: string
  ownerId: number
  companyId: number
  createdBy: number
  createdAt?: string
  updatedBy?: number
  updatedAt?: string
}

export interface SalesTargetResponse {
  id: number
  targetName: string
  targetCode: string
  description?: string
  targetType: string
  targetTypeDescription: string
  targetValue: number
  currentValue: number
  completionRate: number
  unit: string
  period: string
  periodDescription: string
  periodStart: string
  periodEnd: string
  status: string
  statusDescription: string
  priority: number
  isActive: boolean
  categoryId?: number
  categoryName?: string
  customerType?: string
  ownerId: number
  ownerName?: string
  companyId: number
  companyName?: string
  createdBy: number
  createdByName?: string
  createdAt: string
  updatedBy?: number
  updatedByName?: string
  updatedAt?: string
}

export interface SalesTargetQueryRequest {
  targetName?: string
  targetCode?: string
  targetType?: string
  status?: string
  isActive?: boolean
  categoryId?: number
  customerType?: string
  ownerId?: number
  targetValueFrom?: number
  targetValueTo?: number
  completionRateFrom?: number
  completionRateTo?: number
  priorityFrom?: number
  priorityTo?: number
  periodStartFrom?: string
  periodStartTo?: string
  periodEndFrom?: string
  periodEndTo?: string
  createdAtFrom?: string
  createdAtTo?: string
  companyId: number
  page: number
  size: number
}

export interface SalesTargetForm {
  targetName: string
  targetCode: string
  description: string
  targetType: string
  targetValue: number
  unit: string
  period: string
  periodStart: string
  periodEnd: string
  status: string
  priority: number
  isActive: boolean
  categoryId: number | null
  customerType: string
  ownerId: number
}

export interface SalesTargetTypeOption {
  value: string
  label: string
  description: string
}

export interface SalesTargetPeriodOption {
  value: string
  label: string
  description: string
}

export interface SalesTargetStatusOption {
  value: string
  label: string
  description: string
}

export interface TargetProgressUpdate {
  targetId: number
  currentValue: number
}

export interface TargetCompletionStatistics {
  totalCount: number
  completedCount: number
  activeCount: number
  draftCount: number
  cancelledCount: number
  completionRate: number
}

export interface TargetTypeStatistics {
  totalCount: number
  typeDistribution: Record<string, number>
  statusDistribution: Record<string, number>
}
