<template>
  <div class="chat-list">
    <div
      v-for="friend in friends"
      :key="friend.userId"
      class="chat-list-item"
      :class="{ active: activeUserId === friend.userId }"
      @click="$emit('select', friend)"
    >
      <Avatar :text="friend.nickname || friend.username" :src="friend.avatar" size="md" />
      <div class="chat-item-info">
        <div class="chat-item-name">{{ friend.nickname || friend.username }}</div>
        <div class="chat-item-preview">{{ lastMessage(friend.userId) }}</div>
      </div>
      <div v-if="friend.unreadCount > 0" class="unread-dot" />
    </div>
    <div v-if="friends.length === 0" class="empty-hint">暂无聊天</div>
  </div>
</template>

<script setup lang="ts">
import Avatar from '@/components/common/Avatar.vue'
import type { Friend } from '@/types'

defineProps<{
  friends: Friend[]
  activeUserId: number | null
}>()

defineEmits<{
  select: [friend: Friend]
}>()

function lastMessage(friendId: number): string {
  return '开始聊天吧'
}
</script>

<style scoped lang="scss">
.chat-list {
  overflow-y: auto;
  flex: 1;
}

.chat-list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: var(--transition);
  position: relative;

  &:hover {
    background: var(--bg-tertiary);
  }

  &.active {
    background: var(--primary-light);
  }
}

.chat-item-info {
  flex: 1;
  min-width: 0;
}

.chat-item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.chat-item-preview {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: var(--danger);
  border-radius: 50%;
}

.empty-hint {
  padding: 40px 16px;
  text-align: center;
  color: var(--text-placeholder);
  font-size: 13px;
}
</style>
