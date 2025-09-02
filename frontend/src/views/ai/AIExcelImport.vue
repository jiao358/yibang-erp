<template>
  <div class="ai-excel-import-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">AI Excel任务管理中心</h1>
      <p class="page-description">
        使用AI智能识别Excel文件内容，自动匹配商品和客户信息，批量创建订单
      </p>
      
      <!-- 开发模式开关 -->
      <div class="dev-mode-switch">
        <el-switch
          v-model="devMode"
          active-text="开发模式"
          inactive-text="生产模式"
          @change="handleDevModeChange"
        />
        <span class="dev-mode-hint">
          {{ devMode ? '当前使用模拟数据' : '当前使用真实API' }}
        </span>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 任务概览 -->
      <TaskOverview 
        :statistics="statistics"
        @upload-new="showUploadDialog = true"
        @refresh="loadTaskHistory"
        @view-all="scrollToTable"
      />

      <!-- 任务列表 - 提前显示，减少用户滚动 -->
      <div class="task-section">
        <div class="section-header">
          <h3>任务列表</h3>
          <div class="section-actions">
            <el-button @click="loadTaskHistory" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
        
        <TaskTable 
          :tasks="filteredTasks"
          :loading="loading"
          :total-tasks="totalTasks"
          @refresh="loadTaskHistory"
          @export="exportTasks"
          @view-detail="viewTaskDetail"
          @retry-task="retryTask"
          @delete-task="deleteTask"
          @selection-change="handleSelectionChange"
        />
      </div>

      <!-- 任务筛选 - 移到任务列表下方 -->
      <TaskFilter 
        v-model="filterForm"
        @filter-change="handleFilterChange"
      />

      <!-- 任务详情弹窗 -->
      <TaskDetailDialog 
        v-model="detailDialogVisible"
        :task-detail="selectedTask"
        @view-results="viewTaskResults"
        @retry-task="retryTask"
        @download-results="downloadResults"
        @view-logs="viewLogs"
        @delete-task="deleteTask"
        @close="handleDetailDialogClose"
      />

      <!-- 文件上传弹窗 -->
      <el-dialog
        v-model="showUploadDialog"
        title="上传Excel文件"
        width="600px"
        :before-close="handleCloseUploadDialog"
      >
        <div class="upload-dialog-content">
          <FileUpload 
            @fileSelected="handleFileSelected"
            @uploadSuccess="handleUploadSuccess"
            @uploadError="handleUploadError"
          />
          
          <div v-if="selectedFile" class="ai-config-section">
            <h4>AI配置</h4>
            <AIConfigPanel 
              v-model:config="aiConfig"
              @config-change="handleConfigChange"
            />
          </div>
        </div>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handleCloseUploadDialog">取消</el-button>
            <el-button 
              type="primary" 
              :disabled="!selectedFile"
              @click="startProcessing"
            >
              开始处理
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 处理进度弹窗 -->
      <el-dialog
        v-model="showProgressDialog"
        title="处理进度"
        width="500px"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
      >
        <ProcessingProgress 
          :progress="progress"
          :status="processingStatus"
          @cancel-processing="handleCancelProcessing"
        />
        
        <template #footer>
          <div class="dialog-footer">
            <el-button 
              v-if="processingStatus === 'PROCESSING'"
              @click="handleCancelProcessing"
            >
              取消处理
            </el-button>
            <el-button 
              v-else
              type="primary" 
              @click="showProgressDialog = false"
            >
              确定
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import TaskOverview from './components/TaskOverview.vue'
import TaskFilter from './components/TaskFilter.vue'
import TaskTable from './components/TaskTable.vue'
import TaskDetailDialog from './components/TaskDetailDialog.vue'
import FileUpload from './components/FileUpload.vue'
import AIConfigPanel from './components/AIConfigPanel.vue'
import ProcessingProgress from './components/ProcessingProgress.vue'
import { aiExcelImportApi } from '@/api/aiExcelImport'
import type { 
  AIExcelConfig, 
  ProcessingProgress as ProcessingProgressType, 
  TaskHistoryItem,
  TaskFilterForm
} from '@/types/ai'

