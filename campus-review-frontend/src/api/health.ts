import axios from 'axios'
import type { HealthCheckResponse, ReadyCheckResponse } from '@/types'

/**
 * 健康检查
 */
export async function healthCheck(): Promise<HealthCheckResponse> {
  const response = await axios.get(
    import.meta.env.VITE_API_BASE_URL + '/api/health/'
  )
  return response.data
}

/**
 * 就绪检查
 */
export async function readyCheck(): Promise<ReadyCheckResponse> {
  const response = await axios.get(
    import.meta.env.VITE_API_BASE_URL + '/api/health/ready'
  )
  return response.data
}
