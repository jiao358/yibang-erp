<template>
  <div class="inventory-check-list">
    <div class="page-header">
      <h2>库存盘点</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog">新建盘点</el-button>
        <el-button type="success" @click="handleAutoDetect">自动检测</el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="盘点类型">
          <el-select
            v-model="searchForm.checkType"
            placeholder="请选择盘点类型"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="option in CHECK_TYPE_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select
            v-model="searchForm.warehouseId"
            placeholder="请选择仓库"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="warehouse in warehouseOptions"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择盘点状态"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="option in CHECK_STATUS_OPTIONS"
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

    <!-- 盘点列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="checkList"
        border
        style="width: 100%"
      >
        <el-table-column prop="checkNo" label="盘点单号" width="150" />
        <el-table-column prop="checkType" label="盘点类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getCheckTypeTag(row.checkType)">
              {{ getCheckTypeLabel(row.checkType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseId" label="仓库ID" width="100" />
        <el-table-column prop="status" label="盘点状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getCheckStatusTag(row.status)">
              {{ getCheckStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="plannedStartTime" label="计划开始时间" width="180" />
        <el-table-column prop="plannedEndTime" label="计划结束时间" width="180" />
        <el-table-column prop="actualStartTime" label="实际开始时间" width="180" />
        <el-table-column prop="actualEndTime" label="实际结束时间" width="180" />
        <el-table-column prop="checkerId" label="盘点人ID" width="100" />
        <el-table-column prop="reviewerId" label="审核人ID" width="100" />
        <el-table-column prop="reviewStatus" label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getReviewStatusTag(row.reviewStatus)">
              {{ getReviewStatusLabel(row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'PLANNED'" 
              size="small" 
              type="primary" 
              @click="handleStartCheck(row)"
            >
              开始盘点
            </el-button>
            <el-button 
              v-if="row.status === 'IN_PROGRESS'" 
              size="small" 
              type="success" 
              @click="handleCompleteCheck(row)"
            >
              完成盘点
            </el-button>
            <el-button 
              v-if="row.status === 'COMPLETED'" 
              size="small" 
              type="info" 
              @click="handleReviewCheck(row)"
            >
              审核盘点
            </el-button>
            <el-button 
              v-if="row.status === 'PLANNED'" 
              size="small" 
              type="warning" 
              @click="handleCancelCheck(row)"
            >
              取消盘点
            </el-button>
            <el-button size="small" @click="handleViewDetails(row)">查看详情</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新建盘点对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建盘点"
      width="600px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="120px"
      >
        <el-form-item label="盘点类型" prop="checkType">
          <el-select v-model="createForm.checkType" placeholder="请选择盘点类型" style="width: 100%">
            <el-option
              v-for="option in CHECK_TYPE_OPTIONS"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="createForm.warehouseId" placeholder="请选择仓库" style="width: 100%">
            <el-option
              v-for="warehouse in warehouseOptions"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始时间" prop="plannedStartTime">
          <el-date-picker
            v-model="createForm.plannedStartTime"
            type="datetime"
            placeholder="请选择计划开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="计划结束时间" prop="plannedEndTime">
          <el-date-picker
            v-model="createForm.plannedEndTime"
            type="datetime"
            placeholder="请选择计划结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="盘点人" prop="checkerId">
          <el-input v-model.number="createForm.checkerId" placeholder="请输入盘点人ID" />
        </el-form-item>
        <el-form-item label="审核人" prop="reviewerId">
          <el-input v-model.number="createForm.reviewerId" placeholder="请输入审核人ID" />
        </el-form-item>
        <el-form-item label="盘点描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入盘点描述"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核盘点对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="审核盘点"
      width="500px"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="100px"
      >
        <el-form-item label="审核结果" prop="reviewStatus">
          <el-radio-group v-model="reviewForm.reviewStatus">
            <el-radio value="APPROVED">通过</el-radio>
            <el-radio value="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  getCheckPage, 
  createCheck, 
  startCheck, 
  completeCheck, 
  cancelCheck, 
  reviewCheck,
  deleteCheck 
} from '@/api/inventoryCheck'
import { getWarehouseList } from '@/api/warehouse'
import type { InventoryCheck, InventoryCheckRequest } from '@/types/inventory'
import type { Warehouse } from '@/types/warehouse'
import { 
  CHECK_TYPE_OPTIONS, 
  CHECK_STATUS_OPTIONS, 
  REVIEW_STATUS_OPTIONS 
} from '@/types/inventory'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const createDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const createFormRef = ref<FormInstance>()
const reviewFormRef = ref<FormInstance>()

const searchForm = reactive({
  checkType: '',
  warehouseId: null as number | null,
  status: ''
})

const createForm = reactive<InventoryCheckRequest>({
  checkType: 'FULL',
  warehouseId: 0,
  plannedStartTime: '',
  plannedEndTime: '',
  checkerId: 0,
  reviewerId: 0,
  description: '',
  remark: ''
})

const reviewForm = reactive({
  checkId: 0,
  reviewStatus: 'APPROVED',
  reviewComment: ''
})

const createRules: FormRules = {
  checkType: [
    { required: true, message: '请选择盘点类型', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  plannedStartTime: [
    { required: true, message: '请选择计划开始时间', trigger: 'change' }
  ],
  plannedEndTime: [
    { required: true, message: '请选择计划结束时间', trigger: 'change' }
  ],
  checkerId: [
    { required: true, message: '请输入盘点人ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '盘点人ID必须大于0', trigger: 'blur' }
  ],
  reviewerId: [
    { required: true, message: '请输入审核人ID', trigger: 'blur' },
    { type: 'number', min: 1, message: '审核人ID必须大于0', trigger: 'blur' }
  ]
}

const reviewRules: FormRules = {
  reviewStatus: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  reviewComment: [
    { required: true, message: '请输入审核意见', trigger: 'blur' }
  ]
}

const checkList = ref<InventoryCheck[]>([])
const warehouseOptions = ref<Warehouse[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 方法
const loadCheckList = async () => {
  loading.value = true
  try {
    const response = await getCheckPage({
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    if (response.success) {
      checkList.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('加载盘点列表失败')
  } finally {
    loading.value = false
  }
}

const loadWarehouseOptions = async () => {
  try {
    const response = await getWarehouseList({ page: 1, size: 1000 })
    if (response.success) {
      warehouseOptions.value = response.data.records
    }
  } catch (error) {
    ElMessage.error('加载仓库列表失败')
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadCheckList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    checkType: '',
    warehouseId: null,
    status: ''
  })
  pagination.current = 1
  loadCheckList()
}

const showCreateDialog = () => {
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  
  await createFormRef.value.validate()
  
  submitting.value = true
  try {
    await createCheck(createForm)
    ElMessage.success('新建盘点成功')
    createDialogVisible.value = false
    loadCheckList()
  } catch (error) {
    ElMessage.error('新建盘点失败')
  } finally {
    submitting.value = false
  }
}

const handleStartCheck = async (row: InventoryCheck) => {
  try {
    await startCheck(row.id)
    ElMessage.success('开始盘点成功')
    loadCheckList()
  } catch (error) {
    ElMessage.error('开始盘点失败')
  }
}

const handleCompleteCheck = async (row: InventoryCheck) => {
  try {
    await completeCheck(row.id)
    ElMessage.success('完成盘点成功')
    loadCheckList()
  } catch (error) {
    ElMessage.error('完成盘点失败')
  }
}

const handleCancelCheck = async (row: InventoryCheck) => {
  try {
    await cancelCheck(row.id)
    ElMessage.success('取消盘点成功')
    loadCheckList()
  } catch (error) {
    ElMessage.error('取消盘点失败')
  }
}

const handleReviewCheck = (row: InventoryCheck) => {
  reviewForm.checkId = row.id
  reviewForm.reviewStatus = 'APPROVED'
  reviewForm.reviewComment = ''
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate()
  
  submitting.value = true
  try {
    await reviewCheck(reviewForm.checkId, {
      reviewStatus: reviewForm.reviewStatus,
      reviewComment: reviewForm.reviewComment
    })
    ElMessage.success('审核盘点成功')
    reviewDialogVisible.value = false
    loadCheckList()
  } catch (error) {
    ElMessage.error('审核盘点失败')
  } finally {
    submitting.value = false
  }
}

const handleViewDetails = (row: InventoryCheck) => {
  // TODO: 实现查看详情功能
  ElMessage.info('查看详情功能待实现')
}

const handleEdit = (row: InventoryCheck) => {
  // TODO: 实现编辑功能
  ElMessage.info('编辑功能待实现')
}

const handleDelete = async (row: InventoryCheck) => {
  try {
    await ElMessageBox.confirm('确定要删除这个盘点记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteCheck(row.id)
    ElMessage.success('删除成功')
    loadCheckList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleAutoDetect = () => {
  // TODO: 实现自动检测功能
  ElMessage.info('自动检测功能待实现')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadCheckList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadCheckList()
}

// 状态标签和颜色
const getCheckTypeTag = (type: string) => {
  switch (type) {
    case 'FULL': return 'primary'
    case 'PARTIAL': return 'success'
    case 'RANDOM': return 'warning'
    default: return 'info'
  }
}

const getCheckTypeLabel = (type: string) => {
  const option = CHECK_TYPE_OPTIONS.find(opt => opt.value === type)
  return option ? option.label : type
}

const getCheckStatusTag = (status: string) => {
  switch (status) {
    case 'PLANNED': return 'info'
    case 'IN_PROGRESS': return 'warning'
    case 'COMPLETED': return 'success'
    case 'CANCELLED': return 'danger'
    default: return 'info'
  }
}

const getCheckStatusLabel = (status: string) => {
  const option = CHECK_STATUS_OPTIONS.find(opt => opt.value === status)
  return option ? option.label : status
}

const getReviewStatusTag = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
}

const getReviewStatusLabel = (status: string) => {
  const option = REVIEW_STATUS_OPTIONS.find(opt => opt.value === status)
  return option ? option.label : status
}

// 生命周期
onMounted(() => {
  loadCheckList()
  loadWarehouseOptions()
})
</script>

<style scoped>
.inventory-check-list {
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
