<template>
  <div class="inventory-alert-list">
    <div class="page-header">
      <h2>库存预警管理</h2>
      <div class="header-actions">
        <el-button type="warning" @click="handleAutoDetect">自动检测预警</el-button>
        <el-button type="primary" @click="showAddDialog">新增预警</el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="预警类型">
          <el-select
            v-model="searchForm.alertType"
            placeholder="请选择预警类型"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="option in ALERT_TYPE_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预警级别">
          <el-select
            v-model="searchForm.alertLevel"
            placeholder="请选择预警级别"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="option in ALERT_LEVEL_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预警状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择预警状态"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="option in ALERT_STATUS_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预警列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="alertList"
        border
        style="width: 100%"
      >
        <el-table-column prop="alertNo" label="预警编号" width="150" />
        <el-table-column prop="alertType" label="预警类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlertTypeTag(row.alertType)">
              {{ getAlertTypeLabel(row.alertType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertLevel" label="预警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlertLevelTag(row.alertLevel)">
              {{ getAlertLevelLabel(row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column prop="warehouseId" label="仓库ID" width="100" />
        <el-table-column prop="currentQuantity" label="当前库存" width="100" />
        <el-table-column prop="thresholdQuantity" label="预警阈值" width="100" />
        <el-table-column prop="alertContent" label="预警内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="primary"
              @click="handleProcess(row)"
            >
              处理
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="warning"
              @click="handleIgnore(row)"
            >
              忽略
            </el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 新增/编辑预警对话框 -->
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
        <el-form-item label="预警类型" prop="alertType">
          <el-select v-model="form.alertType" placeholder="请选择预警类型" style="width: 100%">
            <el-option
              v-for="option in ALERT_TYPE_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预警级别" prop="alertLevel">
          <el-select v-model="form.alertLevel" placeholder="请选择预警级别" style="width: 100%">
            <el-option
              v-for="option in ALERT_LEVEL_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品ID" prop="productId">
          <el-input v-model.number="form.productId" placeholder="请输入商品ID" />
        </el-form-item>
        <el-form-item label="仓库ID" prop="warehouseId">
          <el-input v-model.number="form.warehouseId" placeholder="请输入仓库ID" />
        </el-form-item>
        <el-form-item label="当前库存" prop="currentQuantity">
          <el-input-number v-model="form.currentQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预警阈值" prop="thresholdQuantity">
          <el-input-number v-model="form.thresholdQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预警内容" prop="alertContent">
          <el-input
            v-model="form.alertContent"
            type="textarea"
            :rows="3"
            placeholder="请输入预警内容"
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

    <!-- 处理预警对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理预警"
      width="500px"
    >
      <el-form
        ref="processFormRef"
        :model="processForm"
        :rules="processRules"
        label-width="100px"
      >
        <el-form-item label="处理结果" prop="handlingResult">
          <el-input v-model="processForm.handlingResult" placeholder="请输入处理结果" />
        </el-form-item>
        <el-form-item label="处理备注" prop="handlingRemark">
          <el-input
            v-model="processForm.handlingRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess" :loading="processing">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 忽略预警对话框 -->
    <el-dialog
      v-model="ignoreDialogVisible"
      title="忽略预警"
      width="500px"
    >
      <el-form
        ref="ignoreFormRef"
        :model="ignoreForm"
        :rules="ignoreRules"
        label-width="100px"
      >
        <el-form-item label="忽略原因" prop="reason">
          <el-input
            v-model="ignoreForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入忽略原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ignoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitIgnore" :loading="ignoring">
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
import { 
  getInventoryAlertList, 
  createInventoryAlert, 
  updateInventoryAlert, 
  deleteInventoryAlert,
  handleInventoryAlert,
  ignoreInventoryAlert,
  autoDetectInventoryAlerts
} from '@/api/inventoryAlert'
import { 
  ALERT_TYPE_OPTIONS, 
  ALERT_LEVEL_OPTIONS, 
  ALERT_STATUS_OPTIONS,
  type InventoryAlert,
  type InventoryAlertRequest
} from '@/types/inventory'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const processDialogVisible = ref(false)
const processing = ref(false)
const processFormRef = ref<FormInstance>()
const currentAlertId = ref<number | null>(null)

const ignoreDialogVisible = ref(false)
const ignoring = ref(false)
const ignoreFormRef = ref<FormInstance>()

const searchForm = reactive({
  alertType: '',
  alertLevel: '',
  status: ''
})

const form = reactive<InventoryAlertRequest>({
  alertType: 'LOW_STOCK',
  alertLevel: 'MEDIUM',
  productId: 0,
  warehouseId: 0,
  currentQuantity: 0,
  thresholdQuantity: 0,
  alertContent: '',
  status: 'PENDING'
})

const processForm = reactive({
  handlingResult: '',
  handlingRemark: ''
})

const ignoreForm = reactive({
  reason: ''
})

const rules: FormRules = {
  alertType: [
    { required: true, message: '请选择预警类型', trigger: 'change' }
  ],
  alertLevel: [
    { required: true, message: '请选择预警级别', trigger: 'change' }
  ],
  productId: [
    { required: true, message: '请输入商品ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '商品ID必须大于0', trigger: 'blur' }
  ],
  warehouseId: [
    { required: true, message: '请输入仓库ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '仓库ID必须大于0', trigger: 'blur' }
  ],
  currentQuantity: [
    { required: true, message: '请输入当前库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '当前库存不能小于0', trigger: 'blur' }
  ],
  thresholdQuantity: [
    { required: true, message: '请输入预警阈值', trigger: 'blur' },
    { type: 'number', min: 0, message: '预警阈值不能小于0', trigger: 'blur' }
  ]
}

const processRules: FormRules = {
  handlingResult: [
    { required: true, message: '请输入处理结果', trigger: 'blur' }
  ]
}

const ignoreRules: FormRules = {
  reason: [
    { required: true, message: '请输入忽略原因', trigger: 'blur' }
  ]
}

const alertList = ref<InventoryAlert[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑预警' : '新增预警')

// 方法
const loadAlertList = async () => {
  loading.value = true
  try {
    const response = await getInventoryAlertList({
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    if (response.success) {
      alertList.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('加载预警列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadAlertList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    alertType: '',
    alertLevel: '',
    status: ''
  })
  pagination.current = 1
  loadAlertList()
}

const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: InventoryAlert) => {
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
      await updateInventoryAlert(form.id!, form)
      ElMessage.success('预警更新成功')
    } else {
      await createInventoryAlert(form)
      ElMessage.success('预警创建成功')
    }
    dialogVisible.value = false
    loadAlertList()
  } catch (error) {
    ElMessage.error(isEdit.value ? '预警更新失败' : '预警创建失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: InventoryAlert) => {
  try {
    await ElMessageBox.confirm('确定要删除该预警吗？', '提示', {
      type: 'warning'
    })
    await deleteInventoryAlert(row.id)
    ElMessage.success('预警删除成功')
    loadAlertList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('预警删除失败')
    }
  }
}

const handleProcess = (row: InventoryAlert) => {
  currentAlertId.value = row.id
  processDialogVisible.value = true
}

const submitProcess = async () => {
  if (!processFormRef.value || !currentAlertId.value) return
  
  await processFormRef.value.validate()
  
  processing.value = true
  try {
    await handleInventoryAlert(currentAlertId.value, processForm.handlingResult, processForm.handlingRemark)
    ElMessage.success('预警处理成功')
    processDialogVisible.value = false
    loadAlertList()
  } catch (error) {
    ElMessage.error('预警处理失败')
  } finally {
    processing.value = false
  }
}

const handleIgnore = (row: InventoryAlert) => {
  currentAlertId.value = row.id
  ignoreDialogVisible.value = true
}

const submitIgnore = async () => {
  if (!ignoreFormRef.value || !currentAlertId.value) return
  
  await ignoreFormRef.value.validate()
  
  ignoring.value = true
  try {
    await ignoreInventoryAlert(currentAlertId.value, ignoreForm.reason)
    ElMessage.success('预警已忽略')
    ignoreDialogVisible.value = false
    loadAlertList()
  } catch (error) {
    ElMessage.error('忽略预警失败')
  } finally {
    ignoring.value = false
  }
}

const handleAutoDetect = async () => {
  try {
    await autoDetectInventoryAlerts()
    ElMessage.success('自动检测预警完成')
    loadAlertList()
  } catch (error) {
    ElMessage.error('自动检测预警失败')
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    alertType: 'LOW_STOCK',
    alertLevel: 'MEDIUM',
    productId: 0,
    warehouseId: 0,
    currentQuantity: 0,
    thresholdQuantity: 0,
    alertContent: '',
    status: 'PENDING'
  })
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadAlertList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadAlertList()
}

const getAlertTypeLabel = (type: string) => {
  const option = ALERT_TYPE_OPTIONS.find(opt => opt.value === type)
  return option?.label || type
}

const getAlertTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    LOW_STOCK: 'danger',
    OVER_STOCK: 'warning',
    EXPIRY: 'warning',
    DAMAGED: 'danger'
  }
  return tagMap[type] || ''
}

const getAlertLevelLabel = (level: string) => {
  const option = ALERT_LEVEL_OPTIONS.find(opt => opt.value === level)
  return option?.label || level
}

const getAlertLevelTag = (level: string) => {
  const tagMap: Record<string, string> = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }
  return tagMap[level] || ''
}

const getStatusLabel = (status: string) => {
  const option = ALERT_STATUS_OPTIONS.find(opt => opt.value === status)
  return option?.label || status
}

const getStatusTag = (status: string) => {
  const tagMap: Record<string, string> = {
    PENDING: 'danger',
    PROCESSING: 'warning',
    RESOLVED: 'success',
    IGNORED: 'info'
  }
  return tagMap[status] || ''
}

// 生命周期
onMounted(() => {
  loadAlertList()
})
</script>

<style scoped>
.inventory-alert-list {
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
