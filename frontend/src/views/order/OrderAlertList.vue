<template>
  <div class="order-alert-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>订单预警</h1>
        <p class="page-description">管理需要人工处理的订单预警</p>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <el-card class="search-card">
        <el-form :model="searchForm" :inline="true" class="search-form" >
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 120px;" clearable>
              <el-option
                v-for="item in STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="处理类型">
            <el-select v-model="searchForm.processingType"  style="width: 120px;"  placeholder="请选择处理类型" clearable>
              <el-option
                v-for="item in PROCESSING_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="订单编号">
            <el-input
              v-model="searchForm.orderNumber"
              placeholder="请输入订单编号"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="源订单ID">
            <el-input
              v-model="searchForm.sourceOrderId"
              placeholder="请输入源订单ID"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="dateRange"
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
      </el-card>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>预警列表</span>
            <div class="header-actions">
              <el-button @click="handleRefresh">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </template>

        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          style="width: 100%"
          @sort-change="handleSortChange"
        >
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column prop="sourceOrderId" label="源订单ID" width="150" show-overflow-tooltip />
          
          <el-table-column prop="processingTypeDesc" label="处理类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getProcessingTypeTagType(row.processingType)">
                {{ row.processingTypeDesc }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="statusDesc" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ row.statusDesc }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="priorityDesc" label="优先级" width="100">
            <template #default="{ row }">
              <el-tag :type="getPriorityTagType(row.priority)">
                {{ row.priorityDesc }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="processingReason" label="处理原因" min-width="350" show-overflow-tooltip />
          
          <!-- <el-table-column prop="assignedToName" label="分配给" width="120" /> -->
          
          <el-table-column prop="processedByName" label="处理人" width="120" />
          
          <el-table-column prop="createdByName" label="创建人" width="120" />
          
          <el-table-column prop="createdAt" label="创建时间" width="180" sortable="custom">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="processedAt" label="处理时间" width="180">
            <template #default="{ row }">
              {{ row.processedAt ? formatDateTime(row.processedAt) : '-' }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <!-- <el-button
                v-if="row.status === ALERT_STATUS.PENDING"
                type="primary"
                size="small"
                @click="handleAssign(row)"
              >
                分配
              </el-button>
               -->
              <el-button
                v-if="row.status === ALERT_STATUS.PENDING || row.status === ALERT_STATUS.PROCESSING"
                type="success"
                size="small"
                @click="handleProcess(row)"
              >
                处理
              </el-button>
              
              <el-button
                v-if="row.status === ALERT_STATUS.PROCESSING"
                type="warning"
                size="small"
                @click="handleComplete(row)"
              >
                完成
              </el-button>
              
              <el-button
                v-if="row.status === ALERT_STATUS.PENDING || row.status === ALERT_STATUS.PROCESSING"
                type="danger"
                size="small"
                @click="handleReject(row)"
              >
                拒绝
              </el-button>
              
              <el-button
                type="info"
                size="small"
                @click="handleViewDetail(row)"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 分配对话框 -->
    <AssignDialog
      v-model="assignDialogVisible"
      :alert-id="selectedAlertId"
      @success="handleAssignSuccess"
    />

    <!-- 处理对话框 -->
    <ProcessDialog
      v-model="processDialogVisible"
      :alert="selectedAlert"
      :action-type="processActionType"
      @success="handleProcessSuccess"
    />

    <!-- 详情对话框 -->
    <DetailDialog
      v-model="detailDialogVisible"
      :alert="selectedAlert"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { orderAlertApi } from '@/api/orderAlert'
import type { OrderAlertResponse, OrderAlertRequest } from '@/types/orderAlert'
import {
  STATUS_OPTIONS,
  PROCESSING_TYPE_OPTIONS,
  ALERT_STATUS,
  PROCESSING_TYPE,
  PRIORITY
} from '@/types/orderAlert'
import AssignDialog from './components/AssignDialog.vue'
import ProcessDialog from './components/ProcessDialog.vue'
import DetailDialog from './components/DetailDialog.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<OrderAlertResponse[]>([])
const dateRange = ref<[string, string] | null>(null)

// 搜索表单
const searchForm = reactive<OrderAlertRequest>({
  current: 1,
  size: 20,
  status: '',
  processingType: '',
  orderNumber: '',
  sourceOrderId: '',
  sortField: 'created_at',
  sortOrder: 'desc'
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 对话框状态
const assignDialogVisible = ref(false)
const processDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedAlertId = ref<number | null>(null)
const selectedAlert = ref<OrderAlertResponse | null>(null)
const processActionType = ref<'process' | 'complete' | 'reject'>('process')

// 计算属性
const searchParams = computed(() => {
  const params = { ...searchForm }
  if (dateRange.value) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  return params
})

// 方法
const loadData = async () => {
  try {
    loading.value = true
    const params = {
      ...searchParams.value,
      current: pagination.current,
      size: pagination.size
    }
    
    const response = await orderAlertApi.getOrderAlerts(params)
    
    if (response.success) {
      tableData.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.data.error || '获取数据失败')
    }
  } catch (error) {
    console.error('获取预警列表失败:', error)
    ElMessage.error('获取预警列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    current: 1,
    size: 20,
    status: '',
    processingType: '',
    orderNumber: '',
    sourceOrderId: '',
    sortField: 'created_at',
    sortOrder: 'desc'
  })
  dateRange.value = null
  pagination.current = 1
  loadData()
}

const handleRefresh = () => {
  loadData()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadData()
}

const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  searchForm.sortField = prop
  searchForm.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  loadData()
}

const handleAssign = (row: OrderAlertResponse) => {
  selectedAlertId.value = row.id
  assignDialogVisible.value = true
}

const handleProcess = (row: OrderAlertResponse) => {
  selectedAlert.value = row
  processActionType.value = 'process'
  processDialogVisible.value = true
}

const handleComplete = (row: OrderAlertResponse) => {
  selectedAlert.value = row
  processActionType.value = 'complete'
  processDialogVisible.value = true
}

const handleReject = (row: OrderAlertResponse) => {
  selectedAlert.value = row
  processActionType.value = 'reject'
  processDialogVisible.value = true
}

const handleViewDetail = (row: OrderAlertResponse) => {
  selectedAlert.value = row
  detailDialogVisible.value = true
}

const handleAssignSuccess = () => {
  loadData()
}

const handleProcessSuccess = () => {
  loadData()
}

// 工具方法
const getProcessingTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    [PROCESSING_TYPE.ORDER_CLOSE]: 'danger',
    [PROCESSING_TYPE.ADDRESS_CHANGE]: 'warning',
    [PROCESSING_TYPE.REFUND]: 'info',
    [PROCESSING_TYPE.CANCEL]: 'danger'
  }
  return typeMap[type] || ''
}

const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    [ALERT_STATUS.PENDING]: 'warning',
    [ALERT_STATUS.PROCESSING]: 'primary',
    [ALERT_STATUS.COMPLETED]: 'success',
    [ALERT_STATUS.REJECTED]: 'danger'
  }
  return statusMap[status] || ''
}

const getPriorityTagType = (priority: string) => {
  const priorityMap: Record<string, string> = {
    [PRIORITY.LOW]: 'info',
    [PRIORITY.NORMAL]: '',
    [PRIORITY.HIGH]: 'warning',
    [PRIORITY.URGENT]: 'danger'
  }
  return priorityMap[priority] || ''
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-alert-list {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.search-section {
  margin-bottom: 20px;
}

.search-card {
  border-radius: 8px;
}

.search-form {
  margin-bottom: 0;
}

.table-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
