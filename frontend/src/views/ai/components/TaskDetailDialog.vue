<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`任务详情 - ${taskDetail?.fileName || ''}`"
    width="1200px"
    :before-close="handleClose"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    :lock-scroll="false"
    :append-to-body="true"
    :destroy-on-close="false"
    class="task-detail-dialog"
    top="5vh"
  >
    <div v-if="taskDetail" class="task-detail-content">
      <!-- 标签页导航 -->
      <el-tabs v-model="activeTab" class="task-detail-tabs">
        <!-- 基本信息标签页 -->
        <el-tab-pane label="基本信息" name="basic">
          <div class="tab-content">
            <!-- 基本信息 -->
            <el-card class="info-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <span class="card-title">基本信息</span>
                  <el-tag :type="getStatusType(taskDetail.status)" size="large">
                    {{ getStatusText(taskDetail.status) }}
                  </el-tag>
                </div>
              </template>
              
              <el-descriptions :column="3" border>
                <el-descriptions-item label="任务ID">
                  <el-tag type="info" size="small">{{ taskDetail.taskId }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="文件名">{{ taskDetail.fileName }}</el-descriptions-item>
                <el-descriptions-item label="供应商" v-if="taskDetail.supplier">
                  {{ taskDetail.supplier }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatDateTime(taskDetail.createdAt) }}</el-descriptions-item>
                <el-descriptions-item label="开始时间" v-if="taskDetail.startedAt">
                  {{ formatDateTime(taskDetail.startedAt) }}
                </el-descriptions-item>
                <el-descriptions-item label="完成时间" v-if="taskDetail.completedAt">
                  {{ formatDateTime(taskDetail.completedAt) }}
                </el-descriptions-item>
                <el-descriptions-item label="处理耗时" v-if="taskDetail.processingTime">
                  {{ formatProcessingTime(taskDetail.processingTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="文件大小" v-if="taskDetail.fileSize">
                  {{ formatFileSize(taskDetail.fileSize) }}
                </el-descriptions-item>
                <el-descriptions-item label="上传用户" v-if="taskDetail.uploadUser">
                  {{ taskDetail.uploadUser }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>

            <!-- 处理结果统计 -->
            <el-card class="stats-card" shadow="never">
              <el-row :gutter="16">
                <el-col :span="6">
                  <div class="stat-card total">
                    <div class="stat-icon">
                      <el-icon><Document /></el-icon>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ taskDetail.totalRows }}</div>
                      <div class="stat-label">总行数</div>
                    </div>
                  </div>
                </el-col>
                
                <el-col :span="6">
                  <div class="stat-card success">
                    <div class="stat-icon">
                      <el-icon><CircleCheck /></el-icon>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ taskDetail.successRows }}</div>
                      <div class="stat-label">成功处理</div>
                    </div>
                  </div>
                </el-col>
                
                <el-col :span="6">
                  <div class="stat-card failed">
                    <div class="stat-icon">
                      <el-icon><CircleClose /></el-icon>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ taskDetail.failedRows }}</div>
                      <div class="stat-label">处理失败</div>
                    </div>
                  </div>
                </el-col>
                
                <el-col :span="6">
                  <div class="stat-card manual">
                    <div class="stat-icon">
                      <el-icon><Tools /></el-icon>
                    </div>
                    <div class="stat-content">
                      <div class="stat-number">{{ taskDetail.manualProcessRows || 0 }}</div>
                      <div class="stat-label">需手动处理</div>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </el-card>

            <!-- 进度详情 -->
            <el-card class="progress-card" shadow="never" v-if="taskDetail.status === 'PROCESSING'">
              <template #header>
                <span class="card-title">实时进度</span>
              </template>
              
              <div class="progress-details">
                <el-progress 
                  :percentage="getProgressPercentage()"
                  :status="getProgressStatus()"
                  :stroke-width="12"
                  :show-text="false"
                  class="main-progress"
                />
                <div class="progress-text">
                  {{ getProgressText() }}
                </div>
                
                <div class="progress-stats">
                  <div class="progress-stat-item">
                    <span class="stat-label">已处理:</span>
                    <span class="stat-value">{{ getProcessedRows() }}</span>
                  </div>
                  <div class="progress-stat-item">
                    <span class="stat-label">剩余:</span>
                    <span class="stat-value">{{ getRemainingRows() }}</span>
                  </div>
                  <div class="progress-stat-item">
                    <span class="stat-label">预计剩余时间:</span>
                    <span class="stat-value">{{ getEstimatedTime() }}</span>
                  </div>
                </div>
              </div>
            </el-card>

            <!-- 操作按钮 -->
            <el-card class="actions-card" shadow="never">
              <div class="action-buttons">
                <el-button 
                  type="primary" 
                  @click="viewResults"
                >
                  <el-icon><View /></el-icon>
                  查看结果
                </el-button>
                
                <el-button 
                  v-if="canRetry(taskDetail.status)"
                  type="success" 
                  @click="retryTask"
                >
                  <el-icon><Refresh /></el-icon>
                  重新处理
                </el-button>
                
                <el-button 
                  type="info" 
                  @click="downloadResults"
                >
                  <el-icon><Download /></el-icon>
                  下载结果
                </el-button>
                
                <el-button 
                  type="warning" 
                  @click="viewLogs"
                >
                  <el-icon><Document /></el-icon>
                  查看日志
                </el-button>
                
                <el-button 
                  type="danger" 
                  @click="deleteTask"
                >
                  <el-icon><Delete /></el-icon>
                  删除任务
                </el-button>
              </div>
            </el-card>
                    </div>
        </el-tab-pane>

        <!-- 成功订单标签页 -->
        <el-tab-pane label="成功订单" name="success-orders">
          <div class="tab-content">
            <SuccessOrdersList 
              :task-id="taskDetail.taskId"
              @view-order="handleViewOrder"
              @refresh="handleRefreshSuccessOrders"
            />
          </div>
        </el-tab-pane>

        <!-- 失败订单标签页 -->
        <el-tab-pane label="失败订单" name="failed-orders">
          <div class="tab-content">
            <FailedOrdersList 
              :task-id="taskDetail.taskId"
              @manual-add-order="handleManualAddOrder"
              @refresh="handleRefreshFailedOrders"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, 
  CircleCheck, 
  CircleClose, 
  Tools, 
  View, 
  Refresh, 
  Download, 
  Delete 
} from '@element-plus/icons-vue'
import type { TaskHistoryItem } from '@/types/ai'
import FailedOrdersList from './FailedOrdersList.vue'
import SuccessOrdersList from './SuccessOrdersList.vue'

