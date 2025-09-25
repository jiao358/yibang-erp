import request from '@/utils/request'
import type { 
  OrderResponse, 
  OrderCreateRequest, 
  OrderUpdateRequest, 
  OrderListRequest,
  OrderStatusUpdateRequest,
  OrderConflictResolutionRequest,
  OrderBatchProcessRequest
} from '@/types/order'

export const orderApi = {
  // 创建订单
  createOrder(data: OrderCreateRequest): Promise<OrderResponse> {
    return request.post('/api/orders', data)
  },

  // 更新订单
  updateOrder(id: number, data: OrderUpdateRequest): Promise<OrderResponse> {
    return request.put(`/api/orders/${id}`, data)
  },

  // 删除订单
  deleteOrder(id: number): Promise<boolean> {
    return request.delete(`/api/orders/${id}`)
  },

  // 根据ID获取订单
  getOrderById(id: number): Promise<OrderResponse> {
    return request.get(`/api/orders/${id}`)
  },

  // 根据平台订单号获取订单
  getOrderByPlatformOrderNo(platformOrderNo: string): Promise<OrderResponse> {
    return request.get(`/api/orders/platform-order/${platformOrderNo}`)
  },

  // 分页查询订单列表
  getOrderList(params: OrderListRequest): Promise<any> {
    return request.get('/api/orders', { params })
  },

  // 更新订单状态
  updateOrderStatus(id: number, data: OrderStatusUpdateRequest): Promise<OrderResponse> {
    return request.put(`/api/orders/${id}/status`, data)
  },

  // 提交订单
  submitOrder(id: number): Promise<OrderResponse> {
    return request.post(`/api/orders/${id}/submit`)
  },

  // 取消订单
  cancelOrder(id: number): Promise<OrderResponse> {
    return request.post(`/api/orders/${id}/cancel`)
  },

  // 供应商确认订单
  supplierConfirmOrder(id: number): Promise<OrderResponse> {
    return request.post(`/api/orders/${id}/supplier-confirm`)
  },

  // 供应商发货
  supplierShipOrder(id: number, data: { warehouseId: number; warehouseName?: string; trackingNumber: string; carrier: string; carrierCode?: string; shippingMethod?: string; shippingNotes?: string; operatorId: number; operatorName?: string; operatorRole?: string }): Promise<OrderResponse> {
    return request.post(`/api/orders/${id}/supplier-ship`, data)
  },

  // 供应商拒绝订单
  supplierRejectOrder(id: number, data: { rejectReason: string; operatorId: number; operatorName?: string; operatorRole?: string }): Promise<OrderResponse> {
    return request.post(`/api/orders/${id}/supplier-reject`, data)
  },

  // 已结清订单
  completeOrder(id: number): Promise<OrderResponse> {
    return request.post(`/api/orders/${id}/complete`)
  },

  // 批量供应商确认订单
  batchSupplierConfirmOrders(data: { orderIds: number[]; reason?: string; operatorId: number; operatorName?: string; operatorRole?: string }): Promise<any> {
    return request.post('/api/orders/batch-supplier-confirm', data)
  },

  // 解决订单冲突
  resolveOrderConflict(id: number, data: OrderConflictResolutionRequest): Promise<OrderResponse> {
    return request.post(`/api/orders/${id}/resolve-conflict`, data)
  },

  // 批量处理订单
  batchProcessOrders(data: OrderBatchProcessRequest): Promise<any> {
    return request.post('/api/orders/batch-process', data)
  },

  // 根据销售用户ID获取订单列表
  getOrdersBySalesUserId(salesUserId: number): Promise<OrderResponse[]> {
    return request.get(`/api/orders/sales-user/${salesUserId}`)
  },

  // 根据供应商公司ID获取订单列表
  getOrdersBySupplierCompanyId(supplierCompanyId: number): Promise<OrderResponse[]> {
    return request.get(`/api/orders/supplier-company/${supplierCompanyId}`)
  },

  // 根据客户ID获取订单列表
  getOrdersByCustomerId(customerId: number): Promise<OrderResponse[]> {
    return request.get(`/api/orders/customer/${customerId}`)
  },

  // 检查订单状态转换是否有效
  isValidStatusTransition(id: number, targetStatus: string): Promise<boolean> {
    return request.get(`/api/orders/${id}/status-transition-valid`, { params: { targetStatus } })
  },

  // 获取订单状态历史
  getOrderStatusHistory(id: number): Promise<any[]> {
    return request.get(`/api/orders/${id}/status-history`)
  },

  // 导出订单
  exportOrders(orderIds: number[]): Promise<Blob> {
    return request.post('/api/orders/export', { orderIds }, {
      responseType: 'blob'
    })
  },

  // 下载发货模板
  downloadShipTemplate(): Promise<Blob> {
    return request.get('/api/orders/ship-template', {
      responseType: 'blob'
    })
  },

  // 预览发货导入数据
  previewShipImport(file: FormData): Promise<{ data: any[] }> {
    return request.post('/api/orders/ship-import/preview', file, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 导入发货数据
  importShipData(file: FormData): Promise<{ successCount: number; failCount: number }> {
    return request.post('/api/orders/ship-import', file, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 生成平台订单号
  generatePlatformOrderNo(): Promise<string> {
    return request.get('/api/orders/generate-order-no')
  },

  // 验证订单数据
  validateOrderData(data: OrderCreateRequest): Promise<boolean> {
    return request.post('/api/orders/validate', data)
  },

  // 处理多渠道订单冲突
  handleMultiSourceConflict(
    salesOrderId: string,
    salesUserId: number,
    conflictingOrders: OrderCreateRequest[]
  ): Promise<OrderResponse> {
    return request.post('/api/orders/resolve-multi-source-conflict', conflictingOrders, { 
      params: { salesOrderId, salesUserId } 
    })
  },

  // Excel批量导入
  importExcelOrders(file: File, templateId?: number): Promise<any> {
    const formData = new FormData()
    formData.append('file', file)
    if (templateId) {
      formData.append('templateId', templateId.toString())
    }

    return request.post('/api/orders/batch/import-excel', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
