<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑销售目标' : '新建销售目标'"
    width="900px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="140px"
      class="sales-target-form"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="目标名称" prop="targetName">
            <el-input
              v-model="form.targetName"
              placeholder="请输入目标名称"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标类型" prop="targetType">
            <el-select
              v-model="form.targetType"
              placeholder="请选择目标类型"
              style="width: 100%"
            >
              <el-option label="GMV目标" value="GMV" />
              <el-option label="收入目标" value="REVENUE" />
              <el-option label="订单数量" value="ORDER_COUNT" />
              <el-option label="客户数量" value="CUSTOMER_COUNT" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="目标年份" prop="targetYear">
            <el-date-picker
              v-model="form.targetYear"
              type="year"
              placeholder="请选择目标年份"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标周期" prop="targetPeriod">
            <el-select
              v-model="form.targetPeriod"
              placeholder="请选择目标周期"
              style="width: 100%"
            >
              <el-option label="Q1" value="Q1" />
              <el-option label="Q2" value="Q2" />
              <el-option label="Q3" value="Q3" />
              <el-option label="Q4" value="Q4" />
              <el-option label="H1" value="H1" />
              <el-option label="H2" value="H2" />
              <el-option label="年度" value="YEARLY" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="周期开始日期" prop="periodStartDate">
            <el-date-picker
              v-model="form.periodStartDate"
              type="date"
              placeholder="请选择开始日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="周期结束日期" prop="periodEndDate">
            <el-date-picker
              v-model="form.periodEndDate"
              type="date"
              placeholder="请选择结束日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="GMV目标" prop="gmvTarget">
            <el-input-number
              v-model="form.gmvTarget"
              :min="0"
              :precision="2"
              placeholder="请输入GMV目标"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="收入目标" prop="revenueTarget">
            <el-input-number
              v-model="form.revenueTarget"
              :min="0"
              :precision="2"
              placeholder="请输入收入目标"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="订单数量目标" prop="orderCountTarget">
            <el-input-number
              v-model="form.orderCountTarget"
              :min="0"
              placeholder="请输入订单数量目标"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户数量目标" prop="customerCountTarget">
            <el-input-number
              v-model="form.customerCountTarget"
              :min="0"
              placeholder="请输入客户数量目标"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="佣金率" prop="commissionRate">
            <el-input-number
              v-model="form.commissionRate"
              :min="0"
              :max="1"
              :precision="4"
              :step="0.01"
              placeholder="请输入佣金率"
              style="width: 100%"
            >
              <template #suffix>%</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select
              v-model="form.status"
              placeholder="请选择状态"
              style="width: 100%"
            >
              <el-option label="草稿" value="DRAFT" />
              <el-option label="激活" value="ACTIVE" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="商品分类目标" prop="productCategoryTargets">
        <el-input
          v-model="form.productCategoryTargets"
          type="textarea"
          :rows="3"
          placeholder="请输入商品分类目标（JSON格式）"
          clearable
        />
        <div class="form-tip">支持JSON格式的商品分类目标配置</div>
      </el-form-item>

      <el-form-item label="地区目标" prop="regionalTargets">
        <el-input
          v-model="form.regionalTargets"
          type="textarea"
          :rows="3"
          placeholder="请输入地区目标（JSON格式）"
          clearable
        />
        <div class="form-tip">支持JSON格式的地区目标配置</div>
      </el-form-item>

      <el-form-item label="奖金结构" prop="bonusStructure">
        <el-input
          v-model="form.bonusStructure"
          type="textarea"
          :rows="3"
          placeholder="请输入奖金结构（JSON格式）"
          clearable
        />
        <div class="form-tip">支持JSON格式的奖金结构配置</div>
      </el-form-item>

      <el-form-item label="KPI指标" prop="kpiMetrics">
        <el-input
          v-model="form.kpiMetrics"
          type="textarea"
          :rows="3"
          placeholder="请输入KPI指标（JSON格式）"
          clearable
        />
        <div class="form-tip">支持JSON格式的KPI指标配置</div>
      </el-form-item>

      <el-form-item label="备注" prop="notes">
        <el-input
          v-model="form.notes"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
          clearable
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { salesTargetApi } from '@/api/salesTarget'

// Props
interface Props {
  modelValue: boolean
  salesTarget?: any
}

