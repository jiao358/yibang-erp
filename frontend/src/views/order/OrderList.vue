<template>
  <div class="order-list">
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新建订单
        </el-button>
        <el-button @click="showImportDialog">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="平台订单号">
          <el-input
            v-model="searchForm.platformOrderNo"
            placeholder="请输入平台订单号"
            clearable
          />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input
            v-model="searchForm.customerName"
            placeholder="请输入客户名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="供应商确认" value="SUPPLIER_CONFIRMED" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="运输中" value="IN_TRANSIT" />
            <el-option label="已送达" value="DELIVERED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单来源">
          <el-select v-model="searchForm.source" placeholder="请选择来源" clearable>
            <el-option label="手动创建" value="MANUAL" />
            <el-option label="Excel导入" value="EXCEL_IMPORT" />
            <el-option label="API接口" value="API" />
            <el-option label="网站" value="WEBSITE" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单列表 -->
    <div class="order-table">
      <el-table
        v-loading="loading"
        :data="orderList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="platformOrderNo" label="平台订单号" width="180" />
        <el-table-column prop="salesUserName" label="销售人" width="100" />
        <el-table-column prop="deliveryContact" label="收货人" width="100" />
        <el-table-column prop="deliveryPhone" label="手机号" width="130" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="source" label="订单来源" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ getSourceText(row.source) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="expectedDeliveryDate" label="最迟发货时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.expectedDeliveryDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewOrder(row)">查看</el-button>
            <el-button
              v-if="canEdit(row)"
              size="small"
              type="primary"
              @click="editOrder(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canDelete(row)"
              size="small"
              type="danger"
              @click="deleteOrder(row)"
            >
              删除
            </el-button>
            <el-dropdown @command="handleAction" trigger="click">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="canSubmit(row)"
                    :command="{ action: 'submit', order: row }"
                  >
                    提交订单
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canCancel(row)"
                    :command="{ action: 'cancel', order: row }"
                  >
                    取消订单
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canConfirm(row)"
                    :command="{ action: 'confirm', order: row }"
                  >
                    确认订单
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canShip(row)"
                    :command="{ action: 'ship', order: row }"
                  >
                    发货
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="{ action: 'history', order: row }"
                  >
                    状态历史
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新建/编辑订单对话框 -->
    <OrderDialog
      v-model="dialogVisible"
      :order="currentOrder"
      :mode="dialogMode"
      @success="handleDialogSuccess"
    />

    <!-- 批量导入对话框 -->
    <ImportDialog
      v-model="importDialogVisible"
      @success="handleImportSuccess"
    />

    <!-- 状态历史对话框 -->
    <StatusHistoryDialog
      v-model="historyDialogVisible"
      :order-id="currentOrderId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import OrderDialog from './components/OrderDialog.vue'
import ImportDialog from './components/ImportDialog.vue'
import StatusHistoryDialog from './components/StatusHistoryDialog.vue'
import { orderApi } from '@/api/order'
import type { OrderResponse, OrderListRequest } from '@/types/order'

// 响应式数据
const loading = ref(false)
const orderList = ref<OrderResponse[]>([])
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentOrder = ref<OrderResponse | null>(null)
const importDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const currentOrderId = ref<number | null>(null)

