<template>
  <el-dialog
    v-model="visible"
    title="批量导入客户"
    width="600px"
    :before-close="handleClose"
    destroy-on-close
  >
    <div class="import-content">
      <!-- 文件上传 -->
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :data="uploadData"
        :before-upload="beforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :on-progress="handleUploadProgress"
        :file-list="fileList"
        :limit="1"
        accept=".xlsx,.xls"
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

      <!-- 导入说明 -->
      <div class="import-instructions">
        <h4>导入说明</h4>
        <el-alert
          title="请按照以下格式准备Excel文件："
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>第一行：客户编码 | 客户名称 | 联系人 | 联系电话 | 联系邮箱 | 地址 | 客户类型 | 客户等级 | 所属公司</p>
            <p>客户类型：INDIVIDUAL(个人)、ENTERPRISE(企业)、DEALER(经销商)、AGENT(代理商)</p>
            <p>客户等级：NORMAL(普通)、VIP(VIP)、IMPORTANT(重要)、STRATEGIC(战略)</p>
            <p>所属公司：可选，留空表示个人客户</p>
          </template>
        </el-alert>
      </div>

      <!-- 导入进度 -->
      <div v-if="importProgress.show" class="import-progress">
        <h4>导入进度</h4>
        <el-progress
          :percentage="importProgress.percentage"
          :status="importProgress.status"
        />
        <div class="progress-details">
          <span>总数: {{ importProgress.total }}</span>
          <span>成功: {{ importProgress.success }}</span>
          <span>失败: {{ importProgress.failed }}</span>
          <span>跳过: {{ importProgress.skipped }}</span>
        </div>
        <div v-if="importProgress.message" class="progress-message">
          {{ importProgress.message }}
        </div>
      </div>

      <!-- 错误信息 -->
      <div v-if="errorDetails.length > 0" class="error-details">
        <h4>错误详情</h4>
        <el-collapse>
          <el-collapse-item
            v-for="(error, index) in errorDetails"
            :key="index"
            :title="`第${error.row}行: ${error.message}`"
          >
            <div class="error-content">
              <p><strong>错误信息:</strong> {{ error.message }}</p>
              <p><strong>行数据:</strong> {{ error.data }}</p>
              <p v-if="error.suggestion"><strong>建议:</strong> {{ error.suggestion }}</p>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="!importProgress.show"
          type="primary"
          @click="startImport"
          :disabled="!fileList.length"
        >
          开始导入
        </el-button>
        <el-button
          v-else
          type="danger"
          @click="cancelImport"
          :disabled="importProgress.status === 'success'"
        >
          取消导入
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { UploadProps, UploadUserFile } from 'element-plus'

// Props
interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const uploadRef = ref()
const fileList = ref<UploadUserFile[]>([])
const uploadUrl = ref('')
const uploadHeaders = ref({})
const uploadData = ref({})

// 导入进度
const importProgress = reactive({
  show: false,
  percentage: 0,
  status: 'active' as 'active' | 'success' | 'exception',
  total: 0,
  success: 0,
  failed: 0,
  skipped: 0,
  message: ''
})

const errorDetails = ref<Array<{
  row: number
  message: string
  data: string
  suggestion?: string
}>>([])

// 上传前检查
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }

  return true
}

// 上传成功
const handleUploadSuccess: UploadProps['onSuccess'] = (response, file) => {
  ElMessage.success('文件上传成功')
  
  // 开始导入处理
  startImportProcess(file)
}

// 上传失败
const handleUploadError: UploadProps['onError'] = (error, file) => {
  ElMessage.error('文件上传失败')
  console.error('上传失败:', error)
}

// 上传进度
const handleUploadProgress: UploadProps['onProgress'] = (event, file) => {
  console.log('上传进度:', event.percent)
}

// 开始导入处理
const startImportProcess = async (file: File) => {
  try {
    importProgress.show = true
    importProgress.status = 'active'
    importProgress.message = '正在处理文件...'

    // TODO: 调用客户批量导入API
    // const response = await customerApi.importExcelCustomers(file)
    
    // 模拟导入过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    importProgress.total = 100
    importProgress.success = 95
    importProgress.failed = 3
    importProgress.skipped = 2
    importProgress.percentage = 100
    importProgress.status = 'success'
    importProgress.message = '导入完成'
    
    ElMessage.success('客户导入完成')
    
    // 延迟关闭对话框
    setTimeout(() => {
      emit('success')
      visible.value = false
    }, 2000)
  } catch (error) {
    importProgress.status = 'exception'
    importProgress.message = '导入失败'
    ElMessage.error('客户导入失败')
    console.error('导入失败:', error)
  }
}

// 开始导入
const startImport = () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }
  
  // 触发文件上传
  uploadRef.value?.submit()
}

// 取消导入
const cancelImport = async () => {
  try {
    await ElMessageBox.confirm('确定要取消导入吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用取消导入API
    ElMessage.info('导入已取消')
    importProgress.show = false
  } catch (error) {
    // 用户取消
  }
}

// 关闭对话框
const handleClose = () => {
  if (importProgress.show && importProgress.status === 'active') {
    ElMessageBox.confirm('导入正在进行中，确定要关闭吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      visible.value = false
    }).catch(() => {
      // 用户取消
    })
  } else {
    visible.value = false
  }
}
</script>

<style scoped>
.import-content {
  padding: 20px 0;
}

.upload-demo {
  margin-bottom: 20px;
}

.import-instructions {
  margin-bottom: 20px;
}

.import-instructions h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.import-progress {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 6px;
}

.import-progress h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.progress-details {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
}

.progress-message {
  margin-top: 10px;
  padding: 8px;
  background: #e1f3d8;
  border-radius: 4px;
  color: #67c23a;
  font-size: 14px;
}

.error-details {
  margin-top: 20px;
}

.error-details h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.error-content p {
  margin: 5px 0;
  font-size: 14px;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style>
