<template>
  <div class="tiptap-wrapper">
    <div v-if="editor" class="tiptap-toolbar">
      <div class="toolbar-group">
        <button
          :class="{ active: editor.isActive('heading', { level: 1 }) }"
          title="标题1"
          @click="editor.chain().focus().toggleHeading({ level: 1 }).run()"
        >H1</button>
        <button
          :class="{ active: editor.isActive('heading', { level: 2 }) }"
          title="标题2"
          @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
        >H2</button>
        <button
          :class="{ active: editor.isActive('heading', { level: 3 }) }"
          title="标题3"
          @click="editor.chain().focus().toggleHeading({ level: 3 }).run()"
        >H3</button>
        <button
          :class="{ active: editor.isActive('paragraph') }"
          title="正文"
          @click="editor.chain().focus().setParagraph().run()"
        >P</button>
      </div>
      <div class="toolbar-group">
        <button
          :class="{ active: editor.isActive('bold') }"
          title="粗体 Ctrl+B"
          @click="editor.chain().focus().toggleBold().run()"
        ><b>B</b></button>
        <button
          :class="{ active: editor.isActive('italic') }"
          title="斜体 Ctrl+I"
          @click="editor.chain().focus().toggleItalic().run()"
        ><i>I</i></button>
        <button
          :class="{ active: editor.isActive('underline') }"
          title="下划线 Ctrl+U"
          @click="editor.chain().focus().toggleUnderline().run()"
        ><u>U</u></button>
        <button
          :class="{ active: editor.isActive('strike') }"
          title="删除线"
          @click="editor.chain().focus().toggleStrike().run()"
        ><s>S</s></button>
        <button
          :class="{ active: editor.isActive('code') }"
          title="行内代码"
          @click="editor.chain().focus().toggleCode().run()"
        >&lt;/&gt;</button>
        <button
          :class="{ active: editor.isActive('highlight') }"
          title="高亮"
          @click="editor.chain().focus().toggleHighlight().run()"
        ><span class="hl-mark">A</span></button>
      </div>
      <div class="toolbar-group">
        <button
          :class="{ active: editor.isActive('bulletList') }"
          title="无序列表"
          @click="editor.chain().focus().toggleBulletList().run()"
        >•</button>
        <button
          :class="{ active: editor.isActive('orderedList') }"
          title="有序列表"
          @click="editor.chain().focus().toggleOrderedList().run()"
        >1.</button>
        <button
          :class="{ active: editor.isActive('blockquote') }"
          title="引用"
          @click="editor.chain().focus().toggleBlockquote().run()"
        >"</button>
        <button
          :class="{ active: editor.isActive('codeBlock') }"
          title="代码块"
          @click="editor.chain().focus().toggleCodeBlock().run()"
        >{ }</button>
        <button title="分割线" @click="editor.chain().focus().setHorizontalRule().run()">—</button>
        <button title="数学公式" @click="insertMath">fx</button>
      </div>
      <div class="toolbar-group">
        <button title="撤销 Ctrl+Z" @click="editor.chain().focus().undo().run()">↩</button>
        <button title="重做 Ctrl+Y" @click="editor.chain().focus().redo().run()">↪</button>
      </div>
    </div>
    <div class="tiptap-editor-container">
      <editor-content :editor="editor" class="tiptap-content" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, watch, shallowRef } from 'vue'
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import Underline from '@tiptap/extension-underline'
import Link from '@tiptap/extension-link'
import Image from '@tiptap/extension-image'
import { Table } from '@tiptap/extension-table'
import { TableRow } from '@tiptap/extension-table-row'
import { TableCell } from '@tiptap/extension-table-cell'
import { TableHeader } from '@tiptap/extension-table-header'
import CodeBlockLowlight from '@tiptap/extension-code-block-lowlight'
import Highlight from '@tiptap/extension-highlight'
import Typography from '@tiptap/extension-typography'
import Mathematics from '@tiptap/extension-mathematics'
import katex from 'katex'
import { common, createLowlight } from 'lowlight'

const lowlight = createLowlight(common)

const props = defineProps<{
  content?: string
}>()

const emit = defineEmits<{
  change: [html: string]
}>()

const editor = shallowRef<Editor>()

