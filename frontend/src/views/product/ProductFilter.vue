<template>
  <div class="product-filter-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">商品筛选</h1>
      <p class="page-subtitle">浏览和筛选可销售的商品</p>
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
        <el-form-item label="供应商公司">
          <el-select
            v-model="searchForm.supplierCompanyId"
            placeholder="请选择供应商公司"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="company in supplierCompanies"
              :key="company.id"
              :label="company.name"
              :value="company.id"
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

    <!-- 商品列表表格 -->
    <div class="table-section">
      <el-table
        :data="productList"
        v-loading="loading"
        style="width: 100%"
        class="product-table"
      >
        <el-table-column prop="name" label="商品名称" min-width="200">
          <template #default="{ row }">
            <div class="product-name">
              <span class="name-text">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="sku" label="商品编码" width="120" />
        
        <el-table-column prop="sellingPrice" label="一件代发价格" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.sellingPrice }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="marketPrice" label="零售限价" width="120">
          <template #default="{ row }">
            <span class="market-price">¥{{ row.marketPrice || '--' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="image" label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              style="width: 60px; height: 60px"
              fit="cover"
              :preview-src-list="[row.image]"
            />
            <span v-else class="no-image">无图片</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="brand" label="品牌" width="100">
          <template #default="{ row }">
            <span>{{ row.brand || '--' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="specifications" label="规格" width="100">
          <template #default="{ row }">
            <span>{{ row.unit || '--' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="supplierCompanyName" label="供应商公司" width="150" />
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleView(row)">
                查看详情
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
      v-model:visible="detailDialogVisible"
      :product="selectedProduct"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ProductDetail from './components/ProductDetail.vue'
import { getProductBrands, getProductCategories } from '@/api/product'
import { companyApi } from '@/api/company'
import type { Product } from '@/types/product'

// 响应式数据
const loading = ref(false)
const productList = ref<Product[]>([])
const detailDialogVisible = ref(false)
const selectedProduct = ref<Product | null>(null)
const supplierCompanies = ref<any[]>([])
const brandOptions = ref<any[]>([])
const categoryOptions = ref<any[]>([])

// 搜索表单
const searchForm = reactive({
  name: '',
  brand: '',
  categoryId: null as number | null,
  minPrice: '',
  maxPrice: '',
  supplierCompanyId: null as number | null
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 获取商品列表（仅显示上架商品）
const loadProductList = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.current,
      size: pagination.size,
      name: searchForm.name || undefined,
      brand: searchForm.brand || undefined,
      categoryId: searchForm.categoryId || undefined,
      supplierCompanyId: searchForm.supplierCompanyId || undefined,
      minPrice: searchForm.minPrice ? parseFloat(searchForm.minPrice) : undefined,
      maxPrice: searchForm.maxPrice ? parseFloat(searchForm.maxPrice) : undefined
    }
    
    // 过滤空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    // 使用销售商品API
    const response = await fetch(`/api/products/sales?${new URLSearchParams(params)}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.success && data.data) {
      productList.value = data.data.records || []
      pagination.total = data.data.total || 0
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

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadProductList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    brand: '',
    categoryId: null,
    minPrice: '',
    maxPrice: '',
    supplierCompanyId: null
  })
  pagination.current = 1
  loadProductList()
}

// 查看商品详情
const handleView = (row: Product) => {
  selectedProduct.value = row
  detailDialogVisible.value = true
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadProductList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.current = page
  loadProductList()
}

// 获取供应商公司列表
const loadSupplierCompanies = async () => {
  try {
    const response = await companyApi.getSupplierCompanies()
    if (response && response.data) {
      supplierCompanies.value = response.data || []
    } else {
      supplierCompanies.value = []
    }
  } catch (error) {
    console.error('获取供应商公司列表失败:', error)
    ElMessage.warning('获取供应商公司列表失败')
    supplierCompanies.value = []
  }
}

// 获取品牌列表
const loadBrandOptions = async () => {
  try {
    const response = await getProductBrands()
    // 安全地处理响应数据
    if (response && response.data) {
      brandOptions.value = response.data
    } else {
      brandOptions.value = []
    }
  } catch (error) {
    console.error('获取品牌列表失败:', error)
    ElMessage.warning('获取品牌列表失败')
    brandOptions.value = []
  }
}

// 获取分类列表
const loadCategoryOptions = async () => {
  try {
    const response = await getProductCategories()
    // 安全地处理响应数据
    if (response && response.data) {
      categoryOptions.value = response.data
    } else {
      categoryOptions.value = []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.warning('获取分类列表失败')
    categoryOptions.value = []
  }
}

// 生命周期
onMounted(() => {
  loadProductList()
  loadSupplierCompanies()
  loadBrandOptions()
  loadCategoryOptions()
})
</script>

<style scoped>
.product-filter-container {
  padding: 24px;
  background: var(--md-sys-color-surface);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
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
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.search-form {
  margin: 0;
}

.table-section {
  background: var(--md-sys-color-surface);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--md-sys-elevation-1);
}

.product-table {
  border-radius: 12px;
}

.product-name {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-text {
  font-weight: 500;
  color: var(--md-sys-color-on-surface);
}

.price {
  font-weight: 600;
  color: var(--md-sys-color-primary);
}

.price-tier {
  font-size: 12px;
  color: var(--md-sys-color-on-surface-variant);
  margin-top: 2px;
}

.retail-price {
  font-weight: 500;
  color: var(--md-sys-color-error);
}

.no-image {
  color: var(--md-sys-color-on-surface-variant);
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination-section {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
