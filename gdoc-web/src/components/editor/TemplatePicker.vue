<template>
  <div class="template-picker">
    <div class="picker-header">
      <h3>从模板创建</h3>
      <button class="btn-close" @click="$emit('close')">&times;</button>
    </div>
    <div class="category-tabs">
      <button
        v-for="cat in categories"
        :key="cat"
        :class="{ active: selectedCategory === cat }"
        @click="selectedCategory = cat"
      >{{ cat }}</button>
    </div>
    <div class="template-list">
      <div
        v-for="tpl in filteredTemplates"
        :key="tpl.id"
        class="template-card"
        @click="$emit('select', tpl)"
      >
        <div class="template-preview" v-html="tpl.content?.substring(0, 200) || ''"></div>
        <div class="template-info">
          <span class="template-name">{{ tpl.name }}</span>
          <span class="template-desc">{{ tpl.description }}</span>
        </div>
      </div>
      <div v-if="filteredTemplates.length === 0" class="empty-state">
        <p>暂无模板</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { templateApi, type Template } from '@/api/template'

const emit = defineEmits<{
  select: [tpl: Template]
  close: []
}>()

const templates = ref<Template[]>([])
const selectedCategory = ref('全部')

const categories = computed(() => {
  const cats = new Set(templates.value.map(t => t.category).filter(Boolean))
  return ['全部', ...cats]
})

const filteredTemplates = computed(() => {
  if (selectedCategory.value === '全部') return templates.value
  return templates.value.filter(t => t.category === selectedCategory.value)
})

onMounted(async () => {
  try {
    const res = await templateApi.list()
    templates.value = res.data || []
  } catch (e) {
    console.error('Failed to load templates', e)
  }
})
</script>

<style scoped>
.template-picker {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  max-height: 80vh;
  overflow-y: auto;
  padding: 20px;
  width: 600px;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.picker-header h3 {
  margin: 0;
  font-size: 16px;
}

.btn-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
}

.category-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.category-tabs button {
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  font-size: 13px;
  cursor: pointer;
  transition: var(--transition);
}

.category-tabs button.active {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.template-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.template-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 12px;
  cursor: pointer;
  transition: var(--transition);
}

.template-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.template-preview {
  font-size: 12px;
  color: var(--text-secondary);
  max-height: 80px;
  overflow: hidden;
  margin-bottom: 8px;
  line-height: 1.4;
}

.template-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.template-name {
  font-size: 14px;
  font-weight: 500;
}

.template-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}
</style>