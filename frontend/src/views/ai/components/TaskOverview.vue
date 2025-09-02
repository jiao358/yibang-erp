<template>
  <div class="task-overview-container">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ props.statistics.totalTasks }}</div>
            <div class="stat-label">总任务数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card processing">
          <div class="stat-icon">
            <el-icon><Loading /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ props.statistics.processingTasks }}</div>
            <div class="stat-label">处理中</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card success">
          <div class="stat-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ props.statistics.completedTasks }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card failed">
          <div class="stat-icon">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ props.statistics.failedTasks }}</div>
            <div class="stat-label">失败</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快速操作区域 -->
    <div class="quick-actions">
      <el-button type="primary" @click="$emit('uploadNew')">
        <el-icon><Upload /></el-icon>
        上传新文件
      </el-button>
      
      <el-button @click="$emit('refresh')">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
      
      <el-button @click="$emit('viewAll')">
        <el-icon><View /></el-icon>
        查看任务列表
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { 
  Document, 
  Loading, 
  CircleCheck, 
  CircleClose, 
  Upload, 
  Refresh, 
  View 
} from '@element-plus/icons-vue'

// Props
interface Props {
  statistics: {
    totalTasks: number
    processingTasks: number
    completedTasks: number
    failedTasks: number
  }
  autoRefresh?: boolean
  refreshInterval?: number
}

const props = withDefaults(defineProps<Props>(), {
  autoRefresh: true,
  refreshInterval: 30000 // 30秒
})

// Emits
const emit = defineEmits<{
  uploadNew: []
  refresh: []
  viewAll: []
}>()

// 响应式数据 - 移除硬编码的statistics
// const statistics = ref({ ... }) - 删除这行

// 定时器
let refreshTimer: NodeJS.Timeout | null = null

// 方法
const loadStatistics = async () => {
  try {
    // 触发父组件刷新统计数据
    emit('refresh')
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const startAutoRefresh = () => {
  if (props.autoRefresh && props.refreshInterval > 0) {
    refreshTimer = setInterval(() => {
      loadStatistics()
    }, props.refreshInterval)
  }
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 生命周期
onMounted(() => {
  loadStatistics()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.task-overview-container {
  margin-bottom: 24px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
}

.stat-card.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card.processing {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-card.success {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-card.failed {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
  opacity: 0.8;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.quick-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.quick-actions .el-button {
  min-width: 120px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-cards .el-col {
    margin-bottom: 16px;
  }
  
  .quick-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .quick-actions .el-button {
    width: 100%;
    max-width: 200px;
  }
}
</style>
