// 角色状态枚举
export enum RoleStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE'
}

// 角色状态显示文本
export const RoleStatusText: Record<RoleStatus, string> = {
  [RoleStatus.ACTIVE]: '激活',
  [RoleStatus.INACTIVE]: '未激活'
}

// 角色状态标签类型
export const RoleStatusType: Record<RoleStatus, string> = {
  [RoleStatus.ACTIVE]: 'success',
  [RoleStatus.INACTIVE]: 'info'
}

// 角色实体类型
export interface Role {
  id: number
  name: string
  description?: string
  permissions?: string
  isSystem: boolean
  status: RoleStatus
  createdAt: string
  updatedAt: string
  createdBy?: number
  updatedBy?: number
  deleted: boolean
}

// 角色创建请求类型
export interface CreateRoleRequest {
  name: string
  description?: string
  permissions?: string
  status: RoleStatus
}

// 角色更新请求类型
export interface UpdateRoleRequest {
  name?: string
  description?: string
  permissions?: string
  status?: RoleStatus
}

// 角色查询参数类型
export interface RoleQueryParams {
  page: number
  size: number
  name?: string
  status?: RoleStatus
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

// 角色列表响应类型
export type RoleListResponse = ApiResponse<PageResponse<Role>>

// 角色详情响应类型
export type RoleDetailResponse = ApiResponse<Role>

// 角色列表响应类型（不分页）
export type RoleArrayResponse = ApiResponse<Role[]>

// 操作结果响应类型
export type OperationResponse = ApiResponse<boolean | number>
