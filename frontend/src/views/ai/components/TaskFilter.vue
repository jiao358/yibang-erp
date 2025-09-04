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
          <!-- 文件名搜索 -->
          <el-col :span="6">
            <el-form-item label="文件名">
              <el-input
                v-model="filterForm.fileName"
                placeholder="搜索文件名"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
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
              />
            </el-form-item>
          </el-col>
          
          
          

          <!-- 状态筛选 -->
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select 
                v-model="filterForm.status" 
                placeholder="选择状态"
                clearable
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
          
       
          
          <!-- 操作按钮 -->
          <el-col :span="4">
            <el-form-item label=" ">
              <el-button type="primary" @click="handleConfirmFilter">
                <el-icon><Search /></el-icon>
                确认筛选
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 高级筛选已移除 -->
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import type { TaskFilterForm } from '@/types/ai'

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

const filterForm = reactive<TaskFilterForm>({
  status: '',
  dateRange: [],
  fileName: ''
})

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  Object.assign(filterForm, newValue)
}, { deep: true })

// 方法
const handleConfirmFilter = () => {
  emit('update:modelValue', { ...filterForm })
  emit('filterChange', { ...filterForm })
}

const resetFilters = () => {
  Object.assign(filterForm, {
    status: '',
    dateRange: [],
    fileName: ''
  })
  emit('update:modelValue', { ...filterForm })
  emit('filterChange', { ...filterForm })
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
