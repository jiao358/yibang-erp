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
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
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
              :disabled="row.isSystem"
            >编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
              :disabled="row.isSystem"
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
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
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
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const response = await getRoleList(params)
    roleList.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('获取角色列表失败')
    console.error('获取角色列表失败:', error)
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
  formVisible.value = true
}

// 查看角色
const handleView = (row: Role) => {
  currentRole.value = { ...row }
  formVisible.value = true
}

// 编辑角色
const handleEdit = (row: Role) => {
  currentRole.value = { ...row }
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
