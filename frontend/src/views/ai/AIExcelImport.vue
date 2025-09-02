<template>
  <div class="ai-excel-import-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">AI Excel订单导入</h1>
      <p class="page-description">
        使用AI智能识别Excel文件内容，自动匹配商品和客户信息，批量创建订单
      </p>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 文件上传区域 -->
      <div class="upload-section">
        <h3 class="section-title">文件上传</h3>
        <FileUpload 
          @fileSelected="handleFileSelected"
          @uploadSuccess="handleUploadSuccess"
          @uploadError="handleUploadError"
        />
      </div>

      <!-- AI配置面板 -->
      <div class="config-section" v-if="selectedFile">
        <h3 class="section-title">AI配置</h3>
        <AIConfigPanel 
          v-model:config="aiConfig"
          @config-change="handleConfigChange"
        />
      </div>

      <!-- 处理进度 -->
      <div class="progress-section" v-if="isProcessing">
        <h3 class="section-title">处理进度</h3>
        <ProcessingProgress 
          :progress="progress"
          :status="processingStatus"
          @cancel-processing="handleCancelProcessing"
        />
      </div>

      <!-- 结果展示 -->
      <div class="result-section" v-if="processingResult">
        <h3 class="section-title">处理结果</h3>
        <ResultDisplay 
          :result="processingResult"
          @export-results="handleExportResults"
          @retry-processing="handleRetryProcessing"
        />
      </div>

      <!-- 错误订单展示 -->
      <div class="error-section" v-if="errorOrders.length > 0">
        <h3 class="section-title">错误订单</h3>
        <ErrorOrderDisplay 
          :task-id="currentTaskId || undefined"
          :error-orders="errorOrders"
          @refresh="loadErrorOrders"
          @create-order="handleCreateOrderFromError"
        />
      </div>

      <!-- 历史任务 -->
      <div class="history-section">
        <h3 class="section-title">历史任务</h3>
        <TaskHistory 
          :tasks="taskHistory"
          @view-detail="handleViewTaskDetail"
          @retry-task="handleRetryTask"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import FileUpload from './components/FileUpload.vue'
import AIConfigPanel from './components/AIConfigPanel.vue'
import ProcessingProgress from './components/ProcessingProgress.vue'
import ResultDisplay from './components/ResultDisplay.vue'
import TaskHistory from './components/TaskHistory.vue'
import ErrorOrderDisplay from './components/ErrorOrderDisplay.vue'
import { aiExcelImportApi } from '@/api/aiExcelImport'
import type { AIExcelConfig, ProcessingProgress as ProcessingProgressType, ProcessingResult, TaskHistoryItem, ErrorOrderInfo } from '@/types/ai'

// 响应式数据
const selectedFile = ref<File | null>(null)
const isProcessing = ref(false)
const processingStatus = ref<string>('')
const progress = ref<ProcessingProgressType | null>(null)
const processingResult = ref<ProcessingResult | null>(null)
const taskHistory = ref<TaskHistoryItem[]>([])
const errorOrders = ref<ErrorOrderInfo[]>([])

// AI配置
const aiConfig = reactive<AIExcelConfig>({
  modelType: 'deepseek',
  confidenceThreshold: 0.8,
  autoMatchStrategy: 'smart',
  enableFallback: true,
  maxRetries: 3,
  temperature: 0.1,
  maxTokens: 2000,
  timeout: 30
})

// 事件处理
const handleFileSelected = (file: File) => {
  console.log('🎯 主页面收到 fileSelected 事件:', file)
  console.log('📁 文件详情:', {
    name: file.name,
    size: file.size,
    type: file.type,
    lastModified: file.lastModified
  })
  
  selectedFile.value = file
  console.log('📁 已设置主页面selectedFile:', selectedFile.value)
  ElMessage.success(`已选择文件: ${file.name}`)
}

const handleUploadSuccess = async (response: any) => {
  console.log('🎯 主页面收到 uploadSuccess 事件:', response)
  try {
    ElMessage.success('文件上传成功，开始AI处理')
    console.log('🚀 准备启动AI处理...')
    await startAIProcessing()
  } catch (error) {
    console.error('❌ 启动AI处理失败:', error)
    ElMessage.error('启动AI处理失败')
  }
}