// Props
interface Props {
  modelValue: boolean
  taskDetail: TaskHistoryItem | null
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  viewResults: [taskId: string]
  retryTask: [taskId: string]
  downloadResults: [taskId: string]
  viewLogs: [taskId: string]
  deleteTask: [taskId: string]
  close: []
}>()

// 响应式数据
const activeTab = ref('basic')

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 方法
const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING': return 'info'
    case 'PROCESSING': return 'warning'
    case 'COMPLETED': return 'success'
    case 'FAILED': return 'danger'
    case 'CANCELLED': return 'info'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '等待处理'
    case 'PROCESSING': return '处理中'
    case 'COMPLETED': return '处理完成'
    case 'FAILED': return '处理失败'
    case 'CANCELLED': return '已取消'
    default: return '未知状态'
  }
}

const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const formatProcessingTime = (milliseconds: number) => {
  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}小时 ${minutes % 60}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟 ${seconds % 60}秒`
  } else {
    return `${seconds}秒`
  }
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getProgressPercentage = () => {
  if (!props.taskDetail) return 0
  const { status, totalRows, successRows, failedRows } = props.taskDetail
  
  if (status === 'COMPLETED' || status === 'FAILED') {
    return 100
  }
  if (status === 'PENDING') {
    return 0
  }
  
  const processed = (successRows || 0) + (failedRows || 0)
  return Math.round((processed / totalRows) * 100)
}

const getProgressStatus = () => {
  if (!props.taskDetail) return ''
  const { status } = props.taskDetail
  
  switch (status) {
    case 'COMPLETED': return 'success'
    case 'FAILED': return 'exception'
    case 'PROCESSING': return 'warning'
    default: return ''
  }
}

const getProgressText = () => {
  const percentage = getProgressPercentage()
  return `${percentage}%`
}

// 失败订单相关方法
const handleManualAddOrder = (orderId: number) => {
  ElMessage.info(`手动添加订单: ${orderId}`)
}

const handleRefreshFailedOrders = () => {
  ElMessage.success('失败订单列表已刷新')
}

// 成功订单相关方法
const handleViewOrder = (orderId: string) => {
  ElMessage.info(`查看订单详情: ${orderId}`)
}

const handleRefreshSuccessOrders = () => {
  ElMessage.success('成功订单列表已刷新')
}

const getProcessedRows = () => {
  if (!props.taskDetail) return 0
  const { successRows, failedRows } = props.taskDetail
  return (successRows || 0) + (failedRows || 0)
}

const getRemainingRows = () => {
  if (!props.taskDetail) return 0
  const { totalRows } = props.taskDetail
  return totalRows - getProcessedRows()
}

const getEstimatedTime = () => {
  if (!props.taskDetail) return '计算中...'
  const { processingTime, totalRows } = props.taskDetail
  const processed = getProcessedRows()
  
  if (processed === 0 || !processingTime) return '计算中...'
  
  const avgTimePerRow = processingTime / processed
  const remaining = getRemainingRows()
  const estimatedMs = avgTimePerRow * remaining
  
  return formatProcessingTime(estimatedMs)
}

const canRetry = (status: string) => {
  return ['FAILED', 'CANCELLED'].includes(status)
}

const handleClose = () => {
  dialogVisible.value = false
  // 触发close事件，通知父组件弹窗已关闭
  emit('close')
}

const viewResults = () => {
  if (props.taskDetail) {
    emit('viewResults', props.taskDetail.taskId)
  }
}

const retryTask = () => {
  if (props.taskDetail) {
    emit('retryTask', props.taskDetail.taskId)
  }
}

const downloadResults = () => {
  if (props.taskDetail) {
    emit('downloadResults', props.taskDetail.taskId)
  }
}

const viewLogs = () => {
  if (props.taskDetail) {
    emit('viewLogs', props.taskDetail.taskId)
  }
}

const deleteTask = async () => {
  if (!props.taskDetail) return
  
  try {
    await ElMessageBox.confirm(
      '确定要删除这个任务吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    emit('deleteTask', props.taskDetail.taskId)
    handleClose()
  } catch {
    // 用户取消删除
  }
}
</script>

<style scoped>
.task-detail-dialog {
  max-height: 90vh;
}

/* 确保弹窗在视口中心显示，不受主页面滚动影响 */
:deep(.el-dialog) {
  position: fixed !important;
  top: 5vh !important;
  left: 50% !important;
  transform: translateX(-50%) !important;
  margin: 0 !important;
  max-height: 90vh;
  overflow: hidden;
  z-index: 2000 !important;
}

:deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}

/* 弹窗遮罩层也固定位置 */
:deep(.el-overlay) {
  position: fixed !important;
  z-index: 1999 !important;
}

/* 确保弹窗内容不受主页面影响 */
.task-detail-dialog {
  position: relative;
  z-index: 2001;
}

.task-detail-tabs {
  margin-bottom: 20px;
}

.tab-content {
  padding: 20px 0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.task-detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.info-card,
.stats-card,
.progress-card,
.actions-card {
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
}

.stats-card {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
}

.stat-card.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card.success {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-card.failed {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.stat-card.manual {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #333;
}

.stat-icon {
  font-size: 24px;
  margin-right: 16px;
  opacity: 0.8;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.progress-details {
  text-align: center;
}

.main-progress {
  margin-bottom: 16px;
}

.progress-text {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 20px;
}

.progress-stats {
  display: flex;
  justify-content: space-around;
  gap: 20px;
}

.progress-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.progress-stat-item .stat-label {
  font-size: 14px;
  color: #606266;
}

.progress-stat-item .stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  min-width: 120px;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .progress-stats {
    flex-direction: column;
    gap: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .action-buttons .el-button {
    width: 100%;
    max-width: 200px;
  }
}
</style>
