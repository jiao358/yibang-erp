<template>
  <div class="result-display-container">
    <!-- 结果概览 -->
    <div class="result-overview">
      <div class="overview-header">
        <h3 class="overview-title">处理结果概览</h3>
        <div class="overview-actions">
          <el-button type="primary" @click="handleExportResults">
            <el-icon><Download /></el-icon>
            导出结果
          </el-button>
          <el-button type="success" @click="handleRetryProcessing">
            <el-icon><Refresh /></el-icon>
            重新处理
          </el-button>
        </div>
      </div>

      <!-- 结果统计卡片 -->
      <div class="result-statistics">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card total">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ result.totalRows }}</div>
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
                <div class="stat-number">{{ result.successRows }}</div>
                <div class="stat-label">成功处理</div>
                <div class="stat-percentage">{{ getSuccessPercentage() }}%</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card failed">
              <div class="stat-icon">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ result.failedRows }}</div>
                <div class="stat-label">处理失败</div>
                <div class="stat-percentage">{{ getFailedPercentage() }}%</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card manual">
              <div class="stat-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ result.manualProcessRows }}</div>
                <div class="stat-label">需手动处理</div>
                <div class="stat-percentage">{{ getManualPercentage() }}%</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 处理时间信息 -->
      <div class="processing-time">
        <div class="time-item">
          <span class="time-label">开始时间：</span>
          <span class="time-value">{{ formatDateTime(result.createdAt) }}</span>
        </div>
        <div class="time-item">
          <span class="time-label">完成时间：</span>
          <span class="time-value">{{ formatDateTime(result.completedAt) }}</span>
        </div>
        <div class="time-item">
          <span class="time-label">处理耗时：</span>
          <span class="time-value">{{ formatProcessingTime(result.processingTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 详细结果表格 -->
    <div class="result-details">
      <div class="details-header">
        <h3 class="details-title">详细处理结果</h3>
        <div class="details-filters">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable>
            <el-option label="全部" value="" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="需手动处理" value="MANUAL_PROCESS" />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索关键词"
            clearable
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 结果表格 -->
      <el-table
        :data="filteredDetails"
        style="width: 100%"
        max-height="500"
        stripe
        border
      >
        <el-table-column prop="rowNumber" label="行号" width="80" align="center" />
        
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="confidence" label="置信度" width="100" align="center">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.confidence * 100" 
              :status="getConfidenceStatus(row.confidence)"
              :stroke-width="8"
              :show-text="false"
            />
            <span class="confidence-text">{{ (row.confidence * 100).toFixed(1) }}%</span>
          </template>
        </el-table-column>

        <el-table-column label="客户匹配" min-width="200">
          <template #default="{ row }">
            <div class="match-info">
              <div class="match-status">
                <el-tag 
                  :type="row.customerMatch.matched ? 'success' : 'danger'" 
                  size="small"
                >
                  {{ row.customerMatch.matched ? '匹配成功' : '匹配失败' }}
                </el-tag>
              </div>
              <div class="match-details" v-if="row.customerMatch.matched">
                <div>客户: {{ row.customerMatch.customerName }}</div>
                <div>编码: {{ row.customerMatch.customerCode }}</div>
              </div>
              <div class="match-reason" v-else>
                {{ row.customerMatch.matchReason }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="商品匹配" min-width="200">
          <template #default="{ row }">
            <div class="match-info">
              <div class="match-status">
                <el-tag 
                  :type="row.productMatch.matched ? 'success' : 'danger'" 
                  size="small"
                >
                  {{ row.productMatch.matched ? '匹配成功' : '匹配失败' }}
                </el-tag>
              </div>
              <div class="match-details" v-if="row.productMatch.matched">
                <div>商品: {{ row.productMatch.productName }}</div>
                <div>SKU: {{ row.productMatch.sku }}</div>
              </div>
              <div class="match-reason" v-else>
                {{ row.productMatch.matchReason }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="orderId" label="订单ID" width="120" align="center">
          <template #default="{ row }">
            <el-link 
              v-if="row.orderId" 
              type="primary" 
              @click="viewOrder(row.orderId)"
            >
              {{ row.orderId }}
            </el-link>
            <span v-else class="no-order">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="errorMessage" label="错误信息" min-width="200">
          <template #default="{ row }">
            <el-tooltip 
              v-if="row.errorMessage" 
              :content="row.errorMessage" 
              placement="top"
            >
              <span class="error-message">{{ row.errorMessage }}</span>
            </el-tooltip>
            <span v-else class="no-error">-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewDetail(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredDetails.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="处理详情"
      width="800px"
      :before-close="handleCloseDetailDialog"
    >
      <div v-if="selectedDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="行号">{{ selectedDetail.rowNumber }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedDetail.status)">
              {{ getStatusText(selectedDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="置信度">{{ (selectedDetail.confidence * 100).toFixed(1) }}%</el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ selectedDetail.orderId || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">客户匹配详情</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="匹配状态">
            <el-tag :type="selectedDetail.customerMatch.matched ? 'success' : 'danger'">
              {{ selectedDetail.customerMatch.matched ? '匹配成功' : '匹配失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="匹配置信度">{{ (selectedDetail.customerMatch.confidence * 100).toFixed(1) }}%</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ selectedDetail.customerMatch.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户编码">{{ selectedDetail.customerMatch.customerCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="匹配原因" :span="2">{{ selectedDetail.customerMatch.matchReason }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="2" v-if="selectedDetail.customerMatch.suggestion">
            {{ selectedDetail.customerMatch.suggestion }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">商品匹配详情</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="匹配状态">
            <el-tag :type="selectedDetail.productMatch.matched ? 'success' : 'danger'">
              {{ selectedDetail.productMatch.matched ? '匹配成功' : '匹配失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="匹配置信度">{{ (selectedDetail.productMatch.confidence * 100).toFixed(1) }}%</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ selectedDetail.productMatch.productName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品SKU">{{ selectedDetail.productMatch.sku || '-' }}</el-descriptions-item>
          <el-descriptions-item label="匹配原因" :span="2">{{ selectedDetail.productMatch.matchReason }}</el-descriptions-item>
          <el-descriptions-item label="建议" :span="2" v-if="selectedDetail.productMatch.suggestion">
            {{ selectedDetail.productMatch.suggestion }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">错误信息</el-divider>
        <div v-if="selectedDetail.errorMessage" class="error-detail">
          <el-alert
            :title="selectedDetail.errorMessage"
            type="error"
            :closable="false"
            show-icon
          />
        </div>
        <div v-else class="no-error-detail">
          <el-alert
            title="无错误信息"
            type="success"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Download, 
  Refresh, 
  Document, 
  CircleCheck, 
  CircleClose, 
  Warning,
  Search
} from '@element-plus/icons-vue'
import type { ProcessingResult, ProcessingDetail } from '@/types/ai'

// Props
interface Props {
  result: ProcessingResult
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  exportResults: []
  retryProcessing: []
}>()

// 响应式数据
const statusFilter = ref('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const detailDialogVisible = ref(false)
const selectedDetail = ref<ProcessingDetail | null>(null)

// 计算属性
const filteredDetails = computed(() => {
  let filtered = props.result.details

  // 状态筛选
  if (statusFilter.value) {
    filtered = filtered.filter(item => item.status === statusFilter.value)
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(item => {
      return (
        item.customerMatch.customerName?.toLowerCase().includes(keyword) ||
        item.customerMatch.customerCode?.toLowerCase().includes(keyword) ||
        item.productMatch.productName?.toLowerCase().includes(keyword) ||
        item.productMatch.sku?.toLowerCase().includes(keyword) ||
        item.errorMessage?.toLowerCase().includes(keyword)
      )
    })
  }

  return filtered
})

// 方法
const getSuccessPercentage = () => {
  if (props.result.totalRows === 0) return 0
  return Math.round((props.result.successRows / props.result.totalRows) * 100)
}

const getFailedPercentage = () => {
  if (props.result.totalRows === 0) return 0
  return Math.round((props.result.failedRows / props.result.totalRows) * 100)
}

const getManualPercentage = () => {
  if (props.result.totalRows === 0) return 0
  return Math.round((props.result.manualProcessRows / props.result.totalRows) * 100)
}

const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const formatProcessingTime = (milliseconds: number) => {
  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes % 60}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟${seconds % 60}秒`
  } else {
    return `${seconds}秒`
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'SUCCESS': return 'success'
    case 'FAILED': return 'danger'
    case 'MANUAL_PROCESS': return 'warning'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'SUCCESS': return '成功'
    case 'FAILED': return '失败'
    case 'MANUAL_PROCESS': return '需手动处理'
    default: return '未知'
  }
}

const getConfidenceStatus = (confidence: number) => {
  if (confidence >= 0.8) return 'success'
  if (confidence >= 0.6) return 'warning'
  return 'exception'
}

const handleExportResults = () => {
  emit('exportResults')
  ElMessage.success('开始导出结果')
}

const handleRetryProcessing = () => {
  emit('retryProcessing')
  ElMessage.info('开始重新处理')
}

const viewOrder = (orderId: string) => {
  // TODO: 跳转到订单详情页面
  ElMessage.info(`查看订单: ${orderId}`)
}

const viewDetail = (detail: ProcessingDetail) => {
  selectedDetail.value = detail
  detailDialogVisible.value = true
}

const handleCloseDetailDialog = () => {
  detailDialogVisible.value = false
  selectedDetail.value = null
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
.result-display-container {
  width: 100%;
}

.result-overview {
  margin-bottom: 32px;
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.overview-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.overview-actions {
  display: flex;
  gap: 12px;
}

.result-statistics {
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
  margin-bottom: 4px;
}

.stat-percentage {
  font-size: 12px;
  color: #67c23a;
  font-weight: 600;
}

.processing-time {
  display: flex;
  gap: 32px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.time-value {
  font-size: 14px;
  color: #303133;
}

.result-details {
  margin-bottom: 32px;
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.details-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.details-filters {
  display: flex;
  gap: 16px;
}

.confidence-text {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.match-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.match-status {
  display: flex;
  justify-content: center;
}

.match-details {
  font-size: 12px;
  color: #606266;
  text-align: center;
}

.match-reason {
  font-size: 12px;
  color: #f56c6c;
  text-align: center;
}

.no-order,
.no-error {
  color: #909399;
  font-style: italic;
}

.error-message {
  color: #f56c6c;
  cursor: pointer;
  text-decoration: underline;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.error-detail,
.no-error-detail {
  margin-top: 16px;
}
</style>
