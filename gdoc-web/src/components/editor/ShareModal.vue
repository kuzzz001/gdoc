<script setup lang="ts">
import { ref, watch } from 'vue'
import Modal from '@/components/common/Modal.vue'
import Avatar from '@/components/common/Avatar.vue'
import { shareApi, collaboratorApi } from '@/api/document'
import type { Collaborator } from '@/types'

const props = defineProps<{
  visible: boolean
  docId: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const shareLink = ref('')
const sharePermission = ref<'view' | 'edit'>('view')
const collaborators = ref<Collaborator[]>([])
const collabUserId = ref('')
const collabRole = ref<'editor' | 'viewer'>('editor')

watch(() => props.visible, (val) => {
  if (val) {
    loadCollaborators()
  }
})

async function loadCollaborators() {
  try {
    collaborators.value = await collaboratorApi.list(props.docId)
  } catch {
    collaborators.value = []
  }
}

async function createShareLink() {
  try {
    const link = await shareApi.create(props.docId, { permission: sharePermission.value })
    shareLink.value = `${window.location.origin}/share/${link.token}`
  } catch {
    // handled by global toast
  }
}

function copyShareLink() {
  navigator.clipboard.writeText(shareLink.value)
}

async function addCollaborator() {
  const userId = Number(collabUserId.value)
  if (!userId) return
  try {
    await collaboratorApi.add(props.docId, { userId, role: collabRole.value })
    collabUserId.value = ''
    loadCollaborators()
  } catch {
    // handled by global toast
  }
}

async function removeCollaborator(userId: number) {
  try {
    await collaboratorApi.remove(props.docId, userId)
    loadCollaborators()
  } catch {
    // handled by global toast
  }
}

function close() {
  emit('update:visible', false)
}
</script>

<template>
  <Modal :visible="visible" title="分享文档" size="lg" @update:visible="close">
    <div class="share-content">
      <div class="share-link-section">
        <label>分享链接</label>
        <div class="share-link-input">
          <input :value="shareLink" readonly placeholder="点击创建按钮生成分享链接" />
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
</template>

<style scoped lang="scss">
.share-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.share-link-section {
  label { display: block; margin-bottom: 8px; font-weight: 500; }
}

.share-link-input {
  display: flex; gap: 8px;
  input { flex: 1; padding: 8px 12px; border: 1px solid var(--border-color); border-radius: 6px; }
  button { padding: 8px 16px; background: var(--accent-color); color: #fff; border: none; border-radius: 6px; cursor: pointer; }
}

.share-perm {
  margin-top: 12px;
  select { padding: 6px 12px; border: 1px solid var(--border-color); border-radius: 6px; margin-left: 8px; }
}

.btn-create-link {
  margin-top: 12px;
  padding: 8px 20px;
  background: var(--accent-color);
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.collaborators-section {
  h4 { margin-bottom: 12px; }
}

.add-collaborator {
  display: flex; gap: 8px; margin-bottom: 12px;
  input { flex: 1; padding: 8px 12px; border: 1px solid var(--border-color); border-radius: 6px; }
  select { padding: 6px 12px; border: 1px solid var(--border-color); border-radius: 6px; }
  button { padding: 8px 16px; background: var(--accent-color); color: #fff; border: none; border-radius: 6px; cursor: pointer; }
}

.collaborator-item {
  display: flex; align-items: center; gap: 8px; padding: 8px 0; border-bottom: 1px solid var(--border-color);
}

.role-tag {
  font-size: 12px; padding: 2px 8px; border-radius: 4px; background: var(--bg-tertiary);
}

.btn-remove {
  margin-left: auto; padding: 4px 12px; background: #ff4d4f; color: #fff; border: none; border-radius: 4px; cursor: pointer;
}

.empty-hint { color: var(--text-secondary); padding: 12px 0; }
</style>