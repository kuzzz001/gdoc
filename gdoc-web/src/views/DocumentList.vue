<template>
  <div class="doc-list-page">
    <header class="page-header">
      <div class="header-left">
        <h1>吉智文档</h1>
      </div>
      <div class="header-center">
        <div class="search-bar">
          <input v-model="searchKeyword" placeholder="搜索文档..." @keydown.enter="handleSearch" />
          <button @click="handleSearch">搜索</button>
          <button v-if="isSearching" class="btn-clear" @click="clearSearch">清除</button>
        </div>
      </div>
      <div class="header-right">
        <nav class="header-nav">
          <router-link to="/" class="nav-link active">文档</router-link>
          <router-link to="/social" class="nav-link">社交</router-link>
        </nav>
        <div class="notification-bell" @click="showNotifications = !showNotifications">
          &#128276;
          <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
        </div>
        <div class="user-menu">
          <Avatar :text="userStore.nickname || userStore.username" :src="userStore.avatar" size="sm" />
          <span class="user-name">{{ userStore.nickname || userStore.username }}</span>
          <button class="btn-logout" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <div class="page-body">
      <FolderTree @select="handleFolderSelect" />

      <main class="page-content">
        <div class="content-header">
          <h2>{{ currentViewTitle }}</h2>
          <div class="header-actions">
            <button class="btn-import" @click="showImportModal = true">导入</button>
            <button class="btn-create" @click="handleCreate">
              <span>+</span> 新建文档
            </button>
          </div>
        </div>

        <!-- Tags -->
        <div v-if="tags.length > 0" class="tags-bar">
          <span
            v-for="tag in tags"
            :key="tag.id"
            class="tag-chip"
            :class="{ active: selectedTagId === tag.id }"
            @click="filterByTag(tag.id)"
          >{{ tag.name }}</span>
          <span v-if="selectedTagId" class="tag-chip clear-tag" @click="selectedTagId = null">清除筛选</span>
        </div>

        <!-- Recycle bin view -->
        <template v-if="currentFolderId === 'trash'">
          <div v-if="trashDocs.length === 0" class="empty-state">
            <p>回收站为空</p>
          </div>
          <div v-else class="doc-grid">
            <div v-for="doc in trashDocs" :key="doc.id" class="doc-card trash-card">
              <div class="doc-card-header">
                <div class="doc-icon">
                  <svg viewBox="0 0 24 24" fill="none"><path d="M6 2h9l5 5v13a2 2 0 01-2 2H6a2 2 0 01-2-2V4a2 2 0 012-2z" stroke="#999" stroke-width="1.5"/><path d="M14 2v5h5" stroke="#999" stroke-width="1.5"/></svg>
                </div>
                <div class="doc-title">{{ doc.title }}</div>
              </div>
              <div class="doc-card-footer">
                <span class="doc-meta">{{ formatDate(doc.updatedAt) }}</span>
                <div class="doc-actions">
                  <button class="action-btn restore" @click="restoreDoc(doc.id)" title="恢复">&#8634;</button>
                  <button class="action-btn delete" @click="permanentDeleteDoc(doc.id)" title="永久删除">&times;</button>
                </div>
              </div>
            </div>
          </div>
          <div v-if="trashDocs.length > 0" class="trash-actions">
            <button class="btn-empty-trash" @click="emptyTrash">清空回收站</button>
          </div>
        </template>

        <!-- Document list view -->
        <template v-else>
          <div v-if="docStore.loading" class="loading-state">加载中...</div>

          <div v-else-if="filteredDocs.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg viewBox="0 0 64 64" fill="none"><rect x="12" y="8" width="40" height="48" rx="4" stroke="#dadce0" stroke-width="2"/><path d="M24 24h16M24 32h16M24 40h10" stroke="#dadce0" stroke-width="2" stroke-linecap="round"/></svg>
            </div>
            <p>{{ isSearching ? '未找到匹配文档' : '还没有文档' }}</p>
            <button v-if="!isSearching" class="btn-create-inline" @click="handleCreate">创建第一个文档</button>
          </div>

          <div v-else class="doc-grid">
            <div v-for="doc in filteredDocs" :key="doc.id" class="doc-card">
              <div class="doc-card-header" @click="openDoc(doc.id)">
                <div class="doc-icon">
                  <svg viewBox="0 0 24 24" fill="none"><path d="M6 2h9l5 5v13a2 2 0 01-2 2H6a2 2 0 01-2-2V4a2 2 0 012-2z" stroke="#4285f4" stroke-width="1.5"/><path d="M14 2v5h5" stroke="#4285f4" stroke-width="1.5"/></svg>
                </div>
                <div class="doc-title">{{ doc.title }}</div>
              </div>
              <div class="doc-card-footer">
                <span class="doc-meta">{{ formatDate(doc.updatedAt) }}</span>
                <div class="doc-actions">
                  <button class="action-btn export" @click.stop="showExportMenu(doc.id)" title="导出">&#8689;</button>
                  <button class="action-btn delete" @click.stop="handleDelete(doc.id)" title="删除">&times;</button>
                </div>
              </div>
            </div>
          </div>

          <Pagination
            v-if="docStore.total > 20"
            :current-page="currentPage"
            :total="docStore.total"
            :page-size="20"
            @change="handlePageChange"
          />
        </template>
      </main>

      <!-- Notification panel overlay -->
      <div v-if="showNotifications" class="notification-overlay">
        <NotificationCenter ref="notifCenterRef" />
      </div>
    </div>

    <!-- Import Modal -->
    <Modal v-model:visible="showImportModal" title="导入文档" size="md">
      <div class="import-content">
        <div class="import-section">
          <h4>导入 Markdown</h4>
          <input v-model="importTitle" placeholder="文档标题" />
          <input type="file" accept=".md" @change="handleFileSelect" />
          <button class="btn-import-action" :disabled="!importFile" @click="handleImport">导入</button>
        </div>
      </div>
    </Modal>

    <!-- Export Menu -->
    <Modal v-model:visible="showExportModal" title="导出文档" size="sm">
      <div class="export-options">
        <button class="export-btn" @click="handleExport('pdf')">&#128196; 导出 HTML</button>
        <button class="export-btn" @click="handleExport('word')">&#128209; 导出 Word</button>
        <button class="export-btn" @click="handleExport('markdown')">&#128221; 导出 Markdown</button>
      </div>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import Avatar from '@/components/common/Avatar.vue'
