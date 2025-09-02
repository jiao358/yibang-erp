<template>
  <div class="processing-progress-container">
    <!-- 进度概览 -->
    <div class="progress-overview">
      <div class="progress-header">
        <div class="status-indicator">
          <el-tag :type="getStatusType(status)" size="large">
            {{ getStatusText(status) }}
          </el-tag>
        </div>
        <div class="progress-actions">
          <el-button 
            type="danger" 
            size="small" 
            @click="handleCancel"
            :disabled="status === 'COMPLETED' || status === 'FAILED'"
          >
            取消处理
          </el-button>
          <el-button 
            type="info" 
            size="small" 
            @click="refreshProgress"
            :loading="refreshing"
          >
            刷新进度
          </el-button>
        </div>
      </div>

      <!-- 主要进度条 -->
      <div class="main-progress" v-if="progress">
        <div class="progress-info">
          <span class="progress-text">总体进度</span>
          <span class="progress-percentage">{{ progress.percentage }}%</span>
        </div>
        <el-progress 
          :percentage="progress.percentage" 
          :status="getProgressStatus(progress.percentage)"
          :stroke-width="12"
          :show-text="false"
        />
        <div class="progress-details">
          <span>已处理: {{ progress.processedRows }}/{{ progress.totalRows }}</span>
          <span v-if="progress.estimatedTime">预计剩余: {{ progress.estimatedTime }}</span>
        </div>
      </div>
    </div>

    <!-- 详细进度统计 -->
    <div class="progress-statistics" v-if="progress">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card total">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ progress.totalRows }}</div>
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
              <div class="stat-number">{{ progress.successRows }}</div>
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
              <div class="stat-number">{{ progress.failedRows }}</div>
              <div class="stat-label">处理失败</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card manual">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ progress.manualProcessRows }}</div>
              <div class="stat-label">需手动处理</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 当前步骤信息 -->
    <div class="current-step" v-if="progress && progress.currentStep">
      <h4 class="step-title">当前步骤</h4>
      <div class="step-content">
        <el-icon class="step-icon"><Loading /></el-icon>
        <span class="step-text">{{ progress.currentStep }}</span>
      </div>
    </div>

    <!-- 实时日志 -->
    <div class="progress-logs">
      <h4 class="logs-title">
        处理日志
        <el-button 
          type="text" 
          size="small" 
          @click="clearLogs"
          :disabled="logs.length === 0"
        >
          清空日志
        </el-button>
      </h4>
      <div class="logs-container">
        <div 
          v-for="(log, index) in logs" 
          :key="index"
          class="log-item"
          :class="getLogLevel(log.level)"
        >
          <span class="log-time">{{ formatTime(log.timestamp) }}</span>
          <span class="log-level">{{ log.level }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
        <div v-if="logs.length === 0" class="no-logs">
          暂无处理日志
        </div>
      </div>
    </div>

    <!-- 处理时间线 -->
    <div class="processing-timeline" v-if="timeline.length > 0">
      <h4 class="timeline-title">处理时间线</h4>
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in timeline"
          :key="index"
          :timestamp="formatTime(item.timestamp)"
          :type="getTimelineType(item.type)"
        >
          {{ item.description }}
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, 
  CircleCheck, 
  CircleClose, 
  Warning, 
  Loading 
} from '@element-plus/icons-vue'
import type { ProcessingProgress } from '@/types/ai'

// Props
interface Props {
  progress: ProcessingProgress | null
  status: string
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  cancelProcessing: []
}>()

// 响应式数据
const refreshing = ref(false)
const logs = ref<LogItem[]>([])
const timeline = ref<TimelineItem[]>([])

// 日志项类型
interface LogItem {
  timestamp: number
  level: 'INFO' | 'WARNING' | 'ERROR' | 'SUCCESS'
  message: string
}

// 时间线项类型
interface TimelineItem {
  timestamp: number
  type: 'primary' | 'success' | 'warning' | 'danger'
  description: string
}

// 计算属性
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

