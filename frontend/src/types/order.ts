// 订单响应类型
export interface OrderResponse {
  id: number
  platformOrderId: string
  customerId: number
  customerName?: string
  salesId: number
  salesUserName?: string
  supplierCompanyId?: number
  supplierCompanyName?: string
  orderType: string
  orderStatus: string
  orderSource: string
  expectedDeliveryDate: string
  approvalStatus: string
  approvalComment?: string
  approvalAt?: string
  approvalBy?: number
  totalAmount: number
  discountAmount: number
  taxAmount: number
  shippingAmount: number
  finalAmount: number
  currency: string
  paymentStatus: string
  paymentMethod: string
  paymentAt?: string
  deliveryAddress: string
  deliveryContact: string
  deliveryPhone: string
  specialRequirements?: string
  aiConfidence?: number
  aiProcessed: boolean
  createdAt: string
  updatedAt: string
  deleted: boolean
  orderItems: OrderItemResponse[]
}

// 订单项响应类型
export interface OrderItemResponse {
  id: number
  orderId: number
  productId: number
  productName?: string
  sku: string
  productSpecifications?: string
  quantity: number
  unitPrice: number
  unit: string
  discountRate?: number
  discountAmount?: number
  taxRate?: number
  taxAmount?: number
  subtotal: number
  aiMappedProductId?: number
  aiConfidence?: number
  aiProcessingStatus?: string
  aiProcessingResult?: string
  createdAt: string
  updatedAt: string
}

// 创建订单请求类型
export interface OrderCreateRequest {
  platformOrderId?: string
  customerId: number
  orderType: string
  expectedDeliveryDate: string
  currency: string
  specialRequirements?: string
  deliveryAddress: string
  deliveryContact: string
  deliveryPhone: string
  remarks?: string
  orderItems: OrderItemCreateRequest[]
  discountAmount?: number
  shippingAmount?: number
  taxAmount?: number
  finalAmount?: number
  paymentMethod: string
}

// 更新订单请求类型
export interface OrderUpdateRequest {
  customerId: number
  orderType: string
  expectedDeliveryDate: string
  currency: string
  specialRequirements?: string
  deliveryAddress: string
  deliveryContact: string
  deliveryPhone: string
  remarks?: string
  orderItems: OrderItemCreateRequest[]
  discountAmount?: number
  shippingAmount?: number
  taxAmount?: number
  finalAmount?: number
  paymentMethod: string
}

// 创建订单项请求类型
export interface OrderItemCreateRequest {
  productId: number
  productSpecifications?: string
  quantity: number
  unitPrice: number
  subtotal?: number
}

// 订单列表请求类型
export interface OrderListRequest {
  current: number
  size: number
  platformOrderId?: string
  customerName?: string
  orderStatus?: string
  orderSource?: string
  dateRange?: string[]
  salesUserId?: number
  supplierCompanyId?: number
  customerId?: number
}

// 订单状态更新请求类型
export interface OrderStatusUpdateRequest {
  orderStatus: string
  changeReason?: string
  remarks?: string
}

// 订单冲突解决请求类型
export interface OrderConflictResolutionRequest {
  resolutionType: 'REJECT' | 'MANUAL_CONFIRM' | 'AUTO_MERGE'
  selectedOrderId?: number
  comment?: string
}

// 订单批量处理请求类型
export interface OrderBatchProcessRequest {
  orderIds: number[]
  action: 'UPDATE_STATUS' | 'DELETE' | 'ASSIGN_SUPPLIER'
  data?: any
}

// 订单状态日志类型
export interface OrderStatusLog {
  id: number
  orderId: number
  orderStatus: string
  changeReason?: string
  operatorType: string
  operatorId?: number
  operatorName?: string
  operatorRole?: string
  operatedAt?: string
  isSystemOperation: boolean
  operationSource: string
  remarks?: string
  createdAt: string
  updatedAt: string
}

// 订单审批日志类型
export interface OrderApprovalLog {
  id: number
  orderId: number
  action: string
  status: string
  comment?: string
  approverId: number
  approvalAt: string
  createdAt: string
  approverName?: string
  approverRole?: string
}

// 物流信息类型
export interface LogisticsInfo {
  id: number
  orderId: number
  trackingNumber: string
  carrier: string
  carrierCode?: string
  shippingMethod: string
  shippingDate?: string
  estimatedDeliveryDate?: string
  actualDeliveryDate?: string
  shippingAddress: string
  deliveryAddress: string
  shippingContact: string
  deliveryContact: string
  shippingPhone: string
  deliveryPhone: string
  shippingNotes?: string
  deliveryNotes?: string
  status: string
  createdAt: string
  updatedAt: string
  platformOrderId?: string
}

// Excel导入日志类型
export interface ExcelImportLog {
  id: number
  fileName: string
  fileSize: number
  fileHash: string
  importStatus: string
  totalRows: number
  processedRows: number
  successRows: number
  failedRows: number
  errorMessage?: string
  aiProcessingStatus?: string
  aiProcessingResult?: string
  importUserId: number
  startedAt: string
  completedAt?: string
  createdAt: string
  updatedAt: string
  extendedFields?: string
  importUserName?: string
}

// 订单状态枚举
export enum OrderStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  SUPPLIER_CONFIRMED = 'SUPPLIER_CONFIRMED',
  SHIPPED = 'SHIPPED',
  IN_TRANSIT = 'IN_TRANSIT',
  DELIVERED = 'DELIVERED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  REJECTED = 'REJECTED'
}

// 订单来源枚举
export enum OrderSource {
  MANUAL = 'MANUAL',
  EXCEL_IMPORT = 'EXCEL_IMPORT',
  API = 'API',
  WEBSITE = 'WEBSITE',
  IMAGE_RECOGNITION = 'IMAGE_RECOGNITION',
  PDF_EXTRACTION = 'PDF_EXTRACTION',
  WORD_EXTRACTION = 'WORD_EXTRACTION',
  EMAIL_EXTRACTION = 'EMAIL_EXTRACTION',
  VOICE_RECOGNITION = 'VOICE_RECOGNITION'
}

// 订单类型枚举
export enum OrderType {
  NORMAL = 'NORMAL',
  URGENT = 'URGENT',
  SAMPLE = 'SAMPLE',
  REPAIR = 'REPAIR'
}

// 支付状态枚举
export enum PaymentStatus {
  PENDING = 'PENDING',
  PARTIAL = 'PARTIAL',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

// 支付方式枚举
export enum PaymentMethod {
  BANK_TRANSFER = 'BANK_TRANSFER',
  CASH = 'CASH',
  CHECK = 'CHECK',
  CREDIT_CARD = 'CREDIT_CARD',
  OTHER = 'OTHER'
}

// 审批状态枚举
export enum ApprovalStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  NOT_REQUIRED = 'NOT_REQUIRED'
}
