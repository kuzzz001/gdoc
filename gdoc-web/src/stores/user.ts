import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { authApi, userApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string>('')

  const isLoggedIn = computed(() => !!user.value)
  const userId = computed(() => user.value?.id || 0)
  const username = computed(() => user.value?.username || '')
  const nickname = computed(() => user.value?.nickname || '')
  const avatar = computed(() => user.value?.avatarUrl || '')

  function persistUser() {
    if (user.value) {
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    user.value = res.user
    persistUser()
  }

  async function register(username: string, password: string, nickname: string) {
    const res = await authApi.register({ username, password, nickname })
    user.value = res.user
    persistUser()
  }

  async function fetchUser() {
    try {
      user.value = await userApi.getMe()
      persistUser()
    } catch {
      user.value = null
      localStorage.removeItem('user')
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // ignore
    }
    user.value = null
    token.value = ''
    localStorage.removeItem('user')
  }

  function init() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch {
        localStorage.removeItem('user')
      }
    }
    fetchUser()
  }

  return { user, token, isLoggedIn, userId, username, nickname, avatar, login, register, fetchUser, logout, init }
})