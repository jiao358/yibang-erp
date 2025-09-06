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
        :has-processing-tasks="hasProcessingTasks"
        @upload-new="handleUploadNew"
        @refresh="loadTaskHistory"
        @view-all="scrollToTable"
      />

      <!-- 任务筛选 -->
      <TaskFilter 
        v-model="filterForm"
        @filter-change="handleFilterChange"
      />

      <!-- 任务列表 -->
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
          :current-page="currentPage"
          :page-size="pageSize"
          @refresh="loadTaskHistory"
          @export="exportTasks"
          @view-detail="viewTaskDetail"
          @retry-task="retryTask"
          @delete-task="deleteTask"
          @selection-change="handleSelectionChange"
          @page-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>

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
          
          <!-- AI配置已移至管理员页面，普通用户使用默认配置 -->
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
        title="AI处理启动"
        width="400px"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
      >
        <div class="auto-processing-notice">
          <div class="notice-icon">
            <el-icon size="48" color="#409eff"><Loading /></el-icon>
      </div>
          <div class="notice-content">
            <h3>AI处理已启动</h3>
            <p>系统正在后台处理您的Excel文件，请稍候...</p>
            <div class="countdown">
              <span>弹窗将在 {{ countdownSeconds }} 秒后自动关闭</span>
      </div>
      </div>
      </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import TaskOverview from './components/TaskOverview.vue'
