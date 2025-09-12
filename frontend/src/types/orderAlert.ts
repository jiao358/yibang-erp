// 订单预警查询请求
export interface OrderAlertRequest {
  current?: number
  size?: number
  status?: string
  processingType?: string
  startDate?: string
  endDate?: string
  orderNumber?: string
  sourceOrderId?: string
  sortField?: string
  sortOrder?: string
}

// 订单预警响应
export interface OrderAlertResponse {
  id: number
  orderId: number
  sourceOrderId: string
  processingType: string
  processingTypeDesc: string
  processingReason: string
  status: string
  statusDesc: string
  priority: string
  priorityDesc: string
  assignedTo?: number
  assignedToName?: string
  processedBy?: number
  processedByName?: string
  processedAt?: string
  processingNotes?: string
  rejectionReason?: string
  createdAt: string
  updatedAt: string
  createdBy: number
  createdByName?: string
  orderInfo?: OrderBasicInfo
}

// 订单基本信息
export interface OrderBasicInfo {
  orderNumber: string
  customerName: string
  orderStatus: string
  orderStatusDesc: string
  totalAmount: string
  deliveryAddress: string
  buyerNote?: string
}

// 人工处理请求
export interface ManualProcessingRequest {
  processingNotes?: string
  rejectionReason?: string
  assignedTo?: number
}

// 预警状态枚举
export const ALERT_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  COMPLETED: 'COMPLETED',
  REJECTED: 'REJECTED'
} as const

// 处理类型枚举
export const PROCESSING_TYPE = {
  ORDER_CLOSE: 'ORDER_CLOSE',
  ADDRESS_CHANGE: 'ADDRESS_CHANGE',
  REFUND: 'REFUND',
  CANCEL: 'CANCEL'
} as const

// 优先级枚举
export const PRIORITY = {
  LOW: 'LOW',
  NORMAL: 'NORMAL',
  HIGH: 'HIGH',
  URGENT: 'URGENT'
} as const

// 状态选项
export const STATUS_OPTIONS = [
  { label: '待处理', value: ALERT_STATUS.PENDING },
  { label: '处理中', value: ALERT_STATUS.PROCESSING },
  { label: '已完成', value: ALERT_STATUS.COMPLETED },
  { label: '已拒绝', value: ALERT_STATUS.REJECTED }
]

// 处理类型选项
export const PROCESSING_TYPE_OPTIONS = [
  { label: '订单关闭', value: PROCESSING_TYPE.ORDER_CLOSE },
  { label: '地址修改', value: PROCESSING_TYPE.ADDRESS_CHANGE },
  { label: '退款', value: PROCESSING_TYPE.REFUND },
  { label: '取消订单', value: PROCESSING_TYPE.CANCEL }
]

// 优先级选项
export const PRIORITY_OPTIONS = [
  { label: '低', value: PRIORITY.LOW },
  { label: '普通', value: PRIORITY.NORMAL },
  { label: '高', value: PRIORITY.HIGH },
  { label: '紧急', value: PRIORITY.URGENT }
]
