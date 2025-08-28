<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="logo">
        <img src="/logo.png" alt="易邦ERP" v-if="!isCollapse" />
        <span v-if="!isCollapse">易邦ERP</span>
        <el-icon v-else><Platform /></el-icon>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        
        <el-sub-menu index="system" v-if="hasPermission(['ADMIN'])">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/user">用户管理</el-menu-item>
          <el-menu-item index="/role">角色管理</el-menu-item>
          <el-menu-item index="/company">公司管理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="business" v-if="hasPermission(['ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN'])">
          <template #title>
            <el-icon><Briefcase /></el-icon>
            <span>业务管理</span>
          </template>
          <el-menu-item index="/product">产品管理</el-menu-item>
          <el-menu-item index="/order">订单管理</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/digital-screen" v-if="hasPermission(['ADMIN', 'SUPPLIER_ADMIN', 'SALES_ADMIN', 'SUPPLIER_OPERATOR', 'SALES'])">
          <el-icon><Monitor /></el-icon>
          <template #title>数字大屏</template>
        </el-menu-item>
        
        <el-menu-item index="/ai-management" v-if="hasPermission(['ADMIN'])">
          <el-icon><Cpu /></el-icon>
          <template #title>AI管理</template>
        </el-menu-item>
        
        <el-menu-item index="/system-monitor" v-if="hasPermission(['ADMIN'])">
          <el-icon><TrendCharts /></el-icon>
          <template #title>系统监控</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区域 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-button
            type="text"
            @click="toggleSidebar"
            class="collapse-btn"
          >
            <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>
          
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userAvatar">
                {{ userInfo.realName?.charAt(0) || userInfo.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userInfo.realName || userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Platform,
  DataBoard,
  Setting,
  Briefcase,
  Monitor,
  Cpu,
  TrendCharts,
  Fold,
  Expand,
  ArrowDown
} from '@element-plus/icons-vue'

// 路由和导航
const route = useRoute()
const router = useRouter()

// 响应式数据
const isCollapse = ref(false)
const userInfo = ref({
  username: 'admin',
  realName: '系统管理员',
  avatar: ''
})

// 计算属性
const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title as string
  }))
})

const userAvatar = computed(() => userInfo.value.avatar || '')

// 权限检查
const hasPermission = (requiredRoles: string[]) => {
  const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]')
  return requiredRoles.some(role => 
    userRoles.includes(role) || userRoles.includes('ADMIN')
  )
}

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 处理用户命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中...')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中...')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '确认退出',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        // 清除本地存储
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userRoles')
        
        // 跳转到登录页
        router.push('/login')
        ElMessage.success('已退出登录')
      } catch (error) {
        // 用户取消
      }
      break
  }
}

// 监听路由变化，更新面包屑
watch(() => route.path, () => {
  // 可以在这里添加路由变化的处理逻辑
}, { immediate: true })

// 组件挂载时获取用户信息
const getUserInfo = () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  }
}

// 组件挂载
getUserInfo()
</script>

<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
}

.sidebar {
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2b2f3a;
  color: white;
  font-size: 18px;
  font-weight: bold;
}

.logo img {
  height: 32px;
  margin-right: 8px;
}

.sidebar-menu {
  border: none;
  background: #304156;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: #bfcbd9;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: #263445;
  color: #409eff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #409eff;
  color: white;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  margin-right: 20px;
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