const handleUploadError = (error: string) => {
  ElMessage.error(`文件上传失败: ${error}`)
}

const handleConfigChange = (config: AIExcelConfig) => {
  console.log('AI配置已更新:', config)
  // 可以在这里保存配置到后端
}

const startAIProcessing = async () => {
  console.log('🚀 startAIProcessing 开始执行')
  
  if (!selectedFile.value) {
    console.error('❌ 没有选择文件，无法开始处理')
    ElMessage.error('请先选择文件')
    return
  }
  
  console.log('📁 当前选择的文件:', selectedFile.value)
      console.log('⚙️ 当前AI配置:', aiConfig)
  
  try {
    isProcessing.value = true
    processingStatus.value = 'PROCESSING'
    console.log('🔄 设置处理状态为处理中')
    
    // 创建FormData对象，包含文件和参数
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('salesUserId', getCurrentUserId().toString())
    formData.append('salesCompanyId', getCurrentCompanyId().toString())
    formData.append('priority', '3')
    formData.append('enableAIProductMatching', 'true')
    formData.append('minConfidenceThreshold', aiConfig.confidenceThreshold.toString())
    formData.append('autoCreateCustomer', 'false')
    formData.append('autoCreateProduct', 'false')
    formData.append('remarks', 'AI Excel导入')
    
    console.log('📤 准备发送FormData，文件:', selectedFile.value.name)
    
    // 调用后端API开始处理
    console.log('🌐 调用 aiExcelImportApi.startProcessing...')
    const response = await aiExcelImportApi.startProcessing(formData)
    console.log('📥 收到API响应:', response)
    
    // 检查响应结构
    if (response && response.taskId) {
      console.log('✅ 成功获取任务ID:', response.taskId)
      ElMessage.success('AI处理已启动')
      // 开始轮询进度
      startProgressPolling(response.taskId)
    } else {
      console.error('❌ 响应数据不完整:', response)
      throw new Error('启动处理失败：未获取到任务ID')
    }
    
  } catch (error) {
    console.error('❌ startAIProcessing 执行失败:', error)
    isProcessing.value = false
    processingStatus.value = 'FAILED'
    ElMessage.error(`启动AI处理失败: ${error}`)
  }
}

// 工具方法
const getCurrentUserId = (): number => {
  try {
    // 优先从localStorage获取用户信息
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (user.id) {
        console.log('📋 从localStorage获取到用户ID:', user.id)
        return user.id
      }
    }
    
    // 从JWT token解析用户信息
    const token = localStorage.getItem('token')
    if (token) {
      try {
        const parts = token.split('.')
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]))
          if (payload.userId) {
            console.log('📋 从JWT获取到用户ID:', payload.userId)
            return payload.userId
          }
        }
      } catch (e) {
        console.error('解析JWT失败:', e)
      }
    }
    
    console.warn('⚠️ 无法获取用户ID，使用默认值1')
    return 1
  } catch (error) {
    console.error('获取用户ID失败:', error)
    return 1
  }
}

const getCurrentCompanyId = (): number => {
  try {
    // 优先从localStorage获取用户信息
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (user.companyId) {
        console.log('📋 从localStorage获取到公司ID:', user.companyId)
        return user.companyId
      }
    }
    
    // 从JWT token解析用户信息
    const token = localStorage.getItem('token')
    if (token) {
      try {
        const parts = token.split('.')
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]))
          if (payload.companyId) {
            console.log('📋 从JWT获取到公司ID:', payload.companyId)
            return payload.companyId
          }
        }
      } catch (e) {
        console.error('解析JWT失败:', e)
      }
    }
    
    console.warn('⚠️ 无法获取公司ID，使用默认值1')
    return 1
  } catch (error) {
    console.error('获取公司ID失败:', error)
    return 1
  }
}

// 进度轮询相关
let progressInterval: NodeJS.Timeout | null = null
let currentTaskId: string | null = null

