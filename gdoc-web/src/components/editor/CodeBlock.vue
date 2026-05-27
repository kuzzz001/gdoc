<template>
  <div class="code-block" :class="{ 'is-expanded': isExpanded }">
    <div class="code-header">
      <span class="code-lang">{{ language }}</span>
      <div class="code-actions">
        <button class="code-action" title="复制代码" @click="copyCode">
          <span v-if="!copied">📋</span>
          <span v-else class="copied">✓</span>
        </button>
        <button v-if="canExpand" class="code-action" title="展开/收起" @click="toggleExpand">
          {{ isExpanded ? '收起' : '展开' }}
        </button>
      </div>
    </div>
    <div class="code-content">
      <pre><code :class="`language-${language}`">{{ displayCode }}</code></pre>
    </div>
    <div v-if="showLineNumbers" class="line-numbers">
      <span v-for="n in totalLines" :key="n" class="line-number">{{ n }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = withDefaults(
  defineProps<{
    code: string
    language?: string
    showLineNumbers?: boolean
    maxLines?: number
    filename?: string
  }>(),
  {
    language: 'plaintext',
    showLineNumbers: true,
    maxLines: 15,
    filename: '',
  }
)

const isExpanded = ref(false)
const copied = ref(false)

const lines = computed(() => props.code.split('\n'))
const totalLines = computed(() => lines.value.length)
const canExpand = computed(() => totalLines.value > props.maxLines)

const displayCode = computed(() => {
  if (canExpand.value && !isExpanded.value) {
    return lines.value.slice(0, props.maxLines).join('\n') + '\n...'
  }
  return props.code
})

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

async function copyCode() {
  try {
    await navigator.clipboard.writeText(props.code)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch {
    console.error('复制失败')
  }
}
</script>

<style scoped lang="scss">
.code-block {
  position: relative;
  background: var(--code-bg, #1e1e1e);
  border-radius: var(--radius-md);
  overflow: hidden;
  margin: 12px 0;
  font-family: 'Fira Code', 'JetBrains Mono', 'Consolas', monospace;
}

.code-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: var(--code-header-bg, #2d2d2d);
  border-bottom: 1px solid var(--border-color, #3d3d3d);
}

.code-lang {
  font-size: 12px;
  color: var(--text-secondary);
  text-transform: uppercase;
  font-weight: 500;
}

.code-actions {
  display: flex;
  gap: 8px;
}

.code-action {
  padding: 4px 10px;
  background: transparent;
  border: 1px solid var(--border-color, #3d3d3d);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: var(--bg-tertiary, #3d3d3d);
    color: var(--text-primary);
  }

  .copied {
    color: var(--success);
  }
}

.code-content {
  position: relative;
  padding: 16px;
  overflow-x: auto;

  pre {
    margin: 0;
    padding: 0;
  }

  code {
    display: block;
    font-size: 13px;
    line-height: 1.6;
    color: var(--code-color, #d4d4d4);
    white-space: pre;
  }
}

.line-numbers {
  position: absolute;
  left: 0;
  top: 52px;
  bottom: 0;
  width: 40px;
  padding: 16px 8px;
  background: var(--code-header-bg, #2d2d2d);
  border-right: 1px solid var(--border-color, #3d3d3d);
  text-align: right;
  user-select: none;
  pointer-events: none;

  .line-number {
    display: block;
    font-size: 12px;
    line-height: 1.6;
    color: var(--text-placeholder);
  }
}

.code-content {
  padding-left: 50px;
}

.code-block:not(.show-line-numbers) {
  .line-numbers {
    display: none;
  }

  .code-content {
    padding-left: 16px;
  }
}
</style>
