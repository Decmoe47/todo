import { useTodoStore } from '@/stores/todo'
import { useUserStore } from '@/stores/user.ts'
import { getAccessToken } from '@/utils/auth.ts'
import type { RouteRecordRaw } from 'vue-router'
import { createRouter, createWebHistory } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/p/inbox',
    children: [
      {
        path: '/p/:listId',
        name: 'Todos',
        component: () => import('@/views/TodoView.vue'),
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
const authWhiteList = ['/login', '/register', '/logout']

router.beforeEach(async (to, from, next) => {
  const isLogin = !!getAccessToken()
  const userStore = useUserStore()
  const todoStore = useTodoStore()

  if (isLogin && !userStore.user) {
    await userStore.getUser()
  }
  if (isLogin && to.meta.requiresAuth && todoStore.todoLists.length === 0) {
    await todoStore.loadAllTodoLists()
  }

  if (isLogin) {
    // 已登录用户访问登录页时，重定向到首页
    if (to.path === '/login') {
      next('/')
    } else {
      next()
    }
  } else {
    // 未登录用户访问非白名单路径时，跳转到登录页
    if (authWhiteList.includes(to.path)) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
