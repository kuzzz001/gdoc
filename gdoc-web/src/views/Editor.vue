<template>
  <div class="editor-page">
    <header class="editor-header">
      <div class="header-left">
        <router-link to="/" class="back-btn">&larr;</router-link>
        <input
          v-if="editingTitle"
          v-model="titleInput"
          class="title-input"
          @blur="saveTitle"
          @keyup.enter="saveTitle"
          @keyup.escape="cancelEditTitle"
        />
        <h2 v-else class="doc-title" @click="startEditTitle">{{ docTitle }}</h2>
        <span class="save-status">{{ saveStatus }}</span>
      </div>
      <div class="header-right">
        <div class="online-users">
          <span
            v-for="cursor in collabStore.cursors"
            :key="cursor.userId"
            class="user-badge"
            :style="{ background: cursor.color }"
            :title="cursor.username"
          >
            {{ cursor.username.charAt(0) }}
          </span>
        </div>
        <button class="btn-share" @click="showShareModal = true">分享</button>
      </div>
    </header>

    <div class="editor-body">
      <Toolbar @command="handleCommand" />
      <div
        ref="editorRef"
        class="editor-content"
        contenteditable="true"
        @input="onInput"
        @keydown="onKeyDown"
      ></div>
    </div>

    <Modal v-model:visible="showShareModal" title="分享文档" size="lg">
      <div class="share-content">
        <div class="share-link-section">
          <label>分享链接</label>
          <div class="share-link-input">
            <input :value="shareLink" readonly />
            <button @click="copyShareLink">复制</button>
          </div>
          <div class="share-perm">
            <label>权限</label>
            <select v-model="sharePermission">
              <option value="view">仅查看</option>
              <option value="edit">可编辑</option>
            </select>
          </div>
          <button class="btn-create-link" @click="createShareLink">创建分享链接</button>
        </div>

        <div class="collaborators-section">
          <h4>协作者</h4>
          <div class="add-collaborator">
            <input v-model="collabUserId" placeholder="输入用户ID" />
            <select v-model="collabRole">
              <option value="editor">可编辑</option>
              <option value="viewer">仅查看</option>
            </select>
            <button @click="addCollaborator">添加</button>
          </div>
          <div v-if="collaborators.length === 0" class="empty-hint">暂无协作者</div>
          <div v-for="c in collaborators" :key="c.userId" class="collaborator-item">
            <Avatar :text="c.nickname || c.username" size="sm" />
            <span>{{ c.nickname || c.username }}</span>
            <span class="role-tag">{{ c.role === 'editor' ? '可编辑' : '仅查看' }}</span>
            <button class="btn-remove" @click="removeCollaborator(c.userId)">移除</button>
          </div>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Toolbar from '@/components/editor/Toolbar.vue'
import Modal from '@/components/common/Modal.vue'
import Avatar from '@/components/common/Avatar.vue'
import { useDocumentStore } from '@/stores/document'
import { useCollabStore } from '@/stores/collab'
import { useUserStore } from '@/stores/user'
import { shareApi, collaboratorApi } from '@/api/document'
import type { Collaborator } from '@/types'

const route = useRoute()
const router = useRouter()
const docStore = useDocumentStore()
const collabStore = useCollabStore()
const userStore = useUserStore()

const docId = computed(() => Number(route.params.id))
const docTitle = ref('未命名文档')
const editorRef = ref<HTMLElement | null>(null)
const saveStatus = ref('已保存')
const editingTitle = ref(false)
const titleInput = ref('')
const showShareModal = ref(false)
const shareLink = ref('')
const sharePermission = ref<'view' | 'edit'>('view')
const collaborators = ref<Collaborator[]>([])
const collabUserId = ref('')
const collabRole = ref<'editor' | 'viewer'>('editor')

let saveTimer: ReturnType<typeof setTimeout> | null = null

onMounted(async () => {
  const doc = await docStore.fetchDocument(docId.value)
  if (doc) {
    docTitle.value = doc.title
    if (editorRef.value) {
      editorRef.value.innerHTML = doc.content || '<p><br></p>'
    }
  }
  loadCollaborators()
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

function onInput() {
  saveStatus.value = '编辑中...'
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    saveContent()
  }, 1500)
}

async function saveContent() {
  if (!editorRef.value) return
  const content = editorRef.value.innerHTML
  try {
    await docStore.fetchDocument(docId.value)
    saveStatus.value = '已保存'
  } catch {
    saveStatus.value = '保存失败'
  }
}

function handleCommand() {
  editorRef.value?.focus()
}

function onKeyDown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    saveContent()
  }
}

function startEditTitle() {
  editingTitle.value = true
  titleInput.value = docTitle.value
}

function saveTitle() {
  if (titleInput.value.trim()) {
    docTitle.value = titleInput.value.trim()
  }
  editingTitle.value = false
}

