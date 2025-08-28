<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑公司' : '新增公司'"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent="handleSubmit"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="公司名称" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入公司名称"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="业务类型" prop="type">
            <el-select
              v-model="form.type"
              placeholder="请选择业务类型"
              style="width: 100%"
            >
              <el-option label="供应商" value="SUPPLIER" />
              <el-option label="销售商" value="SALES" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select
              v-model="form.status"
              placeholder="请选择状态"
              style="width: 100%"
            >
              <el-option label="激活" value="ACTIVE" />
              <el-option label="未激活" value="INACTIVE" />
              <el-option label="暂停" value="SUSPENDED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="营业执照号" prop="businessLicense">
            <el-input
              v-model="form.businessLicense"
              placeholder="请输入营业执照号"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="联系人" prop="contactPerson">
            <el-input
              v-model="form.contactPerson"
              placeholder="请输入联系人"
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
          <el-form-item label="联系邮箱" prop="contactEmail">
            <el-input
              v-model="form.contactEmail"
              placeholder="请输入联系邮箱"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="公司地址" prop="address">
            <el-input
              v-model="form.address"
              placeholder="请输入公司地址"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="公司描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入公司描述"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createCompany, updateCompany } from '@/api/company'
import type { Company } from '@/types/company'

// Props
interface Props {
  visible: boolean
  companyData?: Company | null
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  companyData: null
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const form = reactive({
  name: '',
  type: 'SUPPLIER',
  status: 'ACTIVE',
  businessLicense: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  description: ''
})

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入公司名称', trigger: 'blur' },
    { min: 2, max: 100, message: '公司名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择业务类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  contactPerson: [
    { required: false, message: '请输入联系人', trigger: 'blur' },
    { max: 50, message: '联系人不能超过 50 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: false, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  contactEmail: [
    { required: false, message: '请输入联系邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  address: [
    { required: false, message: '请输入公司地址', trigger: 'blur' },
    { max: 200, message: '公司地址不能超过 200 个字符', trigger: 'blur' }
  ],
  description: [
    { required: false, message: '请输入公司描述', trigger: 'blur' },
    { max: 500, message: '公司描述不能超过 500 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const isEdit = computed(() => !!props.companyData)

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    name: '',
    type: 'SUPPLIER',
    status: 'ACTIVE',
    businessLicense: '',
    contactPerson: '',
    contactPhone: '',
    contactEmail: '',
    address: '',
    description: ''
  })
  formRef.value?.clearValidate()
}

// 填充表单数据
const fillFormData = (companyData: Company) => {
  Object.assign(form, {
    name: companyData.name || '',
    type: companyData.type || 'SUPPLIER',
    status: companyData.status || 'ACTIVE',
    businessLicense: companyData.businessLicense || '',
    contactPerson: companyData.contactPerson || '',
    contactPhone: companyData.contactPhone || '',
    contactEmail: companyData.contactEmail || '',
    address: companyData.address || '',
    description: companyData.description || ''
  })
}

// 监听公司数据变化
watch(() => props.companyData, (newVal) => {
  if (newVal) {
    fillFormData(newVal)
  } else {
    resetForm()
  }
}, { immediate: true })

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value && props.companyData) {
      // 编辑公司
      await updateCompany(props.companyData.id, form)
      ElMessage.success('更新成功')
    } else {
      // 新增公司
      await createCompany(form)
      ElMessage.success('创建成功')
    }
    
    emit('success')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      console.error('提交表单失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

// 组件挂载时获取数据
onMounted(() => {
  // 可以在这里获取一些基础数据
})
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
