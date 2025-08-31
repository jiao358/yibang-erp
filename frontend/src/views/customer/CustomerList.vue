<template>
  <div class="customer-list">
    <div class="page-header">
      <h2>客户管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新建客户
        </el-button>
        <el-button @click="showImportDialog">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户编码">
          <el-input
            v-model="searchForm.customerCode"
            placeholder="请输入客户编码"
            clearable
          />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入客户名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="客户类型">
          <el-select v-model="searchForm.customerType" placeholder="请选择类型" clearable>
            <el-option label="个人" value="INDIVIDUAL" />
            <el-option label="企业" value="ENTERPRISE" />
            <el-option label="经销商" value="DEALER" />
            <el-option label="代理商" value="AGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="searchForm.customerLevel" placeholder="请选择等级" clearable>
            <el-option label="普通客户" value="NORMAL" />
            <el-option label="VIP客户" value="VIP" />
            <el-option label="重要客户" value="IMPORTANT" />
            <el-option label="战略客户" value="STRATEGIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="黑名单" value="BLACKLIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入关键词"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 客户列表 -->
    <div class="customer-table">
      <el-table
        v-loading="loading"
        :data="customerList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="customerCode" label="客户编码" width="120" />
        <el-table-column prop="name" label="客户名称" width="200" />
        <el-table-column prop="customerType" label="客户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getCustomerTypeTagType(row.customerType)">
              {{ getCustomerTypeText(row.customerType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="customerLevel" label="客户等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getCustomerLevelTagType(row.customerLevel)">
              {{ getCustomerLevelText(row.customerLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="contactEmail" label="联系邮箱" width="180" />
        <el-table-column prop="address" label="地址" width="200" show-overflow-tooltip />
        <el-table-column prop="creditLimit" label="信用额度" width="120">
          <template #default="{ row }">
            ¥{{ row.creditLimit?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewCustomer(row)">查看</el-button>
            <el-button
              v-if="canEdit(row)"
              size="small"
              type="primary"
              @click="editCustomer(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canDelete(row)"
              size="small"
              type="danger"
              @click="deleteCustomer(row)"
            >
              删除
            </el-button>
            <el-dropdown @command="handleAction" trigger="click">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="canActivate(row)"
                    :command="{ action: 'activate', customer: row }"
                  >
                    激活
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canDeactivate(row)"
                    :command="{ action: 'deactivate', customer: row }"
                  >
                    停用
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canBlacklist(row)"
                    :command="{ action: 'blacklist', customer: row }"
                  >
                    加入黑名单
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="{ action: 'orders', customer: row }"
                  >
                    查看订单
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
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

    <!-- 新建/编辑客户对话框 -->
    <CustomerDialog
      v-model="dialogVisible"
      :customer="currentCustomer"
      :mode="dialogMode"
      @success="handleDialogSuccess"
    />

    <!-- 批量导入对话框 -->
    <ImportDialog
      v-model="importDialogVisible"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import CustomerDialog from './components/CustomerDialog.vue'
import ImportDialog from './components/ImportDialog.vue'
import { customerApi } from '@/api/customer'
import type { Customer } from '@/types/customer'

// 响应式数据
const loading = ref(false)
const customerList = ref<Customer[]>([])
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentCustomer = ref<Customer | null>(null)
const importDialogVisible = ref(false)

// 搜索表单
const searchForm = reactive({
  current: 1,
  size: 20,
  customerCode: '',
  name: '',
  customerType: '',
  customerLevel: '',
  status: '',
  keyword: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 生命周期
onMounted(() => {
  loadCustomerList()
})

// 加载客户列表
const loadCustomerList = async () => {
  try {
    loading.value = true
    const response = await customerApi.getCustomerList(searchForm)
    customerList.value = response || []
    pagination.total = response?.length || 0
  } catch (error) {
    ElMessage.error('加载客户列表失败')
    console.error('加载客户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.current = 1
  loadCustomerList()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    current: 1,
    size: 20,
    customerCode: '',
    name: '',
    customerType: '',
    customerLevel: '',
    status: '',
    keyword: ''
  })
  loadCustomerList()
}

// 分页处理
const handleSizeChange = (size: number) => {
  searchForm.size = size
  searchForm.current = 1
  loadCustomerList()
}

const handleCurrentChange = (current: number) => {
  searchForm.current = current
  loadCustomerList()
}

// 显示新建对话框
const showCreateDialog = () => {
  dialogMode.value = 'create'
  currentCustomer.value = null
  dialogVisible.value = true
}

// 显示编辑对话框
const editCustomer = (customer: Customer) => {
  dialogMode.value = 'edit'
  currentCustomer.value = customer
  dialogVisible.value = true
}

// 查看客户
const viewCustomer = (customer: Customer) => {
  // TODO: 实现查看客户详情
  ElMessage.info('查看客户详情功能待实现')
}

// 删除客户
const deleteCustomer = async (customer: Customer) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除客户 ${customer.name} 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await customerApi.deleteCustomer(customer.id)
    ElMessage.success('删除成功')
    loadCustomerList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除客户失败:', error)
    }
  }
}

// 显示导入对话框
const showImportDialog = () => {
  importDialogVisible.value = true
}

// 处理操作
const handleAction = async (command: { action: string; customer: Customer }) => {
  const { action, customer } = command
  
  try {
    switch (action) {
      case 'activate':
        await customerApi.updateCustomer(customer.id, { ...customer, status: 'ACTIVE' })
        ElMessage.success('客户激活成功')
        break
      case 'deactivate':
        await customerApi.updateCustomer(customer.id, { ...customer, status: 'INACTIVE' })
        ElMessage.success('客户停用成功')
        break
      case 'blacklist':
        await customerApi.updateCustomer(customer.id, { ...customer, status: 'BLACKLIST' })
        ElMessage.success('客户已加入黑名单')
        break
      case 'orders':
        // TODO: 跳转到订单页面，显示该客户的订单
        ElMessage.info('查看客户订单功能待实现')
        return
      default:
        ElMessage.warning('未知操作')
        return
    }
    
    loadCustomerList()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('操作失败:', error)
  }
}

// 对话框成功回调
const handleDialogSuccess = () => {
  dialogVisible.value = false
  loadCustomerList()
}

// 导入成功回调
const handleImportSuccess = () => {
  importDialogVisible.value = false
  loadCustomerList()
}

// 权限检查
const canEdit = (customer: Customer) => {
  return customer.status !== 'BLACKLIST'
}

const canDelete = (customer: Customer) => {
  return customer.status === 'INACTIVE'
}

const canActivate = (customer: Customer) => {
  return customer.status === 'INACTIVE'
}

const canDeactivate = (customer: Customer) => {
  return customer.status === 'ACTIVE'
}

const canBlacklist = (customer: Customer) => {
  return customer.status === 'ACTIVE'
}

// 客户类型标签类型
const getCustomerTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    INDIVIDUAL: '',
    ENTERPRISE: 'success',
    DEALER: 'warning',
    AGENT: 'info'
  }
  return typeMap[type] || ''
}

// 客户类型文本
const getCustomerTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    INDIVIDUAL: '个人',
    ENTERPRISE: '企业',
    DEALER: '经销商',
    AGENT: '代理商'
  }
  return textMap[type] || type
}

// 客户等级标签类型
const getCustomerLevelTagType = (level: string) => {
  const typeMap: Record<string, string> = {
    NORMAL: '',
    VIP: 'warning',
    IMPORTANT: 'success',
    STRATEGIC: 'danger'
  }
  return typeMap[level] || ''
}

// 客户等级文本
const getCustomerLevelText = (level: string) => {
  const textMap: Record<string, string> = {
    NORMAL: '普通客户',
    VIP: 'VIP客户',
    IMPORTANT: '重要客户',
    STRATEGIC: '战略客户'
  }
  return textMap[level] || level
}

// 状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    BLACKLIST: 'danger'
  }
  return typeMap[status] || ''
}

// 状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    ACTIVE: '活跃',
    INACTIVE: '停用',
    BLACKLIST: '黑名单'
  }
  return textMap[status] || status
}

// 格式化日期时间
const formatDateTime = (dateTime: string | Date) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.customer-list {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.search-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.customer-table {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #fff;
}
</style>
