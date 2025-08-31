<template>
  <div class="product-audit">
    <div v-if="product" class="audit-content">
      <!-- 商品基本信息 -->
      <div class="product-info">
        <h4 class="info-title">商品信息</h4>
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
            <label>当前状态：</label>
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
        </div>
      </div>

      <!-- 审核表单 -->
      <div class="audit-form">
        <h4 class="form-title">审核操作</h4>
        
        <el-form
          ref="formRef"
          :model="auditForm"
          :rules="rules"
          label-width="100px"
        >
          <el-form-item label="审核结果" prop="approvalStatus">
            <el-radio-group v-model="auditForm.approvalStatus">
              <el-radio label="APPROVED">
                <el-icon color="var(--md-sys-color-success)">
                  <Check />
                </el-icon>
                通过
              </el-radio>
              <el-radio label="REJECTED">
                <el-icon color="var(--md-sys-color-error)">
                  <Close />
                </el-icon>
                拒绝
              </el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item 
            label="审核意见" 
            prop="approvalComment"
            :rules="auditForm.approvalStatus === 'REJECTED' ? rules.approvalComment : []"
          >
            <el-input
              v-model="auditForm.approvalComment"
              type="textarea"
              :rows="4"
              :placeholder="getCommentPlaceholder()"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 审核历史 -->
      <div class="audit-history" v-if="auditHistory.length > 0">
        <h4 class="history-title">审核历史</h4>
        <div class="history-list">
          <div
            v-for="(item, index) in auditHistory"
            :key="index"
            class="history-item"
          >
            <div class="history-header">
              <div class="history-status">
                <el-tag :type="getApprovalStatusTagType(item.status)" size="small">
                  {{ getApprovalStatusText(item.status) }}
                </el-tag>
              </div>
              <div class="history-time">
                {{ formatDate(item.approvalAt) }}
              </div>
            </div>
            <div class="history-content" v-if="item.comment">
              {{ item.comment }}
            </div>
            <div class="history-footer">
              <span class="history-approver">审核人：{{ item.approverName || '未知' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="audit-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit" 
          :loading="submitting"
          :disabled="!auditForm.approvalStatus"
        >
          提交审核
        </el-button>
      </div>
    </div>

    <div v-else class="no-data">
      <el-empty description="暂无商品信息" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
// 使用Element Plus内置图标，避免@element-plus/icons-vue的导入问题
import { auditProduct } from '@/api/product'
import type { Product } from '@/types/product'

// Props
interface Props {
  product: Product | null
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  success: []
  cancel: []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const auditHistory = ref<any[]>([])

// 审核表单
const auditForm = reactive({
  approvalStatus: '',
  approvalComment: ''
})

// 表单验证规则
const rules: FormRules = {
  approvalStatus: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  approvalComment: [
    { required: true, message: '请输入审核意见', trigger: 'blur' },
    { min: 5, max: 1000, message: '审核意见长度在 5 到 1000 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const product = computed(() => props.product)

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

const getCommentPlaceholder = () => {
  if (auditForm.approvalStatus === 'APPROVED') {
    return '请输入审核通过的意见（可选）'
  } else if (auditForm.approvalStatus === 'REJECTED') {
    return '请输入拒绝原因（必填）'
  }
  return '请输入审核意见'
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const loadAuditHistory = async () => {
  if (!product.value) return
  
  try {
    // 这里应该调用实际的API获取审核历史
    // 暂时使用模拟数据
    auditHistory.value = [
      {
        status: 'PENDING',
        comment: '商品信息完整，等待审核',
        approvalAt: new Date().toISOString(),
        approverName: '系统'
      }
    ]
  } catch (error) {
    console.error('加载审核历史失败:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value || !product.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const auditData = {
      productId: product.value.id,
      approvalStatus: auditForm.approvalStatus,
      approvalComment: auditForm.approvalComment,
      approverId: 1 // 这里应该传入实际的用户ID
    }
    
    await auditProduct(product.value.id, auditData)
    
    ElMessage.success('审核提交成功')
    emit('success')
  } catch (error) {
    console.error('审核提交失败:', error)
    ElMessage.error('审核提交失败，请检查信息')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

// 监听审核状态变化
const handleApprovalStatusChange = () => {
  if (auditForm.approvalStatus === 'APPROVED') {
    auditForm.approvalComment = ''
  }
}

// 生命周期
onMounted(() => {
  loadAuditHistory()
})
</script>

<style scoped>
.product-audit {
  padding: 0;
}

.audit-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-info,
.audit-form,
.audit-history {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 12px;
  padding: 20px;
}

.info-title,
.form-title,
.history-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--md-sys-color-outline-variant);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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

.audit-form .el-form {
  margin: 0;
}

.audit-form .el-radio-group {
  display: flex;
  gap: 24px;
}

.audit-form .el-radio {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: 1px solid var(--md-sys-color-outline-variant);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.audit-form .el-radio:hover {
  border-color: var(--md-sys-color-primary);
  box-shadow: var(--md-sys-elevation-level1);
}

.audit-form .el-radio.is-checked {
  border-color: var(--md-sys-color-primary);
  background: var(--md-sys-color-primary-container);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  background: var(--md-sys-color-surface);
  border-radius: 8px;
  padding: 16px;
  border: 1px solid var(--md-sys-color-outline-variant);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-time {
  font-size: 12px;
  color: var(--md-sys-color-on-surface-variant);
}

.history-content {
  color: var(--md-sys-color-on-surface);
  line-height: 1.6;
  margin-bottom: 12px;
  padding: 12px;
  background: var(--md-sys-color-surface-container);
  border-radius: 6px;
}

.history-footer {
  display: flex;
  justify-content: flex-end;
}

.history-approver {
  font-size: 12px;
  color: var(--md-sys-color-on-surface-variant);
}

.audit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid var(--md-sys-color-outline-variant);
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
  
  .audit-form .el-radio-group {
    flex-direction: column;
    gap: 16px;
  }
  
  .audit-form .el-radio {
    width: 100%;
    justify-content: center;
  }
  
  .audit-actions {
    flex-direction: column;
  }
  
  .audit-actions .el-button {
    width: 100%;
  }
}
</style>
