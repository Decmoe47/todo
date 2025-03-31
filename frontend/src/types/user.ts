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

  tokens: AuthenticationTokens
}

export interface AuthenticationTokens {
  accessToken: string
  refreshToken: string
}
