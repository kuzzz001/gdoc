import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/DocumentList.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/editor/:id',
    name: 'Editor',
    component: () => import('@/views/Editor.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/social',
    name: 'Social',
    component: () => import('@/views/Social.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/share/:token',
    name: 'ShareView',
    component: () => import('@/views/ShareView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/components',
    name: 'Components',
    component: () => import('@/views/Components.vue'),
    meta: { requiresAuth: false },
  },
]

export default routes
