<template>
  <div class="friend-card" @click="$emit('click', friend)">
    <Avatar :text="friend.nickname || friend.username" :src="friend.avatarUrl" size="md" />
    <div class="friend-info">
      <div class="friend-name">{{ friend.nickname || friend.username }}</div>
      <div class="friend-username">@{{ friend.username }}</div>
    </div>
    <div v-if="friend.unreadCount > 0" class="unread-badge">
      {{ friend.unreadCount > 99 ? '99+' : friend.unreadCount }}
    </div>
  </div>
</template>

<script setup lang="ts">
import Avatar from '@/components/common/Avatar.vue'
import type { Friend } from '@/types'

defineProps<{
  friend: Friend
}>()

defineEmits<{
  click: [friend: Friend]
}>()
</script>

<style scoped lang="scss">
.friend-card {
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
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.friend-username {
  font-size: 12px;
  color: var(--text-secondary);
}

.unread-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--danger);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
