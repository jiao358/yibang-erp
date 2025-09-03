import request from '@/utils/request'

/**
 * 系统监控API接口
 * 实现按需采集的监控数据获取
 */

export interface SystemMetrics {
  collectTime: string
  systemInfo?: {
    osName: string
    osVersion: string
    javaVersion: string
    javaVendor: string
    startTime: string
    uptime: number
    processorCount: number
    hostname: string
  }
  jvmMetrics?: {
    totalMemory: number
    freeMemory: number
    usedMemory: number
    memoryUsagePercentage: number
    threadCount: number
    peakThreadCount: number
    totalStartedThreadCount: number
    cpuUsage: number
  }
  databaseStatus?: {
    status: string
    activeConnections: number
    totalConnections: number
    idleConnections: number
    maxConnections: number
    connectionUsagePercentage: number
    avgResponseTime: number
    errorCount: number
    timeoutCount: number
  }
  services?: Array<{
    name: string
    status: string
    statusText: string
    port: number
    version: string
    uptime: string
    lastCheckTime: string
  }>
  alerts?: Array<{
    id: number
    level: string
    title: string
    message: string
    time: string
    status: string
  }>
}

export interface DatabaseStatus {
  collectTime: string
  status: string
  activeConnections: number
  totalConnections: number
  idleConnections: number
  maxConnections: number
  connectionUsagePercentage: number
  avgResponseTime: number
  errorCount: number
  timeoutCount: number
  version: string
  databaseName: string
  lastCheckTime: string
}

/**
 * 手动采集系统信息
 */
export const collectSystemInfo = (): Promise<SystemMetrics> => {
  return request.post('/api/monitor/collect-system-info')
}

/**
 * 手动采集JVM指标
 */
export const collectJVMMetrics = (): Promise<SystemMetrics> => {
  return request.post('/api/monitor/collect-jvm-metrics')
}

/**
 * 手动采集数据库状态
 */
export const collectDatabaseStatus = (): Promise<DatabaseStatus> => {
  return request.post('/api/monitor/collect-database-status')
}

/**
 * 获取最新采集的数据
 */
export const getLatestData = (): Promise<SystemMetrics> => {
  return request.get('/api/monitor/get-latest-data')
}

/**
 * 采集所有监控数据
 */
export const collectAllMetrics = (): Promise<SystemMetrics> => {
  return request.post('/api/monitor/collect-all-metrics')
}

/**
 * 健康检查
 */
export const checkHealth = (): Promise<string> => {
  return request.get('/api/monitor/health')
}

/**
 * 格式化内存大小
 */
export const formatMemorySize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化运行时间
 */
export const formatUptime = (milliseconds: number): string => {
  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) {
    return `${days}天 ${hours % 24}小时 ${minutes % 60}分钟`
  } else if (hours > 0) {
    return `${hours}小时 ${minutes % 60}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟 ${seconds % 60}秒`
  } else {
    return `${seconds}秒`
  }
}

/**
 * 获取状态对应的颜色
 */
export const getStatusColor = (status: string): string => {
  switch (status) {
    case 'success':
      return '#67C23A'
    case 'warning':
      return '#E6A23C'
    case 'error':
      return '#F56C6C'
    case 'info':
      return '#909399'
    default:
      return '#909399'
  }
}

/**
 * 获取告警级别对应的图标
 */
export const getAlertIcon = (level: string): string => {
  switch (level) {
    case 'info':
      return 'InfoFilled'
    case 'warning':
      return 'Warning'
    case 'error':
      return 'CircleClose'
    default:
      return 'InfoFilled'
  }
}
