<template>
  <div class="ai-config-panel">
    <!-- 配置状态卡片 -->
    <div class="status-card">
      <div class="status-header">
        <h3 class="status-title">AI服务状态</h3>
        <el-tag :type="configStatus.type" size="large">
          {{ configStatus.text }}
        </el-tag>
      </div>
      <div class="status-info">
        <div class="status-item">
          <span class="label">配置状态：</span>
          <span class="value">{{ configStatus.configText }}</span>
        </div>
        <div class="status-item">
          <span class="label">连接状态：</span>
          <span class="value">{{ configStatus.connectionText }}</span>
        </div>
        <div class="status-item">
          <span class="label">最后测试：</span>
          <span class="value">{{ configStatus.lastTestText }}</span>
        </div>
      </div>
      <div class="status-actions">
        <el-button type="primary" @click="testConnection" :loading="testing">
          <el-icon><Connection /></el-icon>
          测试连接
        </el-button>
        <el-button type="success" @click="refreshConfig">
          <el-icon><Refresh /></el-icon>
          刷新状态
        </el-button>
      </div>
    </div>

    <!-- 配置表单 -->
    <div class="config-form-card">
      <div class="form-header">
        <h3 class="form-title">DeepSeek API配置</h3>
        <p class="form-subtitle">配置AI模型参数，只有系统管理员可以修改</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="configForm"
        :rules="formRules"
        label-width="140px"
        class="config-form"
      >
        <div class="form-section">
          <h4 class="section-title">基础配置</h4>
          <div class="form-grid">
            <el-form-item label="API基础URL" prop="baseUrl">
              <el-input
                v-model="configForm.baseUrl"
                placeholder="https://api.deepseek.com"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="API密钥" prop="apiKey">
              <el-input
                v-model="configForm.apiKey"
                type="password"
                placeholder="请输入DeepSeek API密钥"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item label="默认模型" prop="defaultModel">
              <el-input
                v-model="configForm.defaultModel"
                placeholder="deepseek-chat"
                clearable
              />
            </el-form-item>
          </div>
        </div>
        
        <div class="form-section">
          <h4 class="section-title">模型参数</h4>
          <div class="form-grid">
            <el-form-item label="最大Token数" prop="maxTokens">
              <el-input-number
                v-model="configForm.maxTokens"
                :min="100"
                :max="100000"
                :step="100"
                style="width: 100%"
                placeholder="4096"
              />
            </el-form-item>
            
            <el-form-item label="温度参数" prop="temperature">
              <el-slider
                v-model="configForm.temperature"
                :min="0"
                :max="2"
                :step="0.1"
                :show-tooltip="true"
                :format-tooltip="(val) => val.toFixed(1)"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="超时时间(ms)" prop="timeout">
              <el-input-number
                v-model="configForm.timeout"
                :min="5000"
                :max="300000"
                :step="1000"
                style="width: 100%"
                placeholder="30000"
              />
            </el-form-item>
          </div>
        </div>
        
        <div class="form-section">
          <h4 class="section-title">功能开关</h4>
          <div class="form-grid">
            <el-form-item label="启用AI功能">
              <el-switch
                v-model="configForm.enabled"
                active-text="启用"
                inactive-text="禁用"
                active-color="var(--md-sys-color-success)"
              />
            </el-form-item>
          </div>
        </div>
        
        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="saveConfig" :loading="saving">
            保存配置
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 配置说明 -->
    <div class="config-info-card">
      <div class="info-header">
        <h3 class="info-title">配置说明</h3>
        <el-icon class="info-icon"><InfoFilled /></el-icon>
      </div>
      <div class="info-content">
        <div class="info-item">
          <h4>API密钥获取</h4>
          <p>请访问 <a href="https://platform.deepseek.com" target="_blank">DeepSeek平台</a> 获取您的API密钥</p>
        </div>
        <div class="info-item">
          <h4>模型参数说明</h4>
          <p><strong>温度参数：</strong>控制输出的随机性，值越低输出越确定，值越高输出越随机</p>
          <p><strong>最大Token：</strong>限制AI响应的最大长度，建议根据业务需求设置</p>
        </div>
        <div class="info-item">
          <h4>安全提醒</h4>
          <p>API密钥具有访问您账户的权限，请妥善保管，不要泄露给他人</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Connection, Refresh, InfoFilled } from '@element-plus/icons-vue'
import { getAIConfig, updateAIConfig, testAIConnection } from '@/api/ai'
import type { AIConfigResponse, AIConfigRequest } from '@/types/ai'

// 响应式数据
const formRef = ref<FormInstance>()
const testing = ref(false)
const saving = ref(false)
const configData = ref<AIConfigResponse | null>(null)

// 配置表单
const configForm = reactive({
  baseUrl: '',
  apiKey: '',
  defaultModel: '',
  maxTokens: 4096,
  temperature: 0.7,
  timeout: 30000,
  enabled: false
})