// 响应式数据
const loading = ref(false)
const showUploadDialog = ref(false)
const showProgressDialog = ref(false)
const selectedFile = ref<File | null>(null)
const processingStatus = ref<string>('')
const progress = ref<ProcessingProgressType | null>(null)
const taskHistory = ref<TaskHistoryItem[]>([])
const totalTasks = ref(0)
const detailDialogVisible = ref(false)
const selectedTask = ref<TaskHistoryItem | null>(null)
const devMode = ref(false) // 新增开发模式开关

// 统计数据
const statistics = ref({
  totalTasks: 0,
  processingTasks: 0,
  completedTasks: 0,
  failedTasks: 0
})

// 筛选表单
const filterForm = reactive<TaskFilterForm>({
  status: '',
  dateRange: [],
  fileName: '',
  sortBy: 'createdAt',
  minRows: undefined,
  maxRows: undefined,
  successRate: '',
  processingDuration: ''
})

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

// 计算属性
const filteredTasks = computed(() => {
  let tasks = [...taskHistory.value]
  
  // 状态筛选
  if (filterForm.status) {
    tasks = tasks.filter(task => task.status === filterForm.status)
  }
  
  // 文件名筛选
  if (filterForm.fileName) {
    tasks = tasks.filter(task => 
      task.fileName.toLowerCase().includes(filterForm.fileName.toLowerCase())
    )
  }
  
  // 时间范围筛选
  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    const [startDate, endDate] = filterForm.dateRange
    tasks = tasks.filter(task => {
      const taskDate = new Date(task.createdAt)
      const start = new Date(startDate)
      const end = new Date(endDate)
      return taskDate >= start && taskDate <= end
    })
  }
  
  // 行数筛选
  if (filterForm.minRows !== undefined) {
    tasks = tasks.filter(task => task.totalRows >= filterForm.minRows!)
  }
  if (filterForm.maxRows !== undefined) {
    tasks = tasks.filter(task => task.totalRows <= filterForm.maxRows!)
  }
  
  // 成功率筛选
  if (filterForm.successRate) {
    const rate = parseInt(filterForm.successRate.replace('+', ''))
    tasks = tasks.filter(task => {
      const successRate = (task.successRows / task.totalRows) * 100
      return successRate >= rate
    })
  }
  
  // 排序
  tasks.sort((a, b) => {
    switch (filterForm.sortBy) {
      case 'fileName':
        return a.fileName.localeCompare(b.fileName)
      case 'status':
        return a.status.localeCompare(b.status)
      case 'processingTime':
        return (a.processingTime || 0) - (b.processingTime || 0)
      case 'createdAt':
      default:
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    }
  })
  
  return tasks
})

// 事件处理
const handleFileSelected = (file: File) => {
  selectedFile.value = file
  ElMessage.success(`已选择文件: ${file.name}`)
}

const handleUploadSuccess = (response: any) => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error: string) => {
  ElMessage.error(`文件上传失败: ${error}`)
}

const handleConfigChange = (config: AIExcelConfig) => {
  console.log('AI配置已更新:', config)
}

const handleFilterChange = (filters: TaskFilterForm) => {
  console.log('筛选条件已更新:', filters)
  // 可以在这里添加额外的筛选逻辑
}

const handleSelectionChange = (selectedTasks: TaskHistoryItem[]) => {
  console.log('选中的任务:', selectedTasks)
}

const handleCloseUploadDialog = () => {
  showUploadDialog.value = false
  selectedFile.value = null
}

