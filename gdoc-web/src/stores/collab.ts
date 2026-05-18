import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CursorState } from '@/types'

export const useCollabStore = defineStore('collab', () => {
  const connected = ref(false)
  const cursors = ref<CursorState[]>([])
  const onlineUsers = ref<Map<number, string>>(new Map())

  function addCursor(cursor: CursorState) {
    const idx = cursors.value.findIndex((c) => c.userId === cursor.userId)
    if (idx >= 0) {
      cursors.value[idx] = cursor
    } else {
      cursors.value.push(cursor)
    }
  }

  function removeCursor(userId: number) {
    cursors.value = cursors.value.filter((c) => c.userId !== userId)
  }

  function clearCursors() {
    cursors.value = []
  }

  return { connected, cursors, onlineUsers, addCursor, removeCursor, clearCursors }
})
