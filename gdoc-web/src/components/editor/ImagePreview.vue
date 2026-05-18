<template>
  <Teleport to="body">
    <div v-if="visible" class="image-preview-overlay" @click.self="close">
      <div class="image-preview">
        <button class="preview-close" @click="close">&times;</button>
        <img :src="src" :alt="alt" />
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  src: string
  alt?: string
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

function close() {
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
.image-preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.image-preview {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;

  img {
    max-width: 100%;
    max-height: 90vh;
    border-radius: var(--radius-md);
    box-shadow: var(--shadow-xl);
  }
}

.preview-close {
  position: absolute;
  top: -40px;
  right: 0;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 24px;
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}
</style>
