import Cookies from 'js-cookie'

const TOKEN_KEY = 'campus_review_token'
const REFRESH_TOKEN_KEY = 'campus_review_refresh_token'
const USER_ID_KEY = 'campus_review_user_id'

/**
 * 存储 Token
 * 注意：HttpOnly Cookie 需要后端设置，前端无法设置
 */
export function setToken(token: string): void {
  Cookies.set(TOKEN_KEY, token, {
    expires: 1,
    secure: false,
    sameSite: 'Lax'
  })
}

/**
 * 获取 Token
 */
export function getToken(): string | undefined {
  return Cookies.get(TOKEN_KEY)
}

/**
 * 存储 Refresh Token
 */
export function setRefreshToken(refreshToken: string): void {
  Cookies.set(REFRESH_TOKEN_KEY, refreshToken, {
    expires: 7,
    secure: false,
    sameSite: 'Lax'
  })
}

/**
 * 获取 Refresh Token
 */
export function getRefreshToken(): string | undefined {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

/**
 * 移除 Token
 */
export function removeToken(): void {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(REFRESH_TOKEN_KEY)
  Cookies.remove(USER_ID_KEY)
}

/**
 * 存储用户 ID
 */
export function setUserId(userId: number): void {
  Cookies.set(USER_ID_KEY, String(userId), {
    expires: 7,
    secure: false,
    sameSite: 'Lax'
  })
}

/**
 * 获取用户 ID
 */
export function getUserId(): number | undefined {
  const userId = Cookies.get(USER_ID_KEY)
  if (!userId) return undefined
  const num = Number(userId)
  return isNaN(num) ? undefined : num
}

/**
 * 检查是否已登录
 */
export function isLoggedIn(): boolean {
  return !!getToken()
}
