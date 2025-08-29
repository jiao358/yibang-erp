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
