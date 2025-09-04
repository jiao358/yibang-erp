<template>
  <div class="task-table-container">
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <span class="table-title">任务列表</span>
          <div class="table-actions">
            <el-button size="small" @click="$emit('refresh')">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button size="small" type="primary" @click="$emit('export')">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 任务列表表格 -->
      <el-table 
        :data="paginatedTasks" 
        style="width: 100%"
        v-loading="loading"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column prop="taskId" label="任务ID" width="120" align="center">
          <template #default="{ row }">
            <el-link 
              type="primary" 
              @click="viewTaskDetail(row.taskId)"
              class="task-id-link"
              :class="{ 'cached-task': row.isCached }"
            >
              {{ formatTaskId(row.taskId) }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="fileName" label="文件名" min-width="200">
          <template #default="{ row }">
            <div class="file-info">
              <el-icon class="file-icon"><Document /></el-icon>
              <span class="file-name">{{ row.fileName }}</span>
              <el-tag v-if="row.supplier" size="small" type="info" class="supplier-tag">
                {{ row.supplier }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="进度" width="200" align="center">
          <template #default="{ row }">
            <div class="progress-info">
              <el-progress 
                :percentage="getProgressPercentage(row)"
                :status="getProgressStatus(row.status)"
                :stroke-width="8"
                :show-text="false"
              />
              <div class="progress-text">
                {{ getProgressText(row) }}
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="处理统计" width="250" align="center">
          <template #default="{ row }">
            <div class="processing-stats">
              <div class="stat-item">
                <span class="stat-label">总数:</span>
                <span class="stat-value total">{{ row.isCached ? '计算中...' : row.totalRows }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">成功:</span>
                <span class="stat-value success">{{ row.isCached ? '计算中...' : row.successRows }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">失败:</span>
                <span class="stat-value failed">{{ row.isCached ? '计算中...' : row.failedRows }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            <div class="time-info">
              <div class="create-time">{{ formatDate(row.createdAt) }}</div>
              <div class="create-time-short">{{ formatTime(row.createdAt) }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="completedAt" label="完成时间" width="160" align="center">
          <template #default="{ row }">
            <div class="time-info">
              <div v-if="row.completedAt" class="complete-time">
                {{ formatDate(row.completedAt) }}
              </div>
              <div v-else class="no-complete-time">-</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="processingTime" label="处理耗时" width="120" align="center">
          <template #default="{ row }">
            <div v-if="row.processingTime" class="processing-time">
              {{ formatProcessingTime(row.processingTime) }}
            </div>
            <div v-else class="no-processing-time">-</div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewTaskDetail(row.taskId)"
                :disabled="row.isCached"
              >
                查看详情
              </el-button>
              <el-button 
                v-if="canRetry(row.status)"
                type="success" 
                size="small" 
                @click="retryTask(row.taskId)"
                :disabled="row.isCached"
              >
                重新处理
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteTask(row.taskId)"
                :disabled="row.isCached"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="props.currentPage"
          v-model:page-size="props.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="props.totalTasks"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, 
  Download, 
  Refresh 
} from '@element-plus/icons-vue'
import type { TaskHistoryItem } from '@/types/ai'

// Props
interface Props {
  tasks: TaskHistoryItem[]
  loading?: boolean
  totalTasks?: number
  currentPage?: number
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  totalTasks: 0,
  currentPage: 1,
  pageSize: 20
})

// Emits
const emit = defineEmits<{
  refresh: []
  export: []
  viewDetail: [taskId: string]
  retryTask: [taskId: string]
  deleteTask: [taskId: string]
  selectionChange: [selectedTasks: TaskHistoryItem[]]
  pageChange: [page: number]
  sizeChange: [size: number]
}>()

// 响应式数据
const selectedTasks = ref<TaskHistoryItem[]>([])

// 计算属性 - 直接使用props中的分页参数
const paginatedTasks = computed(() => {
  return props.tasks
})

// 方法
const formatTaskId = (taskId: string) => {
  return taskId.substring(0, 8) + '...'
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'SYSTEM_PROCESSING': return 'warning'
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
    case 'SYSTEM_PROCESSING': return '系统处理中'
    case 'PENDING': return '等待处理'
    case 'PROCESSING': return '处理中'
    case 'COMPLETED': return '处理完成'
    case 'FAILED': return '处理失败'
    case 'CANCELLED': return '已取消'
    default: return '未知状态'
  }
}

const getProgressPercentage = (row: TaskHistoryItem) => {
  if (row.status === 'COMPLETED' || row.status === 'FAILED') {
    return 100
  }
  if (row.status === 'SYSTEM_PROCESSING') {
    return 20 // 缓存任务显示20%进度
  }
  if (row.status === 'PENDING') {
    return 0
  }
  // 处理中状态，根据成功和失败行数计算进度
  const processed = (row.successRows || 0) + (row.failedRows || 0)
  return Math.round((processed / row.totalRows) * 100)
}

const getProgressStatus = (status: string) => {
  switch (status) {
    case 'COMPLETED': return 'success'
    case 'FAILED': return 'exception'
    case 'PROCESSING': return 'warning'
    default: return ''
  }
}

const getProgressText = (row: TaskHistoryItem) => {
  if (row.status === 'COMPLETED') {
    return '100%'
  }
  if (row.status === 'SYSTEM_PROCESSING') {
    return '20%' // 缓存任务固定显示20%
  }
  if (row.status === 'PENDING') {
    return '0%'
  }
  const percentage = getProgressPercentage(row)
  return `${percentage}%`
}

const canRetry = (status: string) => {
  return ['FAILED', 'CANCELLED'].includes(status)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const formatTime = (dateString: string) => {
  return new Date(dateString).toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const formatProcessingTime = (milliseconds: number) => {
  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}h ${minutes % 60}m`
  } else if (minutes > 0) {
    return `${minutes}m ${seconds % 60}s`
  } else {
    return `${seconds}s`
  }
}

const viewTaskDetail = (taskId: string) => {
  // 检查是否是缓存任务
  const task = props.tasks.find(t => t.taskId === taskId)
  if (task?.isCached) {
    ElMessage.info('系统正在加速处理中，请稍后查看详情')
    return
  }
  emit('viewDetail', taskId)
}

const retryTask = (taskId: string) => {
  // 检查是否是缓存任务
  const task = props.tasks.find(t => t.taskId === taskId)
  if (task?.isCached) {
    ElMessage.info('系统正在加速处理中，请稍后操作')
    return
  }
  emit('retryTask', taskId)
}

const deleteTask = async (taskId: string) => {
  // 检查是否是缓存任务
  const task = props.tasks.find(t => t.taskId === taskId)
  if (task?.isCached) {
    ElMessage.info('系统正在加速处理中，请稍后操作')
    return
  }
  
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
    emit('deleteTask', taskId)
  } catch {
    // 用户取消删除
  }
}

const handleSelectionChange = (selection: TaskHistoryItem[]) => {
  selectedTasks.value = selection
  emit('selectionChange', selection)
}

const handleSizeChange = (size: number) => {
  emit('sizeChange', size)
}

const handleCurrentChange = (page: number) => {
  emit('pageChange', page)
}
</script>

<style scoped>
.task-table-container {
  width: 100%;
}

.table-card {
  border: 1px solid #e4e7ed;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.task-id-link {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

.cached-task {
  color: #909399 !important;
  cursor: not-allowed;
  text-decoration: none;
}

.cached-task:hover {
  color: #909399 !important;
  text-decoration: none;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #409eff;
  font-size: 16px;
}

.file-name {
  font-size: 14px;
  color: #303133;
}

.supplier-tag {
  margin-left: 8px;
}

.progress-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.progress-text {
  font-size: 12px;
  color: #606266;
  text-align: center;
}

.processing-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.stat-label {
  color: #909399;
}

.stat-value {
  font-weight: 600;
}

.stat-value.total {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.failed {
  color: #f56c6c;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.create-time,
.complete-time {
  font-size: 12px;
  color: #303133;
}

.create-time-short {
  font-size: 11px;
  color: #909399;
}

.no-complete-time,
.no-processing-time {
  color: #909399;
  font-style: italic;
  font-size: 12px;
}

.processing-time {
  font-size: 12px;
  color: #409eff;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .table-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
