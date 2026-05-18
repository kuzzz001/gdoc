<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="8" y="6" width="32" height="36" rx="4" fill="#4285f4" opacity="0.1" />
            <rect x="8" y="6" width="32" height="36" rx="4" stroke="#4285f4" stroke-width="2" />
            <path d="M16 18h16M16 24h16M16 30h10" stroke="#4285f4" stroke-width="2" stroke-linecap="round" />
          </svg>
        </div>
        <h1>吉智文档</h1>
        <p>多人实时协同编辑系统</p>
      </div>

      <div class="login-tabs">
        <button
          :class="{ active: isLogin }"
          @click="isLogin = true"
        >
          登录
        </button>
        <button
          :class="{ active: !isLogin }"
          @click="isLogin = false"
        >
          注册
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="login-form">
        <div v-if="!isLogin" class="form-group">
          <label>昵称</label>
          <input v-model="form.nickname" type="text" placeholder="请输入昵称" required />
        </div>
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" placeholder="请输入用户名" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" required />
        </div>

        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '处理中...' : isLogin ? '登录' : '注册' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: '',
  nickname: '',
})

async function handleSubmit() {
  loading.value = true
  errorMsg.value = ''
  try {
    if (isLogin.value) {
      await userStore.login(form.username, form.password)
    } else {
      await userStore.register(form.username, form.password, form.nickname)
    }
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    errorMsg.value = e.message || '操作失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  padding: 40px 32px;
}

.login-header {
  text-align: center;
  margin-bottom: 28px;

  .logo-icon {
    width: 56px;
    height: 56px;
    margin: 0 auto 12px;

    svg {
      width: 100%;
      height: 100%;
    }
  }

  h1 {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  p {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.login-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  padding: 4px;

  button {
    flex: 1;
    padding: 8px;
    border: none;
    border-radius: var(--radius-sm);
    background: transparent;
    font-size: 14px;
    font-weight: 500;
    color: var(--text-secondary);
    cursor: pointer;
    transition: var(--transition);

    &.active {
      background: var(--bg-primary);
      color: var(--primary);
      box-shadow: var(--shadow-sm);
    }
  }
}

.login-form {
  .form-group {
    margin-bottom: 16px;

    label {
      display: block;
      font-size: 13px;
      font-weight: 500;
      color: var(--text-secondary);
      margin-bottom: 6px;
    }

    input {
      width: 100%;
      padding: 10px 14px;
      border: 1.5px solid var(--border-color);
      border-radius: var(--radius-md);
      font-size: 14px;
      outline: none;
      transition: var(--transition);

      &:focus {
        border-color: var(--primary);
        box-shadow: 0 0 0 4px rgba(26, 115, 232, 0.08);
      }
    }
  }
}

.error-msg {
  padding: 10px 14px;
  background: var(--danger-light);
  color: var(--danger);
  border-radius: var(--radius-sm);
  font-size: 13px;
  margin-bottom: 16px;
}

.btn-submit {
  width: 100%;
  padding: 12px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);

  &:hover:not(:disabled) {
    background: var(--primary-hover);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}
</style>
