<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ 'collapsed': isSidebarCollapsed }">
      <div class="logo">
        <div class="logo-text">懿邦ERP</div>
      </div>
      
      <div class="nav-menu">
        <el-menu
          :default-active="$route.path"
          class="material-menu"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><Monitor /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-sub-menu index="/system" v-if="hasSystemManagePermission">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item 
              v-if="hasUserManagePermission" 
              index="/user"
            >
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item 
              v-if="hasSystemAdminPermission" 
              index="/role"
            >
              <el-icon><Lock /></el-icon>
              <span>角色管理</span>
            </el-menu-item>
            <el-menu-item  v-if="hasSystemAdminPermission" index="/company">
              <el-icon><OfficeBuilding /></el-icon>
              <span>公司管理</span>
            </el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/business">
            <template #title>
              <el-icon><Briefcase /></el-icon>
              <span>业务管理</span>
            </template>
            <el-menu-item 
              v-if="hasSupplierPermission" 
              index="/supplier-product"
            >
              <el-icon><Box /></el-icon>
              <span>供应链商品管理</span>
            </el-menu-item>
            <el-menu-item 
              v-if="hasSalesPermission" 
              index="/product-filter"
            >
              <el-icon><Search /></el-icon>
              <span>商品筛选</span>
            </el-menu-item>
            <el-menu-item index="/order">
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item 
              v-if="hasCustomerManagePermission" 
              index="/customer"
            >
              <el-icon><UserFilled /></el-icon>
              <span>客户管理</span>
            </el-menu-item>
                      <el-menu-item 
            v-if="hasAIExcelImportPermission" 
            index="/ai-excel-import"
          >
            <el-icon><Upload /></el-icon>
            <span>AI Excel导入</span>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 库存管理模块 -->
        <el-sub-menu index="/inventory" v-if="hasInventoryPermission">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </template>
          <el-menu-item index="/warehouse">
            <el-icon><House /></el-icon>
            <span>仓库管理</span>
          </el-menu-item>
          <el-menu-item index="/inventory">
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 价格管理模块 -->
        <el-sub-menu index="/pricing" v-if="hasPricingPermission">
          <template #title>
            <el-icon><TrendCharts /></el-icon>
            <span>价格管理</span>
          </template>
          <el-menu-item index="/pricing">
            <el-icon><TrendCharts /></el-icon>
            <span>价格分层管理</span>
          </el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/digital-screen">
          <el-icon><DataBoard /></el-icon>
          <span>数字大屏</span>
        </el-menu-item>
        
        <el-menu-item index="/ai-management" v-if="hasSystemAdminPermission">
          <el-icon><Cpu /></el-icon>
          <span>AI管理</span>
        </el-menu-item>
        
        <el-menu-item index="/monitor" v-if="hasSystemAdminPermission">
          <el-icon><TrendCharts /></el-icon>
          <span>系统监控</span>
        </el-menu-item>
        </el-menu>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <div class="header">
        <div class="left-section">
          <button class="toggle-btn material-btn material-btn-secondary" @click="toggleSidebar">
            <el-icon><Fold /></el-icon>
          </button>
          
          <el-breadcrumb class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">仪表盘</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="right-section">
          <el-dropdown trigger="click" class="user-dropdown">
            <button class="user-btn material-btn material-btn-secondary">
              <div class="user-info">
                <div class="user-avatar">
                  {{ userInitial }}
                </div>
                <span class="user-name">{{ userInfo.realName || userInfo.username }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
            </button>
            
            <template #dropdown>
              <el-dropdown-menu class="material-dropdown">
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 页面内容 -->
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Monitor, Setting, User, Lock, OfficeBuilding, Briefcase, 
  Box, Document, DataBoard, Cpu, TrendCharts, Fold, 
  ArrowDown, SwitchButton, UserFilled, House, Warning, Check, Upload
} from '@element-plus/icons-vue'

const router = useRouter()

// 响应式数据
const isSidebarCollapsed = ref(false)
const userInfo = ref({
  username: 'admin',
  realName: '系统管理员'
})

// 计算属性
const userInitial = computed(() => {
  const name = userInfo.value.realName || userInfo.value.username
  return name.charAt(0).toUpperCase()
})

// 检查是否有系统管理员权限
const hasSystemAdminPermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN')
    } catch {
      return false
    }
  }
  return false
})

// 检查是否有系统管理权限（系统管理员或供应链管理员）
const hasSystemManagePermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN') || roles.includes('SUPPLIER_ADMIN')
    } catch {
      return false
    }
  }
  return false
})

// 检查是否有用户管理权限（系统管理员或供应链管理员）
const hasUserManagePermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN') || roles.includes('SUPPLIER_ADMIN')
    } catch {
      return false
    }
  }
  return false
})

// 检查是否有供应链权限（系统管理员或供应链管理员）
const hasSupplierPermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN') || roles.includes('SUPPLIER_ADMIN')
    } catch {
      return false
    }
  }
  return false
})

// 检查是否有销售权限（系统管理员、销售管理员或销售）
const hasSalesPermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN') || roles.includes('SALES_ADMIN') || roles.includes('SALES')
    } catch {
      return false
    }
  }
  return false
})

// 检查是否有客户管理权限（系统管理员或销售管理员）
const hasCustomerManagePermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN') || roles.includes('SALES_ADMIN')
    } catch {
      return false
    }
  }
  return false
})

