<template>
  <div class="share-view-page">
    <header class="share-header">
      <h1>{{ doc?.title || '加载中...' }}</h1>
      <div class="share-header-right">
        <span class="share-perm-tag">{{ permission === 'edit' ? '可编辑' : '仅查看' }}</span>
        <router-link v-if="isOwner" to="/" class="btn-back">返回文档列表</router-link>
      </div>
    </header>

    <div v-if="loading" class="loading-state">加载中...</div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <router-link to="/login">返回登录</router-link>
    </div>

    <div v-else class="share-content">
      <div
        ref="editorRef"
        class="share-editor"
        :contenteditable="permission === 'edit'"
        @input="onInput"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { shareApi } from '@/api/document'
import type { Document } from '@/types'

const route = useRoute()
const token = route.params.token as string

const doc = ref<Document | null>(null)
const permission = ref<'view' | 'edit'>('view')
const loading = ref(true)
const error = ref('')
const editorRef = ref<HTMLElement | null>(null)
const isOwner = ref(false)

onMounted(async () => {
  try {
    doc.value = await shareApi.getByToken(token)
    permission.value = doc.value.permission === 'editor' ? 'edit' : 'view'
    if (editorRef.value && doc.value.content) {
      editorRef.value.innerHTML = doc.value.content
    }
  } catch {
    error.value = '分享链接无效或已过期'
  } finally {
    loading.value = false
  }
})

function onInput() {
  // Save content if editable
}
</script>

<style scoped lang="scss">
.share-view-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

.share-header {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  h1 {
    font-size: 18px;
    font-weight: 600;
  }
}

.share-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.share-perm-tag {
  padding: 4px 12px;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.btn-back {
  padding: 6px 16px;
  background: var(--primary);
  color: #fff;
  border-radius: var(--radius-sm);
  font-size: 13px;
  text-decoration: none;

  &:hover {
    background: var(--primary-hover);
    text-decoration: none;
  }
}

.loading-state,
.error-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  gap: 12px;
}

.error-state {
  color: var(--danger);
}

.share-content {
  flex: 1;
  overflow-y: auto;
}

.share-editor {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 80px;
  background: var(--bg-primary);
  min-height: calc(100vh - 60px);
  font-size: 15px;
  line-height: 1.8;

  &[contenteditable="false"] {
    cursor: default;
  }
}
</style>
