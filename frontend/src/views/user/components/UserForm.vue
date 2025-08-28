<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="600px"
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
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :disabled="isEdit"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="form.realName"
              placeholder="请输入真实姓名"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="部门" prop="department">
            <el-input
              v-model="form.department"
              placeholder="请输入部门"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="职位" prop="position">
            <el-input
              v-model="form.position"
              placeholder="请输入职位"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="角色" prop="roleId">
            <el-select
              v-model="form.roleId"
              placeholder="请选择角色"
              style="width: 100%"
            >
              <el-option
                v-for="role in roleOptions"
                :key="role.id"
                :label="role.name"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属公司" prop="companyId">
            <el-select
              v-model="form.companyId"
              placeholder="请选择公司"
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
              <el-option label="锁定" value="LOCKED" />
              <el-option label="待审核" value="PENDING" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="密码" prop="password" v-if="!isEdit">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="头像" prop="avatar">
        <el-input
          v-model="form.avatar"
          placeholder="请输入头像URL"
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
import { createUser, updateUser, getRoleList, getCompanyList } from '@/api/user'
import type { User, Role, Company } from '@/types/user'

// Props
interface Props {
  visible: boolean
  userData?: User | null
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  userData: null
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const roleOptions = ref<Role[]>([])
const companyOptions = ref<Company[]>([])

// 表单数据
const form = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  realName: '',
  avatar: '',
  roleId: undefined as number | undefined,
  companyId: undefined as number | undefined,
  department: '',
  position: '',
  status: 'ACTIVE'
})

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  companyId: [
    { required: true, message: '请选择所属公司', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const isEdit = computed(() => !!props.userData)

// 获取角色列表
const fetchRoleList = async () => {
  try {
    const response = await getRoleList()
    roleOptions.value = response.data
  } catch (error) {
    ElMessage.error('获取角色列表失败')
    console.error('获取角色列表失败:', error)
  }
}

// 获取公司列表
const fetchCompanyList = async () => {
  try {
    const response = await getCompanyList()
    companyOptions.value = response.data
  } catch (error) {
    ElMessage.error('获取公司列表失败')
    console.error('获取公司列表失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    username: '',
    password: '',
    email: '',
    phone: '',
    realName: '',
    avatar: '',
    roleId: undefined,
    companyId: undefined,
    department: '',
    position: '',
    status: 'ACTIVE'
  })
  formRef.value?.clearValidate()
}

// 填充表单数据
const fillFormData = (userData: User) => {
  Object.assign(form, {
    username: userData.username,
    email: userData.email || '',
    phone: userData.phone || '',
    realName: userData.realName || '',
    avatar: userData.avatar || '',
    roleId: userData.roleId,
    companyId: userData.companyId,
    department: userData.department || '',
    position: userData.position || '',
    status: userData.status
  })
}

// 监听用户数据变化
watch(() => props.userData, (newVal) => {
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
    
    if (isEdit.value && props.userData) {
      // 编辑用户
      const updateData = { ...form }
      delete updateData.password
      await updateUser(props.userData.id, updateData)
      ElMessage.success('更新成功')
    } else {
      // 新增用户
      await createUser(form)
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
  fetchRoleList()
  fetchCompanyList()
})
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
