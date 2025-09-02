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
          <el-select v-model="searchForm.orderStatus" placeholder="请选择状态" clearable>
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
        :row-class-name="getRowClassName"
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
        <el-table-column prop="orderStatus" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.orderStatus)">
              {{ getStatusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="拒绝原因" width="150">
          <template #default="{ row }">
            <el-tooltip 
              v-if="row.orderStatus === 'REJECTED' && row.extendedFields?.rejectReason" 
              :content="row.extendedFields.rejectReason" 
              placement="top"
            >
              <el-tag type="danger" size="small">
                {{ row.extendedFields.rejectReason?.substring(0, 10) }}{{ row.extendedFields.rejectReason?.length > 10 ? '...' : '' }}
              </el-tag>
            </el-tooltip>
            <span v-else-if="row.orderStatus === 'REJECTED'">已拒绝</span>
            <span v-else>-</span>
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
        <el-table-column prop="aiConfidence" label="AI置信度" width="120">
          <template #default="{ row }">
            <div v-if="row.aiProcessed">
              <el-tag 
                :type="getAIConfidenceTagType(row.aiConfidence)" 
                size="small"
              >
                {{ formatAIConfidence(row.aiConfidence) }}
              </el-tag>
              <el-tooltip 
                v-if="isLowConfidence(row)" 
                content="有风险、请一定要审核修改" 
                placement="top"
              >
                <el-icon class="warning-icon" style="margin-left: 4px; color: #e6a23c;">
                  <Warning />
                </el-icon>
              </el-tooltip>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <!-- 调试信息：显示权限状态 -->
            <div v-if="isSupplierUser()" style="font-size: 10px; color: #999; margin-bottom: 5px;">
              权限: 确认[{{ canSupplierConfirm(row) ? '✓' : '✗' }}] 
              发货[{{ canSupplierShip(row) ? '✓' : '✗' }}] 
              拒绝[{{ canSupplierReject(row) ? '✓' : '✗' }}]
            </div>
            
            <el-button
              v-if="canEdit(row)"
              size="small"
              type="success"
              @click="editOrder(row)"
            >
              修改
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
                  <!-- 销售用户操作 -->
                  <el-dropdown-item
                    v-if="canSubmit(row)"
                    :command="{ action: 'submit', order: row }"
                  >
                    提交
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canCancel(row)"
                    :command="{ action: 'cancel', order: row }"
                  >
                    撤回
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
                  
                  <!-- 供应链用户操作 -->
                  <el-dropdown-item
                    v-if="canSupplierConfirm(row)"
                    :command="{ action: 'supplierConfirm', order: row }"
                  >
                    供应商确认
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canSupplierShip(row)"
                    :command="{ action: 'supplierShip', order: row }"
                  >
                    供应商发货
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canSupplierReject(row)"
                    :command="{ action: 'supplierReject', order: row }"
                  >
                    拒绝订单
                  </el-dropdown-item>
                  
                  <!-- 调试：显示权限检查结果 -->
                  <el-dropdown-item v-if="isSupplierUser()" disabled style="color: #999;">
                    调试: 确认[{{ canSupplierConfirm(row) ? '✓' : '✗' }}] 
                    发货[{{ canSupplierShip(row) ? '✓' : '✗' }}] 
                    拒绝[{{ canSupplierReject(row) ? '✓' : '✗' }}]
                  </el-dropdown-item>
                  
                  <!-- 通用操作 -->
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

    <!-- 调试信息 -->
    <div v-if="getUserRole() !== 'SALES'" style="background: #f0f9ff; border: 1px solid #3b82f6; border-radius: 8px; padding: 15px; margin: 20px 0;">
      <h4 style="margin: 0 0 10px 0; color: #1e40af;">🔍 调试信息</h4>
      <p style="margin: 5px 0; color: #1e40af;">
        <strong>当前用户角色:</strong> {{ getUserRole() }}
      </p>
      <p style="margin: 5px 0; color: #1e40af;">
        <strong>是否为供应链用户:</strong> {{ isSupplierUser() ? '是' : '否' }}
      </p>
      <p style="margin: 5px 0; color: #1e40af;">
        <strong>是否为销售用户:</strong> {{ isSalesUser() ? '是' : '否' }}
      </p>
      <p style="margin: 5px 0; color: #1e40af;">
        <strong>JWT角色详情:</strong> {{ getJwtRoleDetails() }}
      </p>
      <p style="margin: 5px 0; color: #1e40af;">
        <strong>localStorage userInfo:</strong> {{ getLocalStorageUserInfo() }}
      </p>
      <p style="margin: 5px 0; color: #1e40af;">
        <strong>localStorage token:</strong> {{ getLocalStorageToken() }}
      </p>
      <p style="margin: 5px 0; color: #1e40af;">
        <strong>订单列表状态分布:</strong> {{ getOrderStatusDistribution() }}
      </p>
      <p style="margin: 5px 0; color: #1e40af;">
        <strong>可操作的订单数量:</strong> {{ getOperableOrderCount() }}
      </p>
    </div>

    <!-- 新建/编辑订单对话框 -->
    <OrderDialog
      v-model="dialogVisible"
      :order="currentOrder"
      :mode="dialogMode"
      :risk="currentOrder ? isLowConfidence(currentOrder) : false"
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
import { Plus, Upload, Search, Refresh, ArrowDown, Warning } from '@element-plus/icons-vue'
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
  orderStatus: '',
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
    orderStatus: '',
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
  // 确保在新建模式下清空之前的数据
  setTimeout(() => {
    dialogVisible.value = true
  }, 0)
}

// 显示编辑对话框
const editOrder = (order: OrderResponse) => {
  dialogMode.value = 'edit'
  currentOrder.value = order
  dialogVisible.value = true
}




// 删除订单
const deleteOrder = async (order: OrderResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除订单 ${order.platformOrderNo} 吗？`,
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
        try {
          await ElMessageBox.confirm(
            `确定要提交订单 ${order.platformOrderNo} 吗？`,
            '确认提交',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
          await orderApi.submitOrder(order.id)
          ElMessage.success('订单提交成功')
        } catch (error) {
          if (error === 'cancel') {
            return // 用户取消，不显示错误信息
          }
          throw error // 重新抛出其他错误
        }
        break
      case 'cancel':
        try {
          await ElMessageBox.confirm(
            `确定要撤回订单 ${order.platformOrderNo} 吗？`,
            '确认撤回',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
          await orderApi.cancelOrder(order.id)
          ElMessage.success('订单撤回成功')
        } catch (error) {
          if (error === 'cancel') {
            return // 用户取消，不显示错误信息
          }
          throw error // 重新抛出其他错误
        }
        break
      case 'confirm':
        await orderApi.supplierConfirmOrder(order.id)
        ElMessage.success('订单确认成功')
        break
      case 'ship':
        await orderApi.supplierShipOrder(order.id)
        ElMessage.success('订单发货成功')
        break
      case 'supplierConfirm':
        try {
          await ElMessageBox.confirm(
            `确定要确认订单 ${order.platformOrderNo} 吗？确认后将进入发货阶段。`,
            '供应商确认',
            {
              confirmButtonText: '确定确认',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
          await orderApi.supplierConfirmOrder(order.id)
          ElMessage.success('供应商确认成功')
        } catch (error) {
          if (error === 'cancel') {
            return // 用户取消，不显示错误信息
          }
          throw error // 重新抛出其他错误
        }
        break
      case 'supplierShip':
        try {
          await ElMessageBox.confirm(
            `确定要发货订单 ${order.platformOrderNo} 吗？发货后将进入运输阶段。`,
            '供应商发货',
            {
              confirmButtonText: '确定发货',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )
          await orderApi.supplierShipOrder(order.id)
          ElMessage.success('供应商发货成功')
        } catch (error) {
          if (error === 'cancel') {
            return // 用户取消，不显示错误信息
          }
          throw error // 重新抛出其他错误
        }
        break
      case 'supplierReject':
        try {
          const rejectReason = await ElMessageBox.prompt(
            '请输入拒绝原因（必填）。拒绝后订单将打回给销售端重新处理。',
            '拒绝订单',
            {
              confirmButtonText: '确定拒绝',
              cancelButtonText: '取消',
              type: 'warning',
              inputPattern: /.+/,
              inputErrorMessage: '拒绝原因不能为空'
            }
          )
          // 先更新订单状态为"已拒绝"
          await orderApi.updateOrderStatus(order.id, { 
            orderStatus: 'REJECTED', 
            changeReason: rejectReason.value 
          })
          
          // 将拒绝原因保存到扩展字段中
          await orderApi.updateOrder(order.id, {
            extendedFields: {
              rejectReason: rejectReason.value,
              rejectedAt: new Date().toISOString(),
              rejectedBy: getUserRole()
            }
          })
          ElMessage.success('订单已拒绝，等待销售端重新处理')
        } catch (error) {
          if (error === 'cancel') {
            return // 用户取消，不显示错误信息
          }
          throw error // 重新抛出其他错误
        }
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
  // 清理当前订单数据，避免影响下次新建
  currentOrder.value = null
  dialogMode.value = 'create'
  loadOrderList()
}

// 导入成功回调
const handleImportSuccess = () => {
  importDialogVisible.value = false
  loadOrderList()
}

// 获取当前用户角色
const getUserRole = () => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const parsed = JSON.parse(userInfo)
      console.log('用户信息:', parsed)
      
      // 检查多种可能的角色字段
      if (parsed.role) {
        console.log('找到role字段:', parsed.role)
        return parsed.role
      }
      if (parsed.userType) {
        console.log('找到userType字段:', parsed.userType)
        return parsed.userType
      }
      if (parsed.userRole) {
        console.log('找到userRole字段:', parsed.userRole)
        return parsed.userRole
      }
      if (parsed.type) {
        console.log('找到type字段:', parsed.type)
        return parsed.type
      }
    }
    
    // 检查JWT token中的角色信息
    const token = localStorage.getItem('token')
    if (token) {
      try {
        const parts = token.split('.')
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]))
          console.log('JWT Payload:', payload)
          
          // 检查roles数组（复数形式）
          if (payload.roles && Array.isArray(payload.roles) && payload.roles.length > 0) {
            console.log('JWT中找到roles数组:', payload.roles)
            return payload.roles[0] // 返回第一个角色
          }
          
          // 检查单数形式的角色字段
          if (payload.role) {
            console.log('JWT中找到role字段:', payload.role)
            return payload.role
          }
          if (payload.userType) {
            console.log('JWT中找到userType字段:', payload.userType)
            return payload.userType
          }
          if (payload.userRole) {
            console.log('JWT中找到userRole字段:', payload.userRole)
            return payload.userRole
          }
          if (payload.type) {
            console.log('JWT中找到type字段:', payload.type)
            return payload.type
          }
        }
      } catch (e) {
        console.log('解析JWT失败:', e)
      }
    }
    
    console.log('未找到角色信息，默认为SALES')
    return 'SALES' // 默认为销售角色
  } catch (error) {
    console.error('获取用户角色失败:', error)
    return 'SALES' // 出错时默认为销售角色
  }
}

// 权限检查
const canEdit = (order: OrderResponse) => {
  // 草稿和已取消状态的订单可以编辑
  return ['DRAFT', 'CANCELLED'].includes(order.orderStatus)
}

const canDelete = (order: OrderResponse) => {
  // 草稿和已取消状态的订单可以删除
  return ['DRAFT', 'CANCELLED'].includes(order.orderStatus)
}

const canSubmit = (order: OrderResponse) => {
  // 草稿和已取消状态的订单可以提交
  return ['DRAFT', 'CANCELLED'].includes(order.orderStatus)
}

const canCancel = (order: OrderResponse) => {
  // 草稿、已提交和已取消状态的订单可以撤回
  return ['DRAFT', 'SUBMITTED', 'CANCELLED'].includes(order.orderStatus)
}

// 判断是否为供应链用户
const isSupplierUser = () => {
  const role = getUserRole()
  const supplierRoles = [
    'SUPPLIER', 'SUPPLY_CHAIN', 'VENDOR', 'PROVIDER', 'MANUFACTURER',
    'SUPPLIER_ADMIN', 'SUPPLIER_USER', 'SUPPLIER_MANAGER',
    'VENDOR_ADMIN', 'VENDOR_USER', 'VENDOR_MANAGER'
  ]
  const result = supplierRoles.includes(role)
  
  console.log('isSupplierUser() 检查:', {
    role,
    supplierRoles,
    result
  })
  
  return result
}

// 判断是否为销售用户
const isSalesUser = () => {
  const role = getUserRole()
  const salesRoles = ['SALES', 'SALESMAN', 'ACCOUNT_MANAGER', 'CUSTOMER_SERVICE']
  return salesRoles.includes(role)
}

// 辅助函数：获取localStorage状态
const getLocalStorageUserInfo = () => {
  try {
    return localStorage.getItem('userInfo') ? '已设置' : '未设置'
  } catch {
    return '访问失败'
  }
}

const getLocalStorageToken = () => {
  try {
    return localStorage.getItem('token') ? '已设置' : '未设置'
  } catch {
    return '访问失败'
  }
}

// 获取JWT角色详情
const getJwtRoleDetails = () => {
  try {
    const token = localStorage.getItem('token')
    if (token) {
      const parts = token.split('.')
      if (parts.length === 3) {
        const payload = JSON.parse(atob(parts[1]))
        if (payload.roles && Array.isArray(payload.roles)) {
          return `roles: [${payload.roles.join(', ')}]`
        }
        if (payload.role) {
          return `role: ${payload.role}`
        }
        return '无角色信息'
      }
    }
    return '无token'
  } catch {
    return '解析失败'
  }
}

// 获取订单状态分布
const getOrderStatusDistribution = () => {
  if (!orderList.value || orderList.value.length === 0) {
    return '无订单数据'
  }
  
  const statusCount: Record<string, number> = {}
  orderList.value.forEach(order => {
    const status = order.orderStatus
    statusCount[status] = (statusCount[status] || 0) + 1
  })
  
  return Object.entries(statusCount)
    .map(([status, count]) => `${status}: ${count}`)
    .join(', ')
}

// 获取可操作的订单数量
const getOperableOrderCount = () => {
  if (!orderList.value || orderList.value.length === 0) {
    return '无订单数据'
  }
  
  const canConfirmCount = orderList.value.filter(order => canSupplierConfirm(order)).length
  const canShipCount = orderList.value.filter(order => canSupplierShip(order)).length
  const canRejectCount = orderList.value.filter(order => canSupplierReject(order)).length
  
  return `可确认: ${canConfirmCount}, 可发货: ${canShipCount}, 可拒绝: ${canRejectCount}`
}

// 供应商权限检查
const canSupplierConfirm = (order: OrderResponse) => {
  const isSupplier = isSupplierUser()
  // 支持已提交状态
  const statusMatch = order.orderStatus === 'SUBMITTED'
  const result = isSupplier && statusMatch
  
  console.log(`订单 ${order.platformOrderNo} 供应商确认权限检查:`, {
    isSupplier,
    orderStatus: order.orderStatus,
    statusMatch,
    result
  })
  
  return result
}

const canSupplierShip = (order: OrderResponse) => {
  const isSupplier = isSupplierUser()
  // 支持已审批状态
  const statusMatch = order.orderStatus === 'APPROVED'
  const result = isSupplier && statusMatch
  
  console.log(`订单 ${order.platformOrderNo} 供应商发货权限检查:`, {
    isSupplier,
    orderStatus: order.orderStatus,
    statusMatch,
    result
  })
  
  return result
}

const canSupplierReject = (order: OrderResponse) => {
  const isSupplier = isSupplierUser()
  // 供应商在发货前始终可以拒绝订单
  const statusMatch = ['SUBMITTED', 'APPROVED'].includes(order.orderStatus)
  const result = isSupplier && statusMatch
  
  console.log(`订单 ${order.platformOrderNo} 供应商拒绝权限检查:`, {
    isSupplier,
    orderStatus: order.orderStatus,
    statusMatch,
    result
  })
  
  return result
}

// 销售用户权限检查
const canConfirm = (order: OrderResponse) => {
  // 只有销售用户且订单状态为已提交时才能确认
  return isSalesUser() && order.orderStatus === 'SUBMITTED'
}

const canShip = (order: OrderResponse) => {
  // 只有销售用户且订单状态为供应商确认时才能发货
  return isSalesUser() && order.orderStatus === 'SUPPLIER_CONFIRMED'
}

// 状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: '',
    SUBMITTED: 'warning',
    APPROVED: 'success',
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
    APPROVED: '已审批',
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

// AI置信度相关方法（后端返回为0-1字符串，如"0.9500"）
const parseConfidence = (confidence?: string): number => {
  const raw = (confidence || '').trim()
  const num = parseFloat(raw)
  if (Number.isNaN(num)) return 0
  return num
}

const isLowConfidence = (row: OrderResponse) => {
  const value = parseConfidence(row.aiConfidence)
  return row.aiProcessed && value < 0.8
}

const getAIConfidenceTagType = (confidence: string) => {
  const value = parseConfidence(confidence)
  if (value >= 0.8) return 'success'
  if (value >= 0.6) return 'warning'
  return 'danger'
}

const formatAIConfidence = (confidence: string) => {
  const value = parseConfidence(confidence)
  return `${(value * 100).toFixed(1)}%`
}

// 行样式方法
const getRowClassName = ({ row }: { row: OrderResponse }) => {
  if (isLowConfidence(row)) {
    return 'warning-row'
  }
  return ''
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

/* 警告行样式 */
:deep(.warning-row) {
  background-color: #fdf6ec !important;
}

:deep(.warning-row:hover) {
  background-color: #f5e6d3 !important;
}

.warning-icon {
  cursor: pointer;
}
</style>