import Pagination from '@/components/common/Pagination.vue'
import FolderTree from '@/components/common/FolderTree.vue'
import NotificationCenter from '@/components/common/NotificationCenter.vue'
import Modal from '@/components/common/Modal.vue'
import { useUserStore } from '@/stores/user'
import { useDocumentStore } from '@/stores/document'
import { searchApi, recycleBinApi, tagApi, exportApi } from '@/api/search'
import { notificationApi } from '@/api/notification'
import type { Document } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const docStore = useDocumentStore()
const currentPage = ref(1)
const searchKeyword = ref('')
const isSearching = ref(false)
const currentFolderId = ref<number | null | 'trash'>(null)
const trashDocs = ref<Document[]>([])
const tags = ref<any[]>([])
const selectedTagId = ref<number | null>(null)
const showNotifications = ref(false)
const unreadCount = ref(0)
const notifCenterRef = ref<InstanceType<typeof NotificationCenter> | null>(null)

const showImportModal = ref(false)
const importTitle = ref('')
const importFile = ref<File | null>(null)
const showExportModal = ref(false)
const exportDocId = ref<number | null>(null)

const currentViewTitle = computed(() => {
  if (currentFolderId.value === 'trash') return '回收站'
  if (isSearching.value) return `搜索: ${searchKeyword.value}`
  if (currentFolderId.value) return '文件夹文档'
  return '我的文档'
})

