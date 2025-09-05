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
          <el-form-item label="所属公司" prop="companyId">
            <el-select
              v-model="form.companyId"
              placeholder="请选择所属公司"
              clearable
              style="width: 100%"
            >
              <el-option label="个人用户" :value="null" />
              <el-option
                v-for="company in availableCompanies"
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
          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="form.realName"
              placeholder="请输入真实姓名"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <!-- 占位，保持布局平衡 -->
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="角色" prop="roleId">
            <el-select
              v-model="form.roleId"
              placeholder="请选择角色"
              style="width: 100%"
              clearable
            >
              <el-option
                v-for="role in availableRoles"
                :key="role.id"
                :label="role.name"
                :value="role.id"
              />
            </el-select>
            <div class="form-tip">当前选择: {{ form.roleId ? availableRoles.find(r => r.id === form.roleId)?.name : '未选择' }}</div>
          </el-form-item>
        </el-col>
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
              <el-option label="待激活" value="PENDING" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="价格分层" prop="priceTierId">
            <el-select
              v-model="form.priceTierId"
              placeholder="请选择价格分层等级"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="tier in availablePriceTiers"
                :key="tier.id"
                :label="tier.tierName"
                :value="tier.id"
              >
                <span style="float: left">{{ tier.tierName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  {{ tier.tierType }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="部门" prop="department">
            <el-input
              v-model="form.department"
              placeholder="请输入部门"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="!isEdit" :gutter="20">
        <el-col :span="12">
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>
        </el-col>
      </el-row>
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
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createUser, updateUser, getAvailableRoles } from '@/api/user'
import { companyApi } from '@/api/company'
import { roleApi } from '@/api/role'
import { priceTierApi } from '@/api/priceTier'
import type { User } from '@/types/user'

interface Props {
  visible: boolean
  userData?: User | null
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  userData: null
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const availableRoles = ref<any[]>([])
const availablePriceTiers = ref<any[]>([])
const availableCompanies = ref<any[]>([])

// 表单数据
const form = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  roleId: undefined as number | undefined,
  status: 'ACTIVE',
  password: '',
  confirmPassword: '',
  priceTierId: undefined as number | undefined,
  department: '',
  companyId: undefined as number | undefined
})

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度在2到20个字符', trigger: 'blur' }
  ],
  companyId: [
    { 
      validator: (rule, value, callback) => {
        if (value === undefined || value === null) {
          callback() // 公司ID可以为空
        } else if (isNaN(Number(value))) {
          callback(new Error('请选择有效的公司'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (value === undefined || value === null) {
          callback(new Error('请选择角色'))
        } else if (isNaN(Number(value))) {
          callback(new Error('请选择有效的角色'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const isEdit = computed(() => !!props.userData?.id)

// 获取可用角色
const fetchAvailableRoles = async () => {
  try {
    const response = await getAvailableRoles()
    console.log('角色API响应:', response)
    if (response && response.data && Array.isArray(response.data)) {
      availableRoles.value = response.data
      console.log('可用角色已加载:', availableRoles.value)
    } else {
      console.warn('角色数据格式不正确:', response)
      availableRoles.value = []
    }
  } catch (error) {
    console.error('获取可用角色失败:', error)
    ElMessage.error('获取可用角色失败')
    availableRoles.value = []
  }
}

const fetchAvailablePriceTiers = async () => {
  try {
    // 获取当前用户信息，确定公司ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const currentCompanyId = userInfo.companyId
    
    if (!currentCompanyId) {
      console.warn('当前用户没有公司ID，无法获取价格分层')
      availablePriceTiers.value = []
      return
    }
    
    // 调用真实的价格分层API，按公司ID过滤
    const response = await priceTierApi.getPriceTierList({ 
      page: 1, 
      size: 100,
      companyId: currentCompanyId
    })
    
    if (response && Array.isArray(response)) {
      availablePriceTiers.value = response.map((tier: any) => ({
        id: tier.id,
        tierName: tier.tierName,
        tierType: tier.tierType || 'RETAIL'
      }))
      console.log('获取到价格分层数据:', availablePriceTiers.value)
    } else {
      console.warn('价格分层API返回格式不正确:', response)
      availablePriceTiers.value = []
    }
  } catch (error) {
    console.error('获取价格分层失败:', error)
    ElMessage.warning('获取价格分层失败')
    availablePriceTiers.value = []
  }
}

// 获取可用公司列表
const fetchAvailableCompanies = async () => {
  try {
    const response = await companyApi.getAvailableCompanies()
    console.log('公司API响应:', response)
    if (response && response.data && Array.isArray(response.data)) {
      availableCompanies.value = response.data
      console.log('公司列表已加载:', availableCompanies.value)
    } else {
      console.warn('公司数据格式不正确:', response)
      availableCompanies.value = []
    }
  } catch (error) {
    console.error('获取公司列表失败:', error)
    ElMessage.warning('获取公司列表失败')
    availableCompanies.value = []
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: '',
    username: '',
    companyId: undefined,
    email: '',
    phone: '',
    roleId: undefined,
    status: 'ACTIVE',
    priceTierId: undefined,
    department: '',
    password: '',
    confirmPassword: ''
  })
  formRef.value?.clearValidate()
}

// 填充表单数据
const fillFormData = () => {
  if (props.userData) {
    console.log('编辑用户数据:', props.userData)
    console.log('当前可用角色列表:', availableRoles.value)
    
    // 根据roleName找到对应的roleId
    let roleId: number | undefined
    if (props.userData.roleName && availableRoles.value.length > 0) {
      const matchedRole = availableRoles.value.find(role => role.name === props.userData.roleName)
      roleId = matchedRole ? matchedRole.id : undefined
      console.log('角色映射:', { 
        roleName: props.userData.roleName, 
        roleId, 
        matchedRole,
        availableRoles: availableRoles.value 
      })
    } else if (props.userData.roleId !== null && props.userData.roleId !== undefined) {
      // 如果roleName没有找到，尝试使用roleId（向后兼容）
      const converted = Number(props.userData.roleId)
      roleId = isNaN(converted) ? undefined : converted
      console.log('使用roleId回填:', { roleId: props.userData.roleId, converted })
    } else {
      console.warn('无法找到角色信息:', { 
        roleName: props.userData.roleName, 
        roleId: props.userData.roleId,
        availableRolesCount: availableRoles.value.length 
      })
    }
    
    // 安全地转换 priceTierId，避免 NaN
    let priceTierId: number | undefined
    if (props.userData.priceTierId !== null && props.userData.priceTierId !== undefined) {
      const converted = Number(props.userData.priceTierId)
      priceTierId = isNaN(converted) ? undefined : converted
    }
    
    // 安全地转换 companyId，避免 NaN
    let companyId: number | undefined
    if (props.userData.companyId !== null && props.userData.companyId !== undefined) {
      const converted = Number(props.userData.companyId)
      companyId = isNaN(converted) ? undefined : converted
    }
    
    Object.assign(form, {
      id: props.userData.id,
      username: props.userData.username,
      realName: props.userData.personalCompanyName || '', // 映射personalCompanyName到realName
      companyId: companyId, // 使用安全转换后的值
      email: props.userData.email,
      phone: props.userData.phone,
      roleId: roleId, // 使用安全转换后的值
      status: props.userData.status,
      priceTierId: priceTierId, // 使用安全转换后的值
      department: props.userData.department,
      password: '',
      confirmPassword: ''
    })
    console.log('表单数据已填充:', form)
  } else {
    Object.assign(form, {
      id: '',
      username: '',
      realName: '',
      companyId: undefined,
      email: '',
      phone: '',
      roleId: undefined,
      status: 'ACTIVE',
      priceTierId: undefined,
      department: '',
      password: '',
      confirmPassword: ''
    })
  }
}

// 监听 userData 变化 - 这是回填的核心！
watch(() => props.userData, (newVal) => {
  console.log('userData 变化:', newVal)
  if (newVal) {
    // 确保在下一个 tick 执行，让 DOM 更新完成
    nextTick(() => {
      fillFormData()
    })
  } else {
    resetForm()
  }
}, { immediate: true, deep: true })

// 监听对话框显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    nextTick(async () => {
      // 先加载可用数据
      await Promise.all([
        fetchAvailableRoles(),
        fetchAvailablePriceTiers(),
        fetchAvailableCompanies()
      ])
      // 如果已有用户数据，则填充表单
      if (props.userData) {
        fillFormData()
      }
    })
  } else {
    // 对话框关闭时重置表单
    resetForm()
  }
})

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      // 编辑用户
      const updateData: any = {
        id: form.id,
        username: form.username,
        email: form.email,
        phone: form.phone,
        roleId: form.roleId,
        companyId: form.companyId,
        priceTierId: form.priceTierId,
        department: form.department,
        status: form.status
      }
      await updateUser(Number(form.id), updateData)
      ElMessage.success('更新成功')
    } else {
      // 新增用户
      const createData: any = {
        username: form.username,
        email: form.email,
        phone: form.phone,
        roleId: form.roleId,
        companyId: form.companyId,
        priceTierId: form.priceTierId,
        department: form.department,
        status: form.status,
        password: form.password
      }
      await createUser(createData)
      ElMessage.success('创建成功')
    }
    
    emit('success')
  } catch (error) {
    console.error('表单提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
