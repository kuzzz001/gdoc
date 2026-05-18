<template>
  <div class="invitation-card">
    <div class="invitation-title">
      {{ invitation.documentTitle }}
    </div>
    <div class="invitation-meta">
      <span>{{ invitation.inviterName }}</span>
      <span class="meta-divider">·</span>
      <span>{{ permLabel(invitation.permission) }}</span>
      <span class="meta-divider">·</span>
      <span>{{ formatTime(invitation.createdAt) }}</span>
    </div>
    <div v-if="invitation.message" class="invitation-message">
      {{ invitation.message }}
    </div>
    <div class="invitation-actions">
      <template v-if="invitation.status === 'pending'">
        <button class="btn-accept" @click="$emit('accept', invitation)">接受</button>
        <button class="btn-reject" @click="$emit('reject', invitation)">拒绝</button>
      </template>
      <span v-else class="status-tag" :class="invitation.status">
        {{ statusLabel(invitation.status) }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CollabInvitation } from '@/types'

defineProps<{
  invitation: CollabInvitation
}>()

defineEmits<{
  accept: [invitation: CollabInvitation]
  reject: [invitation: CollabInvitation]
}>()

function permLabel(p: string) {
  return p === 'editor' ? '可编辑' : '只读'
}

function statusLabel(s: string) {
  const map: Record<string, string> = { accepted: '已接受', rejected: '已拒绝', pending: '待处理' }
  return map[s] || s
}

function formatTime(t: string) {
  return new Date(t).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.invitation-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 14px 16px;
  margin: 8px 0;
  transition: var(--transition);

  &:hover {
    border-color: var(--primary);
    box-shadow: var(--shadow-sm);
  }
}

.invitation-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.invitation-meta {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.meta-divider {
  margin: 0 4px;
}

.invitation-message {
  font-size: 13px;
  color: var(--text-secondary);
  padding: 8px 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  margin-bottom: 10px;
}

.invitation-actions {
  display: flex;
  gap: 8px;
}

.btn-accept {
  padding: 5px 16px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: var(--primary-hover);
  }
}

.btn-reject {
  padding: 5px 16px;
  background: var(--bg-primary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    color: var(--danger);
    border-color: var(--danger);
  }
}

.status-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: var(--radius-sm);

  &.accepted {
    background: var(--success-light);
    color: var(--success);
  }

  &.rejected {
    background: var(--danger-light);
    color: var(--danger);
  }
}
</style>
