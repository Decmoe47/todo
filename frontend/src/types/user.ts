export interface LoginForm {
  email: string
  password: string
}

export interface RegisterForm {
  email: string
  password: string
  verificationCode: string
}

export interface UserDTO {
  id: number
  name: string
  email: string
  isLocked: boolean
  accountExpireTime: string
  credentialExpireTime: string
  lastLoginTime: string
  registerTime: string
}
