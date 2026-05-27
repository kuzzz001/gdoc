<template>
  <button class="theme-toggle" :title="themeTitle" @click="toggleTheme">
    <span v-if="themeStore.theme === 'light'" class="icon">☀️</span>
    <span v-else-if="themeStore.theme === 'dark'" class="icon">🌙</span>
    <span v-else class="icon">💻</span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

const themeTitle = computed(() => {
  if (themeStore.theme === 'light') return '亮色模式'
  if (themeStore.theme === 'dark') return '暗色模式'
  return '跟随系统'
})

function toggleTheme() {
  themeStore.toggleTheme()
}
</script>

<style scoped lang="scss">
.theme-toggle {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: var(--bg-secondary);
    border-color: var(--primary);
  }

  .icon {
    font-size: 18px;
  }
}
</style>
