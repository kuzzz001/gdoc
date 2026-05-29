<template>
  <div class="social-page">
    <header class="page-header">
      <div class="header-left">
        <h1>吉智文档</h1>
      </div>
      <div class="header-right">
        <nav class="header-nav">
          <router-link to="/" class="nav-link">文档</router-link>
          <router-link to="/social" class="nav-link active">社交</router-link>
        </nav>
        <div class="user-menu">
          <Avatar :text="userStore.nickname || userStore.username" :src="userStore.avatar" size="sm" />
          <span class="user-name">{{ userStore.nickname || userStore.username }}</span>
          <button class="btn-logout" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <main class="page-content">
      <div class="social-layout">
        <aside class="social-sidebar">
          <div class="sidebar-tabs">
            <button
              :class="{ active: activeTab === 'friends' }"
              @click="activeTab = 'friends'"
            >
              好友
            </button>
            <button
              :class="{ active: activeTab === 'requests' }"
              @click="activeTab = 'requests'"
            >
              申请
            </button>
            <button
              :class="{ active: activeTab === 'invitations' }"
              @click="activeTab = 'invitations'"
            >
              邀请
            </button>
          </div>

          <div class="sidebar-content">
            <div v-if="activeTab === 'friends'" class="friends-list">
              <div class="search-box">
                <input v-model="searchKeyword" placeholder="搜索用户..." @keyup.enter="searchUsers" />
                <button @click="searchUsers">搜索</button>
              </div>
              <div v-if="searchResults.length > 0" class="search-results">
                <div v-for="u in searchResults" :key="u.id" class="search-result-item">
                  <Avatar :text="u.nickname || u.username" size="sm" />
                  <span>{{ u.nickname || u.username }}</span>
                  <button class="btn-add-friend" @click="addFriend(u.id)">添加</button>
                </div>
              </div>
              <ChatList
                :friends="socialStore.friends"
                :active-user-id="activeChatUserId"
                @select="selectFriend"
              />
            </div>

            <div v-if="activeTab === 'requests'" class="requests-list">
              <div v-if="socialStore.friendRequests.length === 0" class="empty-hint">
                暂无好友申请
              </div>
              <div
                v-for="req in socialStore.friendRequests"
                :key="req.id"
                class="request-item"
              >
                <Avatar :text="req.fromNickname || req.fromUsername" :src="req.fromAvatar" size="md" />
                <div class="request-info">
                  <div class="request-name">{{ req.fromNickname || req.fromUsername }}</div>
                  <div class="request-time">{{ formatTime(req.createdAt) }}</div>
                </div>
                <div class="request-actions">
                  <button class="btn-accept" @click="handleFriendRequest(req.id, 'accept')">接受</button>
                  <button class="btn-reject" @click="handleFriendRequest(req.id, 'reject')">拒绝</button>
                </div>
              </div>
            </div>

            <div v-if="activeTab === 'invitations'" class="invitations-list">
              <div v-if="socialStore.invitations.length === 0" class="empty-hint">
                暂无协作邀请
              </div>
              <InvitationCard
                v-for="inv in socialStore.invitations"
                :key="inv.id"
                :invitation="inv"
                @accept="handleInvitation(inv.id, 'accept')"
                @reject="handleInvitation(inv.id, 'reject')"
              />
            </div>
          </div>
        </aside>

        <section class="chat-area">
          <div v-if="!activeChatUserId" class="chat-empty">
            <div class="chat-empty-icon">
              <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 20h40v28a4 4 0 01-4 4H16a4 4 0 01-4-4V20z" stroke="#dadce0" stroke-width="2" />
                <path d="M12 20l8-8h24l8 8" stroke="#dadce0" stroke-width="2" />
                <path d="M24 32h16M24 38h10" stroke="#dadce0" stroke-width="2" stroke-linecap="round" />
              </svg>
            </div>
            <p>选择一个好友开始聊天</p>
          </div>

          <template v-else>
            <div class="chat-header">
              <Avatar :text="activeFriend?.nickname || activeFriend?.username" :src="activeFriend?.avatarUrl" size="md" />
              <div class="chat-header-info">
                <div class="chat-header-name">{{ activeFriend?.nickname || activeFriend?.username }}</div>
                <div class="chat-header-status">在线</div>
              </div>
            </div>

            <div class="chat-messages" ref="messagesRef">
              <div
                v-for="msg in currentMessages"
                :key="msg.id"
                class="message-item"
                :class="{ 'message-self': msg.senderId === userStore.userId }"
              >
                <Avatar
                  v-if="msg.senderId !== userStore.userId"
                  :text="activeFriend?.nickname?.charAt(0) || '?'"
                  :src="activeFriend?.avatar"
                  size="sm"
                />
                <div class="message-bubble">{{ msg.content }}</div>
                <Avatar
                  v-if="msg.senderId === userStore.userId"
                  :text="userStore.nickname?.charAt(0) || '?'"
                  :src="userStore.avatar"
                  size="sm"
                />
              </div>
            </div>

            <div class="chat-input">
              <input
                v-model="messageInput"
                placeholder="输入消息..."
                @keyup.enter="sendMessage"
              />
              <button @click="sendMessage">发送</button>
            </div>
          </template>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Avatar from '@/components/common/Avatar.vue'
