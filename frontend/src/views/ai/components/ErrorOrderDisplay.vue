<template>
  <div class="error-orders-container">
    <!-- 错误统计摘要 -->
    <div class="error-summary" v-if="errorOrders.length > 0">
      <el-alert 
        :title="`发现 ${errorOrders.length} 个错误订单`" 
        type="warning" 
        show-icon 
        :closable="false"
      >
        <template #default>
          <div class="error-stats">
            <el-tag v-for="(count, type) in errorTypeStats" :key="type" :type="getErrorTypeTagType(type)">
              {{ getErrorTypeLabel(type) }}: {{ count }}
            </el-tag>
          </div>
        </template>
      </el-alert>
    </div>

    <!-- 错误订单表格 -->
    <div class="error-table" v-if="errorOrders.length > 0">
      <div class="table-header">
        <h4>错误订单详情</h4>
        <div class="table-actions">
          <el-button size="small" @click="refreshErrorOrders">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button size="small" type="primary" @click="exportErrorOrders">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>
      </div>

      <!-- 动态表格 -->
      <el-table :data="errorOrders" border stripe>
        <el-table-column prop="excelRowNumber" label="行号" width="80" align="center" />
        
        <el-table-column prop="errorType" label="错误类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getErrorTypeTagType(row.errorType)">
              {{ row.errorTypeLabel }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 动态列：根据rawData动态生成 -->
        <template v-for="column in dynamicColumns" :key="column.key">
          <el-table-column :prop="column.key" :label="column.label" :width="column.width" :min-width="column.minWidth">
            <template #default="{ row }">
              <span :class="{ 'error-field': isErrorField(row, column.key) }">
                {{ formatFieldValue(row.rawData[column.key]) }}
              </span>
            </template>
          </el-table-column>
        </template>
        
        <el-table-column prop="errorMessage" label="错误详情" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.errorMessage" placement="top">
              <span class="error-message">{{ truncateText(row.errorMessage, 50) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column prop="suggestedAction" label="建议处理" width="150">
          <template #default="{ row }">
            <span class="suggested-action">{{ truncateText(row.suggestedAction, 30) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.statusLabel }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewErrorDetail(row)">
              查看详情
            </el-button>
            <el-button size="small" type="primary" @click="createOrderManually(row)">
              手动创建订单
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 无错误订单提示 -->
    <div class="no-errors" v-else>
      <el-empty description="暂无错误订单" />
    </div>

    <!-- 错误详情弹窗 -->
    <ErrorDetailDialog 
      v-model="detailDialogVisible"
      :error-detail="selectedErrorOrder"
      @create-order="handleCreateOrder"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download } from '@element-plus/icons-vue'
import type { ErrorOrderInfo } from '@/types/ai'
import ErrorDetailDialog from './ErrorDetailDialog.vue'

// Props
interface Props {
  taskId?: string
  errorOrders: ErrorOrderInfo[]
}

const props = withDefaults(defineProps<Props>(), {
  errorOrders: () => []
})

// Emits
const emit = defineEmits<{
  refresh: []
  createOrder: [errorOrder: ErrorOrderInfo]
}>()

// 响应式数据
const detailDialogVisible = ref(false)
const selectedErrorOrder = ref<ErrorOrderInfo | null>(null)

// 计算属性
const errorTypeStats = computed(() => {
  const stats: Record<string, number> = {}
  props.errorOrders.forEach(order => {
    const type = order.errorType
    stats[type] = (stats[type] || 0) + 1
  })
  return stats
})

const dynamicColumns = computed(() => {
  const allFields = new Set<string>()
  
  // 收集所有错误订单中的字段
  props.errorOrders.forEach(order => {
    if (order.rawData) {
      Object.keys(order.rawData).forEach(key => allFields.add(key))
    }
  })
  
  // 转换为列配置
  return Array.from(allFields)
    .filter(field => !['rowNumber', 'errorType', 'errorMessage', 'suggestedAction'].includes(field))
    .map(field => ({
      key: field,
      label: getFieldLabel(field),
      width: getFieldWidth(field),
      minWidth: 120
    }))
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

const getErrorTypeLabel = (errorType: string): string => {
  const labelMap: Record<string, string> = {
    'PARSE_ERROR': '解析错误',
    'VALIDATION_ERROR': '验证错误',
    'PRODUCT_NOT_FOUND': '商品未找到',
    'CUSTOMER_NOT_FOUND': '客户未找到',
    'DATA_FORMAT_ERROR': '数据格式错误',
    'BUSINESS_RULE_ERROR': '业务规则错误',
    'SYSTEM_ERROR': '系统错误',
    'INSUFFICIENT_STOCK': '库存不足',
    'PRICE_ERROR': '价格错误',
    'QUANTITY_ERROR': '数量错误'
  }
  return labelMap[errorType] || errorType
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
    'remarks': '备注'
  }
  
  return labelMap[field] || field.replace(/([A-Z])/g, ' $1').trim()
}

const getFieldWidth = (field: string): number => {
  const widthMap: Record<string, number> = {
    'customerName': 150,
    'customerCode': 120,
    'productSku': 120,
    'productName': 200,
    'quantity': 80,
    'unitPrice': 100,
    'totalPrice': 100,
    'deliveryAddress': 200,
    'contactPerson': 100,
    'contactPhone': 120,
    'expectedDeliveryDate': 120,
    'remarks': 150
  }
  
  return widthMap[field] || 120
}

const formatFieldValue = (value: any): string => {
  if (value == null || value === '') return '-'
  if (typeof value === 'string') return value
  if (typeof value === 'number') return value.toString()
  if (typeof value === 'boolean') return value ? '是' : '否'
  return JSON.stringify(value)
}

const isErrorField = (row: ErrorOrderInfo, fieldKey: string): boolean => {
  // 根据错误类型判断哪些字段有问题
  const errorFieldMap: Record<string, string[]> = {
    'PRODUCT_NOT_FOUND': ['productSku', 'productName'],
    'CUSTOMER_NOT_FOUND': ['customerName', 'customerCode'],
    'DATA_FORMAT_ERROR': ['quantity', 'unitPrice', 'totalPrice'],
    'PRICE_ERROR': ['unitPrice', 'totalPrice'],
    'QUANTITY_ERROR': ['quantity']
  }
  
  const errorFields = errorFieldMap[row.errorType] || []
  return errorFields.includes(fieldKey)
}

const truncateText = (text: string, maxLength: number): string => {
  if (!text) return '-'
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const refreshErrorOrders = () => {
  emit('refresh')
}

const exportErrorOrders = () => {
  ElMessage.info('导出功能开发中...')
}

const viewErrorDetail = (errorOrder: ErrorOrderInfo) => {
  selectedErrorOrder.value = errorOrder
  detailDialogVisible.value = true
}

const createOrderManually = (errorOrder: ErrorOrderInfo) => {
  emit('createOrder', errorOrder)
}

const handleCreateOrder = (errorOrder: ErrorOrderInfo) => {
  emit('createOrder', errorOrder)
  detailDialogVisible.value = false
}

// 生命周期
onMounted(() => {
  console.log('🚀 ErrorOrderDisplay 组件已挂载')
  console.log('📋 错误订单数量:', props.errorOrders.length)
})
</script>

<style scoped>
.error-orders-container {
  width: 100%;
}

.error-summary {
  margin-bottom: 20px;
}

.error-stats {
  margin-top: 8px;
}

.error-stats .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.error-table {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-header h4 {
  margin: 0;
  color: #303133;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.error-field {
  color: #f56c6c;
  font-weight: 500;
}

.error-message {
  color: #f56c6c;
  cursor: pointer;
}

.suggested-action {
  color: #409eff;
  font-style: italic;
}

.no-errors {
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .table-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
