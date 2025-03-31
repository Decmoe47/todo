import { defineStore } from 'pinia'
import axiosInstance from '@/libs/axios.ts'
import type { AuthenticationTokens, LoginForm, RegisterForm, UserDTO } from '@/types/user.ts'
import { clearToken, getAccessToken, getRefreshToken, setAccessToken, setRefreshToken } from '@/utils/auth.ts'

interface UserState {
  user?: UserDTO
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({}),

  actions: {
    async login(form: LoginForm) {
      const user = await axiosInstance.post<UserDTO, UserDTO>('auth/login', form)
      this.user = user

      setAccessToken(user.tokens.accessToken)
      setRefreshToken(user.tokens.refreshToken)

      return user
    },

    async register(form: RegisterForm) {
      return await axiosInstance.post<UserDTO, UserDTO>('auth/register', form)
    },

    async refreshToken() {
      const refreshToken = getRefreshToken()
      const tokens = await axiosInstance.post<AuthenticationTokens, AuthenticationTokens>(
        'auth/refresh-token', { refreshToken: refreshToken })

      setAccessToken(tokens.accessToken)
      setRefreshToken(tokens.refreshToken)
    },

    async sendVerificationCode(email: string) {
      return await axiosInstance.post<void, void>('auth/send-verification-code', { email: email })
    },

    async logout() {
      await axiosInstance.post('auth/logout')
      this.clearSessionAndCache()
    },

    async getUser() {
      const userDTO = await axiosInstance.get<UserDTO, UserDTO>(
        'users/by-token', { params: { token: getAccessToken() } })
      this.user = userDTO
      return userDTO
    },

    clearSessionAndCache() {
      clearToken()
      this.user = undefined
    },

    setUser(user: UserDTO) {
      this.user = user
    },
  },

  getters: {
    userId: (state: UserState) => state.user!.id,
  },
})
