<template>
  <div class="folder-tree">
    <div class="tree-header">
      <span class="tree-title">文件夹</span>
      <button class="btn-add" @click="showCreateFolder = true" title="新建文件夹">+</button>
    </div>

    <div v-if="showCreateFolder" class="create-folder">
      <input v-model="newFolderName" placeholder="文件夹名称" @keydown.enter="createFolder" @keydown.escape="showCreateFolder = false" />
      <button @click="createFolder">确定</button>
      <button @click="showCreateFolder = false">取消</button>
    </div>

    <div class="tree-root" @click="selectFolder(null)">
      <span :class="{ active: selectedFolderId === null }" class="tree-item root-item">
        &#128193; 全部文档
      </span>
    </div>

    <div class="tree-nodes">
      <FolderNode
        v-for="folder in folders"
        :key="folder.id"
        :folder="folder"
        :selected-id="selectedFolderId"
        @select="selectFolder"
        @rename="renameFolder"
        @delete="deleteFolder"
      />
    </div>

    <div class="tree-sections">
      <div class="tree-item" :class="{ active: selectedFolderId === 'trash' }" @click="selectFolder('trash')">
        &#128465; 回收站
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import FolderNode from './FolderNode.vue'
import { folderApi } from '@/api/folder'

interface FolderItem {
  id: number
  name: string
  parentId: number
  children: FolderItem[]
}

const emit = defineEmits<{
  select: [folderId: number | null | 'trash']
}>()

const folders = ref<FolderItem[]>([])
const selectedFolderId = ref<number | null | 'trash'>(null)
const showCreateFolder = ref(false)
const newFolderName = ref('')

async function loadFolders() {
  try {
    folders.value = await folderApi.list()
  } catch {
    folders.value = []
  }
}

function selectFolder(id: number | null | 'trash') {
  selectedFolderId.value = id
  emit('select', id)
}

async function createFolder() {
  if (!newFolderName.value.trim()) return
  try {
    await folderApi.create({ name: newFolderName.value.trim(), parentId: 0 })
    newFolderName.value = ''
    showCreateFolder.value = false
    loadFolders()
  } catch { /* ignore */ }
}

async function renameFolder(id: number, name: string) {
  try {
    await folderApi.rename(id, name)
    loadFolders()
  } catch { /* ignore */ }
}

async function deleteFolder(id: number) {
  if (!confirm('确定删除此文件夹？')) return
  try {
    await folderApi.delete(id)
    if (selectedFolderId.value === id) selectFolder(null)
    loadFolders()
  } catch { /* ignore */ }
}

onMounted(loadFolders)
</script>

<style scoped>
.folder-tree {
  width: 220px;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow-y: auto;
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 12px 8px;
}

.tree-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.btn-add {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-secondary);
  font-size: 16px;
  cursor: pointer;
}

.btn-add:hover { background: var(--bg-tertiary); }

.create-folder {
  display: flex;
  gap: 4px;
  padding: 4px 12px 8px;
}

.create-folder input {
  flex: 1;
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 12px;
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.create-folder button {
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: transparent;
  font-size: 11px;
  cursor: pointer;
  color: var(--text-secondary);
}

.tree-root { padding: 0 4px; }

.tree-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  font-size: 13px;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: var(--transition);
  color: var(--text-primary);
}

.tree-item:hover { background: var(--bg-tertiary); }
.tree-item.active { background: var(--primary-light); color: var(--primary); }

.tree-nodes { padding: 0 4px; flex: 1; }

.tree-sections {
  padding: 8px 4px;
  border-top: 1px solid var(--border-color);
}
</style>