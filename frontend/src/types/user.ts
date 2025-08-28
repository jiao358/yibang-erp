// 用户状态枚举
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  LOCKED = 'LOCKED',
  PENDING = 'PENDING'
}

// 用户状态显示文本
export const UserStatusText: Record<UserStatus, string> = {
  [UserStatus.ACTIVE]: '激活',
  [UserStatus.INACTIVE]: '未激活',
  [UserStatus.LOCKED]: '锁定',
  [UserStatus.PENDING]: '待审核'
}

// 用户状态标签类型
export const UserStatusType: Record<UserStatus, string> = {
  [UserStatus.ACTIVE]: 'success',
  [UserStatus.INACTIVE]: 'info',
  [UserStatus.LOCKED]: 'danger',
  [UserStatus.PENDING]: 'warning'
}

// 用户实体类型
export interface User {
  id: number
  username: string
  passwordHash?: string
  email?: string
  phone?: string
  realName?: string
  avatar?: string
  roleId: number
  companyId: number
  department?: string
  position?: string
  status: UserStatus
  lastLoginAt?: string
  lastLoginIp?: string
  loginAttempts: number
  lockedUntil?: string
  passwordChangedAt: string
  createdAt: string
  updatedAt: string
  createdBy?: number
  updatedBy?: number
  deleted: boolean
}

// 角色实体类型
export interface Role {
  id: number
  name: string
  description?: string
  permissions?: string
  isSystem: boolean
  status: string
  createdAt: string
  updatedAt: string
  createdBy?: number
  updatedBy?: number
  deleted: boolean
}

// 公司实体类型
export interface Company {
  id: number
  name: string
  type: 'SUPPLIER' | 'SALES'
  status: string
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

// 公司类型显示文本
export const CompanyTypeText: Record<string, string> = {
  SUPPLIER: '供应商',
  SALES: '销售商'
}

// 公司状态显示文本
export const CompanyStatusText: Record<string, string> = {
  ACTIVE: '激活',
  INACTIVE: '未激活',
  SUSPENDED: '暂停'
}

// 用户创建请求类型
export interface CreateUserRequest {
  username: string
  password: string
  email?: string
  phone?: string
  realName?: string
  avatar?: string
  roleId: number
  companyId: number
  department?: string
  position?: string
  status: UserStatus
}

// 用户更新请求类型
export interface UpdateUserRequest {
  email?: string
  phone?: string
  realName?: string
  avatar?: string
  roleId?: number
  companyId?: number
  department?: string
  position?: string
  status?: UserStatus
}

// 用户查询参数类型
export interface UserQueryParams {
  page: number
  size: number
  username?: string
  realName?: string
  status?: UserStatus
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

// 用户列表响应类型
export type UserListResponse = ApiResponse<PageResponse<User>>

// 用户详情响应类型
export type UserDetailResponse = ApiResponse<User>

// 角色列表响应类型
export type RoleListResponse = ApiResponse<Role[]>

// 公司列表响应类型
export type CompanyListResponse = ApiResponse<Company[]>

// 操作结果响应类型
export type OperationResponse = ApiResponse<boolean | number>
