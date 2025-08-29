<template>
  <div class="sales-target-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">销售目标管理</h1>
      <p class="page-description">管理销售目标和进度跟踪，实现精准的业绩管理</p>
    </div>

    <!-- 搜索和过滤区域 -->
    <div class="search-section">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="目标名称">
          <el-input
            v-model="searchForm.targetName"
            placeholder="请输入目标名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select
            v-model="searchForm.targetType"
            placeholder="请选择目标类型"
            clearable
            style="width: 150px"
          >
            <el-option label="GMV目标" value="GMV" />
            <el-option label="收入目标" value="REVENUE" />
            <el-option label="订单数量" value="ORDER_COUNT" />
            <el-option label="客户数量" value="CUSTOMER_COUNT" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标周期">
          <el-select
            v-model="searchForm.targetPeriod"
            placeholder="请选择目标周期"
            clearable
            style="width: 150px"
          >
            <el-option label="Q1" value="Q1" />
            <el-option label="Q2" value="Q2" />
            <el-option label="Q3" value="Q3" />
            <el-option label="Q4" value="Q4" />
            <el-option label="H1" value="H1" />
            <el-option label="H2" value="H2" />
            <el-option label="年度" value="YEARLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="草稿" value="DRAFT" />
            <el-option label="激活" value="ACTIVE" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
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

    <!-- 操作按钮区域 -->
    <div class="action-section">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建销售目标
      </el-button>
      <el-button type="success" @click="handleBatchActivate" :disabled="!hasSelection">
        <el-icon><Check /></el-icon>
        批量激活
      </el-button>
      <el-button type="warning" @click="handleBatchCancel" :disabled="!hasSelection">
        <el-icon><Close /></el-icon>
        批量取消
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="!hasSelection">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        border
        stripe
        style="width: 100%"
        :height="400"
        :max-height="600"
        :row-key="(row: any) => row.id"
        :default-sort="{ prop: 'createdAt', order: 'descending' }"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="targetName" label="目标名称" min-width="150" />
        <el-table-column prop="targetType" label="目标类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTargetTypeType(row.targetType)">
              {{ getTargetTypeLabel(row.targetType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetPeriod" label="目标周期" width="100">
          <template #default="{ row }">
            <el-tag :type="getTargetPeriodType(row.targetPeriod)">
              {{ row.targetPeriod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetYear" label="目标年份" width="100" />
        <el-table-column prop="gmvTarget" label="GMV目标" width="120">
          <template #default="{ row }">
            <span v-if="row.gmvTarget">¥{{ formatNumber(row.gmvTarget) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="revenueTarget" label="收入目标" width="120">
          <template #default="{ row }">
            <span v-if="row.revenueTarget">¥{{ formatNumber(row.revenueTarget) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="progressPercentage" label="进度" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="row.progressPercentage || 0"
              :status="getProgressStatus(row.progressPercentage)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              v-if="row.status === 'DRAFT'"
              type="success"
              size="small"
              @click="handleActivate(row)"
            >
              激活
            </el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              type="warning"
              size="small"
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button
              v-if="['DRAFT', 'ACTIVE'].includes(row.status)"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button type="info" size="small" @click="handleViewProgress(row)">
              进度
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新建/编辑对话框 -->
    <SalesTargetDialog
      v-model="dialogVisible"
      :sales-target="currentSalesTarget"
      @success="handleDialogSuccess"
    />

    <!-- 进度查看对话框 -->
    <SalesTargetProgressDialog
      v-model="progressDialogVisible"
      :sales-target="currentSalesTarget"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Check, Close, Delete } from '@element-plus/icons-vue'
import SalesTargetDialog from './components/SalesTargetDialog.vue'
import SalesTargetProgressDialog from './components/SalesTargetProgressDialog.vue'
import { salesTargetApi } from '@/api/salesTarget'

// 搜索表单
const searchForm = reactive({
  targetName: '',
  targetType: '',
  targetPeriod: '',
  status: ''
})

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)
const selectedRows = ref<any[]>([])

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 性能优化：防抖搜索
const searchDebounce = ref<NodeJS.Timeout | null>(null)
const searchDelay = 300 // 搜索延迟时间

// 对话框
const dialogVisible = ref(false)
const progressDialogVisible = ref(false)
const currentSalesTarget = ref(null)

// 计算属性
const hasSelection = computed(() => selectedRows.value.length > 0)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const response = await salesTargetApi.getSalesTargetPage({
      ...searchForm,
      page: pagination.current,
      size: pagination.size
    })
    tableData.value = response.records
    pagination.total = response.total
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 性能优化：防抖搜索
const handleSearchDebounced = () => {
  if (searchDebounce.value) {
    clearTimeout(searchDebounce.value)
  }
  searchDebounce.value = setTimeout(() => {
    handleSearch()
  }, searchDelay)
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    targetName: '',
    targetType: '',
    targetPeriod: '',
    status: ''
  })
  pagination.current = 1
  fetchData()
}

// 选择变化
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 新建
const handleCreate = () => {
  currentSalesTarget.value = null
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  currentSalesTarget.value = { ...row }
  dialogVisible.value = true
}

// 激活
const handleActivate = async (row: any) => {
  try {
    await salesTargetApi.activateSalesTarget(row.id)
    row.status = 'ACTIVE'
    ElMessage.success('激活成功')
  } catch (error) {
    ElMessage.error('激活失败')
  }
}

// 完成
const handleComplete = async (row: any) => {
  try {
    await salesTargetApi.completeSalesTarget(row.id)
    row.status = 'COMPLETED'
    ElMessage.success('完成成功')
  } catch (error) {
    ElMessage.error('完成失败')
  }
}

// 取消
const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消这个销售目标吗？', '提示', {
      type: 'warning'
    })
    await salesTargetApi.cancelSalesTarget(row.id)
    row.status = 'CANCELLED'
    ElMessage.success('取消成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

// 查看进度
const handleViewProgress = (row: any) => {
  currentSalesTarget.value = { ...row }
  progressDialogVisible.value = true
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个销售目标吗？', '提示', {
      type: 'warning'
    })
    await salesTargetApi.deleteSalesTarget(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量操作
const handleBatchActivate = async () => {
  try {
    const ids = selectedRows.value.map(row => row.id)
    await salesTargetApi.batchActivateSalesTargets(ids)
    ElMessage.success('批量激活成功')
    fetchData()
  } catch (error) {
    ElMessage.error('批量激活失败')
  }
}

const handleBatchCancel = async () => {
  try {
    const ids = selectedRows.value.map(row => row.id)
    await salesTargetApi.batchCancelSalesTargets(ids)
    ElMessage.success('批量取消成功')
    fetchData()
  } catch (error) {
    ElMessage.error('批量取消失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个销售目标吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await salesTargetApi.batchDeleteSalesTargets(ids)
    ElMessage.success('批量删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 分页
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchData()
}

// 对话框成功回调
const handleDialogSuccess = () => {
  dialogVisible.value = false
  fetchData()
}

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

const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.sales-target-list {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-section {
  margin-bottom: 16px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-form {
  margin: 0;
}

.action-section {
  margin-bottom: 16px;
  padding: 16px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.pagination-section {
  padding: 20px;
  text-align: right;
  background: white;
}

:deep(.el-table) {
  border-radius: 0;
}

:deep(.el-table th) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-button + .el-button) {
  margin-left: 8px;
}

:deep(.el-progress) {
  margin: 0;
}
</style>
