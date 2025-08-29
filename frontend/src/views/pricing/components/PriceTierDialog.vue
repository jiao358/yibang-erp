<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑价格分层' : '新建价格分层'"
    width="800px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="price-tier-form"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="分层名称" prop="tierName">
            <el-input
              v-model="form.tierName"
              placeholder="请输入分层名称"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分层代码" prop="tierCode">
            <el-input
              v-model="form.tierCode"
              placeholder="请输入分层代码"
              clearable
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="分层级别" prop="tierLevel">
            <el-select
              v-model="form.tierLevel"
              placeholder="请选择分层级别"
              style="width: 100%"
            >
              <el-option label="钻石级" :value="1" />
              <el-option label="黄金级" :value="2" />
              <el-option label="白银级" :value="3" />
              <el-option label="标准级" :value="4" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-input-number
              v-model="form.priority"
              :min="0"
              :max="999"
              placeholder="请输入优先级"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="折扣率" prop="discountRate">
            <el-input-number
              v-model="form.discountRate"
              :min="0"
              :max="1"
              :precision="4"
              :step="0.01"
              placeholder="请输入折扣率"
              style="width: 100%"
            >
              <template #suffix>%</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="加价率" prop="markupRate">
            <el-input-number
              v-model="form.markupRate"
              :min="0"
              :max="2"
              :precision="4"
              :step="0.01"
              placeholder="请输入加价率"
              style="width: 100%"
            >
              <template #suffix>%</template>
            </el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="最小订单金额" prop="minOrderAmount">
            <el-input-number
              v-model="form.minOrderAmount"
              :min="0"
              :precision="2"
              placeholder="请输入最小订单金额"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大订单金额" prop="maxOrderAmount">
            <el-input-number
              v-model="form.maxOrderAmount"
              :min="0"
              :precision="2"
              placeholder="请输入最大订单金额"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="最小订单数量" prop="minOrderQuantity">
            <el-input-number
              v-model="form.minOrderQuantity"
              :min="0"
              placeholder="请输入最小订单数量"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大订单数量" prop="maxOrderQuantity">
            <el-input-number
              v-model="form.maxOrderQuantity"
              :min="0"
              placeholder="请输入最大订单数量"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="客户等级要求" prop="customerLevelRequirement">
            <el-select
              v-model="form.customerLevelRequirement"
              placeholder="请选择客户等级要求"
              style="width: 100%"
            >
              <el-option label="钻石级" value="PREMIUM" />
              <el-option label="黄金级" value="VIP" />
              <el-option label="白银级" value="REGULAR" />
              <el-option label="标准级" value="ALL" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="isActive">
            <el-switch
              v-model="form.isActive"
              active-text="启用"
              inactive-text="禁用"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="付款条件" prop="paymentTerms">
        <el-input
          v-model="form.paymentTerms"
          placeholder="请输入付款条件"
          clearable
        />
      </el-form-item>

      <el-form-item label="信用额度" prop="creditLimit">
        <el-input-number
          v-model="form.creditLimit"
          :min="0"
          :precision="2"
          placeholder="请输入信用额度"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="分层描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入分层描述"
          clearable
        />
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
import { priceTierApi } from '@/api/priceTier'

// Props
interface Props {
  modelValue: boolean
  priceTier?: any
}

const props = withDefaults(defineProps<Props>(), {
  priceTier: null
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
  tierName: '',
  tierCode: '',
  tierLevel: 4,
  description: '',
  discountRate: 1.00,
  markupRate: 1.00,
  minOrderAmount: null as number | null,
  maxOrderAmount: null as number | null,
  minOrderQuantity: null as number | null,
  maxOrderQuantity: null as number | null,
  customerLevelRequirement: 'ALL',
  paymentTerms: '',
  creditLimit: null as number | null,
  isActive: true
})

// 表单验证规则
const rules: FormRules = {
  tierName: [
    { required: true, message: '请输入分层名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  tierCode: [
    { required: true, message: '请输入分层代码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' }
  ],
  tierLevel: [
    { required: true, message: '请选择分层级别', trigger: 'change' }
  ],
  discountRate: [
    { required: true, message: '请输入折扣率', trigger: 'blur' },
    { type: 'number', min: 0, max: 1, message: '折扣率必须在 0 到 1 之间', trigger: 'blur' }
  ],
  markupRate: [
    { required: true, message: '请输入加价率', trigger: 'blur' },
    { type: 'number', min: 0, max: 2, message: '加价率必须在 0 到 2 之间', trigger: 'blur' }
  ],
  customerLevelRequirement: [
    { required: true, message: '请选择客户等级要求', trigger: 'change' }
  ]
}

// 计算属性
const isEdit = computed(() => !!props.priceTier)

// 提交状态
const submitting = ref(false)

// 监听价格分层变化
watch(
  () => props.priceTier,
  (newPriceTier) => {
    if (newPriceTier) {
      // 编辑模式，填充表单
      Object.assign(form, {
        tierName: newPriceTier.tierName || '',
        tierCode: newPriceTier.tierCode || '',
        tierLevel: newPriceTier.tierLevel || 4,
        description: newPriceTier.description || '',
        discountRate: newPriceTier.discountRate || 1.00,
        markupRate: newPriceTier.markupRate || 1.00,
        minOrderAmount: newPriceTier.minOrderAmount || null,
        maxOrderAmount: newPriceTier.maxOrderAmount || null,
        minOrderQuantity: newPriceTier.minOrderQuantity || null,
        maxOrderQuantity: newPriceTier.maxOrderQuantity || null,
        customerLevelRequirement: newPriceTier.customerLevelRequirement || 'ALL',
        paymentTerms: newPriceTier.paymentTerms || '',
        creditLimit: newPriceTier.creditLimit || null,
        isActive: newPriceTier.isActive !== undefined ? newPriceTier.isActive : true
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
    tierName: '',
    tierCode: '',
    tierLevel: 4,
    description: '',
    discountRate: 1.00,
    markupRate: 1.00,
    minOrderAmount: null,
    maxOrderAmount: null,
    minOrderQuantity: null,
    maxOrderQuantity: null,
    customerLevelRequirement: 'ALL',
    paymentTerms: '',
    creditLimit: null,
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
      await priceTierApi.updatePriceTier(props.priceTier.id, form)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await priceTierApi.createPriceTier(form)
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
.price-tier-form {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input-number .el-input__suffix) {
  color: #909399;
}
</style>
