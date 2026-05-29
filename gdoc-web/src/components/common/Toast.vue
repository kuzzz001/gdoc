<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

export interface ToastMessage {
  id: number
  text: string
  type: 'success' | 'error' | 'info' | 'warning'
  duration: number
}

const toasts = ref<ToastMessage[]>([])
let nextId = 0

function addToast(text: string, type: ToastMessage['type'] = 'info', duration = 3000) {
  const id = nextId++
  toasts.value.push({ id, text, type, duration })
  if (duration > 0) {
    setTimeout(() => removeToast(id), duration)
  }
}

function removeToast(id: number) {
  toasts.value = toasts.value.filter(t => t.id !== id)
}

defineExpose({
  success: (text: string, duration?: number) => addToast(text, 'success', duration),
  error: (text: string, duration?: number) => addToast(text, 'error', duration),
  info: (text: string, duration?: number) => addToast(text, 'info', duration),
  warning: (text: string, duration?: number) => addToast(text, 'warning', duration),
})
</script>

<template>
  <teleport to="body">
    <div class="toast-container">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="toast-item"
        :class="'toast-' + toast.type"
        @click="removeToast(toast.id)"
      >
        <span class="toast-icon">
          <span v-if="toast.type === 'success'">&#10003;</span>
          <span v-else-if="toast.type === 'error'">&#10007;</span>
          <span v-else-if="toast.type === 'warning'">&#9888;</span>
          <span v-else>&#8505;</span>
        </span>
        <span class="toast-text">{{ toast.text }}</span>
      </div>
    </div>
  </teleport>
</template>

<style scoped lang="scss">
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  animation: slideIn 0.3s ease;
  min-width: 200px;
  max-width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.toast-success { background: #52c41a; }
.toast-error { background: #ff4d4f; }
.toast-info { background: #1890ff; }
.toast-warning { background: #faad14; }

.toast-icon {
  font-size: 16px;
  font-weight: bold;
}

@keyframes slideIn {
  from { transform: translateX(100%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}
</style>