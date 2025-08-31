import request from '@/utils/request'
import type { WarehouseRequest } from '@/types/warehouse'

// 获取仓库列表
export const getWarehouseList = (params: {
  page: number
  size: number
  warehouseCode?: string
  warehouseName?: string
  warehouseType?: string
  status?: string
  companyId?: number
}) => {
  return request.get('/api/warehouses', { params })
}

// 根据ID获取仓库
export const getWarehouseById = (id: number) => {
  return request.get(`/api/warehouses/${id}`)
}

// 创建仓库
export const createWarehouse = (data: WarehouseRequest) => {
  return request.post('/api/warehouses', data)
}

// 更新仓库
export const updateWarehouse = (id: number, data: WarehouseRequest) => {
  return request.put(`/api/warehouses/${id}`, data)
}

// 删除仓库
export const deleteWarehouse = (id: number) => {
  return request.delete(`/api/warehouses/${id}`)
}

// 启用仓库
export const activateWarehouse = (id: number) => {
  return request.post(`/api/warehouses/${id}/activate`)
}

// 停用仓库
export const deactivateWarehouse = (id: number) => {
  return request.post(`/api/warehouses/${id}/deactivate`)
}

// 维护仓库
export const maintainWarehouse = (id: number) => {
  return request.post(`/api/warehouses/${id}/maintain`)
}

// 根据公司ID获取仓库列表
export const getWarehousesByCompanyId = (companyId: number) => {
  return request.get(`/api/warehouses/company/${companyId}`)
}
