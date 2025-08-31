/**
 * 性能优化组件统一导出
 */

// 虚拟滚动组件
export { default as VirtualScroll } from './VirtualScroll.vue'

// 高级分页组件
export { default as AdvancedPagination } from './AdvancedPagination.vue'

// 无限滚动组件
export { default as InfiniteScroll } from './InfiniteScroll.vue'

// 性能工具函数
export * from '@/utils/performance'

// 组件类型定义
export interface VirtualScrollProps {
  data: any[]
  itemHeight: number
  containerHeight: number
  overscan?: number
  itemKey?: string | ((item: any) => string | number)
  loading?: boolean
  hasMore?: boolean
  loadMoreThreshold?: number
}

export interface AdvancedPaginationProps {
  total: number
  currentPage: number
  pageSize: number
  pageSizeOptions?: number[]
  layout?: string
  showBatchActions?: boolean
  selectedItems?: any[]
}

export interface InfiniteScrollProps {
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

// 使用示例
export const componentExamples = {
  virtualScroll: `
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
      </div>
    </template>
  </VirtualScroll>
</template>
  `,
  
  advancedPagination: `
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
  `,
  
  infiniteScroll: `
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
        <span>{{ index + 1 }}. {{ item.title }}</span>
      </div>
    </template>
  </InfiniteScroll>
</template>
  `
}

// 性能优化配置
export const performanceConfig = {
  // 虚拟滚动默认配置
  virtualScroll: {
    itemHeight: 60,
    overscan: 5,
    loadMoreThreshold: 0.8
  },
  
  // 分页默认配置
  pagination: {
    pageSize: 20,
    pageSizeOptions: [10, 20, 50, 100],
    layout: 'total, sizes, prev, pager, next, jumper'
  },
  
  // 无限滚动默认配置
  infiniteScroll: {
    containerHeight: 400,
    threshold: 0.8
  },
  
  // 性能优化默认配置
  performance: {
    debounceSearch: 300,
    throttleScroll: 16,
    lazyLoad: true,
    batchUpdate: true
  }
}

// 组件注册函数（用于全局注册）
export function registerPerformanceComponents(app: any) {
  app.component('VirtualScroll', VirtualScroll)
  app.component('AdvancedPagination', AdvancedPagination)
  app.component('InfiniteScroll', InfiniteScroll)
}

// 默认导出
export default {
  VirtualScroll,
  AdvancedPagination,
  InfiniteScroll,
  performanceConfig,
  registerPerformanceComponents
}
