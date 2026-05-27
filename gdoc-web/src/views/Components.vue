<template>
  <div class="components-page">
    <header class="page-header">
      <router-link to="/" class="back-link">&larr; 返回</router-link>
      <h1>组件文档</h1>
      <ThemeToggle />
    </header>

    <nav class="component-nav">
      <a
        v-for="section in sections"
        :key="section.id"
        :href="`#${section.id}`"
        :class="{ active: activeSection === section.id }"
      >
        {{ section.title }}
      </a>
    </nav>

    <main class="component-main">
      <section id="common" class="component-section">
        <h2>通用组件</h2>

        <div class="component-demo">
          <h3>Avatar 头像</h3>
          <p class="desc">用户头像组件，支持文字头像和图片头像</p>
          <div class="demo-preview">
            <div class="demo-row">
              <Avatar text="张三" />
              <Avatar text="李四" size="sm" />
              <Avatar text="王五" size="lg" />
              <Avatar src="https://api.dicebear.com/7.x/avataaars/svg?seed=test" />
            </div>
          </div>
          <div class="demo-code">
            <CodeBlock
              code='<Avatar text="张三" />
<Avatar text="李四" size="sm" />
<Avatar text="王五" size="lg" />
<Avatar src="https://..." />'
              language="vue"
              :show-line-numbers="false"
            />
          </div>
        </div>

        <div class="component-demo">
          <h3>Modal 弹窗</h3>
          <p class="desc">模态对话框组件</p>
          <div class="demo-preview">
            <button class="demo-btn" @click="showModal = true">打开弹窗</button>
            <Modal v-model:visible="showModal" title="示例弹窗">
              <p>这是弹窗内容</p>
            </Modal>
          </div>
        </div>

        <div class="component-demo">
          <h3>Pagination 分页</h3>
          <p class="desc">分页导航组件</p>
          <div class="demo-preview">
            <Pagination :current-page="currentPage" :total="100" :page-size="10" @change="currentPage = $event" />
          </div>
        </div>

        <div class="component-demo">
          <h3>MessageBubble 消息气泡</h3>
          <p class="desc">聊天消息气泡组件</p>
          <div class="demo-preview chat-demo">
            <MessageBubble
              content="你好，这是一条消息"
              sender-name="张三"
              time="2024-01-15 10:30:00"
            />
            <MessageBubble
              content="这是我的回复"
              sender-name="我"
              is-self
              time="2024-01-15 10:31:00"
              status="read"
            />
            <MessageBubble
              content="张三加入了群聊"
              type="system"
            />
          </div>
        </div>
      </section>

      <section id="editor" class="component-section">
        <h2>编辑器组件</h2>

        <div class="component-demo">
          <h3>Toolbar 工具栏</h3>
          <p class="desc">富文本编辑器格式工具栏</p>
          <div class="demo-preview">
            <Toolbar @command="handleCommand" />
          </div>
        </div>

        <div class="component-demo">
          <h3>CodeBlock 代码块</h3>
          <p class="desc">代码展示组件，支持语法高亮和复制</p>
          <div class="demo-preview">
            <CodeBlock
              :code="sampleCode"
              language="typescript"
              :max-lines="10"
            />
          </div>
        </div>

        <div class="component-demo">
          <h3>ImagePreview 图片预览</h3>
          <p class="desc">图片全屏预览组件</p>
          <div class="demo-preview">
            <img
              src="https://picsum.photos/200/120"
              class="preview-trigger"
              @click="previewImage"
            />
            <ImagePreview v-model:visible="showPreview" :src="previewSrc" />
          </div>
        </div>
      </section>

      <section id="social" class="component-section">
        <h2>社交组件</h2>

        <div class="component-demo">
          <h3>FriendCard 好友卡片</h3>
          <p class="desc">好友列表项组件</p>
          <div class="demo-preview">
            <FriendCard
              :friend="{
                id: 1,
                userId: 1,
                username: 'zhangsan',
                nickname: '张三',
                avatar: '',
                unreadCount: 3
              }"
            />
          </div>
        </div>

        <div class="component-demo">
          <h3>ChatList 聊天列表</h3>
          <p class="desc">聊天会话列表组件</p>
          <div class="demo-preview">
            <ChatList :friends="sampleFriends" :active-user-id="null" />
          </div>
        </div>

        <div class="component-demo">
          <h3>InvitationCard 邀请卡片</h3>
          <p class="desc">协作邀请卡片组件</p>
          <div class="demo-preview">
            <InvitationCard
              :invitation="{
                id: 1,
                inviterId: 2,
                inviterName: '李四',
                documentId: 1,
                documentTitle: '项目计划书',
                permission: 'editor',
                message: '邀请您一起编辑文档',
                status: 'pending',
                createdAt: '2024-01-15 10:00:00'
              }"
              @accept="handleAccept"
              @reject="handleReject"
            />
          </div>
        </div>
      </section>

      <section id="theme" class="component-section">
        <h2>主题系统</h2>

        <div class="component-demo">
          <h3>ThemeToggle 主题切换</h3>
          <p class="desc">支持亮色/暗色/跟随系统三种模式</p>
          <div class="demo-preview">
            <ThemeToggle />
            <span class="theme-label">当前主题: {{ themeStore.theme }}</span>
          </div>
        </div>

        <div class="component-demo">
          <h3>CSS 变量</h3>
          <p class="desc">设计系统变量一览</p>
          <div class="demo-preview">
            <div class="color-grid">
              <div class="color-item">
                <div class="color-swatch" style="background: var(--primary)"></div>
                <span>--primary</span>
              </div>
              <div class="color-item">
                <div class="color-swatch" style="background: var(--success)"></div>
                <span>--success</span>
              </div>
              <div class="color-item">
                <div class="color-swatch" style="background: var(--warning)"></div>
                <span>--warning</span>
              </div>
              <div class="color-item">
                <div class="color-swatch" style="background: var(--danger)"></div>
                <span>--danger</span>
              </div>
              <div class="color-item">
                <div class="color-swatch" style="background: var(--bg-primary); border: 1px solid var(--border-color)"></div>
                <span>--bg-primary</span>
              </div>
              <div class="color-item">
                <div class="color-swatch" style="background: var(--bg-secondary)"></div>
                <span>--bg-secondary</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import Avatar from '@/components/common/Avatar.vue'
