<template>
  <div class="digital-screen">
    <div class="screen-header">
      <h1>易邦ERP系统 - 数字大屏</h1>
      <div class="time-display">{{ currentTime }}</div>
    </div>
    
    <div class="screen-content">
      <!-- 顶部统计卡片 -->
      <div class="top-stats">
        <div class="stat-card" v-for="stat in topStats" :key="stat.key">
          <div class="stat-icon">
            <el-icon><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
      
      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 左侧图表 -->
        <div class="left-panel">
          <div class="chart-card">
            <h3>订单趋势</h3>
            <div class="chart-placeholder">
              <el-icon><TrendCharts /></el-icon>
              <p>订单趋势图表</p>
            </div>
          </div>
          
          <div class="chart-card">
            <h3>产品销售排行</h3>
            <div class="chart-placeholder">
              <el-icon><PieChart /></el-icon>
              <p>产品销售排行图表</p>
            </div>
          </div>
        </div>
        
        <!-- 中央地图 -->
        <div class="center-panel">
          <div class="map-card">
            <h3>全国业务分布</h3>
            <div class="map-placeholder">
              <el-icon><Location /></el-icon>
              <p>全国业务分布地图</p>
            </div>
          </div>
        </div>
        
        <!-- 右侧信息 -->
        <div class="right-panel">
          <div class="info-card">
            <h3>实时动态</h3>
            <div class="activity-list">
              <div class="activity-item" v-for="activity in realtimeActivities" :key="activity.id">
                <span class="activity-time">{{ activity.time }}</span>
                <span class="activity-text">{{ activity.text }}</span>
              </div>
            </div>
          </div>
          
          <div class="info-card">
            <h3>系统状态</h3>
            <div class="status-list">
              <div class="status-item" v-for="status in systemStatus" :key="status.key">
                <span class="status-label">{{ status.label }}</span>
                <span class="status-value" :class="status.status">{{ status.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 底部信息 -->
      <div class="bottom-info">
        <div class="info-item">
          <span class="label">数据更新时间：</span>
          <span class="value">{{ lastUpdateTime }}</span>
        </div>
        <div class="info-item">
          <span class="label">系统版本：</span>
          <span class="value">v1.0.0</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { TrendCharts, PieChart, Location, User, OfficeBuilding, Document, Goods } from '@element-plus/icons-vue'

// 当前时间
const currentTime = ref('')
const lastUpdateTime = ref('')

// 顶部统计数据
const topStats = ref([
  { key: 'users', label: '用户总数', value: '1,234', icon: User },
  { key: 'companies', label: '公司总数', value: '89', icon: OfficeBuilding },
  { key: 'orders', label: '订单总数', value: '5,678', icon: Document },
  { key: 'products', label: '产品总数', value: '456', icon: Goods }
])

// 实时动态
const realtimeActivities = ref([
  { id: 1, time: '10:30', text: '用户张三登录系统' },
  { id: 2, time: '10:25', text: '新订单 #12345 已创建' },
  { id: 3, time: '10:20', text: '产品信息已更新' },
  { id: 4, time: '10:15', text: '系统备份完成' },
  { id: 5, time: '10:10', text: '用户权限已修改' }
])

// 系统状态
const systemStatus = ref([
  { key: 'cpu', label: 'CPU使用率', value: '65%', status: 'normal' },
  { key: 'memory', label: '内存使用率', value: '78%', status: 'warning' },
  { key: 'disk', label: '磁盘使用率', value: '45%', status: 'normal' },
  { key: 'network', label: '网络状态', value: '正常', status: 'normal' }
])

// 时间更新定时器
let timeTimer: number

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
  
  lastUpdateTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载
onMounted(() => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
})

// 组件卸载
onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
  }
})
</script>

<style scoped>
.digital-screen {
  background: #000;
  color: #fff;
  min-height: 100vh;
  padding: 20px;
  font-family: 'Courier New', monospace;
}

.screen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(90deg, #1a1a1a 0%, #2d2d2d 100%);
  border-radius: 10px;
  border: 2px solid #333;
}

.screen-header h1 {
  margin: 0;
  font-size: 32px;
  font-weight: bold;
  background: linear-gradient(45deg, #00ffff, #ff00ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.time-display {
  font-size: 24px;
  color: #00ffff;
  font-weight: bold;
}

.screen-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.top-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  padding: 20px;
  border-radius: 10px;
  border: 2px solid #333;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  border-color: #00ffff;
  transform: translateY(-2px);
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.3);
}

.stat-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(45deg, #00ffff, #ff00ff);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #000;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #00ffff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #ccc;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  gap: 20px;
  min-height: 600px;
}

.left-panel, .center-panel, .right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-card, .map-card, .info-card {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  padding: 20px;
  border-radius: 10px;
  border: 2px solid #333;
  flex: 1;
}

.chart-card h3, .map-card h3, .info-card h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #00ffff;
  text-align: center;
  border-bottom: 1px solid #333;
  padding-bottom: 10px;
}

.chart-placeholder, .map-placeholder {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
  border: 2px dashed #333;
  border-radius: 8px;
}

.chart-placeholder .el-icon, .map-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
  color: #00ffff;
}

.map-placeholder {
  height: 400px;
}

.activity-list {
  max-height: 200px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #333;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-time {
  color: #00ffff;
  font-size: 12px;
  margin-right: 10px;
  min-width: 50px;
}

.activity-text {
  color: #ccc;
  font-size: 14px;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.status-label {
  color: #ccc;
  font-size: 14px;
}

.status-value {
  font-weight: bold;
  font-size: 14px;
}

.status-value.normal {
  color: #00ff00;
}

.status-value.warning {
  color: #ffff00;
}

.status-value.error {
  color: #ff0000;
}

.bottom-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(90deg, #1a1a1a 0%, #2d2d2d 100%);
  border-radius: 10px;
  border: 2px solid #333;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-item .label {
  color: #ccc;
  font-size: 14px;
}

.info-item .value {
  color: #00ffff;
  font-weight: bold;
  font-size: 14px;
}

@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .top-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .digital-screen {
    padding: 10px;
  }
  
  .screen-header h1 {
    font-size: 20px;
  }
  
  .time-display {
    font-size: 16px;
  }
  
  .top-stats {
    grid-template-columns: 1fr;
  }
  
  .bottom-info {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
