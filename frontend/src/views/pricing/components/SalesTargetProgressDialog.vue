<template>
  <el-dialog
    :model-value="modelValue"
    title="销售目标进度详情"
    width="1000px"
    @close="handleClose"
  >
    <div v-if="salesTarget" class="progress-dialog">
      <!-- 目标基本信息 -->
      <div class="target-info">
        <h3>{{ salesTarget.targetName }}</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">目标类型：</span>
            <el-tag :type="getTargetTypeType(salesTarget.targetType)">
              {{ getTargetTypeLabel(salesTarget.targetType) }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">目标周期：</span>
            <el-tag :type="getTargetPeriodType(salesTarget.targetPeriod)">
              {{ salesTarget.targetPeriod }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">目标年份：</span>
            <span>{{ salesTarget.targetYear }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态：</span>
            <el-tag :type="getStatusType(salesTarget.status)">
              {{ getStatusLabel(salesTarget.status) }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 目标值对比 -->
      <div class="target-comparison">
        <h4>目标值对比</h4>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="metric-card">
              <div class="metric-title">GMV目标</div>
              <div class="metric-value">¥{{ formatNumber(salesTarget.gmvTarget || 0) }}</div>
              <div class="metric-achieved">已达成：¥{{ formatNumber(progressData.gmvAchieved || 0) }}</div>
              <el-progress
                :percentage="progressData.gmvProgressPercentage || 0"
                :status="getProgressStatus(progressData.gmvProgressPercentage)"
                :stroke-width="8"
              />
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-card">
              <div class="metric-title">收入目标</div>
              <div class="metric-value">¥{{ formatNumber(salesTarget.revenueTarget || 0) }}</div>
              <div class="metric-achieved">已达成：¥{{ formatNumber(progressData.revenueAchieved || 0) }}</div>
              <el-progress
                :percentage="progressData.revenueProgressPercentage || 0"
                :status="getProgressStatus(progressData.revenueProgressPercentage)"
                :stroke-width="8"
              />
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-card">
              <div class="metric-title">订单数量目标</div>
              <div class="metric-value">{{ formatNumber(salesTarget.orderCountTarget || 0) }}</div>
              <div class="metric-achieved">已达成：{{ formatNumber(progressData.orderCountAchieved || 0) }}</div>
              <el-progress
                :percentage="progressData.orderProgressPercentage || 0"
                :status="getProgressStatus(progressData.orderProgressPercentage)"
                :stroke-width="8"
              />
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-card">
              <div class="metric-title">客户数量目标</div>
              <div class="metric-value">{{ formatNumber(salesTarget.customerCountTarget || 0) }}</div>
              <div class="metric-achieved">已达成：{{ formatNumber(progressData.customerCountAchieved || 0) }}</div>
              <el-progress
                :percentage="progressData.customerProgressPercentage || 0"
                :status="getProgressStatus(progressData.customerProgressPercentage)"
                :stroke-width="8"
              />
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 整体进度 -->
      <div class="overall-progress">
        <h4>整体进度</h4>
        <div class="progress-summary">
          <el-progress
            :percentage="progressData.overallProgressPercentage || 0"
            :status="getProgressStatus(progressData.overallProgressPercentage)"
            :stroke-width="12"
            class="overall-progress-bar"
          />
          <div class="progress-text">
            整体完成率：{{ (progressData.overallProgressPercentage || 0).toFixed(2) }}%
          </div>
        </div>
      </div>

      <!-- 差距分析 -->
      <div class="gap-analysis" v-if="progressData.gapAnalysis">
        <h4>差距分析</h4>
        <div class="gap-content">
          {{ progressData.gapAnalysis }}
        </div>
      </div>

      <!-- 行动计划 -->
      <div class="action-plan" v-if="progressData.actionPlan">
        <h4>行动计划</h4>
        <div class="plan-content">
          {{ progressData.actionPlan }}
        </div>
      </div>

      <!-- 进度历史 -->
      <div class="progress-history">
        <h4>进度历史</h4>
        <el-table :data="progressHistory" border stripe style="width: 100%">
          <el-table-column prop="progressDate" label="进度日期" width="120" />
          <el-table-column prop="gmvAchieved" label="GMV达成" width="120">
            <template #default="{ row }">
              ¥{{ formatNumber(row.gmvAchieved || 0) }}
            </template>
          </el-table-column>
          <el-table-column prop="revenueAchieved" label="收入达成" width="120">
            <template #default="{ row }">
              ¥{{ formatNumber(row.revenueAchieved || 0) }}
            </template>
          </el-table-column>
          <el-table-column prop="orderCountAchieved" label="订单达成" width="100" />
          <el-table-column prop="customerCountAchieved" label="客户达成" width="100" />
          <el-table-column prop="overallProgressPercentage" label="整体进度" width="100">
            <template #default="{ row }">
              <el-progress
                :percentage="row.overallProgressPercentage || 0"
                :status="getProgressStatus(row.overallProgressPercentage)"
                :stroke-width="6"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { salesTargetApi } from '@/api/salesTarget'

// Props
interface Props {
  modelValue: boolean
  salesTarget?: any
}

const props = withDefaults(defineProps<Props>(), {
  salesTarget: null
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

// 进度数据
const progressData = ref({
  gmvAchieved: 0,
  revenueAchieved: 0,
  orderCountAchieved: 0,
  customerCountAchieved: 0,
  gmvProgressPercentage: 0,
  revenueProgressPercentage: 0,
  orderProgressPercentage: 0,
  customerProgressPercentage: 0,
  overallProgressPercentage: 0,
  gapAnalysis: '',
  actionPlan: ''
})

// 进度历史
const progressHistory = ref([])

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
}

// 获取进度数据
const fetchProgressData = async () => {
  if (!props.salesTarget) return

  try {
    const response = await salesTargetApi.getTargetProgress(props.salesTarget.id)
    progressData.value = response
  } catch (error) {
    console.error('获取进度数据失败:', error)
  }
}

// 获取进度历史
const fetchProgressHistory = async () => {
  if (!props.salesTarget) return

  try {
    const response = await salesTargetApi.getTargetProgressHistory(props.salesTarget.id)
    progressHistory.value = response
  } catch (error) {
    console.error('获取进度历史失败:', error)
  }
}

// 监听销售目标变化
watch(
  () => props.salesTarget,
  (newSalesTarget) => {
    if (newSalesTarget) {
      fetchProgressData()
      fetchProgressHistory()
    }
  },
  { immediate: true }
)

// 工具函数
const getTargetTypeType = (type: string) => {
  const types: Record<string, string> = {
    'GMV': 'success',
    'REVENUE': 'warning',
    'ORDER_COUNT': 'info',
    'CUSTOMER_COUNT': 'primary'
  }
  return types[type] || 'info'
}

const getTargetTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'GMV': 'GMV目标',
    'REVENUE': '收入目标',
    'ORDER_COUNT': '订单数量',
    'CUSTOMER_COUNT': '客户数量'
  }
  return labels[type] || type
}

const getTargetPeriodType = (period: string) => {
  const types: Record<string, string> = {
    'Q1': 'success',
    'Q2': 'warning',
    'Q3': 'info',
    'Q4': 'primary',
    'H1': 'danger',
    'H2': 'danger',
    'YEARLY': 'danger'
  }
  return types[period] || 'info'
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    'DRAFT': 'info',
    'ACTIVE': 'success',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'DRAFT': '草稿',
    'ACTIVE': '激活',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return labels[status] || status
}

const getProgressStatus = (percentage: number) => {
  if (percentage >= 100) return 'success'
  if (percentage >= 80) return 'warning'
  if (percentage >= 60) return ''
  return 'exception'
}

const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN')
}
</script>

<style scoped>
.progress-dialog {
  padding: 20px 0;
}

.target-info {
  margin-bottom: 24px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.target-info h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 18px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  font-weight: 500;
  color: #606266;
}

.target-comparison {
  margin-bottom: 24px;
}

.target-comparison h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.metric-card {
  padding: 16px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  text-align: center;
}

.metric-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.metric-achieved {
  font-size: 12px;
  color: #606266;
  margin-bottom: 12px;
}

.overall-progress {
  margin-bottom: 24px;
}

.overall-progress h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.progress-summary {
  text-align: center;
}

.overall-progress-bar {
  margin-bottom: 12px;
}

.progress-text {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.gap-analysis,
.action-plan {
  margin-bottom: 24px;
}

.gap-analysis h4,
.action-plan h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.gap-content,
.plan-content {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  white-space: pre-line;
  line-height: 1.6;
}

.progress-history {
  margin-bottom: 24px;
}

.progress-history h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-progress) {
  margin: 0;
}

:deep(.el-table th) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
}
</style>
