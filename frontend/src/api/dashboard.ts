// 数字大屏API接口
// 目前使用模拟数据，后续可以替换为真实的后端接口

export interface ProvinceOrderData {
  province: string
  orderCount: number
  shippedCount: number
  pendingCount: number
  deliveredCount: number
  totalAmount: string
  avgDeliveryTime: number
}

export interface DashboardStats {
  orderStats: {
    todayOrders: number
    monthOrders: number
    weekTrend: number[]
  }
  warehouseStats: {
    totalInventory: number
    lowStockCount: number
    stockDistribution: Array<{ name: string; value: number; color: string }>
  }
  salesStats: {
    todaySales: string
    monthSales: string
    monthlyTrend: number[]
  }
  productStats: {
    totalProducts: number
    hotProducts: number
    categoryDistribution: Array<{ name: string; value: number; color: string }>
  }
}

export interface LogisticsMapData {
  [province: string]: ProvinceOrderData
}

// 模拟数据
const mockProvinceData: LogisticsMapData = {
  '北京': {
    province: '北京',
    orderCount: 89,
    shippedCount: 67,
    pendingCount: 12,
    deliveredCount: 45,
    totalAmount: '156,789',
    avgDeliveryTime: 24
  },
  '上海': {
    province: '上海',
    orderCount: 76,
    shippedCount: 58,
    pendingCount: 8,
    deliveredCount: 38,
    totalAmount: '134,567',
    avgDeliveryTime: 22
  },
  '广东': {
    province: '广东',
    orderCount: 98,
    shippedCount: 78,
    pendingCount: 15,
    deliveredCount: 52,
    totalAmount: '189,234',
    avgDeliveryTime: 26
  },
  '江苏': {
    province: '江苏',
    orderCount: 67,
    shippedCount: 52,
    pendingCount: 10,
    deliveredCount: 34,
    totalAmount: '98,765',
    avgDeliveryTime: 20
  },
  '浙江': {
    province: '浙江',
    orderCount: 72,
    shippedCount: 56,
    pendingCount: 11,
    deliveredCount: 39,
    totalAmount: '112,345',
    avgDeliveryTime: 23
  },
  '山东': {
    province: '山东',
    orderCount: 45,
    shippedCount: 34,
    pendingCount: 6,
    deliveredCount: 23,
    totalAmount: '67,890',
    avgDeliveryTime: 18
  },
  '河南': {
    province: '河南',
    orderCount: 38,
    shippedCount: 29,
    pendingCount: 5,
    deliveredCount: 19,
    totalAmount: '45,678',
    avgDeliveryTime: 16
  },
  '四川': {
    province: '四川',
    orderCount: 42,
    shippedCount: 32,
    pendingCount: 7,
    deliveredCount: 21,
    totalAmount: '56,789',
    avgDeliveryTime: 19
  },
  '湖北': {
    province: '湖北',
    orderCount: 35,
    shippedCount: 27,
    pendingCount: 4,
    deliveredCount: 18,
    totalAmount: '43,567',
    avgDeliveryTime: 17
  },
  '湖南': {
    province: '湖南',
    orderCount: 31,
    shippedCount: 24,
    pendingCount: 3,
    deliveredCount: 16,
    totalAmount: '38,901',
    avgDeliveryTime: 15
  },
  '福建': {
    province: '福建',
    orderCount: 28,
    shippedCount: 22,
    pendingCount: 3,
    deliveredCount: 15,
    totalAmount: '35,678',
    avgDeliveryTime: 14
  },
  '安徽': {
    province: '安徽',
    orderCount: 25,
    shippedCount: 19,
    pendingCount: 3,
    deliveredCount: 13,
    totalAmount: '32,456',
    avgDeliveryTime: 13
  },
  '河北': {
    province: '河北',
    orderCount: 33,
    shippedCount: 25,
    pendingCount: 4,
    deliveredCount: 17,
    totalAmount: '41,234',
    avgDeliveryTime: 16
  },
  '陕西': {
    province: '陕西',
    orderCount: 29,
    shippedCount: 22,
    pendingCount: 4,
    deliveredCount: 15,
    totalAmount: '37,890',
    avgDeliveryTime: 15
  },
  '江西': {
    province: '江西',
    orderCount: 22,
    shippedCount: 17,
    pendingCount: 3,
    deliveredCount: 12,
    totalAmount: '28,901',
    avgDeliveryTime: 12
  }
}

const mockDashboardStats: DashboardStats = {
  orderStats: {
    todayOrders: 156,
    monthOrders: 3247,
    weekTrend: [120, 132, 101, 134, 90, 230, 210]
  },
  warehouseStats: {
    totalInventory: 12580,
    lowStockCount: 23,
    stockDistribution: [
      { name: '正常库存', value: 1048, color: '#00ff00' },
      { name: '低库存', value: 235, color: '#ffaa00' },
      { name: '超储', value: 580, color: '#ff0000' }
    ]
  },
  salesStats: {
    todaySales: '¥89,456',
    monthSales: '¥2,156,789',
    monthlyTrend: [820, 932, 901, 934, 1290, 1330, 1450, 1580, 1620, 1750, 1890, 2100]
  },
  productStats: {
    totalProducts: 1247,
    hotProducts: 89,
    categoryDistribution: [
      { name: '热销', value: 335, color: '#ff6b6b' },
      { name: '正常', value: 310, color: '#4ecdc4' },
      { name: '滞销', value: 234, color: '#45b7d1' }
    ]
  }
}

// 模拟API接口
export const dashboardApi = {
  // 获取物流地图数据
  getLogisticsMapData(timeRange: string = 'today'): Promise<LogisticsMapData> {
    return new Promise((resolve) => {
      setTimeout(() => {
        // 根据时间范围调整数据
        const adjustedData = { ...mockProvinceData }
        if (timeRange === 'week') {
          Object.keys(adjustedData).forEach(province => {
            adjustedData[province] = {
              ...adjustedData[province],
              orderCount: Math.floor(adjustedData[province].orderCount * 0.7),
              shippedCount: Math.floor(adjustedData[province].shippedCount * 0.7),
              pendingCount: Math.floor(adjustedData[province].pendingCount * 0.7),
              deliveredCount: Math.floor(adjustedData[province].deliveredCount * 0.7)
            }
          })
        } else if (timeRange === 'month') {
          Object.keys(adjustedData).forEach(province => {
            adjustedData[province] = {
              ...adjustedData[province],
              orderCount: Math.floor(adjustedData[province].orderCount * 0.3),
              shippedCount: Math.floor(adjustedData[province].shippedCount * 0.3),
              pendingCount: Math.floor(adjustedData[province].pendingCount * 0.3),
              deliveredCount: Math.floor(adjustedData[province].deliveredCount * 0.3)
            }
          })
        }
        resolve(adjustedData)
      }, 300)
    })
  },

  // 获取仪表盘统计数据
  getDashboardStats(): Promise<DashboardStats> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(mockDashboardStats)
      }, 200)
    })
  },

  // 获取省份订单详情
  getProvinceOrderDetail(province: string): Promise<ProvinceOrderData | null> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(mockProvinceData[province] || null)
      }, 100)
    })
  }
}
