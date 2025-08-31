<template>
  <div class="price-tier-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">价格分层管理</h1>
      <p class="page-description">配置供应链公司的客户价格分层体系，支持不同客户等级的价格策略</p>
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
        :default-sort="{ prop: 'priority', order: 'ascending' }"
      >
        <el-table-column type="selection" width="55" />
        
        <!-- 所属公司名称列 - 只有管理员可见 -->
        <el-table-column 
          v-if="isAdmin" 
          prop="companyName" 
          label="所属公司" 
          width="350" 
          show-overflow-tooltip
        />
        
        <el-table-column prop="tierName" label="分层名称" width="150" sortable />
        
        <el-table-column prop="tierCode" label="分层代码" width="120" />
        
        <el-table-column prop="tierType" label="分层类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTierTypeTagType(row.tierType)" size="small">
              {{ getTierTypeLabel(row.tierType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="优先级" width="80" sortable>
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)" size="small">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="描述" width="200" show-overflow-tooltip />
        
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
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

// 搜索表单 - 已移除搜索框，保留空对象用于API调用
const searchForm = reactive({})

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



// 对话框
const dialogVisible = ref(false)
const currentPriceTier = ref(null)

// 计算属性
const hasSelection = computed(() => selectedRows.value.length > 0)

// 判断是否为管理员
const isAdmin = computed(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
    const user = JSON.parse(userInfo)
    const userRoles = localStorage.getItem('userRoles')
    if (userRoles) {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN') || roles.includes('SUPPLIER_ADMIN')
    }
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  }
  return false
})

// 获取用户所属公司ID
const getUserCompanyId = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.companyId || 1 // 如果没有公司ID，默认使用1
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return 1
    }
  }
  return 1
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const response = await priceTierApi.getPriceTierPage({
      ...searchForm,
      page: pagination.current,
      size: pagination.size,
      companyId: getUserCompanyId() // 从用户信息获取公司ID
    })
    
    if (response && response.records) {
      tableData.value = response.records
      pagination.total = response.total
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取价格分层数据失败:', error)
    ElMessage.error('获取数据失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索 - 已移除搜索框，直接刷新数据
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置 - 已移除搜索框，直接刷新数据
const handleReset = () => {
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
    ElMessage.success(`${row.isActive ? '禁用' : '启用'}成功`)
    fetchData()
  } catch (error) {
    ElMessage.error(`${row.isActive ? '禁用' : '启用'}失败`)
  }
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除价格分层"${row.tierName}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await priceTierApi.deletePriceTier(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量启用
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

// 批量禁用
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

// 批量删除
const handleBatchDelete = async () => {
  try {
    const names = selectedRows.value.map(row => row.tierName).join('、')
    await ElMessageBox.confirm(
      `确定要删除以下价格分层吗？\n${names}`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
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

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchData()
}

// 当前页变化
const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchData()
}

// 对话框成功回调
const handleDialogSuccess = () => {
  dialogVisible.value = false
  fetchData()
}

// 获取分层类型标签类型
const getTierTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    'DEALER_LEVEL_1': 'danger',
    'DEALER_LEVEL_2': 'warning',
    'DEALER_LEVEL_3': 'success',
    'VIP_CUSTOMER': 'info',
    'WHOLESALE': 'primary',
    'RETAIL': 'default'
  }
  return types[type] || 'default'
}

// 获取分层类型标签
const getTierTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'DEALER_LEVEL_1': '1级经销商',
    'DEALER_LEVEL_2': '2级经销商',
    'DEALER_LEVEL_3': '3级经销商',
    'VIP_CUSTOMER': 'VIP客户',
    'WHOLESALE': '批发客户',
    'RETAIL': '零售客户'
  }
  return labels[type] || type
}

// 获取优先级标签类型
const getPriorityTagType = (priority: number) => {
  if (priority <= 1) return 'danger'
  if (priority <= 3) return 'warning'
  if (priority <= 5) return 'success'
  return 'info'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
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
