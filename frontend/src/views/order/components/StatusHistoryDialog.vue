<template>
  <el-dialog
    v-model="visible"
    title="订单状态历史"
    width="800px"
    destroy-on-close
  >
    <div class="status-history">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="statusHistory.length === 0" class="empty">
        <el-empty description="暂无状态历史记录" />
      </div>
      
      <div v-else class="timeline">
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in statusHistory"
            :key="index"
            :timestamp="formatDateTime(item.operatedAt)"
            :type="getTimelineItemType(item.toStatus)"
            :color="getTimelineItemColor(item.toStatus)"
            :hollow="item.isSystemOperation"
          >
            <div class="timeline-content">
              <div class="status-header">
                <el-tag :type="getStatusTagType(item.toStatus)">
                  {{ getStatusText(item.toStatus) }}
                </el-tag>
                <span class="operator-info">
                  {{ item.operatorName || '系统' }}
                  <span v-if="item.operatorRole" class="role-tag">
                    {{ item.operatorRole }}
                  </span>
                </span>
              </div>
              
              <div v-if="item.changeReason" class="change-reason">
                <strong>变更原因:</strong> {{ item.changeReason }}
              </div>
              
              <div v-if="item.remarks" class="remarks">
                <strong>备注:</strong> {{ item.remarks }}
              </div>
              
              <div class="operation-source">
                <el-tag size="small" type="info">
                  {{ getOperationSourceText(item.operationSource) }}
                </el-tag>
                <span v-if="item.isSystemOperation" class="system-operation">
                  系统操作
                </span>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
        <el-button
          v-if="statusHistory.length > 0"
          type="primary"
          @click="exportHistory"
        >
          导出历史
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api/order'
import type { OrderStatusLog } from '@/types/order'

// Props
interface Props {
  modelValue: boolean
  orderId: number | null
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const statusHistory = ref<OrderStatusLog[]>([])

// 监听器
watch(() => props.orderId, async (newOrderId) => {
  if (newOrderId && visible.value) {
    await loadStatusHistory(newOrderId)
  }
})

// 加载状态历史
const loadStatusHistory = async (orderId: number) => {
  try {
    loading.value = true
    const response = await orderApi.getOrderStatusHistory(orderId)
    statusHistory.value = response || []
  } catch (error) {
    ElMessage.error('加载状态历史失败')
    console.error('加载状态历史失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取时间线项目类型
const getTimelineItemType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'primary',
    SUBMITTED: 'warning',
    PENDING_APPROVAL: 'warning',
    APPROVED: 'success',
    PROCESSING: 'primary',
    SHIPPED: 'info',
    DELIVERED: 'success',
    COMPLETED: 'success',
    CANCELLED: 'danger',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取时间线项目颜色
const getTimelineItemColor = (status: string) => {
  const colorMap: Record<string, string> = {
    DRAFT: '#409eff',
    SUBMITTED: '#e6a23c',
    PENDING_APPROVAL: '#e6a23c',
    APPROVED: '#67c23a',
    PROCESSING: '#409eff',
    SHIPPED: '#909399',
    DELIVERED: '#67c23a',
    COMPLETED: '#67c23a',
    CANCELLED: '#f56c6c',
    REJECTED: '#f56c6c'
  }
  return colorMap[status] || '#909399'
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: '',
    SUBMITTED: 'warning',
    PENDING_APPROVAL: 'warning',
    APPROVED: 'success',
    PROCESSING: 'primary',
    SHIPPED: 'primary',
    DELIVERED: 'success',
    COMPLETED: 'success',
    CANCELLED: 'danger',
    REJECTED: 'danger'
  }
  return typeMap[status] || ''
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    PENDING_APPROVAL: '待审核',
    APPROVED: '已审核',
    PROCESSING: '处理中',
    SHIPPED: '已发货',
    DELIVERED: '已送达',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REJECTED: '已拒绝'
  }
  return textMap[status] || status
}

// 获取操作来源文本
const getOperationSourceText = (source: string) => {
  const textMap: Record<string, string> = {
    MANUAL: '手动操作',
    SYSTEM: '系统自动',
    API: 'API接口',
    BATCH: '批量操作'
  }
  return textMap[source] || source
}

// 格式化日期时间
const formatDateTime = (dateTime: string | Date) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 导出历史
const exportHistory = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能待实现')
}
</script>

<style scoped>
.status-history {
  max-height: 60vh;
  overflow-y: auto;
}

.loading {
  padding: 20px;
}

.empty {
  padding: 40px;
  text-align: center;
}

.timeline {
  padding: 20px;
}

.timeline-content {
  padding: 10px 0;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.operator-info {
  font-size: 14px;
  color: #606266;
}

.role-tag {
  margin-left: 8px;
  padding: 2px 6px;
  background: #f0f2f5;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
}

.change-reason {
  margin: 8px 0;
  font-size: 14px;
  color: #303133;
}

.remarks {
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.operation-source {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.system-operation {
  font-size: 12px;
  color: #909399;
  font-style: italic;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-timeline-item__node) {
  background-color: v-bind('getTimelineItemColor(statusHistory[0]?.toStatus)');
}

:deep(.el-timeline-item__node--hollow) {
  background-color: transparent;
  border-color: v-bind('getTimelineItemColor(statusHistory[0]?.toStatus)');
}
</style>
