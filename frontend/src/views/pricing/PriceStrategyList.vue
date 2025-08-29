<template>
  <div class="price-strategy-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">价格策略管理</h1>
      <p class="page-description">管理不同定价策略和动态价格调整规则</p>
    </div>

    <!-- 搜索和过滤区域 -->
    <div class="search-section">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="策略名称">
          <el-input
            v-model="searchForm.strategyName"
            placeholder="请输入策略名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="策略类型">
          <el-select
            v-model="searchForm.strategyType"
            placeholder="请选择策略类型"
            clearable
            style="width: 150px"
          >
            <el-option label="固定定价" value="FIXED" />
            <el-option label="百分比定价" value="PERCENTAGE" />
            <el-option label="分层定价" value="TIERED" />
            <el-option label="动态定价" value="DYNAMIC" />
            <el-option label="季节性定价" value="SEASONAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="基础价格类型">
          <el-select
            v-model="searchForm.basePriceType"
            placeholder="请选择基础价格类型"
            clearable
            style="width: 150px"
          >
            <el-option label="成本价" value="COST" />
            <el-option label="市场价" value="MARKET" />
            <el-option label="自定义价" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.isActive"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
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
        新建价格策略
      </el-button>
      <el-button type="success" @click="handleBatchActivate" :disabled="!hasSelection">
        <el-icon><Check /></el-icon>
        批量启用
      </el-button>
      <el-button type="warning" @click="handleBatchDeactivate" :disabled="!hasSelection">
        <el-icon><Close /></el-icon>
        批量禁用
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
        <el-table-column prop="strategyName" label="策略名称" min-width="150" />
        <el-table-column prop="strategyType" label="策略类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getStrategyTypeType(row.strategyType)">
              {{ getStrategyTypeLabel(row.strategyType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="basePriceType" label="基础价格类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBasePriceTypeType(row.basePriceType)">
              {{ getBasePriceTypeLabel(row.basePriceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="basePriceMultiplier" label="价格倍数" width="100">
          <template #default="{ row }">
            <span>{{ row.basePriceMultiplier }}x</span>
          </template>
        </el-table-column>
        <el-table-column prop="minMarginRate" label="最小利润率" width="100">
          <template #default="{ row }">
            <span v-if="row.minMarginRate">{{ (row.minMarginRate * 100).toFixed(2) }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxMarginRate" label="最大利润率" width="100">
          <template #default="{ row }">
            <span v-if="row.maxMarginRate">{{ (row.maxMarginRate * 100).toFixed(2) }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveFrom" label="生效时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.effectiveFrom) }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              :type="row.isActive ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.isActive ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
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
    <PriceStrategyDialog
      v-model="dialogVisible"
      :price-strategy="currentPriceStrategy"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Check, Close, Delete } from '@element-plus/icons-vue'
import PriceStrategyDialog from './components/PriceStrategyDialog.vue'
import { priceStrategyApi } from '@/api/priceStrategy'

// 搜索表单
const searchForm = reactive({
  strategyName: '',
  strategyType: '',
  basePriceType: '',
  isActive: null as boolean | null
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
const currentPriceStrategy = ref(null)

// 计算属性
const hasSelection = computed(() => selectedRows.value.length > 0)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const response = await priceStrategyApi.getPriceStrategyPage({
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
    strategyName: '',
    strategyType: '',
    basePriceType: '',
    isActive: null
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
  currentPriceStrategy.value = null
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  currentPriceStrategy.value = { ...row }
  dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  try {
    await priceStrategyApi.togglePriceStrategyStatus(row.id, !row.isActive)
    row.isActive = !row.isActive
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个价格策略吗？', '提示', {
      type: 'warning'
    })
    await priceStrategyApi.deletePriceStrategy(row.id)
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
    await priceStrategyApi.batchTogglePriceStrategyStatus(ids, true)
    ElMessage.success('批量启用成功')
    fetchData()
  } catch (error) {
    ElMessage.error('批量启用失败')
  }
}

const handleBatchDeactivate = async () => {
  try {
    const ids = selectedRows.value.map(row => row.id)
    await priceStrategyApi.batchTogglePriceStrategyStatus(ids, false)
    ElMessage.success('批量禁用成功')
    fetchData()
  } catch (error) {
    ElMessage.error('批量禁用失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个价格策略吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await priceStrategyApi.batchDeletePriceStrategies(ids)
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
const getStrategyTypeType = (type: string) => {
  const types: Record<string, string> = {
    'FIXED': 'success',
    'PERCENTAGE': 'warning',
    'TIERED': 'info',
    'DYNAMIC': 'danger',
    'SEASONAL': 'primary'
  }
  return types[type] || 'info'
}

const getStrategyTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'FIXED': '固定定价',
    'PERCENTAGE': '百分比定价',
    'TIERED': '分层定价',
    'DYNAMIC': '动态定价',
    'SEASONAL': '季节性定价'
  }
  return labels[type] || type
}

const getBasePriceTypeType = (type: string) => {
  const types: Record<string, string> = {
    'COST': 'success',
    'MARKET': 'warning',
    'CUSTOM': 'info'
  }
  return types[type] || 'info'
}

const getBasePriceTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'COST': '成本价',
    'MARKET': '市场价',
    'CUSTOM': '自定义价'
  }
  return labels[type] || type
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
.price-strategy-list {
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
</style>