// 检查是否有AI Excel导入权限（系统管理员、销售管理员或销售）
const hasAIExcelImportPermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN') || roles.includes('SALES_ADMIN') || roles.includes('SALES')
    } catch {
      return false
    }
  }
  return false
})

// 检查是否有库存管理权限（系统管理员或供应链管理员）
const hasInventoryPermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN') || roles.includes('SUPPLIER_ADMIN')
    } catch {
      return false
    }
  }
  return false
})

// 检查是否有价格管理权限（系统管理员或供应链管理员）
const hasPricingPermission = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SYSTEM_ADMIN') || roles.includes('SUPPLIER_ADMIN')
    } catch {
      return false
    }
  }
  return false
})

// 方法
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

const handleLogout = () => {
  // 清除用户信息
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userRoles')
  localStorage.removeItem('token')
  
  // 跳转到登录页
  router.push('/login')
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
})
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  background-color: var(--background-color);
}

/* 侧边栏 */
.sidebar {
  width: 280px;
  background-color: var(--surface-color);
  border-right: 1px solid var(--divider-color);
  box-shadow: var(--shadow-1);
  transition: all var(--transition-normal);
  display: flex;
  flex-direction: column;
  z-index: 1000;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--divider-color);
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: white;
  text-align: center;
}

.logo-text {
  font-size: var(--font-size-xl);
  font-weight: 700;
  letter-spacing: 1px;
}

.nav-menu {
  flex: 1;
  padding: var(--spacing-md) 0;
  overflow-y: auto;
}

.material-menu {
  border: none;
  background: transparent;
}

.material-menu :deep(.el-menu-item),
.material-menu :deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--border-radius-md);
  color: var(--text-secondary);
  transition: all var(--transition-normal);
}

.material-menu :deep(.el-menu-item:hover),
.material-menu :deep(.el-sub-menu__title:hover) {
  background-color: var(--primary-50);
  color: var(--primary-color);
}

.material-menu :deep(.el-menu-item.is-active) {
  background-color: var(--primary-color);
  color: white;
}

.material-menu :deep(.el-menu-item.is-active:hover) {
  background-color: var(--primary-dark);
}

.material-menu :deep(.el-icon) {
  margin-right: var(--spacing-md);
  font-size: 18px;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部导航栏 */
.header {
  height: 64px;
  background-color: var(--surface-color);
  border-bottom: 1px solid var(--divider-color);
  box-shadow: var(--shadow-1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  position: relative;
}

.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-color), var(--primary-light));
}

.left-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.toggle-btn {
  padding: var(--spacing-sm);
  min-width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.breadcrumb {
  color: var(--text-secondary);
}

.breadcrumb :deep(.el-breadcrumb__item) {
  color: var(--text-secondary);
}

.breadcrumb :deep(.el-breadcrumb__item:last-child) {
  color: var(--text-primary);
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: inherit;
  transition: all var(--transition-normal);
}

.breadcrumb :deep(.el-breadcrumb__inner:hover) {
  color: var(--primary-color);
}

.right-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-btn {
  padding: var(--spacing-sm) var(--spacing-md);
  height: 40px;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(45deg, var(--primary-color), var(--primary-light));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
}

.user-name {
  font-weight: 500;
  color: var(--text-primary);
}

.dropdown-icon {
  transition: transform var(--transition-normal);
}

.user-btn:hover .dropdown-icon {
  transform: rotate(180deg);
}

.material-dropdown {
  background: var(--surface-color);
  border: 1px solid var(--divider-color);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-3);
  overflow: hidden;
}

.material-dropdown :deep(.el-dropdown-menu__item) {
  color: var(--text-primary);
  transition: all var(--transition-normal);
  padding: var(--spacing-sm) var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.material-dropdown :deep(.el-dropdown-menu__item:hover) {
  background-color: var(--primary-50);
  color: var(--primary-color);
}

.material-dropdown :deep(.el-dropdown-menu__item:not(:last-child)) {
  border-bottom: 1px solid var(--divider-color);
}

.content {
  flex: 1;
  padding: 0;
  overflow: auto;
  position: relative;
}

.content::-webkit-scrollbar {
  width: 8px;
}

.content::-webkit-scrollbar-track {
  background: var(--background-color);
}

.content::-webkit-scrollbar-thumb {
  background: var(--divider-color);
  border-radius: 4px;
}

.content::-webkit-scrollbar-thumb:hover {
  background: var(--text-hint);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .sidebar {
    width: 240px;
  }
  
  .logo-text {
    font-size: var(--font-size-lg);
  }
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: -280px;
    z-index: 1000;
    transition: left var(--transition-normal);
  }
  
  .sidebar.open {
    left: 0;
  }
  
  .header {
    padding: 0 var(--spacing-md);
  }
  
  .breadcrumb {
    display: none;
  }
  
  .user-name {
    display: none;
  }
  
  .toggle-btn {
    min-width: 36px;
    height: 36px;
  }
}

/* 动画效果 */
@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.main-layout {
  animation: slideInLeft 0.3s ease-out;
}

.sidebar {
  animation: slideInLeft 0.3s ease-out 0.1s both;
}

.header {
  animation: slideInRight 0.3s ease-out 0.2s both;
}

.content {
  animation: slideInRight 0.3s ease-out 0.3s both;
}
</style>
