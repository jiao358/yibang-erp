<template>
  <div class="workspace-container">
    <!-- 系统管理员工作台 -->
    <SystemAdminWorkspace v-if="isSystemAdmin" />
    
    <!-- 供应链管理员工作台 -->
    <SupplierAdminWorkspace v-else-if="isSupplierAdmin" />
    
    <!-- 销售工作台 -->
    <SalesWorkspace v-else-if="isSales" />
    
    <!-- 默认工作台（其他角色） -->
    <DefaultWorkspace v-else />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import SystemAdminWorkspace from './components/SystemAdminWorkspace.vue'
import SupplierAdminWorkspace from './components/SupplierAdminWorkspace.vue'
import SalesWorkspace from './components/SalesWorkspace.vue'
import DefaultWorkspace from './components/DefaultWorkspace.vue'

// 检查用户角色权限
const isSystemAdmin = computed(() => {
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

const isSupplierAdmin = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SUPPLIER_ADMIN') || roles.includes('SUPPLIER_OPERATOR')
    } catch {
      return false
    }
  }
  return false
})

const isSales = computed(() => {
  const userRoles = localStorage.getItem('userRoles')
  if (userRoles) {
    try {
      const roles = JSON.parse(userRoles)
      return roles.includes('SALES_ADMIN') || roles.includes('SALES')
    } catch {
      return false
    }
  }
  return false
})
</script>

<style scoped>
.workspace-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>
