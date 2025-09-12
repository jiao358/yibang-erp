import request from '@/utils/request'
import type { 
  OrderAlertRequest, 
  OrderAlertResponse, 
  ManualProcessingRequest 
} from '@/types/orderAlert'

export const orderAlertApi = {
  // 分页查询订单预警列表
  getOrderAlerts(params: OrderAlertRequest): Promise<any> {
    return request.get('/api/order-alerts', { params })
  },

  // 分配预警给用户
  assignAlert(id: number, userId: number): Promise<any> {
    return request.post(`/api/order-alerts/${id}/assign`, null, {
      params: { userId }
    })
  },

  // 开始处理预警
  processAlert(id: number, data: ManualProcessingRequest): Promise<any> {
    return request.post(`/api/order-alerts/${id}/process`, data)
  },

  // 完成预警处理
  completeAlert(id: number, data: ManualProcessingRequest): Promise<any> {
    return request.post(`/api/order-alerts/${id}/complete`, data)
  },

  // 拒绝预警处理
  rejectAlert(id: number, data: ManualProcessingRequest): Promise<any> {
    return request.post(`/api/order-alerts/${id}/reject`, data)
  }
}
