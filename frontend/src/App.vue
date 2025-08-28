<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
// 主应用组件
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  margin: 0;
  padding: 0;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
  background: #f0f2f5;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Element Plus 全局样式调整 */
.el-menu {
  border-right: none !important;
}

.el-menu--collapse {
  width: 64px;
}

.el-menu-item.is-active {
  background-color: #409eff !important;
  color: white !important;
}

.el-menu-item:hover,
.el-sub-menu__title:hover {
  background-color: #263445 !important;
  color: #409eff !important;
}

.el-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-button {
  border-radius: 6px;
}

.el-input__inner {
  border-radius: 6px;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-pagination {
  text-align: right;
  margin-top: 20px;
}
</style>
