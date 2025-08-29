// 数据缓存工具
interface CacheItem<T> {
  data: T
  timestamp: number
  ttl: number // 生存时间（毫秒）
}

class DataCache {
  private cache = new Map<string, CacheItem<any>>()
  private maxSize = 100 // 最大缓存条目数

  // 设置缓存
  set<T>(key: string, data: T, ttl: number = 5 * 60 * 1000): void {
    // 如果缓存已满，删除最旧的条目
    if (this.cache.size >= this.maxSize) {
      const oldestKey = this.cache.keys().next().value
      this.cache.delete(oldestKey)
    }

    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }

  // 获取缓存
  get<T>(key: string): T | null {
    const item = this.cache.get(key)
    if (!item) return null

    // 检查是否过期
    if (Date.now() - item.timestamp > item.ttl) {
      this.cache.delete(key)
      return null
    }

    return item.data
  }

  // 删除缓存
  delete(key: string): boolean {
    return this.cache.delete(key)
  }

  // 清空缓存
  clear(): void {
    this.cache.clear()
  }

  // 获取缓存大小
  size(): number {
    return this.cache.size
  }

  // 检查缓存是否存在
  has(key: string): boolean {
    return this.cache.has(key)
  }

  // 获取缓存键列表
  keys(): string[] {
    return Array.from(this.cache.keys())
  }

  // 清理过期缓存
  cleanup(): void {
    const now = Date.now()
    for (const [key, item] of this.cache.entries()) {
      if (now - item.timestamp > item.ttl) {
        this.cache.delete(key)
      }
    }
  }
}

// 创建全局缓存实例
export const dataCache = new DataCache()

// 定期清理过期缓存
setInterval(() => {
  dataCache.cleanup()
}, 60 * 1000) // 每分钟清理一次

// 缓存键生成器
export const generateCacheKey = (prefix: string, params: Record<string, any>): string => {
  const sortedParams = Object.keys(params)
    .sort()
    .map(key => `${key}=${params[key]}`)
    .join('&')
  return `${prefix}:${sortedParams}`
}

// 分页缓存键生成器
export const generatePageCacheKey = (prefix: string, page: number, size: number, filters: Record<string, any>): string => {
  return generateCacheKey(`${prefix}_page`, { page, size, ...filters })
}

// 导出缓存类
export default DataCache
