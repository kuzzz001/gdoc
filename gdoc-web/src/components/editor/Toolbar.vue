<template>
  <div class="editor-toolbar">
    <div class="toolbar-group">
      <select v-model="fontName" @change="exec('fontName', fontName)" title="字体">
        <option value="">字体</option>
        <option v-for="f in fonts" :key="f" :value="f">{{ f }}</option>
      </select>
      <select v-model="fontSize" @change="exec('fontSize', fontSize)" title="字号">
        <option value="">字号</option>
        <option v-for="s in sizes" :key="s" :value="s">{{ s }}px</option>
      </select>
    </div>

    <div class="toolbar-divider" />

    <div class="toolbar-group">
      <button @click="exec('bold')" title="粗体 (Ctrl+B)" :class="{ active: isBold }">
        <strong>B</strong>
      </button>
      <button @click="exec('italic')" title="斜体 (Ctrl+I)" :class="{ active: isItalic }">
        <em>I</em>
      </button>
      <button @click="exec('underline')" title="下划线 (Ctrl+U)" :class="{ active: isUnderline }">
        <u>U</u>
      </button>
      <button @click="exec('strikeThrough')" title="删除线">
        <s>S</s>
      </button>
    </div>

    <div class="toolbar-divider" />

    <div class="toolbar-group">
      <input
        type="color"
        v-model="foreColor"
        @input="exec('foreColor', foreColor)"
        title="文字颜色"
        class="color-picker"
      />
      <input
        type="color"
        v-model="hiliteColor"
        @input="exec('hiliteColor', hiliteColor)"
        title="背景颜色"
        class="color-picker"
      />
    </div>

    <div class="toolbar-divider" />

    <div class="toolbar-group">
      <button @click="exec('justifyLeft')" title="左对齐">
        <span>&#9776;</span>
      </button>
      <button @click="exec('justifyCenter')" title="居中对齐">
        <span>&#9776;</span>
      </button>
      <button @click="exec('justifyRight')" title="右对齐">
        <span>&#9776;</span>
      </button>
    </div>

    <div class="toolbar-divider" />

    <div class="toolbar-group">
      <button @click="exec('insertUnorderedList')" title="无序列表">
        <span>&#8226;</span> 列表
      </button>
      <button @click="exec('insertOrderedList')" title="有序列表">
        <span>1.</span> 列表
      </button>
    </div>

    <div class="toolbar-divider" />

    <div class="toolbar-group">
      <button @click="exec('formatBlock', '<h1>')" title="标题1">H1</button>
      <button @click="exec('formatBlock', '<h2>')" title="标题2">H2</button>
      <button @click="exec('formatBlock', '<h3>')" title="标题3">H3</button>
      <button @click="exec('formatBlock', '<blockquote>')" title="引用">
        &ldquo;&rdquo;
      </button>
    </div>

    <div class="toolbar-divider" />

    <div class="toolbar-group">
      <button @click="insertLink" title="插入链接">
        <span>&#128279;</span>
      </button>
      <button @click="exec('insertHorizontalRule')" title="分割线">
        <span>&#9472;</span>
      </button>
    </div>

    <div class="toolbar-divider" />

    <div class="toolbar-group">
      <button @click="exec('undo')" title="撤销 (Ctrl+Z)">
        <span>&#8617;</span>
      </button>
      <button @click="exec('redo')" title="重做 (Ctrl+Y)">
        <span>&#8618;</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  command: [command: string, value?: string]
}>()

const fontName = ref('')
const fontSize = ref('')
const foreColor = ref('#000000')
const hiliteColor = ref('#ffffff')

const fonts = [
  'Arial',
  'Times New Roman',
  'Microsoft YaHei',
  'SimSun',
  'KaiTi',
  'SimHei',
  'FangSong',
  'Georgia',
  'Verdana',
  'Courier New',
]

const sizes = ['12', '14', '16', '18', '20', '24', '28', '32', '36', '48']

const isBold = ref(false)
const isItalic = ref(false)
const isUnderline = ref(false)

function exec(command: string, value?: string) {
  document.execCommand(command, false, value)
  emit('command', command, value)
}

function insertLink() {
  const url = prompt('请输入链接地址：')
  if (url) exec('createLink', url)
}
</script>

<style scoped lang="scss">
.editor-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px 12px;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  border-bottom: none;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 2px;

  button {
    min-width: 32px;
    height: 32px;
    padding: 0 8px;
    border: 1px solid transparent;
    border-radius: var(--radius-sm);
    background: transparent;
    color: var(--text-primary);
    font-size: 13px;
    cursor: pointer;
    transition: var(--transition);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 2px;

    &:hover {
      background: var(--bg-tertiary);
      border-color: var(--border-color);
    }

    &.active {
      background: var(--primary-light);
      border-color: var(--primary);
      color: var(--primary);
    }
  }

  select {
    height: 32px;
    padding: 0 8px;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-sm);
    background: var(--bg-primary);
    font-size: 12px;
    color: var(--text-primary);
    cursor: pointer;
    outline: none;

    &:focus {
      border-color: var(--primary);
    }
  }
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--border-color);
  margin: 0 4px;
}

.color-picker {
  width: 32px;
  height: 32px;
  padding: 2px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  background: var(--bg-primary);
}
</style>
