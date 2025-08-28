import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
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
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: {
          title: '仪表盘',
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
          roles: ['ADMIN']
        }
      },
      {
        path: 'role',
        name: 'RoleManagement',
        component: () => import('@/views/role/RoleList.vue'),
        meta: {
          title: '角色管理',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'company',
        name: 'CompanyManagement',
        component: () => import('@/views/company/CompanyList.vue'),
        meta: {
          title: '公司管理',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'product',
        name: 'ProductManagement',
        component: () => import('@/views/product/ProductList.vue'),
        meta: {
          title: '产品管理',
          requiresAuth: true,
          roles: ['ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN']
        }
      },
      {
        path: 'order',
        name: 'OrderManagement',
        component: () => import('@/views/order/OrderList.vue'),
        meta: {
          title: '订单管理',
          requiresAuth: true,
          roles: ['ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN', 'SUPPLIER_OPERATOR', 'SALES']
        }
      },
      {
        path: 'digital-screen',
        name: 'DigitalScreen',
        component: () => import('@/views/digital-screen/DigitalScreen.vue'),
        meta: {
          title: '数字大屏',
          requiresAuth: true,
          roles: ['ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN', 'SUPPLIER_OPERATOR', 'SALES']
        }
      },
      {
        path: 'ai-management',
        name: 'AIManagement',
        component: () => import('@/views/ai/AIManagement.vue'),
        meta: {
          title: 'AI管理',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'system-monitor',
        name: 'SystemMonitor',
        component: () => import('@/views/monitor/SystemMonitor.vue'),
        meta: {
          title: '系统监控',
          requiresAuth: true,
          roles: ['ADMIN']
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

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 易邦ERP系统` : '易邦ERP系统'
  
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
        next('/dashboard')
        return
      }
    }
  }
  
  next()
})

export default router
