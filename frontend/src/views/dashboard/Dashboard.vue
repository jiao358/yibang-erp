<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section material-card">
      <h1 class="welcome-title">
        欢迎回来，系统管理员！
      </h1>
      <p class="welcome-subtitle">
        今天是 {{ currentDate }}，祝您工作愉快！
      </p>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card material-card" v-for="stat in stats" :key="stat.title">
        <div class="stat-icon">
          <el-icon :size="32" :color="stat.color">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stat.value }}</div>
          <div class="stat-title">{{ stat.title }}</div>
        </div>
        <div class="stat-trend" :class="stat.trend">
          <el-icon>
            <component :is="stat.trendIcon" />
          </el-icon>
          <span>{{ stat.trendValue }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷操作区域 -->
    <div class="quick-actions material-card">
      <h2 class="section-title">快捷操作</h2>
      <div class="actions-grid">
        <div 
          v-for="action in quickActions" 
          :key="action.title"
          class="action-item material-card"
          @click="navigateTo(action.path)"
        >
          <div class="action-icon">
            <el-icon :size="24" color="var(--primary-color)">
              <component :is="action.icon" />
            </el-icon>
          </div>
          <div class="action-content">
            <h3 class="action-title">{{ action.title }}</h3>
            <p class="action-desc">{{ action.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 系统信息区域 -->
    <div class="system-info material-card">
      <h2 class="section-title">系统信息</h2>
      <div class="info-grid">
        <div class="info-section">
          <h3 class="info-title">系统状态</h3>
          <div class="status-list">
            <div class="status-item">
              <span class="status-label">系统版本：</span>
              <span class="status-value">v1.0.0</span>
            </div>
            <div class="status-item">
              <span class="status-label">运行时间：</span>
              <span class="status-value">{{ uptime }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">最后更新：</span>
              <span class="status-value">{{ lastUpdate }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">系统状态：</span>
              <span class="status-indicator online"></span>
              <span class="status-value">运行正常</span>
            </div>
          </div>
        </div>
        
        <div class="info-section">
          <h3 class="info-title">最近活动</h3>
          <div class="activity-list">
            <div 
              v-for="activity in recentActivities" 
              :key="activity.id"
              class="activity-item"
            >
              <span class="activity-time">{{ activity.time }}</span>
              <span class="activity-text">{{ activity.text }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 实时监控区域 -->
    <div class="monitor-section material-card">
      <h2 class="section-title">实时监控</h2>
      <div class="monitor-grid">
        <div class="monitor-item material-card">
          <h3 class="monitor-title">系统性能</h3>
          <div class="performance-metrics">
            <div class="metric-item">
              <span class="metric-label">CPU使用率</span>
              <div class="progress-bar">
                <div class="progress-fill" style="width: 65%"></div>
              </div>
              <span class="metric-value">65%</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">内存使用率</span>
              <div class="progress-bar">
                <div class="progress-fill" style="width: 78%"></div>
              </div>
              <span class="metric-value">78%</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">磁盘使用率</span>
              <div class="progress-bar">
                <div class="progress-fill" style="width: 45%"></div>
              </div>
              <span class="metric-value">45%</span>
            </div>
          </div>
        </div>
        
        <div class="monitor-item material-card">
          <h3 class="monitor-title">网络状态</h3>
          <div class="network-status">
            <div class="network-item">
              <span class="network-label">内网状态</span>
              <span class="network-status-indicator online">正常</span>
            </div>
            <div class="network-item">
              <span class="network-label">外网状态</span>
              <span class="network-status-indicator online">正常</span>
            </div>
            <div class="network-item">
              <span class="network-label">数据库连接</span>
              <span class="network-status-indicator online">正常</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  User, Setting, OfficeBuilding, Goods, Document, Monitor,
  TrendCharts, Cpu, DataBoard
} from '@element-plus/icons-vue'

const router = useRouter()

// 响应式数据
const userInfo = ref({
  username: 'admin',
  realName: '系统管理员',
  roles: ['ADMIN']
})

const systemInfo = ref({
  version: 'v1.0.0',
  lastUpdate: new Date('2024-01-14T10:00:00'),
  uptime: '3天 12小时 30分钟'
})

const stats = ref([
  { title: '用户总数', value: '1,234', icon: User, color: 'var(--primary-color)', trend: 'up', trendValue: '+12%', trendIcon: 'TrendCharts' },
  { title: '公司总数', value: '89', icon: OfficeBuilding, color: 'var(--success-color)', trend: 'up', trendValue: '+5%', trendIcon: 'TrendCharts' },
  { title: '订单总数', value: '5,678', icon: Document, color: 'var(--warning-color)', trend: 'up', trendValue: '+23%', trendIcon: 'TrendCharts' },
  { title: '产品总数', value: '456', icon: Goods, color: 'var(--info-color)', trend: 'up', trendValue: '+8%', trendIcon: 'TrendCharts' }
])

const recentActivities = ref([
  { id: 1, time: '10:30', text: '用户张三登录系统' },
  { id: 2, time: '10:25', text: '新订单 #12345 已创建' },
  { id: 3, time: '10:20', text: '产品信息已更新' },
  { id: 4, time: '10:15', text: '系统备份完成' },
  { id: 5, time: '10:10', text: '用户权限已修改' }
])

// 计算属性
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const uptime = computed(() => {
  const now = new Date()
  const diff = now.getTime() - new Date(systemInfo.value.lastUpdate).getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  let result = ''
  if (days > 0) result += `${days}天 `
  if (hours > 0) result += `${hours}小时 `
  if (minutes > 0) result += `${minutes}分钟`
  return result.trim()
})

const lastUpdate = computed(() => {
  return new Date(systemInfo.value.lastUpdate).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  })
})

// 权限检查
const hasPermission = (requiredRoles: string[]) => {
  const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]')
  return requiredRoles.some(role => 
    userRoles.includes(role) || userRoles.includes('ADMIN')
  )
}

// 导航到指定页面
const navigateTo = (path: string) => {
  router.push(path)
}

// 获取统计数据
const fetchStats = async () => {
  try {
    // 这里可以调用API获取真实的统计数据
    // 暂时使用模拟数据
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取用户信息
const fetchUserInfo = () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  }
}

// 快捷操作数据
const quickActions = ref([
  { title: '用户管理', description: '管理系统用户和权限', icon: User, path: '/user' },
  { title: '角色管理', description: '配置用户角色和权限', icon: Setting, path: '/role' },
  { title: '公司管理', description: '管理供应商和销售商', icon: OfficeBuilding, path: '/company' },
  { title: '产品管理', description: '管理产品信息和库存', icon: Goods, path: '/product' },
  { title: '订单管理', description: '处理订单和发货', icon: Document, path: '/order' },
  { title: '数字大屏', description: '查看实时数据大屏', icon: Monitor, path: '/digital-screen' }
])

// 组件挂载
onMounted(() => {
  fetchUserInfo()
  fetchStats()
})
</script>

<style scoped>
.dashboard {
  padding: var(--spacing-xl);
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
  background-color: var(--background-color);
  min-height: 100vh;
}

/* 欢迎区域 */
.welcome-section {
  padding: var(--spacing-xxl);
  text-align: center;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: white;
  position: relative;
  overflow: hidden;
}

.welcome-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

.welcome-title {
  margin: 0 0 var(--spacing-md) 0;
  font-size: var(--font-size-xxl);
  font-weight: 700;
  position: relative;
  z-index: 1;
}

.welcome-subtitle {
  margin: 0;
  font-size: var(--font-size-lg);
  opacity: 0.9;
  position: relative;
  z-index: 1;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  padding: var(--spacing-xl);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-lg) auto;
  background: var(--primary-50);
  border: 2px solid var(--primary-100);
}

.stat-content {
  margin-bottom: var(--spacing-md);
}

.stat-number {
  font-size: var(--font-size-xxl);
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: var(--spacing-sm);
}

.stat-title {
  font-size: var(--font-size-md);
  color: var(--text-secondary);
  font-weight: 500;
}

.stat-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.stat-trend.up {
  color: var(--success-color);
}

.stat-trend.down {
  color: var(--error-color);
}

/* 快捷操作 */
.quick-actions {
  margin-bottom: var(--spacing-xl);
}

.section-title {
  margin: 0 0 var(--spacing-lg) 0;
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  font-weight: 600;
  text-align: center;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.action-item {
  padding: var(--spacing-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.action-item:hover {
  transform: translateY(-2px);
}

.action-icon {
  flex-shrink: 0;
  transition: all var(--transition-normal);
}

.action-item:hover .action-icon {
  transform: scale(1.1);
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.action-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 系统信息 */
.system-info {
  margin-bottom: var(--spacing-xl);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--spacing-lg);
}

.info-section {
  padding: var(--spacing-lg);
}

.info-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--divider-color);
}

.status-list, .activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.status-item, .activity-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) 0;
}

