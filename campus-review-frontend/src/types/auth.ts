/**
 * 注册请求
 */
export interface RegisterRequest {
  email: string
  studentNo: string
  password: string
  nickname: string
}

/**
 * 登录请求
 */
export interface LoginRequest {
  account: string
  password: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  userId: number
  roles: string[]
  token: string
  refreshToken: string
  expiresAt: string
  refreshExpiresAt: string
}
