<template>
  <div class="group-chat-panel">
    <div class="chat-header">
      <h3>群聊</h3>
      <button class="btn-close" @click="$emit('close')">&times;</button>
    </div>
    <div class="chat-list" v-if="!activeGroup">
      <button class="btn-create" @click="showCreate = true">+ 新建群聊</button>
      <div v-for="group in groups" :key="group.id" class="group-item" @click="openGroup(group)">
        <div class="group-avatar">{{ group.name.charAt(0) }}</div>
        <span class="group-name">{{ group.name }}</span>
      </div>
      <div v-if="groups.length === 0" class="empty-state">暂无群聊</div>
    </div>
    <div class="chat-room" v-else>
      <div class="room-header">
        <button class="btn-back" @click="activeGroup = null">&larr;</button>
        <span>{{ activeGroup.name }}</span>
      </div>
      <div class="messages" ref="messagesRef">
        <div v-for="msg in messages" :key="msg.id" class="message-item">
          <span class="msg-sender">用户{{ msg.senderId }}</span>
          <span class="msg-content">{{ msg.content }}</span>
          <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
        </div>
      </div>
      <div class="msg-input">
        <input v-model="newMsg" placeholder="输入消息..." @keydown.enter="send" />
        <button :disabled="!newMsg.trim()" @click="send">发送</button>
      </div>
    </div>
    <div v-if="showCreate" class="create-overlay">
      <div class="create-form">
        <h4>新建群聊</h4>
        <input v-model="newGroupName" placeholder="群聊名称" />
        <div class="create-actions">
          <button @click="showCreate = false">取消</button>
          <button :disabled="!newGroupName.trim()" @click="createGroup">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { groupApi, type GroupChat, type GroupMessage } from '@/api/social'

defineEmits<{ close: [] }>()

const groups = ref<GroupChat[]>([])
const activeGroup = ref<GroupChat | null>(null)
const messages = ref<GroupMessage[]>([])
const newMsg = ref('')
const newGroupName = ref('')
const showCreate = ref(false)
const messagesRef = ref<HTMLElement>()

onMounted(async () => {
  try {
    const res = await groupApi.list()
    groups.value = res.data || []
  } catch (e) {
    console.error('Failed to load groups', e)
  }
})

async function openGroup(group: GroupChat) {
  activeGroup.value = group
  const res = await groupApi.getMessages(group.id)
  messages.value = res.data || []
  await nextTick()
  scrollToBottom()
}

async function send() {
  if (!newMsg.value.trim() || !activeGroup.value) return
  await groupApi.sendMessage(activeGroup.value.id, newMsg.value)
  newMsg.value = ''
  const res = await groupApi.getMessages(activeGroup.value.id)
  messages.value = res.data || []
  await nextTick()
  scrollToBottom()
}

async function createGroup() {
  const res = await groupApi.create({ name: newGroupName.value })
  groups.value.push(res.data)
  showCreate.value = false
  newGroupName.value = ''
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.group-chat-panel {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  width: 360px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.chat-header h3 { margin: 0; font-size: 15px; }

.btn-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: var(--text-secondary);
}

.chat-list {
  padding: 12px;
  overflow-y: auto;
}

.btn-create {
  width: 100%;
  padding: 8px;
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-md);
  background: none;
  cursor: pointer;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--primary);
}

.group-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition);
}

.group-item:hover { background: var(--bg-secondary); }

.group-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.group-name { font-size: 14px; }

.chat-room {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.room-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--border-color);
  font-weight: 500;
}

.btn-back {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-secondary);
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.msg-sender {
  font-size: 12px;
  color: var(--primary);
  font-weight: 500;
}

.msg-content {
  font-size: 13px;
  line-height: 1.4;
}

.msg-time {
  font-size: 11px;
  color: var(--text-secondary);
}

.msg-input {
  display: flex;
  gap: 8px;
  padding: 10px 12px;
  border-top: 1px solid var(--border-color);
}

.msg-input input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 13px;
  background: var(--bg-primary);
}

.msg-input button {
  padding: 6px 14px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 13px;
}

.create-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.create-form {
  background: var(--bg-primary);
  padding: 20px;
  border-radius: var(--radius-md);
  width: 280px;
}

.create-form h4 { margin: 0 0 12px; }

.create-form input {
  width: 100%;
  padding: 8px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-bottom: 12px;
  background: var(--bg-primary);
}

.create-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.create-actions button {
  padding: 6px 16px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  cursor: pointer;
}

.create-actions button:last-child {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.empty-state {
  text-align: center;
  padding: 20px;
  color: var(--text-secondary);
  font-size: 13px;
}
</style>