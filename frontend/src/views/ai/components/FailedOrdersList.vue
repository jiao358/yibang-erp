<template>
  <div class="failed-orders-list">
            <!-- 标题和统计 -->
        <div class="list-header">
          <h4>失败订单列表</h4>
          <div class="list-stats">
            <el-tag type="danger">{{ totalFailedOrders }} 个失败订单</el-tag>
          </div>
        </div>
        
        <!-- 功能说明 -->
        <div class="function-description">
          <el-alert
            title="手动添加功能说明"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p><strong>手动添加订单</strong>：当Excel中的某些行数据因为格式错误、数据缺失等原因处理失败时，</p>
              <p>用户可以点击"手动添加"按钮，跳转到订单管理模块手动创建订单。</p>
              <p>这样可以避免重新上传Excel文件，直接在系统中创建订单。</p>
            </template>
          </el-alert>
        </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 失败订单列表 -->
    <div v-else-if="failedOrders.length > 0" class="orders-container">
      <el-table :data="failedOrders" style="width: 100%" class="failed-orders-table">
        <el-table-column prop="excelRowNumber" label="Excel行号" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">第{{ (row.excelRowNumber || 0) + 1 }}行</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="errorType" label="错误类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getErrorTypeTagType(row.errorType)" size="small">
              {{ getErrorTypeText(row.errorType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="errorMessage" label="错误信息" min-width="200">
          <template #default="{ row }">
            <div class="error-message">
              <el-icon class="error-icon"><CircleClose /></el-icon>
              <span>{{ row.errorMessage }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="suggestedAction" label="处理建议" min-width="150">
          <template #default="{ row }">
            <div class="suggested-action">
              <el-icon class="suggestion-icon"><InfoFilled /></el-icon>
              <span>{{ row.suggestedAction || '暂无建议' }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="rawData" label="原始数据" min-width="200">
          <template #default="{ row }">
            <el-button 
              type="text" 
              size="small" 
              @click="viewRawData(row)"
            >
              查看数据
            </el-button>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="manualAddOrder(row)"
            >
              手动添加
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" v-if="totalFailedOrders > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalFailedOrders"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <el-empty description="暂无失败订单" />
    </div>

    <!-- 原始数据查看弹窗 -->
    <el-dialog
      v-model="rawDataDialogVisible"
      title="原始数据详情"
      width="600px"
      :close-on-click-modal="true"
    >
      <div v-if="selectedRawData" class="raw-data-content">
        <pre>{{ formatRawData(selectedRawData) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleClose, InfoFilled } from '@element-plus/icons-vue'
import { aiExcelImportApi } from '@/api/aiExcelImport'

// Props
interface Props {
  taskId: string
  autoLoad?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  autoLoad: true
})

// Emits
const emit = defineEmits<{
  manualAddOrder: [orderId: number]
  refresh: []
}>()

// 响应式数据
const loading = ref(false)
const failedOrders = ref<any[]>([])
const totalFailedOrders = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const rawDataDialogVisible = ref(false)
const selectedRawData = ref<any>(null)

// 加载失败订单列表
const loadFailedOrders = async () => {
  if (!props.taskId) return
  
  try {
    loading.value = true
    console.log('📋 开始加载失败订单列表，任务ID:', props.taskId)
    
    const response = await aiExcelImportApi.getFailedOrders(props.taskId, {
      page: currentPage.value,
      size: pageSize.value,
      sortBy: 'excelRowNumber',
      sortOrder: 'asc'
    })
    
    if (response && response.content) {
      failedOrders.value = response.content
      totalFailedOrders.value = response.totalElements
      console.log('✅ 失败订单列表加载成功，总数:', response.totalElements)
    } else {
      failedOrders.value = []
      totalFailedOrders.value = 0
      console.warn('⚠️ 失败订单列表响应格式异常:', response)
    }
  } catch (error: any) {
    console.error('❌ 加载失败订单列表失败:', error)
    ElMessage.error(`加载失败订单列表失败: ${error.message || '未知错误'}`)
    failedOrders.value = []
    totalFailedOrders.value = 0
  } finally {
    loading.value = false
  }
}

// 查看原始数据
const viewRawData = (order: any) => {
  selectedRawData.value = order.rawData
  rawDataDialogVisible.value = true
}

// 格式化原始数据
const formatRawData = (rawData: string) => {
  try {
    if (typeof rawData === 'string') {
      const parsed = JSON.parse(rawData)
      return JSON.stringify(parsed, null, 2)
    }
    return JSON.stringify(rawData, null, 2)
  } catch {
    return rawData
  }
}

// 手动添加订单
const manualAddOrder = async (order: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要手动添加第${(order.excelRowNumber || 0) + 1}行的订单吗？\n将跳转到订单管理模块进行手动创建。`,
      '确认手动添加',
      {
        confirmButtonText: '确定跳转',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    // 跳转到订单管理模块
    window.open('/order', '_blank')
    emit('manualAddOrder', order.id)
    ElMessage.success('正在跳转到订单管理模块...')
  } catch {
    // 用户取消
  }
}

// 分页处理
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadFailedOrders()
}

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  loadFailedOrders()
}

// 错误类型相关方法
const getErrorTypeTagType = (errorType: string) => {
  const typeMap: Record<string, string> = {
    'VALIDATION_ERROR': 'warning',
    'DATA_FORMAT_ERROR': 'danger',
    'PRODUCT_NOT_FOUND': 'info',
    'CUSTOMER_NOT_FOUND': 'info',
    'SYSTEM_ERROR': 'danger'
  }
  return typeMap[errorType] || 'info'
}

const getErrorTypeText = (errorType: string) => {
  const textMap: Record<string, string> = {
    'VALIDATION_ERROR': '数据验证错误',
    'DATA_FORMAT_ERROR': '数据格式错误',
    'PRODUCT_NOT_FOUND': '产品未找到',
    'CUSTOMER_NOT_FOUND': '客户未找到',
    'SYSTEM_ERROR': '系统错误'
  }
  return textMap[errorType] || errorType
}

// 状态相关方法
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'PENDING': 'info',
    'PROCESSING': 'warning',
    'PROCESSED': 'success',
    'IGNORED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'PROCESSED': '已处理',
    'IGNORED': '已忽略'
  }
  return textMap[status] || status
}

// 监听任务ID变化
watch(() => props.taskId, (newTaskId) => {
  if (newTaskId && props.autoLoad) {
    currentPage.value = 1
    loadFailedOrders()
  }
})

// 暴露方法给父组件
const refresh = () => {
  loadFailedOrders()
}

defineExpose({
  refresh,
  loadFailedOrders
})

// 生命周期
onMounted(() => {
  if (props.taskId && props.autoLoad) {
    loadFailedOrders()
  }
})
</script>

<style scoped>
.failed-orders-list {
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.function-description {
  margin-bottom: 20px;
}

.function-description .el-alert {
  margin-bottom: 0;
}

.function-description p {
  margin: 4px 0;
  line-height: 1.5;
}

.list-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.list-stats {
  display: flex;
  gap: 12px;
}

.loading-container {
  padding: 40px 0;
}

.orders-container {
  margin-bottom: 20px;
}

.failed-orders-table {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
}

.error-icon {
  color: #f56c6c;
}

.suggested-action {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
}

.suggestion-icon {
  color: #67c23a;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.empty-container {
  padding: 60px 0;
  text-align: center;
}

.raw-data-content {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.raw-data-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
}
</style>
