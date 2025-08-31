import request from '@/utils/request'
import type { 
  ProductPriceTierConfigRequest, 
  ProductPriceTierConfigResponse 
} from '@/types/product'

/**
 * 商品价格分层配置API
 */
export const productPriceConfigApi = {
  /**
   * 创建价格分层配置
   */
  createConfig(data: ProductPriceTierConfigRequest): Promise<ProductPriceTierConfigResponse> {
    return request.post<ProductPriceTierConfigResponse>('/api/product-price-configs', data)
  },

  /**
   * 更新价格分层配置
   */
  updateConfig(id: number, data: ProductPriceTierConfigRequest): Promise<ProductPriceTierConfigResponse> {
    return request.put<ProductPriceTierConfigResponse>(`/api/product-price-configs/${id}`, data)
  },

  /**
   * 删除价格分层配置
   */
  deleteConfig(id: number): Promise<boolean> {
    return request.delete<boolean>(`/api/product-price-configs/${id}`)
  },

  /**
   * 根据ID获取价格分层配置
   */
  getConfigById(id: number): Promise<ProductPriceTierConfigResponse> {
    return request.get<ProductPriceTierConfigResponse>(`/api/product-price-configs/${id}`)
  },

  /**
   * 根据商品ID获取所有价格分层配置
   */
  getConfigsByProductId(productId: number): Promise<ProductPriceTierConfigResponse[]> {
    return request.get<ProductPriceTierConfigResponse[]>(`/api/product-price-configs/product/${productId}`)
  },

  /**
   * 批量保存价格分层配置
   */
  batchSaveConfigs(productId: number, configs: ProductPriceTierConfigRequest[]): Promise<boolean> {
    return request.post<boolean>(`/api/product-price-configs/product/${productId}/batch`, configs)
  },

  /**
   * 根据价格分层ID获取配置
   */
  getConfigsByPriceTierId(priceTierId: number): Promise<ProductPriceTierConfigResponse[]> {
    return request.get<ProductPriceTierConfigResponse[]>(`/api/product-price-configs/price-tier/${priceTierId}`)
  }
}
