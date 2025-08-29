<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑价格策略' : '新建价格策略'"
    width="900px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="140px"
      class="price-strategy-form"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="策略名称" prop="strategyName">
            <el-input
              v-model="form.strategyName"
              placeholder="请输入策略名称"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="策略类型" prop="strategyType">
            <el-select
              v-model="form.strategyType"
              placeholder="请选择策略类型"
              style="width: 100%"
            >
              <el-option label="固定定价" value="FIXED" />
              <el-option label="百分比定价" value="PERCENTAGE" />
              <el-option label="分层定价" value="TIERED" />
              <el-option label="动态定价" value="DYNAMIC" />
              <el-option label="季节性定价" value="SEASONAL" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="基础价格类型" prop="basePriceType">
            <el-select
              v-model="form.basePriceType"
              placeholder="请选择基础价格类型"
              style="width: 100%"
            >
              <el-option label="成本价" value="COST" />
              <el-option label="市场价" value="MARKET" />
              <el-option label="自定义价" value="CUSTOM" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="基础价格倍数" prop="basePriceMultiplier">
            <el-input-number
              v-model="form.basePriceMultiplier"
              :min="0.1"
              :max="10"
              :precision="2"
              :step="0.1"
              placeholder="请输入价格倍数"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="最小利润率" prop="minMarginRate">
            <el-input-number
              v-model="form.minMarginRate"
              :min="0"
              :max="1"
              :precision="4"
              :step="0.01"
              placeholder="请输入最小利润率"
              style="width: 100%"
            >
              <template #suffix>%</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大利润率" prop="maxMarginRate">
            <el-input-number
              v-model="form.maxMarginRate"
              :min="0"
              :max="1"
              :precision="4"
              :step="0.01"
              placeholder="请输入最大利润率"
              style="width: 100%"
            >
              <template #suffix>%</template>
            </el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="竞争对手价格权重" prop="competitorPriceWeight">
            <el-input-number
              v-model="form.competitorPriceWeight"
              :min="0"
              :max="1"
              :precision="2"
              :step="0.1"
              placeholder="请输入权重"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="市场需求权重" prop="marketDemandWeight">
            <el-input-number
              v-model="form.marketDemandWeight"
              :min="0"
              :max="1"
              :precision="2"
              :step="0.1"
              placeholder="请输入权重"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="生效时间" prop="effectiveFrom">
            <el-date-picker
              v-model="form.effectiveFrom"
              type="datetime"
              placeholder="请选择生效时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="失效时间" prop="effectiveTo">
            <el-date-picker
              v-model="form.effectiveTo"
              type="datetime"
              placeholder="请选择失效时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="状态" prop="isActive">
        <el-switch
          v-model="form.isActive"
          active-text="启用"
          inactive-text="禁用"
        />
      </el-form-item>

      <el-form-item label="策略描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入策略描述"
          clearable
        />
      </el-form-item>

      <el-form-item label="季节性因素" prop="seasonalityFactors">
        <el-input
          v-model="form.seasonalityFactors"
          type="textarea"
          :rows="3"
          placeholder="请输入季节性因素（JSON格式）"
          clearable
        />
        <div class="form-tip">支持JSON格式的季节性调整因素</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { priceStrategyApi } from '@/api/priceStrategy'

// Props
interface Props {
  modelValue: boolean
  priceStrategy?: any
}

const props = withDefaults(defineProps<Props>(), {
  priceStrategy: null
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive({
  strategyName: '',
  strategyType: 'FIXED',
  description: '',
  basePriceType: 'COST',
  basePriceMultiplier: 1.00,
  minMarginRate: null as number | null,
  maxMarginRate: null as number | null,
  competitorPriceWeight: null as number | null,
  marketDemandWeight: null as number | null,
  seasonalityFactors: '',
  effectiveFrom: '',
  effectiveTo: '',
  isActive: true
})

// 表单验证规则
const rules: FormRules = {
  strategyName: [
    { required: true, message: '请输入策略名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  strategyType: [
    { required: true, message: '请选择策略类型', trigger: 'change' }
  ],
  basePriceType: [
    { required: true, message: '请选择基础价格类型', trigger: 'change' }
  ],
  basePriceMultiplier: [
    { required: true, message: '请输入基础价格倍数', trigger: 'blur' },
    { type: 'number', min: 0.1, max: 10, message: '价格倍数必须在 0.1 到 10 之间', trigger: 'blur' }
  ],
  effectiveFrom: [
    { required: true, message: '请选择生效时间', trigger: 'change' }
  ]
}

// 计算属性
const isEdit = computed(() => !!props.priceStrategy)

// 提交状态
const submitting = ref(false)

// 监听价格策略变化
watch(
  () => props.priceStrategy,
  (newPriceStrategy) => {
    if (newPriceStrategy) {
      // 编辑模式，填充表单
      Object.assign(form, {
        strategyName: newPriceStrategy.strategyName || '',
        strategyType: newPriceStrategy.strategyType || 'FIXED',
        description: newPriceStrategy.description || '',
        basePriceType: newPriceStrategy.basePriceType || 'COST',
        basePriceMultiplier: newPriceStrategy.basePriceMultiplier || 1.00,
        minMarginRate: newPriceStrategy.minMarginRate || null,
        maxMarginRate: newPriceStrategy.maxMarginRate || null,
        competitorPriceWeight: newPriceStrategy.competitorPriceWeight || null,
        marketDemandWeight: newPriceStrategy.marketDemandWeight || null,
        seasonalityFactors: newPriceStrategy.seasonalityFactors || '',
        effectiveFrom: newPriceStrategy.effectiveFrom || '',
        effectiveTo: newPriceStrategy.effectiveTo || '',
        isActive: newPriceStrategy.isActive !== undefined ? newPriceStrategy.isActive : true
      })
    } else {
      // 新建模式，重置表单
      resetForm()
    }
  },
  { immediate: true }
)

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    strategyName: '',
    strategyType: 'FIXED',
    description: '',
    basePriceType: 'COST',
    basePriceMultiplier: 1.00,
    minMarginRate: null,
    maxMarginRate: null,
    competitorPriceWeight: null,
    marketDemandWeight: null,
    seasonalityFactors: '',
    effectiveFrom: '',
    effectiveTo: '',
    isActive: true
  })
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      // 更新
      await priceStrategyApi.updatePriceStrategy(props.priceStrategy.id, form)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await priceStrategyApi.createPriceStrategy(form)
      ElMessage.success('创建成功')
    }

    emit('success')
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  } finally {
    submitting.value = false
  }
}

// 监听对话框显示状态
watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      nextTick(() => {
        formRef.value?.clearValidate()
      })
    }
  }
)
</script>

<style scoped>
.price-strategy-form {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input-number .el-input__suffix) {
  color: #909399;
}
</style>
