<template>
  <div class="system-admin-workspace">
    <div class="workspace-header">
      <h1>系统管理员工作台</h1>
      <p>欢迎回来，系统管理员</p>
    </div>

    <!-- 系统概览卡片 -->
    <div class="overview-section">
      <h2>系统概览</h2>
      <div class="overview-cards">
        <OverviewCard
          title="用户总数"
          :value="systemStats.totalUsers"
          icon="User"
          color="#409EFF"
        />
        <OverviewCard
          title="角色总数"
          :value="systemStats.totalRoles"
          icon="Lock"
          color="#67C23A"
        />
        <OverviewCard
          title="公司总数"
          :value="systemStats.totalCompanies"
          icon="OfficeBuilding"
          color="#E6A23C"
        />
        <OverviewCard
          title="订单总数"
          :value="systemStats.totalOrders"
          icon="Document"
          color="#F56C6C"
        />
      </div>
    </div>

    <!-- 业务数据概览 -->
    <div class="business-section">
      <h2>业务数据概览</h2>
      <div class="business-cards">
        <OverviewCard
          title="商品总数"
          :value="systemStats.totalProducts"
          icon="Box"
          color="#909399"
        />
        <OverviewCard
          title="客户总数"
          :value="systemStats.totalCustomers"
          icon="UserFilled"
          color="#409EFF"
        />
        <OverviewCard
          title="库存商品数"
          :value="systemStats.totalInventory"
          icon="Box"
          color="#67C23A"
        />
        <OverviewCard
          title="AI处理任务"
          :value="systemStats.totalAITasks"
          icon="Cpu"
          color="#E6A23C"
        />
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section">
      <h2>快捷操作</h2>
      <div class="quick-actions-grid">
        <QuickActionCard
          title="用户管理"
          description="管理系统用户和权限"
          icon="User"
          route="/user"
          color="#409EFF"
        />
        <QuickActionCard
          title="角色管理"
          description="配置用户角色和权限"
          icon="Lock"
          route="/role"
          color="#67C23A"
        />
        <QuickActionCard
          title="公司管理"
          description="管理供应商和销售商"
          icon="OfficeBuilding"
          route="/company"
          color="#E6A23C"
        />
        <QuickActionCard
          title="AI管理"
          description="AI模型配置和监控"
          icon="Cpu"
          route="/ai-management"
          color="#F56C6C"
        />
        <QuickActionCard
          title="系统监控"
          description="系统运行状态监控"
          icon="TrendCharts"
          route="/monitor"
          color="#909399"
        />
        <QuickActionCard
          title="数字大屏"
          description="查看系统数据大屏"
          icon="DataBoard"
          route="/digital-screen"
          color="#409EFF"
        />
      </div>
    </div>

    <!-- 最近数据 -->
    <div class="recent-data-section">
      <div class="recent-orders">
        <h3>最近订单</h3>
        <RecentItemsList
          :items="recentOrders"
          type="order"
          :loading="loading"
        />
      </div>
      <div class="recent-users">
        <h3>最近用户</h3>
        <RecentItemsList
          :items="recentUsers"
          type="user"
          :loading="loading"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import OverviewCard from './common/OverviewCard.vue'
import QuickActionCard from './common/QuickActionCard.vue'
import RecentItemsList from './common/RecentItemsList.vue'
import { getSystemStats, getRecentOrders, getRecentUsers } from '@/api/workspace'

// 响应式数据
const systemStats = ref({
  totalUsers: 0,
  totalRoles: 0,
  totalCompanies: 0,
  totalOrders: 0,
  totalProducts: 0,
  totalCustomers: 0,
  totalInventory: 0,
  totalAITasks: 0
})

const recentOrders = ref([])
const recentUsers = ref([])
const loading = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 加载系统统计数据
    const statsResponse = await getSystemStats()
    if (statsResponse.success) {
      systemStats.value = statsResponse.data
    }

    // 加载最近订单
    const ordersResponse = await getRecentOrders(5)
    if (ordersResponse.success) {
      recentOrders.value = ordersResponse.data
    }

    // 加载最近用户
    const usersResponse = await getRecentUsers(5)
    if (usersResponse.success) {
      recentUsers.value = usersResponse.data
    }
  } catch (error) {
    console.error('加载工作台数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.system-admin-workspace {
  max-width: 1200px;
  margin: 0 auto;
}

.workspace-header {
  margin-bottom: 30px;
  text-align: center;
}

.workspace-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}

.workspace-header p {
  font-size: 16px;
  color: #606266;
}

.overview-section,
.business-section,
.quick-actions-section {
  margin-bottom: 40px;
}

.overview-section h2,
.business-section h2,
.quick-actions-section h2 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

.overview-cards,
.business-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.recent-data-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.recent-data-section h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
  border-left: 3px solid #67C23A;
  padding-left: 10px;
}

@media (max-width: 768px) {
  .recent-data-section {
    grid-template-columns: 1fr;
  }
  
  .overview-cards,
  .business-cards {
    grid-template-columns: 1fr;
  }
  
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
