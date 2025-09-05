import request from '@/utils/request'

export interface InventoryAlert {
  id: number
  productId: number
  productName: string
  warehouseId: number
  warehouseName: string
  currentStock: number
  minStock: number
  maxStock: number
  alertType: 'LOW_STOCK' | 'HIGH_STOCK' | 'ZERO_STOCK'
  status: 'ACTIVE' | 'RESOLVED' | 'IGNORED'
  createdAt: string
  updatedAt: string
}

export interface InventoryAlertRequest {
  productId: number
  warehouseId: number
  minStock: number
  maxStock: number
  alertType: string
}

export interface InventoryAlertQueryRequest {
  page: number
  size: number
  productId?: number
  warehouseId?: number
  alertType?: string
  status?: string
}

export interface InventoryAlertResponse {
  content: InventoryAlert[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const inventoryAlertApi = {
  // 获取库存预警列表
  getInventoryAlertPage(params: InventoryAlertQueryRequest) {
    return request.get<InventoryAlertResponse>('/inventory-alerts', { params })
  },

  // 创建库存预警
  createInventoryAlert(data: InventoryAlertRequest) {
    return request.post<InventoryAlert>('/inventory-alerts', data)
  },

  // 更新库存预警
  updateInventoryAlert(id: number, data: Partial<InventoryAlertRequest>) {
    return request.put<InventoryAlert>(`/inventory-alerts/${id}`, data)
  },

  // 删除库存预警
  deleteInventoryAlert(id: number) {
    return request.delete<boolean>(`/inventory-alerts/${id}`)
  },

  // 批量删除库存预警
  batchDeleteInventoryAlerts(ids: number[]) {
    return request.delete<boolean>('/inventory-alerts/batch', { data: { ids } })
  },

  // 解决预警
  resolveAlert(id: number) {
    return request.put<boolean>(`/inventory-alerts/${id}/resolve`)
  },

  // 忽略预警
  ignoreAlert(id: number) {
    return request.put<boolean>(`/inventory-alerts/${id}/ignore`)
  },

  // 批量解决预警
  batchResolveAlerts(ids: number[]) {
    return request.put<boolean>('/inventory-alerts/batch/resolve', { data: { ids } })
  },

  // 批量忽略预警
  batchIgnoreAlerts(ids: number[]) {
    return request.put<boolean>('/inventory-alerts/batch/ignore', { data: { ids } })
  }
}

// 导出单独的函数以兼容现有代码
export const getInventoryAlertList = inventoryAlertApi.getInventoryAlertPage
export const createInventoryAlert = inventoryAlertApi.createInventoryAlert
export const updateInventoryAlert = inventoryAlertApi.updateInventoryAlert
export const deleteInventoryAlert = inventoryAlertApi.deleteInventoryAlert
export const handleInventoryAlert = inventoryAlertApi.resolveAlert
export const ignoreInventoryAlert = inventoryAlertApi.ignoreAlert
export const autoDetectInventoryAlerts = () => {
  return request.post<boolean>('/inventory-alerts/auto-detect')
}
