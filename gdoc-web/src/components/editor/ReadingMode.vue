<template>
  <div class="reading-mode" :class="{ active: isActive }">
    <div class="reading-toolbar" v-if="isActive">
      <button @click="$emit('toggle')">退出阅读模式</button>
      <div class="font-controls">
        <button @click="decreaseFont">A-</button>
        <span class="font-size">{{ fontSize }}px</span>
        <button @click="increaseFont">A+</button>
      </div>
      <div class="width-controls">
        <button :class="{ active: contentWidth === 'narrow' }" @click="contentWidth = 'narrow'">窄</button>
        <button :class="{ active: contentWidth === 'medium' }" @click="contentWidth = 'medium'">中</button>
        <button :class="{ active: contentWidth === 'wide' }" @click="contentWidth = 'wide'">宽</button>
      </div>
    </div>
    <div
      class="reading-content"
      :style="{ fontSize: fontSize + 'px', maxWidth: widthMap[contentWidth] }"
      v-if="isActive"
      v-html="content"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  isActive: boolean
  content: string
}>()

defineEmits<{ toggle: [] }>()

const fontSize = ref(16)
const contentWidth = ref<'narrow' | 'medium' | 'wide'>('medium')

const widthMap = {
  narrow: '640px',
  medium: '800px',
  wide: '1000px',
}

function increaseFont() {
  if (fontSize.value < 28) fontSize.value += 2
}

function decreaseFont() {
  if (fontSize.value > 12) fontSize.value -= 2
}
</script>

<style scoped>
.reading-mode.active {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.reading-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 20px;
  border-bottom: 1px solid var(--border-color);
  width: 100%;
  background: var(--bg-secondary);
}

.reading-toolbar button {
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  cursor: pointer;
  font-size: 13px;
  transition: var(--transition);
}

.reading-toolbar button:hover {
  border-color: var(--primary);
}

.reading-toolbar button.active {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.font-controls,
.width-controls {
  display: flex;
  align-items: center;
  gap: 6px;
}

.font-size {
  font-size: 12px;
  color: var(--text-secondary);
  min-width: 36px;
  text-align: center;
}

.reading-content {
  flex: 1;
  overflow-y: auto;
  padding: 40px 24px;
  line-height: 1.8;
  width: 100%;
}

.reading-content :deep(h1) { font-size: 2em; margin: 0.67em 0; }
.reading-content :deep(h2) { font-size: 1.5em; margin: 0.75em 0; }
.reading-content :deep(h3) { font-size: 1.17em; margin: 0.83em 0; }
.reading-content :deep(p) { margin: 0.5em 0; }
.reading-content :deep(blockquote) {
  border-left: 3px solid var(--border-color);
  padding-left: 16px;
  color: var(--text-secondary);
}
.reading-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--radius-md);
}
</style>