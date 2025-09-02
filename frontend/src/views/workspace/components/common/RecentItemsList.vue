<template>
  <div class="recent-items-list">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="items.length === 0" class="empty-container">
      <el-empty description="暂无数据" :image-size="80" />
    </div>
    
    <div v-else class="items-container">
      <div
        v-for="(item, index) in items"
        :key="item.id || index"
        class="item-card"
        @click="handleItemClick(item)"
      >
        <div class="item-content">
          <div class="item-title">{{ getItemTitle(item) }}</div>
          <div class="item-subtitle">{{ getItemSubtitle(item) }}</div>
          <div class="item-meta">
            <span class="item-status" :class="getStatusClass(item)">
              {{ getItemStatus(item) }}
            </span>
            <span class="item-time">{{ getItemTime(item) }}</span>
          </div>
        </div>
        
        <div v-if="showActions" class="item-actions">
          <el-button
            type="primary"
            size="small"
            @click.stop="handleActionClick(item)"
          >
            {{ actionText }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  items: any[]
  type: 'order' | 'user' | 'product' | 'customer' | 'inventory' | 'ai-task'
  loading?: boolean
  showActions?: boolean
  actionText?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  showActions: false,
  actionText: '操作'
})

const emit = defineEmits<{
  actionClick: [item: any]
  itemClick: [item: any]
}>()

// 获取项目标题
const getItemTitle = (item: any) => {
  switch (props.type) {
    case 'order':
      return item.platformOrderNo || `订单 #${item.id}`
    case 'user':
      return item.realName || item.username
    case 'product':
      return item.name
    case 'customer':
      return item.name || item.customerName
    case 'inventory':
      return item.productName
    case 'ai-task':
      return item.fileName || `任务 #${item.id}`
    default:
      return item.name || item.title || `项目 #${item.id}`
  }
}

// 获取项目副标题
const getItemSubtitle = (item: any) => {
  switch (props.type) {
    case 'order':
      return item.customerName || '客户订单'
    case 'user':
      return item.email || item.phone
    case 'product':
      return item.brand || item.category
    case 'customer':
      return item.phone || item.email
    case 'inventory':
      return `库存: ${item.quantity || 0}`
    case 'ai-task':
      return item.taskStatus || '处理中'
    default:
      return item.description || ''
  }
}

// 获取项目状态
const getItemStatus = (item: any) => {
  switch (props.type) {
    case 'order':
      return item.orderStatus || '未知'
    case 'user':
      return item.status || '正常'
    case 'product':
      return item.status || '正常'
    case 'customer':
      return item.status || '正常'
    case 'inventory':
      return item.status || '正常'
    case 'ai-task':
      return item.taskStatus || '处理中'
    default:
      return item.status || '正常'
  }
}

// 获取状态样式类
const getStatusClass = (item: any) => {
  const status = getItemStatus(item).toLowerCase()
  if (status.includes('成功') || status.includes('完成') || status.includes('active')) {
    return 'status-success'
  } else if (status.includes('失败') || status.includes('错误') || status.includes('inactive')) {
    return 'status-error'
  } else if (status.includes('处理') || status.includes('审核') || status.includes('pending')) {
    return 'status-warning'
  }
  return 'status-default'
}

// 获取项目时间
const getItemTime = (item: any) => {
  const time = item.createdAt || item.updatedAt || item.createTime
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 1天内
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString()
  }
}

// 处理项目点击
const handleItemClick = (item: any) => {
  emit('itemClick', item)
}

// 处理操作点击
const handleActionClick = (item: any) => {
  emit('actionClick', item)
}
</script>

<style scoped>
.recent-items-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px 20px;
}

.items-container {
  max-height: 400px;
  overflow-y: auto;
}

.item-card {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #F5F7FA;
  cursor: pointer;
  transition: all 0.3s ease;
}

.item-card:last-child {
  border-bottom: none;
}

.item-card:hover {
  background-color: #F5F7FA;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-subtitle {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.item-status {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.status-success {
  background-color: #F0F9FF;
  color: #67C23A;
}

.status-error {
  background-color: #FEF0F0;
  color: #F56C6C;
}

.status-warning {
  background-color: #FDF6EC;
  color: #E6A23C;
}

.status-default {
  background-color: #F4F4F5;
  color: #909399;
}

.item-time {
  color: #C0C4CC;
}

.item-actions {
  flex-shrink: 0;
  margin-left: 12px;
}

@media (max-width: 768px) {
  .item-card {
    padding: 12px 16px;
  }
  
  .item-title {
    font-size: 13px;
  }
  
  .item-subtitle {
    font-size: 12px;
  }
  
  .item-meta {
    font-size: 11px;
    gap: 8px;
  }
}
</style>
