<template>
  <div class="task-history-container">
    <!-- 历史任务表格 -->
    <el-table
      :data="tasks"
      style="width: 100%"
      max-height="400"
      stripe
      border
      v-loading="loading"
    >
      <el-table-column prop="taskId" label="任务ID" width="120" align="center">
        <template #default="{ row }">
          <el-link 
            type="primary" 
            @click="viewTaskDetail(row.taskId)"
            class="task-id-link"
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

      <el-table-column label="处理统计" width="300" align="center">
        <template #default="{ row }">
          <div class="processing-stats">
            <div class="stat-item">
              <span class="stat-label">总数:</span>
              <span class="stat-value total">{{ row.totalRows }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">成功:</span>
              <span class="stat-value success">{{ row.successRows }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">失败:</span>
              <span class="stat-value failed">{{ row.failedRows }}</span>
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
            >
              查看详情
            </el-button>
            <el-button 
              v-if="canRetry(row.status)"
              type="success" 
              size="small" 
              @click="retryTask(row.taskId)"
            >
              重新处理
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deleteTask(row.taskId)"
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
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalTasks"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="任务详情"
      width="900px"
      :before-close="handleCloseDetailDialog"
    >
      <div v-if="selectedTask" class="task-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务ID">{{ selectedTask.taskId }}</el-descriptions-item>
          <el-descriptions-item label="文件名">{{ selectedTask.fileName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedTask.status)">
              {{ getStatusText(selectedTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(selectedTask.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间" v-if="selectedTask.completedAt">
            {{ formatDateTime(selectedTask.completedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理耗时" v-if="selectedTask.processingTime">
            {{ formatProcessingTime(selectedTask.processingTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">处理统计</el-divider>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="detail-stat-card total">
              <div class="detail-stat-number">{{ selectedTask.totalRows }}</div>
              <div class="detail-stat-label">总行数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="detail-stat-card success">
              <div class="detail-stat-number">{{ selectedTask.successRows }}</div>
              <div class="detail-stat-label">成功处理</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="detail-stat-card failed">
              <div class="detail-stat-number">{{ selectedTask.failedRows }}</div>
              <div class="detail-stat-label">处理失败</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="detail-stat-card manual">
              <div class="detail-stat-number">{{ selectedTask.manualProcessRows || 0 }}</div>
              <div class="detail-stat-label">需手动处理</div>
            </div>
          </el-col>
        </el-row>

        <el-divider content-position="left">操作</el-divider>
        <div class="detail-actions">
          <el-button 
            type="primary" 
            @click="downloadResults(selectedTask.taskId)"
          >
            <el-icon><Download /></el-icon>
            下载结果
          </el-button>
          <el-button 
            v-if="canRetry(selectedTask.status)"
            type="success" 
            @click="retryTask(selectedTask.taskId)"
          >
            <el-icon><Refresh /></el-icon>
            重新处理
          </el-button>
          <el-button 
            type="info" 
            @click="viewProcessingLogs(selectedTask.taskId)"
          >
            <el-icon><Document /></el-icon>
            查看日志
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 确认删除对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
    >
      <div class="delete-confirm">
        <el-icon class="delete-icon"><Warning /></el-icon>
        <p>确定要删除这个任务吗？删除后无法恢复。</p>
        <p class="delete-task-info">任务ID: {{ taskToDelete }}</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDeleteTask">确定删除</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, 
  Download, 
  Refresh, 
  Warning 
} from '@element-plus/icons-vue'
import type { TaskHistoryItem } from '@/types/ai'

// Props
interface Props {
  tasks: TaskHistoryItem[]
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  viewDetail: [taskId: string]
  retryTask: [taskId: string]
}>()

// 响应式数据
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const totalTasks = ref(0)
const detailDialogVisible = ref(false)
const selectedTask = ref<TaskHistoryItem | null>(null)
const deleteDialogVisible = ref(false)
const taskToDelete = ref('')

// 计算属性
const paginatedTasks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return props.tasks.slice(start, end)
})

// 方法
const formatTaskId = (taskId: string) => {
  return taskId.substring(0, 8) + '...'
}

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

const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
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
  const task = props.tasks.find(t => t.taskId === taskId)
  if (task) {
    selectedTask.value = task
    detailDialogVisible.value = true
  }
}

const retryTask = (taskId: string) => {
  emit('retryTask', taskId)
  ElMessage.info('开始重新处理任务')
}

const deleteTask = (taskId: string) => {
  taskToDelete.value = taskId
  deleteDialogVisible.value = true
}

const confirmDeleteTask = async () => {
  try {
    // TODO: 调用API删除任务
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('任务删除成功')
    deleteDialogVisible.value = false
    taskToDelete.value = ''
    
    // 重新加载任务列表
    // TODO: 触发父组件重新加载
  } catch (error) {
    ElMessage.error('删除任务失败')
  }
}

const downloadResults = (taskId: string) => {
  // TODO: 实现下载功能
  ElMessage.success('开始下载结果文件')
}

const viewProcessingLogs = (taskId: string) => {
  // TODO: 实现查看日志功能
  ElMessage.info('查看处理日志功能待实现')
}

const handleCloseDetailDialog = () => {
  detailDialogVisible.value = false
  selectedTask.value = null
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}
</script>

<style scoped>
.task-history-container {
  width: 100%;
}

.task-id-link {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
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

.task-detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.detail-stat-card {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.detail-stat-card.total {
  background: #f0f9ff;
  border-color: #b3d8ff;
}

.detail-stat-card.success {
  background: #f0f9ff;
  border-color: #b3d8ff;
}

.detail-stat-card.failed {
  background: #fef0f0;
  border-color: #fbc4c4;
}

.detail-stat-card.manual {
  background: #fdf6ec;
  border-color: #f5dab1;
}

.detail-stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.detail-stat-label {
  font-size: 14px;
  color: #606266;
}

.detail-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.delete-confirm {
  text-align: center;
  padding: 20px 0;
}

.delete-icon {
  font-size: 48px;
  color: #e6a23c;
  margin-bottom: 16px;
}

.delete-confirm p {
  margin: 8px 0;
  color: #606266;
}

.delete-task-info {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #f8f9fa;
  padding: 8px;
  border-radius: 4px;
  color: #303133;
}

.dialog-footer {
  text-align: right;
}
</style>
