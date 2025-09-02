<template>
  <div class="product-image-upload">
    <!-- 图片上传区域 -->
    <div class="upload-section">
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :before-upload="beforeUpload"
        :file-list="fileList"
        accept="image/*"
        :limit="maxCount"
        multiple
        list-type="picture-card"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将图片拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 jpg/png/gif/webp 文件，且不超过 10MB
          </div>
        </template>
      </el-upload>
    </div>

    <!-- 已上传的图片列表 -->
    <div v-if="imageList.length > 0" class="image-list">
      <h4>已上传的图片</h4>
      <div class="image-grid">
        <div 
          v-for="image in imageList" 
          :key="image.id" 
          class="image-item"
          :class="{ 'primary': image.isPrimary }"
        >
          <img :src="image.imageUrl" :alt="image.imageName" />
          <div class="image-overlay">
            <div class="image-actions">
              <el-button 
                v-if="!image.isPrimary"
                type="primary" 
                size="small" 
                @click="setPrimary(image.id)"
              >
                设为主图
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteImage(image.id)"
              >
                删除
              </el-button>
            </div>
            <div v-if="image.isPrimary" class="primary-badge">主图</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button 
        type="primary" 
        :loading="uploading"
        :disabled="fileList.length === 0"
        @click="uploadImages"
      >
        上传图片
      </el-button>
      <el-button 
        v-if="imageList.length > 0"
        type="danger" 
        @click="clearAllImages"
      >
        清空所有图片
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { productImageApi } from '@/api/productImage'
import type { ProductImageResponse } from '@/types/productImage'
import type { UploadFile, UploadFiles } from 'element-plus'

interface Props {
  productId: number
  maxCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  maxCount: 10
})

const emit = defineEmits<{
  'upload-success': [images: ProductImageResponse[]]
  'image-deleted': [imageId: number]
  'primary-changed': [imageId: number]
}>()

// 响应式数据
const uploadRef = ref()
const fileList = ref<UploadFile[]>([])
const imageList = ref<ProductImageResponse[]>([])
const uploading = ref(false)

// 文件上传前的验证
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

// 文件选择变化
const handleFileChange = (file: UploadFile, files: UploadFiles) => {
  fileList.value = files
}

// 文件移除
const handleFileRemove = (file: UploadFile, files: UploadFiles) => {
  fileList.value = files
}

// 上传图片
const uploadImages = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的图片')
    return
  }

  try {
    uploading.value = true
    
    const files = fileList.value.map(file => file.raw!).filter(file => file)
    const isPrimary = imageList.value.length === 0 // 如果没有图片，第一张设为主图
    const sortOrder = imageList.value.length

    const responses = await productImageApi.uploadImages(files, props.productId, isPrimary, sortOrder)
    
    // 添加到图片列表
    imageList.value.push(...responses)
    
    // 清空文件列表
    fileList.value = []
    uploadRef.value?.clearFiles()
    
    ElMessage.success(`成功上传 ${responses.length} 张图片`)
    emit('upload-success', responses)
    
  } catch (error) {
    console.error('上传图片失败:', error)
    ElMessage.error('上传图片失败')
  } finally {
    uploading.value = false
  }
}

// 设置主图
const setPrimary = async (imageId: number) => {
  try {
    await productImageApi.setPrimaryImage(imageId)
    
    // 更新本地状态
    imageList.value.forEach(image => {
      image.isPrimary = image.id === imageId
    })
    
    ElMessage.success('设置主图成功')
    emit('primary-changed', imageId)
    
  } catch (error) {
    console.error('设置主图失败:', error)
    ElMessage.error('设置主图失败')
  }
}

// 删除图片
const deleteImage = async (imageId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？', '确认删除', {
      type: 'warning'
    })
    
    await productImageApi.deleteImage(imageId)
    
    // 从列表中移除
    const index = imageList.value.findIndex(image => image.id === imageId)
    if (index > -1) {
      imageList.value.splice(index, 1)
    }
    
    ElMessage.success('删除图片成功')
    emit('image-deleted', imageId)
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除图片失败:', error)
      ElMessage.error('删除图片失败')
    }
  }
}

// 清空所有图片
const clearAllImages = async () => {
  try {
    await ElMessageBox.confirm('确定要删除所有图片吗？', '确认删除', {
      type: 'warning'
    })
    
    await productImageApi.deleteProductImages(props.productId)
    imageList.value = []
    
    ElMessage.success('清空所有图片成功')
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空图片失败:', error)
      ElMessage.error('清空图片失败')
    }
  }
}

// 加载商品图片
const loadProductImages = async () => {
  try {
    const images = await productImageApi.getProductImages(props.productId)
    imageList.value = images
  } catch (error) {
    console.error('加载商品图片失败:', error)
  }
}

// 监听商品ID变化
watch(() => props.productId, (newProductId) => {
  if (newProductId) {
    loadProductImages()
  }
}, { immediate: true })

// 组件挂载时加载图片
onMounted(() => {
  if (props.productId) {
    loadProductImages()
  }
})
</script>

<style scoped>
.product-image-upload {
  padding: 20px;
}

.upload-section {
  margin-bottom: 20px;
}

.image-list {
  margin-bottom: 20px;
}

.image-list h4 {
  margin-bottom: 15px;
  color: #606266;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.image-item {
  position: relative;
  border: 2px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.3s;
}

.image-item.primary {
  border-color: #409eff;
}

.image-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.image-actions {
  display: flex;
  gap: 8px;
}

.primary-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
}

:deep(.el-upload-list--picture-card) {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

:deep(.el-upload--picture-card) {
  width: 148px;
  height: 148px;
}
</style>
