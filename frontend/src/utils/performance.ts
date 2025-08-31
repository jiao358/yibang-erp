/**
 * 性能优化工具函数
 */

/**
 * 防抖函数
 * @param func 要防抖的函数
 * @param wait 等待时间（毫秒）
 * @param immediate 是否立即执行
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
  immediate: boolean = false
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null
  
  return function executedFunction(...args: Parameters<T>) {
    const later = () => {
      timeout = null
      if (!immediate) func(...args)
    }
    
    const callNow = immediate && !timeout
    
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    
    if (callNow) func(...args)
  }
}

/**
 * 节流函数
 * @param func 要节流的函数
 * @param limit 限制时间（毫秒）
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean = false
  
  return function executedFunction(...args: Parameters<T>) {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 防抖装饰器（用于类方法）
 */
export function debounceDecorator(wait: number, immediate: boolean = false) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const method = descriptor.value
    
    descriptor.value = debounce(method, wait, immediate)
    
    return descriptor
  }
}

/**
 * 节流装饰器（用于类方法）
 */
export function throttleDecorator(limit: number) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const method = descriptor.value
    
    descriptor.value = throttle(method, limit)
    
    return descriptor
  }
}

/**
 * 懒加载图片
 * @param img 图片元素
 * @param src 图片源
 * @param placeholder 占位图
 */
export function lazyLoadImage(
  img: HTMLImageElement,
  src: string,
  placeholder?: string
): Promise<void> {
  return new Promise((resolve, reject) => {
    // 设置占位图
    if (placeholder) {
      img.src = placeholder
    }
    
    // 创建新的图片对象进行预加载
    const tempImg = new Image()
    
    tempImg.onload = () => {
      img.src = src
      resolve()
    }
    
    tempImg.onerror = () => {
      reject(new Error(`Failed to load image: ${src}`))
    }
    
    tempImg.src = src
  })
}

/**
 * 懒加载组件
 * @param importFn 动态导入函数
 * @param loadingComponent 加载中组件
 * @param errorComponent 错误组件
 */
export function lazyLoadComponent(
  importFn: () => Promise<any>,
  loadingComponent?: any,
  errorComponent?: any
) {
  return {
    component: importFn,
    loading: loadingComponent,
    error: errorComponent,
    delay: 200,
    timeout: 10000
  }
}

/**
 * 虚拟列表计算器
 */
export class VirtualListCalculator {
  private itemHeight: number
  private containerHeight: number
  private overscan: number
  
  constructor(itemHeight: number, containerHeight: number, overscan: number = 5) {
    this.itemHeight = itemHeight
    this.containerHeight = containerHeight
    this.overscan = overscan
  }
  
  /**
   * 计算可见范围
   * @param scrollTop 滚动位置
   * @param totalItems 总项目数
   */
  calculateVisibleRange(scrollTop: number, totalItems: number) {
    const startIndex = Math.floor(scrollTop / this.itemHeight)
    const endIndex = Math.floor((scrollTop + this.containerHeight) / this.itemHeight)
    
    const visibleStart = Math.max(0, startIndex - this.overscan)
    const visibleEnd = Math.min(totalItems - 1, endIndex + this.overscan)
    
    return {
      start: visibleStart,
      end: visibleEnd,
      offsetY: visibleStart * this.itemHeight,
      visibleCount: visibleEnd - visibleStart + 1
    }
  }
  
  /**
   * 计算总高度
   * @param totalItems 总项目数
   */
  calculateTotalHeight(totalItems: number) {
    return totalItems * this.itemHeight
  }
  
  /**
   * 计算滚动到指定索引的位置
   * @param index 目标索引
   */
  calculateScrollPosition(index: number) {
    return index * this.itemHeight
  }
}

/**
 * 性能监控器
 */
export class PerformanceMonitor {
  private metrics: Map<string, number[]> = new Map()
  private observers: Map<string, PerformanceObserver> = new Map()
  
  /**
   * 开始性能监控
   */
  start() {
    // 监控长任务
    if ('PerformanceObserver' in window) {
      try {
        const longTaskObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.recordMetric('longTasks', entry.duration)
          }
        })
        longTaskObserver.observe({ entryTypes: ['longtask'] })
        this.observers.set('longTask', longTaskObserver)
      } catch (e) {
        console.warn('Long task monitoring not supported:', e)
      }
      
      // 监控布局偏移
      try {
        const layoutShiftObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.recordMetric('layoutShifts', (entry as any).value)
          }
        })
        layoutShiftObserver.observe({ entryTypes: ['layout-shift'] })
        this.observers.set('layoutShift', layoutShiftObserver)
      } catch (e) {
        console.warn('Layout shift monitoring not supported:', e)
      }
    }
  }
  
  /**
   * 记录性能指标
   * @param name 指标名称
   * @param value 指标值
   */
  recordMetric(name: string, value: number) {
    if (!this.metrics.has(name)) {
      this.metrics.set(name, [])
    }
    this.metrics.get(name)!.push(value)
  }
  
  /**
   * 获取性能指标统计
   * @param name 指标名称
   */
  getMetricStats(name: string) {
    const values = this.metrics.get(name) || []
    if (values.length === 0) return null
    
    const sorted = values.sort((a, b) => a - b)
    const sum = values.reduce((a, b) => a + b, 0)
    
    return {
      count: values.length,
      min: sorted[0],
      max: sorted[sorted.length - 1],
      avg: sum / values.length,
      median: sorted[Math.floor(sorted.length / 2)],
      p95: sorted[Math.floor(sorted.length * 0.95)],
      p99: sorted[Math.floor(sorted.length * 0.99)]
    }
  }
  
  /**
   * 停止性能监控
   */
  stop() {
    this.observers.forEach(observer => observer.disconnect())
    this.observers.clear()
  }
  
  /**
   * 获取所有指标
   */
  getAllMetrics() {
    const result: Record<string, any> = {}
    for (const [name] of this.metrics) {
      result[name] = this.getMetricStats(name)
    }
    return result
  }
}

