import request from '@/utils/request'
import type { StockOperationRequest } from '@/types/inventory'
import type { StockAdjustmentRequest } from '@/types/inventory'

// 商品入库
export const stockIn = (data: StockOperationRequest) => {
  return request.post('/api/inventory/stock-in', data)
}

// 商品出库
export const stockOut = (data: StockOperationRequest) => {
  return request.post('/api/inventory/stock-out', data)
}

// 库存调整
export const adjustStock = (data: StockOperationRequest) => {
  return request.post('/api/inventory/adjust', data)
}

// 库存调整（完整版）
export const adjustStockComplete = (data: StockAdjustmentRequest) => {
  return request.post('/api/inventory/adjust-complete', data)
}

// 库存调拨
export const transferStock = (data: StockOperationRequest) => {
  return request.post('/api/inventory/transfer', data)
}

// 根据商品ID查询库存
export const getInventoryByProductId = (productId: number) => {
  return request.get(`/api/inventory/product/${productId}`)
}

// 根据仓库ID查询库存
export const getInventoryByWarehouseId = (warehouseId: number) => {
  return request.get(`/api/inventory/warehouse/${warehouseId}`)
}

// 分页查询库存列表
export const getInventoryPage = (params: {
  page: number
  size: number
  productId?: number
  warehouseId?: number
  productName?: string
  warehouseName?: string
  companyId?: number
}) => {
  return request.get('/api/inventory', { params })
}

// 查询库存不足的商品
export const getLowStockProducts = () => {
  return request.get('/api/inventory/low-stock')
}

// 查询需要补货的商品
export const getProductsNeedingReorder = () => {
  return request.get('/api/inventory/need-reorder')
}

// 设置库存预警线
export const setStockAlertLevel = (params: {
  productId: number
  warehouseId: number
  minStockLevel: number
  maxStockLevel: number
  reorderPoint: number
}) => {
  return request.post('/api/inventory/alert-level', null, { params })
}

// 查询库存操作记录
export const getOperationHistory = (params: {
  page: number
  size: number
  productId?: number
  warehouseId?: number
  operationType?: string
  startTime?: string
  endTime?: string
}) => {
  return request.get('/api/inventory/operations', { params })
}
