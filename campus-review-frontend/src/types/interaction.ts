/**
 * 互动目标类型
 */
export type TargetType = 'RESTAURANT' | 'REVIEW'

/**
 * 互动请求
 */
export interface InteractRequest {
  targetType: TargetType
  targetId: number
}

/**
 * 互动统计
 */
export interface InteractionCountDTO {
  targetType: TargetType
  targetId: number
  likeCount: number
  favoriteCount: number
}
