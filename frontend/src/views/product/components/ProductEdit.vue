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
          <el-form-item label="成本价" prop="costPrice">
            <el-input-number
              v-model="form.costPrice"
              :precision="2"
              :min="0"
              :step="0.01"
              style="width: 100%"
              placeholder="请输入成本价"
            />
          </el-form-item>
          
          <el-form-item label="销售价" prop="sellingPrice">
            <el-input-number
              v-model="form.sellingPrice"
              :precision="2"
              :min="0"
              :step="0.01"
              style="width: 100%"
              placeholder="请输入销售价"
            />
          </el-form-item>
          
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createProduct, updateProduct } from '@/api/product'
import type { Product } from '@/types/product'

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
  costPrice: undefined as number | undefined,
  sellingPrice: undefined as number | undefined,
  marketPrice: undefined as number | undefined,
  weight: undefined as number | undefined,
  material: '',
  color: '',
  size: '',
  originCountry: '',
  hsCode: '',
  tags: '',
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
    { required: true, message: '请输入成本价', trigger: 'blur' },
    { type: 'number', min: 0, message: '成本价必须大于等于0', trigger: 'blur' }
  ],
  sellingPrice: [
    { required: true, message: '请输入销售价', trigger: 'blur' },
    { type: 'number', min: 0, message: '销售价必须大于等于0', trigger: 'blur' }
  ]
}

// 选项数据
const categoryOptions = ref([
  { value: 1, label: '电子产品' },
  { value: 2, label: '服装鞋帽' },
  { value: 3, label: '食品饮料' },
  { value: 4, label: '家居用品' }
])

const brandOptions = ref([
  { value: 1, label: '苹果' },
  { value: 2, label: '华为' },
  { value: 3, label: '小米' },
  { value: 4, label: '耐克' },
  { value: 5, label: '阿迪达斯' }
])

const statusOptions = ref([
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING', label: '待审核' },
  { value: 'ACTIVE', label: '已上架' },
  { value: 'INACTIVE', label: '已下架' },
  { value: 'DISCONTINUED', label: '已停售' }
])

// 计算属性
const isEdit = computed(() => !!props.product)

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
      costPrice: props.product.costPrice,
      sellingPrice: props.product.sellingPrice,
      marketPrice: props.product.marketPrice,
      weight: props.product.weight,
      material: props.product.material || '',
      color: props.product.color || '',
      size: props.product.size || '',
      originCountry: props.product.originCountry || '',
      hsCode: props.product.hsCode || '',
      tags: props.product.tags || '',
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
      costPrice: undefined,
      sellingPrice: undefined,
      marketPrice: undefined,
      weight: undefined,
      material: '',
      color: '',
      size: '',
      originCountry: '',
      hsCode: '',
      tags: '',
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
    
    const submitData = {
      ...form,
      tags: form.tags ? JSON.stringify(form.tags.split(',').map(tag => tag.trim())) : '[]'
    }
    
    if (isEdit.value && props.product) {
      // 更新商品
      await updateProduct(props.product.id, submitData)
      ElMessage.success('商品更新成功')
    } else {
      // 创建商品
      await createProduct(submitData)
      ElMessage.success('商品创建成功')
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

// 生命周期
onMounted(() => {
  initForm()
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
