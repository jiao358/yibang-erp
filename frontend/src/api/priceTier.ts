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
    return request.post<PriceTierResponse>('/api/price-tiers', data)
  },

  // 更新价格分层
  updatePriceTier(id: number, data: PriceTierForm) {
    return request.put<PriceTierResponse>(`/api/price-tiers/${id}`, data)
  },

  // 删除价格分层
  deletePriceTier(id: number) {
    return request.delete<boolean>(`/api/price-tiers/${id}`)
  },

  // 根据ID获取价格分层
  getPriceTierById(id: number) {
    return request.get<PriceTierResponse>(`/api/price-tiers/${id}`)
  },

  // 分页查询价格分层
  getPriceTierPage(params: PriceTierQueryRequest) {
    return request.get<{
      records: PriceTierResponse[]
      total: number
      current: number
      size: number
    }>('/api/price-tiers/page', { params })
  },

  // 查询价格分层列表
  getPriceTierList(params: PriceTierQueryRequest) {
    return request.get<PriceTierResponse[]>('/api/price-tiers/list', { params })
  },

  // 启用/禁用价格分层
  togglePriceTierStatus(id: number, isActive: boolean) {
    return request.put<boolean>(`/api/price-tiers/${id}/status`, null, { params: { isActive } })
  },

  // 批量启用/禁用价格分层
  batchTogglePriceTierStatus(ids: number[], isActive: boolean) {
    return request.put<boolean>('/api/price-tiers/batch-status', null, { params: { ids, isActive } })
  },

  // 获取适用的价格分层
  getApplicablePriceTiers(categoryId: number | null, customerType: string | null, companyId: number) {
    return request.get<PriceTierResponse[]>('/api/price-tiers/applicable', { params: { categoryId, customerType, companyId } })
  },

  // 计算最终价格
  calculateFinalPrice(priceTierId: number, originalPrice: number) {
    return request.post<number>('/api/price-tiers/calculate-price', null, { params: { priceTierId, originalPrice } })
  },

  // 验证价格分层配置
  validatePriceTierConfig(data: PriceTierForm) {
    return request.post<boolean>('/api/price-tiers/validate', data)
  }
}

// 价格分层类型选项
export const priceTierTypeOptions = [
  { value: 'DEALER_LEVEL_1', label: '1级经销商', description: '一级经销商价格分层' },
  { value: 'DEALER_LEVEL_2', label: '2级经销商', description: '二级经销商价格分层' },
  { value: 'DEALER_LEVEL_3', label: '3级经销商', description: '三级经销商价格分层' },
  { value: 'VIP_CUSTOMER', label: 'VIP客户', description: 'VIP客户价格分层' },
  { value: 'WHOLESALE', label: '批发客户', description: '批发客户价格分层' },
  { value: 'RETAIL', label: '零售客户', description: '零售客户价格分层' }
]

// 客户类型选项
export const customerTypeOptions = [
  { value: 'INDIVIDUAL', label: '个人客户', description: '个人消费者' },
  { value: 'ENTERPRISE', label: '企业客户', description: '企业客户' },
  { value: 'WHOLESALE', label: '批发客户', description: '批发商客户' },
  { value: 'VIP', label: 'VIP客户', description: 'VIP客户' }
]
