<template>
  <el-dialog
    v-model="visible"
    title="批量导入订单"
    width="600px"
    :before-close="handleClose"
    destroy-on-close
    :close-on-click-modal="false"
    :close-on-press-escape="false"
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
        :disabled="importing"
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
            <p>第一行：客户编码 | 客户名称 | 联系人 | 联系电话 | 地址 | 商品SKU | 数量 | 单价 | 预计交货日期</p>
            <p>客户编码：必填，必须是系统中已存在的客户编码</p>
            <p>商品SKU：必填，必须是系统中已存在的商品SKU</p>
            <p>数量：必填，必须大于0的整数</p>
            <p>单价：必填，必须大于等于0的数字</p>
            <p>预计交货日期：可选，格式：YYYY-MM-DD</p>
          </template>
        </el-alert>
      </div>

      <!-- 模板选择 -->
      <div class="template-selection">
        <h4>订单模板</h4>
        <el-select
          v-model="selectedTemplateId"
          placeholder="请选择订单模板（可选）"
          clearable
          style="width: 100%"
          :loading="templateLoading"
          :disabled="templateLoading || importing"
        >
          <el-option
            v-for="template in templateOptions"
            :key="template.id"
            :label="template.templateName"
            :value="template.id"
          >
            <span>{{ template.templateName }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              v{{ template.version }}
            </span>
          </el-option>
        </el-select>
        <div class="form-tip">选择模板可以更好地处理自定义字段映射</div>
      </div>

      <!-- 导入进度 -->
      <div v-if="importProgress.show" class="import-progress">
        <h4>导入进度</h4>
        <el-progress
          :percentage="importProgress.percentage"
          :status="importProgress.status"
          :stroke-width="8"
        />
        <div class="progress-details">
          <div class="progress-item">
            <span class="label">总数:</span>
            <span class="value">{{ importProgress.total }}</span>
          </div>
          <div class="progress-item">
            <span class="label">成功:</span>
            <span class="value success">{{ importProgress.success }}</span>
          </div>
          <div class="progress-item">
            <span class="label">失败:</span>
            <span class="value error">{{ importProgress.failed }}</span>
          </div>
          <div class="progress-item">
            <span class="label">跳过:</span>
            <span class="value warning">{{ importProgress.skipped }}</span>
          </div>
        </div>
        <div v-if="importProgress.message" class="progress-message" :class="importProgress.status">
          {{ importProgress.message }}
        </div>
      </div>

      <!-- 错误信息 -->
      <div v-if="errorDetails.length > 0" class="error-details">
        <h4>错误详情 ({{ errorDetails.length }})</h4>
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

      <!-- 成功统计 -->
      <div v-if="importProgress.status === 'success' && importProgress.success > 0" class="success-summary">
        <el-alert
          :title="`成功导入 ${importProgress.success} 个订单`"
          type="success"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>导入完成！您可以查看订单列表确认导入结果。</p>
            <p v-if="importProgress.failed > 0">有 {{ importProgress.failed }} 个订单导入失败，请查看错误详情。</p>
          </template>
        </el-alert>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" :disabled="importing">取消</el-button>
        <el-button
          v-if="!importProgress.show"
          type="primary"
          @click="startImport"
          :disabled="!fileList.length || importing"
          :loading="importing"
        >
          开始导入
        </el-button>
        <el-button
          v-else-if="importProgress.status === 'active'"
          type="danger"
          @click="cancelImport"
        >
          取消导入
        </el-button>
        <el-button
          v-else
          type="success"
          @click="handleClose"
        >
          完成
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { UploadProps, UploadUserFile } from 'element-plus'
import { orderTemplateApi } from '@/api/orderTemplate'
import type { OrderTemplateResponse } from '@/types/orderTemplate'

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
const importing = ref(false)
const templateLoading = ref(false)
const selectedTemplateId = ref<number>()
const templateOptions = ref<OrderTemplateResponse[]>([])

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

// 生命周期
onMounted(() => {
  loadTemplateOptions()
})

// 加载模板选项
const loadTemplateOptions = async () => {
  try {
    templateLoading.value = true
    const response = await orderTemplateApi.getAllTemplates()
    templateOptions.value = response || []
  } catch (error) {
    console.error('加载模板列表失败:', error)
    ElMessage.warning('加载模板列表失败，将使用默认导入方式')
  } finally {
    templateLoading.value = false
  }
}

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
    importing.value = true
    importProgress.show = true
    importProgress.status = 'active'
    importProgress.message = '正在处理文件...'
    importProgress.percentage = 10

    // 模拟文件解析过程
    await new Promise(resolve => setTimeout(resolve, 1000))
    importProgress.percentage = 30
    importProgress.message = '正在验证数据...'

    // 模拟数据验证过程
    await new Promise(resolve => setTimeout(resolve, 1500))
    importProgress.percentage = 60
    importProgress.message = '正在导入数据...'

    // 模拟数据导入过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 模拟导入结果
    importProgress.total = 100
    importProgress.success = 85
    importProgress.failed = 10
    importProgress.skipped = 5
    importProgress.percentage = 100
    importProgress.status = 'success'
    importProgress.message = '导入完成'
    
    // 模拟错误详情
    errorDetails.value = [
      {
        row: 15,
        message: '客户编码不存在',
        data: 'CUST001,客户A,联系人,13800138000,地址,SKU001,10,100.00,2024-02-01',
        suggestion: '请检查客户编码是否正确，或先创建该客户'
      },
      {
        row: 23,
        message: '商品SKU不存在',
        data: 'CUST002,客户B,联系人,13800138001,地址,SKU999,5,50.00,2024-02-02',
        suggestion: '请检查商品SKU是否正确，或先创建该商品'
      }
    ]
    
    ElMessage.success('订单导入完成')
    
    // 延迟关闭对话框
    setTimeout(() => {
      emit('success')
      visible.value = false
    }, 3000)
  } catch (error) {
    importProgress.status = 'exception'
    importProgress.message = '导入失败'
    ElMessage.error('订单导入失败')
    console.error('导入失败:', error)
  } finally {
    importing.value = false
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
    importing.value = false
  } catch (error) {
    // 用户取消
  }
}

// 关闭对话框
const handleClose = () => {
  if (importing.value) {
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

.template-selection {
  margin-bottom: 20px;
}

.template-selection h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
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
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 15px;
}

.progress-item {
  text-align: center;
  padding: 8px;
  background: white;
  border-radius: 4px;
}

.progress-item .label {
  display: block;
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.progress-item .value {
  display: block;
  font-size: 18px;
  font-weight: bold;
}

.progress-item .value.success {
  color: #67c23a;
}

.progress-item .value.error {
  color: #f56c6c;
}

.progress-item .value.warning {
  color: #e6a23c;
}

.progress-message {
  margin-top: 15px;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
  text-align: center;
}

.progress-message.active {
  background: #e1f3d8;
  color: #67c23a;
}

.progress-message.success {
  background: #e1f3d8;
  color: #67c23a;
}

.progress-message.exception {
  background: #fef0f0;
  color: #f56c6c;
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

.success-summary {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-upload-dragger) {
  width: 100%;
}

:deep(.el-collapse-item__header) {
  font-size: 14px;
  font-weight: 500;
}
</style>
