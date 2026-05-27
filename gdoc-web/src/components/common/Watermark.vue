<template>
  <div class="watermark-overlay" v-if="visible" :style="watermarkStyle">
    <div class="watermark-text" v-for="i in count" :key="i">
      {{ text }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  text?: string
  visible?: boolean
  opacity?: number
  fontSize?: number
  rotate?: number
}>(), {
  text: 'GDOC',
  visible: true,
  opacity: 0.06,
  fontSize: 16,
  rotate: -30,
})

const count = 30

const watermarkStyle = computed(() => ({
  '--wm-opacity': props.opacity,
  '--wm-font-size': props.fontSize + 'px',
  '--wm-rotate': props.rotate + 'deg',
}))
</script>

<style scoped>
.watermark-overlay {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 9999;
  overflow: hidden;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  grid-template-rows: repeat(5, 1fr);
  gap: 20px;
  padding: 20px;
}

.watermark-text {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--wm-font-size);
  color: var(--text-primary);
  opacity: var(--wm-opacity);
  transform: rotate(var(--wm-rotate));
  user-select: none;
  white-space: nowrap;
  font-weight: 600;
}
</style>