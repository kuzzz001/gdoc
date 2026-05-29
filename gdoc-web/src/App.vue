<template>
  <router-view />
  <Toast />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import Toast from '@/components/common/Toast.vue'

const userStore = useUserStore()

onMounted(() => {
  userStore.init()
  if (userStore.isLoggedIn) {
    userStore.fetchUser()
  }
})
</script>