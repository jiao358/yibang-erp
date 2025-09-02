<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="80%"
    :before-close="handleClose"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="order-form"
    >
      <!-- 操作进度提示条 -->
      <div v-if="submitting" class="progress-bar">
        <el-progress 
          :percentage="submitProgress" 
          :show-text="false"
          :stroke-width="6"
          color="#409eff"
        />
        <div class="progress-text">
          <el-icon class="is-loading"><Refresh /></el-icon>
          <span>{{ submitProgressText }}</span>
        </div>
      </div>
      
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="平台订单号" prop="platformOrderNo">
            <el-input
              v-model="form.platformOrderNo"
              placeholder="系统自动生成"
              disabled
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户" prop="customerId">
            <el-select
              v-model="form.customerId"
              placeholder="请选择客户（可选）"
              filterable
              remote
              :remote-method="searchCustomers"
              :loading="customerLoading"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="customer in customerOptions"
                :key="customer.id"
                :label="customer.name"
                :value="customer.id"
              >
                <span>{{ customer.name }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  {{ customer.customerCode }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="订单类型" prop="orderType">
            <el-select v-model="form.orderType" placeholder="请选择订单类型" style="width: 100%">
              <el-option label="普通订单" value="NORMAL" />
              <el-option label="紧急订单" value="URGENT" />
              <el-option label="样品订单" value="SAMPLE" />
              <el-option label="返修订单" value="REPAIR" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预计交货日期" prop="expectedDeliveryDate">
            <el-date-picker
              v-model="form.expectedDeliveryDate"
              type="date"
              placeholder="请选择预计交货日期"
              style="width: 100%"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="货币" prop="currency">
            <el-select v-model="form.currency" placeholder="请选择货币" style="width: 100%">
              <el-option label="人民币 (CNY)" value="CNY" />
              <el-option label="美元 (USD)" value="USD" />
              <el-option label="欧元 (EUR)" value="EUR" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="特殊要求">
            <el-input
              v-model="form.specialRequirements"
              placeholder="请输入特殊要求"
              type="textarea"
              :rows="2"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 收货信息 -->
      <el-divider content-position="left">收货信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="收货地址" prop="deliveryAddress">
            <el-input
              v-model="form.deliveryAddress"
              placeholder="请输入完整收货地址（省市区街道门牌号等）"
              type="textarea"
              :rows="2"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="收货人" prop="deliveryContact">
            <el-input
              v-model="form.deliveryContact"
              placeholder="请输入收货人姓名"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="联系电话" prop="deliveryPhone">
            <el-input
              v-model="form.deliveryPhone"
              placeholder="请输入联系电话"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备注">
            <el-input
              v-model="form.remarks"
              placeholder="请输入备注信息"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 风险补充信息：当AI置信度低于阈值时显示 -->
      <template v-if="props.risk">
        <!-- 强烈风险提示 -->
        <el-alert 
          type="error" 
          show-icon 
          :closable="false" 
          title="⚠️ 高风险订单警告 ⚠️"
          style="margin-bottom: 15px;"
        >
          <template #default>
            <div style="font-weight: bold; color: #f56c6c; margin-bottom: 10px;">
              该订单AI识别置信度较低，存在重大风险！
            </div>
            <div style="color: #f56c6c; margin-bottom: 10px;">
              • 请务必仔细审核所有订单信息<br>
              • 请务必补充完整的收货地区信息<br>
              • 请务必验证商品信息的准确性
            </div>
            <div style="color: #e6a23c; font-weight: bold; border-top: 1px solid #e6a23c; padding-top: 10px;">
              ⚠️ 若您未进行人工确认，造成的风险由您自己承担 ⚠️
            </div>
          </template>
        </el-alert>
        
        <el-divider content-position="left">收货地区补充（必填）</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="省份">
              <el-input v-model="form.extendedFields!.province" placeholder="请输入省份" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市">
              <el-input v-model="form.extendedFields!.city" placeholder="请输入城市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="地区">
              <el-input v-model="form.extendedFields!.district" placeholder="请输入地区/区县" />
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <!-- 商品信息 -->
      <el-divider content-position="left">商品信息</el-divider>
      <div class="order-items">
        <div class="items-header">
          <h4>订单商品</h4>
          <el-button type="primary" size="small" @click="openProductSelector">
            <el-icon><Plus /></el-icon>
            选择商品
          </el-button>
        </div>
        
        <el-table :data="form.orderItems" border style="width: 100%">
                    <el-table-column label="商品名称" width="180">
            <template #default="{ row }">
              <span>{{ row.productName || '--' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="商品编码" width="120">
            <template #default="{ row }">
              <span>{{ row.productSku || '--' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="单位" width="80">
            <template #default="{ row }">
              <span>{{ row.unit || '--' }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="数量" width="140">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :precision="0"
                style="width: 100%"
                @change="calculateItemTotal($index)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="单价" width="160">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.unitPrice"
                :min="0"
                :precision="2"
                style="width: 100%"
                @change="() => calculateItemTotal($index)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              <span>¥{{ (parseFloat(row.subtotal || '0') || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="80">
            <template #default="{ row, $index }">
              <el-button
                type="danger"
                size="small"
                @click="() => removeOrderItem($index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 商品选择对话框 -->
      <el-dialog
        v-model="productSelectorVisible"
        title="选择商品"
        width="80%"
        :close-on-click-modal="true"
        @close="handleProductSelectorClose"
        class="product-selector-dialog"
      >
        <div class="product-selector-content">
          <!-- 搜索条件 -->
          <el-form :inline="true" class="search-form">
            <el-form-item label="商品名称">
              <el-input
                v-model="productSearchForm.name"
                placeholder="请输入商品名称"
                clearable
                @keyup.enter="searchProductsForSelector"
              />
            </el-form-item>
            <el-form-item label="商品编码">
              <el-input
                v-model="productSearchForm.sku"
                placeholder="请输入商品编码"
                clearable
                @keyup.enter="searchProductsForSelector"
              />
            </el-form-item>
            <el-form-item label="品牌">
              <el-input
                v-model="productSearchForm.brand"
                placeholder="请输入品牌"
                clearable
                @keyup.enter="searchProductsForSelector"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchProductsForSelector">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="resetProductSearch">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 商品列表 -->
          <el-table
            :data="productSelectorList"
            border
            style="width: 100%"
            @selection-change="handleProductSelectionChange"
            v-loading="productSelectorLoading"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column label="商品名称" prop="name" min-width="200" />
            <el-table-column label="商品编码" prop="sku" width="120" />
            <el-table-column label="品牌" prop="brand" width="100" />
            <el-table-column label="单位" width="100">
              <template #default="{ row }">
                <span>{{ row.unit || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="一件代发价" width="120">
              <template #default="{ row }">
                <span>¥{{ (parseFloat(row.sellingPrice || '0') || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="零售限价" width="120">
              <template #default="{ row }">
                <span>¥{{ (parseFloat(row.marketPrice || '0') || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="库存" width="80">
              <template #default="{ row }">
                <span>{{ row.availableQuantity || 0 }}</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="productSearchForm.page"
              v-model:page-size="productSearchForm.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="productSelectorTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleProductPageSizeChange"
              @current-change="handleProductPageChange"
            />
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handleProductSelectorClose">取消</el-button>
            <el-button type="primary" @click="confirmProductSelection">
              确定选择 ({{ selectedProducts.length }})
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 费用信息 -->
      <el-divider content-position="left">费用信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="商品总额">
            <span class="amount-display">¥{{ totalAmount.toFixed(2) }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="折扣金额">
            <el-input-number
              v-model="form.discountAmount"
              :min="0"
              :precision="2"
              style="width: 100%"
              @change="calculateFinalAmount"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="运费">
            <el-input-number
              v-model="form.shippingAmount"
              :min="0"
              :precision="2"
              style="width: 100%"
              @change="calculateFinalAmount"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="税费">
            <el-input-number
              v-model="form.taxAmount"
              :min="0"
              :precision="2"
              style="width: 100%"
              @change="calculateFinalAmount"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="最终金额">
            <span class="amount-display final-amount">¥{{ (parseFloat(form.finalAmount || '0') || 0).toFixed(2) }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="支付方式">
            <el-select v-model="form.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
              <el-option label="银行转账" value="BANK_TRANSFER" />
              <el-option label="现金" value="CASH" />
              <el-option label="支票" value="CHECK" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" :disabled="submitting">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit" 
          :loading="submitting"
          :disabled="submitting"
        >
          <el-icon v-if="submitting" class="is-loading"><Refresh /></el-icon>
          {{ submitting ? (mode === 'create' ? '创建中...' : '更新中...') : (mode === 'create' ? '创建' : '更新') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { orderApi } from '@/api/order'
import { customerApi } from '@/api/customer'
import { productApi } from '@/api/product'
import type { OrderResponse, OrderCreateRequest, OrderUpdateRequest } from '@/types/order'
import type { Customer } from '@/types/customer'
import type { Product } from '@/types/product'
// 暂时注释掉 china-area-data 导入，使用简化数据
// import pkg from 'china-area-data'
// const { pcaa } = pkg


// Props
interface Props {
  modelValue: boolean
  order?: OrderResponse | null
  mode: 'create' | 'edit'
  risk?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  order: null,
  mode: 'create',
  risk: false
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formRef = ref<FormInstance>()
const submitting = ref(false)
const submitProgress = ref(0)
const submitProgressText = ref('正在处理...')
const customerLoading = ref(false)
const productLoading = ref(false)
const customerOptions = ref<Customer[]>([])
const productOptions = ref<Product[]>([])

// 商品选择器相关
const productSelectorVisible = ref(false)
const productSelectorList = ref<Product[]>([])
const productSelectorTotal = ref(0)
const selectedProducts = ref<Product[]>([])
const productSelectorLoading = ref(false)

// 商品搜索表单
const productSearchForm = reactive({
  name: '',
  sku: '',
  brand: '',
  page: 1,
  size: 20
})



// 计算48小时后的日期
const getDefaultDeliveryDate = () => {
  const now = new Date()
  const deliveryDate = new Date(now.getTime() + 48 * 60 * 60 * 1000)
  return deliveryDate.toISOString().split('T')[0] // 格式化为 YYYY-MM-DD
}

// 表单数据
const form = reactive<OrderCreateRequest>({
  // 后端必需字段
  salesOrderId: 'MANUAL_' + Date.now(),
  salesUserId: 0, // 将从用户信息获取
  salesCompanyId: 0, // 将从用户信息获取
  customerId: 0,
  source: 'MANUAL',
  templateVersion: '1.0',
  remarks: '',
  extendedFields: {},
  orderItems: [],
  
  // 前端显示字段
  platformOrderNo: '', // 平台订单号
  orderType: 'NORMAL',
  expectedDeliveryDate: getDefaultDeliveryDate(),
  currency: 'CNY',
  specialRequirements: '',
  deliveryAddress: '',
  deliveryContact: '',
  deliveryPhone: '',
  discountAmount: '0',
  shippingAmount: '0',
  taxAmount: '0',
  finalAmount: '0',
  paymentMethod: 'BANK_TRANSFER'
})

// 表单验证规则
const rules: FormRules = {
  customerId: [
    { required: false, message: '请选择客户', trigger: 'change' }
  ],
  orderType: [
    { required: true, message: '请选择订单类型', trigger: 'change' }
  ],
  expectedDeliveryDate: [
    { required: true, message: '请选择预计交货日期', trigger: 'change' }
  ],
  deliveryAddress: [
    { required: true, message: '请输入收货地址', trigger: 'blur' },
    { min: 10, message: '收货地址至少需要10个字符，请包含省市区街道门牌号等详细信息', trigger: 'blur' },
    { max: 200, message: '收货地址不能超过200个字符', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        // 检查是否包含省市区信息
        const provincePattern = /(省|市|区|县|自治区|特别行政区)/
        const streetPattern = /(街道|路|号|室|楼|单元|幢|弄|巷|弄|小区|社区|大厦|广场|公园|学校|医院|银行|酒店|餐厅|超市|菜市场)/;
        
        if (!provincePattern.test(value)) {
          callback(new Error('收货地址应包含省市区信息'))
          return
        }
        
        if (!streetPattern.test(value)) {
          callback(new Error('收货地址应包含详细街道门牌号信息'))
          return
        }
        
        callback()
      }, 
      trigger: 'blur' 
    }
  ],
  deliveryContact: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '收货人姓名长度在2到20个字符', trigger: 'blur' }
  ],
  deliveryPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        
        // 手机号验证：1开头的11位数字
        const mobilePattern = /^1[3-9]\d{9}$/
        // 座机号验证：区号-号码 或 区号号码 格式
        const landlinePattern = /^0\d{2,3}-?\d{7,8}$/
        // 400/800电话验证
        const servicePattern = /^[48]00-?\d{3}-?\d{4}$/
        
        if (mobilePattern.test(value) || landlinePattern.test(value) || servicePattern.test(value)) {
          callback()
        } else {
          callback(new Error('请输入正确的电话号码格式（手机号、座机号或400/800服务电话）'))
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return props.mode === 'create' ? '新建订单' : '编辑订单'
})

const totalAmount = computed(() => {
  return form.orderItems.reduce((sum, item) => sum + parseFloat(item.subtotal || '0'), 0)
})

// 打开商品选择器
const openProductSelector = () => {
  productSelectorVisible.value = true
  searchProductsForSelector()
}

// 搜索商品
const searchProductsForSelector = async () => {
  try {
    productSelectorLoading.value = true
    
    // 构建查询参数
    const params: Record<string, any> = {
      page: productSearchForm.page,
      size: productSearchForm.size,
      name: productSearchForm.name || undefined,
      brand: productSearchForm.brand || undefined
    }
    
    // 过滤空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    // 使用销售商品API - 这是专门为销售用户设计的API
    const response = await fetch(`/api/products/sales?${new URLSearchParams(params)}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    console.log('销售商品API响应:', data) // 调试日志
    
    if (data.success && data.data) {
      productSelectorList.value = data.data.records || []
      productSelectorTotal.value = data.data.total || 0
    } else {
      console.warn('销售商品API返回格式异常:', data)
      productSelectorList.value = []
      productSelectorTotal.value = 0
    }
  } catch (error) {
    console.error('搜索商品失败:', error)
    ElMessage.error('搜索商品失败')
    productSelectorList.value = []
    productSelectorTotal.value = 0
  } finally {
    productSelectorLoading.value = false
  }
}

// 重置商品搜索
const resetProductSearch = () => {
  productSearchForm.name = ''
  productSearchForm.sku = ''
  productSearchForm.brand = ''
  productSearchForm.page = 1
  searchProductsForSelector()
}

// 处理商品选择变化
const handleProductSelectionChange = (selection: Product[]) => {
  selectedProducts.value = selection
}

// 处理商品分页大小变化
const handleProductPageSizeChange = (size: number) => {
  productSearchForm.size = size
  productSearchForm.page = 1
  searchProductsForSelector()
}

// 处理商品分页变化
const handleProductPageChange = (page: number) => {
  productSearchForm.page = page
  searchProductsForSelector()
}

// 处理商品选择器关闭
const handleProductSelectorClose = () => {
  productSelectorVisible.value = false
  selectedProducts.value = []
  productSelectorLoading.value = false
}

// 确认商品选择
const confirmProductSelection = () => {
  if (selectedProducts.value.length === 0) {
    ElMessage.warning('请选择至少一个商品')
    return
  }

  // 将选中的商品添加到订单中
  selectedProducts.value.forEach(product => {
    // 检查是否已经添加过
    const existingIndex = form.orderItems.findIndex(item => item.productId === product.id)
    if (existingIndex === -1) {
      form.orderItems.push({
        productId: product.id,
        quantity: 1,
        unitPrice: (product.sellingPrice || 0).toString(),
        remarks: `${product.name} - ${product.sku}`,
        productName: product.name,
        productSku: product.sku,
        productSpecifications: product.unit,
        unit: product.unit,
        subtotal: (product.sellingPrice || 0).toString()
      })
    }
  })

  // 重新计算总额
  calculateFinalAmount()
  
  // 关闭选择器
  productSelectorVisible.value = false
  selectedProducts.value = []
  
  ElMessage.success(`成功添加 ${selectedProducts.value.length} 个商品`)
}

// 监听器
watch(() => props.order, (newOrder) => {
  if (newOrder && props.mode === 'edit') {
    // 从后端数据填充表单
    Object.assign(form, {
      customerId: newOrder.customerId,
      orderType: newOrder.orderType,
      expectedDeliveryDate: newOrder.expectedDeliveryDate,
      currency: newOrder.currency,
      specialRequirements: newOrder.specialRequirements,
      deliveryAddress: newOrder.deliveryAddress,
      deliveryContact: newOrder.deliveryContact,
      deliveryPhone: newOrder.deliveryPhone,
      remarks: '',
      orderItems: newOrder.orderItems || [],
      discountAmount: newOrder.discountAmount,
      shippingAmount: newOrder.shippingAmount,
      taxAmount: newOrder.taxAmount,
      finalAmount: newOrder.finalAmount,
      paymentMethod: newOrder.paymentMethod
    })
    
    // 从extendedFields中恢复表单数据
    if (newOrder.extendedFields) {
      Object.assign(form, {
        orderType: newOrder.extendedFields.orderType || form.orderType,
        expectedDeliveryDate: newOrder.extendedFields.expectedDeliveryDate || form.expectedDeliveryDate,
        currency: newOrder.extendedFields.currency || form.currency,
        specialRequirements: newOrder.extendedFields.specialRequirements || form.specialRequirements,
        deliveryAddress: newOrder.extendedFields.deliveryAddress || form.deliveryAddress,
        deliveryContact: newOrder.extendedFields.deliveryContact || form.deliveryContact,
        deliveryPhone: newOrder.extendedFields.deliveryPhone || form.deliveryPhone,
        discountAmount: newOrder.extendedFields.discountAmount || form.discountAmount,
        shippingAmount: newOrder.extendedFields.shippingAmount || form.shippingAmount,
        taxAmount: newOrder.extendedFields.taxAmount || form.taxAmount,
        finalAmount: newOrder.extendedFields.finalAmount || form.finalAmount,
        paymentMethod: newOrder.extendedFields.paymentMethod || form.paymentMethod
      })
      
      // 恢复省市区信息
      if (newOrder.extendedFields.province || newOrder.extendedFields.city || newOrder.extendedFields.district) {
        form.extendedFields = {
          ...form.extendedFields,
          province: newOrder.extendedFields.province,
          city: newOrder.extendedFields.city,
          district: newOrder.extendedFields.district
        }
      }
    }
  }
}, { immediate: true })

// 监听对话框关闭，自动重置表单
watch(visible, (newVisible) => {
  if (!newVisible) {
    // 对话框关闭时，延迟重置表单，避免影响当前操作
    setTimeout(() => {
      resetForm()
    }, 100)
  }
})

// 生命周期
onMounted(() => {
  if (props.mode === 'create') {
    generateOrderNo()
    addOrderItem()
    // 获取当前用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (userInfo.id) {
      form.salesUserId = userInfo.id
      form.salesCompanyId = userInfo.companyId || 1
    }
  }
})

// 生成订单号
const generateOrderNo = async () => {
  try {
    const orderNo = await orderApi.generatePlatformOrderNo()
    form.platformOrderNo = orderNo
  } catch (error) {
    console.error('生成订单号失败:', error)
  }
}

// 搜索客户
const searchCustomers = async (query: string) => {
  if (query) {
    try {
      customerLoading.value = true
      const response = await customerApi.searchCustomers(query)
      customerOptions.value = response || []
    } catch (error) {
      console.error('搜索客户失败:', error)
    } finally {
      customerLoading.value = false
    }
  }
}

// 搜索商品
const searchProducts = async (query: string) => {
  if (query) {
    try {
      productLoading.value = true
      const response = await productApi.searchProducts(query)
      productOptions.value = response || []
    } catch (error) {
      console.error('搜索商品失败:', error)
    } finally {
      productLoading.value = false
    }
  }
}

// 添加订单项
const addOrderItem = () => {
  form.orderItems.push({
    productId: 0,
    quantity: 1,
    unitPrice: '0',
    remarks: '',
    productName: '',
    productSku: '',
    productSpecifications: '',
    unit: '',
    subtotal: '0'
  })
}

// 删除订单项
const removeOrderItem = (index: number) => {
  form.orderItems.splice(index, 1)
  calculateFinalAmount()
}

// 处理商品选择变化
const handleProductChange = (index: number) => {
  const item = form.orderItems[index]
  if (item.productId) {
    const product = productOptions.value.find(p => p.id === item.productId)
    if (product) {
      // 使用默认价格或0
      item.unitPrice = '0'
      calculateItemTotal(index)
    }
  }
}

// 计算商品小计
const calculateItemTotal = (index: number) => {
  const item = form.orderItems[index]
  const quantity = item.quantity || 0
  const unitPrice = parseFloat(item.unitPrice || '0')
  const subtotal = quantity * unitPrice
  item.subtotal = subtotal.toFixed(2)
  calculateFinalAmount()
}

// 计算最终金额
const calculateFinalAmount = () => {
  const total = totalAmount.value
  const discount = parseFloat(form.discountAmount || '0')
  const shipping = parseFloat(form.shippingAmount || '0')
  const tax = parseFloat(form.taxAmount || '0')
  const final = total - discount + shipping + tax
  form.finalAmount = final.toFixed(2)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (form.orderItems.length === 0) {
      ElMessage.warning('请至少添加一个商品')
      return
    }
    
    // 开始提交，显示进度
    submitting.value = true
    submitProgress.value = 0
    submitProgressText.value = '正在验证数据...'
    
    // 模拟进度更新
    const progressInterval = setInterval(() => {
      if (submitProgress.value < 90) {
        submitProgress.value += Math.random() * 15
        if (submitProgress.value > 30) submitProgressText.value = '正在处理订单信息...'
        if (submitProgress.value > 60) submitProgressText.value = '正在保存到数据库...'
        if (submitProgress.value > 80) submitProgressText.value = '即将完成...'
      }
    }, 200)
    
    // 发送所有表单数据到后端
    const submitData = {
      platformOrderNo: form.platformOrderNo,
      salesOrderId: form.platformOrderNo || form.salesOrderId,
      salesUserId: form.salesUserId,
      salesCompanyId: form.salesCompanyId,
      customerId: form.customerId,
      source: form.source,
      templateVersion: form.templateVersion,
      remarks: form.remarks,
      extendedFields: {
        ...form.extendedFields,
        // 基本信息
        orderType: form.orderType,
        expectedDeliveryDate: form.expectedDeliveryDate,
        currency: form.currency,
        specialRequirements: form.specialRequirements,
        // 收货信息
        deliveryAddress: form.deliveryAddress,
        deliveryContact: form.deliveryContact,
        deliveryPhone: form.deliveryPhone,
        // 费用信息
        discountAmount: form.discountAmount,
        shippingAmount: form.shippingAmount,
        taxAmount: form.taxAmount,
        finalAmount: form.finalAmount,
        paymentMethod: form.paymentMethod,
        // 风险补充信息（如果有的话）
        province: form.extendedFields?.province,
        city: form.extendedFields?.city,
        district: form.extendedFields?.district
      },
      orderItems: form.orderItems.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        remarks: item.remarks,
        extendedFields: {
          ...item.extendedFields,
          productName: item.productName,
          productSku: item.productSku,
          productSpecifications: item.productSpecifications,
          unit: item.unit,
          subtotal: item.subtotal
        }
      }))
    }
    
    if (props.mode === 'create') {
      submitProgressText.value = '正在创建订单...'
      await orderApi.createOrder(submitData)
      submitProgress.value = 100
      submitProgressText.value = '订单创建成功！'
      ElMessage.success('订单创建成功')
    } else {
      submitProgressText.value = '正在更新订单...'
      // 更新订单需要不同的字段
      const updateData: OrderUpdateRequest = {
        customerId: form.customerId,
        remarks: form.remarks,
        extendedFields: form.extendedFields,
        orderItems: form.orderItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          remarks: item.remarks,
          extendedFields: item.extendedFields
        }))
      }
      await orderApi.updateOrder(props.order!.id, updateData)
      submitProgress.value = 100
      submitProgressText.value = '订单更新成功！'
      ElMessage.success('订单更新成功')
    }
    
    // 延迟一下让用户看到完成状态
    setTimeout(() => {
      emit('success')
    }, 500)
    
  } catch (error) {
    console.error('提交失败:', error)
    submitProgressText.value = '操作失败'
    ElMessage.error('提交失败')
  } finally {
    // 延迟重置状态，让用户看到最终结果
    setTimeout(() => {
      submitting.value = false
      submitProgress.value = 0
      submitProgressText.value = '正在处理...'
    }, 1000)
  }
}

// 关闭对话框
const handleClose = () => {
  ElMessageBox.confirm('确定要关闭吗？未保存的数据将丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    visible.value = false
    resetForm()
  }).catch(() => {
    // 用户取消
  })
}

// 重置表单数据
const resetForm = () => {
  // 重置所有表单字段到初始状态
  Object.assign(form, {
    // 后端必需字段
    salesOrderId: 'MANUAL_' + Date.now(),
    salesUserId: 0,
    salesCompanyId: 0,
    customerId: 0,
    source: 'MANUAL',
    templateVersion: '1.0',
    remarks: '',
    extendedFields: {},
    orderItems: [],
    
    // 前端显示字段
    platformOrderNo: '', // 平台订单号
    orderType: 'NORMAL',
    expectedDeliveryDate: getDefaultDeliveryDate(),
    currency: 'CNY',
    specialRequirements: '',
    deliveryAddress: '',
    deliveryContact: '',
    deliveryPhone: '',
    discountAmount: '0',
    shippingAmount: '0',
    taxAmount: '0',
    finalAmount: '0',
    paymentMethod: 'BANK_TRANSFER'
  })
  
  // 清空表单验证状态
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}


</script>

<style scoped>
.order-form {
  max-height: 70vh;
  overflow-y: auto;
}

/* 进度条样式 */
.progress-bar {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.progress-text {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
  color: #409eff;
  font-weight: 500;
}

.progress-text .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

/* 加载中的图标动画 */
.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.order-items {
  margin: 20px 0;
}

.items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.items-header h4 {
  margin: 0;
  color: #303133;
}

.amount-display {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.final-amount {
  color: #e6a23c;
  font-size: 18px;
}

.dialog-footer {
  text-align: right;
}

  :deep(.el-divider__text) {
    font-size: 16px;
    font-weight: bold;
    color: #409eff;
  }
</style>