function initEditor(initialContent?: string) {
  if (editor.value) editor.value.destroy()

  editor.value = new Editor({
    extensions: [
      StarterKit.configure({
        codeBlock: false,
        heading: { levels: [1, 2, 3] },
      }),
      Placeholder.configure({ placeholder: '开始输入...' }),
      Typography,
      Underline,
      Link.configure({ openOnClick: false }),
      Image.configure({ allowBase64: true }),
      Table.configure({ resizable: true }),
      TableRow,
      TableCell,
      TableHeader,
      CodeBlockLowlight.configure({ lowlight }),
      Highlight.configure({ multicolor: true }),
      Mathematics.configure({
        katexOptions: {
          throwOnError: false,
          strict: false,
        },
      }),
    ],
    content: initialContent || '<p></p>',
    onUpdate: () => {
      emit('change', editor.value?.getHTML() || '')
    },
    editable: true,
  })
}

initEditor(props.content)

watch(() => props.content, (val) => {
  if (val && editor.value && val !== editor.value.getHTML()) {
    editor.value.commands.setContent(val || '<p></p>', { emitUpdate: false })
  }
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})

function getHTML() {
  return editor.value?.getHTML() || ''
}

function getJSON() {
  return editor.value?.getJSON()
}

function insertMath() {
  const latex = prompt('输入 LaTeX 公式:', 'E = mc^2')
  if (latex) {
    editor.value?.chain().focus().insertContent({
      type: 'mathematics',
      attrs: { latex },
    }).run()
  }
}

defineExpose({ getHTML, getJSON, insertMath })
</script>

<style scoped>
.tiptap-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tiptap-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px 12px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.toolbar-group {
  display: flex;
  gap: 2px;
  padding: 0 6px;
  border-right: 1px solid var(--border-color);
}

.toolbar-group:last-child {
  border-right: none;
}

.toolbar-group button {
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
}

.toolbar-group button:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.toolbar-group button.active {
  background: var(--primary-light);
  color: var(--primary);
}

.toolbar-group button b { font-size: 14px; }
.toolbar-group button i { font-size: 14px; }

.hl-mark {
  background: var(--warning);
  padding: 0 3px;
  border-radius: 2px;
  color: #000;
  font-size: 12px;
}

.tiptap-editor-container {
  flex: 1;
  overflow-y: auto;
}

.tiptap-content {
  padding: 24px 32px;
  min-height: 100%;
  outline: none;
}

.tiptap-content :deep(.ProseMirror) {
  outline: none;
  min-height: 400px;
}

.tiptap-content :deep(.ProseMirror p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  float: left;
  color: var(--text-placeholder);
  pointer-events: none;
  height: 0;
}

.tiptap-content :deep(h1) { font-size: 2em; margin: 0.67em 0; }
.tiptap-content :deep(h2) { font-size: 1.5em; margin: 0.75em 0; }
.tiptap-content :deep(h3) { font-size: 1.17em; margin: 0.83em 0; }
.tiptap-content :deep(p) { margin: 0.5em 0; }
.tiptap-content :deep(blockquote) {
  border-left: 3px solid var(--border-color);
  padding-left: 16px;
  color: var(--text-secondary);
  margin: 0.5em 0;
}
.tiptap-content :deep(ul), .tiptap-content :deep(ol) { padding-left: 1.5em; margin: 0.5em 0; }
.tiptap-content :deep(li) { margin: 0.2em 0; }
.tiptap-content :deep(code) {
  background: var(--bg-tertiary);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: var(--mono);
  font-size: 0.9em;
}
.tiptap-content :deep(pre) {
  background: var(--code-bg);
  color: var(--code-color);
  padding: 16px;
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: 0.75em 0;
  font-family: var(--mono);
  font-size: 0.9em;
  line-height: 1.6;
}
.tiptap-content :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
}
.tiptap-content :deep(hr) {
  border: none;
  border-top: 1px solid var(--border-color);
  margin: 1em 0;
}
.tiptap-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--radius-md);
  margin: 0.5em 0;
}
.tiptap-content :deep(a) {
  color: var(--primary);
  cursor: pointer;
}
.tiptap-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 0.75em 0;
}
.tiptap-content :deep(th), .tiptap-content :deep(td) {
  border: 1px solid var(--border-color);
  padding: 8px 12px;
  text-align: left;
}
.tiptap-content :deep(th) {
  background: var(--bg-secondary);
  font-weight: 600;
}
.tiptap-content :deep(mark) {
  background: #fef08a;
  padding: 0 2px;
  border-radius: 2px;
}
</style>