const startProcessing = async () => {
  if (!selectedFile.value) {
    ElMessage.error('请先选择文件')
    return
  }
  
  try {
    showUploadDialog.value = false
    showProgressDialog.value = true
    processingStatus.value = 'PROCESSING'
    
    // 创建FormData对象
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
    
    // 调用后端API开始处理
    const response = await aiExcelImportApi.startProcessing(formData)
    
    if (response && response.taskId) {
      ElMessage.success('AI处理已启动')
      startProgressPolling(response.taskId)
    } else {
      throw new Error('启动处理失败：未获取到任务ID')
    }
    
  } catch (error: any) {
    console.error('启动AI处理失败:', error)
    processingStatus.value = 'FAILED'
    ElMessage.error(`启动AI处理失败: ${error.message || error}`)
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
          processingStatus.value = response.status
          
          // 重新加载任务历史
          await loadTaskHistory()
        }
      }
      } catch (error: any) {
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

const handleCancelProcessing = async () => {
  if (!currentTaskId) return
  
  try {
    await aiExcelImportApi.cancelProcessing(currentTaskId)
    clearProgressPolling()
    processingStatus.value = 'CANCELLED'
    ElMessage.info('处理已取消')
  } catch (error: any) {
    ElMessage.error(`取消处理失败: ${error.message || '未知错误'}`)
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    console.log('📊 开始加载统计数据...')
    
    if (devMode.value) {
      console.log('🧪 使用模拟统计数据')
      statistics.value = {
        totalTasks: 12,
        processingTasks: 3,
        completedTasks: 8,
        failedTasks: 1
      }
      return
    }
    
    const response = await aiExcelImportApi.getTaskStatistics({})
    console.log('📊 统计API响应:', response)
    
    if (response) {
      statistics.value = {
        totalTasks: response.totalTasks || 0,
        processingTasks: response.processingTasks || 0,
        completedTasks: response.completedTasks || 0,
        failedTasks: response.failedTasks || 0
      }
      console.log('✅ 统计数据加载成功:', statistics.value)
    }
  } catch (error: any) {
    console.error('❌ 加载统计数据失败:', error)
    // 使用默认值
    statistics.value = {
      totalTasks: 0,
      processingTasks: 0,
      completedTasks: 0,
      failedTasks: 0
    }
  }
}

// 任务管理相关
const loadTaskHistory = async () => {
  try {
    console.log('🔄 开始加载任务历史...')
    loading.value = true
    
    // 检查用户认证状态
    const token = localStorage.getItem('token')
    if (!token) {
      console.warn('⚠️ 未找到用户Token')
      ElMessage.warning('请先登录')
      return
    }
    
    console.log('🔐 Token验证通过，开始请求API...')
    
    // 同时加载任务历史和统计数据
    const [taskResponse, statsResponse] = await Promise.all([
      aiExcelImportApi.getTaskHistory({
        page: 1,
        size: 1000
      }),
      loadStatistics()
    ])
    
    const response = taskResponse
    
    console.log('📥 API响应:', response)
    
    if (response && response.content) {
      taskHistory.value = response.content
      totalTasks.value = response.totalElements || 0
      console.log(`✅ 成功加载 ${taskHistory.value.length} 个任务`)
    } else {
      console.warn('⚠️ API响应数据格式异常:', response)
      taskHistory.value = []
      totalTasks.value = 0
      ElMessage.warning('任务数据格式异常，已显示空列表')
    }
  } catch (error: any) {
    console.error('❌ 加载任务历史失败:', error)
    
    // 根据错误类型提供不同的错误信息
    if (error.response) {
      const { status, data } = error.response
      console.error(`HTTP ${status}:`, data)
      
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          break
        case 403:
          ElMessage.error('没有权限访问此功能')
          break
        case 404:
          ElMessage.error('任务历史接口不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误，请稍后重试')
          break
        default:
          ElMessage.error(`请求失败 (${status}): ${data?.message || '未知错误'}`)
      }
    } else if (error.request) {
      console.error('网络请求失败:', error.request)
      ElMessage.error('网络连接失败，请检查网络设置')
    } else {
      console.error('其他错误:', error.message)
      ElMessage.error(`加载失败: ${error.message}`)
    }
    
    // 使用模拟数据确保界面能正常显示
    console.log('📋 使用模拟数据...')
    taskHistory.value = getMockTaskData()
    totalTasks.value = taskHistory.value.length
    ElMessage.info('当前显示模拟数据，请检查后端接口')
  } finally {
    loading.value = false
    console.log('🏁 任务历史加载完成')
  }
}

