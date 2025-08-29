<template>
  <div class="ai-statistics-panel">
    <div class="panel-header">
      <h3 class="panel-title">AI处理统计</h3>
      <p class="panel-subtitle">AI订单处理的详细统计信息和趋势分析</p>
    </div>
    
    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <div class="stat-card">
        <div class="stat-icon primary">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statistics.totalCount }}</div>
          <div class="stat-label">总处理数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon success">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statistics.successCount }}</div>
          <div class="stat-label">成功处理</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon warning">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statistics.pendingCount }}</div>
          <div class="stat-label">等待处理</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon error">
          <el-icon><Close /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statistics.failedCount }}</div>
          <div class="stat-label">处理失败</div>
        </div>
      </div>
    </div>
    
    <!-- 详细统计 -->
    <div class="detailed-stats">
      <div class="stats-row">
        <div class="stats-item">
          <div class="stats-label">今日处理数</div>
          <div class="stats-value">{{ statistics.todayCount }}</div>
        </div>
        <div class="stats-item">
          <div class="stats-label">本周处理数</div>
          <div class="stats-value">{{ statistics.thisWeekCount }}</div>
        </div>
        <div class="stats-item">
          <div class="stats-label">本月处理数</div>
          <div class="stats-value">{{ statistics.thisMonthCount }}</div>
        </div>
        <div class="stats-item">
          <div class="stats-label">平均处理时间</div>
          <div class="stats-value">{{ statistics.averageProcessingTime }}ms</div>
        </div>
      </div>
    </div>
    
    <!-- 处理类型统计 -->
    <div class="process-type-stats">
      <h4 class="section-title">处理类型统计</h4>
      <div class="type-stats-grid">
        <div
          v-for="type in statistics.topProcessTypes"
          :key="type.type"
          class="type-stat-item"
        >
          <div class="type-header">
            <span class="type-name">{{ type.type }}</span>
            <span class="type-count">{{ type.count }}次</span>
          </div>
          <div class="type-progress">
            <el-progress
              :percentage="type.successRate"
              :color="getProgressColor(type.successRate)"
              :stroke-width="8"
              :show-text="false"
            />
            <span class="success-rate">{{ type.successRate }}%</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 时间趋势图表 -->
    <div class="trend-chart">
      <h4 class="section-title">处理趋势</h4>
      <div class="chart-container">
        <div class="chart-placeholder">
          <el-empty description="图表功能开发中..." />
        </div>
      </div>
    </div>
    
    <!-- 性能指标 -->
    <div class="performance-metrics">
      <h4 class="section-title">性能指标</h4>
      <div class="metrics-grid">
        <div class="metric-item">
          <div class="metric-label">响应时间</div>
          <div class="metric-value">2.5s</div>
          <div class="metric-trend positive">
            <el-icon><ArrowDown /></el-icon>
            -12%
          </div>
        </div>
        <div class="metric-item">
          <div class="metric-label">成功率</div>
          <div class="metric-value">98.5%</div>
          <div class="metric-trend positive">
            <el-icon><ArrowUp /></el-icon>
            +2.1%
          </div>
        </div>
        <div class="metric-item">
          <div class="metric-label">并发处理</div>
          <div class="metric-value">25</div>
          <div class="metric-trend neutral">
            <el-icon><Minus /></el-icon>
            0%
          </div>
        </div>
        <div class="metric-item">
          <div class="metric-label">错误率</div>
          <div class="metric-value">1.5%</div>
          <div class="metric-trend positive">
            <el-icon><ArrowDown /></el-icon>
            -0.8%
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { TrendCharts, Check, Clock, Close, ArrowDown, ArrowUp, Minus } from '@element-plus/icons-vue'
import type { AIProcessStatistics } from '@/types/ai'

// 响应式数据
const statistics = ref<AIProcessStatistics>({
  totalCount: 0,
  successCount: 0,
  pendingCount: 0,
  failedCount: 0,
  averageProcessingTime: 0,
  todayCount: 0,
  thisWeekCount: 0,
  thisMonthCount: 0,
  topProcessTypes: []
})

// 方法
const loadStatistics = async () => {
  try {
    // TODO: 调用API获取统计信息
    // 模拟数据
    statistics.value = {
      totalCount: 1250,
      successCount: 1230,
      pendingCount: 15,
      failedCount: 5,
      averageProcessingTime: 2500,
      todayCount: 45,
      thisWeekCount: 320,
      thisMonthCount: 1250,
      topProcessTypes: [
        { type: '智能分析', count: 450, successRate: 99.2 },
        { type: '订单优化', count: 380, successRate: 98.5 },
        { type: '库存预测', count: 320, successRate: 97.8 },
        { type: '客户画像', count: 100, successRate: 96.5 }
      ]
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const getProgressColor = (rate: number) => {
  if (rate >= 95) return '#67c23a'
  if (rate >= 80) return '#e6a23c'
  return '#f56c6c'
}

// 生命周期
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.ai-statistics-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel-header {
  margin-bottom: 16px;
}

.panel-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 8px 0;
}

.panel-subtitle {
  font-size: 14px;
  color: var(--md-sys-color-on-surface-variant);
  margin: 0;
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: var(--md-sys-color-surface-container-low);
  border-radius: 16px;
  box-shadow: var(--md-sys-elevation-level1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--md-sys-elevation-level2);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-icon.primary {
  background: var(--md-sys-color-primary);
}

.stat-icon.success {
  background: var(--md-sys-color-success);
}

.stat-icon.warning {
  background: var(--md-sys-color-warning);
}

.stat-icon.error {
  background: var(--md-sys-color-error);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--md-sys-color-on-surface);
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--md-sys-color-on-surface-variant);
  margin-top: 4px;
}

.detailed-stats {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--md-sys-elevation-level1);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 24px;
}

.stats-item {
  text-align: center;
}

.stats-label {
  font-size: 14px;
  color: var(--md-sys-color-on-surface-variant);
  margin-bottom: 8px;
}

.stats-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
}

.process-type-stats {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--md-sys-elevation-level1);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 20px 0;
}

.type-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.type-stat-item {
  background: var(--md-sys-color-surface);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--md-sys-color-outline-variant);
}

.type-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.type-name {
  font-weight: 500;
  color: var(--md-sys-color-on-surface);
}

.type-count {
  font-size: 14px;
  color: var(--md-sys-color-on-surface-variant);
}

.type-progress {
  display: flex;
  align-items: center;
  gap: 12px;
}

.success-rate {
  font-size: 14px;
  font-weight: 500;
  color: var(--md-sys-color-on-surface);
  min-width: 50px;
}

.trend-chart {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--md-sys-elevation-level1);
}

.chart-container {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.performance-metrics {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--md-sys-elevation-level1);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.metric-item {
  background: var(--md-sys-color-surface);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  border: 1px solid var(--md-sys-color-outline-variant);
}

.metric-label {
  font-size: 14px;
  color: var(--md-sys-color-on-surface-variant);
  margin-bottom: 8px;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin-bottom: 8px;
}

.metric-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.metric-trend.positive {
  color: var(--md-sys-color-success);
}

.metric-trend.negative {
  color: var(--md-sys-color-error);
}

.metric-trend.neutral {
  color: var(--md-sys-color-on-surface-variant);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .type-stats-grid {
    grid-template-columns: 1fr;
  }
  
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}
</style>
