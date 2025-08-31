// 仓库实体类型
export interface Warehouse {
  id: number
  warehouseCode: string
  warehouseName: string
  warehouseType: 'MAIN' | 'BRANCH' | 'TEMPORARY'
  companyId: number
  address?: string
  contactPerson?: string
  contactPhone?: string
  area?: number
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'
  description?: string
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
  deleted: boolean
}

// 仓库请求类型
export interface WarehouseRequest {
  warehouseCode: string
  warehouseName: string
  warehouseType: 'MAIN' | 'BRANCH' | 'TEMPORARY'
  companyId: number
  address?: string
  contactPerson?: string
  contactPhone?: string
  area?: number
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'
  description?: string
}

// 仓库搜索参数类型
export interface WarehouseSearchParams {
  page: number
  size: number
  warehouseCode?: string
  warehouseName?: string
  warehouseType?: string
  status?: string
  companyId?: number
}

// 仓库类型选项
export const WAREHOUSE_TYPE_OPTIONS = [
  { label: '主仓库', value: 'MAIN' },
  { label: '分仓库', value: 'BRANCH' },
  { label: '临时仓库', value: 'TEMPORARY' }
]

// 仓库状态选项
export const WAREHOUSE_STATUS_OPTIONS = [
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
  { label: '维护中', value: 'MAINTENANCE' }
]
