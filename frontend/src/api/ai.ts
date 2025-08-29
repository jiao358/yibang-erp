/**
 * AI相关API接口
 */
import request from '@/utils/request'
import type {
  AIConfigResponse,
  AIConfigRequest,
  AIOrderProcessRequest,
  AIOrderProcessResult,
  AIProcessStatistics,
  AIHistoryQueryParams,
  AIHistoryPageResult,
  ExcelImportResult
} from '@/types/ai'

// 获取AI配置信息
export function getAIConfig() {
  return request<AIConfigResponse>({
    url: '/api/ai/config',
    method: 'get'
  })
}

// 更新AI配置
export function updateAIConfig(data: AIConfigRequest) {
  return request<AIConfigResponse>({
    url: '/api/ai/config',
    method: 'put',
    data
  })
}

// 测试AI连接
export function testAIConnection() {
  return request<boolean>({
    url: '/api/ai/config/test',
    method: 'post'
  })
}

// 处理单个AI订单
export function processAIOrder(data: AIOrderProcessRequest) {
  return request<AIOrderProcessResult>({
    url: '/api/ai/orders/process',
    method: 'post',
    data
  })
}

// 批量处理AI订单
export function batchProcessAIOrders(data: AIOrderProcessRequest[]) {
  return request<AIOrderProcessResult[]>({
    url: '/api/ai/orders/batch-process',
    method: 'post',
    data
  })
}

// 从Excel文件批量处理订单
export function processOrdersFromExcel(file: File, companyId: number) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('companyId', companyId.toString())
  
  return request<AIOrderProcessResult[]>({
    url: '/api/ai/orders/excel-process',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取AI处理统计信息
export function getAIProcessStatistics(companyId: number) {
  return request<AIProcessStatistics>({
    url: '/api/ai/statistics',
    method: 'get',
    params: { companyId }
  })
}

// 获取AI处理历史
export function getAIProcessHistory(params: AIHistoryQueryParams) {
  return request<AIHistoryPageResult>({
    url: '/api/ai/history',
    method: 'get',
    params
  })
}

// 获取AI健康状态
export function getAIHealth() {
  return request<string>({
    url: '/api/ai/health',
    method: 'get'
  })
}

// 获取可用的AI处理类型
export function getAIProcessTypes() {
  return request<Array<{ value: string; label: string; description: string }>>({
    url: '/api/ai/process-types',
    method: 'get'
  })
}

// 获取AI模型信息
export function getAIModels() {
  return request<Array<{ name: string; description: string; maxTokens: number }>>({
    url: '/api/ai/models',
    method: 'get'
  })
}

// 取消AI处理任务
export function cancelAIProcess(processId: number) {
  return request<boolean>({
    url: `/api/ai/orders/${processId}/cancel`,
    method: 'post'
  })
}

// 重新处理AI任务
export function retryAIProcess(processId: number) {
  return request<AIOrderProcessResult>({
    url: `/api/ai/orders/${processId}/retry`,
    method: 'post'
  })
}

// 获取AI处理详情
export function getAIProcessDetail(processId: number) {
  return request<AIOrderProcessResult>({
    url: `/api/ai/orders/${processId}`,
    method: 'get'
  })
}

// 导出AI处理历史
export function exportAIProcessHistory(params: AIHistoryQueryParams) {
  return request<Blob>({
    url: '/api/ai/history/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 获取AI使用配额
export function getAIUsageQuota() {
  return request<{
    totalTokens: number
    usedTokens: number
    remainingTokens: number
    resetDate: string
  }>({
    url: '/api/ai/usage/quota',
    method: 'get'
  })
}

// 获取AI使用统计
export function getAIUsageStatistics(startDate: string, endDate: string) {
  return request<Array<{
    date: string
    tokensUsed: number
    requestsCount: number
    cost: number
  }>>({
    url: '/api/ai/usage/statistics',
    method: 'get',
    params: { startDate, endDate }
  })
}
