<template>
  <div class="folder-node">
    <div class="node-row" :class="{ active: folder.id === selectedId }" @click.stop="$emit('select', folder.id)">
      <span class="toggle" @click.stop="expanded = !expanded">{{ expanded ? '&#9660;' : '&#9654;' }}</span>
      <span v-if="!editing" class="folder-name" @dblclick="startEdit">{{ folder.name }}</span>
      <input v-else v-model="editName" class="edit-input" @keydown.enter="saveEdit" @keydown.escape="editing = false" @blur="saveEdit" />
      <span class="node-actions">
        <button class="btn-rename" @click.stop="startEdit" title="重命名">&#9998;</button>
        <button class="btn-del" @click.stop="$emit('delete', folder.id)" title="删除">&times;</button>
      </span>
    </div>
    <div v-if="expanded && folder.children && folder.children.length > 0" class="children">
      <FolderNode
        v-for="child in folder.children"
        :key="child.id"
        :folder="child"
        :selected-id="selectedId"
        @select="$emit('select', $event)"
        @rename="$emit('rename', $event.id, $event.name)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface FolderItem {
  id: number
  name: string
  parentId: number
  children: FolderItem[]
}

const props = defineProps<{
  folder: FolderItem
  selectedId: number | null | 'trash'
}>()

const emit = defineEmits<{
  select: [id: number]
  rename: [payload: { id: number; name: string }]
  delete: [id: number]
}>()

const expanded = ref(true)
const editing = ref(false)
const editName = ref('')

function startEdit() {
  editing.value = true
  editName.value = props.folder.name
}

function saveEdit() {
  if (editName.value.trim() && editName.value.trim() !== props.folder.name) {
    emit('rename', { id: props.folder.id, name: editName.value.trim() })
  }
  editing.value = false
}
</script>

<style scoped>
.folder-node { user-select: none; }

.node-row {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  font-size: 13px;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: var(--transition);
}

.node-row:hover { background: var(--bg-tertiary); }
.node-row.active { background: var(--primary-light); color: var(--primary); }

.toggle {
  font-size: 8px;
  width: 14px;
  text-align: center;
  color: var(--text-secondary);
}

.folder-name { flex: 1; }

.node-actions {
  display: none;
  gap: 2px;
}

.node-row:hover .node-actions { display: flex; }

.node-actions button {
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 50%;
  background: transparent;
  font-size: 12px;
  cursor: pointer;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-del:hover { color: var(--danger); }

.edit-input {
  flex: 1;
  padding: 2px 6px;
  border: 1px solid var(--primary);
  border-radius: var(--radius-sm);
  font-size: 12px;
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.children { padding-left: 16px; }
</style>