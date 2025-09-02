<template>
  <div class="processing-logs-list">
    <div class="list-header">
      <h4>处理日志</h4>
      <div class="list-stats">
        <el-tag type="info">{{ totalLogs }} 条日志记录</el-tag>
      </div>
    </div>

    <!-- 日志级别筛选 -->
    <div class="log-filters">
      <el-select 
        v-model="selectedLevel" 
        placeholder="选择日志级别" 
        clearable
        @change="handleLevelChange"
        style="width: 150px; margin-right: 10px;"
      >
        <el-option label="全部" value="" />
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAILED" />
        <el-option label="手动处理" value="MANUAL_PROCESS" />
      </el-select>
      
      <el-button @click="refreshLogs" :loading="loading" size="small">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="logs.length > 0" class="logs-container">
      <el-table :data="logs" style="width: 100%" class="logs-table">
        <el-table-column prop="timestamp" label="时间" width="180" align="center">
          <template #default="{ row }">
            <span class="timestamp">{{ formatDateTime(row.timestamp) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="level" label="级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)" size="small">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="message" label="消息" min-width="200">
          <template #default="{ row }">
            <div class="log-message">
              {{ row.message }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="excelRowNumber" label="Excel行号" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.excelRowNumber !== null" type="info" size="small">
              第{{ (row.excelRowNumber || 0) + 1 }}行
            </el-tag>
            <span v-else class="no-row">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="step" label="处理步骤" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStepTagType(row.step)" size="small">
              {{ getStepText(row.step) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button 
              v-if="row.details"
              type="primary" 
              size="small" 
              @click="viewLogDetails(row)"
            >
              查看详情
            </el-button>
            <span v-else class="no-details">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" v-if="totalLogs > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalLogs"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <div v-else class="empty-container">
      <el-empty description="暂无处理日志" />
    </div>

    <!-- 日志详情弹窗 -->
    <el-dialog
      v-model="logDetailDialogVisible"
      title="日志详情"
      width="600px"
      :close-on-click-modal="true"
    >
      <div v-if="selectedLog" class="log-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ formatDateTime(selectedLog.timestamp) }}</el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="getLevelTagType(selectedLog.level)" size="small">
              {{ getLevelText(selectedLog.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Excel行号" span="2">
            <el-tag v-if="selectedLog.excelRowNumber !== null" type="info" size="small">
              第{{ (selectedLog.excelRowNumber || 0) + 1 }}行
            </el-tag>
            <span v-else>不适用</span>
          </el-descriptions-item>
          <el-descriptions-item label="处理步骤" span="2">
            <el-tag :type="getStepTagType(selectedLog.step)" size="small">
              {{ getStepText(selectedLog.step) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="消息" span="2">{{ selectedLog.message }}</el-descriptions-item>
          <el-descriptions-item v-if="selectedLog.details" label="详细信息" span="2">
            <div class="log-details">
              {{ selectedLog.details }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import aiExcelImportApi from '@/api/aiExcelImport'

interface Props {
  taskId: string
  autoLoad?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  autoLoad: true
})

const emit = defineEmits<{
  refresh: []
}>()

const loading = ref(false)
const logs = ref<any[]>([])
const totalLogs = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const selectedLevel = ref('')
const logDetailDialogVisible = ref(false)
const selectedLog = ref<any>(null)

// 加载处理日志列表
const loadProcessingLogs = async () => {
  if (!props.taskId) return
  
  try {
    loading.value = true
    console.log('📋 开始加载处理日志列表，任务ID:', props.taskId)
    
    const response = await aiExcelImportApi.getProcessingLogs(props.taskId, {
      page: currentPage.value,
      size: pageSize.value,
      level: selectedLevel.value || undefined,
      sortBy: 'timestamp',
      sortOrder: 'desc'
    })
    
    console.log('📊 处理日志API响应:', response)
    
    if (response && response.content) {
      logs.value = response.content
      totalLogs.value = response.totalElements
      console.log('✅ 处理日志列表加载成功，总数:', response.totalElements)
    } else if (response && Array.isArray(response)) {
      // 如果响应直接是数组
      logs.value = response
      totalLogs.value = response.length
      console.log('✅ 处理日志列表加载成功（数组格式），总数:', response.length)
    } else {
      logs.value = []
      totalLogs.value = 0
      console.warn('⚠️ 处理日志列表响应格式异常:', response)
    }
    
  } catch (error: any) {
    console.error('❌ 加载处理日志列表失败:', error)
    ElMessage.error(`加载处理日志列表失败: ${error.message || '未知错误'}`)
    logs.value = []
    totalLogs.value = 0
  } finally {
    loading.value = false
  }
}

// 查看日志详情
const viewLogDetails = (log: any) => {
  selectedLog.value = log
  logDetailDialogVisible.value = true
}

// 刷新日志
const refreshLogs = () => {
  currentPage.value = 1
  loadProcessingLogs()
  emit('refresh')
}

// 日志级别筛选
const handleLevelChange = () => {
  currentPage.value = 1
  loadProcessingLogs()
}

// 分页处理
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadProcessingLogs()
}

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  loadProcessingLogs()
}

// 格式化时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  try {
    return new Date(dateTime).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return dateTime
  }
}

// 获取日志级别标签类型
const getLevelTagType = (level: string) => {
  switch (level) {
    case 'SUCCESS':
      return 'success'
    case 'FAILED':
      return 'danger'
    case 'MANUAL_PROCESS':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取日志级别文本
const getLevelText = (level: string) => {
  switch (level) {
    case 'SUCCESS':
      return '成功'
    case 'FAILED':
      return '失败'
    case 'MANUAL_PROCESS':
      return '手动处理'
    default:
      return level || '未知'
  }
}

// 获取处理步骤标签类型
const getStepTagType = (step: string) => {
  switch (step) {
    case 'SUCCESS':
      return 'success'
    case 'FAILED':
      return 'danger'
    case 'MANUAL_PROCESS':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取处理步骤文本
const getStepText = (step: string) => {
  switch (step) {
    case 'SUCCESS':
      return '处理成功'
    case 'FAILED':
      return '处理失败'
    case 'MANUAL_PROCESS':
      return '需手动处理'
    default:
      return step || '未知'
  }
}

// 监听任务ID变化
watch(() => props.taskId, (newTaskId) => {
  if (newTaskId && props.autoLoad) {
    currentPage.value = 1
    selectedLevel.value = ''
    loadProcessingLogs()
  }
})

// 暴露方法
const refresh = () => {
  loadProcessingLogs()
}

defineExpose({ refresh, loadProcessingLogs })

onMounted(() => {
  if (props.taskId && props.autoLoad) {
    loadProcessingLogs()
  }
})
</script>

<style scoped>
.processing-logs-list {
  padding: 16px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.list-header h4 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}

.log-filters {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.loading-container {
  padding: 20px;
}

.logs-container {
  background: var(--el-bg-color);
  border-radius: 8px;
  overflow: hidden;
}

.logs-table {
  --el-table-border-color: var(--el-border-color-lighter);
}

.timestamp {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.log-message {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-row, .no-details {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}

.pagination-container {
  padding: 16px;
  display: flex;
  justify-content: center;
  background: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-lighter);
}

.empty-container {
  padding: 40px;
  text-align: center;
}

.log-detail-content {
  padding: 16px 0;
}

.log-details {
  background: var(--el-fill-color-light);
  padding: 12px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
