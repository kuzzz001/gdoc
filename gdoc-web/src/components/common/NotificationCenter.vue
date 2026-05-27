<template>
  <div class="notification-center">
    <div class="panel-header">
      <h3>通知</h3>
      <button v-if="notifications.length > 0" class="btn-read-all" @click="markAllRead">全部已读</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="notifications.length === 0" class="empty">
      <p>暂无通知</p>
    </div>

    <div v-else class="notification-list">
      <div
        v-for="n in notifications"
        :key="n.id"
        class="notification-item"
        :class="{ unread: !n.isRead }"
        @click="handleClick(n)"
      >
        <div class="notification-icon" :class="getTypeClass(n.type)">
          {{ getTypeIcon(n.type) }}
        </div>
        <div class="notification-body">
          <div class="notification-content">{{ n.content }}</div>
          <div class="notification-time">{{ formatTime(n.createdAt) }}</div>
        </div>
        <div v-if="!n.isRead" class="unread-dot"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { notificationApi, type Notification } from '@/api/notification'

const router = useRouter()
const notifications = ref<Notification[]>([])
const loading = ref(false)

async function loadNotifications() {
  loading.value = true
  try {
    notifications.value = await notificationApi.list()
  } catch {
    notifications.value = []
  } finally {
    loading.value = false
  }
}

async function markAllRead() {
  try {
    await notificationApi.markAllRead()
    notifications.value.forEach(n => n.isRead = 1)
  } catch { /* ignore */ }
}

async function handleClick(n: Notification) {
  if (!n.isRead) {
    try {
      await notificationApi.markRead(n.id)
      n.isRead = 1
    } catch { /* ignore */ }
  }
  if (n.relatedId && (n.type === 'doc_shared' || n.type === 'collab_invite' || n.type === 'comment')) {
    router.push(`/editor/${n.relatedId}`)
  }
}

function getTypeIcon(type: string) {
  switch (type) {
    case 'collab_invite': return '&#9993;'
    case 'comment': return '&#128172;'
    case 'doc_shared': return '&#128196;'
    case 'mention': return '@'
    default: return '&#128276;'
  }
}

function getTypeClass(type: string) {
  switch (type) {
    case 'collab_invite': return 'type-invite'
    case 'comment': return 'type-comment'
    case 'doc_shared': return 'type-share'
    case 'mention': return 'type-mention'
    default: return 'type-default'
  }
}

function formatTime(dateStr: string) {
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return d.toLocaleDateString('zh-CN')
}

onMounted(loadNotifications)

defineExpose({ loadNotifications })
</script>

<style scoped>
.notification-center {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-primary);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.panel-header h3 {
  font-size: 15px;
  font-weight: 600;
}

.btn-read-all {
  padding: 4px 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: transparent;
  font-size: 12px;
  color: var(--primary);
  cursor: pointer;
}

.loading, .empty {
  padding: 40px 16px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: var(--transition);
}

.notification-item:hover {
  background: var(--bg-secondary);
}

.notification-item.unread {
  background: var(--primary-light);
}

.notification-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.type-invite { background: #e8f0fe; color: #1a73e8; }
.type-comment { background: #e6f4ea; color: #137333; }
.type-share { background: #fef7e0; color: #b06000; }
.type-mention { background: #fce8e6; color: #c5221f; }
.type-default { background: var(--bg-tertiary); color: var(--text-secondary); }

.notification-body {
  flex: 1;
  min-width: 0;
}

.notification-content {
  font-size: 13px;
  line-height: 1.4;
  word-break: break-word;
}

.notification-time {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  flex-shrink: 0;
  margin-top: 6px;
}
</style>