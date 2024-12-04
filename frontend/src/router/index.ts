import type { RouteRecordRaw } from 'vue-router'
import { createRouter, createWebHistory } from 'vue-router'
import axiosInstance from '@/libs/axios.ts'
import { useUserStore } from '@/stores/user.ts'
import type { UserDTO } from '@/types/user.ts'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/p/inbox',
    children: [
      {
        path: '/p/:listId',
        name: 'Todos',
        component: () => import('@/views/TodoListView.vue'),
        beforeEnter: (to) => {
          if (!to.params.listId) {
            return { path: '/p/inbox', replace: true }
          }
        },
      },
    ],
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    component: () => import('@/views/LoginView.vue'),
  },
  {
    path: '/register',
    component: () => import('@/views/RegisterView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

  let isAuthenticated = false
  try {
    const user = await axiosInstance.get<UserDTO, UserDTO>('/auth/check')
    isAuthenticated = true
    userStore.setUser(user)
  } catch {}

  if (requiresAuth && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
