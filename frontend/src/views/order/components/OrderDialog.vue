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
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="平台订单号" prop="platformOrderId">
            <el-input
              v-model="form.platformOrderId"
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
              <span>¥{{ row.subtotal?.toFixed(2) || '0.00' }}</span>
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
                <span>¥{{ row.sellingPrice?.toFixed(2) || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="零售限价" width="120">
              <template #default="{ row }">
                <span>¥{{ row.marketPrice?.toFixed(2) || '--' }}</span>
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
            <span class="amount-display final-amount">¥{{ form.finalAmount?.toFixed(2) || '0.00' }}</span>
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
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ mode === 'create' ? '创建' : '更新' }}
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
}

const props = withDefaults(defineProps<Props>(), {
  order: null,
  mode: 'create'
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
  customerId: null,
  orderType: 'NORMAL',
  expectedDeliveryDate: getDefaultDeliveryDate(),
  currency: 'CNY',
  specialRequirements: '',
  deliveryAddress: '',
  deliveryContact: '',
  deliveryPhone: '',
  remarks: '',
  orderItems: [],
  discountAmount: 0,
  shippingAmount: 0,
  taxAmount: 0,
  finalAmount: 0,
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
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ],
  deliveryContact: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  deliveryPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return props.mode === 'create' ? '新建订单' : '编辑订单'
})

const totalAmount = computed(() => {
  return form.orderItems.reduce((sum, item) => sum + (item.subtotal || 0), 0)
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
        productName: product.name,
        productSku: product.sku,
        productSpecifications: product.unit,
        unit: product.unit,
        quantity: 1,
        unitPrice: product.sellingPrice || 0,
        subtotal: product.sellingPrice || 0
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
    Object.assign(form, {
      customerId: newOrder.customerId,
      orderType: newOrder.orderType,
      expectedDeliveryDate: newOrder.expectedDeliveryDate,
      currency: newOrder.currency,
      specialRequirements: newOrder.specialRequirements,
      deliveryAddress: newOrder.deliveryAddress,
      deliveryContact: newOrder.deliveryContact,
      deliveryPhone: newOrder.deliveryPhone,
      remarks: newOrder.remarks || '',
      orderItems: newOrder.orderItems || [],
      discountAmount: newOrder.discountAmount,
      shippingAmount: newOrder.shippingAmount,
      taxAmount: newOrder.taxAmount,
      finalAmount: newOrder.finalAmount,
      paymentMethod: newOrder.paymentMethod
    })
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  if (props.mode === 'create') {
    generateOrderNo()
    addOrderItem()
  }
})

// 生成订单号
const generateOrderNo = async () => {
  try {
    const orderNo = await orderApi.generatePlatformOrderNo()
    form.platformOrderId = orderNo
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
    productId: null,
    productSpecifications: '',
    quantity: 1,
    unitPrice: 0,
    subtotal: 0
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
      item.unitPrice = 0
      calculateItemTotal(index)
    }
  }
}

// 计算商品小计
const calculateItemTotal = (index: number) => {
  const item = form.orderItems[index]
  item.subtotal = (item.quantity || 0) * (item.unitPrice || 0)
  calculateFinalAmount()
}

// 计算最终金额
const calculateFinalAmount = () => {
  form.finalAmount = totalAmount.value - (form.discountAmount || 0) + (form.shippingAmount || 0) + (form.taxAmount || 0)
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
    
    // 直接使用输入的地址
    const submitData = { ...form }
    
    submitting.value = true
    
    if (props.mode === 'create') {
      await orderApi.createOrder(submitData)
      ElMessage.success('订单创建成功')
    } else {
      const updateData: OrderUpdateRequest = { ...submitData }
      await orderApi.updateOrder(props.order!.id, updateData)
      ElMessage.success('订单更新成功')
    }
    
    emit('success')
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
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
  }).catch(() => {
    // 用户取消
  })
}


</script>

<style scoped>
.order-form {
  max-height: 70vh;
  overflow-y: auto;
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
