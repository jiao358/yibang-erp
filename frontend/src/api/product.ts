/**
 * 商品相关API接口
 */
import request from '@/utils/request'
import type {
  Product,
  ProductCreateRequest,
  ProductUpdateRequest,
  ProductQueryRequest,
  ProductAuditRequest,
  ProductAuditResult,
  ProductPageResult
} from '@/types/product'

// 获取商品列表
export function getProductList(params: ProductQueryRequest) {
  return request<ProductPageResult>({
    url: '/api/products',
    method: 'get',
    params
  })
}

// 获取商品详情
export function getProduct(id: number) {
  return request<Product>({
    url: `/api/products/${id}`,
    method: 'get'
  })
}

// 创建商品
export function createProduct(data: ProductCreateRequest) {
  return request<Product>({
    url: '/api/products',
    method: 'post',
    data
  })
}

// 更新商品
export function updateProduct(id: number, data: ProductUpdateRequest) {
  return request<Product>({
    url: `/api/products/${id}`,
    method: 'put',
    data
  })
}

// 删除商品
export function deleteProduct(id: number) {
  return request<boolean>({
    url: `/api/products/${id}`,
    method: 'delete'
  })
}

// 提交商品审核
export function submitForApproval(id: number, submitterId: number) {
  return request<boolean>({
    url: `/api/products/${id}/submit`,
    method: 'post',
    params: { submitterId }
  })
}

// 审核商品
export function auditProduct(id: number, data: ProductAuditRequest) {
  return request<ProductAuditResult>({
    url: `/api/products/${id}/audit`,
    method: 'post',
    data
  })
}

// 上架商品
export function activateProduct(id: number, operatorId: number) {
  return request<boolean>({
    url: `/api/products/${id}/activate`,
    method: 'post',
    params: { operatorId }
  })
}

// 下架商品
export function deactivateProduct(id: number, operatorId: number, reason?: string) {
  return request<boolean>({
    url: `/api/products/${id}/deactivate`,
    method: 'post',
    params: { operatorId, reason }
  })
}

// 停售商品
export function discontinueProduct(id: number, operatorId: number, reason?: string) {
  return request<boolean>({
    url: `/api/products/${id}/discontinue`,
    method: 'post',
    params: { operatorId, reason }
  })
}

// 重置商品为草稿状态
export function resetToDraft(id: number, operatorId: number) {
  return request<boolean>({
    url: `/api/products/${id}/reset`,
    method: 'post',
    params: { operatorId }
  })
}

// 获取待审核商品列表
export function getPendingApprovalProducts(companyId: number, page: number, size: number) {
  return request<number[]>({
    url: '/api/products/pending',
    method: 'get',
    params: { companyId, page, size }
  })
}

// 获取待审核商品总数
export function getPendingApprovalCount(companyId: number) {
  return request<number>({
    url: '/api/products/pending/count',
    method: 'get',
    params: { companyId }
  })
}

// 批量审核商品
export function batchAuditProducts(requests: ProductAuditRequest[]) {
  return request<ProductAuditResult[]>({
    url: '/api/products/batch-audit',
    method: 'post',
    data: requests
  })
}

// 获取商品状态
export function getProductStatus(id: number) {
  return request<string>({
    url: `/api/products/${id}/status`,
    method: 'get'
  })
}

// 获取商品审核状态
export function getProductApprovalStatus(id: number) {
  return request<string>({
    url: `/api/products/${id}/approval-status`,
    method: 'get'
  })
}

// 检查状态流转是否合法
export function validateStatusTransition(id: number, targetStatus: string) {
  return request<boolean>({
    url: `/api/products/${id}/validate-transition`,
    method: 'post',
    params: { targetStatus }
  })
}

// 获取商品分类列表
export function getProductCategories() {
  return request<any[]>({
    url: '/api/categories',
    method: 'get'
  })
}

// 获取商品品牌列表
export function getProductBrands() {
  return request<any[]>({
    url: '/api/brands',
    method: 'get'
  })
}
