import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/workspace'
      },
      {
        path: 'workspace',
        name: 'Workspace',
        component: () => import('@/views/workspace/Workspace.vue'),
        meta: {
          title: '工作台',
          requiresAuth: true
        }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/user/UserList.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN']
        }
      },
      {
        path: 'role',
        name: 'RoleManagement',
        component: () => import('@/views/role/RoleList.vue'),
        meta: {
          title: '角色管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN']
        }
      },
      {
        path: 'company',
        name: 'CompanyManagement',
        component: () => import('@/views/company/CompanyList.vue'),
        meta: {
          title: '公司管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN']
        }
      },
      {
        path: 'supplier-product',
        name: 'SupplierProductManagement',
        component: () => import('@/views/product/ProductList.vue'),
        meta: {
          title: '供应链商品管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN']
        }
      },
      {
        path: 'product-filter',
        name: 'ProductFilter',
        component: () => import('@/views/product/ProductFilter.vue'),
        meta: {
          title: '商品筛选',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SALES_ADMIN', 'SALES']
        }
      },
      {
        path: 'order',
        name: 'OrderManagement',
        component: () => import('@/views/order/OrderList.vue'),
        meta: {
          title: '订单管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN', 'SUPPLIER_OPERATOR', 'SALES']
        }
      },
      {
        path: 'order-alert',
        name: 'OrderAlertManagement',
        component: () => import('@/views/order/OrderAlertList.vue'),
        meta: {
          title: '订单预警',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN', 'SUPPLIER_OPERATOR', 'SALES']
        }
      },
      {
        path: 'customer',
        name: 'CustomerManagement',
        component: () => import('@/views/customer/CustomerList.vue'),
        meta: {
          title: '客户管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN', 'SALES']
        }
      },
      {
        path: 'digital-screen',
        name: 'DigitalScreen',
        component: () => import('@/views/dashboard/DigitalScreen.vue'),
        meta: {
          title: '数字大屏',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN']
        }
      },
      {
        path: 'ai-management',
        name: 'AIManagement',
        component: () => import('@/views/ai/AIManagement.vue'),
        meta: {
          title: 'AI管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN']
        }
      },
      {
        path: 'ai-excel-import',
        name: 'AIExcelImport',
        component: () => import('@/views/ai/AIExcelImport.vue'),
        meta: {
          title: 'AI Excel订单导入',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SALES_ADMIN', 'SALES']
        }
      },
      {
        path: 'pricing',
        name: 'PricingManagement',
        component: () => import('@/views/pricing/PriceTierList.vue'),
        meta: {
          title: '价格分层管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN']
        }
      },
      {
        path: 'price-strategy',
        name: 'PriceStrategyManagement',
        component: () => import('@/views/pricing/PriceStrategyList.vue'),
        meta: {
          title: '价格策略管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES']
        }
      },
      {
        path: 'sales-target',
        name: 'SalesTargetManagement',
        component: () => import('@/views/pricing/SalesTargetList.vue'),
        meta: {
          title: '销售目标管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES']
        }
      },
      // 库存管理相关路由
      {
        path: 'warehouse',
        name: 'WarehouseManagement',
        component: () => import('@/views/inventory/WarehouseList.vue'),
        meta: {
          title: '仓库管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN']
        }
      },
      {
        path: 'inventory',
        name: 'InventoryManagement',
        component: () => import('@/views/inventory/InventoryList.vue'),
        meta: {
          title: '库存管理',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN']
        }
      },
      {
        path: 'inventory-alert',
        name: 'InventoryAlertManagement',
        component: () => import('@/views/inventory/InventoryAlertList.vue'),
        meta: {
          title: '库存预警',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN']
        }
      },
      {
        path: 'inventory-check',
        name: 'InventoryCheckManagement',
        component: () => import('@/views/inventory/InventoryCheckList.vue'),
        meta: {
          title: '库存盘点',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN', 'SUPPLIER_ADMIN']
        }
      },
      {
        path: 'system-monitor',
        name: 'SystemMonitor',
        component: () => import('@/views/monitor/SystemMonitor.vue'),
        meta: {
          title: '系统监控',
          requiresAuth: true,
          roles: ['SYSTEM_ADMIN']
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫 - 恢复真实的登录验证
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 懿邦ERP系统` : '懿邦ERP系统'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
      return
    }
    
    // 检查角色权限
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]')
    const requiredRoles = to.meta.roles as string[]
    
    if (requiredRoles && requiredRoles.length > 0) {
      const hasPermission = requiredRoles.some(role => 
        userRoles.includes(role) || userRoles.includes('ADMIN')
      )
      
      if (!hasPermission) {
        next('/')
        return
      }
    }
  }
  
  next()
})

export default router
