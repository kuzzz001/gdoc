<template>
  <div class="doc-list-page">
    <header class="page-header">
      <div class="header-left">
        <h1>吉智文档</h1>
      </div>
      <div class="header-right">
        <nav class="header-nav">
          <router-link to="/" class="nav-link active">文档</router-link>
          <router-link to="/social" class="nav-link">社交</router-link>
        </nav>
        <div class="user-menu">
          <Avatar :text="userStore.nickname || userStore.username" :src="userStore.avatar" size="sm" />
          <span class="user-name">{{ userStore.nickname || userStore.username }}</span>
          <button class="btn-logout" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <main class="page-content">
      <div class="content-header">
        <h2>我的文档</h2>
        <button class="btn-create" @click="handleCreate">
          <span>+</span> 新建文档
        </button>
      </div>

      <div v-if="docStore.loading" class="loading-state">加载中...</div>

      <div v-else-if="docStore.documents.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="12" y="8" width="40" height="48" rx="4" stroke="#dadce0" stroke-width="2" />
            <path d="M24 24h16M24 32h16M24 40h10" stroke="#dadce0" stroke-width="2" stroke-linecap="round" />
          </svg>
        </div>
        <p>还没有文档</p>
        <button class="btn-create-inline" @click="handleCreate">创建第一个文档</button>
      </div>

      <div v-else class="doc-grid">
        <div v-for="doc in docStore.documents" :key="doc.id" class="doc-card">
          <div class="doc-card-header" @click="openDoc(doc.id)">
            <div class="doc-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 2h9l5 5v13a2 2 0 01-2 2H6a2 2 0 01-2-2V4a2 2 0 012-2z" stroke="#4285f4" stroke-width="1.5" />
                <path d="M14 2v5h5" stroke="#4285f4" stroke-width="1.5" />
              </svg>
            </div>
            <div class="doc-title">{{ doc.title }}</div>
          </div>
          <div class="doc-card-footer">
            <span class="doc-meta">{{ formatDate(doc.updatedAt) }}</span>
            <div class="doc-actions">
              <button class="action-btn" @click.stop="handleDelete(doc.id)" title="删除">
                &times;
              </button>
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
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Avatar from '@/components/common/Avatar.vue'
import Pagination from '@/components/common/Pagination.vue'
import { useUserStore } from '@/stores/user'
import { useDocumentStore } from '@/stores/document'

const router = useRouter()
const userStore = useUserStore()
const docStore = useDocumentStore()
const currentPage = ref(1)

onMounted(() => {
  docStore.fetchDocuments()
})

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
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  h2 {
    font-size: 18px;
    font-weight: 600;
  }
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

  &:hover {
    background: var(--primary-hover);
  }
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

  &:hover {
    box-shadow: var(--shadow-md);
    border-color: var(--primary);
  }
}

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

  svg {
    width: 100%;
    height: 100%;
  }
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

.doc-meta {
  font-size: 12px;
  color: var(--text-secondary);
}

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

  &:hover {
    background: var(--danger-light);
    color: var(--danger);
  }
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;

  svg {
    width: 100%;
    height: 100%;
  }
}

.empty-state p {
  margin-bottom: 16px;
}

.btn-create-inline {
  padding: 8px 20px;
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
</style>
