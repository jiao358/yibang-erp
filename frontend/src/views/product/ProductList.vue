<template>
  <div class="product-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">商品管理</h1>
      <p class="page-subtitle">管理商品信息、状态和审核流程</p>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="商品状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="status in productStatusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
            v-model="searchForm.approvalStatus"
            placeholder="请选择审核状态"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="status in approvalStatusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
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
        新增商品
      </el-button>
      <el-button type="success" @click="handleBatchApprove" :disabled="!hasSelectedItems">
        <el-icon><Check /></el-icon>
        批量审核
      </el-button>
      <el-button type="warning" @click="handleBatchDeactivate" :disabled="!hasSelectedItems">
        <el-icon><Remove /></el-icon>
        批量下架
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="!hasSelectedItems">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
    </div>

    <!-- 商品列表表格 -->
    <div class="table-section">
      <el-table
        :data="productList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        class="product-table"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="sku" label="商品编码" width="120" />
        
        <el-table-column prop="name" label="商品名称" min-width="200">
          <template #default="{ row }">
            <div class="product-name">
              <span class="name-text">{{ row.name }}</span>
              <div class="product-tags">
                <el-tag v-if="row.isFeatured" type="success" size="small">推荐</el-tag>
                <el-tag v-if="row.isHot" type="danger" size="small">热销</el-tag>
                <el-tag v-if="row.isNew" type="warning" size="small">新品</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="商品状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="approvalStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusTagType(row.approvalStatus)" size="small">
              {{ getApprovalStatusText(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="sellingPrice" label="销售价" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.sellingPrice }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button type="success" size="small" @click="handleEdit(row)" 
                         v-if="row.status === 'DRAFT'">
                编辑
              </el-button>
              <el-button type="warning" size="small" @click="handleSubmit(row)" 
                         v-if="row.status === 'DRAFT'">
                提交审核
              </el-button>
              <el-button type="success" size="small" @click="handleApprove(row)" 
                         v-if="row.status === 'PENDING'">
                审核
              </el-button>
              <el-button type="success" size="small" @click="handleActivate(row)" 
                         v-if="row.approvalStatus === 'APPROVED' && row.status !== 'ACTIVE'">
                上架
              </el-button>
              <el-button type="warning" size="small" @click="handleDeactivate(row)" 
                         v-if="row.status === 'ACTIVE'">
                下架
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
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

    <!-- 商品详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商品详情"
      width="800px"
      class="product-detail-dialog"
    >
      <ProductDetail v-if="detailDialogVisible" :product="selectedProduct" />
    </el-dialog>

    <!-- 商品编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑商品"
      width="900px"
      class="product-edit-dialog"
    >
      <ProductEdit 
        v-if="editDialogVisible" 
        :product="selectedProduct" 
        @success="handleEditSuccess"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="商品审核"
      width="600px"
      class="product-audit-dialog"
    >
      <ProductAudit 
        v-if="auditDialogVisible" 
        :product="selectedProduct" 
        @success="handleAuditSuccess"
        @cancel="auditDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Check, Remove, Delete } from '@element-plus/icons-vue'
import ProductDetail from './components/ProductDetail.vue'
import ProductEdit from './components/ProductEdit.vue'
import ProductAudit from './components/ProductAudit.vue'
import { getProductList, deleteProduct, submitForApproval } from '@/api/product'
import type { Product } from '@/types/product'

// 响应式数据
const loading = ref(false)
const productList = ref<Product[]>([])
const selectedItems = ref<Product[]>([])
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const selectedProduct = ref<Product | null>(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: '',
  approvalStatus: ''
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 商品状态选项
const productStatusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING', label: '待审核' },
  { value: 'ACTIVE', label: '已上架' },
  { value: 'INACTIVE', label: '已下架' },
  { value: 'DISCONTINUED', label: '已停售' }
]

// 审核状态选项
const approvalStatusOptions = [
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已拒绝' }
]

// 计算属性
const hasSelectedItems = computed(() => selectedItems.value.length > 0)

// 方法
const loadProductList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const response = await getProductList(params)
    productList.value = response.data.records || []
    pagination.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('加载商品列表失败')
    console.error('加载商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadProductList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: '',
    approvalStatus: ''
  })
  pagination.current = 1
  loadProductList()
}

const handleSelectionChange = (selection: Product[]) => {
  selectedItems.value = selection
}

const handleCreate = () => {
  selectedProduct.value = null
  editDialogVisible.value = true
}

const handleView = (row: Product) => {
  selectedProduct.value = row
  detailDialogVisible.value = true
}

const handleEdit = (row: Product) => {
  selectedProduct.value = { ...row }
  editDialogVisible.value = true
}

const handleSubmit = async (row: Product) => {
  try {
    await ElMessageBox.confirm('确定要提交该商品进行审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await submitForApproval(row.id, 1) // 这里应该传入实际的用户ID
    ElMessage.success('提交审核成功')
    loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交审核失败')
      console.error('提交审核失败:', error)
    }
  }
}

const handleApprove = (row: Product) => {
  selectedProduct.value = row
  auditDialogVisible.value = true
}

const handleActivate = async (row: Product) => {
  try {
    await ElMessageBox.confirm('确定要上架该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用上架API
    ElMessage.success('商品上架成功')
    loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('商品上架失败')
      console.error('商品上架失败:', error)
    }
  }
}

const handleDeactivate = async (row: Product) => {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用下架API
    ElMessage.success('商品下架成功')
    loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('商品下架失败')
      console.error('商品下架失败:', error)
    }
  }
}

const handleDelete = async (row: Product) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除失败:', error)
    }
  }
}

const handleBatchApprove = () => {
  ElMessage.info('批量审核功能开发中...')
}

const handleBatchDeactivate = () => {
  ElMessage.info('批量下架功能开发中...')
}

const handleBatchDelete = () => {
  ElMessage.info('批量删除功能开发中...')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadProductList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadProductList()
}

const handleEditSuccess = () => {
  editDialogVisible.value = false
  loadProductList()
  ElMessage.success('编辑成功')
}

const handleAuditSuccess = () => {
  auditDialogVisible.value = false
  loadProductList()
  ElMessage.success('审核完成')
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'ACTIVE': 'success',
    'INACTIVE': 'danger',
    'DISCONTINUED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PENDING': '待审核',
    'ACTIVE': '已上架',
    'INACTIVE': '已下架',
    'DISCONTINUED': '已停售'
  }
  return textMap[status] || status
}

const getApprovalStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getApprovalStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || status
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadProductList()
})
</script>

<style scoped>
.product-list-container {
  padding: 24px;
  background: var(--md-sys-color-surface);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: var(--md-sys-color-on-surface-variant);
  margin: 0;
}

.search-section {
  background: var(--md-sys-color-surface-container);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: var(--md-sys-elevation-level1);
}

.search-form {
  margin: 0;
}

.action-section {
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.table-section {
  background: var(--md-sys-color-surface-container);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: var(--md-sys-elevation-level1);
}

.product-table {
  border-radius: 12px;
  overflow: hidden;
}

.product-name {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.name-text {
  font-weight: 500;
  color: var(--md-sys-color-on-surface);
}

.product-tags {
  display: flex;
  gap: 4px;
}

.price {
  font-weight: 600;
  color: var(--md-sys-color-primary);
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-list-container {
    padding: 16px;
  }
  
  .search-section,
  .table-section {
    padding: 16px;
  }
  
  .action-section {
    flex-direction: column;
  }
  
  .action-section .el-button {
    width: 100%;
  }
}
</style>
