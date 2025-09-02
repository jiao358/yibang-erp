<template>
  <div class="sales-workspace">
    <div class="workspace-header">
      <h1>销售工作台</h1>
      <p>欢迎回来，销售</p>
    </div>

    <!-- 销售概览卡片 -->
    <div class="overview-section">
      <h2>销售概览</h2>
      <div class="overview-cards">
        <OverviewCard
          title="我的订单"
          :value="salesStats.myOrders"
          icon="Document"
          color="#409EFF"
        />
        <OverviewCard
          title="今日新增"
          :value="salesStats.todayOrders"
          icon="Plus"
          color="#67C23A"
        />
        <OverviewCard
          title="我的客户"
          :value="salesStats.myCustomers"
          icon="UserFilled"
          color="#E6A23C"
        />
        <OverviewCard
          title="可销售商品"
          :value="salesStats.availableProducts"
          icon="Box"
          color="#F56C6C"
        />
      </div>
    </div>

    <!-- 订单管理中心 -->
    <div class="order-section">
      <h2>订单管理中心</h2>
      <div class="order-cards">
        <div class="order-list-card">
          <h3>我的订单</h3>
          <RecentItemsList
            :items="myOrders"
            type="order"
            :loading="loading"
            :show-actions="true"
            action-text="查看"
            @action-click="handleOrderView"
          />
        </div>
        <div class="order-list-card">
          <h3>待处理订单</h3>
          <RecentItemsList
            :items="pendingOrders"
            type="order"
            :loading="loading"
            :show-actions="true"
            action-text="处理"
            @action-click="handleOrderProcess"
          />
        </div>
      </div>
    </div>

    <!-- 客户管理概览 -->
    <div class="customer-section">
      <h2>客户管理概览</h2>
      <div class="customer-cards">
        <OverviewCard
          title="我的客户"
          :value="salesStats.myCustomers"
          icon="UserFilled"
          color="#409EFF"
        />
        <OverviewCard
          title="活跃客户"
          :value="salesStats.activeCustomers"
          icon="User"
          color="#67C23A"
        />
        <OverviewCard
          title="新增客户"
          :value="salesStats.newCustomers"
          icon="Plus"
          color="#E6A23C"
        />
        <OverviewCard
          title="客户价值"
          :value="salesStats.customerValue"
          icon="Money"
          color="#F56C6C"
        />
      </div>
    </div>

    <!-- AI工具中心 -->
    <div class="ai-tools-section">
      <h2>AI工具中心</h2>
      <div class="ai-tools-cards">
        <OverviewCard
          title="AI导入任务"
          :value="salesStats.aiTasks"
          icon="Cpu"
          color="#409EFF"
        />
        <OverviewCard
          title="导入成功率"
          :value="salesStats.importSuccessRate"
          icon="Check"
          color="#67C23A"
        />
        <OverviewCard
          title="今日导入"
          :value="salesStats.todayImports"
          icon="Upload"
          color="#E6A23C"
        />
        <OverviewCard
          title="待处理任务"
          :value="salesStats.pendingTasks"
          icon="Clock"
          color="#F56C6C"
        />
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section">
      <h2>快捷操作</h2>
      <div class="quick-actions-grid">
        <QuickActionCard
          title="创建订单"
          description="手动创建新订单"
          icon="Plus"
          route="/order?action=create"
          color="#67C23A"
        />
        <QuickActionCard
          title="AI Excel导入"
          description="使用AI智能导入订单"
          icon="Upload"
          route="/ai-excel-import"
          color="#409EFF"
        />
        <QuickActionCard
          title="客户管理"
          description="管理客户信息"
          icon="UserFilled"
          route="/customer"
          color="#E6A23C"
        />
        <QuickActionCard
          title="商品筛选"
          description="筛选可销售商品"
          icon="Search"
          route="/product-filter"
          color="#F56C6C"
        />
        <QuickActionCard
          title="订单查询"
          description="查询订单状态"
          icon="Document"
          route="/order"
          color="#909399"
        />
        <QuickActionCard
          title="销售报表"
          description="查看销售数据"
          icon="TrendCharts"
          route="/order?tab=reports"
          color="#409EFF"
        />
      </div>
    </div>

    <!-- 最近数据 -->
    <div class="recent-data-section">
      <div class="recent-customers">
        <h3>最近客户</h3>
        <RecentItemsList
          :items="recentCustomers"
          type="customer"
          :loading="loading"
        />
      </div>
      <div class="recent-ai-tasks">
        <h3>AI导入历史</h3>
        <RecentItemsList
          :items="recentAITasks"
          type="ai-task"
          :loading="loading"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import OverviewCard from './common/OverviewCard.vue'
import QuickActionCard from './common/QuickActionCard.vue'
import RecentItemsList from './common/RecentItemsList.vue'
import { getSalesStats, getMyOrders, getPendingOrders, getRecentCustomers, getRecentAITasks } from '@/api/workspace'

const router = useRouter()

// 响应式数据
const salesStats = ref({
  myOrders: 0,
  todayOrders: 0,
  myCustomers: 0,
  availableProducts: 0,
  activeCustomers: 0,
  newCustomers: 0,
  customerValue: 0,
  aiTasks: 0,
  importSuccessRate: 0,
  todayImports: 0,
  pendingTasks: 0
})

const myOrders = ref([])
const pendingOrders = ref([])
const recentCustomers = ref([])
const recentAITasks = ref([])
const loading = ref(false)

// 处理订单查看
const handleOrderView = (order: any) => {
  router.push(`/order?id=${order.id}`)
}

// 处理订单处理
const handleOrderProcess = (order: any) => {
  router.push(`/order?action=process&id=${order.id}`)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 加载销售统计数据
    const statsResponse = await getSalesStats()
    if (statsResponse.success) {
      salesStats.value = statsResponse.data
    }

    // 加载我的订单
    const myOrdersResponse = await getMyOrders(5)
    if (myOrdersResponse.success) {
      myOrders.value = myOrdersResponse.data
    }

    // 加载待处理订单
    const pendingOrdersResponse = await getPendingOrders(5)
    if (pendingOrdersResponse.success) {
      pendingOrders.value = pendingOrdersResponse.data
    }

    // 加载最近客户
    const customersResponse = await getRecentCustomers(5)
    if (customersResponse.success) {
      recentCustomers.value = customersResponse.data
    }

    // 加载AI导入历史
    const aiTasksResponse = await getRecentAITasks(5)
    if (aiTasksResponse.success) {
      recentAITasks.value = aiTasksResponse.data
    }
  } catch (error) {
    console.error('加载销售工作台数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.sales-workspace {
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
.order-section,
.customer-section,
.ai-tools-section,
.quick-actions-section {
  margin-bottom: 40px;
}

.overview-section h2,
.order-section h2,
.customer-section h2,
.ai-tools-section h2,
.quick-actions-section h2 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  border-left: 4px solid #F56C6C;
  padding-left: 12px;
}

.overview-cards,
.customer-cards,
.ai-tools-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.order-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.order-list-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.order-list-card h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
  border-left: 3px solid #F56C6C;
  padding-left: 10px;
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
  border-left: 3px solid #F56C6C;
  padding-left: 10px;
}

@media (max-width: 768px) {
  .order-cards,
  .recent-data-section {
    grid-template-columns: 1fr;
  }
  
  .overview-cards,
  .customer-cards,
  .ai-tools-cards {
    grid-template-columns: 1fr;
  }
  
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