import TaskFilter from './components/TaskFilter.vue'
import TaskTable from './components/TaskTable.vue'
import TaskDetailDialog from './components/TaskDetailDialog.vue'
import FileUpload from './components/FileUpload.vue'
// AI配置已移至管理员页面
// import ProcessingProgress from './components/ProcessingProgress.vue'
import { aiExcelImportApi } from '@/api/aiExcelImport'
import type { 
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
const countdownSeconds = ref(5) // 倒计时秒数
// 前端缓存任务持久化键名
const CACHED_TASKS_KEY = 'aiExcelCachedTasks'

// 统计数据
const statistics = ref({
  totalTasks: 0,
  processingTasks: 0,
  completedTasks: 0,
  failedTasks: 0
})

// 筛选表单（移除排序与高级选项）
const filterForm = reactive<TaskFilterForm>({
  status: '',
  dateRange: [],
  fileName: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(20)

// AI配置已移至管理员页面，使用默认配置

// 计算属性 - 直接使用taskHistory，不再前端筛选
const filteredTasks = computed(() => {
  return taskHistory.value
})

// 计算属性 - 检查是否有正在处理的任务
const hasProcessingTasks = computed(() => {
  return taskHistory.value.some(task => 
    task.status === 'PROCESSING' || 
    task.status === 'SYSTEM_PROCESSING' ||
    task.status === 'PENDING'
  )
})

// 事件处理
const handleUploadNew = () => {
  // 检查当前用户的任务数量是否小于3
  const userId = getCurrentUserId()
  const userKey = `userTaskCount_${userId}`
  const existingData = localStorage.getItem(userKey)
  
  let currentTaskCount = 0
  if (existingData) {
    try {
      const parsedData = JSON.parse(existingData)
      currentTaskCount = parsedData.taskCount || 0
    } catch (error) {
      console.warn('解析用户任务数据失败:', error)
      currentTaskCount = 0
    }
  }
  
  if (currentTaskCount >= 3) {
    ElMessage.warning('当前任务数量已达上限（3个），请等待任务完成后再上传新文件')
    return
  }
  
  // 清空之前选择的文件
  selectedFile.value = null
  
  showUploadDialog.value = true
}

const handleFileSelected = (file: File) => {
  selectedFile.value = file
  ElMessage.success(`已选择文件: ${file.name}`)
}

const handleUploadSuccess = (_response: any) => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error: string) => {
  ElMessage.error(`文件上传失败: ${error}`)
}

// AI配置相关方法已移除

const handleFilterChange = (filters: TaskFilterForm) => {
  console.log('筛选条件已更新:', filters)
  // 更新本地筛选表单
  Object.assign(filterForm, filters)
  // 重置到第一页并重新加载
  currentPage.value = 1
  loadTaskHistory()
}

const handleSelectionChange = (selectedTasks: TaskHistoryItem[]) => {
  console.log('选中的任务:', selectedTasks)
}

// 分页处理
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadTaskHistory()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadTaskHistory()
}

const handleCloseUploadDialog = () => {
  showUploadDialog.value = false
  selectedFile.value = null
}


function storeUserTaskCount(tempTaskId: string) {
  try {
    const userId = getCurrentUserId()
    
    // 读取用户之前的任务数量
    const userKey = `userTaskCount_${userId}`
    const existingData = localStorage.getItem(userKey)
    let currentTaskCount = 0
    
    if (existingData) {
      try {
        const parsedData = JSON.parse(existingData)
        currentTaskCount = parsedData.taskCount || 0
      } catch (parseError) {
        console.warn('解析用户任务数据失败，使用默认值:', parseError)
        currentTaskCount = 0
      }
    }
    
    // 将上传数量+1
    const newTaskCount = currentTaskCount + 1
    const uploadTime = new Date().toISOString()
    
    const taskData = {
      tempTaskId: tempTaskId,
      taskCount: newTaskCount,
      uploadTime: uploadTime
    }
    
    const key = `userTaskCount_${userId}_${tempTaskId}`
    localStorage.setItem(key, JSON.stringify(taskData))
    
    // 同时更新用户的总任务数量
    localStorage.setItem(userKey, JSON.stringify({
      taskCount: newTaskCount,
      lastUpdateTime: uploadTime
    }))
    
    console.log(`📊 已存储用户任务数据:`, taskData)
  } catch (error) {
    console.error('存储任务数据失败:', error)
  }
}

// 轮询任务：检查并清理过期的缓存任务
function startTaskCleanupPolling() {
  // 先清理之前的轮询
  clearTaskCleanupPolling()
  
  // 每30秒检查一次过期任务
  taskCleanupInterval = setInterval(() => {
    cleanupExpiredTasks()
  }, 30000)
  
  console.log('🧹 任务清理轮询已启动')
}

// 清理过期的缓存任务
function cleanupExpiredTasks() {
  try {
    const userId = getCurrentUserId()
    const now = new Date().getTime()
    const expireTime = 24 * 60 * 60 * 1000 // 24小时过期
    
    // 获取所有localStorage键
    const keys = Object.keys(localStorage)
    const userTaskKeys = keys.filter(key => key.startsWith(`userTaskCount_${userId}_`))
    
    let cleanedCount = 0
    
    userTaskKeys.forEach(key => {
      try {
        const data = localStorage.getItem(key)
        if (data) {
          const taskData = JSON.parse(data)
          const uploadTime = new Date(taskData.uploadTime).getTime()
          
          // 如果任务超过24小时，删除缓存任务
          if (now - uploadTime > expireTime) {
            localStorage.removeItem(key)
            
            // 从taskHistory中删除对应的任务
            const tempTaskId = taskData.tempTaskId
            const taskIndex = taskHistory.value.findIndex(t => t.taskId === tempTaskId)
            if (taskIndex !== -1) {
              taskHistory.value.splice(taskIndex, 1)
              totalTasks.value = Math.max(0, totalTasks.value - 1)
              cleanedCount++
              console.log(`🗑️ 已清理过期任务: ${tempTaskId}`)
            }
          }
        }
      } catch (error) {
        console.error(`清理任务失败 ${key}:`, error)
        // 如果解析失败，直接删除
        localStorage.removeItem(key)
      }
    })
    
    if (cleanedCount > 0) {
      console.log(`🧹 清理了 ${cleanedCount} 个过期任务`)
      // 重新加载任务历史
      loadTaskHistory()
    }
  } catch (error) {
    console.error('清理过期任务失败:', error)
  }
}

// 清理任务清理轮询
function clearTaskCleanupPolling() {
  if (taskCleanupInterval) {
    clearInterval(taskCleanupInterval)
    taskCleanupInterval = null
  }
}



//上传任务开始处理
const startProcessing = async () => {
  if (!selectedFile.value) {
    ElMessage.error('请先选择文件')
    return
  }
  
  // 生成临时任务ID，用于缓存任务
  const tempTaskId = 'temp-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9)
  storeUserTaskCount(tempTaskId)
  try {
    showUploadDialog.value = false
    showProgressDialog.value = true
    processingStatus.value = 'PROCESSING'
    
    // 立即创建缓存任务，不等待API响应
    const cachedTask = createCachedTask(tempTaskId, selectedFile.value)
    console.log('📝 创建的缓存任务:', cachedTask)
    
    taskHistory.value.unshift(cachedTask)
    totalTasks.value++
    // 持久化缓存任务，刷新后恢复
    persistCachedTask(cachedTask)
    
    console.log('📋 当前任务列表长度:', taskHistory.value.length)
    console.log('📋 任务列表内容:', taskHistory.value)
    
    // 立即启动5秒倒计时，不等待后端响应
    startCountdown()
    
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('salesUserId', getCurrentUserId().toString())
    formData.append('salesCompanyId', getCurrentCompanyId().toString())
    formData.append('priority', '3')
    formData.append('enableAIProductMatching', 'true')
    formData.append('minConfidenceThreshold', '0.8') // 使用默认置信度阈值
    formData.append('autoCreateCustomer', 'false')
    formData.append('autoCreateProduct', 'false')
    formData.append('remarks', 'AI Excel导入')
    
    // 调用后端API开始处理
    const response = await aiExcelImportApi.startProcessing(formData)
    
    if (response && response.taskId) {
      ElMessage.success('AI处理已启动')
      
      // 更新缓存任务为真实任务ID
      const updatedCachedTask = { ...cachedTask, taskId: response.taskId }
      const taskIndex = taskHistory.value.findIndex(t => t.taskId === tempTaskId)
      if (taskIndex !== -1) {
        taskHistory.value[taskIndex] = updatedCachedTask
        persistCachedTask(updatedCachedTask)
        console.log('📝 更新缓存任务ID:', tempTaskId, '->', response.taskId)
      }
      
      // 后台开始进度轮询
      startProgressPolling(response.taskId)
    } else {
      throw new Error('启动处理失败：未获取到任务ID')
    }
    
  } catch (error: any) {
    console.error('启动AI处理失败:', error)
    processingStatus.value = 'FAILED'
    
    // 更新缓存任务状态为失败
    const taskIndex = taskHistory.value.findIndex(t => t.taskId === tempTaskId)
    if (taskIndex !== -1) {
      taskHistory.value[taskIndex] = { 
        ...taskHistory.value[taskIndex], 
        status: 'FAILED',
        supplier: '处理失败'
      }
      persistCachedTask(taskHistory.value[taskIndex])
      console.log('📝 更新缓存任务状态为失败')
    }
    
    ElMessage.error(`启动AI处理失败: ${error.message || error}`)
    clearCountdown()
    showProgressDialog.value = false
  }
}

// 倒计时相关
let countdownInterval: NodeJS.Timeout | null = null

// 进度轮询相关
let progressInterval: NodeJS.Timeout | null = null

// 任务清理轮询相关
let taskCleanupInterval: NodeJS.Timeout | null = null

// 启动倒计时
const startCountdown = () => {
  // 先清理之前的倒计时
  clearCountdown()
  
  // 重置倒计时为5秒
  countdownSeconds.value = 5
  console.log('⏰ 开始倒计时，初始值:', countdownSeconds.value)
  
  countdownInterval = setInterval(() => {
    countdownSeconds.value--
    console.log('⏰ 倒计时:', countdownSeconds.value, '秒')
    
    if (countdownSeconds.value <= 0) {
      console.log('⏰ 倒计时结束，关闭弹窗')
      clearCountdown()
      showProgressDialog.value = false
      ElMessage.info('系统正在后台处理，请稍候查看处理结果')
    }
  }, 1000)
}

// 清理倒计时
const clearCountdown = () => {
  if (countdownInterval) {
    clearInterval(countdownInterval)
    countdownInterval = null
  }
}

// 创建缓存任务
const createCachedTask = (taskId: string, file: File): TaskHistoryItem => {
  // 获取当前用户信息
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const currentUsername = userInfo.username || userInfo.name || '当前用户'
  
  return {
    taskId: taskId,
    fileName: file.name,
    status: 'SYSTEM_PROCESSING',
    totalRows: 100, // 设置一个合理的总数，避免除零错误
    successRows: 20, // 初始显示20%进度
    failedRows: 0,
    manualProcessRows: 0,
    createdAt: new Date().toISOString(),
    fileSize: file.size,
    uploadUser: currentUsername,
    supplier: '系统处理中',
    isCached: true
  }
}

const startProgressPolling = (taskId: string) => {
  // 先清理之前的轮询
  clearProgressPolling()
  
  progressInterval = setInterval(async () => {
    try {
      const response = await aiExcelImportApi.getProgress(taskId)
      // 先检查是否完成（不依赖progress字段）
      if (response.status === 'COMPLETED' || response.status === 'FAILED') {
        clearProgressPolling()
        clearCountdown() // 清理倒计时
        processingStatus.value = response.status
        
        // 清理对应的缓存任务
        removeCachedTask(taskId)
        
        // 重新加载任务历史
        await loadTaskHistory()
        
        // 关闭弹窗
        showProgressDialog.value = false
        ElMessage.success('AI处理完成')
      } else if (response.progress) {
        // 只更新全局进度，不影响缓存任务
        progress.value = response.progress
      }
    } catch (error: any) {
      console.error('获取进度失败:', error)
      // 不清理轮询，继续尝试
    }
  }, 2000) // 每2秒轮询一次
}

const clearProgressPolling = () => {
  if (progressInterval) {
    clearInterval(progressInterval)
    progressInterval = null
  }
  // 不清理倒计时，让倒计时自然结束
}

// 缓存任务持久化：读/写/删/恢复
function loadCachedTasks(): TaskHistoryItem[] {
  try {
    const raw = localStorage.getItem(CACHED_TASKS_KEY)
    if (!raw) return []
    const arr = JSON.parse(raw)
    if (!Array.isArray(arr)) return []
    
    // 清理过期的缓存任务（超过1小时）
    const now = new Date().getTime()
    const validTasks = arr.filter(task => {
      const taskTime = new Date(task.createdAt).getTime()
      const hoursDiff = (now - taskTime) / (1000 * 60 * 60)
      return hoursDiff < 1 // 保留1小时内的缓存任务
    })
    
    // 如果有任务被清理，更新localStorage
    if (validTasks.length !== arr.length) {
      localStorage.setItem(CACHED_TASKS_KEY, JSON.stringify(validTasks))
      console.log(`📋 清理了 ${arr.length - validTasks.length} 个过期缓存任务`)
    }
    
    return validTasks
  } catch {
    return []
  }
}

function persistCachedTask(task: TaskHistoryItem) {
  const arr = loadCachedTasks()
  const idx = arr.findIndex((t: TaskHistoryItem) => t.taskId === task.taskId)
  if (idx >= 0) arr[idx] = task; else arr.unshift(task)
  
  // 限制缓存任务数量，最多保留10个
  if (arr.length > 10) {
    arr.splice(10)
    console.log('📋 缓存任务数量超限，已清理多余任务')
  }
  
  localStorage.setItem(CACHED_TASKS_KEY, JSON.stringify(arr))
  console.log(`📋 缓存任务已保存: ${task.taskId}`)
}


function restoreCachedTasks() {
  const cached = loadCachedTasks()
  if (!cached.length) {
    console.log('📋 没有缓存任务需要恢复')
    return
  }
  
  const existingIds = new Set(taskHistory.value.map(t => t.taskId))
  const toAdd = cached.filter(t => !existingIds.has(t.taskId))
  
  if (toAdd.length) {
    taskHistory.value.unshift(...toAdd)
    totalTasks.value += toAdd.length
    console.log(`📋 恢复了 ${toAdd.length} 个缓存任务`)
  } else {
    console.log('📋 没有新的缓存任务需要恢复')
  }
}

// 清理指定的缓存任务
function removeCachedTask(taskId: string) {
  // 1. 从 localStorage 中清理
  const arr = loadCachedTasks()
  const filtered = arr.filter(t => t.taskId !== taskId)
  if (filtered.length !== arr.length) {
    localStorage.setItem(CACHED_TASKS_KEY, JSON.stringify(filtered))
    console.log(`📋 已从localStorage清理缓存任务: ${taskId}`)
  }
  
  // 2. 从 taskHistory.value 中清理
  const taskIndex = taskHistory.value.findIndex(t => t.taskId === taskId)
  if (taskIndex !== -1) {
    taskHistory.value.splice(taskIndex, 1)
    totalTasks.value = Math.max(0, totalTasks.value - 1)
    console.log(`📋 已从任务列表中清理缓存任务: ${taskId}`)
  }
}

// 预留：取消处理（当前未在UI中挂载）
// 取消处理能力如需启用，可在UI中挂载后再恢复实现

// 加载统计数据
// 统计加载（在刷新任务历史时并行调用）
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
    console.log('📋 当前筛选条件:', filterForm)
    
    // 构建请求参数
    const requestParams = {
      page: currentPage.value,
      size: pageSize.value,
      status: filterForm.status || undefined,
      fileName: filterForm.fileName || undefined,
      startDate: filterForm.dateRange && filterForm.dateRange.length > 0 ? filterForm.dateRange[0] : undefined,
      endDate: filterForm.dateRange && filterForm.dateRange.length > 1 ? filterForm.dateRange[1] : undefined
    }
    
    console.log('🚀 API请求参数:', requestParams)
    
    // 加载任务历史 - 使用筛选条件和分页
    const taskResponse = await aiExcelImportApi.getTaskHistory(requestParams)
    
    const response = taskResponse
    
    console.log('📥 API响应:', response)
    
    if (response && response.content) {
      const realTasks = response.content
      
      // 合并真实任务和缓存任务
      const cachedTasks = loadCachedTasks()
      const existingIds = new Set(realTasks.map(t => t.taskId))
      const validCachedTasks = cachedTasks.filter(t => !existingIds.has(t.taskId))
      
      // 将缓存任务插入到真实任务前面
      taskHistory.value = [...validCachedTasks, ...realTasks]
      
      // 更新总数（包含缓存任务）
      totalTasks.value = response.totalElements + validCachedTasks.length
      
      console.log('✅ 任务历史加载完成，总数:', totalTasks.value, '缓存任务:', validCachedTasks.length)
    } else {
      console.warn('⚠️ API响应格式异常:', response)
      ElMessage.warning('获取任务列表失败')
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

const viewTaskResults = (_taskId: string) => {
  ElMessage.info('查看结果功能开发中...')
}

const downloadResults = (_taskId: string) => {
  ElMessage.info('下载结果功能开发中...')
}

const viewLogs = (_taskId: string) => {
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
onMounted(async () => {
  console.log('🚀 AIExcelImport 主页面已挂载')
  // 恢复本地缓存任务，优先显示
  restoreCachedTasks()
  // 加载统计信息（只在页面初始化时调用）
  await loadStatistics()
  // 加载任务历史
  await loadTaskHistory()
  // 启动任务清理轮询
  startTaskCleanupPolling()
})

onUnmounted(() => {
  // 清理所有定时器
  clearCountdown()
  clearProgressPolling()
  clearTaskCleanupPolling()
})

// 监控倒计时变化
watch(countdownSeconds, (newVal, oldVal) => {
  console.log('⏰ 倒计时变化:', oldVal, '->', newVal)
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
    // 同步恢复本地缓存任务
    restoreCachedTasks()
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

.auto-processing-notice {
  text-align: center;
  padding: 20px;
}

.notice-icon {
  margin-bottom: 20px;
}

.notice-content h3 {
  color: #303133;
  margin-bottom: 12px;
  font-size: 18px;
}

.notice-content p {
  color: #606266;
  margin-bottom: 20px;
  font-size: 14px;
  line-height: 1.6;
}

.countdown {
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 6px;
  padding: 12px;
  margin-top: 16px;
}

.countdown span {
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
}
</style>
