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
              placeholder="请选择客户"
              filterable
              remote
              :remote-method="searchCustomers"
              :loading="customerLoading"
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
              placeholder="请输入收货地址"
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
          <el-button type="primary" size="small" @click="addOrderItem">
            <el-icon><Plus /></el-icon>
            添加商品
          </el-button>
        </div>
        
        <el-table :data="form.orderItems" border style="width: 100%">
          <el-table-column label="商品名称" width="200">
            <template #default="{ row, $index }">
              <el-select
                v-model="row.productId"
                placeholder="请选择商品"
                filterable
                remote
                :remote-method="searchProducts"
                :loading="productLoading"
                style="width: 100%"
                @change="handleProductChange($index)"
              >
                <el-option
                  v-for="product in productOptions"
                  :key="product.id"
                  :label="product.name"
                  :value="product.id"
                >
                  <span>{{ product.name }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ product.sku }}
                  </span>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          
          <el-table-column label="规格" width="150">
            <template #default="{ row }">
              <el-input v-model="row.productSpecifications" placeholder="规格" />
            </template>
          </el-table-column>
          
          <el-table-column label="数量" width="100">
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
          
          <el-table-column label="单价" width="120">
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
          
          <el-table-column label="小计" width="120">
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
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { orderApi } from '@/api/order'
import { customerApi } from '@/api/customer'
import { productApi } from '@/api/product'
import type { OrderResponse, OrderCreateRequest, OrderUpdateRequest } from '@/types/order'
import type { Customer } from '@/types/customer'
import type { Product } from '@/types/product'

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

// 表单数据
const form = reactive<OrderCreateRequest>({
  customerId: null,
  orderType: 'NORMAL',
  expectedDeliveryDate: '',
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
    { required: true, message: '请选择客户', trigger: 'change' }
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
      remarks: newOrder.remarks,
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
    
    submitting.value = true
    
    if (props.mode === 'create') {
      await orderApi.createOrder(form)
      ElMessage.success('订单创建成功')
    } else {
      const updateData: OrderUpdateRequest = { ...form }
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
