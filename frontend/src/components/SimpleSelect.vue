<template>
  <div class="simple-select" ref="triggerRef">
    <div
      class="simple-select__trigger"
      :class="{ 'is-disabled': disabled, 'is-open': isOpen }"
      @click="onTriggerClick"
      @keydown.stop.prevent="onTriggerKeydown"
      tabindex="0"
      role="combobox"
      :aria-expanded="isOpen ? 'true' : 'false'"
      aria-haspopup="listbox"
    >
      <span v-if="displayLabel" class="simple-select__label">{{ displayLabel }}</span>
      <span v-else class="simple-select__placeholder">{{ placeholder }}</span>
      <button
        v-if="clearable && modelValue !== null && modelValue !== undefined && modelValue !== '' && !disabled"
        class="simple-select__clear"
        type="button"
        @click.stop="clearSelection"
        aria-label="clear"
      >×</button>
      <span class="simple-select__arrow">▾</span>
    </div>

    <teleport to="body">
      <div
        v-if="isOpen"
        class="simple-select__dropdown"
        :style="dropdownStyle"
        role="listbox"
        ref="dropdownRef"
        @keydown.stop.prevent="onDropdownKeydown"
      >
        <div v-if="normalizedOptions.length === 0" class="simple-select__empty">无可选项</div>
        <div
          v-for="(opt, idx) in normalizedOptions"
          :key="opt.value + ''"
          class="simple-select__option"
          :class="{ 'is-selected': isSelected(opt.value), 'is-disabled': !!opt.disabled, 'is-hover': hoverIndex === idx }"
          role="option"
          :aria-selected="isSelected(opt.value) ? 'true' : 'false'"
          @mouseenter="hoverIndex = idx"
          @mouseleave="hoverIndex = -1"
          @click.stop="onOptionClick(opt)"
        >
          <span class="simple-select__option-label">{{ opt.label }}</span>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

interface OptionItem {
  label: string
  value: string | number
  disabled?: boolean
}

const props = defineProps<{
  modelValue: string | number | null | undefined
  options: OptionItem[]
  placeholder?: string
  clearable?: boolean
  disabled?: boolean
  zIndex?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number | null): void
  (e: 'change', value: string | number | null): void
  (e: 'open'): void
  (e: 'close'): void
}>()

const placeholder = computed(() => props.placeholder ?? '请选择')
const clearable = computed(() => props.clearable ?? true)
const disabled = computed(() => props.disabled ?? false)
const zIndex = computed(() => props.zIndex ?? 10000)

const isOpen = ref(false)
const triggerRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)
const triggerRect = ref<DOMRect | null>(null)
const hoverIndex = ref<number>(-1)

const normalizedOptions = computed<OptionItem[]>(() => props.options ?? [])

const displayLabel = computed<string | undefined>(() => {
  const current = normalizedOptions.value.find(o => String(o.value) === String(props.modelValue))
  if (current) { return current.label }
  if (props.modelValue !== null && props.modelValue !== undefined && props.modelValue !== '') {
    return '已选择(外部)'
  }
  return undefined
})

const dropdownStyle = computed(() => {
  const rect = triggerRect.value
  const width = rect ? rect.width : 0
  const top = rect ? rect.bottom : 0
  const left = rect ? rect.left : 0
  return {
    position: 'fixed',
    top: `${top}px`,
    left: `${left}px`,
    width: `${width}px`,
    zIndex: String(zIndex.value)
  } as Record<string, string>
})

function open() {
  if (disabled.value) return
  updateTriggerRect()
  isOpen.value = true
  hoverIndex.value = getSelectedIndex()
  nextTick(() => {
    bindGlobal()
    emit('open')
  })
}

function close() {
  if (!isOpen.value) return
  isOpen.value = false
  unbindGlobal()
  emit('close')
}

function toggle() { isOpen.value ? close() : open() }

function onTriggerClick() { toggle() }

