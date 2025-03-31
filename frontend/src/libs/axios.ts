import { ResultEnums } from '@/enums/result.enums.ts'
import { pinia } from '@/main.ts'
import router from '@/router'
import { useUserStore } from '@/stores/user.ts'
import { getAccessToken } from '@/utils/auth.ts'
import type { InternalAxiosRequestConfig } from 'axios'
import axios, { AxiosError } from 'axios'
import { ElMessage, ElNotification } from 'element-plus'

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
  timeout: 5000,
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
  async (response) => {
    if (response.data.code === 0) {
      return response.data.data
    } else if (response.data.code === ResultEnums.ACCESS_TOKEN_EXPIRED) {
      // token 过期，尝试刷新
      await handleTokenRefresh()
    } else if (response.data.code === ResultEnums.REFRESH_TOKEN_EXPIRED) {
      // refresh token 过期，需要重新登录
      await handleSessionExpired()
    }

    ElMessage.error(response.data.message)
    console.error(JSON.stringify(response.data))
    return Promise.reject(new Error(`${response.data.code}: ${response.data.message}`))
  },
  async (error: AxiosError) => {
    let msg = error.message
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 400:
          msg = 'Invalid request body'
          break
        case 401:
          msg = 'Unauthorized, please login'
          break
        case 403:
          msg = 'Forbidden'
          break
        case 404:
          msg = 'Not Found'
          break
        case 500:
          msg = 'Internal Server Error'
          break
        default:
          msg = `Connection error ${status}`
      }
    } else if (error.code === 'ECONNABORTED' && error.message.indexOf('timeout') !== -1) {
      msg = 'Timeout'
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
    title: '提示',
    message: '您的会话已过期，请重新登录',
    type: 'info',
  })
  useUserStore(pinia).clearSessionAndCache()
  await router.push('/login')
}

export default axiosInstance
