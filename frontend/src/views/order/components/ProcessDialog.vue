<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="600px"
    :before-close="handleClose"
  >
    <div class="alert-info">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预警ID">{{ alert?.id }}</el-descriptions-item>
        <el-descriptions-item label="源订单ID">{{ alert?.sourceOrderId }}</el-descriptions-item>
        <el-descriptions-item label="处理类型">{{ alert?.processingTypeDesc }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ alert?.priorityDesc }}</el-descriptions-item>
        <el-descriptions-item label="处理原因" :span="2">
          {{ alert?.processingReason }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      style="margin-top: 20px"
    >
      <el-form-item
        v-if="actionType === 'process'"
        label="处理备注"
        prop="processingNotes"
      >
        <el-input
          v-model="form.processingNotes"
          type="textarea"
          :rows="4"
          placeholder="请输入处理备注"
        />
      </el-form-item>

      <el-form-item
        v-if="actionType === 'complete'"
        label="完成备注"
        prop="processingNotes"
      >
        <el-input
          v-model="form.processingNotes"
          type="textarea"
          :rows="4"
          placeholder="请输入完成备注"
        />
      </el-form-item>

      <el-form-item
        v-if="actionType === 'reject'"
        label="拒绝原因"
        prop="rejectionReason"
      >
        <el-input
          v-model="form.rejectionReason"
          type="textarea"
          :rows="4"
          placeholder="请输入拒绝原因"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          {{ submitButtonText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { orderAlertApi } from '@/api/orderAlert'
import type { OrderAlertResponse } from '@/types/orderAlert'
import type { FormInstance, FormRules } from 'element-plus'

interface Props {
  modelValue: boolean
  alert: OrderAlertResponse | null
  actionType: 'process' | 'complete' | 'reject'
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  processingNotes: '',
  rejectionReason: ''
})

const rules: FormRules = {
  processingNotes: [
    { required: true, message: '请输入备注信息', trigger: 'blur' }
  ],
  rejectionReason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => {
  const titleMap = {
    process: '开始处理预警',
    complete: '完成预警处理',
    reject: '拒绝预警处理'
  }
  return titleMap[props.actionType]
})

const submitButtonText = computed(() => {
  const textMap = {
    process: '开始处理',
    complete: '完成处理',
    reject: '确认拒绝'
  }
  return textMap[props.actionType]
})

// 监听props变化
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    resetForm()
  }
})

watch(dialogVisible, (newVal) => {
  emit('update:modelValue', newVal)
})

const resetForm = () => {
  form.processingNotes = ''
  form.rejectionReason = ''
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (!props.alert) {
      ElMessage.error('预警信息不能为空')
      return
    }
    
    loading.value = true
    
    const data = {
      processingNotes: form.processingNotes,
      rejectionReason: form.rejectionReason
    }
    
    switch (props.actionType) {
      case 'process':
        await orderAlertApi.processAlert(props.alert.id, data)
        ElMessage.success('开始处理成功')
        break
      case 'complete':
        await orderAlertApi.completeAlert(props.alert.id, data)
        ElMessage.success('完成处理成功')
        break
      case 'reject':
        await orderAlertApi.rejectAlert(props.alert.id, data)
        ElMessage.success('拒绝处理成功')
        break
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    console.error('处理预警失败:', error)
    ElMessage.error('处理预警失败')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}
</script>

<style scoped>
.alert-info {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
}
</style>
