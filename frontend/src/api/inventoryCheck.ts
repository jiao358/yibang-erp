import request from '@/utils/request'
import type { InventoryCheck, InventoryCheckRequest } from '@/types/inventory'

// 获取盘点列表（分页）
export function getCheckPage(params: {
  page: number
  size: number
  checkType?: string
  warehouseId?: number
  status?: string
}) {
  return request({
    url: '/api/inventory-checks',
    method: 'get',
    params
  })
}

// 根据ID获取盘点详情
export function getCheckById(id: number) {
  return request({
    url: `/api/inventory-checks/${id}`,
    method: 'get'
  })
}

// 创建盘点
export function createCheck(data: InventoryCheckRequest) {
  return request({
    url: '/api/inventory-checks',
    method: 'post',
    data
  })
}

// 更新盘点
export function updateCheck(id: number, data: Partial<InventoryCheckRequest>) {
  return request({
    url: `/api/inventory-checks/${id}`,
    method: 'put',
    data
  })
}

// 删除盘点
export function deleteCheck(id: number) {
  return request({
    url: `/api/inventory-checks/${id}`,
    method: 'delete'
  })
}

// 开始盘点
export function startCheck(id: number) {
  return request({
    url: `/api/inventory-checks/${id}/start`,
    method: 'put'
  })
}

// 完成盘点
export function completeCheck(id: number) {
  return request({
    url: `/api/inventory-checks/${id}/complete`,
    method: 'put'
  })
}

// 取消盘点
export function cancelCheck(id: number) {
  return request({
    url: `/api/inventory-checks/${id}/cancel`,
    method: 'put'
  })
}

// 审核盘点
export function reviewCheck(id: number, data: {
  reviewStatus: string
  reviewComment: string
}) {
  return request({
    url: `/api/inventory-checks/${id}/review`,
    method: 'put',
    data
  })
}

// 获取盘点项目列表
export function getCheckItems(checkId: number) {
  return request({
    url: `/api/inventory-checks/${checkId}/items`,
    method: 'get'
  })
}

// 获取有差异的盘点项目
export function getItemsWithDifference(checkId: number) {
  return request({
    url: `/api/inventory-checks/${checkId}/items/difference`,
    method: 'get'
  })
}

// 添加盘点项目
export function addCheckItem(checkId: number, data: {
  productId: number
  warehouseId: number
  systemQuantity: number
  actualQuantity: number
  differenceReason?: string
  handlingMethod?: string
  handlingRemark?: string
}) {
  return request({
    url: `/api/inventory-checks/${checkId}/items`,
    method: 'post',
    data
  })
}

// 更新盘点项目
export function updateCheckItem(checkId: number, itemId: number, data: {
  actualQuantity: number
  differenceReason?: string
  handlingMethod?: string
  handlingRemark?: string
}) {
  return request({
    url: `/api/inventory-checks/${checkId}/items/${itemId}`,
    method: 'put',
    data
  })
}

// 验证盘点项目
export function verifyCheckItem(checkId: number, itemId: number, data: {
  verifiedQuantity: number
  verificationRemark?: string
}) {
  return request({
    url: `/api/inventory-checks/${checkId}/items/${itemId}/verify`,
    method: 'put',
    data
  })
}
