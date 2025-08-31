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
              placeholder="请输入分层名称，如：1级经销商"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分层代码" prop="tierCode">
            <el-input
              v-model="form.tierCode"
              placeholder="请输入分层代码，如：DEALER_LEVEL_1"
              clearable
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-input-number
              v-model="form.priority"
              :min="1"
              :max="999"
              placeholder="数字越小优先级越高"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
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

      <el-form-item label="分层描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入分层描述，说明该分层的适用场景和特点"
          clearable
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
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
import type { PriceTier, PriceTierResponse } from '@/types/priceTier'

// Props
interface Props {
  modelValue: boolean
  priceTier?: PriceTierResponse | null
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

// 加载状态
const loading = ref(false)

// 表单数据 - 只保留核心字段
const form = reactive({
  tierName: '',
  tierCode: '',
  priority: 1,
  isActive: true,
  description: ''
})

// 表单验证规则 - 只保留核心字段验证
const rules: FormRules = {
  tierName: [
    { required: true, message: '请输入分层名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  tierCode: [
    { required: true, message: '请输入分层代码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请输入优先级', trigger: 'blur' },
    { type: 'number', min: 1, max: 999, message: '优先级范围在 1 到 999', trigger: 'blur' }
  ]
}

// 计算属性
const isEdit = computed(() => !!props.priceTier)

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

// 获取当前用户ID
const getCurrentUserId = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.id || 1 // 如果没有用户ID，默认使用1
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return 1
    }
  }
  return 1
}

// 重置表单 - 只保留核心字段
const resetForm = () => {
  Object.assign(form, {
    tierName: '',
    tierCode: '',
    priority: 1,
    isActive: true,
    description: ''
  })
  
  // 清除验证错误
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 监听价格分层数据变化 - 只保留核心字段
watch(() => props.priceTier, (newPriceTier) => {
  if (newPriceTier) {
    // 编辑模式：填充表单数据
    Object.assign(form, {
      tierName: newPriceTier.tierName || '',
      tierCode: newPriceTier.tierCode || '',
      priority: newPriceTier.priority || 1,
      isActive: newPriceTier.isActive ?? true,
      description: newPriceTier.description || ''
    })
  } else {
    // 新建模式：重置表单数据
    resetForm()
  }
}, { immediate: true })

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 构建提交数据，设置必要的字段
    const submitData = {
      ...form,
      tierType: 'RETAIL', // 默认设置为零售类型（符合数据库枚举约束）
      tierLevel: 1, // 默认设置为1级
      companyId: getUserCompanyId(), // 从用户信息获取公司ID
      createdBy: getCurrentUserId() // 从用户信息获取创建人ID
    }
    
    if (isEdit.value && props.priceTier) {
      // 更新
      await priceTierApi.updatePriceTier(props.priceTier.id, submitData)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await priceTierApi.createPriceTier(submitData)
      ElMessage.success('创建成功')
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
}
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

:deep(.el-input-number) {
  width: 100%;
}
</style>
