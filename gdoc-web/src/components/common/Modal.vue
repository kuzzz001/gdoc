<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="modal-overlay" @click.self="handleOverlayClick">
        <div class="modal" :class="{ 'modal-sm': size === 'sm', 'modal-lg': size === 'lg' }">
          <div class="modal-header">
            <h3>{{ title }}</h3>
            <button class="modal-close" @click="close">&times;</button>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    visible: boolean
    title?: string
    size?: 'sm' | 'md' | 'lg'
    closeOnOverlay?: boolean
  }>(),
  {
    title: '',
    size: 'md',
    closeOnOverlay: true,
  }
)

const emit = defineEmits<{
  'update:visible': [value: boolean]
  close: []
}>()

function close() {
  emit('update:visible', false)
  emit('close')
}

function handleOverlayClick() {
  if (props.closeOnOverlay) close()
}
</script>

<style scoped lang="scss">
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  width: 500px;
  max-width: 90vw;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-xl);
  overflow: hidden;

  &-sm {
    width: 360px;
  }

  &-lg {
    width: 720px;
  }
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-secondary);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
  }
}

.modal-close {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  transition: var(--transition);

  &:hover {
    background: var(--bg-tertiary);
    color: var(--text-primary);
  }
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .modal {
  transition: transform 0.2s ease;
}

.modal-fade-enter-from .modal {
  transform: scale(0.95);
}
</style>
