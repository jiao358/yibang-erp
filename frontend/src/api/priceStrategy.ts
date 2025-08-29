import request from '@/utils/request'
import type { 
  PriceStrategy, 
  PriceStrategyResponse, 
  PriceStrategyQueryRequest, 
  PriceStrategyForm,
  StrategyApplicationRequest,
  StrategyApplicationResponse,
  StrategyStatistics
} from '@/types/priceStrategy'

// 价格策略管理API
export const priceStrategyApi = {
  // 创建价格策略
  createPriceStrategy(data: PriceStrategyForm) {
    return request<PriceStrategyResponse>({
      url: '/api/price-strategies',
      method: 'post',
      data
    })
  },

  // 更新价格策略
  updatePriceStrategy(id: number, data: PriceStrategyForm) {
    return request<PriceStrategyResponse>({
      url: `/api/price-strategies/${id}`,
      method: 'put',
      data
    })
  },

  // 删除价格策略
  deletePriceStrategy(id: number) {
    return request<boolean>({
      url: `/api/price-strategies/${id}`,
      method: 'delete'
    })
  },

  // 根据ID获取价格策略
  getPriceStrategyById(id: number) {
    return request<PriceStrategyResponse>({
      url: `/api/price-strategies/${id}`,
      method: 'get'
    })
  },

  // 分页查询价格策略
  getPriceStrategyPage(params: PriceStrategyQueryRequest) {
    return request<{
      records: PriceStrategyResponse[]
      total: number
      current: number
      size: number
    }>({
      url: '/api/price-strategies/page',
      method: 'get',
      params
    })
  },

  // 查询价格策略列表
  getPriceStrategyList(params: PriceStrategyQueryRequest) {
    return request<PriceStrategyResponse[]>({
      url: '/api/price-strategies/list',
      method: 'get',
      params
    })
  },

  // 启用/禁用价格策略
  togglePriceStrategyStatus(id: number, isActive: boolean) {
    return request<boolean>({
      url: `/api/price-strategies/${id}/status`,
      method: 'put',
      params: { isActive }
    })
  },

  // 批量启用/禁用价格策略
  batchTogglePriceStrategyStatus(ids: number[], isActive: boolean) {
    return request<boolean>({
      url: '/api/price-strategies/batch-status',
      method: 'put',
      params: { ids, isActive }
    })
  },

  // 获取适用的价格策略
  getApplicablePriceStrategies(categoryId: number | null, customerType: string | null, companyId: number) {
    return request<PriceStrategyResponse[]>({
      url: '/api/price-strategies/applicable',
      method: 'get',
      params: { categoryId, customerType, companyId }
    })
  },

  // 应用价格策略计算最终价格
  applyPriceStrategy(strategyId: number, originalPrice: number) {
    return request<number>({
      url: '/api/price-strategies/apply',
      method: 'post',
      params: { strategyId, originalPrice }
    })
  },

  // 批量应用价格策略
  batchApplyPriceStrategies(strategyIds: number[], originalPrices: number[]) {
    return request<number[]>({
      url: '/api/price-strategies/batch-apply',
      method: 'post',
      params: { strategyIds, originalPrices }
    })
  },

  // 验证价格策略配置
  validatePriceStrategyConfig(data: PriceStrategyForm) {
    return request<boolean>({
      url: '/api/price-strategies/validate',
      method: 'post',
      data
    })
  },

  // 获取策略类型统计信息
  getStrategyTypeStatistics(companyId: number) {
    return request<StrategyStatistics>({
      url: '/api/price-strategies/statistics',
      method: 'get',
      params: { companyId }
    })
  }
}

// 价格策略类型选项
export const priceStrategyTypeOptions = [
  { value: 'DISCOUNT', label: '折扣策略', description: '按比例或金额进行折扣' },
  { value: 'MARKUP', label: '加价策略', description: '按比例或金额进行加价' },
  { value: 'FIXED', label: '固定价格', description: '设置固定的最终价格' },
  { value: 'DYNAMIC', label: '动态定价', description: '根据条件动态调整价格' }
]

// 客户类型选项
export const customerTypeOptions = [
  { value: 'INDIVIDUAL', label: '个人客户', description: '个人消费者' },
  { value: 'ENTERPRISE', label: '企业客户', description: '企业客户' },
  { value: 'WHOLESALE', label: '批发客户', description: '批发商客户' },
  { value: 'VIP', label: 'VIP客户', description: 'VIP客户' }
]
