# 性能优化组件使用指南

本目录包含了用于提升应用性能的组件和工具函数。

## 组件列表

### 1. VirtualScroll - 虚拟滚动组件

用于处理大量数据的列表渲染，只渲染可见区域的数据，大幅提升性能。

#### 基本用法

```vue
<template>
  <VirtualScroll
    :data="items"
    :item-height="60"
    :container-height="400"
    :loading="loading"
    :has-more="hasMore"
    @load-more="loadMore"
  >
    <template #default="{ item, index }">
      <div class="list-item">
        <span>{{ index + 1 }}. {{ item.name }}</span>
        <p>{{ item.description }}</p>
      </div>
    </template>
  </VirtualScroll>
</template>

<script setup>
import { VirtualScroll } from '@/components'
import { ref } from 'vue'

const items = ref([])
const loading = ref(false)
const hasMore = ref(true)

const loadMore = () => {
  // 加载更多数据
  loading.value = true
  // ... 异步加载逻辑
}
</script>
```

#### 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| data | Array | [] | 要渲染的数据数组 |
| itemHeight | Number | - | 每个项目的高度（像素） |
| containerHeight | Number | 400 | 容器高度（像素） |
| overscan | Number | 5 | 预渲染的项目数量 |
| itemKey | String/Function | - | 项目的唯一标识 |
| loading | Boolean | false | 是否正在加载 |
| hasMore | Boolean | true | 是否还有更多数据 |
| loadMoreThreshold | Number | 0.8 | 触发加载更多的阈值 |

#### 事件

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| loadMore | 需要加载更多数据时触发 | - |
| scroll | 滚动时触发 | event: Event |

### 2. AdvancedPagination - 高级分页组件

提供完整的分页功能，包括快速跳转、批量操作等。

#### 基本用法

```vue
<template>
  <AdvancedPagination
    :total="total"
    :current-page="currentPage"
    :page-size="pageSize"
    :show-batch-actions="true"
    :selected-items="selectedItems"
    @current-change="handlePageChange"
    @size-change="handleSizeChange"
    @select-all="handleSelectAll"
    @batch-delete="handleBatchDelete"
  />
</template>

<script setup>
import { AdvancedPagination } from '@/components'
import { ref } from 'vue'

const total = ref(1000)
const currentPage = ref(1)
const pageSize = ref(20)
const selectedItems = ref([])

const handlePageChange = (page) => {
  currentPage.value = page
  // 加载对应页数据
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  // 重新加载数据
}

const handleSelectAll = (selected) => {
  // 处理全选逻辑
}

const handleBatchDelete = () => {
  // 处理批量删除
}
</script>
```

#### 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| total | Number | - | 总记录数 |
| currentPage | Number | - | 当前页码 |
| pageSize | Number | - | 每页显示数量 |
| pageSizeOptions | Array | [10,20,50,100] | 每页显示数量选项 |
| layout | String | 'total, sizes, prev, pager, next, jumper' | 分页器布局 |
| showBatchActions | Boolean | false | 是否显示批量操作 |
| selectedItems | Array | [] | 已选择的项目 |

### 3. InfiniteScroll - 无限滚动组件

支持自动加载更多数据的滚动容器。

#### 基本用法

```vue
<template>
  <InfiniteScroll
    :data="items"
    :loading="loading"
    :has-more="hasMore"
    :container-height="500"
    @load-more="loadMore"
    @retry="retry"
  >
    <template #default="{ item, index }">
      <div class="scroll-item">
        <h3>{{ item.title }}</h3>
        <p>{{ item.content }}</p>
      </div>
    </template>
  </InfiniteScroll>
</template>

<script setup>
import { InfiniteScroll } from '@/components'
import { ref } from 'vue'

const items = ref([])
const loading = ref(false)
const hasMore = ref(true)

const loadMore = () => {
  // 加载更多数据
}

const retry = () => {
  // 重试加载
}
</script>
```

## 工具函数

### 防抖和节流

```typescript
import { debounce, throttle } from '@/components'

// 防抖搜索
const debouncedSearch = debounce((query) => {
  // 执行搜索
}, 300)

// 节流滚动
const throttledScroll = throttle((event) => {
  // 处理滚动事件
}, 16)
```

### 性能监控

```typescript
import { PerformanceMonitor, MemoryMonitor } from '@/components'

// 创建性能监控器
const monitor = new PerformanceMonitor()
monitor.start()

// 获取内存信息
const memoryInfo = MemoryMonitor.getMemoryInfo()
console.log('内存使用:', memoryInfo)
```

### 虚拟滚动计算器

```typescript
import { VirtualListCalculator } from '@/components'

const calculator = new VirtualListCalculator(60, 400, 5)
const range = calculator.calculateVisibleRange(100, 1000)
console.log('可见范围:', range)
```

## 性能优化建议

### 1. 数据渲染优化

- 使用虚拟滚动处理大量数据
- 合理设置 `itemHeight` 和 `overscan`
- 避免在滚动过程中进行复杂计算

### 2. 事件处理优化

- 使用防抖处理搜索输入
- 使用节流处理滚动事件
- 避免频繁的DOM操作

### 3. 内存管理

- 及时清理不需要的事件监听器
- 使用 `v-if` 而不是 `v-show` 控制组件显示
- 合理使用 `keep-alive` 缓存组件

### 4. 网络请求优化

- 实现分页加载
- 使用缓存减少重复请求
- 实现请求去重和取消

## 最佳实践

### 1. 虚拟滚动配置

```typescript
// 推荐配置
const virtualScrollConfig = {
  itemHeight: 60,        // 固定高度，避免动态计算
  overscan: 5,           // 预渲染数量，平衡性能和体验
  loadMoreThreshold: 0.8 // 提前触发加载，避免用户等待
}
```

### 2. 分页配置

```typescript
// 推荐配置
const paginationConfig = {
  pageSize: 20,          // 适中的页面大小
  pageSizeOptions: [10, 20, 50, 100], // 常用选项
  showQuickJumper: true, // 快速跳转
  showTotal: true        // 显示总数
}
```

### 3. 无限滚动配置

```typescript
// 推荐配置
const infiniteScrollConfig = {
  threshold: 0.8,        // 提前触发加载
  containerHeight: 500,  // 合适的容器高度
  errorRetry: true       // 错误重试
}
```

## 注意事项

1. **虚拟滚动**：确保 `itemHeight` 是固定值，避免动态高度导致的渲染问题
2. **分页组件**：大数据量时建议使用虚拟滚动，避免一次性渲染所有数据
3. **无限滚动**：注意内存管理，及时清理不需要的数据
4. **性能监控**：生产环境建议关闭详细的性能监控，避免影响性能

## 兼容性

- Vue 3.x
- Element Plus 2.x
- 现代浏览器（支持 ES6+）
- 移动端友好，支持触摸滚动

## 更新日志

### v1.0.0
- 初始版本发布
- 支持虚拟滚动、分页、无限滚动
- 提供完整的性能优化工具函数
