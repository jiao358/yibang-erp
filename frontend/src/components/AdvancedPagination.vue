<template>
  <div class="advanced-pagination">
    <!-- 分页信息 -->
    <div class="pagination-info">
      <span class="info-text">
        共 {{ total }} 条记录，当前第 {{ currentPage }}/{{ totalPages }} 页
      </span>
      <span class="page-size-info">
        每页显示
        <el-select 
          v-model="pageSize" 
          size="small" 
          style="width: 80px; margin: 0 8px;"
          @change="handlePageSizeChange"
        >
          <el-option 
            v-for="size in pageSizeOptions" 
            :key="size" 
            :label="size" 
            :value="size" 
          />
        </el-select>
        条
      </span>
    </div>

    <!-- 分页导航 -->
    <div class="pagination-nav">
      <!-- 快速跳转 -->
      <div class="quick-jump">
        <span>跳转到</span>
        <el-input
          v-model="jumpPage"
          size="small"
          style="width: 60px; margin: 0 8px;"
          @keyup.enter="handleJumpPage"
          @blur="handleJumpPage"
        />
        <span>页</span>
      </div>

      <!-- 分页按钮 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizeOptions"
        :total="total"
        :layout="layout"
        :background="true"
        :hide-on-single-page="false"
        @current-change="handleCurrentChange"
        @size-change="handlePageSizeChange"
      />

      <!-- 快速导航按钮 -->
      <div class="quick-nav">
        <el-button 
          size="small" 
          :disabled="currentPage === 1"
          @click="goToFirst"
        >
          首页
        </el-button>
        <el-button 
          size="small" 
          :disabled="currentPage === 1"
          @click="goToPrev"
        >
          上一页
        </el-button>
        <el-button 
          size="small" 
          :disabled="currentPage === totalPages"
          @click="goToNext"
        >
          下一页
        </el-button>
        <el-button 
          size="small" 
          :disabled="currentPage === totalPages"
          @click="goToLast"
        >
          末页
        </el-button>
      </div>
    </div>

    <!-- 批量操作 -->
    <div v-if="showBatchActions" class="batch-actions">
      <el-checkbox 
        v-model="selectAll" 
        :indeterminate="isIndeterminate"
        @change="handleSelectAllChange"
      >
        全选
      </el-checkbox>
      <span class="selected-count">
        已选择 {{ selectedCount }} 项
      </span>
      <el-button 
        v-if="selectedCount > 0"
        type="danger" 
        size="small"
        @click="handleBatchDelete"
      >
        批量删除
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
interface Props {
  total: number
  currentPage: number
  pageSize: number
  pageSizeOptions?: number[]
  layout?: string
  showBatchActions?: boolean
  selectedItems?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  pageSizeOptions: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  showBatchActions: false,
  selectedItems: () => []
})

// Emits
const emit = defineEmits<{
  'update:currentPage': [page: number]
  'update:pageSize': [size: number]
  'currentChange': [page: number]
  'sizeChange': [size: number]
  'jumpPage': [page: number]
  'selectAll': [selected: boolean]
  'batchDelete': []
}>()

// 响应式数据
const jumpPage = ref('')
const selectAll = ref(false)

// 计算属性
const totalPages = computed(() => Math.ceil(props.total / props.pageSize))

const selectedCount = computed(() => props.selectedItems.length)

const isIndeterminate = computed(() => {
  return selectedCount.value > 0 && selectedCount.value < props.total
})

// 监听器
watch(() => props.currentPage, (newPage) => {
  jumpPage.value = newPage.toString()
}, { immediate: true })

watch(() => props.selectedItems, (items) => {
  if (items.length === 0) {
    selectAll.value = false
  } else if (items.length === props.total) {
    selectAll.value = true
  }
}, { deep: true })

// 方法
const handleCurrentChange = (page: number) => {
  emit('update:currentPage', page)
  emit('currentChange', page)
}

const handlePageSizeChange = (size: number) => {
  emit('update:pageSize', size)
  emit('sizeChange', size)
  // 重置到第一页
  handleCurrentChange(1)
}

const handleJumpPage = () => {
  const page = parseInt(jumpPage.value)
  if (isNaN(page) || page < 1 || page > totalPages.value) {
    ElMessage.warning(`请输入1-${totalPages.value}之间的页码`)
    jumpPage.value = props.currentPage.toString()
    return
  }
  
  if (page !== props.currentPage) {
    handleCurrentChange(page)
  }
}

const goToFirst = () => {
  if (props.currentPage !== 1) {
    handleCurrentChange(1)
  }
}

const goToPrev = () => {
  if (props.currentPage > 1) {
    handleCurrentChange(props.currentPage - 1)
  }
}

const goToNext = () => {
  if (props.currentPage < totalPages.value) {
    handleCurrentChange(props.currentPage + 1)
  }
}

const goToLast = () => {
  if (props.currentPage !== totalPages.value) {
    handleCurrentChange(totalPages.value)
  }
}

const handleSelectAllChange = (selected: boolean) => {
  emit('selectAll', selected)
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCount.value} 项吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    emit('batchDelete')
  } catch (error) {
    // 用户取消
  }
}

// 暴露方法
defineExpose({
  goToFirst,
  goToPrev,
  goToNext,
  goToLast,
  jumpToPage: handleJumpPage
})
</script>

<style scoped>
.advanced-pagination {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.info-text {
  font-weight: 500;
}

.page-size-info {
  display: flex;
  align-items: center;
}

.pagination-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.quick-jump {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.quick-nav {
  display: flex;
  gap: 8px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.selected-count {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pagination-nav {
    flex-direction: column;
    gap: 12px;
  }
  
  .pagination-info {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .quick-nav {
    justify-content: center;
  }
}

/* 分页器样式优化 */
:deep(.el-pagination) {
  --el-pagination-font-size: 14px;
  --el-pagination-button-margin: 0 4px;
}

:deep(.el-pagination .el-pager li) {
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  border-radius: 4px;
}

:deep(.el-pagination .el-pager li.is-active) {
  background-color: #409eff;
  color: #fff;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  min-width: 32px;
  height: 32px;
  border-radius: 4px;
}

:deep(.el-pagination .el-pagination__sizes) {
  margin-right: 16px;
}

:deep(.el-pagination .el-pagination__jump) {
  margin-left: 16px;
}
</style>
