<template>
  <div class="success-orders-list">
    <!-- 标题和统计 -->
    <div class="list-header">
      <h4>成功订单列表</h4>
      <div class="list-stats">
        <el-tag type="success">{{ totalSuccessOrders }} 个成功订单</el-tag>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 成功订单列表 -->
    <div v-else-if="successOrders.length > 0" class="orders-container">
      <el-table :data="successOrders" style="width: 100%" class="success-orders-table">
        <el-table-column prop="orderId" label="订单编号" width="150" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.orderId }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="customerName" label="客户名称" min-width="150">
          <template #default="{ row }">
            <div class="customer-info">
              <el-icon class="customer-icon"><User /></el-icon>
              <span>{{ row.customerName || '未知客户' }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="productName" label="产品名称" min-width="150">
          <template #default="{ row }">
            <div class="product-info">
              <el-icon class="product-icon"><Goods /></el-icon>
              <span>{{ row.productName || '未知产品' }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="quantity" label="数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.quantity }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(computeAmount(row)) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <span class="time">{{ formatDateTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewOrder(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" v-if="totalSuccessOrders > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalSuccessOrders"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <el-empty description="暂无成功订单" />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="orderDetailDialogVisible"
      title="订单详情"
      width="800px"
      :close-on-click-modal="true"
    >
      <div v-if="selectedOrder" class="order-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ selectedOrder.orderId }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ selectedOrder.customerName }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ selectedOrder.productName }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ selectedOrder.quantity }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ formatAmount(computeAmount(selectedOrder)) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedOrder.status)">
              {{ getStatusText(selectedOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(selectedOrder.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="Excel行号">{{ selectedOrder.excelRowNumber }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Goods } from '@element-plus/icons-vue'
import aiExcelImportApi from '@/api/aiExcelImport'

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
  viewOrder: [orderId: string]
  refresh: []
}>()

// 响应式数据
const loading = ref(false)
const successOrders = ref<any[]>([])
const totalSuccessOrders = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const orderDetailDialogVisible = ref(false)
const selectedOrder = ref<any>(null)

    // 加载成功订单列表
    const loadSuccessOrders = async () => {
      if (!props.taskId) return
      
      try {
        loading.value = true
        console.log('📋 开始加载成功订单列表，任务ID:', props.taskId)
        
        const response = await aiExcelImportApi.getSuccessOrders(props.taskId, {
          page: currentPage.value,
          size: pageSize.value,
          sortBy: 'created_at',
          sortOrder: 'desc'
        })
        
        console.log('📊 成功订单API响应:', response)
        
        if (response && response.content) {
          successOrders.value = response.content
          totalSuccessOrders.value = response.totalElements
          console.log('✅ 成功订单列表加载成功，总数:', response.totalElements)
        } else if (response && Array.isArray(response)) {
          // 如果响应直接是数组
          successOrders.value = response
          totalSuccessOrders.value = response.length
          console.log('✅ 成功订单列表加载成功（数组格式），总数:', response.length)
        } else {
          successOrders.value = []
          totalSuccessOrders.value = 0
          console.warn('⚠️ 成功订单列表响应格式异常:', response)
        }
        
      } catch (error: any) {
        console.error('❌ 加载成功订单列表失败:', error)
        ElMessage.error(`加载成功订单列表失败: ${error.message || '未知错误'}`)
        successOrders.value = []
        totalSuccessOrders.value = 0
      } finally {
        loading.value = false
      }
    }

// 查看订单详情
const viewOrder = (order: any) => {
  selectedOrder.value = order
  orderDetailDialogVisible.value = true
  emit('viewOrder', order.orderId)
}

// 计算金额：quantity * unitPrice（后端未提供时默认0）
const computeAmount = (row: any) => {
  const qty = Number(row?.quantity ?? 0)
  const unit = Number(row?.unitPrice ?? 0)
  return qty * unit
}

// 格式化金额
const formatAmount = (amount: number) => {
  const num = Number(amount) || 0
  return num.toFixed(2)
}

// 格式化时间
const formatDateTime = (dateString: string) => {
  if (!dateString) return '未知时间'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 分页处理
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadSuccessOrders()
}

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  loadSuccessOrders()
}

// 状态相关方法
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'CREATED': 'success',
    'PROCESSING': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'CREATED': '已创建',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

// 监听任务ID变化
watch(() => props.taskId, (newTaskId) => {
  if (newTaskId && props.autoLoad) {
    currentPage.value = 1
    loadSuccessOrders()
  }
})

// 暴露方法给父组件
const refresh = () => {
  loadSuccessOrders()
}

defineExpose({
  refresh,
  loadSuccessOrders
})

// 生命周期
onMounted(() => {
  if (props.taskId && props.autoLoad) {
    loadSuccessOrders()
  }
})
</script>

<style scoped>
.success-orders-list {
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
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

.success-orders-table {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.customer-info,
.product-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.customer-icon {
  color: #409eff;
}

.product-icon {
  color: #67c23a;
}

.amount {
  font-weight: 600;
  color: #67c23a;
}

.time {
  color: #909399;
  font-size: 12px;
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

.order-detail-content {
  padding: 20px 0;
}
</style>
