<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="700px"
    :before-close="handleClose"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="customer-form"
    >
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="客户编码" prop="customerCode">
            <el-input
              v-model="form.customerCode"
              placeholder="请输入客户编码"
              :disabled="mode === 'edit'"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户名称" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入客户名称"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属公司">
            <el-select
              v-model="form.companyId"
              placeholder="请选择所属公司"
              clearable
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="company in companyOptions"
                :key="company.id"
                :label="company.name"
                :value="company.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户类型" prop="customerType">
            <el-select v-model="form.customerType" placeholder="请选择客户类型" style="width: 100%">
              <el-option label="个人" value="INDIVIDUAL" />
              <el-option label="企业" value="ENTERPRISE" />
              <el-option label="经销商" value="DEALER" />
              <el-option label="代理商" value="AGENT" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="客户等级" prop="customerLevel">
            <el-select v-model="form.customerLevel" placeholder="请选择客户等级" style="width: 100%">
              <el-option label="普通客户" value="NORMAL" />
              <el-option label="VIP客户" value="VIP" />
              <el-option label="重要客户" value="IMPORTANT" />
              <el-option label="战略客户" value="STRATEGIC" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择客户状态" style="width: 100%">
              <el-option label="活跃" value="ACTIVE" />
              <el-option label="非活跃" value="INACTIVE" />
              <el-option label="黑名单" value="BLACKLIST" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 联系信息 -->
      <el-divider content-position="left">联系信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="联系人" prop="contactPerson">
            <el-input
              v-model="form.contactPerson"
              placeholder="请输入联系人姓名"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input
              v-model="form.contactPhone"
              placeholder="请输入联系电话"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="联系邮箱">
            <el-input
              v-model="form.contactEmail"
              placeholder="请输入联系邮箱"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="地址">
            <el-input
              v-model="form.address"
              placeholder="请输入地址"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 业务信息 -->
      <el-divider content-position="left">业务信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="信用额度">
            <el-input-number
              v-model="form.creditLimit"
              :min="0"
              :precision="2"
              style="width: 100%"
              placeholder="请输入信用额度"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="付款条件">
            <el-input
              v-model="form.paymentTerms"
              placeholder="请输入付款条件"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="税号">
            <el-input
              v-model="form.taxNumber"
              placeholder="请输入税号"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="银行账户">
            <el-input
              v-model="form.bankAccount"
              placeholder="请输入银行账户"
            />
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
import type { FormInstance, FormRules } from 'element-plus'
import { customerApi } from '@/api/customer'
import { companyApi } from '@/api/company'
import type { Customer, CustomerCreateRequest, CustomerUpdateRequest } from '@/types/customer'
import type { Company } from '@/types/company'

// Props
interface Props {
  modelValue: boolean
  customer?: Customer | null
  mode: 'create' | 'edit'
}

const props = withDefaults(defineProps<Props>(), {
  customer: null,
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
const companyOptions = ref<Company[]>([])

// 表单数据
const form = reactive<CustomerCreateRequest>({
  customerCode: '',
  name: '',
  companyId: undefined,
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  customerType: 'INDIVIDUAL',
  customerLevel: 'NORMAL',
  creditLimit: undefined,
  paymentTerms: '',
  taxNumber: '',
  bankAccount: '',
  status: 'ACTIVE'
})

// 表单验证规则
const rules: FormRules = {
  customerCode: [
    { required: true, message: '请输入客户编码', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  customerType: [
    { required: true, message: '请选择客户类型', trigger: 'change' }
  ],
  customerLevel: [
    { required: true, message: '请选择客户等级', trigger: 'change' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择客户状态', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return props.mode === 'create' ? '新建客户' : '编辑客户'
})

// 监听器
watch(() => props.customer, (newCustomer) => {
  if (newCustomer && props.mode === 'edit') {
    Object.assign(form, {
      customerCode: newCustomer.customerCode,
      name: newCustomer.name,
      companyId: newCustomer.companyId,
      contactPerson: newCustomer.contactPerson,
      contactPhone: newCustomer.contactPhone,
      contactEmail: newCustomer.contactEmail,
      address: newCustomer.address,
      customerType: newCustomer.customerType,
      customerLevel: newCustomer.customerLevel,
      creditLimit: newCustomer.creditLimit,
      paymentTerms: newCustomer.paymentTerms,
      taxNumber: newCustomer.taxNumber,
      bankAccount: newCustomer.bankAccount,
      status: newCustomer.status
    })
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  loadCompanyOptions()
})

// 加载公司选项
const loadCompanyOptions = async () => {
  try {
    const response = await companyApi.getCompanyList()
    companyOptions.value = response || []
  } catch (error) {
    console.error('加载公司列表失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (props.mode === 'create') {
      // 创建客户时，将form转换为Customer类型
      const customerData: Customer = {
        ...form,
        id: 0, // 临时ID，后端会重新分配
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        deleted: false
      }
      await customerApi.createCustomer(customerData)
      ElMessage.success('客户创建成功')
    } else {
      // 更新客户时，将form转换为CustomerUpdateRequest类型
      const updateData: CustomerUpdateRequest = { ...form }
      await customerApi.updateCustomer(props.customer!.id, updateData)
      ElMessage.success('客户更新成功')
    }
    
    emit('success')
  } catch (error: any) {
    console.error('提交失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('提交失败，请检查表单信息')
    }
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
.customer-form {
  max-height: 70vh;
  overflow-y: auto;
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