.status-label {
  color: var(--text-secondary);
  min-width: 80px;
}

.status-value {
  color: var(--text-primary);
  font-weight: 500;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: var(--spacing-xs);
}

.status-indicator.online {
  background-color: var(--success-color);
}

.activity-time {
  color: var(--text-hint);
  font-size: var(--font-size-sm);
  min-width: 50px;
}

.activity-text {
  color: var(--text-primary);
}

/* 监控区域 */
.monitor-section {
  margin-bottom: var(--spacing-xl);
}

.monitor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--spacing-lg);
}

.monitor-item {
  padding: var(--spacing-lg);
}

.monitor-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--divider-color);
}

.performance-metrics, .network-status {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.metric-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.metric-label {
  color: var(--text-secondary);
  min-width: 100px;
  font-size: var(--font-size-sm);
}

.progress-bar {
  flex: 1;
  height: 8px;
  background-color: var(--divider-color);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--primary-light));
  border-radius: 4px;
  transition: width var(--transition-normal);
}

.metric-value {
  color: var(--text-primary);
  font-weight: 600;
  min-width: 40px;
  text-align: right;
}

.network-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) 0;
}

.network-label {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.network-status-indicator {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
  text-transform: uppercase;
}

.network-status-indicator.online {
  background-color: var(--success-color);
  color: white;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .dashboard {
    padding: var(--spacing-lg);
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }
  
  .actions-grid {
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: var(--spacing-md);
  }
  
  .welcome-section {
    padding: var(--spacing-lg);
  }
  
  .welcome-title {
    font-size: var(--font-size-xl);
  }
  
  .welcome-subtitle {
    font-size: var(--font-size-md);
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .monitor-grid {
    grid-template-columns: 1fr;
  }
  
  .section-title {
    font-size: var(--font-size-lg);
  }
}
</style>
