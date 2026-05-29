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
        <button class="btn-comment" @click="showComments = !showComments">评论</button>
        <button class="btn-share" @click="showShareModal = true">分享</button>
      </div>
    </header>

    <div class="editor-body">
      <TipTapEditor ref="editorCompRef" :content="editorContent" @change="onContentChange" />
      <div v-if="showComments" class="comment-sidebar">
        <CommentPanel :doc-id="docId" />
      </div>
    </div>

    <ShareModal v-model:visible="showShareModal" :doc-id="docId" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TipTapEditor from '@/components/editor/TipTapEditor.vue'
import CommentPanel from '@/components/editor/CommentPanel.vue'
import ShareModal from '@/components/editor/ShareModal.vue'
import { useDocumentStore } from '@/stores/document'
import { useCollabStore } from '@/stores/collab'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const docStore = useDocumentStore()
const collabStore = useCollabStore()
const userStore = useUserStore()
const toast = useToast()

const docId = computed(() => Number(route.params.id))
const docTitle = ref('未命名文档')
const editorCompRef = ref<InstanceType<typeof TipTapEditor> | null>(null)
const editorContent = ref('')
const saveStatus = ref('已保存')
const editingTitle = ref(false)
const titleInput = ref('')
const showShareModal = ref(false)
const showComments = ref(false)

let saveTimer: ReturnType<typeof setTimeout> | null = null

onMounted(async () => {
  const doc = await docStore.fetchDocument(docId.value)
  if (doc) {
    docTitle.value = doc.title
    editorContent.value = doc.content || ''
  }
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

function onContentChange() {
  saveStatus.value = '编辑中...'
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    saveContent()
  }, 1500)
}

async function saveContent() {
  if (!editorCompRef.value) return
  const content = editorCompRef.value.getHTML()
  try {
    await docStore.updateDocument(docId.value, { title: docTitle.value, content })
    saveStatus.value = '已保存'
  } catch {
    saveStatus.value = '保存失败'
  }
}

function startEditTitle() {
  editingTitle.value = true
  titleInput.value = docTitle.value
}

async function saveTitle() {
  if (titleInput.value.trim()) {
    docTitle.value = titleInput.value.trim()
    try {
      await docStore.updateDocument(docId.value, { title: docTitle.value })
    } catch {
      toast.error('保存标题失败')
    }
  }
  editingTitle.value = false
}

function cancelEditTitle() {
  editingTitle.value = false
}

function connectWebSocket() {
  collabStore.connect(docId.value, userStore.userId)
}

function disconnectWebSocket() {
  collabStore.disconnect()
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
  border-radius: 6px;
  color: var(--text-primary);
  text-decoration: none;
  font-size: 18px;
  &:hover { background: var(--bg-tertiary); }
}

.doc-title {
  font-size: 16px;
  cursor: pointer;
  &:hover { color: var(--accent-color); }
}

.title-input {
  font-size: 16px;
  padding: 2px 8px;
  border: 1px solid var(--accent-color);
  border-radius: 4px;
  outline: none;
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

.btn-comment, .btn-share {
  padding: 6px 16px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-primary);
  cursor: pointer;
  &:hover { background: var(--bg-tertiary); }
}

.editor-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.comment-sidebar {
  width: 320px;
  border-left: 1px solid var(--border-color);
  overflow-y: auto;
  background: var(--bg-primary);
}
</style>