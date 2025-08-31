<template>
  <div 
    ref="containerRef" 
    class="infinite-scroll-container"
    :style="{ height: containerHeight + 'px' }"
    @scroll="handleScroll"
  >
    <!-- 数据列表 -->
    <div class="infinite-scroll-content">
      <slot 
        v-for="(item, index) in data" 
        :key="getItemKey(item, index)"
        :item="item" 
        :index="index"
      />
    </div>

    <!-- 加载状态指示器 -->
    <div v-if="loading" class="loading-indicator">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 加载完成指示器 -->
    <div v-if="!hasMore && data.length > 0" class="end-indicator">
      <el-divider>
        <span class="end-text">已加载全部数据</span>
      </el-divider>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && data.length === 0" class="empty-state">
      <el-empty description="暂无数据" />
    </div>

    <!-- 错误状态 -->
    <div v-if="error" class="error-state">
      <el-alert
        :title="error.message || '加载失败'"
        type="error"
        :closable="false"
        show-icon
      >
        <template #default>
          <p>{{ error.details || '请检查网络连接后重试' }}</p>
          <el-button 
            type="primary" 
            size="small" 
            @click="retry"
          >
            重试
          </el-button>
        </template>
      </el-alert>
    </div>

    <!-- 滚动到顶部按钮 -->
    <el-button
      v-if="showScrollToTop"
      class="scroll-to-top"
      type="primary"
      size="small"
      circle
      @click="scrollToTop"
    >
      <el-icon><ArrowUp /></el-icon>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { Loading, ArrowUp } from '@element-plus/icons-vue'

// Props
interface Props {
  data: any[]
  loading: boolean
  hasMore: boolean
  containerHeight?: number
  threshold?: number
  itemKey?: string | ((item: any) => string | number)
  error?: {
    message?: string
    details?: string
  } | null
}

const props = withDefaults(defineProps<Props>(), {
  containerHeight: 400,
  threshold: 0.8,
  error: null
})

// Emits
const emit = defineEmits<{
  'loadMore': []
  'retry': []
  'scroll': [event: Event]
}>()

// 响应式数据
const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)
const showScrollToTop = ref(false)

// 计算属性
const shouldLoadMore = computed(() => {
  if (!containerRef.value || props.loading || !props.hasMore) return false
  
  const { scrollTop: currentScrollTop, scrollHeight, clientHeight } = containerRef.value
  const scrollRatio = (currentScrollTop + clientHeight) / scrollHeight
  return scrollRatio >= props.threshold
})

// 方法
const getItemKey = (item: any, index: number) => {
  if (typeof props.itemKey === 'function') {
    return props.itemKey(item)
  }
  if (typeof props.itemKey === 'string') {
    return item[props.itemKey]
  }
  return index
}

const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement
  scrollTop.value = target.scrollTop
  
  // 显示/隐藏滚动到顶部按钮
  showScrollToTop.value = scrollTop.value > 200
  
  emit('scroll', event)
  
  // 检查是否需要加载更多
  if (shouldLoadMore.value) {
    loadMore()
  }
}

const loadMore = () => {
  if (!props.loading && props.hasMore) {
    emit('loadMore')
  }
}

const retry = () => {
  emit('retry')
}

const scrollToTop = () => {
  if (containerRef.value) {
    containerRef.value.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

// 滚动到指定位置
const scrollToPosition = (top: number, behavior: 'auto' | 'smooth' = 'auto') => {
  if (containerRef.value) {
    containerRef.value.scrollTo({
      top,
      behavior
    })
  }
}

// 滚动到指定元素
const scrollToElement = (index: number, behavior: 'auto' | 'smooth' = 'auto') => {
  if (containerRef.value) {
    const element = containerRef.value.querySelector(`[data-index="${index}"]`)
    if (element) {
      element.scrollIntoView({ behavior })
    }
  }
}

// 获取滚动位置
const getScrollPosition = () => {
  if (!containerRef.value) return { top: 0, left: 0 }
  
  return {
    top: containerRef.value.scrollTop,
    left: containerRef.value.scrollLeft
  }
}

// 设置滚动位置
const setScrollPosition = (top: number, left?: number) => {
  if (containerRef.value) {
    containerRef.value.scrollTop = top
    if (left !== undefined) {
      containerRef.value.scrollLeft = left
    }
  }
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    if (containerRef.value) {
      containerRef.value.addEventListener('scroll', handleScroll)
    }
  })
})

onUnmounted(() => {
  if (containerRef.value) {
    containerRef.value.removeEventListener('scroll', handleScroll)
  }
})

// 暴露方法
defineExpose({
  scrollToTop,
  scrollToPosition,
  scrollToElement,
  getScrollPosition,
  setScrollPosition
})
</script>

<style scoped>
.infinite-scroll-container {
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.infinite-scroll-content {
  min-height: 100%;
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.loading-indicator .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.end-indicator {
  padding: 20px;
}

.end-text {
  color: #909399;
  font-size: 14px;
}

.empty-state {
  padding: 40px 20px;
}

.error-state {
  padding: 20px;
}

.scroll-to-top {
  position: fixed;
  right: 40px;
  bottom: 40px;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.scroll-to-top:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 自定义滚动条样式 */
.infinite-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.infinite-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.infinite-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.infinite-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .scroll-to-top {
    right: 20px;
    bottom: 20px;
  }
}

/* 动画效果 */
.loading-indicator,
.end-indicator,
.empty-state,
.error-state {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
