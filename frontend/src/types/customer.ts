// 客户类型
export interface Customer {
  id: number
  customerCode: string
  name: string
  companyId?: number
  companyName?: string
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address?: string
  customerType: string
  customerLevel: string
  creditLimit?: number
  paymentTerms?: string
  taxNumber?: string
  bankAccount?: string
  status: string
  createdAt: string
  updatedAt: string
  createdBy?: number
  updatedBy?: number
  deleted: boolean
}

// 客户类型枚举
export enum CustomerType {
  INDIVIDUAL = 'INDIVIDUAL',
  ENTERPRISE = 'ENTERPRISE',
  DEALER = 'DEALER',
  AGENT = 'AGENT'
}

// 客户等级枚举
export enum CustomerLevel {
  NORMAL = 'NORMAL',
  VIP = 'VIP',
  IMPORTANT = 'IMPORTANT',
  STRATEGIC = 'STRATEGIC'
}

// 客户状态枚举
export enum CustomerStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  BLACKLIST = 'BLACKLIST'
}

// 客户搜索请求类型
export interface CustomerSearchRequest {
  keyword?: string
  customerType?: string
  customerLevel?: string
  status?: string
  companyId?: number
  current?: number
  size?: number
}

// 客户创建请求类型
export interface CustomerCreateRequest {
  customerCode: string
  name: string
  companyId?: number
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address?: string
  customerType: string
  customerLevel: string
  creditLimit?: number
  paymentTerms?: string
  taxNumber?: string
  bankAccount?: string
  status: string
}

// 客户更新请求类型
export interface CustomerUpdateRequest {
  customerCode: string
  name: string
  companyId?: number
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address?: string
  customerType: string
  customerLevel: string
  creditLimit?: number
  paymentTerms?: string
  taxNumber?: string
  bankAccount?: string
  status: string
}
