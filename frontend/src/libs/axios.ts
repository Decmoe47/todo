import { ResultEnums } from '@/constants/enums'
import { pinia } from '@/main.ts'
import router from '@/router'
import { useUserStore } from '@/stores/user.ts'
import type { R } from '@/types/response'
import { getAccessToken } from '@/utils/auth.ts'
import type { InternalAxiosRequestConfig } from 'axios'
import axios, { AxiosError } from 'axios'
import { ElMessage, ElNotification } from 'element-plus'

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
  timeout: 10000,
})

axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const accessToken = getAccessToken()
    if (config.headers.Authorization !== 'no-auth' && accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    } else {
      delete config.headers.Authorization
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

axiosInstance.interceptors.response.use(
  // 2xx
  async (response) => {
    if (response.data.code === ResultEnums.SUCCESS) {
      return response.data
    } else {
      ElMessage.error(response.data.message || 'Something went wrong, please try again later!')
      console.error(JSON.stringify(response.data))
      return Promise.reject(new Error(`${response.data.code}: ${response.data.message}`))
    }
  },
  // 4xx, 5xx
  async (error: AxiosError) => {
    let msg = 'Something went wrong, please try again later!'
    if (error.response) {
      const res = error.response.data as R<any>
      switch (error.response.status) {
        case 401:
          if (res.code === ResultEnums.ACCESS_TOKEN_EXPIRED) {
            await handleTokenRefresh()
          } else if (res.code === ResultEnums.REFRESH_TOKEN_EXPIRED) {
            await handleSessionExpired()
          } else if (res.code === ResultEnums.UNAUTHORIZED) {
            await router.push('/login')
          }
          break
        case 404:
          if (res.code === ResultEnums.USER_NOT_FOUND) {
            await router.push('/login')
          }
          break
      }
      msg = res.message || msg
      console.error(JSON.stringify(res))
    } else {
      console.error(error)
    }

    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

async function handleTokenRefresh() {
  try {
    await useUserStore(pinia).refreshToken()
  } catch (e) {
    console.error('handleTokenRefresh error', e)
    await handleSessionExpired()
  }
}

async function handleSessionExpired() {
  ElNotification({
    title: 'Session Expired',
    message: 'Your session has expired. Please log in again.',
    type: 'info',
  })
  useUserStore(pinia).clearSessionAndCache()
  await router.push('/login')
}

export default axiosInstance