function cancelEditTitle() {
  editingTitle.value = false
}

async function loadCollaborators() {
  try {
    collaborators.value = await collaboratorApi.list(docId.value)
  } catch {
    collaborators.value = []
  }
}

async function createShareLink() {
  try {
    const link = await shareApi.create(docId.value, { permission: sharePermission.value })
    shareLink.value = `${window.location.origin}/share/${link.token}`
  } catch {
    alert('创建分享链接失败')
  }
}

function copyShareLink() {
  navigator.clipboard.writeText(shareLink.value)
  alert('链接已复制')
}

async function addCollaborator() {
  const userId = Number(collabUserId.value)
  if (!userId) return
  try {
    await collaboratorApi.add(docId.value, { userId, role: collabRole.value })
    collabUserId.value = ''
    loadCollaborators()
  } catch {
    alert('添加协作者失败')
  }
}

async function removeCollaborator(userId: number) {
  try {
    await collaboratorApi.remove(docId.value, userId)
    loadCollaborators()
  } catch {
    alert('移除协作者失败')
  }
}

function connectWebSocket() {
  // WebSocket connection for real-time collaboration
  // Will be implemented with STOMP/SockJS
}

function disconnectWebSocket() {
  // Clean up WebSocket connection
}
</script>

<style scoped lang="scss">
.editor-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

.editor-header {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  padding: 8px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-secondary);
  font-size: 18px;
  cursor: pointer;
  text-decoration: none;
  transition: var(--transition);

  &:hover {
    background: var(--bg-tertiary);
    text-decoration: none;
  }
}

.doc-title {
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: var(--transition);

  &:hover {
    background: var(--bg-tertiary);
  }
}

.title-input {
  font-size: 16px;
  font-weight: 600;
  padding: 4px 8px;
  border: 1.5px solid var(--primary);
  border-radius: var(--radius-sm);
  outline: none;
  width: 240px;
}

.save-status {
  font-size: 12px;
  color: var(--text-secondary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.online-users {
  display: flex;
  gap: 4px;
}

.user-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.btn-share {
  padding: 6px 16px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: var(--primary-hover);
  }
}

.editor-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-content {
  flex: 1;
  overflow-y: auto;
  padding: 40px 80px;
  background: var(--bg-primary);
  outline: none;
  font-size: 15px;
  line-height: 1.8;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;

  &:focus {
    outline: none;
  }

  :deep(p) {
    margin-bottom: 8px;
  }

  :deep(h1) {
    font-size: 28px;
    font-weight: 700;
    margin: 24px 0 12px;
  }

  :deep(h2) {
    font-size: 22px;
    font-weight: 600;
    margin: 20px 0 10px;
  }

  :deep(h3) {
    font-size: 18px;
    font-weight: 600;
    margin: 16px 0 8px;
  }

  :deep(blockquote) {
    border-left: 4px solid var(--border-color);
    padding: 8px 16px;
    margin: 12px 0;
    background: var(--bg-secondary);
    color: var(--text-secondary);
  }

  :deep(img) {
    max-width: 100%;
    border-radius: var(--radius-md);
    cursor: pointer;
  }
}

.share-content {
  .share-link-section {
    margin-bottom: 24px;

    label {
      display: block;
      font-size: 13px;
      font-weight: 500;
      color: var(--text-secondary);
      margin-bottom: 8px;
    }
  }

  .share-link-input {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;

    input {
      flex: 1;
      padding: 8px 12px;
      border: 1px solid var(--border-color);
      border-radius: var(--radius-sm);
      font-size: 13px;
      background: var(--bg-secondary);
      color: var(--text-secondary);
    }

    button {
      padding: 8px 16px;
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

  .share-perm {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    select {
      padding: 6px 12px;
      border: 1px solid var(--border-color);
      border-radius: var(--radius-sm);
      font-size: 13px;
    }
  }

  .btn-create-link {
    padding: 8px 20px;
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

.collaborators-section {
  h4 {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
  }
}

.add-collaborator {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;

  input {
    flex: 1;
    padding: 6px 10px;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-sm);
    font-size: 13px;
  }

  select {
    padding: 6px 10px;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-sm);
    font-size: 13px;
  }

  button {
    padding: 6px 16px;
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

.empty-hint {
  padding: 16px;
  text-align: center;
  color: var(--text-placeholder);
  font-size: 13px;
}

.collaborator-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-light);

  span {
    font-size: 13px;
  }
}

.role-tag {
  font-size: 11px !important;
  padding: 2px 8px;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: 10px;
}

.btn-remove {
  margin-left: auto;
  padding: 4px 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: transparent;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;

  &:hover {
    color: var(--danger);
    border-color: var(--danger);
  }
}
</style>
