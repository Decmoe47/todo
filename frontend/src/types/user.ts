export interface LoginForm {
  email: string
  password: string
}

export interface RegisterForm {
  name: string
  email: string
  password: string
  verifyCode: string
}

export interface UserDTO {
  id: number
  name: string
  email: string

  tokens: AuthenticationTokens
}

export interface AuthenticationTokens {
  accessToken: string
  refreshToken: string
}
