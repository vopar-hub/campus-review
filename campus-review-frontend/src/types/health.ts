/**
 * 健康检查响应
 */
export interface HealthCheckResponse {
  status: string
  service: string
  timestamp: string
}

/**
 * 就绪检查响应
 */
export interface ReadyCheckResponse {
  status: string
  service: string
  checks: Record<string, string>
  timestamp: string
}
