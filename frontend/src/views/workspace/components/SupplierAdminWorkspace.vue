<template>
  <div class="supplier-admin-workspace">
    <div class="workspace-header">
      <h1>供应链管理员工作台</h1>
      <p>欢迎回来，供应链管理员</p>
    </div>

    <!-- 业务概览卡片 -->
    <div class="overview-section">
      <h2>业务概览</h2>
      <div class="overview-cards">
        <OverviewCard
          title="待审核订单"
          :value="supplierStats.pendingOrders"
          icon="Document"
          color="#E6A23C"
        />
        <OverviewCard
          title="待发货订单"
          :value="supplierStats.pendingShipments"
          icon="Truck"
          color="#F56C6C"
        />
        <OverviewCard
          title="商品总数"
          :value="supplierStats.totalProducts"
          icon="Box"
          color="#409EFF"
        />
        <OverviewCard
          title="库存预警"
          :value="supplierStats.lowStockItems"
          icon="Warning"
          color="#F56C6C"
        />
      </div>
    </div>

    <!-- 订单处理中心 -->
    <div class="order-section">
      <h2>订单处理中心</h2>
      <div class="order-cards">
        <div class="order-list-card">
          <h3>待审核订单</h3>
          <RecentItemsList
            :items="pendingOrders"
            type="order"
            :loading="loading"
            :show-actions="true"
            action-text="审核"
            @action-click="handleOrderAction"
          />
        </div>
        <div class="order-list-card">
          <h3>待发货订单</h3>
          <RecentItemsList
            :items="pendingShipments"
            type="order"
            :loading="loading"
            :show-actions="true"
            action-text="发货"
            @action-click="handleShipmentAction"
          />
        </div>
      </div>
    </div>

    <!-- 商品管理概览 -->
    <div class="product-section">
      <h2>商品管理概览</h2>
      <div class="product-cards">
        <OverviewCard
          title="商品总数"
          :value="supplierStats.totalProducts"
          icon="Box"
          color="#409EFF"
        />
        <OverviewCard
          title="待审核商品"
          :value="supplierStats.pendingProducts"
          icon="Edit"
          color="#E6A23C"
        />
        <OverviewCard
          title="已上架商品"
          :value="supplierStats.activeProducts"
          icon="Check"
          color="#67C23A"
        />
        <OverviewCard
          title="库存预警"
          :value="supplierStats.lowStockItems"
          icon="Warning"
          color="#F56C6C"
        />
      </div>
    </div>

    <!-- 库存管理概览 -->
    <div class="inventory-section">
      <h2>库存管理概览</h2>
      <div class="inventory-cards">
        <OverviewCard
          title="库存商品数"
          :value="supplierStats.totalInventory"
          icon="Box"
          color="#67C23A"
        />
        <OverviewCard
          title="仓库数量"
          :value="supplierStats.totalWarehouses"
          icon="House"
          color="#409EFF"
        />
        <OverviewCard
          title="库存盘点"
          :value="supplierStats.inventoryChecks"
          icon="List"
          color="#E6A23C"
        />
        <OverviewCard
          title="库存价值"
          :value="supplierStats.inventoryValue"
          icon="Money"
          color="#67C23A"
        />
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section">
      <h2>快捷操作</h2>
      <div class="quick-actions-grid">
        <QuickActionCard
          title="商品管理"
          description="管理商品信息和库存"
          icon="Box"
          route="/supplier-product"
          color="#409EFF"
        />
        <QuickActionCard
          title="库存管理"
          description="查看和管理库存"
          icon="Box"
          route="/inventory"
          color="#67C23A"
        />
        <QuickActionCard
          title="订单审核"
          description="审核待处理订单"
          icon="Document"
          route="/order"
          color="#E6A23C"
        />
        <QuickActionCard
          title="仓库管理"
          description="管理仓库信息"
          icon="House"
          route="/warehouse"
          color="#909399"
        />
        <QuickActionCard
          title="价格策略"
          description="管理价格策略"
          icon="TrendCharts"
          route="/pricing"
          color="#F56C6C"
        />
        <QuickActionCard
          title="库存盘点"
          description="进行库存盘点"
          icon="List"
          route="/inventory-check"
          color="#409EFF"
        />
      </div>
    </div>

    <!-- 最近数据 -->
    <div class="recent-data-section">
      <div class="recent-products">
        <h3>最近商品</h3>
        <RecentItemsList
          :items="recentProducts"
          type="product"
          :loading="loading"
        />
      </div>
      <div class="recent-inventory">
        <h3>库存预警</h3>
        <RecentItemsList
          :items="lowStockItems"
          type="inventory"
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
import { getSupplierStats, getPendingOrders, getPendingShipments, getRecentProducts, getLowStockItems } from '@/api/workspace'

const router = useRouter()

// 响应式数据
const supplierStats = ref({
  pendingOrders: 0,
  pendingShipments: 0,
  totalProducts: 0,
  lowStockItems: 0,
  pendingProducts: 0,
  activeProducts: 0,
  totalInventory: 0,
  totalWarehouses: 0,
  inventoryChecks: 0,
  inventoryValue: 0
})

const pendingOrders = ref([])
const pendingShipments = ref([])
const recentProducts = ref([])
const lowStockItems = ref([])
const loading = ref(false)

// 处理订单操作
const handleOrderAction = (order: any) => {
  router.push(`/order?action=review&id=${order.id}`)
}

// 处理发货操作
const handleShipmentAction = (order: any) => {
  router.push(`/order?action=ship&id=${order.id}`)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 加载供应链统计数据
    const statsResponse = await getSupplierStats()
    if (statsResponse.success) {
      supplierStats.value = statsResponse.data
    }

    // 加载待审核订单
    const pendingOrdersResponse = await getPendingOrders(5)
    if (pendingOrdersResponse.success) {
      pendingOrders.value = pendingOrdersResponse.data
    }

    // 加载待发货订单
    const pendingShipmentsResponse = await getPendingShipments(5)
    if (pendingShipmentsResponse.success) {
      pendingShipments.value = pendingShipmentsResponse.data
    }

    // 加载最近商品
    const productsResponse = await getRecentProducts(5)
    if (productsResponse.success) {
      recentProducts.value = productsResponse.data
    }

    // 加载库存预警
    const lowStockResponse = await getLowStockItems(5)
    if (lowStockResponse.success) {
      lowStockItems.value = lowStockResponse.data
    }
  } catch (error) {
    console.error('加载供应链工作台数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.supplier-admin-workspace {
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
.product-section,
.inventory-section,
.quick-actions-section {
  margin-bottom: 40px;
}

.overview-section h2,
.order-section h2,
.product-section h2,
.inventory-section h2,
.quick-actions-section h2 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  border-left: 4px solid #67C23A;
  padding-left: 12px;
}

.overview-cards,
.product-cards,
.inventory-cards {
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
  border-left: 3px solid #E6A23C;
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
  border-left: 3px solid #67C23A;
  padding-left: 10px;
}

@media (max-width: 768px) {
  .order-cards,
  .recent-data-section {
    grid-template-columns: 1fr;
  }
  
  .overview-cards,
  .product-cards,
  .inventory-cards {
    grid-template-columns: 1fr;
  }
  
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
