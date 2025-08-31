/**
 * 商品相关API接口
 */
import request from '@/utils/request'
import type { Product } from '@/types/product'

export const productApi = {
  // 根据关键词搜索产品
  searchProducts(keyword: string): Promise<Product[]> {
    return request.get('/api/products/search', { params: { keyword } })
  },

  // 根据ID获取产品
  getProductById(id: number): Promise<Product> {
    return request.get(`/api/products/${id}`)
  },

  // 获取产品列表
  getProductList(params?: any): Promise<any> {
    return request.get('/api/products', { params })
  }
}
export const getProductList = (params?: any): Promise<any> => {
  return request.get('/api/products', { params })
}

export const getProductById = (id: number): Promise<Product> => {
  return request.get(`/api/products/${id}`)
}

export const searchProducts = (keyword: string): Promise<Product[]> => {
  return request.get('/api/products/search', { params: { keyword } })
}


// 直接导出函数，支持解构导入
export const createProduct = (data: any): Promise<Product> => {
  return request.post('/api/products', data)
}

export const updateProduct = (id: number, data: any): Promise<Product> => {
  return request.put(`/api/products/${id}`, data)
}

export const deleteProduct = (id: number): Promise<boolean> => {
  return request.delete(`/api/products/${id}`)
}

export const submitForApproval = (id: number): Promise<boolean> => {
  return request.post(`/api/products/${id}/submit-approval`)
}

export const auditProduct = (id: number, data: any): Promise<boolean> => {
  return request.post(`/api/products/${id}/audit`, data)
}

export const getProductBrands = (): Promise<{
  data: any[]
  success: boolean
  message: string
  timestamp: number
}> => {
  return request.get('/api/brands')
}

export const getProductCategories = (): Promise<{
  data: any[]
  success: boolean
  message: string
  timestamp: number
}> => {
  return request.get('/api/categories')
}

export const activateProduct = (id: number, operatorId: number): Promise<boolean> => {
  return request.post(`/api/products/${id}/activate`, null, { params: { operatorId } })
}

export const deactivateProduct = (id: number, operatorId: number, reason?: string): Promise<boolean> => {
  return request.post(`/api/products/${id}/deactivate`, null, { params: { operatorId, reason } })
}