// 模拟数据生成函数
const getMockTaskData = (): TaskHistoryItem[] => {
  return [
    {
      taskId: 'mock-task-001',
      fileName: '青岛啤酒订单.xlsx',
      status: 'COMPLETED',
      totalRows: 150,
      successRows: 145,
      failedRows: 5,
      createdAt: '2024-01-15T10:00:00Z',
      completedAt: '2024-01-15T10:05:00Z',
      processingTime: 300000, // 5分钟
      supplier: '青岛啤酒',
      fileSize: 1024000, // 1MB
      uploadUser: '张三'
    },
    {
      taskId: 'mock-task-002',
      fileName: '雪花啤酒订单.xlsx',
      status: 'PROCESSING',
      totalRows: 200,
      successRows: 120,
      failedRows: 3,
      createdAt: '2024-01-15T11:00:00Z',
      processingTime: 180000, // 3分钟
      supplier: '雪花啤酒',
      fileSize: 1536000, // 1.5MB
      uploadUser: '李四'
    },
    {
      taskId: 'mock-task-003',
      fileName: '燕京啤酒订单.xlsx',
      status: 'FAILED',
      totalRows: 80,
      successRows: 0,
      failedRows: 80,
      createdAt: '2024-01-15T12:00:00Z',
      processingTime: 60000, // 1分钟
      supplier: '燕京啤酒',
      fileSize: 512000, // 0.5MB
      uploadUser: '王五'
    },
    {
      taskId: 'mock-task-004',
      fileName: '百威啤酒订单.xlsx',
      status: 'PENDING',
      totalRows: 100,
      successRows: 0,
      failedRows: 0,
      createdAt: '2024-01-15T13:00:00Z',
      supplier: '百威啤酒',
      fileSize: 768000, // 0.75MB
      uploadUser: '赵六'
    }
  ]
}

const viewTaskDetail = (taskId: string) => {
  const task = taskHistory.value.find(t => t.taskId === taskId)
  if (task) {
    selectedTask.value = task
    detailDialogVisible.value = true
    
    // 记录当前滚动位置
    const currentScrollY = window.scrollY
    selectedTask.value.currentScrollPosition = currentScrollY
    
    // 锁定主页面滚动 - 使用更强力的方法
    const scrollBarWidth = window.innerWidth - document.documentElement.clientWidth
    
    // 保存原始样式
    const originalStyle = {
      overflow: document.body.style.overflow,
      paddingRight: document.body.style.paddingRight,
      position: document.body.style.position
    }
    
    // 设置锁定样式
    document.body.style.overflow = 'hidden'
    document.body.style.paddingRight = `${scrollBarWidth}px`
    document.body.style.position = 'relative'
    
    // 保存原始样式到任务对象中，以便恢复
    selectedTask.value.originalBodyStyle = originalStyle
    
    // 添加更强力的滚动阻止 - 通过事件监听器
    const preventScroll = (e: Event) => {
      e.preventDefault()
      e.stopPropagation()
      return false
    }
    
    // 保存事件监听器引用，以便移除
    selectedTask.value.scrollPreventer = preventScroll
    
    // 添加事件监听器到多个元素
    document.addEventListener('wheel', preventScroll, { passive: false })
    document.addEventListener('touchmove', preventScroll, { passive: false })
    document.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowUp' || e.key === 'ArrowDown' || e.key === 'PageUp' || e.key === 'PageDown' || e.key === 'Home' || e.key === 'End') {
        e.preventDefault()
        return false
      }
    })
  }
}

const handleDetailDialogClose = () => {
  // 弹窗关闭后恢复主页面滚动位置和状态
  if (selectedTask.value?.currentScrollPosition !== undefined) {
    const scrollPosition = selectedTask.value.currentScrollPosition
    const originalStyle = selectedTask.value.originalBodyStyle
    
    // 恢复body样式
    if (originalStyle) {
      document.body.style.overflow = originalStyle.overflow
      document.body.style.paddingRight = originalStyle.paddingRight
      document.body.style.position = originalStyle.position
    } else {
      // 如果没有保存的样式，使用默认值
      document.body.style.overflow = ''
      document.body.style.paddingRight = ''
      document.body.style.position = ''
    }
    
    // 恢复滚动位置
    setTimeout(() => {
      window.scrollTo(0, scrollPosition)
    }, 50)
    
    // 移除事件监听器
    if (selectedTask.value.scrollPreventer) {
      document.removeEventListener('wheel', selectedTask.value.scrollPreventer)
      document.removeEventListener('touchmove', selectedTask.value.scrollPreventer)
      // 移除键盘事件监听器
      document.removeEventListener('keydown', selectedTask.value.scrollPreventer)
    }
    
    // 清理临时数据
    selectedTask.value.currentScrollPosition = undefined
    selectedTask.value.originalBodyStyle = undefined
    selectedTask.value.scrollPreventer = undefined
  }
}

