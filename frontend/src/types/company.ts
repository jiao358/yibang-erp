// 公司业务类型枚举
export enum CompanyType {
  SUPPLIER = 'SUPPLIER',
  SALES = 'SALES'
}

// 公司状态枚举
export enum CompanyStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
}

// 公司业务类型显示文本
export const CompanyTypeText: Record<CompanyType, string> = {
  [CompanyType.SUPPLIER]: '供应商',
  [CompanyType.SALES]: '销售商'
}

// 公司状态显示文本
export const CompanyStatusText: Record<CompanyStatus, string> = {
  [CompanyStatus.ACTIVE]: '激活',
  [CompanyStatus.INACTIVE]: '未激活',
  [CompanyStatus.SUSPENDED]: '暂停'
}

// 公司业务类型标签类型
export const CompanyTypeTagType: Record<CompanyType, string> = {
  [CompanyType.SUPPLIER]: 'success',
  [CompanyType.SALES]: 'warning'
}

// 公司状态标签类型
export const CompanyStatusTagType: Record<CompanyStatus, string> = {
  [CompanyStatus.ACTIVE]: 'success',
  [CompanyStatus.INACTIVE]: 'info',
  [CompanyStatus.SUSPENDED]: 'danger'
}

// 公司实体类型
export interface Company {
  id: number
  name: string
  type: CompanyType
  status: CompanyStatus
  businessLicense?: string
  contactPerson?: string
  contactPhone?: string
  contactEmail?: string
  address?: string
  description?: string
  createdAt: string
  updatedAt: string
  createdBy?: number
  updatedBy?: number
  deleted: boolean
}

// 公司创建请求类型
export interface CreateCompanyRequest {
  name: string
  type: CompanyType
  status: CompanyStatus
  businessLicense?: string
  contactPerson?: string
  contactPhone?: string
  contactEmail?: string
  address?: string
  description?: string
}

// 公司更新请求类型
export interface UpdateCompanyRequest {
  name?: string
  type?: CompanyType
  status?: CompanyStatus
  businessLicense?: string
  contactPerson?: string
  contactPhone?: string
  contactEmail?: string
  address?: string
  description?: string
}

// 公司查询参数类型
export interface CompanyQueryParams {
  page: number
  size: number
  name?: string
  type?: CompanyType
  status?: CompanyStatus
}

// 分页响应类型
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// API响应类型
export interface ApiResponse<T = any> {
  success: boolean
  data: T
  message: string
  timestamp: number
}

// 公司列表响应类型
export type CompanyListResponse = ApiResponse<PageResponse<Company>>

// 公司详情响应类型
export type CompanyDetailResponse = ApiResponse<Company>

// 公司列表响应类型（不分页）
export type CompanyArrayResponse = ApiResponse<Company[]>

// 操作结果响应类型
export type OperationResponse = ApiResponse<boolean | number>
