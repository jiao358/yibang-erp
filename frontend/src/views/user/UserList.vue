<template>
  <div class="user-list">
    <div class="page-header material-card">
      <h2 class="page-title">用户管理</h2>
      <el-button type="primary" class="material-btn material-btn-primary" @click="showUserForm">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form material-card">
      <el-form :model="searchForm" inline class="material-form">
        <div class="form-row">
          <el-form-item label="用户名" class="form-item">
            <el-input 
              v-model="searchForm.username" 
              placeholder="请输入用户名"
              class="material-input"
              clearable
            />
          </el-form-item>
          <el-form-item label="真实姓名" class="form-item">
            <el-input 
              v-model="searchForm.realName" 
              placeholder="请输入真实姓名"
              class="material-input"
              clearable
            />
          </el-form-item>
          <el-form-item label="状态" class="form-item">
            <el-select 
              v-model="searchForm.status" 
              placeholder="请选择状态"
              class="material-select"
              clearable
            >
              <el-option label="启用" value="ACTIVE" />
              <el-option label="禁用" value="INACTIVE" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item class="form-item form-actions">
            <el-button type="primary" class="material-btn material-btn-primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset" class="material-btn material-btn-secondary">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <div class="user-table material-card">
      <el-table 
        :data="userList" 
        v-loading="loading"
        class="material-table"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 'ACTIVE' ? 'success' : 'danger'"
              class="material-tag"
            >
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              class="material-btn material-btn-primary"
              @click="editUser(row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              class="material-btn material-btn-secondary"
              @click="deleteUser(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="material-pagination"
        />
      </div>
    </div>

    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="userFormVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="600px"
      class="material-dialog"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
        class="material-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="userForm.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="userForm.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" placeholder="请选择状态">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="userFormVisible = false" class="material-btn material-btn-secondary">
            取消
          </el-button>
          <el-button type="primary" @click="submitUserForm" class="material-btn material-btn-primary">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const userFormVisible = ref(false)
const isEdit = ref(false)
const userFormRef = ref()

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  status: ''
})

// 用户表单
const userForm = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  position: '',
  status: 'ACTIVE'
})

// 表单验证规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 用户列表
const userList = ref([
  {
    id: 1,
    username: 'admin',
    realName: '系统管理员',
    email: 'admin@yibang.com',
    phone: '13800138000',
    department: '技术部',
    position: '系统管理员',
    status: 'ACTIVE',
    lastLoginTime: '2024-01-14 10:30:00'
  },
  {
    id: 2,
    username: 'zhangsan',
    realName: '张三',
    email: 'zhangsan@yibang.com',
    phone: '13800138001',
    department: '销售部',
    position: '销售经理',
    status: 'ACTIVE',
    lastLoginTime: '2024-01-14 09:15:00'
  },
  {
    id: 3,
    username: 'lisi',
    realName: '李四',
    email: 'lisi@yibang.com',
    phone: '13800138002',
    department: '采购部',
    position: '采购专员',
    status: 'ACTIVE',
    lastLoginTime: '2024-01-14 08:45:00'
  }
])

// 方法
const showUserForm = (user?: any) => {
  if (user) {
    isEdit.value = true
    Object.assign(userForm, user)
  } else {
    isEdit.value = false
    resetUserForm()
  }
  userFormVisible.value = true
}

const resetUserForm = () => {
  Object.assign(userForm, {
    id: '',
    username: '',
    realName: '',
    email: '',
    phone: '',
    department: '',
    position: '',
    status: 'ACTIVE'
  })
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchUserList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    realName: '',
    status: ''
  })
  handleSearch()
}

const editUser = (user: any) => {
  showUserForm(user)
}

const deleteUser = (user: any) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${user.realName}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 这里调用删除API
    ElMessage.success('删除成功')
    fetchUserList()
  }).catch(() => {
    // 用户取消
  })
}

const submitUserForm = async () => {
  try {
    await userFormRef.value.validate()
    
    if (isEdit.value) {
      // 更新用户
      ElMessage.success('更新成功')
    } else {
      // 创建用户
      ElMessage.success('创建成功')
    }
    
    userFormVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchUserList()
}

const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  fetchUserList()
}