const filteredDocs = computed(() => {
  if (!selectedTagId.value) return docStore.documents
  return docStore.documents
})

onMounted(async () => {
  docStore.fetchDocuments()
  loadTags()
  loadUnreadCount()
})

async function loadTags() {
  try {
    tags.value = await tagApi.list()
  } catch { tags.value = [] }
}

async function loadUnreadCount() {
  try {
    unreadCount.value = await notificationApi.unreadCount()
  } catch { unreadCount.value = 0 }
}

function openDoc(id: number) {
  router.push(`/editor/${id}`)
}

async function handleCreate() {
  const doc = await docStore.createDocument()
  router.push(`/editor/${doc.id}`)
}

async function handleDelete(id: number) {
  if (confirm('确定删除此文档？')) {
    await docStore.deleteDocument(id)
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  docStore.fetchDocuments(page)
}

async function handleSearch() {
  if (!searchKeyword.value.trim()) return
  isSearching.value = true
  try {
    const res = await searchApi.search(searchKeyword.value.trim(), currentPage.value)
    docStore.documents = res.records
    docStore.total = res.total
  } catch { /* ignore */ }
}

function clearSearch() {
  searchKeyword.value = ''
  isSearching.value = false
  docStore.fetchDocuments()
}

function handleFolderSelect(id: number | null | 'trash') {
  currentFolderId.value = id
  if (id === 'trash') {
    loadTrashDocs()
  } else {
    docStore.fetchDocuments()
  }
}

async function loadTrashDocs() {
  try {
    const res = await recycleBinApi.list()
    trashDocs.value = res.records
  } catch { trashDocs.value = [] }
}

async function restoreDoc(id: number) {
  try {
    await recycleBinApi.restore(id)
    loadTrashDocs()
  } catch { /* ignore */ }
}

async function permanentDeleteDoc(id: number) {
  if (!confirm('永久删除无法恢复，确定？')) return
  try {
    await recycleBinApi.permanentDelete(id)
    loadTrashDocs()
  } catch { /* ignore */ }
}

async function emptyTrash() {
  if (!confirm('确定清空回收站？此操作不可恢复！')) return
  try {
    await recycleBinApi.emptyBin()
    trashDocs.value = []
  } catch { /* ignore */ }
}

function filterByTag(tagId: number) {
  selectedTagId.value = selectedTagId.value === tagId ? null : tagId
}

function showExportMenu(docId: number) {
  exportDocId.value = docId
  showExportModal.value = true
}

function handleExport(format: string) {
  if (!exportDocId.value) return
  const url = format === 'pdf' ? exportApi.exportPdf(exportDocId.value)
    : format === 'word' ? exportApi.exportWord(exportDocId.value)
    : exportApi.exportMarkdown(exportDocId.value)
  window.open(url, '_blank')
  showExportModal.value = false
}

function handleFileSelect(e: Event) {
  const target = e.target as HTMLInputElement
  importFile.value = target.files?.[0] || null
}

async function handleImport() {
  if (!importFile.value) return
  try {
    await exportApi.importMarkdown(importTitle.value || '导入的文档', importFile.value)
    showImportModal.value = false
    importTitle.value = ''
    importFile.value = null
    docStore.fetchDocuments()
  } catch {
    alert('导入失败')
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped lang="scss">
.doc-list-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

.page-header {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left h1 {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
}

.header-center {
  flex: 1;
  max-width: 400px;
  margin: 0 24px;
}

.search-bar {
  display: flex;
  gap: 6px;

  input {
    flex: 1;
    padding: 6px 12px;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    font-size: 13px;
    background: var(--bg-secondary);
    color: var(--text-primary);
    outline: none;

    &:focus { border-color: var(--primary); }
  }

  button {
    padding: 6px 14px;
    background: var(--primary);
    color: #fff;
    border: none;
    border-radius: var(--radius-md);
    font-size: 13px;
    cursor: pointer;
  }

  .btn-clear {
    background: transparent;
    color: var(--text-secondary);
    border: 1px solid var(--border-color);
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-nav {
  display: flex;
  gap: 12px;
}

.nav-link {
  font-size: 14px;
  color: var(--text-secondary);
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  transition: var(--transition);
  text-decoration: none;

  &:hover { background: var(--bg-tertiary); text-decoration: none; }
  &.active { color: var(--primary); background: var(--primary-light); }
}

.notification-bell {
  position: relative;
  cursor: pointer;
  font-size: 18px;
  padding: 4px;
}

.badge {
  position: absolute;
  top: -4px;
  right: -6px;
  background: var(--danger);
  color: #fff;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name { font-size: 13px; color: var(--text-primary); }

.btn-logout {
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: transparent;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition);

  &:hover { color: var(--danger); border-color: var(--danger); }
}

.page-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.page-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  overflow-y: auto;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  h2 { font-size: 18px; font-weight: 600; }
}

.header-actions {
  display: flex;
  gap: 8px;
}

.btn-import {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;

  &:hover { border-color: var(--primary); color: var(--primary); }
}

.btn-create {
  padding: 8px 20px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);

  &:hover { background: var(--primary-hover); }
}

.tags-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.tag-chip {
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  font-size: 12px;
  cursor: pointer;
  transition: var(--transition);
  color: var(--text-secondary);

  &:hover { border-color: var(--primary); color: var(--primary); }
  &.active { background: var(--primary-light); color: var(--primary); border-color: var(--primary); }
  &.clear-tag { color: var(--danger); border-color: var(--danger); }
}

.doc-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.doc-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: var(--transition);

  &:hover { box-shadow: var(--shadow-md); border-color: var(--primary); }
}

.trash-card { opacity: 0.7; }

.doc-card-header {
  padding: 20px 16px 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.doc-icon {
  width: 48px;
  height: 48px;
  svg { width: 100%; height: 100%; }
}

.doc-title {
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.doc-card-footer {
  padding: 10px 16px;
  border-top: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.doc-meta { font-size: 12px; color: var(--text-secondary); }

.doc-actions { display: flex; gap: 4px; }

.action-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--text-secondary);
  font-size: 16px;
  cursor: pointer;
  transition: var(--transition);

  &:hover { background: var(--danger-light); color: var(--danger); }
  &.restore:hover { background: var(--success-light); color: var(--success); }
  &.export:hover { background: var(--primary-light); color: var(--primary); }
}

.loading-state, .empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  svg { width: 100%; height: 100%; }
}

.empty-state p { margin-bottom: 16px; }

.btn-create-inline {
  padding: 8px 20px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  cursor: pointer;
  &:hover { background: var(--primary-hover); }
}

.trash-actions {
  margin-top: 16px;
  text-align: center;
}

.btn-empty-trash {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid var(--danger);
  border-radius: var(--radius-md);
  color: var(--danger);
  font-size: 13px;
  cursor: pointer;
  &:hover { background: var(--danger); color: #fff; }
}

.notification-overlay {
  position: fixed;
  top: 60px;
  right: 24px;
  width: 360px;
  height: 480px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  z-index: 200;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.import-content {
  padding: 16px 0;
}

.import-section {
  h4 { font-size: 14px; font-weight: 600; margin-bottom: 12px; }

  input {
    display: block;
    width: 100%;
    padding: 8px 12px;
    margin-bottom: 8px;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-sm);
    font-size: 13px;
    background: var(--bg-secondary);
    color: var(--text-primary);
  }
}

.btn-import-action {
  padding: 8px 20px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  &:disabled { opacity: 0.5; cursor: not-allowed; }
}

.export-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.export-btn {
  padding: 12px 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  font-size: 14px;
  cursor: pointer;
  text-align: left;
  transition: var(--transition);
  color: var(--text-primary);

  &:hover { border-color: var(--primary); background: var(--primary-light); }
}
</style>