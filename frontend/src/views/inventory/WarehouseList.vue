<template>
  <div class="warehouse-list">
    <div class="page-header">
      <h2>仓库管理</h2>
      <el-button type="primary" @click="showAddDialog">新增仓库</el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="仓库编码">
          <el-input
            v-model="searchForm.warehouseCode"
            placeholder="请输入仓库编码"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="仓库名称">
          <el-input
            v-model="searchForm.warehouseName"
            placeholder="请输入仓库名称"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="仓库类型">
          <el-select
            v-model="searchForm.warehouseType"
            placeholder="请选择仓库类型"
            clearable
            style="width: 150px"
          >
            <el-option label="主仓库" value="MAIN" />
            <el-option label="分仓库" value="BRANCH" />
            <el-option label="临时仓库" value="TEMPORARY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 仓库列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="warehouseList"
        border
        style="width: 100%"
      >
        <el-table-column prop="warehouseCode" label="仓库编码" width="120" />
        <el-table-column prop="warehouseName" label="仓库名称" width="150" />
        <el-table-column prop="warehouseType" label="仓库类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getWarehouseTypeTag(row.warehouseType)">
              {{ getWarehouseTypeLabel(row.warehouseType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="area" label="面积(㎡)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'INACTIVE'"
              size="small"
              type="success"
              @click="handleActivate(row)"
            >
              启用
            </el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              size="small"
              type="warning"
              @click="handleDeactivate(row)"
            >
              停用
            </el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              size="small"
              type="info"
              @click="handleMaintain(row)"
            >
              维护
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
      <div class="pagination-wrapper">
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
    </el-card>

    <!-- 新增/编辑仓库对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="仓库编码" prop="warehouseCode">
          <el-input v-model="form.warehouseCode" placeholder="请输入仓库编码" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="仓库类型" prop="warehouseType">
          <el-select v-model="form.warehouseType" placeholder="请选择仓库类型" style="width: 100%">
            <el-option label="主仓库" value="MAIN" />
            <el-option label="分仓库" value="BRANCH" />
            <el-option label="临时仓库" value="TEMPORARY" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入仓库地址" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="面积(㎡)" prop="area">
          <el-input-number v-model="form.area" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getWarehouseList, createWarehouse, updateWarehouse, deleteWarehouse, activateWarehouse, deactivateWarehouse, maintainWarehouse } from '@/api/warehouse'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  warehouseCode: '',
  warehouseName: '',
  warehouseType: '',
  status: ''
})

const form = reactive({
  id: null as number | null,
  warehouseCode: '',
  warehouseName: '',
  warehouseType: '',
  companyId: 1, // 从用户信息获取
  address: '',
  contactPerson: '',
  contactPhone: '',
  area: 0,
  status: 'ACTIVE',
  description: ''
})

const rules: FormRules = {
  warehouseCode: [
    { required: true, message: '请输入仓库编码', trigger: 'blur' },
    { max: 50, message: '仓库编码长度不能超过50个字符', trigger: 'blur' }
  ],
  warehouseName: [
    { required: true, message: '请输入仓库名称', trigger: 'blur' },
    { max: 100, message: '仓库名称长度不能超过100个字符', trigger: 'blur' }
  ],
  warehouseType: [
    { required: true, message: '请选择仓库类型', trigger: 'change' }
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '联系电话格式不正确', trigger: 'blur' }
  ]
}

const warehouseList = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑仓库' : '新增仓库')

// 方法
const loadWarehouseList = async () => {
  loading.value = true
  try {
    const response = await getWarehouseList({
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    if (response.success) {
      warehouseList.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('加载仓库列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadWarehouseList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    warehouseCode: '',
    warehouseName: '',
    warehouseType: '',
    status: ''
  })
  pagination.current = 1
  loadWarehouseList()
}

const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate()
  
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateWarehouse(form.id!, form)
      ElMessage.success('仓库更新成功')
    } else {
      await createWarehouse(form)
      ElMessage.success('仓库创建成功')
    }
    dialogVisible.value = false
    loadWarehouseList()
  } catch (error) {
    ElMessage.error(isEdit.value ? '仓库更新失败' : '仓库创建失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该仓库吗？', '提示', {
      type: 'warning'
    })
    await deleteWarehouse(row.id)
    ElMessage.success('仓库删除成功')
    loadWarehouseList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('仓库删除失败')
    }
  }
}

const handleActivate = async (row: any) => {
  try {
    await activateWarehouse(row.id)
    ElMessage.success('仓库启用成功')
    loadWarehouseList()
  } catch (error) {
    ElMessage.error('仓库启用失败')
  }
}

const handleDeactivate = async (row: any) => {
  try {
    await deactivateWarehouse(row.id)
    ElMessage.success('仓库停用成功')
    loadWarehouseList()
  } catch (error) {
    ElMessage.error('仓库停用失败')
  }
}

const handleMaintain = async (row: any) => {
  try {
    await maintainWarehouse(row.id)
    ElMessage.success('仓库设置为维护状态成功')
    loadWarehouseList()
  } catch (error) {
    ElMessage.error('仓库设置维护状态失败')
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: null,
    warehouseCode: '',
    warehouseName: '',
    warehouseType: '',
    companyId: 1,
    address: '',
    contactPerson: '',
    contactPhone: '',
    area: 0,
    status: 'ACTIVE',
    description: ''
  })
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadWarehouseList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadWarehouseList()
}

const getWarehouseTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    MAIN: '主仓库',
    BRANCH: '分仓库',
    TEMPORARY: '临时仓库'
  }
  return typeMap[type] || type
}

const getWarehouseTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    MAIN: 'primary',
    BRANCH: 'success',
    TEMPORARY: 'warning'
  }
  return tagMap[type] || ''
}

const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    ACTIVE: '启用',
    INACTIVE: '停用',
    MAINTENANCE: '维护中'
  }
  return statusMap[status] || status
}

const getStatusTag = (status: string) => {
  const tagMap: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'danger',
    MAINTENANCE: 'warning'
  }
  return tagMap[status] || ''
}

// 生命周期
onMounted(() => {
  loadWarehouseList()
})
</script>

<style scoped>
.warehouse-list {
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

.search-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
