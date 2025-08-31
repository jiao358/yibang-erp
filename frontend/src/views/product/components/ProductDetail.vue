<template>
  <el-dialog
    v-model="visible"
    :title="product ? `${product.name} - 商品详情` : '商品详情'"
    width="800px"
    :before-close="handleClose"
  >
    <div v-if="product" class="product-detail">
      <div class="detail-content">
      <!-- 基本信息 -->
      <div class="detail-section">
        <h3 class="section-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>商品编码：</label>
            <span>{{ product.sku }}</span>
          </div>
          <div class="info-item">
            <label>商品名称：</label>
            <span>{{ product.name }}</span>
          </div>
          <div class="info-item">
            <label>商品状态：</label>
            <el-tag :type="getStatusTagType(product.status)" size="small">
              {{ getStatusText(product.status) }}
            </el-tag>
          </div>
          <div class="info-item">
            <label>审核状态：</label>
            <el-tag :type="getApprovalStatusTagType(product.approvalStatus)" size="small">
              {{ getApprovalStatusText(product.approvalStatus) }}
            </el-tag>
          </div>
          <div class="info-item">
            <label>单位：</label>
            <span>{{ product.unit }}</span>
          </div>
          <div class="info-item">
            <label>创建时间：</label>
            <span>{{ formatDate(product.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- 价格信息 -->
      <div class="detail-section">
        <h3 class="section-title">价格信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>成本价：</label>
            <span class="price cost-price">¥{{ product.costPrice }}</span>
          </div>
          <div class="info-item">
            <label>销售价：</label>
            <span class="price selling-price">¥{{ product.sellingPrice }}</span>
          </div>
          <div class="info-item">
            <label>市场价：</label>
            <span class="price market-price">¥{{ product.marketPrice || '--' }}</span>
          </div>
          <div class="info-item">
            <label>零售限价：</label>
            <span class="price retail-limit-price">¥{{ product.retailLimitPrice || '--' }}</span>
          </div>
        </div>
      </div>

      <!-- 价格分层信息 -->
      <div class="detail-section" v-if="product.priceTierConfigs && product.priceTierConfigs.length > 0">
        <h3 class="section-title">价格分层配置</h3>
        <div class="price-tier-grid">
          <div 
            v-for="config in product.priceTierConfigs" 
            :key="config.id" 
            class="price-tier-item"
          >
            <div class="tier-header">
              <h4>{{ config.priceTierName }}</h4>
              <el-tag :type="config.isActive ? 'success' : 'info'" size="small">
                {{ config.isActive ? '启用' : '停用' }}
              </el-tag>
            </div>
            <div class="tier-content">
              <div class="tier-price">
                <label>一件代发价格：</label>
                <span class="price dropshipping-price">¥{{ config.dropshippingPrice }}</span>
              </div>
              <div class="tier-price">
                <label>零售限价：</label>
                <span class="price retail-limit-price">¥{{ config.retailLimitPrice }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品属性 -->
      <div class="detail-section">
        <h3 class="section-title">商品属性</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>重量：</label>
            <span>{{ product.weight ? `${product.weight}kg` : '--' }}</span>
          </div>
          <div class="info-item">
            <label>材质：</label>
            <span>{{ product.material || '--' }}</span>
          </div>
          <div class="info-item">
            <label>颜色：</label>
            <span>{{ product.color || '--' }}</span>
          </div>
          <div class="info-item">
            <label>尺寸：</label>
            <span>{{ product.size || '--' }}</span>
          </div>
          <div class="info-item">
            <label>原产国：</label>
            <span>{{ product.originCountry || '--' }}</span>
          </div>
          <div class="info-item">
            <label>海关编码：</label>
            <span>{{ product.hsCode || '--' }}</span>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="detail-section" v-if="product.description">
        <h3 class="section-title">商品描述</h3>
        <div class="description-content">
          {{ product.description }}
        </div>
      </div>

      <!-- 商品简介 -->
      <div class="detail-section" v-if="product.shortDescription">
        <h3 class="section-title">商品简介</h3>
        <div class="description-content">
          {{ product.shortDescription }}
        </div>
      </div>

      <!-- 商品标签 -->
      <div class="detail-section" v-if="product.tags">
        <h3 class="section-title">商品标签</h3>
        <div class="tags-content">
          <el-tag
            v-for="tag in parseTags(product.tags)"
            :key="tag"
            size="small"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <!-- 审核信息 -->
      <div class="detail-section" v-if="product.approvalComment || product.approvalAt">
        <h3 class="section-title">审核信息</h3>
        <div class="info-grid">
          <div class="info-item" v-if="product.approvalComment">
            <label>审核意见：</label>
            <span>{{ product.approvalComment }}</span>
          </div>
          <div class="info-item" v-if="product.approvalAt">
            <label>审核时间：</label>
            <span>{{ formatDate(product.approvalAt) }}</span>
          </div>
          <div class="info-item" v-if="product.approvalBy">
            <label>审核人：</label>
            <span>{{ product.approvalBy }}</span>
          </div>
        </div>
      </div>

      <!-- 商品特色 -->
      <div class="detail-section">
        <h3 class="section-title">商品特色</h3>
        <div class="features-content">
          <div class="feature-item">
            <el-icon :class="{ 'feature-active': product.isFeatured }">
              <Star />
            </el-icon>
            <span>推荐商品</span>
          </div>
          <div class="feature-item">
            <el-icon :class="{ 'feature-active': product.isHot }">
              <Fire />
            </el-icon>
            <span>热销商品</span>
          </div>
          <div class="feature-item">
            <el-icon :class="{ 'feature-active': product.isNew }">
              <Gift />
            </el-icon>
            <span>新品</span>
          </div>
        </div>
      </div>
    </div>

    </div>
    
    <div v-else class="no-data">
      <el-empty description="暂无商品信息" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
// 使用Element Plus内置图标，避免@element-plus/icons-vue的导入问题
import type { Product } from '@/types/product'

// Props
interface Props {
  visible: boolean
  product: Product | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

// 计算属性
const product = computed(() => props.product)

// 对话框可见性
const visible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value)
})

// 关闭对话框
const handleClose = () => {
  emit('update:visible', false)
}

// 方法
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'ACTIVE': 'success',
    'INACTIVE': 'danger',
    'DISCONTINUED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PENDING': '待审核',
    'ACTIVE': '已上架',
    'INACTIVE': '已下架',
    'DISCONTINUED': '已停售'
  }
  return textMap[status] || status
}

const getApprovalStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getApprovalStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || status
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const parseTags = (tags: string) => {
  try {
    const parsed = JSON.parse(tags)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}
</script>

<style scoped>
.product-detail {
  padding: 0;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--md-sys-color-outline-variant);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item label {
  font-weight: 500;
  color: var(--md-sys-color-on-surface-variant);
  min-width: 80px;
}

.info-item span {
  color: var(--md-sys-color-on-surface);
}

.price {
  font-weight: 600;
}

.cost-price {
  color: var(--md-sys-color-error);
}

.selling-price {
  color: var(--md-sys-color-primary);
}

.market-price {
  color: var(--md-sys-color-on-surface-variant);
}

.retail-limit-price {
  color: var(--md-sys-color-tertiary);
}

/* 价格分层配置样式 */
.price-tier-grid {
  display: grid;
  gap: 16px;
  margin-top: 16px;
}

.price-tier-item {
  background: var(--md-sys-color-surface);
  border: 1px solid var(--md-sys-color-outline-variant);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.price-tier-item:hover {
  border-color: var(--md-sys-color-primary);
  box-shadow: var(--md-sys-elevation-level1);
}

.tier-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--md-sys-color-outline-variant);
}

.tier-header h4 {
  margin: 0;
  color: var(--md-sys-color-on-surface);
  font-size: 16px;
  font-weight: 600;
}

.tier-content {
  display: grid;
  gap: 12px;
}

.tier-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.tier-price label {
  color: var(--md-sys-color-on-surface-variant);
  font-size: 14px;
  font-weight: 500;
}

.tier-price .price {
  font-weight: 600;
  font-size: 16px;
}

.dropshipping-price {
  color: var(--md-sys-color-primary);
}

.retail-limit-price {
  color: var(--md-sys-color-tertiary);
}

.description-content {
  color: var(--md-sys-color-on-surface);
  line-height: 1.6;
  background: var(--md-sys-color-surface);
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid var(--md-sys-color-primary);
}

.tags-content {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin: 0;
}

.features-content {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--md-sys-color-surface);
  border-radius: 8px;
  border: 1px solid var(--md-sys-color-outline-variant);
  transition: all 0.3s ease;
}

.feature-item:hover {
  border-color: var(--md-sys-color-primary);
  box-shadow: var(--md-sys-elevation-level1);
}

.feature-item .el-icon {
  font-size: 18px;
  color: var(--md-sys-color-on-surface-variant);
  transition: color 0.3s ease;
}

.feature-item .feature-active {
  color: var(--md-sys-color-primary);
}

.feature-item span {
  color: var(--md-sys-color-on-surface);
  font-weight: 500;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .features-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .feature-item {
    width: 100%;
    justify-content: center;
  }
}
</style>
