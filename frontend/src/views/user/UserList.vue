<template>
  <div class="user-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
    
        <el-form-item label="邮箱">
          <el-input
            v-model="searchForm.email"
            placeholder="请输入邮箱"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="公司名称">
          <el-select v-model="searchForm.companyId" placeholder="请选择公司名称" clearable style="width: 200px;">
            
            <el-option
              v-for="company in companyOptions"
              :key="company.id"
              :label="company.name"
              :value="company.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="未激活" value="INACTIVE" />
            <el-option label="锁定" value="LOCKED" />
            <el-option label="待激活" value="PENDING" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <div class="table-area">
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="companyName" label="所属公司" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="roleName" label="角色名" width="120" />
    
        <el-table-column prop="priceTierName" label="价格分层" width="120">
          <template #default="{ row }">
            <span v-if="row.priceTierName">
              <el-tag :type="getPriceTierType(row.priceTierType)" size="small">
                {{ row.priceTierName }}
              </el-tag>
            </span>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="warning"
              @click="handlePasswordReset(row)"
            >
              密码重置
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-area">
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

    <!-- 用户表单对话框 -->
    <UserForm
      v-model:visible="formVisible"
      :user-data="currentUser"
      @success="handleFormSuccess"
    />

    <!-- 密码重置对话框 -->
    <el-dialog
      v-model="passwordResetVisible"
      title="密码重置"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordResetFormRef"
        :model="passwordResetForm"
        :rules="passwordResetRules"
        label-width="100px"
      >
        <el-form-item label="用户名">
          <el-input :model-value="currentUser?.username || ''" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordResetForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordResetForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="passwordResetVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordReset" :loading="submitting">
            确认重置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import UserForm from './components/UserForm.vue'
import { getUserList, deleteUser, resetPassword, getAvailableCompanies } from '@/api/user'
import type { User } from '@/types/user'

// 响应式数据
const loading = ref(false)
const userList = ref<User[]>([])
const formVisible = ref(false)
const currentUser = ref<User | null>(null)
const passwordResetVisible = ref(false)
const submitting = ref(false)
const passwordResetFormRef = ref<FormInstance>()
const companyOptions = ref<any[]>([])

// 搜索表单
const searchForm = reactive({
  username: '',
  companyName: '',
  email: '',
  companyId: null as number | null,
  status: ''
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 密码重置表单
const passwordResetForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 密码重置验证规则
const passwordResetRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordResetForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取公司列表
const fetchCompanyOptions = async () => {
  try {
    const response = await getAvailableCompanies()
    if (response && response.data) {
      companyOptions.value = response.data
    }
  } catch (error) {
    console.error('获取公司列表失败:', error)
    ElMessage.error('获取公司列表失败')
  }
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.current,
      size: pagination.size
    }
    
    // 添加搜索条件
    if (searchForm.username) {
      params.username = searchForm.username
    }
    if (searchForm.companyName) {
      params.companyName = searchForm.companyName
    }
    if (searchForm.email) {
      params.email = searchForm.email
    }
    if (searchForm.companyId) {
      params.companyId = searchForm.companyId
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    const response = await getUserList(params)
    
    if (response && response.data) {
      if (response.data.records) {
        userList.value = response.data.records
        pagination.total = response.data.total || 0
        pagination.current = response.data.current || 1
        pagination.size = response.data.size || 20
      } else {
        userList.value = response.data
        pagination.total = response.data.length
      }
    } else {
      userList.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error('获取用户列表失败:', error)
    userList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    companyName: '',
    email: '',
    companyId: null,
    status: ''
  })
  pagination.current = 1
  fetchUserList()
}

// 新增用户
const handleAdd = () => {
  currentUser.value = null
  formVisible.value = true
}

// 编辑用户
const handleEdit = (row: User) => {
  currentUser.value = { ...row }
  formVisible.value = true
}

// 删除用户
const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除用户失败:', error)
    }
  }
}

// 密码重置
const handlePasswordReset = (row: User) => {
  currentUser.value = row
  passwordResetForm.newPassword = ''
  passwordResetForm.confirmPassword = ''
  passwordResetVisible.value = true
}

// 提交密码重置
const submitPasswordReset = async () => {
  if (!passwordResetFormRef.value || !currentUser.value) return
  
  try {
    await passwordResetFormRef.value.validate()
    submitting.value = true
    
    await resetPassword(currentUser.value.id, passwordResetForm.newPassword, passwordResetForm.confirmPassword)
    ElMessage.success('密码重置成功')
    passwordResetVisible.value = false
    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('密码重置失败')
      console.error('密码重置失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  formVisible.value = false
  fetchUserList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchUserList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchUserList()
}

// 获取状态类型
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    LOCKED: 'danger',
    PENDING: 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    ACTIVE: '激活',
    INACTIVE: '未激活',
    LOCKED: '锁定',
    PENDING: '待激活'
  }
  return statusMap[status] || status
}

// 获取价格分层标签类型
const getPriceTierType = (tierType: string) => {
  const typeMap: Record<string, string> = {
    'DEALER_LEVEL_1': 'success',
    'DEALER_LEVEL_2': 'warning',
    'DEALER_LEVEL_3': 'info',
    'VIP_CUSTOMER': 'danger',
    'WHOLESALE': 'primary',
    'RETAIL': 'default'
  }
  return typeMap[tierType] || 'default'
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUserList()
  fetchCompanyOptions()
})
</script>

<style scoped>
.user-list {
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

.search-area {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.table-area {
  background: white;
  border-radius: 4px;
  padding: 20px;
}

.pagination-area {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>
