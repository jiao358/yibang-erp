<template>
  <div class="file-upload-container">
    <!-- 使用隐藏的文件输入框 -->
    <input
      ref="fileInput"
      type="file"
      accept=".xlsx,.xls"
      style="display: none"
      @change="handleFileInputChange"
    />
    
    <!-- 自定义上传区域 -->
    <div class="upload-area" @click="triggerFileSelect">
      <el-icon class="upload-icon">
        <UploadFilled />
      </el-icon>
      
      <div class="upload-text">
        <div class="primary-text">将Excel文件拖到此处，或<em>点击上传</em></div>
        <div class="secondary-text">支持 .xlsx 和 .xls 格式，文件大小不超过 10MB</div>
      </div>
    </div>

    <!-- 文件信息展示 -->
    <div class="file-info" v-if="selectedFile">
      <div class="file-card">
        <div class="file-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="file-details">
          <div class="file-name">{{ selectedFile.name }}</div>
          <div class="file-meta">
            <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
            <span class="file-type">{{ getFileType(selectedFile.name) }}</span>
            <span class="file-date">{{ formatDate(selectedFile.lastModified) }}</span>
          </div>
        </div>
        <div class="file-actions">
          <el-button 
            type="danger" 
            size="small" 
            @click="removeFile"
          >
            移除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 上传提示 -->
    <div class="upload-tips">
      <el-alert
        title="上传说明"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <ul class="tips-list">
            <li>请确保Excel文件包含必要的列：客户信息、商品信息、数量、价格等</li>
            <li>系统将使用AI智能识别列标题和内容含义</li>
            <li>支持的文件大小：最大 10MB</li>
            <li>建议使用 .xlsx 格式以获得最佳兼容性</li>
          </ul>
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Document } from '@element-plus/icons-vue'

// Props
interface Props {
  maxSize?: number // 最大文件大小（MB）
}

const props = withDefaults(defineProps<Props>(), {
  maxSize: 10
})

// Emits
const emit = defineEmits<{
  fileSelected: [file: File]
  uploadSuccess: [response: any]
  uploadError: [error: string]
}>()

// 响应式数据
const selectedFile = ref<File | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

// 方法
const triggerFileSelect = () => {
  console.log('🖱️ 点击上传区域，触发文件选择')
  if (fileInput.value) {
    fileInput.value.click()
  } else {
    console.error('❌ fileInput ref 未找到')
  }
}

const handleFileInputChange = (event: Event) => {
  console.log('📁 文件输入框变化事件触发')
  const target = event.target as HTMLInputElement
  const files = target.files
  
  if (files && files.length > 0) {
    const file = files[0]
    console.log('📁 选择的文件:', file)
    
    // 验证文件
    if (validateFile(file)) {
      // 设置选中的文件
      selectedFile.value = file
      console.log('📁 已设置selectedFile:', selectedFile.value)
      
      // 触发文件选择事件
      console.log('📤 准备触发 fileSelected 事件...')
      emit('fileSelected', file)
      console.log('📤 fileSelected 事件已触发')
      
      // 模拟上传成功
      console.log('⏱️ 准备模拟上传成功...')
      setTimeout(() => {
        console.log('📤 触发 uploadSuccess 事件')
        emit('uploadSuccess', { success: true, message: '文件选择成功' })
      }, 500)
    }
  }
  
  // 清空input值，允许重复选择同一文件
  if (target) {
    target.value = ''
  }
}

const validateFile = (file: File): boolean => {
  console.log('🔍 开始验证文件:', file.name)
  
  // 检查文件类型
  const isValidType = /\.(xlsx|xls)$/i.test(file.name)
  console.log('📋 文件类型检查结果:', { fileName: file.name, isValidType })
  
  if (!isValidType) {
    console.error('❌ 文件类型验证失败')
    ElMessage.error('只能上传 Excel 文件！')
    return false
  }

  // 检查文件大小
  const fileSizeMB = file.size / 1024 / 1024
  const isValidSize = fileSizeMB < props.maxSize
  console.log('📏 文件大小检查结果:', { 
    fileSizeBytes: file.size, 
    fileSizeMB: fileSizeMB.toFixed(2), 
    maxSizeMB: props.maxSize, 
    isValidSize 
  })
  
  if (!isValidSize) {
    console.error('❌ 文件大小验证失败')
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB！`)
    return false
  }

  console.log('✅ 文件验证通过')
  return true
}

const removeFile = async () => {
  console.log('🗑️ removeFile 被调用')
  try {
    console.log('📝 显示确认对话框...')
    await ElMessageBox.confirm(
      '确定要移除已选择的文件吗？',
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    console.log('✅ 用户确认移除，清空selectedFile')
    selectedFile.value = null
    ElMessage.success('文件已移除')
    console.log('📁 selectedFile已清空:', selectedFile.value)
  } catch (error) {
    console.log('❌ 用户取消移除或发生错误:', error)
    // 用户取消
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileType = (fileName: string): string => {
  const extension = fileName.split('.').pop()?.toLowerCase()
  return extension === 'xlsx' ? 'Excel 2007+' : 'Excel 97-2003'
}

const formatDate = (timestamp: number): string => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 组件挂载时的调试信息
onMounted(() => {
  console.log('🚀 FileUpload 组件已挂载')
  console.log('📋 组件Props:', props)
  console.log('📋 组件Emits:', {
    fileSelected: 'function',
    uploadSuccess: 'function',
    uploadError: 'function'
  })
  console.log('📋 当前selectedFile状态:', selectedFile.value)
  
  // 检查DOM元素
  console.log('📋 fileInput ref:', fileInput.value)
  
  // 检查事件处理器绑定状态
  console.log('📋 事件处理器绑定状态:', {
    triggerFileSelect: typeof triggerFileSelect,
    handleFileInputChange: typeof handleFileInputChange,
    validateFile: typeof validateFile
  })
})
</script>

<style scoped>
.file-upload-container {
  width: 100%;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  background-color: #fafafa;
  transition: all 0.3s;
  cursor: pointer;
}

.upload-area:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.upload-area:hover .upload-icon {
  color: #409eff;
}

.upload-text {
  text-align: center;
}

.primary-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.primary-text em {
  color: #409eff;
  font-style: normal;
  font-weight: 600;
}

.secondary-text {
  font-size: 14px;
  color: #909399;
}

.file-info {
  margin-top: 20px;
}

.file-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.file-icon {
  margin-right: 16px;
  font-size: 24px;
  color: #409eff;
}

.file-details {
  flex: 1;
}

.file-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.file-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #909399;
}

.file-actions {
  margin-left: 16px;
}

.upload-tips {
  margin-top: 20px;
}

.tips-list {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.tips-list li {
  margin-bottom: 4px;
  line-height: 1.5;
}
</style>