import ChatList from '@/components/social/ChatList.vue'
import InvitationCard from '@/components/social/InvitationCard.vue'
import { useUserStore } from '@/stores/user'
import { useSocialStore } from '@/stores/social'
import { socialApi, invitationApi } from '@/api/social'
import type { Friend, User } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const socialStore = useSocialStore()

const activeTab = ref<'friends' | 'requests' | 'invitations'>('friends')
const activeChatUserId = ref<number | null>(null)
const searchKeyword = ref('')
const searchResults = ref<User[]>([])
const messageInput = ref('')
const messagesRef = ref<HTMLElement | null>(null)

onMounted(async () => {
  await Promise.all([
    socialStore.fetchFriends(),
    socialStore.fetchFriendRequests(),
    socialStore.fetchInvitations(),
  ])
})

const activeFriend = computed(() =>
  socialStore.friends.find((f) => f.userId === activeChatUserId.value) || null
)

const currentMessages = computed(() => {
  if (!activeChatUserId.value) return []
  return socialStore.messages.get(activeChatUserId.value) || []
})

function selectFriend(friend: Friend) {
  activeChatUserId.value = friend.userId
  socialStore.activeChatUserId = friend.userId
  socialStore.fetchMessages(friend.userId)
  socialStore.markRead(friend.userId)
}

async function searchUsers() {
  if (!searchKeyword.value.trim()) return
  try {
    searchResults.value = await socialApi.searchUsers(searchKeyword.value)
  } catch {
    searchResults.value = []
  }
}

async function addFriend(userId: number) {
  try {
    await socialApi.sendFriendRequest(userId)
    searchResults.value = []
    alert('好友申请已发送')
  } catch {
    alert('发送好友申请失败')
  }
}

async function handleFriendRequest(id: number, action: 'accept' | 'reject') {
  try {
    await socialApi.handleFriendRequest(id, action)
    socialStore.fetchFriendRequests()
  } catch {
    alert('操作失败')
  }
}

async function handleInvitation(id: number, action: 'accept' | 'reject') {
  try {
    await invitationApi.handle(id, action)
    socialStore.fetchInvitations()
  } catch {
    alert('操作失败')
  }
}

