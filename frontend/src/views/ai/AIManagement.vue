<template>
  <div class="ai-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">AI智能管理</h1>
      <p class="page-subtitle">DeepSeek AI模型集成，智能处理订单业务</p>
    </div>

    <!-- 功能导航标签 -->
    <div class="tab-navigation">
      <el-tabs v-model="activeTab" class="ai-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="AI配置" name="config">
          <AIConfigPanel />
        </el-tab-pane>
        <el-tab-pane label="订单处理" name="orders">
          <AIOrderPanel />
        </el-tab-pane>
        <el-tab-pane label="处理历史" name="history">
          <AIHistoryPanel />
        </el-tab-pane>
        <el-tab-pane label="统计信息" name="statistics">
          <AIStatisticsPanel />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AIConfigPanel from './components/AIConfigPanel.vue'
import AIOrderPanel from './components/AIOrderPanel.vue'
import AIHistoryPanel from './components/AIHistoryPanel.vue'
import AIStatisticsPanel from './components/AIStatisticsPanel.vue'

// 响应式数据
const activeTab = ref('config')

// 方法
const handleTabChange = (tabName: string) => {
  console.log('切换到标签页:', tabName)
}
</script>

<style scoped>
.ai-management-container {
  padding: 24px;
  background: var(--md-sys-color-surface);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: var(--md-sys-color-on-surface-variant);
  margin: 0;
}

.tab-navigation {
  background: var(--md-sys-color-surface-container);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--md-sys-elevation-level1);
}

.ai-tabs {
  --el-tabs-header-height: 56px;
}

.ai-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.ai-tabs :deep(.el-tabs__nav-wrap) {
  padding: 0 8px;
}

.ai-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  padding: 0 24px;
  height: 56px;
  line-height: 56px;
  border-radius: 8px 8px 0 0;
  transition: all 0.3s ease;
}

.ai-tabs :deep(.el-tabs__item.is-active) {
  background: var(--md-sys-color-primary-container);
  color: var(--md-sys-color-on-primary-container);
}

.ai-tabs :deep(.el-tabs__active-bar) {
  background: var(--md-sys-color-primary);
  height: 4px;
  border-radius: 2px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-management-container {
    padding: 16px;
  }
  
  .tab-navigation {
    padding: 16px;
  }
  
  .ai-tabs :deep(.el-tabs__item) {
    padding: 0 16px;
    font-size: 14px;
  }
}
</style>
