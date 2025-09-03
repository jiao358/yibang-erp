<template>
  <div class="system-monitor">
    <div class="monitor-header">
      <h1>系统监控</h1>
      <div class="refresh-controls">
        <el-button type="primary" @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          inactive-text="手动刷新"
        />
      </div>
    </div>

    <div class="monitor-content">
      <!-- 系统概览 -->
      <div class="overview-section">
        <h2>系统概览</h2>
        <div class="overview-grid">
          <div class="overview-card" v-for="metric in displayMetrics" :key="metric.key">
            <div class="metric-header">
              <span class="metric-label">{{ metric.label }}</span>
              <el-tag :type="metric.status" size="small">{{ metric.statusText }}</el-tag>
            </div>
            <div class="metric-value">{{ metric.value }}</div>
            <div class="metric-trend" :class="metric.trend">
              <el-icon><component :is="metric.trendIcon" /></el-icon>
              {{ metric.trendValue }}
            </div>
          </div>
        </div>
      </div>

      <!-- 性能监控 -->
      <div class="performance-section">
        <h2>性能监控</h2>
        <div class="performance-grid">
          <div class="performance-card">
            <h3>CPU使用率</h3>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon><Cpu /></el-icon>
                <p>CPU使用率图表</p>
              </div>
            </div>
            <div class="performance-stats">
              <div class="stat-item">
                <span class="label">当前：</span>
                <span class="value">{{ cpuUsage.current }}%</span>
              </div>
              <div class="stat-item">
                <span class="label">平均：</span>
                <span class="value">{{ cpuUsage.average }}%</span>
              </div>
              <div class="stat-item">
                <span class="label">峰值：</span>
                <span class="value">{{ cpuUsage.peak }}%</span>
              </div>
            </div>
          </div>

          <div class="performance-card">
            <h3>内存使用率</h3>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon><DataBoard /></el-icon>
                <p>内存使用率图表</p>
              </div>
            </div>
            <div class="performance-stats">
              <div class="stat-item">
                <span class="label">已用：</span>
                <span class="value">{{ memoryUsage.used }}GB</span>
              </div>
              <div class="stat-item">
                <span class="label">总计：</span>
                <span class="value">{{ memoryUsage.total }}GB</span>
              </div>
              <div class="stat-item">
                <span class="label">使用率：</span>
                <span class="value">{{ memoryUsage.percentage }}%</span>
              </div>
            </div>
          </div>

          <div class="performance-card">
            <h3>磁盘使用率</h3>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon><Folder /></el-icon>
                <p>磁盘使用率图表</p>
              </div>
            </div>
            <div class="performance-stats">
              <div class="stat-item">
                <span class="label">已用：</span>
                <span class="value">{{ diskUsage.used }}GB</span>
              </div>
              <div class="stat-item">
                <span class="label">总计：</span>
                <span class="value">{{ diskUsage.total }}GB</span>
              </div>
              <div class="stat-item">
                <span class="label">使用率：</span>
                <span class="value">{{ diskUsage.percentage }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 网络监控 -->
      <div class="network-section">
        <h2>网络监控</h2>
        <div class="network-grid">
          <div class="network-card">
            <h3>网络状态</h3>
            <div class="network-status-list">
              <div class="status-item" v-for="status in networkStatus" :key="status.key">
                <div class="status-info">
                  <span class="status-label">{{ status.label }}</span>
                  <span class="status-value" :class="status.status">{{ status.value }}</span>
                </div>
                <div class="status-details">
                  <span class="detail-item">延迟: {{ status.latency }}ms</span>
                  <span class="detail-item">丢包率: {{ status.packetLoss }}%</span>
                </div>
              </div>
            </div>
          </div>

          <div class="network-card">
            <h3>连接统计</h3>
            <div class="connection-stats">
              <div class="stat-row" v-for="stat in connectionStats" :key="stat.key">
                <span class="stat-label">{{ stat.label }}</span>
                <span class="stat-value">{{ stat.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 服务状态 -->
      <div class="services-section">
        <h2>服务状态</h2>
        <div class="services-grid">
          <div class="service-card" v-for="service in services" :key="service.name">
            <div class="service-header">
              <h4>{{ service.name }}</h4>
              <el-tag :type="service.status" size="small">{{ service.statusText }}</el-tag>
            </div>
            <div class="service-info">
              <div class="info-item">
                <span class="label">端口：</span>
                <span class="value">{{ service.port }}</span>
              </div>
              <div class="info-item">
                <span class="label">版本：</span>
                <span class="value">{{ service.version }}</span>
              </div>
              <div class="info-item">
                <span class="label">运行时间：</span>
                <span class="value">{{ service.uptime }}</span>
              </div>
            </div>
            <div class="service-actions">
              <el-button size="small" @click="restartService(service.name)">重启</el-button>
              <el-button size="small" @click="viewLogs(service.name)">查看日志</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 告警信息 -->
      <div class="alerts-section">
        <h2>告警信息</h2>
        <div class="alerts-list">
          <div class="alert-item" v-for="alert in alerts" :key="alert.id" :class="alert.level">
            <div class="alert-header">
              <el-icon><component :is="alert.icon" /></el-icon>
              <span class="alert-title">{{ alert.title }}</span>
              <span class="alert-time">{{ alert.time }}</span>
            </div>
            <div class="alert-message">{{ alert.message }}</div>
            <div class="alert-actions">
              <el-button size="small" @click="acknowledgeAlert(alert.id)">确认</el-button>
              <el-button size="small" @click="resolveAlert(alert.id)">解决</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, Cpu, DataBoard, Folder
} from '@element-plus/icons-vue'
import { 
  collectAllMetrics, 
  getLatestData, 
  type SystemMetrics 
} from '@/api/monitor'

// 加载状态
const loading = ref(false)
const autoRefresh = ref(false) // 改为false，避免自动刷新
const lastCollectTime = ref<string>('')
const collectStatus = ref<'idle' | 'collecting' | 'success' | 'error'>('idle')

// 系统指标数据
const systemMetricsData = ref<SystemMetrics | null>(null)

// 系统指标显示数据
const displayMetrics = ref([
  { 
    key: 'cpu', 
    label: 'CPU使用率', 
    value: '65%', 
    status: 'warning', 
    statusText: '警告',
    trend: 'up',
    trendValue: '+5%',
    trendIcon: 'ArrowUp'
  },
  { 
    key: 'memory', 
    label: '内存使用率', 
    value: '78%', 
    status: 'warning', 
    statusText: '警告',
    trend: 'up',
    trendValue: '+3%',
    trendIcon: 'ArrowUp'
  },
  { 
    key: 'disk', 
    label: '磁盘使用率', 
    value: '45%', 
    status: 'success', 
    statusText: '正常',
    trend: 'down',
    trendValue: '-2%',
    trendIcon: 'ArrowDown'
  },
  { 
    key: 'network', 
    label: '网络状态', 
    value: '正常', 
    status: 'success', 
    statusText: '正常',
    trend: 'stable',
    trendValue: '稳定',
    trendIcon: 'CircleCheck'
  }
])

// CPU使用率
const cpuUsage = ref({
  current: 65,
  average: 58,
  peak: 89
})

// 内存使用率
const memoryUsage = ref({
  used: 12.5,
  total: 16.0,
  percentage: 78
})

// 磁盘使用率
const diskUsage = ref({
  used: 450,
  total: 1000,
  percentage: 45
})

// 网络状态
const networkStatus = ref([
  { key: 'internal', label: '内网', value: '正常', status: 'success', latency: 2, packetLoss: 0 },
  { key: 'external', label: '外网', value: '正常', status: 'success', latency: 45, packetLoss: 0.1 },
  { key: 'database', label: '数据库', value: '正常', status: 'success', latency: 5, packetLoss: 0 }
])

// 连接统计
const connectionStats = ref([
  { key: 'active', label: '活跃连接', value: '1,234' },
  { key: 'total', label: '总连接数', value: '5,678' },
  { key: 'failed', label: '失败连接', value: '23' },
  { key: 'timeout', label: '超时连接', value: '12' }
])

// 服务状态
const services = ref([
  { 
    name: 'Web服务', 
    status: 'success', 
    statusText: '运行中', 
    port: 8080, 
    version: 'v1.0.0', 
    uptime: '3天 12小时' 
  },
  { 
    name: '数据库服务', 
    status: 'success', 
    statusText: '运行中', 
    port: 3306, 
    version: 'v8.0', 
    uptime: '15天 8小时' 
  },
  { 
    name: 'Redis服务', 
    status: 'success', 
    statusText: '运行中', 
    port: 6379, 
    version: 'v7.0', 
    uptime: '7天 3小时' 
  },
  { 
    name: '消息队列', 
    status: 'warning', 
    statusText: '警告', 
    port: 5672, 
    version: 'v3.12', 
    uptime: '2天 18小时' 
  }
])

// 告警信息
const alerts = ref([
  {
    id: 1,
    level: 'warning',
    icon: 'Warning',
    title: '内存使用率过高',
    message: '系统内存使用率已达到78%，建议及时清理或扩容',
    time: '10分钟前'
  },
  {
    id: 2,
    level: 'info',
    icon: 'InfoFilled',
    title: '系统备份完成',
    message: '每日系统备份已完成，备份文件大小：2.5GB',
    time: '1小时前'
  },
  {
    id: 3,
    level: 'error',
    icon: 'CircleClose',
    title: '数据库连接异常',
    message: '检测到数据库连接池异常，已自动重连',
    time: '2小时前'
  }
])

// 自动刷新定时器
let refreshTimer: number | undefined

// 刷新数据 - 改为手动采集
const refreshData = async () => {
  if (loading.value) return
  
  loading.value = true
  collectStatus.value = 'collecting'
  
  try {
    console.log('开始手动采集监控数据')
    
    // 调用真实API采集所有监控数据
    const data = await collectAllMetrics()
    systemMetricsData.value = data
    lastCollectTime.value = data.collectTime
    
    // 更新显示数据
    updateDisplayData(data)
    
    collectStatus.value = 'success'
    ElMessage.success('监控数据采集成功')
    
    console.log('监控数据采集完成')
    
  } catch (error) {
    console.error('采集监控数据失败:', error)
    collectStatus.value = 'error'
    ElMessage.error('监控数据采集失败，请重试')
  } finally {
    loading.value = false
  }
}

// 更新指标数据（保留以备将来使用）
// const updateMetrics = () => {
//   // 模拟数据变化
//   cpuUsage.value.current = Math.floor(Math.random() * 30) + 50
//   memoryUsage.value.percentage = Math.floor(Math.random() * 20) + 70
//   diskUsage.value.percentage = Math.floor(Math.random() * 15) + 40
// }

// 更新显示数据
const updateDisplayData = (data: SystemMetrics) => {
  // 更新系统指标
  if (data.jvmMetrics) {
    // CPU使用率（暂时使用JVM指标，后续可以扩展）
    const cpuValue = data.jvmMetrics.cpuUsage || 0
    updateMetric('cpu', cpuValue, cpuValue > 80 ? 'warning' : 'success')
    
    // 内存使用率
    const memoryValue = data.jvmMetrics.memoryUsagePercentage
    updateMetric('memory', memoryValue, memoryValue > 80 ? 'warning' : 'success')
    
    // 更新内存使用详情
    memoryUsage.value = {
      used: data.jvmMetrics.usedMemory / (1024 * 1024 * 1024), // 转换为GB
      total: data.jvmMetrics.totalMemory / (1024 * 1024 * 1024), // 转换为GB
      percentage: memoryValue
    }
  }
  
  // 更新数据库状态
  if (data.databaseStatus) {
    const dbValue = data.databaseStatus.connectionUsagePercentage
    updateMetric('disk', dbValue, dbValue > 80 ? 'warning' : 'success')
    
    // 更新连接统计
    connectionStats.value = [
      { key: 'active', label: '活跃连接', value: data.databaseStatus.activeConnections.toString() },
      { key: 'total', label: '总连接数', value: data.databaseStatus.totalConnections.toString() },
      { key: 'failed', label: '失败连接', value: data.databaseStatus.errorCount.toString() },
      { key: 'timeout', label: '超时连接', value: data.databaseStatus.timeoutCount.toString() }
    ]
    
    // 更新网络状态（数据库连接）
    networkStatus.value[2] = {
      key: 'database',
      label: '数据库',
      value: data.databaseStatus.status,
      status: data.databaseStatus.status === '正常' ? 'success' : 'error',
      latency: data.databaseStatus.avgResponseTime,
      packetLoss: 0
    }
  }
  
  // 更新服务状态
  if (data.services) {
    services.value = data.services.map(service => ({
      ...service,
      status: service.status,
      statusText: service.statusText,
      uptime: service.uptime
    }))
  }
  
  // 更新告警信息
  if (data.alerts) {
    alerts.value = data.alerts.map(alert => ({
      id: alert.id,
      level: alert.level,
      icon: getAlertIcon(alert.level),
      title: alert.title,
      message: alert.message,
      time: formatTime(alert.time)
    }))
  }
}

// 更新指标显示
const updateMetric = (key: string, value: number, status: string) => {
  const metric = displayMetrics.value.find((m: any) => m.key === key)
  if (metric) {
    metric.value = `${value.toFixed(1)}%`
    metric.status = status
    metric.statusText = status === 'warning' ? '警告' : '正常'
    metric.trendValue = value > 80 ? '高' : '正常'
    metric.trendIcon = value > 80 ? 'ArrowUp' : 'CircleCheck'
  }
}

// 格式化时间
const formatTime = (timeStr: string): string => {
  try {
    const time = new Date(timeStr)
    const now = new Date()
    const diff = now.getTime() - time.getTime()
    
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    return `${Math.floor(diff / 86400000)}天前`
  } catch {
    return timeStr
  }
}

// 获取告警级别对应的图标
const getAlertIcon = (level: string): string => {
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

// 重启服务
const restartService = (serviceName: string) => {
  ElMessage.info(`正在重启 ${serviceName} 服务...`)
}

// 查看日志
const viewLogs = (serviceName: string) => {
  ElMessage.info(`查看 ${serviceName} 服务日志`)
}

// 确认告警
const acknowledgeAlert = (alertId: number) => {
  alerts.value = alerts.value.filter(alert => alert.id !== alertId)
  ElMessage.success('告警已确认')
}

// 解决告警
const resolveAlert = (alertId: number) => {
  alerts.value = alerts.value.filter(alert => alert.id !== alertId)
  ElMessage.success('告警已解决')
}

// 组件挂载
onMounted(async () => {
  // 首次进入页面，尝试获取最新数据（如果有的话）
  try {
    const latestData = await getLatestData()
    if (latestData && latestData.collectTime) {
      systemMetricsData.value = latestData
      lastCollectTime.value = latestData.collectTime
      updateDisplayData(latestData)
      collectStatus.value = 'success'
    }
  } catch (error) {
    console.log('没有可用的历史数据，请手动采集')
    collectStatus.value = 'idle'
  }
})

// 组件卸载
onUnmounted(() => {
  if (refreshTimer !== undefined) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.system-monitor {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.monitor-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.refresh-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.monitor-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.overview-section, .performance-section, .network-section, .services-section, .alerts-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.overview-section h2, .performance-section h2, .network-section h2, .services-section h2, .alerts-section h2 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 12px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.overview-card {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.overview-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.metric-label {
  font-size: 14px;
  color: #606266;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.metric-trend.up {
  color: #f56c6c;
}

.metric-trend.down {
  color: #67c23a;
}

.metric-trend.stable {
  color: #909399;
}

.performance-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.performance-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.performance-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  margin-bottom: 16px;
}

.chart-placeholder {
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  color: #909399;
}

.chart-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.performance-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-item .label {
  color: #606266;
  font-size: 14px;
}

.stat-item .value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

.network-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.network-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.network-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.network-status-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.status-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.status-label {
  color: #606266;
  font-size: 14px;
}

.status-value {
  font-weight: 500;
  font-size: 14px;
}

.status-value.success {
  color: #67c23a;
}

.status-value.warning {
  color: #e6a23c;
}

.status-value.error {
  color: #f56c6c;
}

.status-details {
  display: flex;
  gap: 16px;
}

.detail-item {
  color: #909399;
  font-size: 12px;
}

.connection-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-row:last-child {
  border-bottom: none;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.stat-value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.service-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.service-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.service-info {
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.info-item .label {
  color: #606266;
  font-size: 14px;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

.service-actions {
  display: flex;
  gap: 8px;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.alert-item {
  padding: 16px;
  border-radius: 6px;
  border-left: 4px solid;
}

.alert-item.warning {
  background: #fdf6ec;
  border-left-color: #e6a23c;
}

.alert-item.info {
  background: #f4f4f5;
  border-left-color: #909399;
}

.alert-item.error {
  background: #fef0f0;
  border-left-color: #f56c6c;
}

.alert-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.alert-title {
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.alert-time {
  color: #909399;
  font-size: 12px;
}

.alert-message {
  color: #606266;
  margin-bottom: 12px;
  line-height: 1.5;
}

.alert-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .system-monitor {
    padding: 16px;
  }
  
  .monitor-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .performance-grid {
    grid-template-columns: 1fr;
  }
  
  .network-grid {
    grid-template-columns: 1fr;
  }
  
  .services-grid {
    grid-template-columns: 1fr;
  }
}
</style>
