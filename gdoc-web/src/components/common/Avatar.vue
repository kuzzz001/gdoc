<template>
  <div class="gdoc-avatar" :style="avatarStyle">
    <svg v-if="!src" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
      <circle :fill="bgColor" cx="20" cy="20" r="20" />
      <text
        x="20"
        y="26"
        text-anchor="middle"
        fill="#fff"
        font-size="16"
        font-weight="600"
        font-family="inherit"
      >
        {{ displayText }}
      </text>
    </svg>
    <img v-else :src="src" :alt="alt" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    src?: string
    alt?: string
    size?: 'sm' | 'md' | 'lg'
    text?: string
    color?: string
  }>(),
  {
    alt: '',
    size: 'md',
    text: '',
    color: '',
  }
)

const sizeMap = { sm: '32px', md: '40px', lg: '48px' }
const fontSizeMap = { sm: '12px', md: '16px', lg: '20px' }
const colors = ['#4285f4', '#ea4335', '#fbbc05', '#34a853', '#8e24aa', '#e91e63', '#00897b', '#ff6d00']

const displayText = computed(() => props.text?.charAt(0).toUpperCase() || '?')
const bgColor = computed(() => props.color || colors[Math.abs(hashCode(props.text)) % colors.length])
const avatarStyle = computed(() => ({
  width: sizeMap[props.size],
  height: sizeMap[props.size],
  fontSize: fontSizeMap[props.size],
}))

function hashCode(str: string): number {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash + str.charCodeAt(i)) | 0
  }
  return hash
}
</script>

<style scoped lang="scss">
.gdoc-avatar {
  border-radius: 50%;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
</style>
