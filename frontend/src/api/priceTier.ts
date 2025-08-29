import request from '@/utils/request'
import type { 
  PriceTier, 
  PriceTierResponse, 
  PriceTierQueryRequest, 
  PriceTierForm,
  PriceCalculationRequest,
  PriceCalculationResponse
} from '@/types/priceTier'

// 价格分层管理API
export const priceTierApi = {
  // 创建价格分层
  createPriceTier(data: PriceTierForm) {
    return request<PriceTierResponse>({
      url: '/api/price-tiers',
      method: 'post',
      data
    })
  },

  // 更新价格分层
  updatePriceTier(id: number, data: PriceTierForm) {
    return request<PriceTierResponse>({
      url: `/api/price-tiers/${id}`,
      method: 'put',
      data
    })
  },

  // 删除价格分层
  deletePriceTier(id: number) {
    return request<boolean>({
      url: `/api/price-tiers/${id}`,
      method: 'delete'
    })
  },

  // 根据ID获取价格分层
  getPriceTierById(id: number) {
    return request<PriceTierResponse>({
      url: `/api/price-tiers/${id}`,
      method: 'get'
    })
  },

  // 分页查询价格分层
  getPriceTierPage(params: PriceTierQueryRequest) {
    return request<{
      records: PriceTierResponse[]
      total: number
      current: number
      size: number
    }>({
      url: '/api/price-tiers/page',
      method: 'get',
      params
    })
  },

  // 查询价格分层列表
  getPriceTierList(params: PriceTierQueryRequest) {
    return request<PriceTierResponse[]>({
      url: '/api/price-tiers/list',
      method: 'get',
      params
    })
  },

  // 启用/禁用价格分层
  togglePriceTierStatus(id: number, isActive: boolean) {
    return request<boolean>({
      url: `/api/price-tiers/${id}/status`,
      method: 'put',
      params: { isActive }
    })
  },

  // 批量启用/禁用价格分层
  batchTogglePriceTierStatus(ids: number[], isActive: boolean) {
    return request<boolean>({
      url: '/api/price-tiers/batch-status',
      method: 'put',
      params: { ids, isActive }
    })
  },

  // 获取适用的价格分层
  getApplicablePriceTiers(categoryId: number | null, customerType: string | null, companyId: number) {
    return request<PriceTierResponse[]>({
      url: '/api/price-tiers/applicable',
      method: 'get',
      params: { categoryId, customerType, companyId }
    })
  },

  // 计算最终价格
  calculateFinalPrice(priceTierId: number, originalPrice: number) {
    return request<number>({
      url: '/api/price-tiers/calculate-price',
      method: 'post',
      params: { priceTierId, originalPrice }
    })
  },

  // 验证价格分层配置
  validatePriceTierConfig(data: PriceTierForm) {
    return request<boolean>({
      url: '/api/price-tiers/validate',
      method: 'post',
      data
    })
  }
}

// 价格分层类型选项
export const priceTierTypeOptions = [
  { value: 'BASIC', label: '基础价格', description: '基础价格分层' },
  { value: 'VIP', label: 'VIP价格', description: 'VIP客户价格分层' },
  { value: 'WHOLESALE', label: '批发价格', description: '批发客户价格分层' },
  { value: 'CUSTOM', label: '自定义价格', description: '自定义价格分层' }
]

// 客户类型选项
export const customerTypeOptions = [
  { value: 'INDIVIDUAL', label: '个人客户', description: '个人消费者' },
  { value: 'ENTERPRISE', label: '企业客户', description: '企业客户' },
  { value: 'WHOLESALE', label: '批发客户', description: '批发商客户' },
  { value: 'VIP', label: 'VIP客户', description: 'VIP客户' }
]
