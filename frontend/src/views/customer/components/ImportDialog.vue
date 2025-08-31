<template>
  <el-dialog
    v-model="visible"
    title="批量导入客户"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="import-content">
      <!-- 文件上传 -->
      <div class="upload-section">
        <el-upload
          ref="uploadRef"
          :action="uploadAction"
          :headers="uploadHeaders"
          :data="uploadData"
          :before-upload="beforeUpload"
          :on-success="onUploadSuccess"
          :on-error="onUploadError"
          :on-progress="onUploadProgress"
          :show-file-list="true"
          :limit="1"
          accept=".xlsx,.xls,.csv"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 .xlsx, .xls, .csv 格式，文件大小不超过 10MB
            </div>
          </template>
        </el-upload>
      </div>

      <!-- 导入进度 -->
      <div v-if="importing" class="progress-section">
        <el-progress 
          :percentage="importProgress" 
          :status="importProgress === 100 ? 'success' : undefined"
        />
        <div class="progress-text">
          {{ progressText }}
        </div>
      </div>

      <!-- 导入结果 -->
      <div v-if="importResult" class="result-section">
        <el-alert
          :title="importResult.success ? '导入成功' : '导入失败'"
          :type="importResult.success ? 'success' : 'error'"
          :description="importResult.message"
          show-icon
        />
        
        <div v-if="importResult.success" class="result-details">
          <div class="detail-item">
            <span class="label">总行数：</span>
            <span class="value">{{ importResult.totalRows }}</span>
          </div>
          <div class="detail-item">
            <span class="label">成功行数：</span>
            <span class="value success">{{ importResult.successRows }}</span>
          </div>
          <div class="detail-item">
            <span class="label">失败行数：</span>
            <span class="value error">{{ importResult.failedRows }}</span>
          </div>
        </div>

        <div v-if="importResult.errors && importResult.errors.length > 0" class="error-list">
          <h4>错误详情：</h4>
          <el-table :data="importResult.errors" size="small" max-height="200">
            <el-table-column prop="row" label="行号" width="80" />
            <el-table-column prop="field" label="字段" width="120" />
            <el-table-column prop="message" label="错误信息" />
          </el-table>
        </div>
      </div>

      <!-- 模板下载 -->
      <div class="template-section">
        <el-button type="text" @click="downloadTemplate">
          <el-icon><download /></el-icon>
          下载导入模板
        </el-button>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
        <el-button 
          v-if="!importing && !importResult" 
          type="primary" 
          @click="startImport"
          :disabled="!selectedFile"
        >
          开始导入
        </el-button>
        <el-button 
          v-if="importResult" 
          type="primary" 
          @click="closeDialog"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Download } from '@element-plus/icons-vue'
import type { UploadInstance, UploadProps } from 'element-plus'

interface ImportResult {
  success: boolean
  message: string
  totalRows?: number
  successRows?: number
  failedRows?: number
  errors?: Array<{
    row: number
    field: string
    message: string
  }>
}

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'import-success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | null>(null)
const importing = ref(false)
const importProgress = ref(0)
const progressText = ref('')
const importResult = ref<ImportResult | null>(null)

// 上传配置
const uploadAction = '/api/customers/import'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))
const uploadData = {
  type: 'customer'
}

// 文件上传前验证
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isValidType = /\.(xlsx|xls|csv)$/.test(file.name)
  const isValidSize = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('只能上传 Excel 或 CSV 文件！')
    return false
  }

  if (!isValidSize) {
    ElMessage.error('文件大小不能超过 10MB！')
    return false
  }

  selectedFile.value = file
  return false // 阻止自动上传
}

// 开始导入
const startImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要导入文件 "${selectedFile.value.name}" 吗？`,
      '确认导入',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    importing.value = true
    importProgress.value = 0
    progressText.value = '正在上传文件...'

    // 模拟上传进度
    const progressTimer = setInterval(() => {
      if (importProgress.value < 90) {
        importProgress.value += Math.random() * 20
        if (importProgress.value > 90) importProgress.value = 90
      }
    }, 200)

    // 模拟导入过程
    setTimeout(() => {
      clearInterval(progressTimer)
      importProgress.value = 100
      progressText.value = '导入完成'

      // 模拟导入结果
      setTimeout(() => {
        importing.value = false
        importResult.value = {
          success: true,
          message: '客户数据导入成功！',
          totalRows: 150,
          successRows: 148,
          failedRows: 2,
          errors: [
            { row: 23, field: '手机号', message: '手机号格式不正确' },
            { row: 67, field: '邮箱', message: '邮箱格式不正确' }
          ]
        }

        emit('import-success')
      }, 1000)
    }, 3000)

  } catch (error) {
    // 用户取消
  }
}

// 上传成功回调
const onUploadSuccess = (response: any, file: File) => {
  ElMessage.success('文件上传成功')
}

// 上传失败回调
const onUploadError = (error: any, file: File) => {
  ElMessage.error('文件上传失败')
}

// 上传进度回调
const onUploadProgress = (event: any, file: File) => {
  // 处理上传进度
}

// 下载模板
const downloadTemplate = () => {
  // 创建模板数据
  const templateData = [
    ['客户名称', '联系人', '手机号', '邮箱', '公司名称', '地址', '备注'],
    ['张三', '张三', '13800138000', 'zhangsan@example.com', '张三公司', '北京市朝阳区', '重要客户'],
    ['李四', '李四', '13800138001', 'lisi@example.com', '李四公司', '上海市浦东新区', '普通客户']
  ]

  // 转换为CSV格式
  const csvContent = templateData.map(row => row.join(',')).join('\n')
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  link.setAttribute('href', url)
  link.setAttribute('download', '客户导入模板.csv')
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 关闭对话框
const closeDialog = () => {
  visible.value = false
  // 重置状态
  selectedFile.value = null
  importing.value = false
  importProgress.value = 0
  progressText.value = ''
  importResult.value = null
}
</script>

<style scoped>
.import-content {
  padding: 20px 0;
}

.upload-section {
  margin-bottom: 24px;
}

.progress-section {
  margin-bottom: 24px;
}

.progress-text {
  text-align: center;
  margin-top: 8px;
  color: #606266;
  font-size: 14px;
}

.result-section {
  margin-bottom: 24px;
}

.result-details {
  margin-top: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item .label {
  color: #606266;
  font-size: 14px;
}

.detail-item .value {
  font-weight: 500;
  font-size: 14px;
}

.detail-item .value.success {
  color: #67c23a;
}

.detail-item .value.error {
  color: #f56c6c;
}

.error-list {
  margin-top: 16px;
}

.error-list h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

.template-section {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
}

:deep(.el-upload__tip) {
  margin-top: 8px;
}
</style>
