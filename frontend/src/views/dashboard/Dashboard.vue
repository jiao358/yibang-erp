<template>
  <div class="dashboard">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <h1>欢迎回来，{{ userInfo.realName || userInfo.username }}！</h1>
      <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon user-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.userCount }}</div>
                <div class="stat-label">用户总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon company-icon">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.companyCount }}</div>
                <div class="stat-label">公司总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon order-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.orderCount }}</div>
                <div class="stat-label">订单总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon product-icon">
                <el-icon><Goods /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.productCount }}</div>
                <div class="stat-label">产品总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section">
      <h2>快捷操作</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-if="hasPermission(['ADMIN'])">
          <el-card class="action-card" @click="navigateTo('/user')">
            <div class="action-content">
              <el-icon class="action-icon"><User /></el-icon>
              <div class="action-title">用户管理</div>
              <div class="action-desc">管理系统用户和权限</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6" v-if="hasPermission(['ADMIN'])">
          <el-card class="action-card" @click="navigateTo('/role')">
            <div class="action-content">
              <el-icon class="action-icon"><Setting /></el-icon>
              <div class="action-title">角色管理</div>
              <div class="action-desc">配置用户角色和权限</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6" v-if="hasPermission(['ADMIN'])">
          <el-card class="action-card" @click="navigateTo('/company')">
            <div class="action-content">
              <el-icon class="action-icon"><OfficeBuilding /></el-icon>
              <div class="action-title">公司管理</div>
              <div class="action-desc">管理供应商和销售商</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6" v-if="hasPermission(['ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN'])">
          <el-card class="action-card" @click="navigateTo('/product')">
            <div class="action-content">
              <el-icon class="action-icon"><Goods /></el-icon>
              <div class="action-title">产品管理</div>
              <div class="action-desc">管理产品信息和库存</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6" v-if="hasPermission(['ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN', 'SUPPLIER_OPERATOR', 'SALES'])">
          <el-card class="action-card" @click="navigateTo('/order')">
            <div class="action-content">
              <el-icon class="action-icon"><Document /></el-icon>
              <div class="action-title">订单管理</div>
              <div class="action-desc">处理订单和发货</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6" v-if="hasPermission(['ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN', 'SUPPLIER_OPERATOR', 'SALES'])">
          <el-card class="action-card" @click="navigateTo('/digital-screen')">
            <div class="action-content">
              <el-icon class="action-icon"><Monitor /></el-icon>
              <div class="action-title">数字大屏</div>
              <div class="action-desc">查看实时数据大屏</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 系统信息 -->
    <div class="system-info-section">
      <h2>系统信息</h2>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>系统状态</span>
            </template>
            <div class="system-status">
              <div class="status-item">
                <span class="status-label">系统版本：</span>
                <span class="status-value">v1.0.0</span>
              </div>
              <div class="status-item">
                <span class="status-label">运行时间：</span>
                <span class="status-value">{{ systemInfo.uptime }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">最后更新：</span>
                <span class="status-value">{{ systemInfo.lastUpdate }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>最近活动</span>
            </template>
            <div class="recent-activities">
              <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
                <span class="activity-time">{{ activity.time }}</span>
                <span class="activity-text">{{ activity.text }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, OfficeBuilding, Document, Goods, Setting, Monitor } from '@element-plus/icons-vue'

// 路由
const router = useRouter()

// 响应式数据
const userInfo = ref({
  username: 'admin',
  realName: '系统管理员'
})

const stats = ref({
  userCount: 0,
  companyCount: 0,
  orderCount: 0,
  productCount: 0
})

const systemInfo = ref({
  uptime: '0天 0小时 0分钟',
  lastUpdate: '2024-01-14 10:00:00'
})

const recentActivities = ref([
  { id: 1, time: '10:30', text: '用户 admin 登录系统' },
  { id: 2, time: '10:25', text: '新增角色：销售经理' },
  { id: 3, time: '10:20', text: '更新公司信息：ABC公司' },
  { id: 4, time: '10:15', text: '系统启动完成' }
])

// 计算属性
const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
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
    stats.value = {
      userCount: 25,
      companyCount: 8,
      orderCount: 156,
      productCount: 89
    }
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

// 组件挂载
onMounted(() => {
  fetchUserInfo()
  fetchStats()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 30px;
  text-align: center;
}

.welcome-section h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: bold;
}

.welcome-section p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.stats-section {
  margin-bottom: 30px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.user-icon { background: #409eff; }
.company-icon { background: #67c23a; }
.order-icon { background: #e6a23c; }
.product-icon { background: #f56c6c; }

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.quick-actions-section {
  margin-bottom: 30px;
}

.quick-actions-section h2 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 20px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  padding: 20px;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.action-icon {
  font-size: 32px;
  color: #409eff;
  margin-bottom: 15px;
}

.action-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.action-desc {
  font-size: 12px;
  color: #909399;
}

.system-info-section h2 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 20px;
}

.system-status .status-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.status-label {
  color: #606266;
  font-weight: 500;
}

.status-value {
  color: #303133;
}

.recent-activities .activity-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-time {
  color: #909399;
  font-size: 12px;
  min-width: 60px;
}

.activity-text {
  color: #606266;
  flex: 1;
  margin-left: 15px;
}
</style>