// 表单验证规则
const formRules: FormRules = {
  baseUrl: [
    { required: true, message: '请输入API基础URL', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL格式', trigger: 'blur' }
  ],
  apiKey: [
    { required: true, message: '请输入API密钥', trigger: 'blur' },
    { min: 10, message: 'API密钥长度不能少于10个字符', trigger: 'blur' }
  ],
  defaultModel: [
    { required: true, message: '请输入默认模型名称', trigger: 'blur' }
  ],
  maxTokens: [
    { required: true, message: '请设置最大Token数', trigger: 'blur' },
    { type: 'number', min: 100, max: 100000, message: 'Token数必须在100-100000之间', trigger: 'blur' }
  ],
  temperature: [
    { required: true, message: '请设置温度参数', trigger: 'blur' },
    { type: 'number', min: 0, max: 2, message: '温度参数必须在0-2之间', trigger: 'blur' }
  ],
  timeout: [
    { required: true, message: '请设置超时时间', trigger: 'blur' },
    { type: 'number', min: 5000, max: 300000, message: '超时时间必须在5000-300000毫秒之间', trigger: 'blur' }
  ]
}

// 计算属性
const configStatus = computed(() => {
  if (!configData.value) {
    return {
      type: 'info',
      text: '未配置',
      configText: '未配置',
      connectionText: '未知',
      lastTestText: '未测试'
    }
  }
  
  const isConfigured = configData.value.configured
  const isEnabled = configData.value.enabled
  const lastTestResult = configData.value.lastTestResult
  
  let type = 'info'
  let text = '未配置'
  
  if (isConfigured && isEnabled) {
    if (lastTestResult === true) {
      type = 'success'
      text = '运行正常'
    } else if (lastTestResult === false) {
      type = 'warning'
      text = '连接异常'
    } else {
      type = 'warning'
      text = '未测试'
    }
  } else if (isConfigured && !isEnabled) {
    type = 'info'
    text = '已禁用'
  }
  
  return {
    type,
    text,
    configText: isConfigured ? '已配置' : '未配置',
    connectionText: lastTestResult === true ? '正常' : lastTestResult === false ? '异常' : '未知',
    lastTestText: configData.value.lastTestTime ? new Date(configData.value.lastTestTime).toLocaleString('zh-CN') : '未测试'
  }
})

// 方法
const loadConfig = async () => {
  try {
    const response = await getAIConfig()
    configData.value = response.data
    
    // 填充表单
    Object.assign(configForm, {
      baseUrl: response.data.baseUrl || '',
      apiKey: '', // 不显示实际密钥
      defaultModel: response.data.defaultModel || '',
      maxTokens: response.data.maxTokens || 4096,
      temperature: response.data.temperature || 0.7,
      timeout: response.data.timeout || 30000,
      enabled: response.data.enabled || false
    })
  } catch (error) {
    ElMessage.error('加载配置失败')
    console.error('加载配置失败:', error)
  }
}

const testConnection = async () => {
  testing.value = true
  try {
    const response = await testAIConnection()
    if (response.data) {
      ElMessage.success('AI连接测试成功')
      await loadConfig() // 刷新配置状态
    } else {
      ElMessage.warning('AI连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
    console.error('连接测试失败:', error)
  } finally {
    testing.value = false
  }
}

const saveConfig = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    const request: AIConfigRequest = {
      ...configForm,
      configUserId: 1 // 这里应该传入实际的用户ID
    }
    
    await updateAIConfig(request)
    ElMessage.success('配置保存成功')
    await loadConfig() // 刷新配置
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  if (configData.value) {
    Object.assign(configForm, {
      baseUrl: configData.value.baseUrl || '',
      apiKey: '',
      defaultModel: configData.value.defaultModel || '',
      maxTokens: configData.value.maxTokens || 4096,
      temperature: configData.value.temperature || 0.7,
      timeout: configData.value.timeout || 30000,
      enabled: configData.value.enabled || false
    })
  }
}

const refreshConfig = () => {
  loadConfig()
}

// 生命周期
onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.ai-config-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.status-card,
.config-form-card,
.config-info-card {
  background: var(--md-sys-color-surface-container-low);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--md-sys-elevation-level1);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0;
}

.status-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-item .label {
  font-weight: 500;
  color: var(--md-sys-color-on-surface-variant);
  min-width: 80px;
}

.status-item .value {
  color: var(--md-sys-color-on-surface);
}

.status-actions {
  display: flex;
  gap: 12px;
}

.form-header {
  margin-bottom: 24px;
}

.form-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: var(--md-sys-color-on-surface-variant);
  margin: 0;
}

.config-form {
  margin: 0;
}

.form-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--md-sys-color-outline-variant);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid var(--md-sys-color-outline-variant);
}

.info-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.info-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0;
}

.info-icon {
  font-size: 24px;
  color: var(--md-sys-color-primary);
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--md-sys-color-on-surface);
  margin: 0 0 8px 0;
}

.info-item p {
  color: var(--md-sys-color-on-surface-variant);
  line-height: 1.6;
  margin: 0;
}

.info-item a {
  color: var(--md-sys-color-primary);
  text-decoration: none;
}

.info-item a:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .status-info {
    grid-template-columns: 1fr;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
  
  .status-actions {
    flex-direction: column;
  }
  
  .status-actions .el-button {
    width: 100%;
  }
}
</style>
