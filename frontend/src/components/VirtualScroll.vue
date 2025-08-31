<template>
  <div 
    ref="containerRef" 
    class="virtual-scroll-container"
    :style="{ height: containerHeight + 'px' }"
    @scroll="handleScroll"
  >
    <div 
      class="virtual-scroll-phantom"
      :style="{ height: totalHeight + 'px' }"
    />
    <div
      class="virtual-scroll-content"
      :style="{ transform: `translateY(${offsetY}px)` }"
    >
      <div
        v-for="item in visibleData"
        :key="getItemKey(item, item.index)"
        :style="{ height: itemHeight + 'px' }"
        class="virtual-scroll-item"
      >
        <slot 
          :item="item.data" 
          :index="item.index"
          :isScrolling="isScrolling"
        />
      </div>
    </div>
    
    <!-- 加载更多指示器 -->
    <div 
      v-if="showLoadMore && !loading" 
      class="load-more-trigger"
      :style="{ top: (visibleEndIndex * itemHeight) + 'px' }"
    >
      <el-button 
        type="text" 
        @click="loadMore"
        :loading="loading"
      >
        加载更多
      </el-button>
    </div>
    
    <!-- 加载状态指示器 -->
    <div 
      v-if="loading" 
      class="loading-indicator"
      :style="{ top: (visibleEndIndex * itemHeight) + 'px' }"
    >
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'

// Props
interface Props {
  data: any[]
  itemHeight: number
  containerHeight: number
  overscan?: number
  itemKey?: string | ((item: any) => string | number)
  loading?: boolean
  hasMore?: boolean
  loadMoreThreshold?: number
}

const props = withDefaults(defineProps<Props>(), {
  overscan: 5,
  loading: false,
  hasMore: true,
  loadMoreThreshold: 0.8
})

// Emits
const emit = defineEmits<{
  'loadMore': []
  'scroll': [event: Event]
}>()

// 响应式数据
const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)
const isScrolling = ref(false)
const scrollTimer = ref<NodeJS.Timeout>()

// 计算属性
const totalHeight = computed(() => props.data.length * props.itemHeight)

const startIndex = computed(() => {
  const start = Math.floor(scrollTop.value / props.itemHeight)
  return Math.max(0, start - props.overscan)
})

const endIndex = computed(() => {
  const end = Math.floor((scrollTop.value + props.containerHeight) / props.itemHeight)
  return Math.min(props.data.length - 1, end + props.overscan)
})

const visibleData = computed(() => {
  const items = []
  for (let i = startIndex.value; i <= endIndex.value; i++) {
    if (props.data[i]) {
      items.push({
        data: props.data[i],
        index: i
      })
    }
  }
  return items
})

const offsetY = computed(() => startIndex.value * props.itemHeight)

const visibleEndIndex = computed(() => endIndex.value)

const showLoadMore = computed(() => {
  if (!props.hasMore || props.loading) return false
  const scrollRatio = (scrollTop.value + props.containerHeight) / totalHeight.value
  return scrollRatio >= props.loadMoreThreshold
})

// 方法
const getItemKey = (item: any, index: number) => {
  if (typeof props.itemKey === 'function') {
    return props.itemKey(item.data)
  }
  if (typeof props.itemKey === 'string') {
    return item.data[props.itemKey]
  }
  return index
}

const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement
  scrollTop.value = target.scrollTop
  
  // 防抖处理滚动事件
  if (scrollTimer.value) {
    clearTimeout(scrollTimer.value)
  }
  
  isScrolling.value = true
  scrollTimer.value = setTimeout(() => {
    isScrolling.value = false
  }, 150)
  
  emit('scroll', event)
  
  // 检查是否需要加载更多
  if (showLoadMore.value) {
    loadMore()
  }
}

const loadMore = () => {
  if (!props.loading && props.hasMore) {
    emit('loadMore')
  }
}

// 滚动到指定索引
const scrollToIndex = (index: number, behavior: 'auto' | 'smooth' = 'auto') => {
  if (!containerRef.value) return
  
  const targetScrollTop = index * props.itemHeight
  containerRef.value.scrollTo({
    top: targetScrollTop,
    behavior
  })
}

// 滚动到顶部
const scrollToTop = (behavior: 'auto' | 'smooth' = 'auto') => {
  scrollToIndex(0, behavior)
}

// 滚动到底部
const scrollToBottom = (behavior: 'auto' | 'smooth' = 'auto') => {
  scrollToIndex(props.data.length - 1, behavior)
}

// 获取可见范围
const getVisibleRange = () => {
  return {
    start: startIndex.value,
    end: endIndex.value,
    visible: visibleData.value.length
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
  if (scrollTimer.value) {
    clearTimeout(scrollTimer.value)
  }
  if (containerRef.value) {
    containerRef.value.removeEventListener('scroll', handleScroll)
  }
})

// 监听数据变化，自动滚动到顶部
watch(() => props.data, () => {
  if (scrollTop.value > 0) {
    nextTick(() => {
      scrollToTop()
    })
  }
}, { deep: true })

// 暴露方法
defineExpose({
  scrollToIndex,
  scrollToTop,
  scrollToBottom,
  getVisibleRange
})
</script>

<style scoped>
.virtual-scroll-container {
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
}

.virtual-scroll-phantom {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: -1;
}

.virtual-scroll-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}

.virtual-scroll-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.virtual-scroll-item:hover {
  background-color: #f5f7fa;
}

.load-more-trigger,
.loading-indicator {
  position: absolute;
  left: 0;
  right: 0;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  border-top: 1px solid #f0f0f0;
  z-index: 10;
}

.loading-indicator {
  color: #909399;
  font-size: 14px;
}

.loading-indicator .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

/* 自定义滚动条样式 */
.virtual-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.virtual-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.virtual-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.virtual-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
