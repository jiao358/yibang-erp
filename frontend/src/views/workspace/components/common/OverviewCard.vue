<template>
  <div class="overview-card" :style="{ '--card-color': color }">
    <div class="card-icon">
      <el-icon :size="24">
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="card-content">
      <div class="card-title">{{ title }}</div>
      <div class="card-value">{{ formattedValue }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  title: string
  value: number | string
  icon: string
  color?: string
}

const props = withDefaults(defineProps<Props>(), {
  color: '#409EFF'
})

// 格式化数值显示
const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    if (props.value >= 10000) {
      return (props.value / 10000).toFixed(1) + '万'
    } else if (props.value >= 1000) {
      return (props.value / 1000).toFixed(1) + 'K'
    }
    return props.value.toString()
  }
  return props.value
})
</script>

<style scoped>
.overview-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  border-left: 4px solid var(--card-color);
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--card-color), rgba(64, 158, 255, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  line-height: 1.4;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

@media (max-width: 768px) {
  .overview-card {
    padding: 16px;
    gap: 12px;
  }
  
  .card-icon {
    width: 40px;
    height: 40px;
  }
  
  .card-title {
    font-size: 13px;
  }
  
  .card-value {
    font-size: 20px;
  }
}
</style>
