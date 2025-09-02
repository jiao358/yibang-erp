<template>
  <div class="quick-action-card" @click="handleClick">
    <div class="card-icon" :style="{ '--card-color': color }">
      <el-icon :size="20">
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="card-content">
      <div class="card-title">{{ title }}</div>
      <div class="card-description">{{ description }}</div>
    </div>
    <div class="card-arrow">
      <el-icon :size="16">
        <ArrowRight />
      </el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'

interface Props {
  title: string
  description: string
  icon: string
  route: string
  color?: string
}

const props = withDefaults(defineProps<Props>(), {
  color: '#409EFF'
})

const router = useRouter()

const handleClick = () => {
  router.push(props.route)
}
</script>

<style scoped>
.quick-action-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.quick-action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
  border-color: var(--card-color);
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
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
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.card-description {
  font-size: 14px;
  color: #909399;
  line-height: 1.4;
}

.card-arrow {
  color: #C0C4CC;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.quick-action-card:hover .card-arrow {
  color: var(--card-color);
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .quick-action-card {
    padding: 16px;
    gap: 12px;
  }
  
  .card-icon {
    width: 36px;
    height: 36px;
  }
  
  .card-title {
    font-size: 15px;
  }
  
  .card-description {
    font-size: 13px;
  }
}
</style>
