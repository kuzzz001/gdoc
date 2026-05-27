<template>
  <div class="message-bubble" :class="{ 'is-self': isSelf, 'is-system': isSystem }">
    <div v-if="!isSystem" class="message-avatar">
      <Avatar :text="senderName" :src="avatar" :size="isSelf ? 'sm' : 'md'" />
    </div>
    <div class="message-content">
      <div v-if="!isSelf && showName" class="message-sender">{{ senderName }}</div>
      <div class="message-body" :class="`message-${type}`">
        <template v-if="type === 'text'">
          {{ content }}
        </template>
        <template v-else-if="type === 'image'">
          <img :src="content" alt="图片消息" class="message-image" @click="$emit('preview', content)" />
        </template>
        <template v-else-if="type === 'file'">
          <div class="message-file">
            <span class="file-icon">📎</span>
            <span class="file-name">{{ fileName }}</span>
            <span class="file-size">{{ fileSize }}</span>
          </div>
        </template>
        <template v-else-if="type === 'system'">
          <span class="system-text">{{ content }}</span>
        </template>
      </div>
      <div v-if="showTime" class="message-time">{{ formatTime(time) }}</div>
      <div v-if="isSelf && showStatus" class="message-status">
        <span v-if="status === 'sent'" class="status-sent">✓</span>
        <span v-else-if="status === 'delivered'" class="status-delivered">✓✓</span>
        <span v-else-if="status === 'read'" class="status-read">✓✓</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Avatar from './Avatar.vue'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    content: string
    type?: 'text' | 'image' | 'file' | 'system'
    isSelf?: boolean
    senderName?: string
    avatar?: string
    time?: string
    showName?: boolean
    showTime?: boolean
    showStatus?: boolean
    status?: 'sent' | 'delivered' | 'read'
    fileName?: string
    fileSize?: string
  }>(),
  {
    type: 'text',
    isSelf: false,
    senderName: '',
    avatar: '',
    time: '',
    showName: true,
    showTime: true,
    showStatus: true,
    status: 'sent',
    fileName: '',
    fileSize: '',
  }
)

defineEmits<{
  preview: [src: string]
}>()

const isSystem = computed(() => props.type === 'system')

function formatTime(time: string): string {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  if (isToday) {
    return `${hours}:${minutes}`
  }
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}
</script>

<style scoped lang="scss">
.message-bubble {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;

  &.is-self {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;
    }

    .message-body {
      background: var(--primary);
      color: #fff;
      border-radius: var(--radius-lg) var(--radius-sm) var(--radius-lg) var(--radius-lg);
    }

    .message-time {
      text-align: right;
    }
  }

  &.is-system {
    justify-content: center;

    .message-body {
      background: var(--bg-tertiary);
      border-radius: var(--radius-md);
      padding: 6px 16px;
    }
  }
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.message-sender {
  font-size: 12px;
  color: var(--text-secondary);
  padding-left: 4px;
}

.message-body {
  padding: 10px 14px;
  border-radius: var(--radius-sm) var(--radius-lg) var(--radius-lg) var(--radius-lg);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    opacity: 0.9;
  }
}

.message-file {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  cursor: pointer;

  .file-icon {
    font-size: 20px;
  }

  .file-name {
    font-size: 13px;
    color: var(--text-primary);
    max-width: 150px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .file-size {
    font-size: 11px;
    color: var(--text-secondary);
  }
}

.system-text {
  font-size: 12px;
  color: var(--text-secondary);
}

.message-time {
  font-size: 11px;
  color: var(--text-placeholder);
  padding-left: 4px;
}

.message-status {
  padding-left: 4px;

  span {
    font-size: 12px;
  }

  .status-sent {
    color: var(--text-placeholder);
  }

  .status-delivered {
    color: var(--text-secondary);
  }

  .status-read {
    color: var(--primary);
  }
}
</style>
