<template>
  <div class="product-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">供应链商品管理</h1>
      <p class="page-subtitle">管理供应链公司的商品信息、状态和库存</p>
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
        <el-form-item label="品牌">
          <el-select
            v-model="searchForm.brand"
            placeholder="请选择品牌"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="brand in brandOptions"
              :key="brand.id"
              :label="brand.name"
              :value="brand.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="category in categoryOptions"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格范围">
          <el-input
            v-model="searchForm.minPrice"
            placeholder="最低价"
            clearable
            style="width: 120px"
          />
          <span style="margin: 0 8px;">-</span>
          <el-input
            v-model="searchForm.maxPrice"
            placeholder="最高价"
            clearable
            style="width: 120px"
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><i class="el-icon-search"></i></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><i class="el-icon-refresh"></i></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="action-section">
      <!-- <el-button type="primary" @click="handleCreate">
        <el-icon><i class="el-icon-plus"></i></el-icon>
        新增商品
      </el-button> -->
      <el-button type="primary" @click="handleCreatePlus">
        <el-icon><i class="el-icon-plus"></i></el-icon>
        新增商品
      </el-button>
      <el-button type="success" @click="handleBatchActivate" :disabled="!hasSelectedItems">
        <el-icon><i class="el-icon-check"></i></el-icon>
        批量上架
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
        
        <el-table-column prop="name" label="商品名称" width="200">
          <template #default="{ row }">
            <div class="product-name">
              <el-tooltip 
                :content="row.name" 
                placement="top" 
                :show-after="500"
                :hide-after="0"
              >
                <span class="name-text">{{ row.name.length > 20 ? row.name.substring(0, 20) + '...' : row.name }}</span>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="images" label="商品图片" width="100">
          <template #default="{ row }">
            <div class="product-image">
              <el-image
                v-if="row.images"
                :src="row.images"
                style="width: 60px; height: 60px"
                fit="cover"
                :preview-src-list="[row.images]"
                :preview-teleported="true"
                :z-index="999999"
                :hide-on-click-modal="true"
              />
              <span v-else class="no-image">无图片</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="sku" label="69码" width="120">
          <template #default="{ row }">
            <span>{{ row.sku || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="unit" label="规格单位" width="100">
          <template #default="{ row }">
            <span>{{ row.unit || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="costPrice" label="成本价" width="100">
          <template #default="{ row }">
            <span>¥{{ row.costPrice || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="sellingPrice" label="销售价" width="100">
          <template #default="{ row }">
            <span>¥{{ row.sellingPrice || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="商品状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button type="success" size="small" @click="handleEdit(row)" 
                         v-if="row.status === 'DRAFT' || row.status === 'INACTIVE'">
                编辑
              </el-button>
              <el-button type="success" size="small" @click="handleActivate(row)" 
                         v-if="row.status === 'DRAFT' || row.status === 'INACTIVE'">
                上架
              </el-button>
              <el-button type="warning" size="small" @click="handleDeactivate(row)" 
                         v-if="row.status === 'ACTIVE'">
                下架
              </el-button>
              <el-button type="info" size="small" @click="handleUploadImage(row)">
                上传图片
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
    <ProductDetail 
      v-if="detailDialogVisible" 
      :visible="detailDialogVisible"
      :product="selectedProduct" 
      @update:visible="detailDialogVisible = $event"
    />

    <!-- 商品编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑商品"
      width="900px"
      class="product-edit-dialog"
      append-to-body
      :z-index="9999"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <ProductEdit 
        v-if="editDialogVisible" 
        :product="selectedProduct"
        :visible="editDialogVisible"
        @success="handleEditSuccess"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>

    <!-- 商品Plus表单对话框 -->
    <ProductFormPlus
      v-model:visible="formPlusVisible"
      :product="selectedProduct"
      @success="handleFormPlusSuccess"
    />

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="商品审核"
      width="600px"
      class="product-audit-dialog"
      append-to-body
      :z-index="9999"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <ProductAudit 
        v-if="auditDialogVisible" 
        :product="selectedProduct" 
        @success="handleAuditSuccess"
        @cancel="auditDialogVisible = false"
      />
    </el-dialog>

    <!-- 上传图片对话框 -->
    <el-dialog
      v-model="uploadImageDialogVisible"
      title="商品图片管理"
      width="800px"
      class="upload-image-dialog"
      append-to-body
      :z-index="9999"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <ProductImageUpload
        v-if="selectedProduct"
        :product-id="selectedProduct.id"
        :max-count="10"
        @upload-success="handleImageUploadSuccess"
        @image-deleted="handleImageDeleted"
        @primary-changed="handlePrimaryChanged"
      />
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="uploadImageDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>


  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 使用Element Plus内置图标，避免@element-plus/icons-vue的导入问题
import ProductDetail from './components/ProductDetail.vue'
import ProductEdit from './components/ProductEdit.vue'
import ProductFormPlus from './components/ProductFormPlus.vue'
import ProductAudit from './components/ProductAudit.vue'
import ProductImageUpload from '@/components/ProductImageUpload.vue'
import { getProductList, deleteProduct, submitForApproval, getProductBrands, getProductCategories, activateProduct, deactivateProduct } from '@/api/product'
import { productPriceConfigApi } from '@/api/productPriceConfig'
import type { Product } from '@/types/product'

// 响应式数据
const loading = ref(false)
const productList = ref<Product[]>([])
const selectedItems = ref<Product[]>([])
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const formPlusVisible = ref(false)
const auditDialogVisible = ref(false)
const uploadImageDialogVisible = ref(false)
const selectedProduct = ref<Product | null>(null)



// 搜索表单
const searchForm = reactive({
  name: '',
  brand: '',
  categoryId: null as number | null,
  minPrice: '',
  maxPrice: '',
  status: ''
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
  { value: 'ACTIVE', label: '上架' },
  { value: 'INACTIVE', label: '下架' }
]

// 品牌选项
const brandOptions = ref<any[]>([])

// 分类选项
const categoryOptions = ref<any[]>([])

// 图片上传相关配置已移至ProductImageUpload组件

// 计算属性
const hasSelectedItems = computed(() => selectedItems.value.length > 0)

// 方法
const loadProductList = async () => {
  loading.value = true
  try {
    // 获取当前用户的公司ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const companyId = userInfo.companyId
    
    const params: any = {
      page: pagination.current,
      size: pagination.size,
      companyId: companyId, // 添加公司ID参数
      ...searchForm
    }
    // 过滤空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    const response = await getProductList(params)
    // 安全地处理响应数据，避免 undefined 错误
    if (response && response.data) {
      productList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      productList.value = []
      pagination.total = 0
    }
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
    brand: '',
    categoryId: null,
    minPrice: '',
    maxPrice: '',
    status: ''
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

const handleCreatePlus = () => {
  selectedProduct.value = null
  formPlusVisible.value = true
}

const handleView = async (row: Product) => {
  try {
    // 获取商品详情，包括价格分层配置
    const productWithConfigs = { ...row }
    
    // 调用价格分层配置API获取该商品的价格配置
    const priceConfigs = await productPriceConfigApi.getConfigsByProductId(row.id)
    productWithConfigs.priceTierConfigs = priceConfigs
    
    selectedProduct.value = productWithConfigs
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取商品价格配置失败:', error)
    // 如果获取价格配置失败，仍然显示基本信息
    selectedProduct.value = row
    detailDialogVisible.value = true
  }
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
    
    await submitForApproval(row.id) // 提交审核
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
    
    // 调用上架API - 更新商品状态为ACTIVE
    await activateProduct(row.id, 1) // 传入操作人ID
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
    
    // 调用下架API - 更新商品状态为INACTIVE
    await deactivateProduct(row.id, 1) // 传入操作人ID
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

const handleUploadImage = (row: Product) => {
  selectedProduct.value = row
  uploadImageDialogVisible.value = true
}

// 图片上传成功回调
const handleImageUploadSuccess = (images: any[]) => {
  console.log('图片上传成功:', images)
  // 可以在这里更新商品列表或执行其他操作
}

// 图片删除回调
const handleImageDeleted = (imageId: number) => {
  console.log('图片删除成功:', imageId)
  // 可以在这里执行其他操作
}

// 主图变更回调
const handlePrimaryChanged = (imageId: number) => {
  console.log('主图变更成功:', imageId)
  // 可以在这里执行其他操作
}

const handleBatchActivate = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要上架的商品')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要上架选中的 ${selectedItems.value.length} 个商品吗？`,
      '确认上架',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 批量上架逻辑
    for (const item of selectedItems.value) {
      if (item.status === 'DRAFT' || item.status === 'INACTIVE') {
        await activateProduct(item.id, 1) // 假设当前用户ID为1
      }
    }
    
    ElMessage.success('批量上架成功')
    loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量上架失败')
      console.error('批量上架失败:', error)
    }
  }
}

const handleBatchDeactivate = () => {
  ElMessage.info('批量下架功能开发中...')
}

// 移除批量删除功能

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

const handleFormPlusSuccess = () => {
  formPlusVisible.value = false
  loadProductList()
  ElMessage.success('Plus表单操作成功')
}

const handleAuditSuccess = () => {
  auditDialogVisible.value = false
  loadProductList()
  ElMessage.success('审核完成')
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'DRAFT': 'info',
    'ACTIVE': 'success',
    'INACTIVE': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'DRAFT': '草稿',
    'ACTIVE': '上架',
    'INACTIVE': '下架'
  }
  return textMap[status] || status
}

// 移除审核状态相关方法

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 获取品牌列表
const loadBrandOptions = async () => {
  try {
    const response = await getProductBrands()
    brandOptions.value = response.data || []
  } catch (error) {
    console.error('获取品牌列表失败:', error)
    ElMessage.warning('获取品牌列表失败')
  }
}

// 获取分类列表
const loadCategoryOptions = async () => {
  try {
    const response = await getProductCategories()
    categoryOptions.value = response.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.warning('获取分类列表失败')
  }
}



// 生命周期
onMounted(() => {
  loadProductList()
  loadBrandOptions()
  loadCategoryOptions()
})

// 组件卸载时清理
onUnmounted(() => {
  // 确保恢复页面滚动
  document.body.style.overflow = ''
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
