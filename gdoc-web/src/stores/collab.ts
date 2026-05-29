import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CursorState } from '@/types'

export const useCollabStore = defineStore('collab', () => {
  const connected = ref(false)
  const cursors = ref<CursorState[]>([])
  const onlineUsers = ref<Map<number, string>>(new Map())
  let stompClient: any = null

  function connect(docId: number, userId: number) {
    if (connected.value) return

    const token = getCookie('gdoc_token')
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = `${protocol}//${window.location.host}/ws/collaboration?docId=${docId}&userId=${userId}&token=${token}`

    const ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      connected.value = true
      sendMessage(ws, { type: 'JOIN', docId, userId })
    }

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        handleMessage(msg)
      } catch {
        // ignore malformed messages
      }
    }

    ws.onclose = () => {
      connected.value = false
      cursors.value = []
    }

    ws.onerror = () => {
      connected.value = false
    }

    ;(window as any).__collabWs = ws
  }

  function disconnect() {
    const ws = (window as any).__collabWs
    if (ws) {
      ws.close()
      delete (window as any).__collabWs
    }
    connected.value = false
    cursors.value = []
  }

  function sendMessage(ws: WebSocket, msg: Record<string, unknown>) {
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(msg))
    }
  }

  function handleMessage(msg: Record<string, any>) {
    switch (msg.type) {
      case 'CURSOR_UPDATE':
        addCursor(msg as CursorState)
        break
      case 'USER_JOINED':
        onlineUsers.value.set(msg.userId, msg.username)
        break
      case 'USER_LEFT':
        onlineUsers.value.delete(msg.userId)
        removeCursor(msg.userId)
        break
      case 'USERS_LIST':
        msg.users?.forEach((u: { userId: number; username: string }) => {
          onlineUsers.value.set(u.userId, u.username)
        })
        break
    }
  }

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

  function getCookie(name: string): string | null {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
    return match ? match[2] : null
  }

  return { connected, cursors, onlineUsers, connect, disconnect, addCursor, removeCursor, clearCursors }
})