<template>
  <el-dialog
    v-model="dialogVisible"
    title="分配预警"
    width="500px"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="分配给" prop="assignedTo">
        <el-select
          v-model="form.assignedTo"
          placeholder="请选择处理人"
          style="width: 100%"
          filterable
        >
          <el-option
            v-for="user in userOptions"
            :key="user.id"
            :label="user.username"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { orderAlertApi } from '@/api/orderAlert'
import { userApi } from '@/api/user'
import type { FormInstance, FormRules } from 'element-plus'

interface User {
  id: number
  username: string
  realName?: string
}

interface Props {
  modelValue: boolean
  alertId: number | null
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
const userOptions = ref<User[]>([])

const form = reactive({
  assignedTo: null as number | null
})

const rules: FormRules = {
  assignedTo: [
    { required: true, message: '请选择处理人', trigger: 'change' }
  ]
}

// 监听props变化
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    loadUsers()
    resetForm()
  }
})

watch(dialogVisible, (newVal) => {
  emit('update:modelValue', newVal)
})

const loadUsers = async () => {
  try {
    const response = await userApi.getUserList({
      current: 1,
      size: 1000,
      status: 'ACTIVE'
    })
    
    if (response.data.success) {
      userOptions.value = response.data.data.records || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

const resetForm = () => {
  form.assignedTo = null
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (!props.alertId) {
      ElMessage.error('预警ID不能为空')
      return
    }
    
    loading.value = true
    
    await orderAlertApi.assignAlert(props.alertId, form.assignedTo!)
    
    ElMessage.success('分配成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('分配预警失败:', error)
    ElMessage.error('分配预警失败')
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
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
