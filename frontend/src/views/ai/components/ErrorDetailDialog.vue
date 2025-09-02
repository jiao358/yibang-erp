<template>
  <el-dialog 
    v-model="dialogVisible" 
    title="错误详情" 
    width="800px"
    :before-close="handleClose"
  >
    <div class="error-detail" v-if="errorDetail">
      <!-- 基本信息 -->
              <el-descriptions :column="2" border>
          <el-descriptions-item label="行号">{{ errorDetail.excelRowNumber }}</el-descriptions-item>
        <el-descriptions-item label="错误类型">
          <el-tag :type="getErrorTypeTagType(errorDetail.errorType)">
            {{ errorDetail.errorTypeLabel }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误详情" :span="2">
          <div class="error-message-detail">{{ errorDetail.errorMessage }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="建议处理方式" :span="2">
          <div class="suggested-action-detail">{{ errorDetail.suggestedAction }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider>原始数据</el-divider>
      
      <!-- 动态展示原始数据 -->
      <div class="raw-data-section">
        <el-row :gutter="16">
          <el-col 
            :span="12" 
            v-for="(value, key) in errorDetail.rawData" 
            :key="key"
            class="data-item-col"
          >
            <div class="data-item">
              <label class="data-label">{{ getFieldLabel(key) }}:</label>
              <span 
                class="data-value"
                :class="{ 'error-value': isErrorValue(key, value) }"
              >
                {{ formatValue(value) }}
              </span>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <el-divider>处理状态</el-divider>
      
      <!-- 处理状态信息 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusTagType(errorDetail.status)">
            {{ errorDetail.statusLabel }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(errorDetail.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" v-if="errorDetail.updatedAt">
          {{ formatDateTime(errorDetail.updatedAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="errorDetail.processedBy">
          {{ errorDetail.processedByName || errorDetail.processedBy }}
        </el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="errorDetail.processedAt">
          {{ formatDateTime(errorDetail.processedAt) }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button 
          type="primary" 
          @click="handleCreateOrder"
          :disabled="errorDetail?.status === 'PROCESSED'"
        >
          手动创建订单
        </el-button>
        <el-button 
          type="warning" 
          @click="handleMarkAsProcessed"
          v-if="errorDetail?.status === 'PENDING'"
        >
          标记为已处理
        </el-button>
        <el-button 
          type="info" 
          @click="handleMarkAsIgnored"
          v-if="errorDetail?.status === 'PENDING'"
        >
          标记为已忽略
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { ErrorOrderInfo } from '@/types/ai'

// Props
interface Props {
  modelValue: boolean
  errorDetail: ErrorOrderInfo | null
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  createOrder: [errorOrder: ErrorOrderInfo]
  markAsProcessed: [errorOrderId: number]
  markAsIgnored: [errorOrderId: number]
}>()

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 方法
const getErrorTypeTagType = (errorType: string): string => {
  const typeMap: Record<string, string> = {
    'PARSE_ERROR': 'danger',
    'VALIDATION_ERROR': 'warning',
    'PRODUCT_NOT_FOUND': 'info',
    'CUSTOMER_NOT_FOUND': 'info',
    'DATA_FORMAT_ERROR': 'warning',
    'BUSINESS_RULE_ERROR': 'danger',
    'SYSTEM_ERROR': 'danger',
    'INSUFFICIENT_STOCK': 'warning',
    'PRICE_ERROR': 'warning',
    'QUANTITY_ERROR': 'warning'
  }
  return typeMap[errorType] || 'info'
}

const getStatusTagType = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PENDING': 'warning',
    'PROCESSED': 'success',
    'IGNORED': 'info'
  }
  return statusMap[status] || 'info'
}

const getFieldLabel = (field: string): string => {
  const labelMap: Record<string, string> = {
    'customerName': '客户名称',
    'customerCode': '客户编码',
    'productSku': '商品SKU',
    'productName': '商品名称',
    'quantity': '数量',
    'unitPrice': '单价',
    'totalPrice': '总价',
    'deliveryAddress': '交货地址',
    'contactPerson': '联系人',
    'contactPhone': '联系电话',
    'expectedDeliveryDate': '预计交货日期',
    'remarks': '备注',
            'excelRowNumber': '行号',
        'matchReason': '匹配原因'
  }
  
  return labelMap[field] || field.replace(/([A-Z])/g, ' $1').trim()
}

const isErrorValue = (fieldKey: string, value: any): boolean => {
  if (!props.errorDetail) return false
  
  // 根据错误类型判断哪些字段有问题
  const errorFieldMap: Record<string, string[]> = {
    'PRODUCT_NOT_FOUND': ['productSku', 'productName'],
    'CUSTOMER_NOT_FOUND': ['customerName', 'customerCode'],
    'DATA_FORMAT_ERROR': ['quantity', 'unitPrice', 'totalPrice'],
    'PRICE_ERROR': ['unitPrice', 'totalPrice'],
    'QUANTITY_ERROR': ['quantity']
  }
  
  const errorFields = errorFieldMap[props.errorDetail.errorType] || []
  return errorFields.includes(fieldKey)
}

const formatValue = (value: any): string => {
  if (value == null || value === '') return '-'
  if (typeof value === 'string') return value
  if (typeof value === 'number') return value.toString()
  if (typeof value === 'boolean') return value ? '是' : '否'
  if (Array.isArray(value)) return value.join(', ')
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  return String(value)
}

const formatDateTime = (dateTime: string | Date | null): string => {
  if (!dateTime) return '-'
  
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch {
    return String(dateTime)
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

const handleCreateOrder = () => {
  if (props.errorDetail) {
    emit('createOrder', props.errorDetail)
    ElMessage.success('准备创建订单，请填写订单信息')
  }
}

const handleMarkAsProcessed = () => {
  if (props.errorDetail) {
    emit('markAsProcessed', props.errorDetail.id)
    ElMessage.success('已标记为已处理')
  }
}

const handleMarkAsIgnored = () => {
  if (props.errorDetail) {
    emit('markAsIgnored', props.errorDetail.id)
    ElMessage.success('已标记为已忽略')
  }
}

// 监听器
watch(() => props.errorDetail, (newDetail) => {
  if (newDetail) {
    console.log('📋 显示错误详情:', newDetail)
  }
})
</script>

<style scoped>
.error-detail {
  max-height: 600px;
  overflow-y: auto;
}

.error-message-detail {
  color: #f56c6c;
  font-weight: 500;
  line-height: 1.5;
  padding: 8px;
  background-color: #fef0f0;
  border-radius: 4px;
}

.suggested-action-detail {
  color: #409eff;
  font-style: italic;
  line-height: 1.5;
  padding: 8px;
  background-color: #f0f9ff;
  border-radius: 4px;
}

.raw-data-section {
  margin: 16px 0;
}

.data-item-col {
  margin-bottom: 16px;
}

.data-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.data-label {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.data-value {
  color: #303133;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-word;
}

.data-value.error-value {
  color: #f56c6c;
  font-weight: 500;
  background-color: #fef0f0;
  padding: 4px 8px;
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .data-item-col {
    :span="24"
  }
  
  .dialog-footer {
    flex-direction: column;
    align-items: stretch;
  }
  
  .dialog-footer .el-button {
    margin-bottom: 8px;
  }
}
</style>
