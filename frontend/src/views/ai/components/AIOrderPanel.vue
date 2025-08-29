<template>
  <div class="ai-order-panel">
    <!-- 快速处理卡片 -->
    <div class="quick-process-card">
      <div class="card-header">
        <h3 class="card-title">快速AI处理</h3>
        <p class="card-subtitle">快速处理单个订单或批量订单</p>
      </div>
      
      <div class="process-options">
        <el-button type="primary" @click="showSingleProcessDialog">
          <el-icon><MagicStick /></el-icon>
          单个订单处理
        </el-button>
        <el-button type="success" @click="showBatchProcessDialog">
          <el-icon><Collection /></el-icon>
          批量订单处理
        </el-button>
        <el-button type="warning" @click="showExcelImportDialog">
          <el-icon><Document /></el-icon>
          Excel批量导入
        </el-button>
      </div>
    </div>

    <!-- 处理状态监控 -->
    <div class="status-monitor-card">
      <div class="card-header">
        <h3 class="card-title">处理状态监控</h3>
        <el-button type="primary" size="small" @click="refreshStatus">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <div class="status-grid">
        <div class="status-item">
          <div class="status-icon success">
            <el-icon><Check /></el-icon>
          </div>
          <div class="status-content">
            <div class="status-number">{{ statusData.successCount }}</div>
            <div class="status-label">处理成功</div>
          </div>
        </div>
        
        <div class="status-item">
          <div class="status-icon warning">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="status-content">
            <div class="status-number">{{ statusData.pendingCount }}</div>
            <div class="status-label">等待处理</div>
          </div>
        </div>
        
        <div class="status-item">
          <div class="status-icon error">
            <el-icon><Close /></el-icon>
          </div>
          <div class="status-content">
            <div class="status-number">{{ statusData.failedCount }}</div>
            <div class="status-label">处理失败</div>
          </div>
        </div>
        
        <div class="status-item">
          <div class="status-icon info">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="status-content">
            <div class="status-number">{{ statusData.totalCount }}</div>
            <div class="status-label">总处理数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近处理记录 -->
    <div class="recent-records-card">
      <div class="card-header">
        <h3 class="card-title">最近处理记录</h3>
        <el-button type="text" @click="viewAllRecords">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <div class="records-list">
        <div
          v-for="record in recentRecords"
          :key="record.processId"
          class="record-item"
          :class="{ 'success': record.success, 'error': !record.success }"
        >
          <div class="record-header">
            <div class="record-status">
              <el-tag :type="record.success ? 'success' : 'danger'" size="small">
                {{ record.success ? '成功' : '失败' }}
              </el-tag>
            </div>
            <div class="record-time">
              {{ formatDate(record.createdAt) }}
            </div>
          </div>
          
          <div class="record-content">
            <div class="record-info">
              <span class="label">订单ID：</span>
              <span class="value">{{ record.orderId }}</span>
            </div>
            <div class="record-info">
              <span class="label">处理类型：</span>
              <span class="value">{{ record.processType }}</span>
            </div>
            <div class="record-info">
              <span class="label">处理结果：</span>
              <span class="value">{{ record.result }}</span>
            </div>
          </div>
          
          <div class="record-footer">
            <span class="processing-time">处理耗时: {{ record.processingTime }}ms</span>
            <el-button type="text" size="small" @click="viewRecordDetail(record)">
              查看详情
            </el-button>
          </div>
        </div>
        
        <div v-if="recentRecords.length === 0" class="no-records">
          <el-empty description="暂无处理记录" />
        </div>
      </div>
    </div>

    <!-- 单个订单处理对话框 -->
    <el-dialog
      v-model="singleProcessDialogVisible"
      title="单个订单AI处理"
      width="600px"
      class="process-dialog"
    >
      <SingleOrderProcess @success="handleProcessSuccess" @cancel="singleProcessDialogVisible = false" />
    </el-dialog>

    <!-- 批量处理对话框 -->
    <el-dialog
      v-model="batchProcessDialogVisible"
      title="批量订单AI处理"
      width="800px"
      class="process-dialog"
    >
      <BatchOrderProcess @success="handleProcessSuccess" @cancel="batchProcessDialogVisible = false" />
    </el-dialog>

    <!-- Excel导入对话框 -->
    <el-dialog
      v-model="excelImportDialogVisible"
      title="Excel批量导入处理"
      width="700px"
      class="process-dialog"
    >
      <ExcelImportProcess @success="handleProcessSuccess" @cancel="excelImportDialogVisible = false" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Collection, Document, Check, Clock, Close, TrendCharts, Refresh, ArrowRight } from '@element-plus/icons-vue'
