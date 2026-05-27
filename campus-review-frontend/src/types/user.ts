/**
 * 用户信息
 */
export interface UserDTO {
  id: number
  email: string
  studentNo: string
  nickname: string
  roles: string[]
  banned: boolean
  createdAt: string
}

/**
 * 用户资料更新请求
 */
export interface UpdateProfileRequest {
  nickname?: string
  avatarUrl?: string
}
