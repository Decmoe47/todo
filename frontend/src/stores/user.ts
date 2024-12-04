import { defineStore } from 'pinia'
import axiosInstance from '@/libs/axios.ts'
import type { LoginForm, RegisterForm, UserDTO } from '@/types/user.ts'

interface UserState {
  user?: UserDTO
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({}),

  actions: {
    async login(form: LoginForm) {
      const user = await axiosInstance.post<UserDTO, UserDTO>('auth/login', form)
      this.user = user
      return user
    },
    async register(form: RegisterForm) {
      return await axiosInstance.post<UserDTO, UserDTO>('auth/register', form)
    },
    async sendVerificationCode(email: string) {
      return await axiosInstance.post<void, void>('auth/sendVerificationCode', { email: email })
    },
    setUser(user: UserDTO) {
      this.user = user
    },
  },

  getters: {
    userId: (state: UserState) => state.user!.id,
  },
})
