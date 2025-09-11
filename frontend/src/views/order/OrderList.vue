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
        <el-button 
          v-if="!isSalesUser()"
          type="success" 
          :disabled="selectedOrders.length === 0"
          @click="showExportDialog('selected')"
        >
          <el-icon><Download /></el-icon>
          导出选中 ({{ selectedOrders.length }})
        </el-button>
        <el-button 
          v-if="!isSalesUser()"
          type="success" 
          :disabled="approvedOrdersCount === 0"
          @click="showExportDialog('all')"
        >
          <el-icon><Download /></el-icon>
          全部导出 ({{ approvedOrdersCount }})
        </el-button>
        <el-button 
          v-if="!isSalesUser()"
          type="primary" 
          :disabled="approvedOrdersCount === 0"
          @click="downloadShipTemplate"
        >
          <el-icon><Download /></el-icon>
          下载发货模板
        </el-button>
        <el-button 
          v-if="!isSalesUser()"
          type="warning" 
          @click="showImportShipDialog"
        >
          <el-icon><Upload /></el-icon>
          批量发货导入
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
        <el-form-item label="来源订单号">
          <el-input
            v-model="searchForm.sourceOrderNo"
            placeholder="请输入来源订单号"
            clearable
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.orderStatus" placeholder="请选择状态" clearable style="width: 150px;">
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
          <el-select v-model="searchForm.source" placeholder="请选择来源" clearable style="width: 150px;">
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
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
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
        <el-table-column label="供应链备注" width="150">
          <template #default="{ row }">
            <el-tooltip 
              v-if="row.orderStatus === 'REJECTED' && row.approvalComment" 
              :content="row.approvalComment" 
              placement="top"
            >
              <el-tag type="danger" size="small">
                {{ row.approvalComment?.substring(0, 10) }}{{ row.approvalComment?.length > 10 ? '...' : '' }}
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
            <!-- <div v-if="isSupplierUser()" style="font-size: 10px; color: #999; margin-bottom: 5px;">
              权限: 确认[{{ canSupplierConfirm(row) ? '✓' : '✗' }}] 
              发货[{{ canSupplierShip(row) ? '✓' : '✗' }}] 
              拒绝[{{ canSupplierReject(row) ? '✓' : '✗' }}]
            </div> -->
            
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
                  <!-- 移除销售侧发货入口，避免与供应商发货重复 -->
                  
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
                  <!-- <el-dropdown-item v-if="isSupplierUser()" disabled style="color: #999;">
                    调试: 确认[{{ canSupplierConfirm(row) ? '✓' : '✗' }}] 
                    发货[{{ canSupplierShip(row) ? '✓' : '✗' }}] 
                    拒绝[{{ canSupplierReject(row) ? '✓' : '✗' }}]
                  </el-dropdown-item> -->
                  
                  <!-- 通用操作 -->
                  <el-dropdown-item
                    :command="{ action: 'viewDetail', order: row }"
                  >
                    查看详情
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

    <!-- 调试信息 -->
    <!-- <div v-if="getUserRole() !== 'SALES'" style="background: #f0f9ff; border: 1px solid #3b82f6; border-radius: 8px; padding: 15px; margin: 20px 0;">
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
    </div> -->

    <!-- 新建/编辑订单对话框 -->
    <OrderDialog
      v-model="dialogVisible"
      :order="currentOrder"
      :mode="dialogMode"
      :risk="currentOrder ? isLowConfidence(currentOrder) : false"
      @success="handleDialogSuccess"
    />

    <!-- 导入功能已移至AI Excel导入模块 -->

    <!-- 订单详情对话框 -->
    <OrderDetail
      v-model:visible="detailDialogVisible"
      :order="selectedOrder"
    />

    <!-- 状态历史对话框 -->
    <StatusHistoryDialog
      v-model="historyDialogVisible"
      :order-id="currentOrderId"
    />

    <!-- 导出确认对话框 -->
    <el-dialog
      v-model="exportDialogVisible"
      title="确认导出"
      width="800px"
      :append-to-body="true"
      :close-on-click-modal="false"
      class="export-dialog"
    >
      <div class="export-confirm-content">
        <el-alert
          title="导出信息确认"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="export-stats">
              <p><strong>查询结果总数：</strong>{{ orderList.length }} 条</p>
              <p><strong>已审批可导出：</strong>{{ approvedOrdersCount }} 条</p>
              <p v-if="exportType === 'selected'">
                <strong>当前选中：</strong>{{ selectedOrders.length }} 条
              </p>
              <p v-if="exportType === 'selected'">
                <strong>选中中可导出：</strong>{{ selectedOrders.filter(order => order.orderStatus === 'APPROVED').length }} 条
              </p>
            </div>
          </template>
        </el-alert>
        
        <div class="export-warning" style="margin-top: 16px;">
          <el-alert
            title="注意"
            type="warning"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>• 只能导出状态为"已审批"的订单</p>
              <p>• 导出格式将根据供应商自动调整</p>
              <p>• 导出文件为Excel格式(.xlsx)</p>
            </template>
          </el-alert>
        </div>
        <!-- 库存影响预览 -->
        <div class="export-inventory" style="margin-top: 16px;">
          <el-alert :closable="false" type="warning" show-icon>
            <template #title>
              导出库存影响预览
            </template>
            <template #default>
              <div v-if="exportInventoryLoading">正在评估库存影响...</div>
              <div v-else style="overflow-x: auto;">
                <el-table :data="exportInventoryPreview" size="small" border height="300" style="min-width: 720px;">
                  <el-table-column prop="productName" label="商品" min-width="160">
                    <template #default="{ row }">
                      {{ row.productName || ('#' + row.productId) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="requiredQty" label="需求数量" width="90" align="center" />
                  <el-table-column prop="totalAvailable" label="总可用" width="90" align="center">
                    <template #default="{ row }">
                      <span :style="{ color: row.isInsufficient ? '#f56c6c' : '' }">{{ row.totalAvailable }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="分仓明细" min-width="260">
                    <template #default="{ row }">
                      <div v-if="row.warehouses && row.warehouses.length">
                        <div v-for="(w, idx) in row.warehouses" :key="idx" style="display:flex; gap:8px; align-items:center; margin:4px 0;">
                          <el-tag size="small">{{ w.warehouseName || '仓库' }}</el-tag>
                          <span>可用: {{ w.availableQuantity }}</span>
                          <span>发完后:
                            <b :style="{ color: (w.afterFullShip < 0 || w.alert) ? '#f56c6c' : '' }">{{ w.afterFullShip }}</b>
                          </span>
                          <el-tag v-if="w.alert" type="danger" size="small">预警</el-tag>
                        </div>
                      </div>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="风险" width="80" align="center">
                    <template #default="{ row }">
                      <el-tag v-if="row.isInsufficient" type="danger" size="small">不足</el-tag>
                      <span v-else>—</span>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-if="exportInventoryHasRisk" style="margin-top:8px; color:#f56c6c;">
                  注：存在可用库存不足或低于预警线的商品，请谨慎导出。
                </div>
              </div>
            </template>
          </el-alert>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmExport">确认导出</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量发货导入对话框 -->
    <el-dialog
      v-model="importShipDialogVisible"
      title="批量发货导入"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="import-ship-content">
        <!-- 文件上传区域 -->
        <div class="upload-section">
          <el-upload
            ref="shipUploadRef"
            class="upload-demo"
            drag
            :auto-upload="false"
            :on-change="(file: any) => handleFileUpload(file.raw)"
            accept=".xlsx,.xls"
            :limit="1"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 xlsx/xls 文件，且不超过 10MB
              </div>
            </template>
          </el-upload>
        </div>

        <!-- 数据预览区域 -->
        <div v-if="importPreviewData.length > 0" class="preview-section">
          <h4>数据预览</h4>
          <el-table :data="importPreviewData" stripe border max-height="300">
            <el-table-column prop="platformOrderNo" label="平台订单号" width="180" />
            <el-table-column prop="trackingNumber" label="物流单号" width="150" />
            <el-table-column prop="carrier" label="物流公司" width="120" />
            <el-table-column prop="shippingMethod" label="发货方式" width="100" />
            <el-table-column prop="shippingNotes" label="发货备注" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'valid' ? 'success' : 'danger'">
                  {{ row.status === 'valid' ? '有效' : '无效' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMessage" label="错误信息" />
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="importShipDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            :loading="importLoading"
            :disabled="importPreviewData.length === 0"
            @click="confirmShipImport"
          >
            确认导入
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 供应商发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="供应商发货"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="shipFormRef"
        :model="shipForm"
        :rules="shipRules"
        label-width="120px"
      >
        <el-form-item label="发货仓库" prop="warehouseId">
          <el-select v-model="shipForm.warehouseId" placeholder="请选择发货仓库" style="width: 100%" :loading="warehouseLoading">
            <el-option
              v-for="w in warehouseList"
              :key="w.id"
              :label="`${w.warehouseName} (${w.warehouseCode})`"
              :value="w.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号" prop="trackingNumber">
          <el-input v-model="shipForm.trackingNumber" placeholder="请输入物流单号" />
        </el-form-item>
        <el-form-item label="物流公司" prop="carrier">
          <el-input v-model="shipForm.carrier" placeholder="请输入物流公司" />
        </el-form-item>
        <el-form-item label="发货方式">
          <el-input v-model="shipForm.shippingMethod" placeholder="可选，填写发货方式" />
        </el-form-item>
        <el-form-item label="发货备注">
          <el-input v-model="shipForm.shippingNotes" type="textarea" :rows="2" placeholder="可选，填写发货备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="shipSubmitting" @click="submitShipForm">确认发货</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, ElLoading, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Upload, Search, Refresh, ArrowDown, Warning, Download, UploadFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import OrderDialog from './components/OrderDialog.vue'
import OrderDetail from './components/OrderDetail.vue'
import StatusHistoryDialog from './components/StatusHistoryDialog.vue'
import { orderApi } from '@/api/order'
import { getInventoryByProductId } from '@/api/inventory'
import { getWarehousesByCompanyId } from '@/api/warehouse'
import type { OrderResponse, OrderListRequest } from '@/types/order'
import type { Warehouse } from '@/types/warehouse'

// 路由实例
const router = useRouter()

// 响应式数据
const loading = ref(false)
const orderList = ref<OrderResponse[]>([])
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentOrder = ref<OrderResponse | null>(null)
const historyDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentOrderId = ref<number | null>(null)
const selectedOrder = ref<OrderResponse | null>(null)

// 多选相关状态
const selectedOrders = ref<OrderResponse[]>([])
const exportDialogVisible = ref(false)
const exportType = ref<'selected' | 'all'>('selected')

// 导出库存影响预览
type ExportWarehouseInfo = {
  warehouseName?: string
  availableQuantity: number
  minStockLevel?: number
  afterFullShip: number
  alert: boolean
}

type ExportInventoryRow = {
  productId: number
  productName?: string
  requiredQty: number
  totalAvailable: number
  isInsufficient: boolean
  warehouses: ExportWarehouseInfo[]
}

const exportInventoryLoading = ref(false)
const exportInventoryPreview = ref<ExportInventoryRow[]>([])
const exportInventoryHasRisk = computed(() => exportInventoryPreview.value.some(r => r.isInsufficient || r.warehouses.some(w => w.alert)))

// 批量发货导入相关状态
const importShipDialogVisible = ref(false)
const shipUploadRef = ref()
const uploadFile = ref<File | null>(null)
const importPreviewData = ref<any[]>([])
const importLoading = ref(false)

// 仓库相关状态
const warehouseList = ref<Warehouse[]>([])
const warehouseLoading = ref(false)

// 供应商发货对话框
const shipDialogVisible = ref(false)
const shipSubmitting = ref(false)
const currentShipOrder = ref<OrderResponse | null>(null)
const shipFormRef = ref<FormInstance>()
const shipForm = reactive({
  warehouseId: undefined as number | undefined,
  trackingNumber: '',
  carrier: '',
  shippingMethod: '',
  shippingNotes: ''
})
const shipRules: FormRules = {
  warehouseId: [{ required: true, message: '发货仓库不能为空', trigger: 'change' }],
  trackingNumber: [{ required: true, message: '物流单号不能为空', trigger: 'blur' }],
  carrier: [{ required: true, message: '物流公司不能为空', trigger: 'blur' }]
}

// 搜索表单
const searchForm = reactive<OrderListRequest>({
  current: 1,
  size: 20,
  platformOrderNo: '',
  customerName: '',
  sourceOrderNo: '',
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
  loadWarehouseList()
})

// 计算属性
const approvedOrdersCount = computed(() => {
  return orderList.value.filter(order => order.orderStatus === 'APPROVED').length
})

// 多选相关方法
const handleSelectionChange = (selection: OrderResponse[]) => {
  selectedOrders.value = selection
}

const showExportDialog = (type: 'selected' | 'all') => {
  exportType.value = type
  exportDialogVisible.value = true
  // 打开时预加载库存影响预览
  loadExportInventoryPreview()
}

const confirmExport = async () => {
  try {
    const orderIds = exportType.value === 'selected' 
      ? selectedOrders.value.filter(order => order.orderStatus === 'APPROVED').map(order => order.id)
      : orderList.value.filter(order => order.orderStatus === 'APPROVED').map(order => order.id)
    
    if (orderIds.length === 0) {
      ElMessage.warning('没有可导出的订单')
      return
    }
    
    const blob = await orderApi.exportOrders(orderIds)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const now = new Date()
    const timestamp = now.toISOString().slice(0, 19).replace(/[:-]/g, '')
    link.download = `订单导出_${timestamp}.xlsx`
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 提交供应商发货
const submitShipForm = async () => {
  if (!shipFormRef.value || !currentShipOrder.value) return
  try {
    await shipFormRef.value.validate()
    shipSubmitting.value = true
    await orderApi.supplierShipOrder(currentShipOrder.value.id, {
      warehouseId: shipForm.warehouseId as number,
      warehouseName: (warehouseList.value.find(w => w.id === shipForm.warehouseId)?.warehouseName) || '',
      trackingNumber: shipForm.trackingNumber.trim(),
      carrier: shipForm.carrier.trim(),
      shippingMethod: shipForm.shippingMethod || '',
      shippingNotes: shipForm.shippingNotes || '',
      operatorId: getCurrentUserId(),
      operatorName: getCurrentUserName(),
      operatorRole: getUserRole()
    })
    ElMessage.success('供应商发货成功')
    shipDialogVisible.value = false
    await loadOrderList()
  } catch (e) {
    // 验证失败或请求失败
    ElMessage.success('供应商发货异常'+ e)
  } finally {
    shipSubmitting.value = false
  }
}

// 载入导出库存影响预览
const loadExportInventoryPreview = async () => {
  try {
    exportInventoryLoading.value = true
    exportInventoryPreview.value = []

    const targetOrders: OrderResponse[] = exportType.value === 'selected'
      ? selectedOrders.value.filter(o => o.orderStatus === 'APPROVED')
      : orderList.value.filter(o => o.orderStatus === 'APPROVED')

    if (targetOrders.length === 0) return

    // 拉取订单详情以获取订单项
    const details = await Promise.all(
      targetOrders.map(o => orderApi.getOrderById(o.id).catch(() => null))
    )
    const validDetails = details.filter(Boolean) as OrderResponse[]

    // 汇总每个商品的需求数量
    const productNeedMap = new Map<number, { productName?: string; qty: number }>()
    validDetails.forEach(order => {
      (order.orderItems || []).forEach(item => {
        const pid = item.productId
        const prev = productNeedMap.get(pid)?.qty || 0
        productNeedMap.set(pid, { productName: item.productName, qty: prev + Number(item.quantity || 0) })
      })
    })

    // 查询库存并形成预览
    const rows: ExportInventoryRow[] = []
    for (const [productId, info] of productNeedMap.entries()) {
      let inventories: any[] = []
      try {
        const invResp: any = await getInventoryByProductId(productId)
        if (Array.isArray(invResp)) inventories = invResp
        else if (invResp?.data && Array.isArray(invResp.data)) inventories = invResp.data
        else inventories = []
      } catch {
        inventories = []
      }

      const warehouses: ExportWarehouseInfo[] = inventories.map(inv => {
        const available = Number(inv.availableQuantity || 0)
        const afterFullShip = available - info.qty
        const minLevel = inv.minStockLevel !== undefined ? Number(inv.minStockLevel) : undefined
        const alert = minLevel !== undefined ? afterFullShip < minLevel : false
        return {
          warehouseName: inv.warehouseName || inv.warehouseCode,
          availableQuantity: available,
          minStockLevel: minLevel,
          afterFullShip,
          alert
        }
      })

      const totalAvailable = warehouses.reduce((s, w) => s + w.availableQuantity, 0)
      const isInsufficient = totalAvailable < info.qty

      rows.push({
        productId,
        productName: info.productName,
        requiredQty: info.qty,
        totalAvailable,
        isInsufficient,
        warehouses
      })
    }

    exportInventoryPreview.value = rows
  } finally {
    exportInventoryLoading.value = false
  }
}

// 批量发货导入相关方法
const downloadShipTemplate = async () => {
  try {
    const blob = await orderApi.downloadShipTemplate()
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const now = new Date()
    const timestamp = now.toISOString().slice(0, 19).replace(/[:-]/g, '')
    link.download = `发货模板_${timestamp}.xlsx`
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

const showImportShipDialog = () => {
  importShipDialogVisible.value = true
  uploadFile.value = null
  importPreviewData.value = []
  // 清空upload组件的文件列表
  if (shipUploadRef.value) {
    shipUploadRef.value.clearFiles()
  }
}

const handleFileUpload = async (file: File) => {
  try {
    importLoading.value = true
    uploadFile.value = file
    
    // 解析Excel文件并预览
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await orderApi.previewShipImport(formData)
    importPreviewData.value = response.data || []
    
    ElMessage.success(`文件解析成功，共${importPreviewData.value.length}条数据`)
  } catch (error) {
    console.error('文件解析失败:', error)
    ElMessage.error('文件解析失败')
    uploadFile.value = null
    importPreviewData.value = []
  } finally {
    importLoading.value = false
  }
}

const confirmShipImport = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先上传文件')
    return
  }
  
  if (importPreviewData.value.length === 0) {
    ElMessage.warning('没有可导入的数据')
    return
  }
  
  try {
    importLoading.value = true
    
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    
    const response = await orderApi.importShipData(formData)
    
    ElMessage.success(`导入成功！成功：${response.successCount}条，失败：${response.failCount}条`)
    
    // 刷新订单列表
    await loadOrderList()
    
    // 关闭对话框
    importShipDialogVisible.value = false
    uploadFile.value = null
    importPreviewData.value = []
    
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

// 加载订单列表
const loadOrderList = async () => {
  try {
    loading.value = true
    
    // 转换搜索参数，匹配后端期望的字段
    const searchParams = {
      current: searchForm.current,
      size: searchForm.size,
      platformOrderNo: searchForm.platformOrderNo,
      customerName: searchForm.customerName,
      sourceOrderNo: searchForm.sourceOrderNo,
      status: searchForm.orderStatus, // 转换为后端期望的status字段
      source: searchForm.source,
      // 转换日期范围
      createdAtStart: searchForm.dateRange && searchForm.dateRange.length > 0 ? new Date(searchForm.dateRange[0]).toISOString() : undefined,
      createdAtEnd: searchForm.dateRange && searchForm.dateRange.length > 1 ? new Date(searchForm.dateRange[1]).toISOString() : undefined
    }
    
    const response = await orderApi.getOrderList(searchParams)
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

// 加载仓库列表
const loadWarehouseList = async () => {
  try {
    warehouseLoading.value = true
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const companyId = userInfo.companyId || userInfo.company_id || 1
    
    const response = await getWarehousesByCompanyId(companyId)
    if (response.success) {
      warehouseList.value = response.data || []
    } else {
      console.error('加载仓库列表失败:', response.message)
      warehouseList.value = []
    }
  } catch (error) {
    console.error('加载仓库列表失败:', error)
    warehouseList.value = []
  } finally {
    warehouseLoading.value = false
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
    sourceOrderNo: '',
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
  // 立即打开对话框，让OrderDialog的监听器处理重置
  dialogVisible.value = true
}

// 显示编辑对话框
const editOrder = async (order: OrderResponse) => {
  try {
    // 显示加载状态
    const loading = ElLoading.service({
      lock: true,
      text: '正在加载订单详情...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    // 从数据库重新查询订单详情，确保数据是最新的
    const freshOrderData = await orderApi.getOrderById(order.id)
    
    loading.close()
    
    dialogMode.value = 'edit'
    currentOrder.value = freshOrderData
    dialogVisible.value = true
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败，请重试')
  }
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

// 显示导入对话框 - 跳转到AI Excel导入模块
const showImportDialog = () => {
  // 跳转到AI Excel导入模块
  router.push('/ai-excel-import')
}

// 显示状态历史对话框
const showOrderDetail = (order: OrderResponse) => {
  selectedOrder.value = order
  detailDialogVisible.value = true
}

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
      case 'ship':
        // 这个case应该使用新的发货逻辑，暂时保留兼容性
        ElMessage.warning('请使用"供应商发货"功能，需要填写物流信息')
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
          currentShipOrder.value = order
          if (warehouseList.value.length === 0) {
            await loadWarehouseList()
          }
          // 重置表单
          Object.assign(shipForm, {
            warehouseId: undefined,
            trackingNumber: '',
            carrier: '',
            shippingMethod: '',
            shippingNotes: ''
          })
          shipDialogVisible.value = true
        } catch (error) {
          console.error('打开发货对话框失败:', error)
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
          // 使用新的供应商拒绝接口
          await orderApi.supplierRejectOrder(order.id, {
            rejectReason: rejectReason.value,
            operatorId: getCurrentUserId(),
            operatorName: getCurrentUserName(),
            operatorRole: getUserRole()
          })
          ElMessage.success('订单已拒绝，等待销售端重新处理')
        } catch (error) {
          if (error === 'cancel') {
            return // 用户取消，不显示错误信息
          }
          throw error // 重新抛出其他错误
        }
        break
      case 'viewDetail':
        showOrderDetail(order)
        return
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

// 导入功能已移至AI Excel导入模块

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

// 获取当前用户ID
const getCurrentUserId = () => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const parsed = JSON.parse(userInfo)
      return parsed.id || parsed.userId || parsed.user_id || 0
    }
    return 0
  } catch (error) {
    console.error('获取用户ID失败:', error)
    return 0
  }
}

// 获取当前用户名
const getCurrentUserName = () => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const parsed = JSON.parse(userInfo)
      return parsed.username || parsed.name || parsed.userName || '未知用户'
    }
    return '未知用户'
  } catch (error) {
    console.error('获取用户名失败:', error)
    return '未知用户'
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


// 已废弃：销售侧发货入口已移除
const canShip = (_order: OrderResponse) => false

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

/* 发货对话框宽度控制 */
:deep(.ship-dialog) {
  width: 500px !important;
}

:deep(.ship-dialog .el-message-box) {
  width: 500px !important;
}
</style>
