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
  AIHistoryPageResult
} from '@/types/ai'

// 获取AI配置信息
export function getAIConfig() {
  return request.get<AIConfigResponse>('/api/ai/config')
}

// 更新AI配置
export function updateAIConfig(data: AIConfigRequest) {
  return request.put<AIConfigResponse>('/api/ai/config', data)
}

// 测试AI连接
export function testAIConnection() {
  return request.post<boolean>('/api/ai/config/test')
}

// 处理单个AI订单
export function processAIOrder(data: AIOrderProcessRequest) {
  return request.post<AIOrderProcessResult>('/api/ai/orders/process', data)
}

// 批量处理AI订单
export function batchProcessAIOrders(data: AIOrderProcessRequest[]) {
  return request.post<AIOrderProcessResult[]>('/api/ai/orders/batch-process', data)
}

// 从Excel文件批量处理订单
export function processOrdersFromExcel(file: File, companyId: number) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('companyId', companyId.toString())
  
  return request.post<AIOrderProcessResult[]>('/api/ai/orders/excel-process', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取AI处理统计信息
export function getAIProcessStatistics(companyId: number) {
  return request.get<AIProcessStatistics>('/api/ai/statistics', {
    params: { companyId }
  })
}

// 获取AI处理历史
export function getAIProcessHistory(params: AIHistoryQueryParams) {
  return request.get<AIHistoryPageResult>('/api/ai/history', {
    params
  })
}

// 获取AI健康状态
export function getAIHealth() {
  return request.get<string>('/api/ai/health')
}

// 获取可用的AI处理类型
export function getAIProcessTypes() {
  return request.get<Array<{ value: string; label: string; description: string }>>('/api/ai/process-types')
}

// 获取AI模型信息
export function getAIModels() {
  return request.get<Array<{ name: string; description: string; maxTokens: number }>>('/api/ai/models')
}

// 取消AI处理任务
export function cancelAIProcess(processId: number) {
  return request.post<boolean>(`/api/ai/orders/${processId}/cancel`)
}

// 重新处理AI任务
export function retryAIProcess(processId: number) {
  return request.post<AIOrderProcessResult>(`/api/ai/orders/${processId}/retry`)
}

// 获取AI处理详情
export function getAIProcessDetail(processId: number) {
  return request.get<AIOrderProcessResult>(`/api/ai/orders/${processId}`)
}

// 导出AI处理历史
export function exportAIProcessHistory(params: AIHistoryQueryParams) {
  return request.get<Blob>('/api/ai/history/export', {
    params,
    responseType: 'blob'
  })
}

// 获取AI使用配额
export function getAIUsageQuota() {
  return request.get<{
    totalTokens: number
    usedTokens: number
    remainingTokens: number
    resetDate: string
  }>('/api/ai/usage/quota')
}

// 获取AI使用统计
export function getAIUsageStatistics(startDate: string, endDate: string) {
  return request.get<Array<{
    date: string
    tokensUsed: number
    requestsCount: number
    cost: number
  }>>('/api/ai/usage/statistics', {
    params: { startDate, endDate }
  })
}
