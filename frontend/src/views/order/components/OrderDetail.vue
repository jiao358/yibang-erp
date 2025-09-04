<template>
  <el-dialog
    v-model="visible"
    :title="order ? `订单详情 - ${order.platformOrderNo}` : '订单详情'"
    width="90%"
    :before-close="handleClose"
    append-to-body
    :z-index="9999"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
  >
    <div v-if="order" class="order-detail">
      <div class="detail-content">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>平台订单号：</label>
              <span>{{ order.platformOrderNo }}</span>
            </div>
            <div class="info-item">
              <label>来源订单号：</label>
              <span>{{ order.sourceOrderNo || '--' }}</span>
            </div>
            <div class="info-item">
              <label>客户：</label>
              <span>{{ order.customerName || '--' }}</span>
            </div>
            <div class="info-item">
              <label>订单状态：</label>
              <el-tag :type="getStatusTagType(order.orderStatus)" size="small">
                {{ getStatusText(order.orderStatus) }}
              </el-tag>
            </div>
            <div class="info-item">
              <label>订单来源：</label>
              <el-tag :type="getSourceTagType(order.source)" size="small">
                {{ getSourceText(order.source) }}
              </el-tag>
            </div>
            <div class="info-item">
              <label>创建时间：</label>
              <span>{{ formatDate(order.createdAt) }}</span>
            </div>
            <div class="info-item">
              <label>更新时间：</label>
              <span>{{ formatDate(order.updatedAt) }}</span>
            </div>
            <div class="info-item">
              <label>创建人：</label>
              <span>{{ order.createUserName || '--' }}</span>
            </div>
          </div>
        </div>

        <!-- 收货信息 -->
        <div class="detail-section">
          <h3 class="section-title">收货信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>收货人：</label>
              <span>{{ order.deliveryContact || '--' }}</span>
            </div>
            <div class="info-item">
              <label>联系电话：</label>
              <span>{{ order.deliveryPhone || '--' }}</span>
            </div>
            <div class="info-item">
              <label>收货地址：</label>
              <span>{{ getFullAddress() }}</span>
            </div>
            <div class="info-item">
              <label>邮政编码：</label>
              <span>{{ order.deliveryPostalCode || '--' }}</span>
            </div>
            <div class="info-item">
              <label>卖家留言</label>
              <span>{{ order.salesNote || '--' }}</span>
            </div>
            <div class="info-item">
              <label>买家留言</label>
              <span>{{ order.buyerNote || '--' }}</span>
            </div>
          </div>
        </div>

        <!-- 物流信息 -->
        <div class="detail-section" v-if="order.logisticsInfo">
          <h3 class="section-title">物流信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>物流公司：</label>
              <span>{{ order.logisticsInfo.carrier || '--' }}</span>
            </div>
            <div class="info-item">
              <label>物流单号：</label>
              <span>{{ order.logisticsInfo.trackingNumber || '--' }}</span>
            </div>
            <div class="info-item">
              <label>发货方式：</label>
              <span>{{ order.logisticsInfo.shippingMethod || '--' }}</span>
            </div>
            <div class="info-item">
              <label>发货时间：</label>
              <span>{{ formatDate(order.logisticsInfo.shippedAt) }}</span>
            </div>
            <div class="info-item">
              <label>物流备注：</label>
              <span>{{ order.logisticsInfo.notes || '--' }}</span>
            </div>
          </div>
        </div>

        <!-- 订单商品 -->
        <div class="detail-section">
          <h3 class="section-title">订单商品</h3>
          <el-table :data="order.orderItems" border style="width: 100%">
            <el-table-column prop="productName" label="商品名称" min-width="200" />
            <el-table-column prop="productSku" label="商品编码" width="120" />
            <el-table-column prop="unit" label="单位" width="150" />
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="currency" label="币种" width="80" align="center" />
            <el-table-column prop="unitPrice" label="单价" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.unitPrice }}
              </template>
            </el-table-column>
            <el-table-column prop="discountAmount" label="折扣金额" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.discountAmount || 0 }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100" align="right">
              <template #default="{ row }">
                ¥{{ (parseFloat(row.quantity) * parseFloat(row.unitPrice)).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>



        <!-- 审核信息 -->
        <div class="detail-section" v-if="order.approvalComment || order.approvalAt">
          <h3 class="section-title">审核信息</h3>
          <div class="info-grid">
            <div class="info-item" v-if="order.approvalComment">
              <label>审核意见：</label>
              <span>{{ order.approvalComment }}</span>
            </div>
            <div class="info-item" v-if="order.approvalAt">
              <label>审核时间：</label>
              <span>{{ formatDate(order.approvalAt) }}</span>
            </div>
            <div class="info-item" v-if="order.approvalBy">
              <label>审核人：</label>
              <span>{{ order.approvalBy }}</span>
            </div>
          </div>
        </div>

        <!-- 备注信息 -->
        <div class="detail-section" v-if="order.notes">
          <h3 class="section-title">备注信息</h3>
          <div class="notes-content">
            {{ order.notes }}
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="no-data">
      <el-empty description="暂无订单信息" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { OrderResponse } from '@/types/order'

// Props
interface Props {
  visible: boolean
  order: OrderResponse | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

// 计算属性
const order = computed(() => props.order)

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
    'CONFIRMED': 'success',
    'SHIPPED': 'primary',
    'DELIVERED': 'success',
    'CANCELLED': 'danger',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'SHIPPED': '已发货',
    'DELIVERED': '已送达',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || status
}

const getSourceTagType = (source: string) => {
  const typeMap: Record<string, string> = {
    'MANUAL': 'primary',
    'IMPORT': 'success',
    'API': 'warning'
  }
  return typeMap[source] || 'info'
}

const getSourceText = (source: string) => {
  const textMap: Record<string, string> = {
    'MANUAL': '手动创建',
    'IMPORT': '批量导入',
    'API': 'API接口'
  }
  return textMap[source] || source
}

const formatDate = (date: string) => {
  if (!date) return '--'
  return new Date(date).toLocaleString('zh-CN')
}

const getFullAddress = () => {
  if (!order.value) return '--'
  const { deliveryProvince, deliveryCity, deliveryDistrict, deliveryAddress } = order.value
  const parts = [deliveryProvince, deliveryCity, deliveryDistrict, deliveryAddress].filter(Boolean)
  return parts.length > 0 ? parts.join('') : '--'
}
</script>

<style scoped>
.order-detail {
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
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
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
  min-width: 100px;
}

.info-item span {
  color: var(--md-sys-color-on-surface);
}

/* 价格汇总样式 */
.price-summary {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 400px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.price-item.total {
  border-top: 2px solid var(--md-sys-color-outline-variant);
  margin-top: 8px;
  padding-top: 16px;
  font-weight: 600;
}

.price-item label {
  color: var(--md-sys-color-on-surface-variant);
  font-weight: 500;
}

.price {
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
}

.price.discount {
  color: var(--md-sys-color-error);
}

.price.total-amount {
  font-size: 18px;
  color: var(--md-sys-color-primary);
}

.notes-content {
  color: var(--md-sys-color-on-surface);
  line-height: 1.6;
  background: var(--md-sys-color-surface);
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid var(--md-sys-color-primary);
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
  
  .price-summary {
    max-width: 100%;
  }
}
</style>
