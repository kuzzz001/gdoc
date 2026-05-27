import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Document, PageResult } from '@/types'
import { documentApi } from '@/api/document'

export const useDocumentStore = defineStore('document', () => {
  const documents = ref<Document[]>([])
  const currentDoc = ref<Document | null>(null)
  const total = ref(0)
  const loading = ref(false)

  async function fetchDocuments(page = 1, size = 20) {
    loading.value = true
    try {
      const res: PageResult<Document> = await documentApi.list({ page, size })
      documents.value = res.records
      total.value = res.total
    } finally {
      loading.value = false
    }
  }

  async function createDocument(title = '未命名文档') {
    const doc = await documentApi.create(title)
    documents.value.unshift(doc)
    return doc
  }

  async function deleteDocument(id: number) {
    await documentApi.delete(id)
    documents.value = documents.value.filter((d) => d.id !== id)
  }

  async function fetchDocument(id: number) {
    currentDoc.value = await documentApi.get(id)
    return currentDoc.value
  }

  async function updateDocument(id: number, data: { title?: string; content?: string }) {
    const doc = await documentApi.update(id, data)
    currentDoc.value = doc
    const idx = documents.value.findIndex((d) => d.id === id)
    if (idx !== -1) {
      documents.value[idx] = doc
    }
    return doc
  }

  return { documents, currentDoc, total, loading, fetchDocuments, createDocument, deleteDocument, fetchDocument, updateDocument }
})