const startProgressPolling = (taskId: string) => {
  currentTaskId = taskId
  progressInterval = setInterval(async () => {
    try {
      const response = await aiExcelImportApi.getProgress(taskId)
      if (response.progress) {
        progress.value = response.progress
        
        // 检查是否完成
        if (response.status === 'COMPLETED' || response.status === 'FAILED') {
          clearProgressPolling()
          isProcessing.value = false
          processingStatus.value = response.status
          
          // 获取处理结果
          await loadProcessingResult(taskId)
        }
      }
    } catch (error) {
      console.error('获取进度失败:', error)
    }
  }, 2000) // 每2秒轮询一次
}

const clearProgressPolling = () => {
  if (progressInterval) {
    clearInterval(progressInterval)
    progressInterval = null
  }
}

// 加载处理结果
const loadProcessingResult = async (taskId: string) => {
  console.log('📥 开始加载处理结果，任务ID:', taskId)
  try {
    const response = await aiExcelImportApi.getResult(taskId)
    console.log('📥 获取到处理结果:', response)
    processingResult.value = response
  } catch (error) {
    console.error('❌ 加载处理结果失败:', error)
    ElMessage.error('加载处理结果失败')
  }
}

const handleCancelProcessing = async () => {
  if (!currentTaskId) return
  
  try {
    await aiExcelImportApi.cancelProcessing(currentTaskId)
    clearProgressPolling()
    isProcessing.value = false
    processingStatus.value = 'CANCELLED'
    ElMessage.info('处理已取消')
  } catch (error) {
    ElMessage.error('取消处理失败')
  }
}

const handleExportResults = () => {
  // TODO: 实现结果导出
  ElMessage.success('结果导出功能待实现')
}

const handleRetryProcessing = () => {
  if (selectedFile.value) {
    startAIProcessing()
  }
}

const handleViewTaskDetail = (taskId: string) => {
  // TODO: 查看任务详情
  console.log('查看任务详情:', taskId)
}

const handleRetryTask = (taskId: string) => {
  // TODO: 重试任务
  console.log('重试任务:', taskId)
}

// 生命周期
onMounted(() => {
  console.log('🚀 AIExcelImport 主页面已挂载')
  console.log('📋 事件处理器状态:', {
    handleFileSelected: typeof handleFileSelected,
    handleUploadSuccess: typeof handleUploadSuccess,
    handleUploadError: typeof handleUploadError
  })
  
  // 显示当前用户信息
  console.log('👤 当前用户信息:', {
    userId: getCurrentUserId(),
    companyId: getCurrentCompanyId(),
    localStorage: {
      userInfo: localStorage.getItem('userInfo') ? '已设置' : '未设置',
      token: localStorage.getItem('token') ? '已设置' : '未设置',
      userRoles: localStorage.getItem('userRoles') ? '已设置' : '未设置'
    }
  })
  
  // TODO: 加载历史任务
  loadTaskHistory()
})

const loadTaskHistory = () => {
  // TODO: 从后端加载历史任务
  taskHistory.value = []
}

const loadErrorOrders = async () => {
  if (!currentTaskId) return
  
  try {
    console.log('📋 加载错误订单，任务ID:', currentTaskId)
    // TODO: 调用后端API获取错误订单
    // const response = await aiExcelImportApi.getErrorOrders(currentTaskId)
    // errorOrders.value = response.data.errorOrders || []
  } catch (error) {
    console.error('加载错误订单失败:', error)
    ElMessage.error('加载错误订单失败')
  }
}

const handleCreateOrderFromError = (errorOrder: ErrorOrderInfo) => {
  console.log('📋 从错误订单创建订单:', errorOrder)
  ElMessage.info('手动创建订单功能开发中...')
  // TODO: 实现手动创建订单的逻辑
}
</script>

<style scoped>
.ai-excel-import-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin: 0;
  line-height: 1.5;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #e4e7ed;
}

.upload-section,
.config-section,
.progress-section,
.result-section,
.history-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.config-section {
  border-left: 4px solid #409eff;
}

.progress-section {
  border-left: 4px solid #67c23a;
}

.result-section {
  border-left: 4px solid #e6a23c;
}

.error-section {
  border-left: 4px solid #f56c6c;
}

.history-section {
  border-left: 4px solid #909399;
}
</style>
