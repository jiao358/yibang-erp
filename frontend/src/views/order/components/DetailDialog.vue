<template>
  <el-dialog
    v-model="dialogVisible"
    title="预警详情"
    width="800px"
    :before-close="handleClose"
  >
    <div v-if="alert" class="alert-detail">
      <!-- 基本信息 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-title">基本信息</div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预警ID">{{ alert.id }}</el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ alert.orderId }}</el-descriptions-item>
          <el-descriptions-item label="源订单ID">{{ alert.sourceOrderId }}</el-descriptions-item>
          <el-descriptions-item label="处理类型">
            <el-tag :type="getProcessingTypeTagType(alert.processingType)">
              {{ alert.processingTypeDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(alert.status)">
              {{ alert.statusDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(alert.priority)">
              {{ alert.priorityDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ alert.createdByName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(alert.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="分配给">{{ alert.assignedToName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ alert.processedByName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">
            {{ alert.processedAt ? formatDateTime(alert.processedAt) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(alert.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 处理原因 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-title">处理原因</div>
        </template>
        <div class="reason-content">{{ alert.processingReason }}</div>
      </el-card>

      <!-- 处理备注 -->
      <el-card v-if="alert.processingNotes" class="detail-card">
        <template #header>
          <div class="card-title">处理备注</div>
        </template>
        <div class="notes-content">{{ alert.processingNotes }}</div>
      </el-card>

      <!-- 拒绝原因 -->
      <el-card v-if="alert.rejectionReason" class="detail-card">
        <template #header>
          <div class="card-title">拒绝原因</div>
        </template>
        <div class="rejection-content">{{ alert.rejectionReason }}</div>
      </el-card>

      <!-- 订单信息 -->
      <el-card v-if="alert.orderInfo" class="detail-card">
        <template #header>
          <div class="card-title">订单信息</div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ alert.orderInfo.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ alert.orderInfo.customerName }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusTagType(alert.orderInfo.orderStatus)">
              {{ alert.orderInfo.orderStatusDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">{{ alert.orderInfo.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">
            {{ alert.orderInfo.deliveryAddress }}
          </el-descriptions-item>
          <el-descriptions-item v-if="alert.orderInfo.buyerNote" label="买家备注" :span="2">
            {{ alert.orderInfo.buyerNote }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { OrderAlertResponse } from '@/types/orderAlert'
import { PROCESSING_TYPE, ALERT_STATUS, PRIORITY } from '@/types/orderAlert'

interface Props {
  modelValue: boolean
  alert: OrderAlertResponse | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const dialogVisible = ref(false)

// 监听props变化
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal
})

watch(dialogVisible, (newVal) => {
  emit('update:modelValue', newVal)
})

const handleClose = () => {
  dialogVisible.value = false
}

// 工具方法
const getProcessingTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    [PROCESSING_TYPE.ORDER_CLOSE]: 'danger',
    [PROCESSING_TYPE.ADDRESS_CHANGE]: 'warning',
    [PROCESSING_TYPE.REFUND]: 'info',
    [PROCESSING_TYPE.CANCEL]: 'danger'
  }
  return typeMap[type] || ''
}

const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    [ALERT_STATUS.PENDING]: 'warning',
    [ALERT_STATUS.PROCESSING]: 'primary',
    [ALERT_STATUS.COMPLETED]: 'success',
    [ALERT_STATUS.REJECTED]: 'danger'
  }
  return statusMap[status] || ''
}

const getPriorityTagType = (priority: string) => {
  const priorityMap: Record<string, string> = {
    [PRIORITY.LOW]: 'info',
    [PRIORITY.NORMAL]: '',
    [PRIORITY.HIGH]: 'warning',
    [PRIORITY.URGENT]: 'danger'
  }
  return priorityMap[priority] || ''
}

const getOrderStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': 'warning',
    'SUBMITTED': 'primary',
    'APPROVED': 'success',
    'SHIPPED': 'info',
    'DELIVERED': 'success',
    'CANCELLED': 'danger',
    'RETURNED': 'warning'
  }
  return statusMap[status] || ''
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.alert-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-card {
  margin-bottom: 20px;
}

.detail-card:last-child {
  margin-bottom: 0;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.reason-content,
.notes-content,
.rejection-content {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  width: 120px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