const fetchUserList = async () => {
  loading.value = true
  try {
    // 这里调用API获取用户列表
    // 暂时使用模拟数据
    pagination.total = userList.value.length
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-list {
  padding: var(--spacing-xl);
  background-color: var(--background-color);
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.page-title {
  font-size: var(--font-size-xxl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

/* 搜索表单 */
.search-form {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.material-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
  align-items: flex-end;
}

.form-item {
  flex: 1;
  min-width: 200px;
}

.form-actions {
  flex: none;
  min-width: auto;
  margin-left: auto;
}

.material-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}

.material-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
  margin-bottom: var(--spacing-sm);
  display: block;
  text-align: left;
}

.material-form :deep(.el-form-item__content) {
  width: 100%;
}

.material-input,
.material-select {
  width: 100%;
}

.material-input :deep(.el-input__wrapper),
.material-select :deep(.el-input__wrapper) {
  border: 1px solid var(--divider-color);
  border-radius: var(--border-radius-md);
  transition: all var(--transition-normal);
  background-color: var(--surface-color);
}

.material-input :deep(.el-input__wrapper:hover),
.material-select :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color);
}

.material-input :deep(.el-input__wrapper.is-focus),
.material-select :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-50);
}

.material-input :deep(.el-input__inner),
.material-select :deep(.el-input__inner) {
  color: var(--text-primary);
}

.material-input :deep(.el-input__inner::placeholder),
.material-select :deep(.el-input__inner::placeholder) {
  color: var(--text-hint);
}

.material-select :deep(.el-select .el-input__wrapper) {
  background-color: var(--surface-color);
}

.material-select :deep(.el-select .el-input__inner) {
  color: var(--text-primary);
}

/* 用户表格 */
.user-table {
  padding: var(--spacing-lg);
}

.material-table {
  width: 100%;
}

.material-table :deep(.el-table__header) {
  background-color: var(--primary-50);
}

.material-table :deep(.el-table__header th) {
  background-color: transparent;
  color: var(--text-primary);
  font-weight: 600;
  border-bottom: 1px solid var(--divider-color);
}

.material-table :deep(.el-table__row) {
  transition: all var(--transition-normal);
}

.material-table :deep(.el-table__row:hover) {
  background-color: var(--primary-50);
}

.material-table :deep(.el-table__row td) {
  border-bottom: 1px solid var(--divider-color);
  color: var(--text-primary);
}

.material-tag {
  border-radius: var(--border-radius-sm);
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--divider-color);
}

.material-pagination :deep(.el-pagination__total),
.material-pagination :deep(.el-pagination__jump) {
  color: var(--text-secondary);
}

.material-pagination :deep(.el-pager li) {
  background-color: var(--surface-color);
  border: 1px solid var(--divider-color);
  color: var(--text-primary);
  transition: all var(--transition-normal);
}

.material-pagination :deep(.el-pager li:hover) {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.material-pagination :deep(.el-pager li.is-active) {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.material-pagination :deep(.btn-prev),
.material-pagination :deep(.btn-next) {
  background-color: var(--surface-color);
  border: 1px solid var(--divider-color);
  color: var(--text-primary);
  transition: all var(--transition-normal);
}

.material-pagination :deep(.btn-prev:hover),
.material-pagination :deep(.btn-next:hover) {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

/* 对话框 */
.material-dialog :deep(.el-dialog) {
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-4);
}

.material-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--divider-color);
  padding: var(--spacing-lg);
}

.material-dialog :deep(.el-dialog__title) {
  color: var(--text-primary);
  font-weight: 600;
}

.material-dialog :deep(.el-dialog__body) {
  padding: var(--spacing-lg);
}

.material-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid var(--divider-color);
  padding: var(--spacing-lg);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .user-list {
    padding: var(--spacing-lg);
  }
  
  .material-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .material-input,
  .material-select {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .user-list {
    padding: var(--spacing-md);
  }
  
  .page-header {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
  }
  
  .page-title {
    text-align: center;
  }
  
  .search-form {
    padding: var(--spacing-md);
  }
  
  .user-table {
    padding: var(--spacing-md);
  }
  
  .material-table {
    font-size: var(--font-size-sm);
  }
}
</style>