import Modal from '@/components/common/Modal.vue'
import Pagination from '@/components/common/Pagination.vue'
import MessageBubble from '@/components/common/MessageBubble.vue'
import Toolbar from '@/components/editor/Toolbar.vue'
import CodeBlock from '@/components/editor/CodeBlock.vue'
import ImagePreview from '@/components/editor/ImagePreview.vue'
import FriendCard from '@/components/social/FriendCard.vue'
import ChatList from '@/components/social/ChatList.vue'
import InvitationCard from '@/components/social/InvitationCard.vue'
import { useThemeStore } from '@/stores/theme'
import type { Conversation, CollabInvitation, Friend } from '@/types'

const themeStore = useThemeStore()

const sections = [
  { id: 'common', title: '通用组件' },
  { id: 'editor', title: '编辑器组件' },
  { id: 'social', title: '社交组件' },
  { id: 'theme', title: '主题系统' },
]

const activeSection = ref('common')
const showModal = ref(false)
const currentPage = ref(1)
const showPreview = ref(false)
const previewSrc = ref('')

const sampleCode = `import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string>('')
  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    token.value = res.token
    user.value = res.user
  }

  return { user, token, isLoggedIn, login }
})`

const sampleConversations: Conversation[] = [
  {
    id: 1,
    userId: 2,
    username: 'zhangsan',
    nickname: '张三',
    avatar: '',
    lastMessage: '好的，明天见',
    unreadCount: 2,
    updatedAt: '2024-01-15 10:30:00',
  },
  {
    id: 2,
    userId: 3,
    username: 'lisi',
    nickname: '李四',
    avatar: '',
    lastMessage: '文档已经更新了',
    unreadCount: 0,
    updatedAt: '2024-01-14 16:20:00',
  },
]

const sampleFriends: Friend[] = [
  {
    id: 1,
    userId: 2,
    username: 'zhangsan',
    nickname: '张三',
    avatar: '',
    unreadCount: 2,
  },
  {
    id: 2,
    userId: 3,
    username: 'lisi',
    nickname: '李四',
    avatar: '',
    unreadCount: 0,
  },
]

function handleCommand(cmd: string) {
  console.log('Command:', cmd)
}

function previewImage(e: Event) {
  previewSrc.value = (e.target as HTMLImageElement).src
  showPreview.value = true
}

function handleAccept(invitation: CollabInvitation) {
  console.log('Accept:', invitation.id)
}

function handleReject(invitation: CollabInvitation) {
  console.log('Reject:', invitation.id)
}
</script>

<style scoped lang="scss">
.components-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);

  h1 {
    flex: 1;
    font-size: 20px;
    font-weight: 600;
  }
}

.back-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;

  &:hover {
    color: var(--primary);
  }
}

.component-nav {
  display: flex;
  gap: 8px;
  padding: 12px 24px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  overflow-x: auto;

  a {
    padding: 6px 16px;
    border-radius: var(--radius-full);
    font-size: 13px;
    color: var(--text-secondary);
    text-decoration: none;
    white-space: nowrap;
    transition: var(--transition);

    &:hover {
      background: var(--bg-secondary);
    }

    &.active {
      background: var(--primary);
      color: #fff;
    }
  }
}

.component-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.component-section {
  margin-bottom: 48px;

  h2 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 24px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-color);
  }
}

.component-demo {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid var(--border-color);

  h3 {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .desc {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 16px;
  }
}

.demo-preview {
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.demo-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.demo-btn {
  padding: 8px 16px;
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

.chat-demo {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-trigger {
  width: 200px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    opacity: 0.9;
  }
}

.theme-label {
  margin-left: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.color-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .color-swatch {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
  }

  span {
    font-size: 11px;
    color: var(--text-secondary);
    font-family: var(--mono);
  }
}
</style>