function sendMessage() {
  if (!messageInput.value.trim() || !activeChatUserId.value) return
  try {
    socialStore.sendMessage(activeChatUserId.value, messageInput.value.trim())
  } catch {
    // handled by store
  } finally {
    messageInput.value = ''
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

function formatTime(t: string) {
  return new Date(t).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.social-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

.page-header {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left h1 {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-nav {
  display: flex;
  gap: 16px;
}

.nav-link {
  font-size: 14px;
  color: var(--text-secondary);
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  transition: var(--transition);
  text-decoration: none;

  &:hover {
    background: var(--bg-tertiary);
    text-decoration: none;
  }

  &.active {
    color: var(--primary);
    background: var(--primary-light);
  }
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 13px;
  color: var(--text-primary);
}

.btn-logout {
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: transparent;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    color: var(--danger);
    border-color: var(--danger);
  }
}

.page-content {
  flex: 1;
  overflow: hidden;
}

.social-layout {
  display: flex;
  height: 100%;
}

.social-sidebar {
  width: 320px;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-tabs {
  display: flex;
  border-bottom: 1px solid var(--border-color);

  button {
    flex: 1;
    padding: 12px;
    border: none;
    background: transparent;
    font-size: 13px;
    font-weight: 500;
    color: var(--text-secondary);
    cursor: pointer;
    transition: var(--transition);
    position: relative;

    &.active {
      color: var(--primary);

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 24px;
        height: 2px;
        background: var(--primary);
        border-radius: 1px;
      }
    }
  }
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

.friends-list {
  .search-box {
    display: flex;
    gap: 8px;
    padding: 12px;
    border-bottom: 1px solid var(--border-light);

    input {
      flex: 1;
      padding: 8px 12px;
      border: 1px solid var(--border-color);
      border-radius: var(--radius-sm);
      font-size: 13px;
      outline: none;

      &:focus {
        border-color: var(--primary);
      }
    }

    button {
      padding: 8px 14px;
      background: var(--primary);
      color: #fff;
      border: none;
      border-radius: var(--radius-sm);
      font-size: 13px;
      cursor: pointer;

      &:hover {
        background: var(--primary-hover);
      }
    }
  }
}

.search-results {
  border-bottom: 1px solid var(--border-light);
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;

  span {
    flex: 1;
    font-size: 13px;
  }
}

.btn-add-friend {
  padding: 4px 12px;
  background: var(--primary-light);
  color: var(--primary);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: var(--primary);
    color: #fff;
  }
}

.requests-list {
  .request-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-bottom: 1px solid var(--border-light);
  }

  .request-info {
    flex: 1;
  }

  .request-name {
    font-size: 13px;
    font-weight: 500;
  }

  .request-time {
    font-size: 11px;
    color: var(--text-secondary);
  }

  .request-actions {
    display: flex;
    gap: 6px;
  }

  .btn-accept {
    padding: 4px 12px;
    background: var(--primary);
    color: #fff;
    border: none;
    border-radius: var(--radius-sm);
    font-size: 12px;
    cursor: pointer;

    &:hover {
      background: var(--primary-hover);
    }
  }

  .btn-reject {
    padding: 4px 12px;
    background: transparent;
    color: var(--text-secondary);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-sm);
    font-size: 12px;
    cursor: pointer;

    &:hover {
      color: var(--danger);
      border-color: var(--danger);
    }
  }
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);

  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

.chat-empty-icon {
  width: 80px;
  height: 80px;

  svg {
    width: 100%;
    height: 100%;
  }
}

.chat-header {
  padding: 12px 20px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--bg-primary);
}

.chat-header-name {
  font-size: 14px;
  font-weight: 600;
}

.chat-header-status {
  font-size: 12px;
  color: var(--success);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-end;
  gap: 8px;

  &.message-self {
    flex-direction: row-reverse;
  }
}

.message-bubble {
  max-width: 60%;
  padding: 10px 14px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  font-size: 14px;
  line-height: 1.5;
}

.message-self .message-bubble {
  background: var(--primary);
  color: #fff;
}

.chat-input {
  padding: 12px 20px;
  border-top: 1px solid var(--border-color);
  display: flex;
  gap: 8px;
  background: var(--bg-primary);

  input {
    flex: 1;
    padding: 10px 14px;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    font-size: 14px;
    outline: none;

    &:focus {
      border-color: var(--primary);
    }
  }

  button {
    padding: 10px 20px;
    background: var(--primary);
    color: #fff;
    border: none;
    border-radius: var(--radius-md);
    font-size: 14px;
    cursor: pointer;

    &:hover {
      background: var(--primary-hover);
    }
  }
}

.empty-hint {
  padding: 40px 16px;
  text-align: center;
  color: var(--text-placeholder);
  font-size: 13px;
}
</style>
