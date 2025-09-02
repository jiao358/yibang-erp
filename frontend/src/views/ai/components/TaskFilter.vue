<template>
  <div class="task-filter-container">
    <el-card class="filter-card" shadow="never">
      <template #header>
        <div class="filter-header">
          <span class="filter-title">任务筛选</span>
          <el-button type="text" @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置筛选
          </el-button>
        </div>
      </template>
      
      <el-form :model="filterForm" label-width="80px" class="filter-form">
        <el-row :gutter="20">
          <!-- 状态筛选 -->
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select 
                v-model="filterForm.status" 
                placeholder="选择状态"
                clearable
                @change="handleFilterChange"
              >
                <el-option label="全部" value="" />
                <el-option label="等待处理" value="PENDING" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="处理失败" value="FAILED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <!-- 时间范围 -->
          <el-col :span="8">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="handleFilterChange"
              />
            </el-form-item>
          </el-col>
          
          <!-- 文件名搜索 -->
          <el-col :span="6">
            <el-form-item label="文件名">
              <el-input
                v-model="filterForm.fileName"
                placeholder="搜索文件名"
                clearable
                @input="handleFilterChange"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          
          <!-- 排序方式 -->
          <el-col :span="4">
            <el-form-item label="排序">
              <el-select 
                v-model="filterForm.sortBy" 
                placeholder="排序方式"
                @change="handleFilterChange"
              >
                <el-option label="创建时间" value="createdAt" />
                <el-option label="文件名" value="fileName" />
                <el-option label="状态" value="status" />
                <el-option label="处理时间" value="processingTime" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 高级筛选 -->
        <el-collapse v-model="advancedFilterVisible">
          <el-collapse-item title="高级筛选" name="advanced">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-form-item label="最小行数">
                  <el-input-number
                    v-model="filterForm.minRows"
                    :min="0"
                    placeholder="最小行数"
                    @change="handleFilterChange"
                  />
                </el-form-item>
              </el-col>
              
              <el-col :span="6">
                <el-form-item label="最大行数">
                  <el-input-number
                    v-model="filterForm.maxRows"
                    :min="0"
                    placeholder="最大行数"
                    @change="handleFilterChange"
                  />
                </el-form-item>
              </el-col>
              
              <el-col :span="6">
                <el-form-item label="成功率">
                  <el-select 
                    v-model="filterForm.successRate" 
                    placeholder="成功率范围"
                    clearable
                    @change="handleFilterChange"
                  >
                    <el-option label="90%以上" value="90+" />
                    <el-option label="80%以上" value="80+" />
                    <el-option label="70%以上" value="70+" />
                    <el-option label="60%以上" value="60+" />
                    <el-option label="50%以上" value="50+" />
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :span="6">
                <el-form-item label="处理时长">
                  <el-select 
                    v-model="filterForm.processingDuration" 
                    placeholder="处理时长"
                    clearable
                    @change="handleFilterChange"
                  >
                    <el-option label="1分钟内" value="1min" />
                    <el-option label="5分钟内" value="5min" />
                    <el-option label="10分钟内" value="10min" />
                    <el-option label="30分钟内" value="30min" />
                    <el-option label="1小时内" value="1hour" />
                    <el-option label="1小时以上" value="1hour+" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

// Props
interface Props {
  modelValue: TaskFilterForm
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: TaskFilterForm]
  filterChange: [filters: TaskFilterForm]
}>()

// 响应式数据
const advancedFilterVisible = ref(['advanced'])

const filterForm = reactive<TaskFilterForm>({
  status: '',
  dateRange: [],
  fileName: '',
  sortBy: 'createdAt',
  minRows: undefined,
  maxRows: undefined,
  successRate: '',
  processingDuration: ''
})

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  Object.assign(filterForm, newValue)
}, { deep: true })

// 监听筛选表单变化
watch(filterForm, (newValue) => {
  emit('update:modelValue', { ...newValue })
}, { deep: true })

// 方法
const handleFilterChange = () => {
  emit('filterChange', { ...filterForm })
}

const resetFilters = () => {
  Object.assign(filterForm, {
    status: '',
    dateRange: [],
    fileName: '',
    sortBy: 'createdAt',
    minRows: undefined,
    maxRows: undefined,
    successRate: '',
    processingDuration: ''
  })
  emit('filterChange', { ...filterForm })
}

// 类型定义
interface TaskFilterForm {
  status: string
  dateRange: string[]
  fileName: string
  sortBy: string
  minRows?: number
  maxRows?: number
  successRate: string
  processingDuration: string
}
</script>

<style scoped>
.task-filter-container {
  margin-bottom: 20px;
}

.filter-card {
  border: 1px solid #e4e7ed;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.filter-form {
  margin-top: 16px;
}

.el-form-item {
  margin-bottom: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .el-col {
    margin-bottom: 16px;
  }
  
  .filter-form .el-row {
    margin: 0;
  }
}
</style>