const retryTask = async (taskId: string) => {
  try {
    console.log(`🔄 开始重试任务: ${taskId}`)
    await aiExcelImportApi.retryTask(taskId)
    ElMessage.success('任务重新处理已启动')
    await loadTaskHistory()
  } catch (error: any) {
    console.error('❌ 重新处理任务失败:', error)
    ElMessage.error(`重新处理任务失败: ${error.message || '未知错误'}`)
  }
}

const deleteTask = async (taskId: string) => {
  try {
    console.log(`🗑️ 开始删除任务: ${taskId}`)
    await aiExcelImportApi.deleteTask(taskId)
    ElMessage.success('任务删除成功')
    await loadTaskHistory()
  } catch (error: any) {
    console.error('❌ 删除任务失败:', error)
    ElMessage.error(`删除任务失败: ${error.message || '未知错误'}`)
  }
}

const exportTasks = () => {
  ElMessage.info('导出功能开发中...')
}

const viewTaskResults = (taskId: string) => {
  ElMessage.info('查看结果功能开发中...')
}

const downloadResults = (taskId: string) => {
  ElMessage.info('下载结果功能开发中...')
}

const viewLogs = (taskId: string) => {
  ElMessage.info('查看日志功能开发中...')
}

const scrollToTable = () => {
  // 滚动到任务列表区域
  const taskSection = document.querySelector('.task-section')
  if (taskSection) {
    taskSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 工具方法
const getCurrentUserId = (): number => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (user.id) return user.id
    }
    
    const token = localStorage.getItem('token')
    if (token) {
      try {
        const parts = token.split('.')
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]))
          if (payload.userId) return payload.userId
        }
      } catch (e: any) {
        console.error('解析JWT失败:', e)
      }
    }
    
    return 1
  } catch (error: any) {
    console.error('获取用户ID失败:', error)
    return 1
  }
}

const getCurrentCompanyId = (): number => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (user.companyId) return user.companyId
    }
    
    const token = localStorage.getItem('token')
    if (token) {
      try {
        const parts = token.split('.')
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]))
          if (payload.companyId) return payload.companyId
        }
      } catch (e: any) {
        console.error('解析JWT失败:', e)
      }
    }
    
    return 1
  } catch (error: any) {
    console.error('获取公司ID失败:', error)
    return 1
  }
}

// 监听弹窗状态变化，确保弹窗关闭时恢复滚动位置
watch(detailDialogVisible, (newValue) => {
  if (!newValue && selectedTask.value?.currentScrollPosition !== undefined) {
    // 弹窗关闭时自动恢复滚动位置
    handleDetailDialogClose()
  }
})

// 生命周期
onMounted(() => {
  console.log('🚀 AIExcelImport 主页面已挂载')
  loadTaskHistory()
})

onUnmounted(() => {
  clearProgressPolling()
})

// 开发模式切换
const handleDevModeChange = (value: boolean) => {
  devMode.value = value
  if (value) {
    console.log('切换到开发模式，使用模拟数据')
    taskHistory.value = getMockTaskData()
    totalTasks.value = taskHistory.value.length
    ElMessage.info('已切换到开发模式，当前使用模拟数据')
  } else {
    console.log('切换到生产模式，使用真实API')
    loadTaskHistory()
  }
}
</script>

<style scoped>
.ai-excel-import-container {
  padding: 24px;
  max-width: 1400px;
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

.dev-mode-switch {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.dev-mode-hint {
  font-size: 14px;
  color: #909399;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.task-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.upload-dialog-content {
  padding: 20px 0;
}

.ai-config-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.ai-config-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-excel-import-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-description {
    font-size: 14px;
  }
}
</style>
