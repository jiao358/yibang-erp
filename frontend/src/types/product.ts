/**
 * 商品相关类型定义
 */

// 商品状态枚举
export enum ProductStatus {
  DRAFT = 'DRAFT',
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED'
}

// 商品审核状态枚举
export enum ProductApprovalStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

// 商品实体类型
export interface Product {
  id: number
  sku: string
  name: string
  categoryId: number
  brandId?: number
  description?: string
  shortDescription?: string
  images?: string
  specifications?: string
  unit: string
  costPrice: number
  sellingPrice: number
  marketPrice?: number
  retailLimitPrice?: number
  weight?: number
  dimensions?: string
  barcode?: string
  hsCode?: string
  originCountry?: string
  material?: string
  color?: string
  size?: string
  tags?: string
  status: ProductStatus
  approvalStatus: ProductApprovalStatus
  approvalComment?: string
  approvalAt?: string
  approvalBy?: number
  companyId: number
  isFeatured: boolean
  isHot: boolean
  isNew: boolean
  viewCount: number
  createdAt: string
  updatedAt: string
  createdBy?: number
  updatedBy?: number
  deleted: boolean
  priceTierConfigs?: ProductPriceTierConfig[] // 添加价格分层配置
}

// 商品创建请求类型
export interface ProductCreateRequest {
  sku: string
  name: string
  categoryId: number
  brandId?: number
  description?: string
  shortDescription?: string
  unit: string
  costPrice: number
  sellingPrice: number
  marketPrice?: number
  retailLimitPrice?: number
  weight?: number
  material?: string
  color?: string
  size?: string
  originCountry?: string
  hsCode?: string
  tags?: string
  isFeatured?: boolean
  isHot?: boolean
  isNew?: boolean
  companyId: number        // 添加公司ID（必填）
  approvalStatus?: string  // 添加审核状态
}

// 商品更新请求类型
export interface ProductUpdateRequest extends Partial<ProductCreateRequest> {
  id: number
}

// 商品查询请求类型
export interface ProductQueryRequest {
  page: number
  size: number
  name?: string
  status?: string
  approvalStatus?: string
  categoryId?: number
  brandId?: number
  companyId?: number
}

// 商品审核请求类型
export interface ProductAuditRequest {
  productId: number
  approvalStatus: ProductApprovalStatus
  approvalComment?: string
  approverId: number
}

// 商品审核结果类型
export interface ProductAuditResult {
  productId: number
  productName: string
  sku: string
  approvalStatus: ProductApprovalStatus
  productStatus: ProductStatus
  approvalComment?: string
  approverId: number
  approverName?: string
  approvalAt: string
  message: string
  success: boolean
}

// 商品分页结果类型
export interface ProductPageResult {
  records: Product[]
  total: number
  size: number
  current: number
  pages: number
}

// 商品分类类型
export interface ProductCategory {
  id: number
  name: string
  parentId?: number
  level: number
  sortOrder: number
  description?: string
  icon?: string
  status: string
  createdAt: string
  updatedAt: string
}

// 商品品牌类型
export interface ProductBrand {
  id: number
  name: string
  logo?: string
  description?: string
  website?: string
  status: string
  createdAt: string
  updatedAt: string
}

// 商品库存类型
export interface ProductInventory {
  id: number
  productId: number
  warehouseId: number
  availableQuantity: number
  reservedQuantity: number
  damagedQuantity: number
  minStockLevel: number
  maxStockLevel?: number
  reorderPoint?: number
  lastStockIn?: string
  lastStockOut?: string
  createdAt: string
  updatedAt: string
}

// 商品价格类型
export interface ProductPrice {
  id: number
  productId: number
  priceType: string
  price: number
  currency: string
  effectiveFrom: string
  effectiveTo?: string
  minQuantity?: number
  maxQuantity?: number
  customerGroup?: string
  companyId?: number
  createdAt: string
  updatedAt: string
}

// 商品审核日志类型
export interface ProductApprovalLog {
  id: number
  productId: number
  action: string
  status: string
  comment?: string
  approverId?: number
  approvalAt: string
  createdAt: string
}

// 商品统计信息类型
export interface ProductStatistics {
  totalCount: number
  draftCount: number
  pendingCount: number
  activeCount: number
  inactiveCount: number
  discontinuedCount: number
  approvedCount: number
  rejectedCount: number
  featuredCount: number
  hotCount: number
  newCount: number
}

// 商品价格分层配置类型
export interface ProductPriceTierConfig {
  id: number
  productId: number
  priceTierId: number
  priceTierName: string
  priceTierType: string
  dropshippingPrice: number // 一件代发价格
  retailLimitPrice: number // 零售限价
  isActive: boolean
  createdAt: string
  updatedAt: string
}

// 商品价格分层配置请求类型
export interface ProductPriceTierConfigRequest {
  productId: number
  priceTierId: number
  dropshippingPrice: number
  retailLimitPrice: number
  isActive: boolean
  companyId?: number    // 添加公司ID
  createdBy?: number    // 添加创建人ID
}

// 商品价格分层配置响应类型
export interface ProductPriceTierConfigResponse {
  id: number
  productId: number
  priceTierId: number
  priceTierName: string
  priceTierType: string
  dropshippingPrice: number
  retailLimitPrice: number
  isActive: boolean
  createdAt: string
  updatedAt: string
}

// 商品价格配置查询请求类型
export interface ProductPriceConfigQueryRequest {
  productId: number
  companyId?: number
  priceTierId?: number
  isActive?: boolean
}
