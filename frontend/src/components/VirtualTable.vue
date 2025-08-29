<template>
  <div class="virtual-table" :style="{ height: height + 'px' }">
    <div class="virtual-table-header" :style="{ height: headerHeight + 'px' }">
      <div
        v-for="column in columns"
        :key="column.prop"
        class="virtual-table-header-cell"
        :style="{ width: column.width + 'px', left: getColumnLeft(column) + 'px' }"
      >
        {{ column.label }}
      </div>
    </div>
    
    <div class="virtual-table-body" :style="{ height: bodyHeight + 'px' }">
      <div
        class="virtual-table-content"
        :style="{ height: totalHeight + 'px', transform: `translateY(${offsetY}px)` }"
      >
        <div
          v-for="(item, index) in visibleItems"
          :key="getItemKey(item, startIndex + index)"
          class="virtual-table-row"
          :style="{ height: itemHeight + 'px' }"
          @click="handleRowClick(item, startIndex + index)"
        >
          <div
            v-for="column in columns"
            :key="column.prop"
            class="virtual-table-cell"
            :style="{ width: column.width + 'px', left: getColumnLeft(column) + 'px' }"
          >
            <slot :name="column.prop" :row="item" :index="startIndex + index">
              {{ getCellValue(item, column.prop) }}
            </slot>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

interface Column {
  prop: string
  label: string
  width: number
}

interface Props {
  data: any[]
  columns: Column[]
  itemHeight: number
  headerHeight: number
  height: number
  bufferSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  bufferSize: 5
})

const emit = defineEmits<{
  'row-click': [item: any, index: number]
}>()

// 响应式数据
const scrollTop = ref(0)
const containerHeight = ref(0)

// 计算属性
const bodyHeight = computed(() => props.height - props.headerHeight)
const totalHeight = computed(() => props.data.length * props.itemHeight)
const visibleCount = computed(() => Math.ceil(bodyHeight.value / props.itemHeight) + props.bufferSize * 2)
const startIndex = computed(() => Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - props.bufferSize))
const endIndex = computed(() => Math.min(props.data.length, startIndex.value + visibleCount.value))
const offsetY = computed(() => startIndex.value * props.itemHeight)
const visibleItems = computed(() => props.data.slice(startIndex.value, endIndex.value))

// 方法
const getColumnLeft = (column: Column): number => {
  let left = 0
  for (const col of props.columns) {
    if (col.prop === column.prop) break
    left += col.width
  }
  return left
}

const getItemKey = (item: any, index: number): string | number => {
  return item.id || item.key || index
}

const getCellValue = (item: any, prop: string): any => {
  return prop.split('.').reduce((obj, key) => obj?.[key], item)
}

const handleScroll = (event: Event): void => {
  const target = event.target as HTMLElement
  scrollTop.value = target.scrollTop
}

const handleRowClick = (item: any, index: number): void => {
  emit('row-click', item, index)
}

// 生命周期
onMounted(() => {
  const container = document.querySelector('.virtual-table-body')
  if (container) {
    container.addEventListener('scroll', handleScroll)
    containerHeight.value = container.clientHeight
  }
})

onUnmounted(() => {
  const container = document.querySelector('.virtual-table-body')
  if (container) {
    container.removeEventListener('scroll', handleScroll)
  }
})

// 监听数据变化
watch(() => props.data, () => {
  scrollTop.value = 0
}, { deep: true })
</script>

<style scoped>
.virtual-table {
  position: relative;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.virtual-table-header {
  position: relative;
  background-color: #fafafa;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
}

.virtual-table-header-cell {
  position: absolute;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 12px;
  font-weight: 600;
  color: #606266;
  border-right: 1px solid #e4e7ed;
  box-sizing: border-box;
}

.virtual-table-body {
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
}

.virtual-table-content {
  position: relative;
}

.virtual-table-row {
  position: absolute;
  width: 100%;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.virtual-table-row:hover {
  background-color: #f5f7fa;
}

.virtual-table-cell {
  position: absolute;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 12px;
  color: #606266;
  border-right: 1px solid #f0f0f0;
  box-sizing: border-box;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.virtual-table-cell:last-child {
  border-right: none;
}
</style>
