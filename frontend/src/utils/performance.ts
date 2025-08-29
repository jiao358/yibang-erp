// 性能监控工具
interface PerformanceMetric {
  name: string
  startTime: number
  endTime?: number
  duration?: number
  metadata?: Record<string, any>
}

class PerformanceMonitor {
  private metrics: Map<string, PerformanceMetric> = new Map()
  private observers: Map<string, (metric: PerformanceMetric) => void> = new Map()

  // 开始监控
  start(name: string, metadata?: Record<string, any>): void {
    this.metrics.set(name, {
      name,
      startTime: performance.now(),
      metadata
    })
  }

  // 结束监控
  end(name: string): PerformanceMetric | null {
    const metric = this.metrics.get(name)
    if (!metric) return null

    metric.endTime = performance.now()
    metric.duration = metric.endTime - metric.startTime

    // 通知观察者
    this.notifyObservers(metric)

    return metric
  }

  // 获取指标
  getMetric(name: string): PerformanceMetric | null {
    return this.metrics.get(name) || null
  }

  // 获取所有指标
  getAllMetrics(): PerformanceMetric[] {
    return Array.from(this.metrics.values())
  }

  // 清除指标
  clearMetrics(): void {
    this.metrics.clear()
  }

  // 添加观察者
  addObserver(name: string, callback: (metric: PerformanceMetric) => void): void {
    this.observers.set(name, callback)
  }

  // 移除观察者
  removeObserver(name: string): void {
    this.observers.delete(name)
  }

  // 通知观察者
  private notifyObservers(metric: PerformanceMetric): void {
    this.observers.forEach(callback => {
      try {
        callback(metric)
      } catch (error) {
        console.error('Performance observer error:', error)
      }
    })
  }

  // 性能装饰器
  measure<T extends (...args: any[]) => any>(
    name: string,
    fn: T,
    metadata?: Record<string, any>
  ): T {
    return ((...args: any[]) => {
      this.start(name, metadata)
      try {
        const result = fn(...args)
        if (result instanceof Promise) {
          return result.finally(() => this.end(name))
        } else {
          this.end(name)
          return result
        }
      } catch (error) {
        this.end(name)
        throw error
      }
    }) as T
  }

  // 获取性能报告
  getReport(): {
    totalMetrics: number
    averageDuration: number
    slowestMetric: PerformanceMetric | null
    fastestMetric: PerformanceMetric | null
    metricsByDuration: PerformanceMetric[]
  } {
    const metrics = this.getAllMetrics()
    if (metrics.length === 0) {
      return {
        totalMetrics: 0,
        averageDuration: 0,
        slowestMetric: null,
        fastestMetric: null,
        metricsByDuration: []
      }
    }

    const completedMetrics = metrics.filter(m => m.duration !== undefined)
    const durations = completedMetrics.map(m => m.duration!)
    const averageDuration = durations.reduce((sum, d) => sum + d, 0) / durations.length

    const slowestMetric = completedMetrics.reduce((slowest, current) => 
      current.duration! > slowest.duration! ? current : slowest
    )

    const fastestMetric = completedMetrics.reduce((fastest, current) => 
      current.duration! < fastest.duration! ? current : fastest
    )

    const metricsByDuration = [...completedMetrics].sort((a, b) => 
      (b.duration || 0) - (a.duration || 0)
    )

    return {
      totalMetrics: metrics.length,
      averageDuration,
      slowestMetric,
      fastestMetric,
      metricsByDuration
    }
  }
}

// 创建全局性能监控实例
export const performanceMonitor = new PerformanceMonitor()

// 性能监控装饰器
export const measure = (name: string, metadata?: Record<string, any>) => {
  return (target: any, propertyKey: string, descriptor: PropertyDescriptor) => {
    const originalMethod = descriptor.value
    descriptor.value = performanceMonitor.measure(name, originalMethod, metadata)
    return descriptor
  }
}

// 性能监控函数包装器
export const measureAsync = async <T>(
  name: string,
  fn: () => Promise<T>,
  metadata?: Record<string, any>
): Promise<T> => {
  performanceMonitor.start(name, metadata)
  try {
    const result = await fn()
    performanceMonitor.end(name)
    return result
  } catch (error) {
    performanceMonitor.end(name)
    throw error
  }
}

// 导出性能监控类
export default PerformanceMonitor
