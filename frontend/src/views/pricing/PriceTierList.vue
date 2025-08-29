<template>
  <div class="price-tier-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">价格分层管理</h1>
      <p class="page-description">管理不同客户等级的价格分层和折扣策略</p>
    </div>

    <!-- 搜索和过滤区域 -->
    <div class="search-section">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="分层名称">
          <el-input
            v-model="searchForm.tierName"
            placeholder="请输入分层名称"
            clearable
            style="width: 200px"
            @input="handleSearchDebounced"
          />
        </el-form-item>
        <el-form-item label="分层代码">
          <el-input
            v-model="searchForm.tierCode"
            placeholder="请输入分层代码"
            clearable
            style="width: 200px"
            @input="handleSearchDebounced"
          />
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select
            v-model="searchForm.customerLevel"
            placeholder="请选择客户等级"
            clearable
            style="width: 150px"
          >
            <el-option label="钻石级" value="PREMIUM" />
            <el-option label="黄金级" value="VIP" />
            <el-option label="白银级" value="REGULAR" />
            <el-option label="标准级" value="ALL" />
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
        新建价格分层
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
        <el-table-column prop="tierName" label="分层名称" min-width="120" />
        <el-table-column prop="tierCode" label="分层代码" width="120" />
        <el-table-column prop="tierLevel" label="分层级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getTierLevelType(row.tierLevel)">
              {{ getTierLevelLabel(row.tierLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣率" width="100">
          <template #default="{ row }">
            <span>{{ (row.discountRate * 100).toFixed(2) }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="markupRate" label="加价率" width="100">
          <template #default="{ row }">
            <span>{{ (row.markupRate * 100).toFixed(2) }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="customerLevelRequirement" label="客户等级要求" width="120">
          <template #default="{ row }">
            <el-tag :type="getCustomerLevelType(row.customerLevelRequirement)">
              {{ getCustomerLevelLabel(row.customerLevelRequirement) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
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
    <PriceTierDialog
      v-model="dialogVisible"
      :price-tier="currentPriceTier"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Check, Close, Delete } from '@element-plus/icons-vue'
import PriceTierDialog from './components/PriceTierDialog.vue'
import { priceTierApi } from '@/api/priceTier'
import { measureAsync } from '@/utils/performance'

// 搜索表单
const searchForm = reactive({
  tierName: '',
  tierCode: '',
  customerLevel: '',
  isActive: undefined as boolean | undefined,
  companyId: 1 // 添加必需的companyId字段
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

// 性能优化：虚拟滚动配置
const virtualScrollConfig = {
  itemHeight: 60, // 每行高度
  bufferSize: 10, // 缓冲区大小
  throttleDelay: 100 // 节流延迟
}

// 性能优化：防抖搜索
const searchDebounce = ref<NodeJS.Timeout | null>(null)
const searchDelay = 300 // 搜索延迟时间

// 对话框
const dialogVisible = ref(false)
const currentPriceTier = ref(null)

// 计算属性
const hasSelection = computed(() => selectedRows.value.length > 0)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const response = await measureAsync('fetchPriceTierData', async () => {
      return await priceTierApi.getPriceTierPage({
        ...searchForm,
        page: pagination.current,
        size: pagination.size
      })
    }, {
      page: pagination.current,
      size: pagination.size,
      filters: searchForm
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
    tierName: '',
    tierCode: '',
    customerLevel: '',
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
  currentPriceTier.value = null
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  currentPriceTier.value = { ...row }
  dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  try {
    await priceTierApi.togglePriceTierStatus(row.id, !row.isActive)
    row.isActive = !row.isActive
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个价格分层吗？', '提示', {
      type: 'warning'
    })
    await priceTierApi.deletePriceTier(row.id)
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
    await priceTierApi.batchTogglePriceTierStatus(ids, true)
    ElMessage.success('批量启用成功')
    fetchData()
  } catch (error) {
    ElMessage.error('批量启用失败')
  }
}

const handleBatchDeactivate = async () => {
  try {
    const ids = selectedRows.value.map(row => row.id)
    await priceTierApi.batchTogglePriceTierStatus(ids, false)
    ElMessage.success('批量禁用成功')
    fetchData()
  } catch (error) {
    ElMessage.error('批量禁用失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个价格分层吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    // 批量删除 - 逐个调用删除API
    for (const id of ids) {
      await priceTierApi.deletePriceTier(id)
    }
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
const getTierLevelType = (level: number) => {
  const types = ['danger', 'warning', 'success', 'info']
  return types[Math.min(level - 1, types.length - 1)]
}

const getTierLevelLabel = (level: number) => {
  const labels = ['钻石级', '黄金级', '白银级', '标准级']
  return labels[Math.min(level - 1, labels.length - 1)]
}

const getCustomerLevelType = (level: string) => {
  const types: Record<string, string> = {
    'PREMIUM': 'danger',
    'VIP': 'warning',
    'REGULAR': 'success',
    'ALL': 'info'
  }
  return types[level] || 'info'
}

const getCustomerLevelLabel = (level: string) => {
  const labels: Record<string, string> = {
    'PREMIUM': '钻石级',
    'VIP': '黄金级',
    'REGULAR': '白银级',
    'ALL': '标准级'
  }
  return labels[level] || level
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
.price-tier-list {
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
