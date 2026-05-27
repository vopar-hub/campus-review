/**
 * API 统一响应结构
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  requestId: string | null
  timestamp: number
}

/**
 * 错误码枚举
 */
export enum ErrorCode {
  OK = 0,
  BAD_REQUEST = 40000,
  UNAUTHORIZED = 40100,
  FORBIDDEN = 40300,
  NOT_FOUND = 40400,
  TOO_MANY_REQUESTS = 42900,
  INTERNAL_ERROR = 50000,
}
