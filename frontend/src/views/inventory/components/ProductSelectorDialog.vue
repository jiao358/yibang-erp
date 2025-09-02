<template>
  <el-dialog
    v-model="visible"
    title="选择商品"
    width="80%"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    append-to-body
    z-index="9999"
    @close="handleClose"
  >
    <div class="product-selector-container">
      <!-- 搜索区域 -->
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
          <el-form-item label="商品编码">
            <el-input
              v-model="searchForm.sku"
              placeholder="请输入商品编码"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="品牌">
            <el-input
              v-model="searchForm.brand"
              placeholder="请输入品牌"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作栏 -->
      <div class="selector-header">
        <div class="header-left">
          <span class="selection-info">
            {{ multiple ? '多选模式' : '单选模式' }}
            <span v-if="multiple"> - 最多选择 {{ maxSelection }} 个商品</span>
            <span class="selected-count">已选择: {{ selectedProducts.length }}</span>
          </span>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="confirmSelection" :disabled="selectedProducts.length === 0">
            <el-icon><Check /></el-icon>
            确认选择 ({{ selectedProducts.length }})
          </el-button>
          <el-button @click="clearSelection">
            <el-icon><Delete /></el-icon>
            清空选择
          </el-button>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="table-section">
        <el-table
          :data="productList"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          style="width: 100%"
          :row-key="row => row.id"
          :reserve-selection="true"
          max-height="400"
        >
          <el-table-column 
            type="selection" 
            width="55" 
            :selectable="checkSelectable"
          />
          
          <el-table-column prop="sku" label="商品编码" width="120" />
          <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="brand" label="品牌" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="sellingPrice" label="销售价" width="100">
            <template #default="{ row }">
              <span>¥{{ row.sellingPrice || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 已选择的商品列表 -->
      <div v-if="selectedProducts.length > 0" class="selected-products">
        <h4>已选择的商品：</h4>
        <div class="selected-list">
          <el-tag
            v-for="product in selectedProducts"
            :key="product.id"
            type="success"
            closable
            size="large"
            @close="removeProduct(product)"
            class="selected-tag"
          >
            {{ product.sku }} - {{ product.name }}
          </el-tag>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { getProductList } from '@/api/product'
import type { Product } from '@/types/product'

// Props
interface Props {
  modelValue: boolean
  multiple?: boolean
  maxSelection?: number
}

const props = withDefaults(defineProps<Props>(), {
  multiple: true,
  maxSelection: 10
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'confirm': [products: Product[]]
}>()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const productList = ref<Product[]>([])
const selectedProducts = ref<Product[]>([])

// 搜索表单
const searchForm = reactive({
  name: '',
  sku: '',
  brand: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取当前用户公司ID
const getUserCompanyId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.companyId || 1
}

// 加载商品列表
const loadProductList = async () => {
  loading.value = true
  try {
    const currentCompanyId = getUserCompanyId()
    
    // 构建查询参数
    const params: any = {
      page: pagination.current,
      size: pagination.size,
      companyId: currentCompanyId,
      status: 'ACTIVE' // 只显示已上架的商品
    }
    
    // 添加搜索条件
    if (searchForm.name && searchForm.name.trim()) {
      params.name = searchForm.name.trim()
    }
    if (searchForm.sku && searchForm.sku.trim()) {
      params.sku = searchForm.sku.trim()
    }
    if (searchForm.brand && searchForm.brand.trim()) {
      params.brand = searchForm.brand.trim()
    }
    
    // 测试：强制添加一些参数
    console.log('搜索表单值:', searchForm)
    console.log('构建的参数对象:', params)
    
    console.log('商品选择器搜索参数:', params)
    const response = await getProductList(params)
    console.log('商品选择器API响应:', response)
    if (response.success) {
      productList.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadProductList()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    sku: '',
    brand: ''
  })
  pagination.current = 1
  loadProductList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadProductList()
}

// 分页变化
const handleCurrentChange = (page: number) => {
  pagination.current = page
  loadProductList()
}

// 检查商品是否可选择
const checkSelectable = (row: Product) => {
  // 只允许选择已上架的商品
  return row.status === 'ACTIVE'
}

// 处理选择变化
const handleSelectionChange = (selection: Product[]) => {
  if (!props.multiple) {
    // 单选模式
    selectedProducts.value = selection
  } else {
    // 多选模式，检查数量限制
    if (selection.length > props.maxSelection) {
      ElMessage.warning(`最多只能选择 ${props.maxSelection} 个商品`)
      return
    }
    selectedProducts.value = selection
  }
}

// 确认选择
const confirmSelection = () => {
  if (selectedProducts.value.length === 0) {
    ElMessage.warning('请至少选择一个商品')
    return
  }

  emit('confirm', [...selectedProducts.value])
  handleClose()
}

// 清空选择
const clearSelection = () => {
  selectedProducts.value = []
}

// 移除单个商品
const removeProduct = (product: Product) => {
  const index = selectedProducts.value.findIndex(p => p.id === product.id)
  if (index > -1) {
    selectedProducts.value.splice(index, 1)
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  selectedProducts.value = []
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'INACTIVE':
      return 'warning'
    case 'DRAFT':
      return 'info'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return '已上架'
    case 'INACTIVE':
      return '未上架'
    case 'DRAFT':
      return '草稿'
    default:
      return '未知'
  }
}

// 监听对话框显示状态
watch(visible, (newVisible) => {
  if (newVisible) {
    loadProductList()
  }
})

// 组件挂载时加载数据
onMounted(() => {
  if (visible.value) {
    loadProductList()
  }
})
</script>

<style scoped>
.product-selector-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.search-section {
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.search-form {
  margin: 0;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  gap: 12px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.selection-info {
  color: #606266;
  font-size: 14px;
}

.selected-count {
  color: #409eff;
  font-weight: 500;
  margin-left: 8px;
}

.table-section {
  flex: 1;
  margin-bottom: 16px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 16px 0;
  border-top: 1px solid #e4e7ed;
}

.selected-products {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

.selected-products h4 {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-tag {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .selector-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .header-left,
  .header-right {
    justify-content: center;
  }
  
  .search-form {
    flex-direction: column;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>
