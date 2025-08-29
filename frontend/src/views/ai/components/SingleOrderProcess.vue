<template>
  <div class="single-order-process">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="订单ID" prop="orderId">
        <el-input v-model="form.orderId" placeholder="请输入订单ID" />
      </el-form-item>
      
      <el-form-item label="处理类型" prop="processType">
        <el-select v-model="form.processType" placeholder="请选择处理类型">
          <el-option label="智能分析" value="智能分析" />
          <el-option label="订单优化" value="订单优化" />
          <el-option label="库存预测" value="库存预测" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="处理描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入处理描述"
        />
      </el-form-item>
      
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="form.priority" placeholder="请选择优先级">
          <el-option label="低" :value="1" />
          <el-option label="普通" :value="2" />
          <el-option label="高" :value="3" />
          <el-option label="紧急" :value="4" />
        </el-select>
      </el-form-item>
    </el-form>
    
    <div class="form-actions">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        开始处理
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const emit = defineEmits<{
  success: []
  cancel: []
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  orderId: '',
  processType: '',
  description: '',
  priority: 2
})

const rules: FormRules = {
  orderId: [
    { required: true, message: '请输入订单ID', trigger: 'blur' }
  ],
  processType: [
    { required: true, message: '请选择处理类型', trigger: 'change' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // TODO: 调用API处理订单
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('订单处理请求已发送')
    emit('success')
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.single-order-process {
  padding: 20px 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--md-sys-color-outline-variant);
}
</style>
