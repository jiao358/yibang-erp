import request from '@/utils/request'
import type { OrderTemplateRequest, OrderTemplateResponse } from '@/types/orderTemplate'

export const orderTemplateApi = {
  // 创建订单模板
  createTemplate(data: OrderTemplateRequest): Promise<OrderTemplateResponse> {
    return request.post('/api/order-templates', data)
  },

  // 更新订单模板
  updateTemplate(id: number, data: OrderTemplateRequest): Promise<OrderTemplateResponse> {
    return request.put(`/api/order-templates/${id}`, data)
  },

  // 删除订单模板
  deleteTemplate(id: number): Promise<boolean> {
    return request.delete(`/api/order-templates/${id}`)
  },

  // 根据ID获取订单模板
  getTemplateById(id: number): Promise<OrderTemplateResponse> {
    return request.get(`/api/order-templates/${id}`)
  },

  // 获取所有订单模板
  getAllTemplates(): Promise<OrderTemplateResponse[]> {
    return request.get('/api/order-templates')
  },

  // 获取当前活跃的订单模板
  getActiveTemplate(): Promise<OrderTemplateResponse> {
    return request.get('/api/order-templates/active')
  },

  // 激活指定版本的订单模板
  activateTemplate(id: number): Promise<OrderTemplateResponse> {
    return request.post(`/api/order-templates/${id}/activate`)
  },

  // 获取模板版本历史
  getTemplateVersionHistory(id: number): Promise<OrderTemplateResponse[]> {
    return request.get(`/api/order-templates/${id}/history`)
  },

  // 验证模板配置
  validateTemplate(data: OrderTemplateRequest): Promise<boolean> {
    return request.post('/api/order-templates/validate', data)
  }
}