const props = withDefaults(defineProps<Props>(), {
  salesTarget: null
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive({
  targetName: '',
  targetType: 'GMV',
  targetYear: new Date().getFullYear(),
  targetPeriod: 'Q1',
  periodStartDate: '',
  periodEndDate: '',
  gmvTarget: null as number | null,
  revenueTarget: null as number | null,
  orderCountTarget: 0,
  customerCountTarget: 0,
  commissionRate: null as number | null,
  status: 'DRAFT',
  productCategoryTargets: '',
  regionalTargets: '',
  bonusStructure: '',
  kpiMetrics: '',
  notes: ''
})

// 表单验证规则
const rules: FormRules = {
  targetName: [
    { required: true, message: '请输入目标名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  targetType: [
    { required: true, message: '请选择目标类型', trigger: 'change' }
  ],
  targetYear: [
    { required: true, message: '请选择目标年份', trigger: 'change' }
  ],
  targetPeriod: [
    { required: true, message: '请选择目标周期', trigger: 'change' }
  ],
  periodStartDate: [
    { required: true, message: '请选择周期开始日期', trigger: 'change' }
  ],
  periodEndDate: [
    { required: true, message: '请选择周期结束日期', trigger: 'change' }
  ],
  gmvTarget: [
    { required: true, message: '请输入GMV目标', trigger: 'blur' },
    { type: 'number', min: 0, message: 'GMV目标必须大于等于0', trigger: 'blur' }
  ],
  revenueTarget: [
    { required: true, message: '请输入收入目标', trigger: 'blur' },
    { type: 'number', min: 0, message: '收入目标必须大于等于0', trigger: 'blur' }
  ],
  orderCountTarget: [
    { required: true, message: '请输入订单数量目标', trigger: 'blur' },
    { type: 'number', min: 0, message: '订单数量目标必须大于等于0', trigger: 'blur' }
  ],
  customerCountTarget: [
    { required: true, message: '请输入客户数量目标', trigger: 'blur' },
    { type: 'number', min: 0, message: '客户数量目标必须大于等于0', trigger: 'blur' }
  ]
}

// 计算属性
const isEdit = computed(() => !!props.salesTarget)

// 提交状态
const submitting = ref(false)

// 监听销售目标变化
watch(
  () => props.salesTarget,
  (newSalesTarget) => {
    if (newSalesTarget) {
      // 编辑模式，填充表单
      Object.assign(form, {
        targetName: newSalesTarget.targetName || '',
        targetType: newSalesTarget.targetType || 'GMV',
        targetYear: newSalesTarget.targetYear || new Date().getFullYear(),
        targetPeriod: newSalesTarget.targetPeriod || 'Q1',
        periodStartDate: newSalesTarget.periodStartDate || '',
        periodEndDate: newSalesTarget.periodEndDate || '',
        gmvTarget: newSalesTarget.gmvTarget || null,
        revenueTarget: newSalesTarget.revenueTarget || null,
        orderCountTarget: newSalesTarget.orderCountTarget || 0,
        customerCountTarget: newSalesTarget.customerCountTarget || 0,
        commissionRate: newSalesTarget.commissionRate || null,
        status: newSalesTarget.status || 'DRAFT',
        productCategoryTargets: newSalesTarget.productCategoryTargets || '',
        regionalTargets: newSalesTarget.regionalTargets || '',
        bonusStructure: newSalesTarget.bonusStructure || '',
        kpiMetrics: newSalesTarget.kpiMetrics || '',
        notes: newSalesTarget.notes || ''
      })
    } else {
      // 新建模式，重置表单
      resetForm()
    }
  },
  { immediate: true }
)

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    targetName: '',
    targetType: 'GMV',
    targetYear: new Date().getFullYear(),
    targetPeriod: 'Q1',
    periodStartDate: '',
    periodEndDate: '',
    gmvTarget: null,
    revenueTarget: null,
    orderCountTarget: 0,
    customerCountTarget: 0,
    commissionRate: null,
    status: 'DRAFT',
    productCategoryTargets: '',
    regionalTargets: '',
    bonusStructure: '',
    kpiMetrics: '',
    notes: ''
  })
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      // 更新
      await salesTargetApi.updateSalesTarget(props.salesTarget.id, form)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await salesTargetApi.createSalesTarget(form)
      ElMessage.success('创建成功')
    }

    emit('success')
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  } finally {
    submitting.value = false
  }
}

// 监听对话框显示状态
watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      nextTick(() => {
        formRef.value?.clearValidate()
      })
    }
  }
)
</script>

<style scoped>
.sales-target-form {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input-number .el-input__suffix) {
  color: #909399;
}
</style>