const getProgressStatus = (percentage: number) => {
  if (percentage >= 100) return 'success'
  if (percentage >= 80) return 'warning'
  if (percentage >= 50) return ''
  return 'exception'
}

const getLogLevel = (level: string) => {
  return `log-${level.toLowerCase()}`
}

const getTimelineType = (type: string) => {
  return type as 'primary' | 'success' | 'warning' | 'danger'
}

// 方法
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要取消当前的处理任务吗？取消后无法恢复。',
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    emit('cancelProcessing')
    ElMessage.info('已发送取消请求')
  } catch {
    // 用户取消
  }
}

const refreshProgress = async () => {
  refreshing.value = true
  try {
    // TODO: 调用API刷新进度
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('进度已刷新')
  } catch (error) {
    ElMessage.error('刷新进度失败')
  } finally {
    refreshing.value = false
  }
}

const clearLogs = () => {
  logs.value = []
  ElMessage.info('日志已清空')
}

const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN')
}

// 模拟日志更新（实际应该通过WebSocket或轮询获取）
const addLog = (level: LogItem['level'], message: string) => {
  logs.value.push({
    timestamp: Date.now(),
    level,
    message
  })
  
  // 限制日志数量
  if (logs.value.length > 100) {
    logs.value.shift()
  }
}

// 模拟时间线更新
const addTimeline = (type: TimelineItem['type'], description: string) => {
  timeline.value.push({
    timestamp: Date.now(),
    type,
    description
  })
}

// 生命周期
onMounted(() => {
  // 模拟添加一些初始日志
  addLog('INFO', '开始处理Excel文件')
  addLog('INFO', '正在识别列标题...')
  
  addTimeline('primary', '任务开始')
  addTimeline('success', '文件上传完成')
  
  // 模拟实时日志更新
  const logInterval = setInterval(() => {
    if (props.status === 'PROCESSING') {
      const messages = [
        '正在处理第100行数据...',
        '商品匹配成功，置信度: 95%',
        '客户信息识别完成',
        '正在创建订单记录...'
      ]
      const randomMessage = messages[Math.floor(Math.random() * messages.length)]
      addLog('INFO', randomMessage)
    }
  }, 3000)
  
  // 清理定时器
  onUnmounted(() => {
    clearInterval(logInterval)
  })
})
</script>

<style scoped>
.processing-progress-container {
  width: 100%;
}

.progress-overview {
  margin-bottom: 24px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.progress-actions {
  display: flex;
  gap: 12px;
}

.main-progress {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.progress-percentage {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.progress-details {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 14px;
  color: #909399;
}

.progress-statistics {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  margin-right: 16px;
  font-size: 32px;
}

.stat-card.total .stat-icon {
  color: #409eff;
}

.stat-card.success .stat-icon {
  color: #67c23a;
}

.stat-card.failed .stat-icon {
  color: #f56c6c;
}

.stat-card.manual .stat-icon {
  color: #e6a23c;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.current-step {
  margin-bottom: 24px;
  padding: 20px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #b3d8ff;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.step-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.step-icon {
  font-size: 20px;
  color: #409eff;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.step-text {
  font-size: 16px;
  color: #409eff;
  font-weight: 500;
}

.progress-logs {
  margin-bottom: 24px;
}

.logs-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.logs-container {
  max-height: 300px;
  overflow-y: auto;
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  border: 1px solid #e9ecef;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #e9ecef;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #909399;
  min-width: 80px;
}

.log-level {
  min-width: 60px;
  font-weight: 600;
}

.log-message {
  flex: 1;
  color: #303133;
}

.log-info .log-level {
  color: #409eff;
}

.log-warning .log-level {
  color: #e6a23c;
}

.log-error .log-level {
  color: #f56c6c;
}

.log-success .log-level {
  color: #67c23a;
}

.no-logs {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.processing-timeline {
  margin-bottom: 24px;
}

.timeline-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.el-timeline {
  padding-left: 20px;
}
</style>
