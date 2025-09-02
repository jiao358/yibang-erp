import request from '@/utils/request'
import type { 
  AIExcelProcessRequest, 
  AIExcelProcessResponse, 
  FileUploadResponse,
  TaskHistoryItem 
} from '@/types/ai'

/**
 * AI Excel订单导入API接口
 */
export const aiExcelImportApi = {
  /**
   * 上传Excel文件
   */
  uploadFile: (file: File, onProgress?: (progress: number) => void) => {
    const formData = new FormData()
    formData.append('file', file)
    
    return request.post<FileUploadResponse>('/api/ai-excel/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      }
    })
  },

  /**
   * 开始AI处理
   */
  startProcessing: (data: FormData) => {
    return request.post<AIExcelProcessResponse>('/api/ai-excel-orders/process', data, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 获取处理进度
   */
  getProgress: (taskId: string) => {
    return request.get<AIExcelProcessResponse>(`/api/ai-excel-orders/progress/${taskId}`)
  },

  /**
   * 取消处理任务
   */
  cancelProcessing: (taskId: string) => {
    return request.post(`/api/ai-excel-orders/cancel/${taskId}`)
  },

  /**
   * 获取处理结果
   */
  getResult: (taskId: string) => {
    return request.get(`/api/ai-excel-orders/result/${taskId}`)
  },

  /**
   * 获取任务历史
   */
  getTaskHistory: (params?: {
    page?: number
    size?: number
    status?: string
    startDate?: string
    endDate?: string
  }) => {
    return request.get<{
      content: TaskHistoryItem[]
      totalElements: number
      totalPages: number
      currentPage: number
    }>('/api/ai-excel-orders/tasks', { params })
  },

  /**
   * 获取任务统计信息
   */
  getTaskStatistics: (params?: {
    companyId?: number
    startDate?: string
    endDate?: string
  }) => {
    return request.get<{
      totalTasks: number
      processingTasks: number
      completedTasks: number
      failedTasks: number
      todayTasks?: number
      thisWeekTasks?: number
      thisMonthTasks?: number
    }>('/api/ai-excel-orders/statistics', { params })
  },

  /**
   * 获取失败订单列表
   */
  getFailedOrders: (taskId: string, params?: {
    page?: number
    size?: number
    sortBy?: string
    sortOrder?: 'asc' | 'desc'
  }) => {
    return request.get<{
      content: Array<{
        id: number
        taskId: string
        excelRowNumber: number
        rawData: string
        errorType: string
        errorMessage: string
        suggestedAction: string
        status: string
        createdAt: string
        updatedAt: string
      }>
      totalElements: number
      totalPages: number
      currentPage: number
      size: number
      status: string
      message: string
    }>(`/api/ai-excel-orders/${taskId}/failed-orders`, { params })
  },

  /**
   * 重新处理任务
   */
  retryTask: (taskId: string) => {
    return request.post(`/api/ai-excel-orders/retry/${taskId}`)
  },

  /**
   * 删除任务
   */
  deleteTask: (taskId: string) => {
    return request.delete(`/api/ai-excel-orders/task/${taskId}`)
  },

  /**
   * 导出处理结果
   */
  exportResults: (taskId: string, format: 'excel' | 'csv' = 'excel') => {
    return request.get(`/api/ai-excel-orders/export/${taskId}`, {
      params: { format },
      responseType: 'blob'
    })
  },

  /**
   * 下载处理日志
   */
  downloadLogs: (taskId: string) => {
    return request.get(`/api/ai-excel-orders/logs/${taskId}`, {
      responseType: 'blob'
    })
  },

  /**
   * 获取AI配置
   */
  getAIConfig: () => {
    return request.get('/api/ai-excel-orders/config')
  },

  /**
   * 更新AI配置
   */
  updateAIConfig: (config: any) => {
    return request.put('/api/ai-excel-orders/config', config)
  },

  /**
   * 测试AI连接
   */
  testAIConnection: () => {
    return request.post('/api/ai-excel-orders/test-connection')
  }
}

export default aiExcelImportApi
