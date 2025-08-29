<template>
  <div class="role-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增角色
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入角色名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="未激活" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 角色列表 -->
    <div class="table-area">
      <el-table
        v-loading="loading"
        :data="roleList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="description" label="角色描述" min-width="200" />
        <el-table-column prop="isSystem" label="系统角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSystem ? 'danger' : 'info'">
              {{ row.isSystem ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="handleEdit(row)"
              :disabled="row.isSystem && !hasAdminPermission"
            >编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
              :disabled="row.isSystem && !hasAdminPermission"
            >删除</el-button>
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

    <!-- 角色表单对话框 -->
    <RoleForm
      v-model:visible="formVisible"
      :role-data="currentRole"
      :is-view-mode="isViewMode"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import RoleForm from './components/RoleForm.vue'
import { getRoleList, deleteRole } from '@/api/role'
import type { Role } from '@/types/role'

// 响应式数据
const loading = ref(false)
const roleList = ref<Role[]>([])
const formVisible = ref(false)
const currentRole = ref<Role | null>(null)
const isViewMode = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params: any = {
      page: pagination.current,
      size: pagination.size
    }
    
    // 添加搜索条件
    if (searchForm.name) {
      params.name = searchForm.name
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    const response = await getRoleList(params)
    console.log('角色列表响应:', response)
    
    // 处理响应数据
    if (response && response.data) {
      // 分页数据结构
      if (response.data.records) {
        roleList.value = response.data.records
        pagination.total = response.data.total || 0
        pagination.current = response.data.current || 1
        pagination.size = response.data.size || 20
      } else {
        // 数组数据
        roleList.value = response.data
        pagination.total = response.data.length
      }
    } else if (Array.isArray(response)) {
      // 直接返回数组
      roleList.value = response
      pagination.total = response.length
    } else {
      roleList.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error('获取角色列表失败')
    console.error('获取角色列表失败:', error)
    roleList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchRoleList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: ''
  })
  pagination.current = 1
  fetchRoleList()
}

// 新增角色
const handleAdd = () => {
  currentRole.value = null
  isViewMode.value = false
  formVisible.value = true
}

// 查看角色
const handleView = (row: Role) => {
  currentRole.value = { ...row }
  isViewMode.value = true
  formVisible.value = true
}

// 编辑角色
const handleEdit = (row: Role) => {
  currentRole.value = { ...row }
  isViewMode.value = false
  formVisible.value = true
}

// 删除角色
const handleDelete = async (row: Role) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${row.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchRoleList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除角色失败:', error)
    }
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  formVisible.value = false
  fetchRoleList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchRoleList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchRoleList()
}

// 获取状态类型
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'info'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    ACTIVE: '激活',
    INACTIVE: '未激活'
  }
  return statusMap[status] || status
}

// 检查是否有管理员权限
const hasAdminPermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN')
    } catch {
      return false
    }
  }
  return false
})

// 格式化日期时间
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped>
.role-list {
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
