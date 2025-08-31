// 商品库存实体类型
export interface ProductInventory {
  id: number
  productId: number
  warehouseId: number
  availableQuantity: number
  reservedQuantity: number
  damagedQuantity: number
  minStockLevel?: number
  maxStockLevel?: number
  reorderPoint?: number
  lastStockIn?: string
  lastStockOut?: string
  createdAt: string
  updatedAt: string
  // 关联查询的字段
  productSku?: string
  productName?: string
  productUnit?: string
  warehouseName?: string
  warehouseCode?: string
}

// 库存操作记录类型
export interface InventoryOperation {
  id: number
  operationNo: string
  operationType: 'STOCK_IN' | 'STOCK_OUT' | 'ADJUSTMENT' | 'TRANSFER' | 'CHECK'
  productId: number
  warehouseId: number
  quantity: number
  beforeAvailableQuantity: number
  afterAvailableQuantity: number
  beforeReservedQuantity: number
  afterReservedQuantity: number
  beforeDamagedQuantity: number
  afterDamagedQuantity: number
  unitPrice?: number
  totalAmount?: number
  orderId?: number
  orderItemId?: number
  reason?: string
  remark?: string
  operatorId: number
  operationTime: string
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
  deleted: boolean
}

// 库存操作请求类型
export interface StockOperationRequest {
  operationType: 'STOCK_IN' | 'STOCK_OUT' | 'ADJUSTMENT' | 'TRANSFER'
  productId: number
  warehouseId: number
  quantity: number
  unitPrice?: number
  orderId?: number
  orderItemId?: number
  reason?: string
  remark?: string
  operatorId: number
}

// 库存调整请求类型（完整版）
export interface StockAdjustmentRequest {
  productId: number
  warehouseId: number
  newAvailableQuantity?: number
  newReservedQuantity?: number
  newMinStockLevel?: number
  reason?: string
  remark?: string
  operatorId: number
}

// 库存预警类型
export interface InventoryAlert {
  id: number
  alertNo: string
  alertType: 'LOW_STOCK' | 'OVER_STOCK' | 'EXPIRY' | 'DAMAGED'
  alertLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  productId: number
  warehouseId: number
  currentQuantity: number
  thresholdQuantity: number
  alertContent?: string
  status: 'PENDING' | 'PROCESSING' | 'RESOLVED' | 'IGNORED'
  handlerId?: number
  handledAt?: string
  handlingResult?: string
  handlingRemark?: string
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
  deleted: boolean
}

// 库存盘点类型
export interface InventoryCheck {
  id: number
  checkNo: string
  checkType: 'FULL' | 'PARTIAL' | 'CYCLE' | 'RANDOM'
  warehouseId: number
  status: 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  plannedStartTime: string
  plannedEndTime: string
  actualStartTime?: string
  actualEndTime?: string
  checkerId: number
  reviewerId?: number
  reviewedAt?: string
  reviewStatus: 'PENDING' | 'APPROVED' | 'REJECTED'
  reviewComment?: string
  description?: string
  remark?: string
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
  deleted: boolean
}

// 库存盘点明细类型
export interface InventoryCheckItem {
  id: number
  checkId: number
  productId: number
  warehouseId: number
  systemQuantity: number
  actualQuantity?: number
  differenceQuantity?: number
  differenceAmount?: number
  status: 'PENDING' | 'CHECKED' | 'VERIFIED'
  checkerId?: number
  checkedAt?: string
  verifierId?: number
  verifiedAt?: string
  differenceReason?: string
  handlingMethod?: 'ADJUST' | 'INVESTIGATE' | 'IGNORE'
  handlingRemark?: string
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
  deleted: boolean
}

// 库存搜索参数类型
export interface InventorySearchParams {
  page: number
  size: number
  productId?: number
  warehouseId?: number
  productName?: string
  warehouseName?: string
}

// 库存操作记录搜索参数类型
export interface OperationHistoryParams {
  page: number
  size: number
  productId?: number
  warehouseId?: number
  operationType?: string
  startTime?: string
  endTime?: string
}

// 库存预警线设置参数类型
export interface StockAlertLevelParams {
  productId: number
  warehouseId: number
  minStockLevel: number
  maxStockLevel: number
  reorderPoint: number
}

// 操作类型选项
export const OPERATION_TYPE_OPTIONS = [
  { label: '入库', value: 'STOCK_IN' },
  { label: '出库', value: 'STOCK_OUT' },
  { label: '调整', value: 'ADJUSTMENT' },
  { label: '调拨', value: 'TRANSFER' }
]

// 预警类型选项
export const ALERT_TYPE_OPTIONS = [
  { label: '库存不足', value: 'LOW_STOCK' },
  { label: '库存过高', value: 'OVER_STOCK' },
  { label: '即将过期', value: 'EXPIRY' },
  { label: '损坏库存', value: 'DAMAGED' }
]

// 预警级别选项
export const ALERT_LEVEL_OPTIONS = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高', value: 'HIGH' },
  { label: '紧急', value: 'CRITICAL' }
]

// 预警状态选项
export const ALERT_STATUS_OPTIONS = [
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已解决', value: 'RESOLVED' },
  { label: '已忽略', value: 'IGNORED' }
]

// 盘点类型选项
export const CHECK_TYPE_OPTIONS = [
  { label: '全面盘点', value: 'FULL' },
  { label: '部分盘点', value: 'PARTIAL' },
  { label: '周期盘点', value: 'CYCLE' },
  { label: '随机盘点', value: 'RANDOM' }
]

// 盘点状态选项
export const CHECK_STATUS_OPTIONS = [
  { label: '已计划', value: 'PLANNED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

// 审核状态选项
export const REVIEW_STATUS_OPTIONS = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
]
