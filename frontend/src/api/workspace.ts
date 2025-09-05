import request from '@/utils/request'

// 工作台API接口
export const workspaceApi = {
  // 获取系统管理员统计数据
  getSystemStats() {
    return request.get('/api/workspace/system-stats')
  },

  // 获取供应链管理员统计数据
  getSupplierStats() {
    return request.get('/api/workspace/supplier-stats')
  },

  // 获取销售统计数据
  getSalesStats() {
    return request.get('/api/workspace/sales-stats')
  },

  // 获取基础统计数据
  getBasicStats() {
    return request.get('/api/workspace/basic-stats')
  },

  // 获取最近订单
  getRecentOrders(limit: number = 5) {
    return request.get('/api/workspace/recent-orders', {
      params: { limit }
    })
  },


  // 获取最近商品
  getRecentProducts(limit: number = 5) {
    return request.get('/api/workspace/recent-products', {
      params: { limit }
    })
  },

  // 获取最近客户
  getRecentCustomers(limit: number = 5) {
    return request.get('/api/workspace/recent-customers', {
      params: { limit }
    })
  },

  // 获取待审核订单
  getPendingOrders(limit: number = 5) {
    return request.get('/api/workspace/pending-orders', {
      params: { limit }
    })
  },

  // 获取待发货订单
  getPendingShipments(limit: number = 5) {
    return request.get('/api/workspace/pending-shipments', {
      params: { limit }
    })
  },

  // 获取我的订单
  getMyOrders(limit: number = 5) {
    return request.get('/api/workspace/my-orders', {
      params: { limit }
    })
  },

  // 获取库存预警商品
  getLowStockItems(limit: number = 5) {
    return request.get('/api/workspace/low-stock-items', {
      params: { limit }
    })
  },

  // 获取AI导入历史
  getRecentAITasks(limit: number = 5) {
    return request.get('/api/workspace/recent-ai-tasks', {
      params: { limit }
    })
  }
}

// 导出常用方法
export const {
  getSystemStats,
  getSupplierStats,
  getSalesStats,
  getBasicStats,
  getRecentOrders,
  getRecentProducts,
  getRecentCustomers,
  getPendingOrders,
  getPendingShipments,
  getMyOrders,
  getLowStockItems,
  getRecentAITasks
} = workspaceApi
