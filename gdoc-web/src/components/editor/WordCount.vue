<template>
  <div class="word-count">
    <span>字数: {{ count }}</span>
    <span>字符: {{ chars }}</span>
    <span>段落: {{ paragraphs }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  content: string
}>()

const count = ref(0)
const chars = ref(0)
const paragraphs = ref(0)

function updateStats() {
  const text = props.content?.replace(/<[^>]+>/g, '') || ''
  chars.value = text.length
  count.value = text.trim() ? text.trim().split(/\s+/).length : 0
  paragraphs.value = text.trim() ? text.split(/\n\s*\n/).filter(Boolean).length : 0
}

watch(() => props.content, updateStats, { immediate: true })
</script>

<style scoped>
.word-count {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-secondary);
  padding: 4px 0;
}
</style>