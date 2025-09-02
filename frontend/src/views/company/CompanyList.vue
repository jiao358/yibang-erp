<template>
  <div class="company-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>公司管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增公司
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :model="searchForm" inline>
        <el-form-item label="公司名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入公司名称"
            clearable
            style="width: 250px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select 
            v-model="searchForm.type" 
            placeholder="请选择业务类型" 
            clearable
            style="width: 200px;"
          >
            <el-option label="供应商" value="SUPPLIER" />
            <el-option label="销售商" value="SALES" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态" 
            clearable
            style="width: 200px;"
          >
            <el-option label="激活" value="ACTIVE" />
            <el-option label="未激活" value="INACTIVE" />
            <el-option label="暂停" value="SUSPENDED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 公司列表 -->
    <div class="table-area">
      <el-table
        v-loading="loading"
        :data="companyList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="公司名称" width="200" />
        <el-table-column prop="type" label="业务类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
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
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="contactEmail" label="联系邮箱" width="180" />
        <el-table-column prop="address" label="公司地址" min-width="200" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
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

    <!-- 公司表单对话框 -->
    <CompanyForm
      v-model:visible="formVisible"
      :company-data="currentCompany"
      :is-view-mode="isViewMode"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import CompanyForm from './components/CompanyForm.vue'
import { companyApi } from '@/api/company'
import type { Company } from '@/types/company'

// 响应式数据
const loading = ref(false)
const companyList = ref<Company[]>([])
const formVisible = ref(false)
const currentCompany = ref<Company | null>(null)
const isViewMode = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 获取公司列表
const fetchCompanyList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const response = await companyApi.getCompanyList(params)
    companyList.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('获取公司列表失败')
    console.error('获取公司列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchCompanyList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    type: '',
    status: ''
  })
  pagination.current = 1
  fetchCompanyList()
}

// 新增公司
const handleAdd = () => {
  currentCompany.value = null
  isViewMode.value = false
  formVisible.value = true
}

// 查看公司
const handleView = (row: Company) => {
  currentCompany.value = { ...row }
  isViewMode.value = true
  formVisible.value = true
}

// 编辑公司
const handleEdit = (row: Company) => {
  currentCompany.value = { ...row }
  isViewMode.value = false
  formVisible.value = true
}

// 删除公司
const handleDelete = async (row: Company) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除公司 "${row.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await companyApi.deleteCompany(row.id)
    ElMessage.success('删除成功')
    fetchCompanyList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除公司失败:', error)
    }
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  formVisible.value = false
  fetchCompanyList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchCompanyList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchCompanyList()
}

// 获取业务类型标签类型
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    SUPPLIER: 'success',
    SALES: 'warning'
  }
  return typeMap[type] || 'info'
}

// 获取业务类型文本
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    SUPPLIER: '供应商',
    SALES: '销售商'
  }
  return typeMap[type] || type
}

// 获取状态类型
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    SUSPENDED: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    ACTIVE: '激活',
    INACTIVE: '未激活',
    SUSPENDED: '暂停'
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
  fetchCompanyList()
})
</script>

<style scoped>
.company-list {
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
