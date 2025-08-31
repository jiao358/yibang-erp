<template>
  <div class="product-edit">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="edit-form"
    >
      <!-- 基本信息 -->
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>
        <div class="form-grid">
          <el-form-item label="商品编码" prop="sku">
            <el-input
              v-model="form.sku"
              placeholder="请输入商品编码"
              :disabled="!!product"
            />
          </el-form-item>
          
          <el-form-item label="商品名称" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入商品名称"
            />
          </el-form-item>
          
          <el-form-item label="商品分类" prop="categoryId">
            <el-select
              v-model="form.categoryId"
              placeholder="请选择商品分类"
              style="width: 100%"
            >
              <el-option
                v-for="category in categoryOptions"
                :key="category.value"
                :label="category.label"
                :value="category.value"
              />
            </el-select>
            <div style="font-size: 12px; color: #999; margin-top: 4px;">
              已加载 {{ categoryCount }} 个分类选项
            </div>
          </el-form-item>
          
          <el-form-item label="商品品牌" prop="brandId">
            <el-select
              v-model="form.brandId"
              placeholder="请选择商品品牌"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="brand in brandOptions"
                :key="brand.value"
                :label="brand.label"
                :value="brand.value"
              />
            </el-select>
            <div style="font-size: 12px; color: #999; margin-top: 4px;">
              已加载 {{ brandCount }} 个品牌选项
            </div>
          </el-form-item>
          
          <el-form-item label="单位" prop="unit">
            <el-input
              v-model="form.unit"
              placeholder="请输入单位"
            />
          </el-form-item>
          
          <el-form-item label="商品状态" prop="status">
            <el-select
              v-model="form.status"
              placeholder="请选择商品状态"
              style="width: 100%"
            >
              <el-option
                v-for="status in statusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
            </el-select>
          </el-form-item>
        </div>
      </div>

      <!-- 价格信息 -->
      <div class="form-section">
        <h3 class="section-title">价格信息</h3>
        <div class="form-grid">
          <el-form-item label="市场价" prop="marketPrice">
            <el-input-number
              v-model="form.marketPrice"
              :precision="2"
              :min="0"
              :step="0.01"
              style="width: 100%"
              placeholder="请输入市场价"
            />
          </el-form-item>
        </div>
        <div style="font-size: 12px; color: #999; margin-top: 8px;">
          <p>注：成本价、销售价、零售限价将自动设置为1元</p>
        </div>
      </div>

      <!-- 价格分层配置 -->
      <div class="form-section">
        <h3 class="section-title">价格分层配置</h3>
        <div class="price-tier-config">
          <div class="config-header">
            <div class="header-left">
              <span class="config-title">为不同客户等级配置价格</span>
              <div class="config-status">
                <el-tag v-if="availablePriceTiers.length === 0" type="warning" size="small">
                  请先配置价格分层模板
                </el-tag>
                <el-tag v-else-if="priceTierConfigs.length === 0" type="info" size="small">
                  暂无价格配置
                </el-tag>
                <el-tag v-else type="success" size="small">
                  {{ configStatusText }}
                </el-tag>
              </div>
            </div>
            <div class="header-right">
              <el-button 
                type="primary" 
                size="small" 
                @click="addPriceTierConfig"
                :disabled="!canAddMoreConfigs"
              >
                添加价格配置
              </el-button>
              <span v-if="!canAddMoreConfigs && availablePriceTiers.length > 0" class="limit-tip">
                已达到最大配置数量
              </span>
            </div>
          </div>
          
          <div v-if="priceTierConfigs.length === 0" class="empty-config">
            <el-empty 
              :description="availablePriceTiers.length === 0 ? '请先配置价格分层模板' : '暂无价格配置'" 
              :image-size="60"
            >
              <el-button 
                type="primary" 
                size="small" 
                @click="addPriceTierConfig"
                :disabled="!canAddMoreConfigs"
              >
                {{ availablePriceTiers.length === 0 ? '配置价格分层模板' : '添加第一个价格配置' }}
              </el-button>
            </el-empty>
          </div>
          
          <div v-else class="config-list">
            <div
              v-for="(config, index) in priceTierConfigs"
              :key="index"
              class="config-item"
            >
              <div class="config-header-row">
                <span class="config-index">价格配置 {{ index + 1 }}</span>
                <el-button
                  type="danger"
                  size="small"
                  @click="removePriceTierConfig(index)"
                  class="delete-btn"
                >
                  删除
                </el-button>
              </div>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="价格分层" required>
                    <el-select
                      v-model="config.priceTierId"
                      placeholder="选择价格分层"
                      style="width: 100%"
                      clearable
                    >
                      <el-option
                        v-for="tier in availablePriceTierOptions"
                        :key="tier.id"
                        :label="`${tier.tierName} (${tier.tierType})`"
                        :value="tier.id"
                      />
                      <el-option
                        v-if="config.priceTierId && !availablePriceTierOptions.find(t => t.id === config.priceTierId)"
                        :key="config.priceTierId"
                        :label="`${availablePriceTiers.find(t => t.id === config.priceTierId)?.tierName || '未知分层'} (已选择)`"
                        :value="config.priceTierId"
                        disabled
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="一件代发价" required>
                    <el-input-number
                      v-model="config.dropshippingPrice"
                      :precision="2"
                      :min="0"
                      :step="0.01"
                      style="width: 100%"
                      placeholder="请输入代发价格"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="零售限价" required>
                    <el-input-number
                      v-model="config.retailLimitPrice"
                      :precision="2"
                      :min="0"
                      :step="0.01"
                      style="width: 100%"
                      placeholder="请输入零售限价"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="状态">
                    <el-switch
                      v-model="config.isActive"
                      active-text="启用"
                      inactive-text="禁用"
                      active-color="#67c23a"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="16">
                  <div class="price-preview">
                    <span class="preview-label">价格预览：</span>
                    <el-tag v-if="config.dropshippingPrice" type="success" size="small">
                      代发价: ¥{{ config.dropshippingPrice }}
                    </el-tag>
                    <el-tag v-if="config.retailLimitPrice" type="warning" size="small">
                      零售限价: ¥{{ config.retailLimitPrice }}
                    </el-tag>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品属性 -->
      <div class="form-section">
        <h3 class="section-title">商品属性</h3>
        <div class="form-grid">
          <el-form-item label="重量(kg)" prop="weight">
            <el-input-number
              v-model="form.weight"
              :precision="3"
              :min="0"
              :step="0.001"
              style="width: 100%"
              placeholder="请输入重量"
            />
          </el-form-item>
          
          <el-form-item label="材质" prop="material">
            <el-input
              v-model="form.material"
              placeholder="请输入材质"
            />
          </el-form-item>
          
          <el-form-item label="颜色" prop="color">
            <el-input
              v-model="form.color"
              placeholder="请输入颜色"
            />
          </el-form-item>
          
          <el-form-item label="尺寸" prop="size">
            <el-input
              v-model="form.size"
              placeholder="请输入尺寸"
            />
          </el-form-item>
          
          <el-form-item label="原产国" prop="originCountry">
            <el-input
              v-model="form.originCountry"
              placeholder="请输入原产国"
            />
          </el-form-item>
          
          <el-form-item label="海关编码" prop="hsCode">
            <el-input
              v-model="form.hsCode"
              placeholder="请输入海关编码"
            />
          </el-form-item>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="form-section">
        <h3 class="section-title">商品描述</h3>
        <div class="form-grid">
          <el-form-item label="商品简介" prop="shortDescription">
            <el-input
              v-model="form.shortDescription"
              type="textarea"
              :rows="3"
              placeholder="请输入商品简介"
            />
          </el-form-item>
          
          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              placeholder="请输入商品描述"
            />
          </el-form-item>
        </div>
      </div>

      <!-- 商品特色 -->
      <div class="form-section">
        <h3 class="section-title">商品特色</h3>
        <div class="form-grid">
          <el-form-item label="推荐商品">
            <el-switch
              v-model="form.isFeatured"
              active-text="是"
              inactive-text="否"
            />
          </el-form-item>
          
          <el-form-item label="热销商品">
            <el-switch
              v-model="form.isHot"
              active-text="是"
              inactive-text="否"
            />
          </el-form-item>
          
          <el-form-item label="新品">
            <el-switch
              v-model="form.isNew"
              active-text="是"
              inactive-text="否"
            />
          </el-form-item>
        </div>
      </div>

      <!-- 商品标签 -->
      <div class="form-section">
        <h3 class="section-title">商品标签</h3>
        <div class="form-grid">
          <el-form-item label="商品标签" prop="tags">
            <el-input
              v-model="form.tags"
              type="textarea"
              :rows="3"
              placeholder="请输入商品标签，多个标签用逗号分隔"
            />
            <div class="form-tip">多个标签用逗号分隔，如：时尚,潮流,年轻</div>
          </el-form-item>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ product ? '更新' : '创建' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createProduct, updateProduct, getProductCategories, getProductBrands } from '@/api/product'
