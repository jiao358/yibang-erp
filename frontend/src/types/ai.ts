/**
 * AI相关类型定义
 */

// AI配置响应类型
export interface AIConfigResponse {
  configId?: number
  baseUrl: string
  apiKeyMasked: string
  defaultModel: string
  maxTokens: number
  temperature: number
  enabled: boolean
  timeout: number
  configStatus: string
  lastTestTime?: string
  lastTestResult?: boolean
  configUserId?: number
  configUserName?: string
  configTime?: string
  updatedAt?: string
  configured: boolean
  configDescription?: string
}

// AI配置请求类型
export interface AIConfigRequest {
  baseUrl?: string
  apiKey: string
  defaultModel?: string
  maxTokens?: number
  temperature?: number
  enabled?: boolean
  timeout?: number
  configUserId: number
}

// AI订单处理请求类型
export interface AIOrderProcessRequest {
  orderId: number
  companyId: number
  processType: string
  description?: string
  userId: number
  priority?: number
  customParams?: string
}

// AI订单处理结果类型
export interface AIOrderProcessResult {
  processId: number
  orderId: number
  companyId: number
  processType: string
  status: string
  result: string
  description?: string
  modelName: string
  processingTime: number
  userId: number
  userName?: string
  errorMessage?: string
  startTime: string
  endTime?: string
  createdAt: string
  success: boolean
  priority?: number
  customParams?: string
}

// AI处理统计信息类型
export interface AIProcessStatistics {
  totalCount: number
  successCount: number
  pendingCount: number
  failedCount: number
  averageProcessingTime: number
  todayCount: number
  thisWeekCount: number
  thisMonthCount: number
  topProcessTypes: Array<{
    type: string
    count: number
    successRate: number
  }>
}

// AI处理历史查询参数类型
export interface AIHistoryQueryParams {
  page: number
  size: number
  companyId: number
  processType?: string
  status?: string
  startDate?: string
  endDate?: string
  userId?: number
}

// AI处理历史分页结果类型
export interface AIHistoryPageResult {
  records: AIOrderProcessResult[]
  total: number
  size: number
  current: number
  pages: number
}

// Excel导入结果类型
export interface ExcelImportResult {
  totalCount: number
  successCount: number
  failedCount: number
  results: AIOrderProcessResult[]
  errors: Array<{
    row: number
    message: string
  }>
}

// AI模型信息类型
export interface AIModelInfo {
  name: string
  description: string
  maxTokens: number
  supportedFeatures: string[]
  pricing: {
    input: number
    output: number
    currency: string
  }
}

// AI处理类型选项
export interface AIProcessTypeOption {
  value: string
  label: string
  description: string
  icon: string
  category: string
}

// AI处理优先级类型
export enum AIProcessPriority {
  LOW = 1,
  NORMAL = 2,
  HIGH = 3,
  URGENT = 4
}

// AI处理状态类型
export enum AIProcessStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

// AI配置状态类型
export enum AIConfigStatus {
  NOT_CONFIGURED = 'NOT_CONFIGURED',
  CONFIGURED = 'CONFIGURED',
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
  ERROR = 'ERROR'
}

// AI Excel配置类型
export interface AIExcelConfig {
  modelType: 'deepseek' | 'gpt4' | 'claude'
  confidenceThreshold: number
  autoMatchStrategy: 'strict' | 'smart' | 'loose'
  enableFallback: boolean
  maxRetries: number
  temperature: number
  maxTokens: number
  timeout: number
}

// 错误订单信息类型
export interface ErrorOrderInfo {
  id: number
  taskId: string
  excelRowNumber: number
  rawData: Record<string, any>
  errorType: string
  errorTypeLabel?: string
  errorMessage: string
  errorLevel: string
  suggestedAction: string
  status: string
  statusLabel?: string
  processedBy?: number
  processedByName?: string
  processedAt?: string
  createdAt: string
  updatedAt: string
}

// 处理进度类型
export interface ProcessingProgress {
  totalRows: number
  processedRows: number
  successRows: number
  failedRows: number
  manualProcessRows: number
  currentStep: string
  percentage: number
  estimatedTime: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'
}

// 处理结果类型
export interface ProcessingResult {
  taskId: string
  status: string
  totalRows: number
  successRows: number
  failedRows: number
  manualProcessRows: number
  processingTime: number
  createdAt: string
  completedAt: string
  details: ProcessingDetail[]
}

// 处理详情类型
export interface ProcessingDetail {
  rowNumber: number
  status: 'SUCCESS' | 'FAILED' | 'MANUAL_PROCESS'
  customerMatch: CustomerMatchResult
  productMatch: ProductMatchResult
  confidence: number
  errorMessage?: string
  orderId?: string
}

// 客户匹配结果类型
export interface CustomerMatchResult {
  matched: boolean
  customerId?: number
  customerName?: string
  customerCode?: string
  confidence: number
  matchReason: string
  suggestion?: string
}

// 商品匹配结果类型
export interface ProductMatchResult {
  matched: boolean
  productId?: number
  productName?: string
  sku?: string
  specification?: string
  confidence: number
  matchReason: string
  suggestion?: string
}

// 任务历史类型
export interface TaskHistoryItem {
  taskId: string
  fileName: string
  status: string
  totalRows: number
  successRows: number
  failedRows: number
  createdAt: string
  completedAt?: string
  processingTime?: number
  // 新增字段
  supplier?: string
  fileSize?: number
  uploadUser?: string
  startedAt?: string
  manualProcessRows?: number
  // 弹窗滚动位置记录
  currentScrollPosition?: number
  // 原始body样式记录
  originalBodyStyle?: {
    overflow: string
    paddingRight: string
    position: string
  }
  // 滚动阻止器引用
  scrollPreventer?: (e: Event) => boolean
}

// AI Excel处理请求类型
export interface AIExcelProcessRequest {
  salesUserId: number
  salesCompanyId: number
  aiConfig: AIExcelConfig
  fileName: string
  fileSize: number
}

// AI Excel处理响应类型
export interface AIExcelProcessResponse {
  taskId: string
  status: string
  message: string
  createdAt: string
  startedAt?: string
  completedAt?: string
  progress?: ProcessingProgress
  statistics?: ProcessStatistics
}

// 处理统计类型
export interface ProcessStatistics {
  totalOrders: number
  successOrders: number
  failedOrders: number
  manualProcessOrders: number
}

// 文件上传响应类型
export interface FileUploadResponse {
  success: boolean
  message: string
  fileId?: string
  fileName?: string
  fileSize?: number
}

// AI模型状态类型
export interface AIModelStatus {
  modelType: string
  status: 'available' | 'unavailable' | 'error'
  lastTestTime?: string
  responseTime?: number
  errorMessage?: string
}

// 字段识别结果类型
export interface FieldRecognitionResult {
  columnIndex: number
  originalHeader: string
  recognizedField: string
  fieldType: string
  confidence: number
  description: string
}

// 列映射结果类型
export interface ColumnMappingResult {
  columnMappings: FieldRecognitionResult[]
  confidence: number
  reasoning: string
}

// 任务筛选表单类型
export interface TaskFilterForm {
  status: string
  dateRange: string[]
  fileName: string
  sortBy: string
  minRows?: number
  maxRows?: number
  successRate: string
  processingDuration: string
}
