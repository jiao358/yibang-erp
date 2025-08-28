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
        <el-form-item label="真实姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入真实姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="激活" value="ACTIVE" />
            <el-option label="未激活" value="INACTIVE" />
            <el-option label="锁定" value="LOCKED" />
            <el-option label="待审核" value="PENDING" />
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="position" label="职位" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import UserForm from './components/UserForm.vue'
import { getUserList, deleteUser } from '@/api/user'
import type { User } from '@/types/user'

// 响应式数据
const loading = ref(false)
const userList = ref<User[]>([])
const formVisible = ref(false)
const currentUser = ref<User | null>(null)

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const response = await getUserList(params)
    userList.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error('获取用户列表失败:', error)
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
    realName: '',
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

// 查看用户
const handleView = (row: User) => {
  currentUser.value = { ...row }
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
    PENDING: '待审核'
  }
  return statusMap[status] || status
}

// 格式化日期时间
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUserList()
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
</style>
