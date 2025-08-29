<template>
  <div class="excel-import-process">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="Excel文件" prop="file">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :on-change="handleFileChange"
          :before-upload="beforeUpload"
          :limit="1"
          accept=".xlsx,.xls"
          drag
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传 xlsx/xls 文件，且不超过 10MB
            </div>
          </template>
        </el-upload>
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
      <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="!form.file">
        开始导入处理
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'

const emit = defineEmits<{
  success: []
  cancel: []
}>()

const formRef = ref<FormInstance>()
const uploadRef = ref()
const submitting = ref(false)

const form = reactive({
  file: null as File | null,
  processType: '',
  description: '',
  priority: 2
})

const rules: FormRules = {
  file: [
    { required: true, message: '请选择Excel文件', trigger: 'change' }
  ],
  processType: [
    { required: true, message: '请选择处理类型', trigger: 'change' }
  ]
}

const handleFileChange = (file: UploadFile) => {
  if (file.raw) {
    form.file = file.raw
  }
}

const beforeUpload = (file: File) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                  file.type === 'application/vnd.ms-excel'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return false // 阻止自动上传
}

const handleSubmit = async () => {
  if (!formRef.value || !form.file) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // TODO: 调用API处理Excel文件
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('Excel导入处理请求已发送')
    emit('success')
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.excel-import-process {
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
