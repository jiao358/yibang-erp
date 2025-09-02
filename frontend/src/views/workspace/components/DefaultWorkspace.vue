<template>
  <div class="default-workspace">
    <div class="workspace-header">
      <h1>工作台</h1>
      <p>欢迎使用懿邦ERP系统</p>
    </div>

    <!-- 基础概览 -->
    <div class="overview-section">
      <h2>系统概览</h2>
      <div class="overview-cards">
        <OverviewCard
          title="订单总数"
          :value="basicStats.totalOrders"
          icon="Document"
          color="#409EFF"
        />
        <OverviewCard
          title="商品总数"
          :value="basicStats.totalProducts"
          icon="Box"
          color="#67C23A"
        />
        <OverviewCard
          title="客户总数"
          :value="basicStats.totalCustomers"
          icon="UserFilled"
          color="#E6A23C"
        />
        <OverviewCard
          title="库存商品"
          :value="basicStats.totalInventory"
          icon="Box"
          color="#F56C6C"
        />
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section">
      <h2>快捷操作</h2>
      <div class="quick-actions-grid">
        <QuickActionCard
          title="订单管理"
          description="查看和管理订单"
          icon="Document"
          route="/order"
          color="#409EFF"
        />
        <QuickActionCard
          title="商品筛选"
          description="筛选和查看商品"
          icon="Search"
          route="/product-filter"
          color="#67C23A"
        />
        <QuickActionCard
          title="客户管理"
          description="管理客户信息"
          icon="UserFilled"
          route="/customer"
          color="#E6A23C"
        />
        <QuickActionCard
          title="AI Excel导入"
          description="使用AI导入订单"
          icon="Upload"
          route="/ai-excel-import"
          color="#F56C6C"
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
      <div class="recent-customers">
        <h3>最近客户</h3>
        <RecentItemsList
          :items="recentCustomers"
          type="customer"
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
import { getBasicStats, getRecentOrders, getRecentCustomers } from '@/api/workspace'

// 响应式数据
const basicStats = ref({
  totalOrders: 0,
  totalProducts: 0,
  totalCustomers: 0,
  totalInventory: 0
})

const recentOrders = ref([])
const recentCustomers = ref([])
const loading = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 加载基础统计数据
    const statsResponse = await getBasicStats()
    if (statsResponse.success) {
      basicStats.value = statsResponse.data
    }

    // 加载最近订单
    const ordersResponse = await getRecentOrders(5)
    if (ordersResponse.success) {
      recentOrders.value = ordersResponse.data
    }

    // 加载最近客户
    const customersResponse = await getRecentCustomers(5)
    if (customersResponse.success) {
      recentCustomers.value = customersResponse.data
    }
  } catch (error) {
    console.error('加载默认工作台数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.default-workspace {
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
.quick-actions-section {
  margin-bottom: 40px;
}

.overview-section h2,
.quick-actions-section h2 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  border-left: 4px solid #909399;
  padding-left: 12px;
}

.overview-cards {
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
  border-left: 3px solid #909399;
  padding-left: 10px;
}

@media (max-width: 768px) {
  .recent-data-section {
    grid-template-columns: 1fr;
  }
  
  .overview-cards {
    grid-template-columns: 1fr;
  }
  
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