function onOptionClick(opt: OptionItem) {
  if (opt.disabled) return
  emit('update:modelValue', opt.value)
  emit('change', opt.value)
  close()
}

function clearSelection() {
  emit('update:modelValue', null)
  emit('change', null)
}

function isSelected(v: string | number) {
  return String(v) === String(props.modelValue)
}

function updateTriggerRect() {
  const el = triggerRef.value
  if (!el) return
  triggerRect.value = el.getBoundingClientRect()
}

function onWindowChange() {
  if (!isOpen.value) return
  updateTriggerRect()
}

function onGlobalClick(e: MouseEvent) {
  const t = e.target as Node
  if (!t) return
  const triggerEl = triggerRef.value
  const dropEl = dropdownRef.value
  if (triggerEl && triggerEl.contains(t)) return
  if (dropEl && dropEl.contains(t)) return
  close()
}

function bindGlobal() {
  window.addEventListener('scroll', onWindowChange, true)
  window.addEventListener('resize', onWindowChange)
  window.addEventListener('click', onGlobalClick, true)
}

function unbindGlobal() {
  window.removeEventListener('scroll', onWindowChange, true)
  window.removeEventListener('resize', onWindowChange)
  window.removeEventListener('click', onGlobalClick, true)
}

function onTriggerKeydown(e: KeyboardEvent) {
  if (disabled.value) return
  if (e.key === 'Enter' || e.key === ' ') {
    toggle()
  } else if (e.key === 'ArrowDown') {
    if (!isOpen.value) open(); else moveHover(1)
  } else if (e.key === 'ArrowUp') {
    if (!isOpen.value) open(); else moveHover(-1)
  } else if (e.key === 'Escape') {
    close()
  }
}

function onDropdownKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    const item = normalizedOptions.value[hoverIndex.value]
    if (item && !item.disabled) onOptionClick(item)
  } else if (e.key === 'ArrowDown') {
    moveHover(1)
  } else if (e.key === 'ArrowUp') {
    moveHover(-1)
  } else if (e.key === 'Escape') {
    close()
  }
}

function moveHover(delta: number) {
  if (normalizedOptions.value.length === 0) return
  let i = hoverIndex.value
  for (let k = 0; k < normalizedOptions.value.length; k++) {
    i = (i + delta + normalizedOptions.value.length) % normalizedOptions.value.length
    if (!normalizedOptions.value[i].disabled) { hoverIndex.value = i; break }
  }
}

function getSelectedIndex() {
  const idx = normalizedOptions.value.findIndex(o => String(o.value) === String(props.modelValue))
  return idx >= 0 ? idx : 0
}

watch(() => props.modelValue, () => { /* no-op for now */ })

onMounted(() => { /* noop */ })
onBeforeUnmount(() => { unbindGlobal() })
</script>

<style scoped>
.simple-select {
  width: 100%;
}
.simple-select__trigger {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 32px;
  line-height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 0 10px;
  background: #fff;
  cursor: pointer;
  outline: none;
}
.simple-select__trigger.is-disabled {
  background: #f5f7fa;
  cursor: not-allowed;
  color: #c0c4cc;
}
.simple-select__label { color: #303133; }
.simple-select__placeholder { color: #909399; }
.simple-select__arrow { margin-left: 6px; color: #909399; }
.simple-select__clear {
  margin-left: auto;
  margin-right: 4px;
  border: none;
  background: transparent;
  color: #909399;
  cursor: pointer;
}

.simple-select__dropdown {
  position: fixed;
  max-height: 260px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.simple-select__empty {
  padding: 8px 10px;
  color: #909399;
}
.simple-select__option {
  padding: 8px 10px;
  cursor: pointer;
}
.simple-select__option.is-hover { background: #f5f7fa; }
.simple-select__option.is-selected { color: #409eff; }
.simple-select__option.is-disabled { color: #c0c4cc; cursor: not-allowed; }
</style>


