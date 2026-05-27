/**
 * 待审核评价信息
 */
export interface PendingReviewDTO {
  id: number
  restaurantId: number
  userId: number
  rating: number
  content: string
  status: string
  createdAt: string
}

/**
 * 用户信息（管理员视角）
 */
export interface AdminUserDTO {
  id: number
  account: string
  nickname: string
  email: string
  status: string
  createdAt: string
}

/**
 * 用户状态枚举
 */
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  BANNED = 'BANNED',
}
