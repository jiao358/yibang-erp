<template>
  <div class="ai-config-panel">
    <el-form 
      :model="config" 
      label-width="140px" 
      class="config-form"
      @change="handleConfigChange"
    >
      <!-- AI模型选择 -->
      <el-form-item label="AI模型类型">
        <el-select v-model="config.modelType" placeholder="选择AI模型">
          <el-option label="DeepSeek" value="deepseek" />
          <el-option label="GPT-4" value="gpt4" />
          <el-option label="Claude" value="claude" />
        </el-select>
        <div class="form-tip">选择用于字段识别的AI模型</div>
      </el-form-item>

      <!-- 置信度阈值 -->
      <el-form-item label="置信度阈值">
        <el-slider
          v-model="config.confidenceThreshold"
          :min="0.1"
          :max="1.0"
          :step="0.1"
          :format-tooltip="formatConfidence"
          show-input
          input-size="small"
        />
        <div class="form-tip">
          设置AI识别的置信度阈值，低于此值将标记为需手动处理
        </div>
      </el-form-item>

      <!-- 自动匹配策略 -->
      <el-form-item label="匹配策略">
        <el-radio-group v-model="config.autoMatchStrategy">
          <el-radio label="strict">严格匹配</el-radio>
          <el-radio label="smart">智能匹配</el-radio>
          <el-radio label="loose">宽松匹配</el-radio>
        </el-radio-group>
        <div class="form-tip">
          选择商品和客户匹配的严格程度
        </div>
      </el-form-item>

      <!-- 启用备选方案 -->
      <el-form-item label="启用备选方案">
        <el-switch v-model="config.enableFallback" />
        <div class="form-tip">
          当AI识别失败时，是否启用规则识别作为备选方案
        </div>
      </el-form-item>

      <!-- 最大重试次数 -->
      <el-form-item label="最大重试次数">
        <el-input-number
          v-model="config.maxRetries"
          :min="1"
          :max="5"
          :step="1"
          size="small"
        />
        <div class="form-tip">
          设置AI识别失败时的最大重试次数
        </div>
      </el-form-item>

      <!-- 高级配置 -->
      <el-form-item label="高级配置">
        <el-collapse v-model="activeAdvancedConfig">
          <el-collapse-item title="高级参数设置" name="advanced">
            <div class="advanced-config">
              <!-- 模型参数 -->
              <el-form-item label="温度参数">
                <el-slider
                  v-model="config.temperature"
                  :min="0.0"
                  :max="1.0"
                  :step="0.1"
                  show-input
                  input-size="small"
                />
                <div class="form-tip">控制AI输出的随机性，0为确定性，1为随机性</div>
              </el-form-item>

              <!-- 最大Token数 -->
              <el-form-item label="最大Token数">
                <el-input-number
                  v-model="config.maxTokens"
                  :min="100"
                  :max="4000"
                  :step="100"
                  size="small"
                />
                <div class="form-tip">限制AI响应的最大长度</div>
              </el-form-item>

              <!-- 超时设置 -->
              <el-form-item label="请求超时(秒)">
                <el-input-number
                  v-model="config.timeout"
                  :min="10"
                  :max="120"
                  :step="10"
                  size="small"
                />
                <div class="form-tip">设置AI请求的超时时间</div>
              </el-form-item>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-form-item>

      <!-- 预设配置 -->
      <el-form-item label="预设配置">
        <el-button-group>
          <el-button 
            size="small" 
            @click="loadPreset('fast')"
            :type="currentPreset === 'fast' ? 'primary' : ''"
          >
            快速模式
          </el-button>
          <el-button 
            size="small" 
            @click="loadPreset('balanced')"
            :type="currentPreset === 'balanced' ? 'primary' : ''"
          >
            平衡模式
          </el-button>
          <el-button 
            size="small" 
            @click="loadPreset('accurate')"
            :type="currentPreset === 'accurate' ? 'primary' : ''"
          >
            精确模式
          </el-button>
        </el-button-group>
        <div class="form-tip">
          选择预设配置快速设置参数
        </div>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" @click="saveConfig">保存配置</el-button>
        <el-button @click="resetConfig">重置配置</el-button>
        <el-button @click="exportConfig">导出配置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { AIExcelConfig } from '@/types/ai'

// Props
interface Props {
  config: AIExcelConfig
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:config': [config: AIExcelConfig]
  configChange: [config: AIExcelConfig]
}>()

// 响应式数据
const activeAdvancedConfig = ref(['advanced'])
const currentPreset = ref('balanced')

// 本地配置副本
const localConfig = reactive<AIExcelConfig>({
  ...props.config,
  temperature: props.config.temperature || 0.1,
  maxTokens: props.config.maxTokens || 2000,
  timeout: props.config.timeout || 30
})

// 预设配置
const presets = {
  fast: {
    confidenceThreshold: 0.6,
    autoMatchStrategy: 'loose',
    enableFallback: true,
    maxRetries: 2,
    temperature: 0.3,
    maxTokens: 1500,
    timeout: 20
  },
  balanced: {
    confidenceThreshold: 0.8,
    autoMatchStrategy: 'smart',
    enableFallback: true,
    maxRetries: 3,
    temperature: 0.1,
    maxTokens: 2000,
    timeout: 30
  },
  accurate: {
    confidenceThreshold: 0.9,
    autoMatchStrategy: 'strict',
    enableFallback: false,
    maxRetries: 5,
    temperature: 0.0,
    maxTokens: 3000,
    timeout: 60
  }
}

// 监听配置变化
watch(localConfig, (newConfig) => {
  emit('update:config', newConfig)
  emit('configChange', newConfig)
}, { deep: true })

// 方法
const formatConfidence = (value: number) => {
  return `${(value * 100).toFixed(0)}%`
}

const handleConfigChange = () => {
  emit('configChange', localConfig)
}

const loadPreset = (presetName: keyof typeof presets) => {
  const preset = presets[presetName]
  Object.assign(localConfig, preset)
  currentPreset.value = presetName
  
  ElMessage.success(`已加载${presetName === 'fast' ? '快速' : presetName === 'balanced' ? '平衡' : '精确'}模式配置`)
}

const saveConfig = () => {
  // TODO: 保存配置到后端或本地存储
  ElMessage.success('配置已保存')
}

const resetConfig = () => {
  Object.assign(localConfig, presets.balanced)
  currentPreset.value = 'balanced'
  ElMessage.info('配置已重置为平衡模式')
}

const exportConfig = () => {
  const configStr = JSON.stringify(localConfig, null, 2)
  const blob = new Blob([configStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'ai-config.json'
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('配置已导出')
}
</script>

<style scoped>
.ai-config-panel {
  width: 100%;
}

.config-form {
  max-width: 600px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.advanced-config {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
  margin-top: 16px;
}

.advanced-config .el-form-item {
  margin-bottom: 16px;
}

.advanced-config .el-form-item:last-child {
  margin-bottom: 0;
}

.el-button-group {
  margin-right: 16px;
}

.el-collapse {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.el-collapse-item {
  border-bottom: 1px solid #e4e7ed;
}

.el-collapse-item:last-child {
  border-bottom: none;
}
</style>
