import axiosInstance from '@/libs/axios.ts'
import type { R } from '@/types/response'
import type { AuthenticationTokens, LoginForm, RegisterForm, User } from '@/types/user.ts'
import { clearToken, getAccessToken, getRefreshToken, setAccessToken, setRefreshToken } from '@/utils/auth.ts'
import { defineStore } from 'pinia'

interface UserState {
  user?: User
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({}),

  actions: {
    async login(form: LoginForm): Promise<User> {
      const res = await axiosInstance.post<R<User>, R<User>>('auth/login', form)
      this.user = res.data

      setAccessToken(this.user.tokens.accessToken)
      setRefreshToken(this.user.tokens.refreshToken)

      return this.user
    },

    async register(form: RegisterForm): Promise<User> {
      const res = await axiosInstance.post<R<User>, R<User>>('auth/register', form)
      return res.data
    },

    async refreshToken() {
      const refreshToken = getRefreshToken()
      const res = await axiosInstance.post<R<AuthenticationTokens>, R<AuthenticationTokens>>('auth/refresh-token', {
        refreshToken: refreshToken,
      })
      const tokens = res.data

      setAccessToken(tokens.accessToken)
      setRefreshToken(tokens.refreshToken)
    },

    async sendVerificationCode(email: string) {
      await axiosInstance.post<R<null>, R<null>>('auth/send-verify-code', { email: email }, { timeout: 20000 })
    },

    async logout() {
      await axiosInstance.post('auth/logout')
      this.clearSessionAndCache()
    },

    async getUser(): Promise<User> {
      const res = await axiosInstance.get<R<User>, R<User>>('users/by-token', {
        params: { token: getAccessToken() },
      })
      this.user = res.data
      return this.user
    },

    clearSessionAndCache() {
      clearToken()
      this.user = undefined
    },

    setUser(user: User) {
      this.user = user
    },
  },

  getters: {
    userId: (state: UserState) => state.user?.id,
  },
})
