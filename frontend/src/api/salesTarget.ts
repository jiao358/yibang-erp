import request from '@/utils/request'
import type { 
  SalesTarget, 
  SalesTargetResponse, 
  SalesTargetQueryRequest, 
  SalesTargetForm,
  TargetProgressUpdate,
  TargetCompletionStatistics,
  TargetTypeStatistics
} from '@/types/salesTarget'

// 销售目标管理API
export const salesTargetApi = {
  // 创建销售目标
  createSalesTarget(data: SalesTargetForm) {
    return request<SalesTargetResponse>({
      url: '/api/sales-targets',
      method: 'post',
      data
    })
  },

  // 更新销售目标
  updateSalesTarget(id: number, data: SalesTargetForm) {
    return request<SalesTargetResponse>({
      url: `/api/sales-targets/${id}`,
      method: 'put',
      data
    })
  },

  // 删除销售目标
  deleteSalesTarget(id: number) {
    return request<boolean>({
      url: `/api/sales-targets/${id}`,
      method: 'delete'
    })
  },

  // 根据ID获取销售目标
  getSalesTargetById(id: number) {
    return request<SalesTargetResponse>({
      url: `/api/sales-targets/${id}`,
      method: 'get'
    })
  },

  // 分页查询销售目标
  getSalesTargetPage(params: SalesTargetQueryRequest) {
    return request<{
      records: SalesTargetResponse[]
      total: number
      current: number
      size: number
    }>({
      url: '/api/sales-targets/page',
      method: 'get',
      params
    })
  },

  // 查询销售目标列表
  getSalesTargetList(params: SalesTargetQueryRequest) {
    return request<SalesTargetResponse[]>({
      url: '/api/sales-targets/list',
      method: 'get',
      params
    })
  },

  // 启用/禁用销售目标
  toggleSalesTargetStatus(id: number, isActive: boolean) {
    return request<boolean>({
      url: `/api/sales-targets/${id}/status`,
      method: 'put',
      params: { isActive }
    })
  },

  // 批量启用/禁用销售目标
  batchToggleSalesTargetStatus(ids: number[], isActive: boolean) {
    return request<boolean>({
      url: '/api/sales-targets/batch-status',
      method: 'put',
      params: { ids, isActive }
    })
  },

  // 更新目标进度
  updateTargetProgress(id: number, currentValue: number) {
    return request<boolean>({
      url: `/api/sales-targets/${id}/progress`,
      method: 'put',
      params: { currentValue }
    })
  },

  // 批量更新目标进度
  batchUpdateTargetProgress(ids: number[], currentValues: number[]) {
    return request<boolean>({
      url: '/api/sales-targets/batch-progress',
      method: 'put',
      params: { ids, currentValues }
    })
  },

  // 激活销售目标
  activateSalesTarget(id: number) {
    return request<boolean>({
      url: `/api/sales-targets/${id}/activate`,
      method: 'put'
    })
  },

  // 完成销售目标
  completeSalesTarget(id: number) {
    return request<boolean>({
      url: `/api/sales-targets/${id}/complete`,
      method: 'put'
    })
  },

  // 取消销售目标
  cancelSalesTarget(id: number) {
    return request<boolean>({
      url: `/api/sales-targets/${id}/cancel`,
      method: 'put'
    })
  },

  // 重置销售目标
  resetSalesTarget(id: number) {
    return request<boolean>({
      url: `/api/sales-targets/${id}/reset`,
      method: 'put'
    })
  },

  // 获取适用的销售目标
  getApplicableSalesTargets(categoryId: number | null, customerType: string | null, companyId: number) {
    return request<SalesTargetResponse[]>({
      url: '/api/sales-targets/applicable',
      method: 'get',
      params: { categoryId, customerType, companyId }
    })
  },

  // 获取目标完成统计信息
  getTargetCompletionStatistics(companyId: number) {
    return request<TargetCompletionStatistics>({
      url: '/api/sales-targets/completion-statistics',
      method: 'get',
      params: { companyId }
    })
  },

  // 获取目标类型统计信息
  getTargetTypeStatistics(companyId: number) {
    return request<TargetTypeStatistics>({
      url: '/api/sales-targets/type-statistics',
      method: 'get',
      params: { companyId }
    })
  },

  // 验证销售目标配置
  validateSalesTargetConfig(data: SalesTargetForm) {
    return request<boolean>({
      url: '/api/sales-targets/validate',
      method: 'post',
      data
    })
  }
}

// 销售目标类型选项
export const salesTargetTypeOptions = [
  { value: 'REVENUE', label: '营收目标', description: '销售额或收入目标' },
  { value: 'QUANTITY', label: '数量目标', description: '销售数量目标' },
  { value: 'CUSTOMER', label: '客户数目标', description: '新增客户数量目标' },
  { value: 'PRODUCT', label: '产品目标', description: '产品销售目标' }
]

// 销售目标周期选项
export const salesTargetPeriodOptions = [
  { value: 'DAILY', label: '日', description: '每日目标' },
  { value: 'WEEKLY', label: '周', description: '每周目标' },
  { value: 'MONTHLY', label: '月', description: '每月目标' },
  { value: 'QUARTERLY', label: '季', description: '每季度目标' },
  { value: 'YEARLY', label: '年', description: '年度目标' }
]

// 销售目标状态选项
export const salesTargetStatusOptions = [
  { value: 'DRAFT', label: '草稿', description: '目标草稿状态' },
  { value: 'ACTIVE', label: '激活', description: '目标激活状态' },
  { value: 'COMPLETED', label: '完成', description: '目标已完成' },
  { value: 'CANCELLED', label: '取消', description: '目标已取消' }
]

// 客户类型选项
export const customerTypeOptions = [
  { value: 'INDIVIDUAL', label: '个人客户', description: '个人消费者' },
  { value: 'ENTERPRISE', label: '企业客户', description: '企业客户' },
  { value: 'WHOLESALE', label: '批发客户', description: '批发商客户' },
  { value: 'VIP', label: 'VIP客户', description: 'VIP客户' }
]
