<template>
  <div class="comment-panel">
    <div class="panel-header">
      <h3>评论</h3>
      <div class="filter-tabs">
        <button :class="{ active: filter === 'all' }" @click="filter = 'all'">全部</button>
        <button :class="{ active: filter === 'open' }" @click="filter = 'open'">未解决</button>
        <button :class="{ active: filter === 'resolved' }" @click="filter = 'resolved'">已解决</button>
      </div>
    </div>

    <div class="new-comment">
      <textarea v-model="newComment" placeholder="添加评论..." rows="2" @keydown.ctrl.enter="submitComment" />
      <button class="btn-submit" :disabled="!newComment.trim()" @click="submitComment">发送</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="filteredComments.length === 0" class="empty">
      <p>暂无评论</p>
    </div>

    <div v-else class="comment-list">
      <div v-for="comment in filteredComments" :key="comment.id" class="comment-item" :class="{ resolved: comment.resolved }">
        <div class="comment-header">
          <Avatar :text="comment.nickname || comment.username" size="xs" />
          <span class="comment-author">{{ comment.nickname || comment.username }}</span>
          <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
          <div class="comment-actions">
            <button v-if="!comment.resolved" class="btn-resolve" @click="resolveComment(comment.id)" title="标记已解决">&#10003;</button>
            <button v-else class="btn-reopen" @click="reopenComment(comment.id)" title="重新打开">&#8634;</button>
            <button class="btn-delete" @click="deleteComment(comment.id)" title="删除">&times;</button>
          </div>
        </div>
        <div class="comment-body">{{ comment.content }}</div>

        <div v-if="comment.replies && comment.replies.length > 0" class="replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <Avatar :text="reply.nickname || reply.username" size="xs" />
            <span class="reply-author">{{ reply.nickname || reply.username }}</span>
            <span class="reply-content">{{ reply.content }}</span>
            <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
          </div>
        </div>

        <div class="reply-input">
          <input v-model="replyInputs[comment.id]" placeholder="回复..." @keydown.enter="submitReply(comment.id)" />
          <button :disabled="!replyInputs[comment.id]?.trim()" @click="submitReply(comment.id)">回复</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import Avatar from '@/components/common/Avatar.vue'
import { commentApi, type Comment } from '@/api/comment'

const props = defineProps<{ docId: number }>()

const comments = ref<Comment[]>([])
const loading = ref(false)
const filter = ref<'all' | 'open' | 'resolved'>('all')
const newComment = ref('')
const replyInputs = ref<Record<number, string>>({})

const filteredComments = computed(() => {
  if (filter.value === 'open') return comments.value.filter(c => !c.resolved)
  if (filter.value === 'resolved') return comments.value.filter(c => c.resolved)
  return comments.value
})

async function loadComments() {
  loading.value = true
  try {
    comments.value = await commentApi.list(props.docId)
  } catch {
    comments.value = []
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  if (!newComment.value.trim()) return
  try {
    await commentApi.create(props.docId, { content: newComment.value.trim() })
    newComment.value = ''
    loadComments()
  } catch {
    alert('评论失败')
  }
}

async function resolveComment(id: number) {
  try {
    await commentApi.resolve(id)
    loadComments()
  } catch { /* ignore */ }
}

async function reopenComment(id: number) {
  try {
    await commentApi.reopen(id)
    loadComments()
  } catch { /* ignore */ }
}

async function deleteComment(id: number) {
  if (!confirm('确定删除此评论？')) return
  try {
    await commentApi.delete(id)
    loadComments()
  } catch { /* ignore */ }
}

async function submitReply(commentId: number) {
  const content = replyInputs.value[commentId]?.trim()
  if (!content) return
  try {
    await commentApi.addReply(commentId, content)
    replyInputs.value[commentId] = ''
    loadComments()
  } catch { /* ignore */ }
}

function formatTime(dateStr: string) {
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return d.toLocaleDateString('zh-CN')
}

watch(() => props.docId, loadComments, { immediate: true })
</script>

<style scoped>
.comment-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-primary);
}

.panel-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.panel-header h3 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.filter-tabs {
  display: flex;
  gap: 4px;
}

.filter-tabs button {
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: transparent;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition);
}

.filter-tabs button.active {
  background: var(--primary-light);
  color: var(--primary);
  border-color: var(--primary);
}

.new-comment {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.new-comment textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 13px;
  resize: vertical;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-family: inherit;
}

.btn-submit {
  margin-top: 8px;
  padding: 6px 16px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
}

.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading, .empty {
  padding: 40px 16px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.comment-list {
  flex: 1;
  overflow-y: auto;
}

.comment-item {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.comment-item.resolved {
  opacity: 0.6;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.comment-author {
  font-size: 13px;
  font-weight: 500;
}

.comment-time {
  font-size: 11px;
  color: var(--text-secondary);
}

.comment-actions {
  margin-left: auto;
  display: flex;
  gap: 4px;
}

.comment-actions button {
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 50%;
  background: transparent;
  font-size: 14px;
  cursor: pointer;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-resolve:hover { color: var(--success); }
.btn-reopen:hover { color: var(--warning); }
.btn-delete:hover { color: var(--danger); }

.comment-body {
  font-size: 13px;
  line-height: 1.5;
  padding-left: 28px;
}

.replies {
  margin-top: 8px;
  padding-left: 28px;
  border-left: 2px solid var(--border-light);
}

.reply-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
  font-size: 12px;
}

.reply-author { font-weight: 500; }
.reply-content { color: var(--text-secondary); }
.reply-time { color: var(--text-placeholder); font-size: 11px; margin-left: auto; }

.reply-input {
  display: flex;
  gap: 6px;
  padding: 8px 0 0 28px;
}

.reply-input input {
  flex: 1;
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 12px;
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.reply-input button {
  padding: 4px 10px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 11px;
  cursor: pointer;
}

.reply-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>