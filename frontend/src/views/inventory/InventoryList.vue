<template>
  <div class="inventory-list">
    <div class="page-header">
      <h2>库存管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showStockInDialog">商品入库</el-button>
        <el-button type="warning" @click="showStockOutDialog">商品出库</el-button>
        <el-button type="info" @click="showAdjustDialog">库存调整</el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.productName"
            placeholder="请输入商品名称"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="商品编码">
          <el-input
            v-model="searchForm.productSku"
            placeholder="请输入商品编码"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select
            v-model="searchForm.warehouseId"
            placeholder="请选择仓库"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="warehouse in warehouseOptions"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select
            v-model="searchForm.stockStatus"
            placeholder="请选择库存状态"
            clearable
            style="width: 150px"
          >
            <el-option label="库存充足" value="SUFFICIENT" />
            <el-option label="库存不足" value="LOW" />
            <el-option label="需要补货" value="NEED_REORDER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 库存列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="inventoryList"
        border
        style="width: 100%"
      >
        <el-table-column prop="productSku" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="warehouseName" label="仓库名称" width="120" />
        <el-table-column prop="availableQuantity" label="可用库存" width="100" />
        <el-table-column prop="reservedQuantity" label="预留库存" width="100" />
        <el-table-column prop="totalQuantity" label="总库存" width="100">
          <template #default="{ row }">
            {{ (row.availableQuantity || 0) + (row.reservedQuantity || 0) + (row.damagedQuantity || 0) }}
          </template>
        </el-table-column>
        <el-table-column prop="minStockLevel" label="最低库存" width="100" />
        <el-table-column label="库存状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStockStatusTag(row)">
              {{ getStockStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleAdjustStock(row)">调整库存</el-button>
            <el-button size="small" @click="handleSetAlertLevel(row)">设置预警</el-button>
            <el-button size="small" @click="handleViewHistory(row)">操作记录</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
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
    </el-card>

    <!-- 入库对话框 -->
    <el-dialog
      v-model="stockInDialogVisible"
      title="商品入库"
      width="500px"
    >
      <el-form
        ref="stockInFormRef"
        :model="stockInForm"
        :rules="stockInRules"
        label-width="100px"
      >
        <el-form-item label="选择商品" prop="productId">
          <div style="width: 100%">
            <!-- 已选择的商品显示 -->
            <div v-if="stockInForm.productId && selectedProduct" class="selected-product">
              <el-tag type="success" closable @close="clearSelectedProduct">
                已选择: {{ selectedProduct.sku }} - {{ selectedProduct.name }}
              </el-tag>
            </div>
            
            <!-- 商品选择按钮 -->
            <el-button 
              type="primary" 
              @click="showProductSelector" 
              style="width: 100%; margin-top: 10px;"
            >
              <el-icon><Search /></el-icon>
              {{ stockInForm.productId ? '重新选择商品' : '选择商品' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="stockInForm.warehouseId" placeholder="请选择仓库" style="width: 100%">
            <el-option
              v-for="warehouse in warehouseOptions"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="stockInForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="入库原因" prop="reason">
          <el-input v-model="stockInForm.reason" placeholder="请输入入库原因" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="stockInForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStockIn" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 调整库存对话框 -->
    <el-dialog
      v-model="adjustStockDialogVisible"
      title="调整库存"
      width="600px"
    >
      <el-form
        ref="adjustStockFormRef"
        :model="adjustStockForm"
        :rules="adjustStockRules"
        label-width="120px"
      >
        <el-form-item label="商品信息">
          <div class="product-info-display">
            <div><strong>商品编码：</strong>{{ adjustStockForm.productSku }}</div>
            <div><strong>商品名称：</strong>{{ adjustStockForm.productName }}</div>
            <div><strong>仓库名称：</strong>{{ adjustStockForm.warehouseName }}</div>
          </div>
        </el-form-item>
        
        <el-form-item label="当前可用库存">
          <el-input-number 
            v-model="adjustStockForm.currentAvailableQuantity" 
            :min="0" 
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="调整后可用库存" prop="newAvailableQuantity">
          <el-input-number 
            v-model="adjustStockForm.newAvailableQuantity" 
            :min="0" 
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="当前预留库存">
          <el-input-number 
            v-model="adjustStockForm.currentReservedQuantity" 
            :min="0" 
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="调整后预留库存" prop="newReservedQuantity">
          <el-input-number 
            v-model="adjustStockForm.newReservedQuantity" 
            :min="0" 
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="当前最低库存">
          <el-input-number 
            v-model="adjustStockForm.currentMinStockLevel" 
            :min="0" 
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="调整后最低库存" prop="newMinStockLevel">
          <el-input-number 
            v-model="adjustStockForm.newMinStockLevel" 
            :min="0" 
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="调整原因" prop="reason">
          <el-input 
            v-model="adjustStockForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入调整库存的原因"
          />
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="adjustStockForm.remark" 
            type="textarea" 
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="adjustStockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjustStock" :loading="submitting">
          确定调整
        </el-button>
      </template>
    </el-dialog>

    <!-- 出库对话框 -->
    <el-dialog
      v-model="stockOutDialogVisible"
      title="商品出库"
      width="500px"
    >
      <el-form
        ref="stockOutFormRef"
        :model="stockOutForm"
        :rules="stockOutRules"
        label-width="100px"
      >
        <el-form-item label="选择商品" prop="productId">
          <div style="width: 100%">
            <!-- 已选择的商品显示 -->
            <div v-if="stockOutForm.productId && selectedProduct" class="selected-product">
              <el-tag type="success" closable @close="clearSelectedProductForOut">
                已选择: {{ selectedProduct.sku }} - {{ selectedProduct.name }}
              </el-tag>
            </div>
            
            <!-- 商品选择按钮 -->
            <el-button 
              type="primary" 
              @click="showProductSelectorForOut" 
              style="width: 100%; margin-top: 10px;"
            >
              <el-icon><Search /></el-icon>
              {{ stockOutForm.productId ? '重新选择商品' : '选择商品' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="stockOutForm.warehouseId" placeholder="请选择仓库" style="width: 100%">
            <el-option
              v-for="warehouse in warehouseOptions"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="stockOutForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="出库原因" prop="reason">
          <el-input v-model="stockOutForm.reason" placeholder="请输入出库原因" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="stockOutForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockOutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStockOut" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 库存调整对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="库存调整"
      width="500px"
    >
      <el-form
        ref="adjustFormRef"
        :model="adjustForm"
        :rules="adjustRules"
        label-width="100px"
      >
        <el-form-item label="选择商品" prop="productId">
          <el-select 
            v-model="adjustForm.productId" 
            placeholder="请选择商品" 
            filterable 
            remote 
            :remote-method="searchProducts"
            :loading="productSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="product in productOptions"
              :key="product.id"
              :value="product.id"
            >
              <div>
                <div><strong>{{ product.sku }}</strong></div>
                <div>{{ product.name }}</div>
                <div style="color: #999; font-size: 12px;">{{ product.unit }}</div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="adjustForm.warehouseId" placeholder="请选择仓库" style="width: 100%">
            <el-option
              v-for="warehouse in warehouseOptions"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调整后数量" prop="quantity">
          <el-input-number v-model="adjustForm.quantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="adjustForm.reason" placeholder="请输入调整原因" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="adjustForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjust" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 商品选择器对话框（入库） -->
    <ProductSelectorDialog
      v-model="productSelectorVisible"
      :multiple="false"
      :max-selection="1"
      @confirm="handleProductSelection"
    />

    <!-- 商品选择器对话框（出库） -->
    <ProductSelectorDialog
      v-model="productSelectorForOutVisible"
      :multiple="false"
      :max-selection="1"
      @confirm="handleProductSelectionForOut"
    />

    <!-- 设置预警线对话框 -->
    <el-dialog
      v-model="alertLevelDialogVisible"
      title="设置库存预警线"
      width="600px"
      append-to-body
      z-index="9999"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <el-form
        ref="alertLevelFormRef"
        :model="alertLevelForm"
        :rules="alertLevelRules"
        label-width="120px"
      >
        <el-form-item label="商品信息">
          <div class="product-info-display">
            <div><strong>商品编码：</strong>{{ alertLevelForm.productSku }}</div>
            <div><strong>商品名称：</strong>{{ alertLevelForm.productName }}</div>
            <div><strong>仓库名称：</strong>{{ alertLevelForm.warehouseName }}</div>
          </div>
        </el-form-item>
        
        <el-form-item label="当前最低库存">
          <el-input-number 
            v-model="alertLevelForm.currentMinStockLevel" 
            :min="0" 
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="设置最低库存" prop="minStockLevel">
          <el-input-number 
            v-model="alertLevelForm.minStockLevel" 
            :min="0" 
            style="width: 100%"
          />
          <div class="form-tip">当库存低于此数量时触发预警</div>
        </el-form-item>
        
        <el-form-item label="当前最高库存">
          <el-input-number 
            v-model="alertLevelForm.currentMaxStockLevel" 
            :min="0" 
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="设置最高库存" prop="maxStockLevel">
          <el-input-number 
            v-model="alertLevelForm.maxStockLevel" 
            :min="0" 
            style="width: 100%"
          />
          <div class="form-tip">当库存高于此数量时触发预警</div>
        </el-form-item>
        
        <el-form-item label="当前补货点">
          <el-input-number 
            v-model="alertLevelForm.currentReorderPoint" 
            :min="0" 
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="设置补货点" prop="reorderPoint">
          <el-input-number 
            v-model="alertLevelForm.reorderPoint" 
            :min="0" 
            style="width: 100%"
          />
          <div class="form-tip">当库存低于此数量时建议补货</div>
        </el-form-item>
        
        <el-form-item label="设置原因" prop="reason">
          <el-input 
            v-model="alertLevelForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入设置预警线的原因"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="alertLevelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAlertLevel" :loading="submitting">
          确定设置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.product-search-results {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 10px;
}

.product-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-item:hover {
  background-color: #f5f7fa;
}

.product-item.selected {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
}

.product-item:last-child {
  border-bottom: none;
}

.product-info {
  flex: 1;
}

.product-sku {
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 4px;
}

.product-name {
  color: #333;
  margin-bottom: 2px;
}

.product-unit {
  color: #999;
  font-size: 12px;
}

.product-status {
  margin-left: 10px;
}

.selected-product {
  margin-top: 10px;
}

.product-pagination {
  text-align: center;
  padding: 10px 0;
  border-top: 1px solid #f0f0f0;
}

.product-info-display {
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

.product-info-display > div {
  margin-bottom: 8px;
  color: #495057;
}

.product-info-display > div:last-child {
  margin-bottom: 0;
}

.product-info-display strong {
  color: #212529;
  margin-right: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getInventoryPage, stockIn, stockOut, adjustStock, adjustStockComplete, setStockAlertLevel } from '@/api/inventory'
import { getWarehouseList } from '@/api/warehouse'
import { getProductList } from '@/api/product'
import type { ProductInventory, StockOperationRequest, StockAdjustmentRequest } from '@/types/inventory'
import type { Warehouse } from '@/types/warehouse'
import ProductSelectorDialog from './components/ProductSelectorDialog.vue'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const stockInDialogVisible = ref(false)
const productSelectorVisible = ref(false)
const productSelectorForOutVisible = ref(false)
const stockOutDialogVisible = ref(false)
const adjustDialogVisible = ref(false)
const adjustStockDialogVisible = ref(false)
const alertLevelDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const stockInFormRef = ref<FormInstance>()
const stockOutFormRef = ref<FormInstance>()
const adjustFormRef = ref<FormInstance>()
const adjustStockFormRef = ref<FormInstance>()
const alertLevelFormRef = ref<FormInstance>()

const searchForm = reactive({
  productName: '',
  productSku: '',
  warehouseId: undefined as number | undefined,
  stockStatus: ''
})

// 获取当前用户ID
const getCurrentUserId = (): number => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.id || 1
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return 1
    }
  }
  return 1
}

const stockInForm = reactive<StockOperationRequest>({
  operationType: 'STOCK_IN',
  productId: 0,
  warehouseId: 0,
  quantity: 1,
  unitPrice: 0,
  reason: '',
  remark: '',
  operatorId: getCurrentUserId()
})

const stockOutForm = reactive<StockOperationRequest>({
  operationType: 'STOCK_OUT',
  productId: 0,
  warehouseId: 0,
  quantity: 1,
  reason: '',
  remark: '',
  operatorId: getCurrentUserId()
})

const adjustForm = reactive<StockOperationRequest>({
  operationType: 'ADJUSTMENT',
  productId: 0,
  warehouseId: 0,
  quantity: 0,
  reason: '',
  remark: '',
  operatorId: getCurrentUserId()
})

// 调整库存表单
const adjustStockForm = reactive({
  productId: 0,
  warehouseId: 0,
  productSku: '',
  productName: '',
  warehouseName: '',
  currentAvailableQuantity: 0,
  newAvailableQuantity: 0,
  currentReservedQuantity: 0,
  newReservedQuantity: 0,
  currentMinStockLevel: 0,
  newMinStockLevel: 0,
  reason: '',
  remark: ''
})

// 预警线设置表单
const alertLevelForm = reactive({
  productId: 0,
  warehouseId: 0,
  productSku: '',
  productName: '',
  warehouseName: '',
  currentMinStockLevel: 0,
  minStockLevel: 0,
  currentMaxStockLevel: 0,
  maxStockLevel: 0,
  currentReorderPoint: 0,
  reorderPoint: 0,
  reason: ''
})

const rules: FormRules = {
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ]
}

const stockInRules: FormRules = { ...rules }
const stockOutRules: FormRules = { ...rules }
const adjustRules: FormRules = { ...rules }

// 调整库存验证规则
const adjustStockRules: FormRules = {
  newAvailableQuantity: [
    { required: true, message: '请输入调整后的可用库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '可用库存不能小于0', trigger: 'blur' }
  ],
  newReservedQuantity: [
    { required: true, message: '请输入调整后的预留库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '预留库存不能小于0', trigger: 'blur' }
  ],
  newMinStockLevel: [
    { required: true, message: '请输入调整后的最低库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '最低库存不能小于0', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' }
  ]
}

// 预警线设置验证规则
const alertLevelRules: FormRules = {
  minStockLevel: [
    { required: true, message: '请输入最低库存预警线', trigger: 'blur' },
    { type: 'number', min: 0, message: '最低库存不能小于0', trigger: 'blur' }
  ],
  maxStockLevel: [
    { required: true, message: '请输入最高库存预警线', trigger: 'blur' },
    { type: 'number', min: 0, message: '最高库存不能小于0', trigger: 'blur' }
  ],
  reorderPoint: [
    { required: true, message: '请输入补货点', trigger: 'blur' },
    { type: 'number', min: 0, message: '补货点不能小于0', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入设置原因', trigger: 'blur' }
  ]
}

const inventoryList = ref<ProductInventory[]>([])
const warehouseOptions = ref<Warehouse[]>([])
const productOptions = ref<any[]>([])
const productSearchLoading = ref(false)

// 商品搜索相关
const productSearchKeyword = ref('')
const productSearchResults = ref<any[]>([])
const productSearchTotal = ref(0)
const productSearchPage = ref(1)
const selectedProduct = ref<any>(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
 })

// 方法
const loadInventoryList = async () => {
  loading.value = true
  try {
    // 获取当前用户的公司ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const companyId = userInfo.companyId
    
    const response = await getInventoryPage({
      page: pagination.current,
      size: pagination.size,
      companyId: companyId, // 传递公司ID进行数据隔离
      ...searchForm
    })
    if (response.success) {
      inventoryList.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('加载库存列表失败')
  } finally {
    loading.value = false
  }
}

const loadWarehouseOptions = async () => {
  try {
    const response = await getWarehouseList({ page: 1, size: 1000 })
    if (response.success) {
      warehouseOptions.value = response.data.records
    }
  } catch (error) {
    ElMessage.error('加载仓库列表失败')
  }
}

const searchProducts = async (query: string) => {
  if (query.length < 2) return
  
  productSearchLoading.value = true
  try {
    // 获取当前用户的公司ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const companyId = userInfo.companyId
    
    // 调用商品搜索API，只搜索当前公司的商品
    const response = await getProductList({
      page: 1,
      size: 20,
      name: query,
      status: 'ACTIVE', // 只搜索上架的商品
      companyId: companyId // 按公司ID过滤
    })
    if (response.success) {
      productOptions.value = response.data.records
    }
  } catch (error) {
    ElMessage.error('搜索商品失败')
  } finally {
    productSearchLoading.value = false
  }
}

const loadInitialProducts = async () => {
  try {
    // 获取当前用户的公司ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const companyId = userInfo.companyId
    
    // 加载一些默认的商品选项
    const response = await getProductList({
      page: 1,
      size: 10,
      status: 'ACTIVE', // 只加载上架的商品
      companyId: companyId // 按公司ID过滤
    })
    if (response.success) {
      productOptions.value = response.data.records
    }
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  }
}

// 商品搜索处理
const handleProductSearch = async () => {
  if (!productSearchKeyword.value.trim()) {
    productSearchResults.value = []
    return
  }
  
  productSearchLoading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const companyId = userInfo.companyId
    
    const response = await getProductList({
      page: productSearchPage.value,
      size: 10,
      name: productSearchKeyword.value,
      status: 'ACTIVE',
      companyId: companyId
    })
    
    if (response.success) {
      productSearchResults.value = response.data.records
      productSearchTotal.value = response.data.total
    }
  } catch (error) {
    ElMessage.error('搜索商品失败')
  } finally {
    productSearchLoading.value = false
  }
}

// 选择商品（入库）
const selectProduct = (product: any) => {
  stockInForm.productId = product.id
  selectedProduct.value = product
  productSearchResults.value = []
  productSearchKeyword.value = ''
}

// 选择商品（出库）
const selectProductForOut = (product: any) => {
  stockOutForm.productId = product.id
  selectedProduct.value = product
  productSearchResults.value = []
  productSearchKeyword.value = ''
}

// 清除选择的商品（入库）
const clearSelectedProduct = () => {
  stockInForm.productId = 0
  selectedProduct.value = null
}

// 清除选择的商品（出库）
const clearSelectedProductForOut = () => {
  stockOutForm.productId = 0
  selectedProduct.value = null
}

// 显示商品选择器
const showProductSelector = () => {
  productSelectorVisible.value = true
}

// 处理商品选择（入库）
const handleProductSelection = (products: any[]) => {
  if (products.length > 0) {
    const product = products[0]
    stockInForm.productId = product.id
    selectedProduct.value = product
    ElMessage.success(`已选择商品: ${product.sku} - ${product.name}`)
  }
}

// 显示商品选择器（出库）
const showProductSelectorForOut = () => {
  productSelectorForOutVisible.value = true
}

// 处理商品选择（出库）
const handleProductSelectionForOut = (products: any[]) => {
  if (products.length > 0) {
    const product = products[0]
    stockOutForm.productId = product.id
    selectedProduct.value = product
    ElMessage.success(`已选择商品: ${product.sku} - ${product.name}`)
  }
}

// 商品搜索分页
const handleProductPageChange = (page: number) => {
  productSearchPage.value = page
  handleProductSearch()
}

const handleSearch = () => {
  pagination.current = 1
  loadInventoryList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    productName: '',
    productSku: '',
    warehouseId: undefined,
    stockStatus: ''
  })
  pagination.current = 1
  loadInventoryList()
}

const showStockInDialog = async () => {
  stockInDialogVisible.value = true
  // 预加载一些商品选项
  await loadInitialProducts()
}

const showStockOutDialog = async () => {
  stockOutDialogVisible.value = true
  // 预加载一些商品选项
  await loadInitialProducts()
}

const showAdjustDialog = async () => {
  adjustDialogVisible.value = true
  // 预加载一些商品选项
  await loadInitialProducts()
}

// 显示调整库存对话框
const handleAdjustStock = (row: ProductInventory) => {
  // 填充表单数据
  adjustStockForm.productId = row.productId
  adjustStockForm.warehouseId = row.warehouseId
  adjustStockForm.productSku = row.productSku || ''
  adjustStockForm.productName = row.productName || ''
  adjustStockForm.warehouseName = row.warehouseName || ''
  adjustStockForm.currentAvailableQuantity = row.availableQuantity || 0
  adjustStockForm.newAvailableQuantity = row.availableQuantity || 0
  adjustStockForm.currentReservedQuantity = row.reservedQuantity || 0
  adjustStockForm.newReservedQuantity = row.reservedQuantity || 0
  adjustStockForm.currentMinStockLevel = row.minStockLevel || 0
  adjustStockForm.newMinStockLevel = row.minStockLevel || 0
  adjustStockForm.reason = ''
  adjustStockForm.remark = ''
  
  adjustStockDialogVisible.value = true
}

// 提交调整库存
const submitAdjustStock = async () => {
  if (!adjustStockFormRef.value) return
  
  await adjustStockFormRef.value.validate()
  
  submitting.value = true
  try {
    // 调用完整的库存调整API
    await adjustStockComplete({
      productId: adjustStockForm.productId,
      warehouseId: adjustStockForm.warehouseId,
      newAvailableQuantity: adjustStockForm.newAvailableQuantity,
      newReservedQuantity: adjustStockForm.newReservedQuantity,
      newMinStockLevel: adjustStockForm.newMinStockLevel,
      reason: adjustStockForm.reason,
      remark: adjustStockForm.remark,
      operatorId: getCurrentUserId()
    })
    
    ElMessage.success('库存调整成功')
    adjustStockDialogVisible.value = false
    loadInventoryList() // 重新加载库存列表
  } catch (error) {
    ElMessage.error('库存调整失败')
  } finally {
    submitting.value = false
  }
}

const submitStockIn = async () => {
  if (!stockInFormRef.value) return
  
  await stockInFormRef.value.validate()
  
  submitting.value = true
  try {
    await stockIn(stockInForm)
    ElMessage.success('商品入库成功')
    stockInDialogVisible.value = false
    loadInventoryList()
  } catch (error) {
    ElMessage.error('商品入库失败')
  } finally {
    submitting.value = false
  }
}

const submitStockOut = async () => {
  if (!stockOutFormRef.value) return
  
  await stockOutFormRef.value.validate()
  
  submitting.value = true
  try {
    await stockOut(stockOutForm)
    ElMessage.success('商品出库成功')
    stockOutDialogVisible.value = false
    loadInventoryList()
  } catch (error) {
    ElMessage.error('商品出库失败')
  } finally {
    submitting.value = false
  }
}

const submitAdjust = async () => {
  if (!adjustFormRef.value) return
  
  await adjustFormRef.value.validate()
  
  submitting.value = true
  try {
    await adjustStock(adjustForm)
    ElMessage.success('库存调整成功')
    adjustDialogVisible.value = false
    loadInventoryList()
  } catch (error) {
    ElMessage.error('库存调整失败')
  } finally {
    submitting.value = false
  }
}

const handleSetAlertLevel = (row: ProductInventory) => {
  // 填充表单数据
  alertLevelForm.productId = row.productId
  alertLevelForm.warehouseId = row.warehouseId
  alertLevelForm.productSku = row.productSku || ''
  alertLevelForm.productName = row.productName || ''
  alertLevelForm.warehouseName = row.warehouseName || ''
  alertLevelForm.currentMinStockLevel = row.minStockLevel || 0
  alertLevelForm.minStockLevel = row.minStockLevel || 0
  alertLevelForm.currentMaxStockLevel = row.maxStockLevel || 0
  alertLevelForm.maxStockLevel = row.maxStockLevel || 0
  alertLevelForm.currentReorderPoint = row.reorderPoint || 0
  alertLevelForm.reorderPoint = row.reorderPoint || 0
  alertLevelForm.reason = ''
  
  alertLevelDialogVisible.value = true
}

// 提交预警线设置
const submitAlertLevel = async () => {
  if (!alertLevelFormRef.value) return
  
  await alertLevelFormRef.value.validate()
  
  submitting.value = true
  try {
    await setStockAlertLevel({
      productId: alertLevelForm.productId,
      warehouseId: alertLevelForm.warehouseId,
      minStockLevel: alertLevelForm.minStockLevel,
      maxStockLevel: alertLevelForm.maxStockLevel,
      reorderPoint: alertLevelForm.reorderPoint
    })
    
    ElMessage.success('预警线设置成功')
    alertLevelDialogVisible.value = false
    loadInventoryList() // 重新加载库存列表
  } catch (error) {
    ElMessage.error('预警线设置失败')
  } finally {
    submitting.value = false
  }
}

const handleViewHistory = (row: ProductInventory) => {
  // TODO: 实现查看操作记录功能
  ElMessage.info('查看操作记录功能待实现')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadInventoryList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadInventoryList()
}

const getStockStatusTag = (row: ProductInventory) => {
  const available = row.availableQuantity || 0
  const minLevel = row.minStockLevel || 0
  const reorderPoint = row.reorderPoint || 0
  
  if (available <= minLevel) return 'danger'
  if (available <= reorderPoint) return 'warning'
  return 'success'
}

const getStockStatusLabel = (row: ProductInventory) => {
  const available = row.availableQuantity || 0
  const minLevel = row.minStockLevel || 0
  const reorderPoint = row.reorderPoint || 0
  
  if (available <= minLevel) return '库存不足'
  if (available <= reorderPoint) return '需要补货'
  return '库存充足'
}

// 生命周期
onMounted(() => {
  loadInventoryList()
  loadWarehouseOptions()
})
</script>

<style scoped>
.inventory-list {
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

.search-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
