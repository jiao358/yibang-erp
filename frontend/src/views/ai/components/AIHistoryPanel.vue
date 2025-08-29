<template>
  <div class="ai-history-panel">
    <div class="panel-header">
      <h3 class="panel-title">AI处理历史</h3>
      <p class="panel-subtitle">查看所有AI订单处理记录和结果</p>
    </div>
    
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="处理类型">
          <el-select v-model="searchForm.processType" placeholder="全部类型" clearable>
            <el-option label="智能分析" value="智能分析" />
            <el-option label="订单优化" value="订单优化" />
            <el-option label="库存预测" value="库存预测" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="处理中" value="PROCESSING" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-section">
      <el-table :data="historyList" v-loading="loading" style="width: 100%">
        <el-table-column prop="processId" label="处理ID" width="80" />
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column prop="processType" label="处理类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="处理结果" min-width="200" show-overflow-tooltip />
        <el-table-column prop="processingTime" label="耗时(ms)" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">
              查看详情
            </el-button>
            <el-button v-if="!row.success" type="warning" size="small" @click="retryProcess(row)">
              重试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import type { AIOrderProcessResult } from '@/types/ai'

// 响应式数据
const loading = ref(false)
const historyList = ref<AIOrderProcessResult[]>([])
const searchForm = reactive({
  processType: '',
  status: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadHistory()
}

const handleReset = () => {
  Object.assign(searchForm, {
    processType: '',
    status: '',
    dateRange: []
  })
  pagination.current = 1
  loadHistory()
}

const loadHistory = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取历史记录
    // 模拟数据
    historyList.value = [
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
    pagination.total = 1
  } catch (error) {
    ElMessage.error('加载历史记录失败')
  } finally {
    loading.value = false
  }
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'PROCESSING': 'warning',
    'PENDING': 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'PROCESSING': '处理中',
    'PENDING': '等待中'
  }
  return textMap[status] || status
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const viewDetail = (row: AIOrderProcessResult) => {
  console.log('查看详情:', row)
}

const retryProcess = async (row: AIOrderProcessResult) => {
  try {
    // TODO: 调用重试API
    ElMessage.success('重试请求已发送')
  } catch (error) {
    ElMessage.error('重试失败')
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadHistory()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadHistory()
}

// 生命周期
onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.ai-history-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel-header {
  margin-bottom: 16px;
}

.panel-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 8px 0;
}

.panel-subtitle {
  font-size: 14px;
  color: var(--md-sys-color-on-surface-variant);
  margin: 0;
}

.search-section {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 12px;
  padding: 20px;
}

.search-form {
  margin: 0;
}

.table-section {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 12px;
  padding: 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
