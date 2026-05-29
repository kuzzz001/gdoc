import { ref } from 'vue'

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

export function useToast() {
  return {
    toasts,
    success: (text: string, duration?: number) => addToast(text, 'success', duration),
    error: (text: string, duration?: number) => addToast(text, 'error', duration),
    info: (text: string, duration?: number) => addToast(text, 'info', duration),
    warning: (text: string, duration?: number) => addToast(text, 'warning', duration),
  }
}