import { productPriceConfigApi } from '@/api/productPriceConfig'
import { priceTierApi } from '@/api/priceTier'
import type { Product, ProductPriceTierConfigRequest } from '@/types/product'

// Props
interface Props {
  product: Product | null
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  success: []
  cancel: []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const form = reactive({
  sku: '',
  name: '',
  categoryId: undefined as number | undefined,
  brandId: undefined as number | undefined,
  description: '',
  shortDescription: '',
  unit: '',
  costPrice: 1.00,
  sellingPrice: 1.00,
  marketPrice: undefined as number | undefined,
  retailLimitPrice: 1.00,
  weight: undefined as number | undefined,
  material: '',
  color: '',
  size: '',
  originCountry: '',
  hsCode: '',
  tags: '',
  status: 'DRAFT',
  isFeatured: false,
  isHot: false,
  isNew: false
})

// 表单验证规则
const rules: FormRules = {
  sku: [
    { required: true, message: '请输入商品编码', trigger: 'blur' },
    { min: 2, max: 100, message: '商品编码长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 200, message: '商品名称长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ],
  costPrice: [
    { required: true, message: '成本价不能为空', trigger: 'blur' },
    { type: 'number', min: 0, message: '成本价必须大于等于0', trigger: 'blur' }
  ],
  sellingPrice: [
    { required: true, message: '销售价不能为空', trigger: 'blur' },
    { type: 'number', min: 0, message: '销售价必须大于等于0', trigger: 'blur' }
  ]
}

// 选项数据 - 动态加载
const categoryOptions = ref<any[]>([])
const brandOptions = ref<any[]>([])

// 调试信息
console.log('ProductEdit组件初始化 - categoryOptions:', categoryOptions.value)
console.log('ProductEdit组件初始化 - brandOptions:', brandOptions.value)

const statusOptions = ref([
  { value: 'DRAFT', label: '草稿' },
  { value: 'ACTIVE', label: '上架' }
])

// 价格分层配置相关数据
const priceTierConfigs = ref<any[]>([])
const availablePriceTiers = ref<any[]>([])

// 加载分类选项
const loadCategoryOptions = async () => {
  try {
    const response = await getProductCategories()
    console.log('分类API响应:', response)
    if (response && response.data && Array.isArray(response.data)) {
      categoryOptions.value = response.data.map((category: any) => ({
        value: category.id,
        label: category.name
      }))
      console.log('分类选项已加载:', categoryOptions.value)
    } else {
      console.warn('分类数据格式不正确:', response)
      categoryOptions.value = []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.warning('获取分类列表失败')
    categoryOptions.value = []
  }
}

// 加载品牌选项
const loadBrandOptions = async () => {
  try {
    const response = await getProductBrands()
    console.log('品牌API响应:', response)
    if (response && response.data && Array.isArray(response.data)) {
      brandOptions.value = response.data.map((brand: any) => ({
        value: brand.id,
        label: brand.name
      }))
      console.log('品牌选项已加载:', brandOptions.value)
    } else {
      console.warn('品牌数据格式不正确:', response)
      brandOptions.value = []
    }
  } catch (error) {
    console.error('获取品牌列表失败:', error)
    ElMessage.warning('获取品牌列表失败')
    brandOptions.value = []
  }
}

// 计算属性
const isEdit = computed(() => !!props.product)

// 调试计算属性
const categoryCount = computed(() => categoryOptions.value.length)
const brandCount = computed(() => brandOptions.value.length)

// 价格分层配置相关计算属性
const canAddMoreConfigs = computed(() => {
  return priceTierConfigs.value.length < availablePriceTiers.value.length
})

const usedPriceTierIds = computed(() => {
  return priceTierConfigs.value
    .map(config => config.priceTierId)
    .filter(id => id !== null)
})

const availablePriceTierOptions = computed(() => {
  return availablePriceTiers.value.filter(tier => 
    !usedPriceTierIds.value.includes(tier.id)
  )
})

const configStatusText = computed(() => {
  const total = availablePriceTiers.value.length
  const used = priceTierConfigs.value.length
  return `${used}/${total} 个价格分层已配置`
})

// 方法
const initForm = () => {
  if (props.product) {
    // 编辑模式，填充表单数据
    Object.assign(form, {
      sku: props.product.sku || '',
      name: props.product.name || '',
      categoryId: props.product.categoryId,
      brandId: props.product.brandId,
      description: props.product.description || '',
      shortDescription: props.product.shortDescription || '',
      unit: props.product.unit || '',
      costPrice: props.product.costPrice || 1.00,
      sellingPrice: props.product.sellingPrice || 1.00,
      marketPrice: props.product.marketPrice,
      weight: props.product.weight,
      material: props.product.material || '',
      color: props.product.color || '',
      size: props.product.size || '',
      originCountry: props.product.originCountry || '',
      hsCode: props.product.hsCode || '',
      tags: props.product.tags || '',
      retailLimitPrice: props.product.retailLimitPrice || 1.00,
      status: props.product.status || 'DRAFT',
      isFeatured: props.product.isFeatured || false,
      isHot: props.product.isHot || false,
      isNew: props.product.isNew || false
    })
  } else {
    // 新增模式，设置默认值
    Object.assign(form, {
      sku: '',
      name: '',
      categoryId: undefined,
      brandId: undefined,
      description: '',
      shortDescription: '',
      unit: '',
      costPrice: 1.00,
      sellingPrice: 1.00,
      marketPrice: undefined,
      weight: undefined,
      material: '',
      color: '',
      size: '',
      originCountry: '',
      hsCode: '',
      tags: '',
      retailLimitPrice: 1.00,
      status: 'DRAFT',
      isFeatured: false,
      isHot: false,
      isNew: false
    })
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 获取当前用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const currentCompanyId = userInfo.companyId || 1
    
    const submitData = {
      ...form,
      companyId: currentCompanyId, // 添加公司ID
      approvalStatus: 'PENDING',   // 添加审核状态
      tags: form.tags ? JSON.stringify(form.tags.split(',').map(tag => tag.trim())) : '[]'
    }
    
    console.log('提交的商品数据:', submitData)
    console.log('当前用户公司ID:', currentCompanyId)
    
    let savedProductId: number
    
    if (isEdit.value && props.product) {
      // 更新商品
      const updatedProduct = await updateProduct(props.product.id, submitData)
      savedProductId = props.product.id
      ElMessage.success('商品更新成功')
    } else {
      // 创建商品
      const createdProduct = await createProduct(submitData)
      savedProductId = createdProduct.id || 0
      ElMessage.success('商品创建成功')
    }
    
    // 保存价格分层配置
    if (savedProductId && priceTierConfigs.value.length > 0) {
      // 验证价格分层配置
      if (!validatePriceTierConfigs()) {
        return
      }
      
      try {
        await saveProductPriceConfigs(savedProductId)
      } catch (error) {
        // 价格配置保存失败不影响商品保存
        console.warn('价格分层配置保存失败，但商品已保存:', error)
      }
    }
    
    emit('success')
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败，请检查表单信息')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

// 价格分层配置相关方法
const addPriceTierConfig = () => {
  // 检查是否还能添加更多配置
  if (priceTierConfigs.value.length >= availablePriceTiers.value.length) {
    ElMessage.warning('已达到最大配置数量限制')
    return
  }
  
  // 检查是否还有可用的价格分层
  const usedTierIds = priceTierConfigs.value.map(config => config.priceTierId).filter(id => id !== null)
  const availableTiers = availablePriceTiers.value.filter(tier => !usedTierIds.includes(tier.id))
  
  if (availableTiers.length === 0) {
    ElMessage.warning('所有价格分层都已配置完成')
    return
  }
  
  priceTierConfigs.value.push({
    priceTierId: null,
    dropshippingPrice: 0,
    retailLimitPrice: 0,
    isActive: true
  })
}

const removePriceTierConfig = (index: number) => {
  priceTierConfigs.value.splice(index, 1)
}

// 验证价格分层配置
const validatePriceTierConfigs = () => {
  const usedTierIds = priceTierConfigs.value
    .map(config => config.priceTierId)
    .filter(id => id !== null)
  
  // 检查是否有重复选择
  const uniqueIds = new Set(usedTierIds)
  if (uniqueIds.size !== usedTierIds.length) {
    ElMessage.error('存在重复的价格分层选择')
    return false
  }
  
  // 检查是否所有配置都选择了价格分层
  const incompleteConfigs = priceTierConfigs.value.filter(config => 
    !config.priceTierId || config.dropshippingPrice <= 0 || config.retailLimitPrice <= 0
  )
  
  if (incompleteConfigs.length > 0) {
    ElMessage.error('请完善所有价格配置信息')
    return false
  }
  
  return true
}

const loadAvailablePriceTiers = async () => {
  try {
    // 从用户信息获取当前公司ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const currentCompanyId = userInfo.companyId || 1
    
    console.log('当前用户公司ID:', currentCompanyId)
    
    // 调用价格分层API获取当前公司可用的选项
    const response = await priceTierApi.getPriceTierList({ 
      page: 1, 
      size: 100,
      companyId: currentCompanyId
    })
    
    if (response && Array.isArray(response)) {
      availablePriceTiers.value = response.map((tier: any) => ({
        id: tier.id,
        tierName: tier.tierName,
        tierType: tier.tierType || 'RETAIL'
      }))
      console.log('加载到价格分层选项:', availablePriceTiers.value)
    } else {
      console.warn('价格分层API返回格式不正确:', response)
      availablePriceTiers.value = []
    }
  } catch (error) {
    console.error('获取价格分层失败:', error)
    availablePriceTiers.value = []
  }
}

const loadProductPriceConfigs = async (productId: number) => {
  try {
    const configs = await productPriceConfigApi.getConfigsByProductId(productId)
    priceTierConfigs.value = configs.map(config => ({
      id: config.id,
      priceTierId: config.priceTierId,
      dropshippingPrice: config.dropshippingPrice,
      retailLimitPrice: config.retailLimitPrice,
      isActive: config.isActive
    }))
  } catch (error) {
    console.error('获取商品价格配置失败:', error)
    priceTierConfigs.value = []
  }
}

const saveProductPriceConfigs = async (productId: number) => {
  try {
    // 获取当前用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const currentCompanyId = userInfo.companyId || 1
    const currentUserId = userInfo.id || 1
    
    const configs: ProductPriceTierConfigRequest[] = priceTierConfigs.value.map(config => ({
      productId: productId,
      priceTierId: config.priceTierId!,
      dropshippingPrice: config.dropshippingPrice!,
      retailLimitPrice: config.retailLimitPrice!,
      isActive: config.isActive,
      companyId: currentCompanyId, // 添加公司ID
      createdBy: currentUserId      // 添加创建人ID
    }))
    
    await productPriceConfigApi.batchSaveConfigs(productId, configs)
    ElMessage.success('价格分层配置保存成功')
  } catch (error) {
    console.error('保存价格分层配置失败:', error)
    ElMessage.error('保存价格分层配置失败')
    throw error
  }
}

// 监听商品数据变化
watch(() => props.product, (newProduct) => {
  if (newProduct) {
    initForm()
    // 如果是编辑模式，加载价格分层配置
    if (newProduct.id) {
      loadProductPriceConfigs(newProduct.id)
    }
  } else {
    initForm()
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  // 加载分类和品牌选项
  loadCategoryOptions()
  loadBrandOptions()
  loadAvailablePriceTiers()
})
</script>

<style scoped>
.product-edit {
  padding: 0;
}

.edit-form {
  max-height: 70vh;
  overflow-y: auto;
}

.form-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 20px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--md-sys-color-outline-variant);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.form-tip {
  font-size: 12px;
  color: var(--md-sys-color-on-surface-variant);
  margin-top: 4px;
  line-height: 1.4;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid var(--md-sys-color-outline-variant);
}

/* 价格分层配置样式 */
.price-tier-config {
  border: 1px solid var(--md-sys-color-outline-variant);
  border-radius: 8px;
  padding: 20px;
  background: var(--md-sys-color-surface-container-low);
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.config-title {
  font-size: 16px;
  font-weight: 600;
  color: white;
}

.config-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.limit-tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-style: italic;
}

.empty-config {
  text-align: center;
  padding: 40px 20px;
  background: #f8f9fa;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-item {
  padding: 20px;
  border: 1px solid var(--md-sys-color-outline);
  border-radius: 8px;
  background: var(--md-sys-color-surface);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.config-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.config-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.config-index {
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
}

.delete-btn {
  border-radius: 6px;
}

.price-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.preview-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
}
</style>
