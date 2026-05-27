<template>
  <div class="version-diff">
    <div class="diff-header">
      <h3>版本对比</h3>
      <button class="btn-close" @click="$emit('close')">&times;</button>
    </div>
    <div class="diff-controls">
      <select v-model="leftVersion" class="version-select">
        <option v-for="v in versions" :key="v.version" :value="v.version">
          v{{ v.version }} - {{ formatTime(v.createdAt) }}
        </option>
      </select>
      <span class="diff-arrow">→</span>
      <select v-model="rightVersion" class="version-select">
        <option v-for="v in versions" :key="v.version" :value="v.version">
          v{{ v.version }} - {{ formatTime(v.createdAt) }}
        </option>
      </select>
    </div>
    <div class="diff-content">
      <div class="diff-pane left-pane">
        <div class="pane-header">v{{ leftVersion }}</div>
        <div class="pane-body" v-html="leftHtml"></div>
      </div>
      <div class="diff-pane right-pane">
        <div class="pane-header">v{{ rightVersion }}</div>
        <div class="pane-body" v-html="rightHtml"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Version {
  version: number
  content: string
  createdAt: string
}

const props = defineProps<{
  versions: Version[]
}>()

defineEmits<{ close: [] }>()

const leftVersion = ref(props.versions.length > 1 ? props.versions[props.versions.length - 2].version : 0)
const rightVersion = ref(props.versions.length > 0 ? props.versions[props.versions.length - 1].version : 0)

const leftContent = computed(() => {
  const v = props.versions.find(v => v.version === leftVersion.value)
  return v?.content || ''
})

const rightContent = computed(() => {
  const v = props.versions.find(v => v.version === rightVersion.value)
  return v?.content || ''
})

const leftHtml = computed(() => computeDiff(leftContent.value, rightContent.value, 'left'))
const rightHtml = computed(() => computeDiff(leftContent.value, rightContent.value, 'right'))

function computeDiff(left: string, right: string, side: 'left' | 'right'): string {
  const leftLines = left.split('\n')
  const rightLines = right.split('\n')
  const maxLen = Math.max(leftLines.length, rightLines.length)
  const result: string[] = []

  for (let i = 0; i < maxLen; i++) {
    const lLine = leftLines[i] ?? ''
    const rLine = rightLines[i] ?? ''

    if (lLine === rLine) {
      result.push(escapeHtml(lLine))
    } else if (side === 'left' && lLine !== rLine) {
      result.push(`<span class="diff-removed">${escapeHtml(lLine)}</span>`)
    } else if (side === 'right' && lLine !== rLine) {
      result.push(`<span class="diff-added">${escapeHtml(rLine)}</span>`)
    }
  }

  return result.join('<br>')
}

function escapeHtml(str: string): string {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.version-diff {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 20px;
  width: 800px;
  max-height: 80vh;
  overflow-y: auto;
}

.diff-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.diff-header h3 { margin: 0; }

.btn-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
}

.diff-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.version-select {
  flex: 1;
  padding: 6px 8px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  font-size: 13px;
}

.diff-arrow {
  color: var(--text-secondary);
  font-size: 18px;
}

.diff-content {
  display: flex;
  gap: 12px;
}

.diff-pane {
  flex: 1;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.pane-header {
  background: var(--bg-secondary);
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
}

.pane-body {
  padding: 12px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

:deep(.diff-removed) {
  background: rgba(255, 0, 0, 0.15);
  text-decoration: line-through;
}

:deep(.diff-added) {
  background: rgba(0, 255, 0, 0.15);
}
</style>