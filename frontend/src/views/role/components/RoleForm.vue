<template>
  <el-dialog
    v-model="dialogVisible"
    :title="getDialogTitle()"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="角色名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入角色名称"
          :disabled="isViewMode || (isEdit && isSystemRole && !hasAdminPermission)"
        />
      </el-form-item>

      <el-form-item label="角色描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入角色描述"
          :disabled="isViewMode || (isEdit && isSystemRole && !hasAdminPermission)"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select
          v-model="form.status"
          placeholder="请选择状态"
          style="width: 100%"
          :disabled="isViewMode || (isEdit && isSystemRole && !hasAdminPermission)"
        >
          <el-option label="激活" value="ACTIVE" />
          <el-option label="未激活" value="INACTIVE" />
        </el-select>
      </el-form-item>

      <el-form-item label="权限配置" prop="permissions">
        <el-input
          v-model="form.permissions"
          type="textarea"
          :rows="4"
          placeholder="请输入权限配置（JSON格式）"
          :disabled="isViewMode"
        />
        <div class="form-tip">
          <small>权限配置使用JSON格式，例如：{"menu": ["read", "write"], "user": ["read"]}</small>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">{{ isViewMode ? '关闭' : '取消' }}</el-button>
        <el-button 
          v-if="!isViewMode"
          type="primary" 
          @click="handleSubmit" 
          :loading="submitting"
        >
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createRole, updateRole } from '@/api/role'
import type { Role } from '@/types/role'

// Props
interface Props {
  visible: boolean
  roleData?: Role | null
  isViewMode?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  roleData: null,
  isViewMode: false
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const form = reactive({
  name: '',
  description: '',
  permissions: '',
  status: 'ACTIVE'
})

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '角色名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: false, message: '请输入角色描述', trigger: 'blur' },
    { max: 200, message: '角色描述不能超过 200 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const isEdit = computed(() => !!props.roleData)

const isSystemRole = computed(() => props.roleData?.isSystem || false)

// 获取对话框标题
const getDialogTitle = () => {
  if (props.isViewMode) {
    return '查看角色'
  }
  return isEdit.value ? '编辑角色' : '新增角色'
}

// 检查是否有管理员权限
const hasAdminPermission = computed(() => {
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

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    name: '',
    description: '',
    permissions: '',
    status: 'ACTIVE'
  })
  formRef.value?.clearValidate()
}

// 填充表单数据
const fillFormData = (roleData: Role) => {
  Object.assign(form, {
    name: roleData.name || '',
    description: roleData.description || '',
    permissions: roleData.permissionsConfig || roleData.permissions || '',
    status: roleData.status || 'ACTIVE'
  })
}

// 监听角色数据变化
watch(() => props.roleData, (newVal) => {
  if (newVal) {
    fillFormData(newVal)
    console.log('RoleForm - 填充表单数据:', newVal)
    console.log('RoleForm - 表单数据:', form)
  } else {
    resetForm()
  }
}, { immediate: true })

// 监听visible变化，确保数据正确填充
watch(() => props.visible, (newVal) => {
  if (newVal && props.roleData) {
    // 延迟一下确保DOM已经渲染
    nextTick(() => {
      fillFormData(props.roleData!)
      console.log('RoleForm - visible变化，重新填充数据:', props.roleData)
      console.log('RoleForm - 表单数据:', form)
    })
  }
})

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value && props.roleData) {
      // 编辑角色
      const updateData: any = { 
        name: form.name,
        description: form.description,
        permissionsConfig: form.permissions, // 映射到后端期望的字段名
        status: form.status
      }
      // 系统角色不允许修改名称和描述（除非是超级管理员）
      if (props.roleData.isSystem && !hasAdminPermission.value) {
        delete updateData.name
        delete updateData.description
      }
      await updateRole(props.roleData.id, updateData)
      ElMessage.success('更新成功')
    } else {
      // 新增角色
      const createData = {
        name: form.name,
        description: form.description,
        permissionsConfig: form.permissions, // 映射到后端期望的字段名
        status: form.status
      }
      await createRole(createData)
      ElMessage.success('创建成功')
    }
    
    emit('success')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      console.error('提交表单失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

// 组件挂载时获取数据
onMounted(() => {
  // 可以在这里获取权限配置模板等数据
})
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

.form-tip {
  margin-top: 5px;
  color: #909399;
}
</style>