import SingleOrderProcess from './SingleOrderProcess.vue'
import BatchOrderProcess from './BatchOrderProcess.vue'
import ExcelImportProcess from './ExcelImportProcess.vue'
import type { AIOrderProcessResult } from '@/types/ai'

// 响应式数据
const singleProcessDialogVisible = ref(false)
const batchProcessDialogVisible = ref(false)
const excelImportDialogVisible = ref(false)
const recentRecords = ref<AIOrderProcessResult[]>([])

// 状态数据
const statusData = ref({
  successCount: 0,
  pendingCount: 0,
  failedCount: 0,
  totalCount: 0
})

// 方法
const showSingleProcessDialog = () => {
  singleProcessDialogVisible.value = true
}

const showBatchProcessDialog = () => {
  batchProcessDialogVisible.value = true
}

const showExcelImportDialog = () => {
  excelImportDialogVisible.value = false
}

const refreshStatus = async () => {
  try {
    // TODO: 调用API获取状态数据
    ElMessage.success('状态已刷新')
  } catch (error) {
    ElMessage.error('刷新状态失败')
  }
}

const viewAllRecords = () => {
  // TODO: 跳转到历史记录页面
  console.log('查看全部记录')
}

const viewRecordDetail = (record: AIOrderProcessResult) => {
  // TODO: 显示记录详情
  console.log('查看记录详情:', record)
}

const handleProcessSuccess = () => {
  // 处理成功后刷新数据
  refreshStatus()
  loadRecentRecords()
}

const loadRecentRecords = async () => {
  try {
    // TODO: 调用API获取最近处理记录
    // 模拟数据
    recentRecords.value = [
      {
        processId: 1,
        orderId: 1001,
        companyId: 1,
        processType: '智能分析',
        status: 'SUCCESS',
        result: '订单分析完成，建议优化库存管理',
        description: '分析订单趋势和客户行为',
        modelName: 'deepseek-chat',
        processingTime: 2500,
        userId: 1,
        userName: 'admin',
        startTime: new Date().toISOString(),
        endTime: new Date().toISOString(),
        createdAt: new Date().toISOString(),
        success: true,
        priority: 1
      }
    ]
  } catch (error) {
    console.error('加载最近记录失败:', error)
  }
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadRecentRecords()
})
</script>

<style scoped>
.ai-order-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.quick-process-card,
.status-monitor-card,
.recent-records-card {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--md-sys-elevation-level1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0;
}

.card-subtitle {
  font-size: 14px;
  color: var(--md-sys-color-on-surface-variant);
  margin: 0;
}

.process-options {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--md-sys-color-surface);
  border-radius: 12px;
  border: 1px solid var(--md-sys-color-outline-variant);
  transition: all 0.3s ease;
}

.status-item:hover {
  border-color: var(--md-sys-color-primary);
  box-shadow: var(--md-sys-elevation-level2);
}

.status-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.status-icon.success {
  background: var(--md-sys-color-success);
}

.status-icon.warning {
  background: var(--md-sys-color-warning);
}

.status-icon.error {
  background: var(--md-sys-color-error);
}

.status-icon.info {
  background: var(--md-sys-color-primary);
}

.status-content {
  flex: 1;
}

.status-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--md-sys-color-on-surface);
  line-height: 1;
}

.status-label {
  font-size: 14px;
  color: var(--md-sys-color-on-surface-variant);
  margin-top: 4px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-item {
  background: var(--md-sys-color-surface);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--md-sys-color-outline-variant);
  transition: all 0.3s ease;
}

.record-item:hover {
  border-color: var(--md-sys-color-primary);
  box-shadow: var(--md-sys-elevation-level1);
}

.record-item.success {
  border-left: 4px solid var(--md-sys-color-success);
}

.record-item.error {
  border-left: 4px solid var(--md-sys-color-error);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.record-time {
  font-size: 12px;
  color: var(--md-sys-color-on-surface-variant);
}

.record-content {
  margin-bottom: 16px;
}

.record-info {
  display: flex;
  margin-bottom: 8px;
}

.record-info .label {
  font-weight: 500;
  color: var(--md-sys-color-on-surface-variant);
  min-width: 80px;
}

.record-info .value {
  color: var(--md-sys-color-on-surface);
}

.record-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.processing-time {
  font-size: 12px;
  color: var(--md-sys-color-on-surface-variant);
}

.no-records {
  padding: 40px 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .process-options {
    flex-direction: column;
  }
  
  .process-options .el-button {
    width: 100%;
  }
  
  .status-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .status-item {
    padding: 16px;
  }
  
  .status-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .status-number {
    font-size: 24px;
  }
}
</style>