/**
 * 内存使用监控器
 */
export class MemoryMonitor {
  /**
   * 获取内存使用情况
   */
  static getMemoryInfo() {
    if ('memory' in performance) {
      const memory = (performance as any).memory
      return {
        usedJSHeapSize: this.formatBytes(memory.usedJSHeapSize),
        totalJSHeapSize: this.formatBytes(memory.totalJSHeapSize),
        jsHeapSizeLimit: this.formatBytes(memory.jsHeapSizeLimit),
        usage: (memory.usedJSHeapSize / memory.jsHeapSizeLimit * 100).toFixed(2) + '%'
      }
    }
    return null
  }
  
  /**
   * 格式化字节数
   * @param bytes 字节数
   */
  private static formatBytes(bytes: number): string {
    if (bytes === 0) return '0 B'
    
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }
  
  /**
   * 强制垃圾回收（仅在开发环境）
   */
  static forceGC() {
    if (process.env.NODE_ENV === 'development') {
      if ('gc' in window) {
        (window as any).gc()
      }
    }
  }
}

/**
 * 防抖的搜索函数
 * @param searchFn 搜索函数
 * @param wait 等待时间
 */
export function createDebouncedSearch<T>(
  searchFn: (query: string) => Promise<T[]>,
  wait: number = 300
) {
  return debounce(async (query: string): Promise<T[]> => {
    if (!query || query.trim().length === 0) {
      return []
    }
    
    try {
      return await searchFn(query.trim())
    } catch (error) {
      console.error('Search error:', error)
      return []
    }
  }, wait)
}

/**
 * 节流的滚动处理函数
 * @param scrollFn 滚动处理函数
 * @param limit 限制时间
 */
export function createThrottledScroll<T>(
  scrollFn: (event: Event) => T,
  limit: number = 16
) {
  return throttle(scrollFn, limit)
}

/**
 * 创建虚拟滚动配置
 * @param itemHeight 项目高度
 * @param containerHeight 容器高度
 * @param overscan 预渲染数量
 */
export function createVirtualScrollConfig(
  itemHeight: number,
  containerHeight: number,
  overscan: number = 5
) {
  return {
    itemHeight,
    containerHeight,
    overscan,
    calculator: new VirtualListCalculator(itemHeight, containerHeight, overscan)
  }
}

/**
 * 性能优化的表格配置
 */
export function createOptimizedTableConfig() {
  return {
    // 虚拟滚动配置
    virtualScroll: true,
    itemHeight: 56, // 表格行高度
    
    // 分页配置
    pagination: {
      pageSize: 20,
      pageSizeOptions: [10, 20, 50, 100],
      showSizeChanger: true,
      showQuickJumper: true,
      showTotal: true
    },
    
    // 性能配置
    performance: {
      debounceSearch: 300,
      throttleScroll: 16,
      lazyLoad: true,
      batchUpdate: true
    }
  }
}

/**
 * 异步性能测量函数
 * @param name 测量名称
 * @param fn 要测量的异步函数
 * @returns 测量结果
 */
export async function measureAsync<T>(
  name: string,
  fn: () => Promise<T>
): Promise<T> {
  const start = performance.now()
  
  try {
    const result = await fn()
    const end = performance.now()
    const duration = end - start
    
    console.log(`[性能测量] ${name}: ${duration.toFixed(2)}ms`)
    
    // 如果支持Performance API，记录到性能时间线
    if (typeof performance !== 'undefined' && performance.mark && performance.measure) {
      performance.mark(`${name}-start`)
      performance.mark(`${name}-end`)
      performance.measure(name, `${name}-start`, `${name}-end`)
    }
    
    return result
  } catch (error) {
    const end = performance.now()
    const duration = end - start
    
    console.error(`[性能测量] ${name}: ${duration.toFixed(2)}ms (失败)`, error)
    throw error
  }
}

/**
 * 同步性能测量函数
 * @param name 测量名称
 * @param fn 要测量的同步函数
 * @returns 测量结果
 */
export function measureSync<T>(
  name: string,
  fn: () => T
): T {
  const start = performance.now()
  
  try {
    const result = fn()
    const end = performance.now()
    const duration = end - start
    
    console.log(`[性能测量] ${name}: ${duration.toFixed(2)}ms`)
    
    // 如果支持Performance API，记录到性能时间线
    if (typeof performance !== 'undefined' && performance.mark && performance.measure) {
      performance.mark(`${name}-start`)
      performance.mark(`${name}-end`)
      performance.measure(name, `${name}-start`, `${name}-end`)
    }
    
    return result
  } catch (error) {
    const end = performance.now()
    const duration = end - start
    
    console.error(`[性能测量] ${name}: ${duration.toFixed(2)}ms (失败)`, error)
    throw error
  }
}
