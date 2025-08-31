<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <h1>欢迎回来，{{ userInfo.username }}！</h1>
      <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
          <div class="stat-trend positive">
            <el-icon><ArrowUp /></el-icon>
            +12%
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.companyCount }}</div>
          <div class="stat-label">公司总数</div>
          <div class="stat-trend positive">
            <el-icon><ArrowUp /></el-icon>
            +5%
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.orderCount }}</div>
          <div class="stat-label">订单总数</div>
          <div class="stat-trend positive">
            <el-icon><ArrowUp /></el-icon>
            +23%
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Goods /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.productCount }}</div>
          <div class="stat-label">产品总数</div>
          <div class="stat-trend positive">
            <el-icon><ArrowUp /></el-icon>
            +8%
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="actions-grid">
        <div class="action-card" @click="navigateTo('/user')">
          <div class="action-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="action-content">
            <h3>用户管理</h3>
            <p>管理系统用户和权限</p>
          </div>
        </div>

        <div class="action-card" @click="navigateTo('/role')">
          <div class="action-icon">
            <el-icon><Key /></el-icon>
          </div>
          <div class="action-content">
            <h3>角色管理</h3>
            <p>配置用户角色和权限</p>
          </div>
        </div>

        <div class="action-card" @click="navigateTo('/company')">
          <div class="action-icon">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="action-content">
            <h3>公司管理</h3>
            <p>管理供应商和销售商</p>
          </div>
        </div>

        <div class="action-card" @click="navigateTo('/supplier-product')">
          <div class="action-icon">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="action-content">
            <h3>产品管理</h3>
            <p>管理产品信息和库存</p>
          </div>
        </div>

        <div class="action-card" @click="navigateTo('/order')">
          <div class="action-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="action-content">
            <h3>订单管理</h3>
            <p>处理订单和发货</p>
          </div>
        </div>

        <div class="action-card" @click="navigateTo('/pricing')">
          <div class="action-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="action-content">
            <h3>价格管理</h3>
            <p>管理价格策略和分层</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 系统信息 -->
    <div class="system-info">
      <h2>系统信息</h2>
      <div class="info-grid">
        <div class="info-card">
          <h3>系统状态</h3>
          <div class="info-item">
            <span class="label">系统版本：</span>
            <span class="value">v1.0.0</span>
          </div>
          <div class="info-item">
            <span class="label">运行时间：</span>
            <span class="value">{{ systemInfo.uptime }}</span>
          </div>
          <div class="info-item">
            <span class="label">最后更新：</span>
            <span class="value">{{ systemInfo.lastUpdate }}</span>
          </div>
          <div class="info-item">
            <span class="label">系统状态：</span>
            <span class="value status-normal">{{ systemInfo.status }}</span>
          </div>
        </div>

        <div class="info-card">
          <h3>最近活动</h3>
          <div class="activity-list">
            <div class="activity-item" v-for="activity in recentActivities" :key="activity.id">
              <span class="time">{{ activity.time }}</span>
              <span class="description">{{ activity.description }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 实时监控 -->
    <div class="real-time-monitor">
      <h2>实时监控</h2>
      <div class="monitor-grid">
        <div class="monitor-card">
          <h3>系统性能</h3>
          <div class="performance-item">
            <span class="label">CPU使用率</span>
            <div class="progress-bar">
              <div class="progress" :style="{ width: systemPerformance.cpu + '%' }"></div>
            </div>
            <span class="value">{{ systemPerformance.cpu }}%</span>
          </div>
          <div class="performance-item">
            <span class="label">内存使用率</span>
            <div class="progress-bar">
              <div class="progress" :style="{ width: systemPerformance.memory + '%' }"></div>
            </div>
            <span class="value">{{ systemPerformance.memory }}%</span>
          </div>
          <div class="performance-item">
            <span class="label">磁盘使用率</span>
            <div class="progress-bar">
              <div class="progress" :style="{ width: systemPerformance.disk + '%' }"></div>
            </div>
            <span class="value">{{ systemPerformance.disk }}%</span>
          </div>
        </div>

        <div class="monitor-card">
          <h3>网络状态</h3>
          <div class="network-item">
            <span class="label">内网状态</span>
            <span class="value status-normal">{{ networkStatus.internal }}</span>
          </div>
          <div class="network-item">
            <span class="label">外网状态</span>
            <span class="value status-normal">{{ networkStatus.external }}</span>
          </div>
          <div class="network-item">
            <span class="label">数据库连接</span>
            <span class="value status-normal">{{ networkStatus.database }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, OfficeBuilding, Document, Goods, Key, ArrowUp, TrendCharts } from '@element-plus/icons-vue'

const router = useRouter()

// 用户信息
const userInfo = ref({
  username: '系统管理员'
})

// 当前日期
const currentDate = ref('')

// 统计数据
const stats = ref({
  userCount: 1234,
  companyCount: 89,
  orderCount: 5678,
  productCount: 456
})

// 系统信息
const systemInfo = ref({
  uptime: '594天 14267小时 856046分钟',
  lastUpdate: '2024/1/14 10:00',
  status: '运行正常'
})

// 最近活动
const recentActivities = ref([
  { id: 1, time: '10:30', description: '用户张三登录系统' },
  { id: 2, time: '10:25', description: '新订单 #12345 已创建' },
  { id: 3, time: '10:20', description: '产品信息已更新' },
  { id: 4, time: '10:15', description: '系统备份完成' },
  { id: 5, time: '10:10', description: '用户权限已修改' }
])

// 系统性能
const systemPerformance = ref({
  cpu: 65,
  memory: 78,
  disk: 45
})

// 网络状态
const networkStatus = ref({
  internal: '正常',
  external: '正常',
  database: '正常'
})

// 导航方法
const navigateTo = (path: string) => {
  router.push(path)
}

// 初始化
onMounted(() => {
  const now = new Date()
  const options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }
  currentDate.value = now.toLocaleDateString('zh-CN', options)
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 32px;
  border-radius: 12px;
  margin-bottom: 24px;
  text-align: center;
}

.welcome-section h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-section p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
}

.stat-trend.positive {
  color: #67c23a;
}

.stat-trend.negative {
  color: #f56c6c;
}

.quick-actions {
  margin-bottom: 32px;
}

.quick-actions h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.action-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 2px solid transparent;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.action-icon {
  background: linear-gradient(135deg, #409eff 0%, #36cfc9 100%);
  color: white;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  font-size: 20px;
}

.action-content h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.action-content p {
  margin: 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

.system-info {
  margin-bottom: 32px;
}

.system-info h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.info-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.info-card h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
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

.status-normal {
  color: #67c23a;
}

.activity-list {
  max-height: 200px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-item .time {
  color: #909399;
  font-size: 12px;
  margin-right: 12px;
  min-width: 40px;
}

.activity-item .description {
  color: #606266;
  font-size: 14px;
}

.real-time-monitor {
  margin-bottom: 32px;
}

.real-time-monitor h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.monitor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.monitor-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.monitor-card h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 12px;
}

.performance-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.performance-item .label {
  color: #606266;
  font-size: 14px;
  min-width: 80px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  margin: 0 16px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, #67c23a 0%, #409eff 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.performance-item .value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
  min-width: 40px;
  text-align: right;
}

.network-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.network-item .label {
  color: #606266;
  font-size: 14px;
}

.network-item .value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
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
}
</style>