// 搜索表单
const searchForm = reactive<OrderListRequest>({
  current: 1,
  size: 20,
  platformOrderNo: '',
  customerName: '',
  status: '',
  source: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 生命周期
onMounted(() => {
  loadOrderList()
})

// 加载订单列表
const loadOrderList = async () => {
  try {
    loading.value = true
    const response = await orderApi.getOrderList(searchForm)
    orderList.value = response.records || []
    pagination.total = response.total || 0
    pagination.current = response.current || 1
    pagination.size = response.size || 20
  } catch (error) {
    ElMessage.error('加载订单列表失败')
    console.error('加载订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.current = 1
  loadOrderList()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    current: 1,
    size: 20,
    platformOrderNo: '',
    customerName: '',
    status: '',
    source: '',
    dateRange: []
  })
  loadOrderList()
}

// 分页处理
const handleSizeChange = (size: number) => {
  searchForm.size = size
  searchForm.current = 1
  loadOrderList()
}

const handleCurrentChange = (current: number) => {
  searchForm.current = current
  loadOrderList()
}

// 显示新建对话框
const showCreateDialog = () => {
  dialogMode.value = 'create'
  currentOrder.value = null
  dialogVisible.value = true
}

// 显示编辑对话框
const editOrder = (order: OrderResponse) => {
  dialogMode.value = 'edit'
  currentOrder.value = order
  dialogVisible.value = true
}

// 查看订单
const viewOrder = (order: OrderResponse) => {
  // TODO: 实现查看订单详情
  ElMessage.info('查看订单详情功能待实现')
}

// 删除订单
const deleteOrder = async (order: OrderResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除订单 ${order.platformOrderId} 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await orderApi.deleteOrder(order.id)
    ElMessage.success('删除成功')
    loadOrderList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除订单失败:', error)
    }
  }
}

// 显示导入对话框
const showImportDialog = () => {
  importDialogVisible.value = true
}

// 显示状态历史对话框
const showStatusHistory = (order: OrderResponse) => {
  currentOrderId.value = order.id
  historyDialogVisible.value = true
}

// 处理操作
const handleAction = async (command: { action: string; order: OrderResponse }) => {
  const { action, order } = command
  
  try {
    switch (action) {
      case 'submit':
        await orderApi.submitOrder(order.id)
        ElMessage.success('订单提交成功')
        break
      case 'cancel':
        await orderApi.cancelOrder(order.id)
        ElMessage.success('订单取消成功')
        break
      case 'confirm':
        await orderApi.supplierConfirmOrder(order.id)
        ElMessage.success('订单确认成功')
        break
      case 'ship':
        await orderApi.supplierShipOrder(order.id)
        ElMessage.success('订单发货成功')
        break
      case 'history':
        showStatusHistory(order)
        return
      default:
        ElMessage.warning('未知操作')
        return
    }
    
    loadOrderList()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('操作失败:', error)
  }
}

// 对话框成功回调
const handleDialogSuccess = () => {
  dialogVisible.value = false
  loadOrderList()
}

// 导入成功回调
const handleImportSuccess = () => {
  importDialogVisible.value = false
  loadOrderList()
}

// 权限检查
const canEdit = (order: OrderResponse) => {
  return order.orderStatus === 'DRAFT'
}

const canDelete = (order: OrderResponse) => {
  return order.orderStatus === 'DRAFT'
}

const canSubmit = (order: OrderResponse) => {
  return order.orderStatus === 'DRAFT'
}

const canCancel = (order: OrderResponse) => {
  return ['DRAFT', 'SUBMITTED'].includes(order.orderStatus)
}

const canConfirm = (order: OrderResponse) => {
  return order.orderStatus === 'SUBMITTED'
}

const canShip = (order: OrderResponse) => {
  return order.orderStatus === 'SUPPLIER_CONFIRMED'
}

// 状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: '',
    SUBMITTED: 'warning',
    SUPPLIER_CONFIRMED: 'success',
    SHIPPED: 'primary',
    IN_TRANSIT: 'info',
    DELIVERED: 'success',
    COMPLETED: 'success',
    CANCELLED: 'danger',
    REJECTED: 'danger'
  }
  return typeMap[status] || ''
}

// 状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    SUPPLIER_CONFIRMED: '供应商确认',
    SHIPPED: '已发货',
    IN_TRANSIT: '运输中',
    DELIVERED: '已送达',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REJECTED: '已拒绝'
  }
  return textMap[status] || status
}

// 来源文本
const getSourceText = (source: string) => {
  const textMap: Record<string, string> = {
    MANUAL: '手动创建',
    EXCEL_IMPORT: 'Excel导入',
    API: 'API接口',
    WEBSITE: '网站'
  }
  return textMap[source] || source
}

// 格式化日期时间
const formatDateTime = (dateTime: string | Date) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 格式化日期
const formatDate = (date: string | Date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.order-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.order-table {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #fff;
}
</style>
