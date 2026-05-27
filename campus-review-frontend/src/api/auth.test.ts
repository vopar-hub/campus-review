import { describe, it, expect, vi, beforeEach } from 'vitest'
import { login, register } from './auth'
import { post } from './request'

// Mock 请求模块
vi.mock('./request', () => ({
  post: vi.fn(),
}))

describe('认证 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该成功登录', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: {
        userId: 1,
        roles: ['USER'],
        token: 'mock-jwt-token',
        expiresAt: '2024-01-02T00:00:00Z',
      },
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await login({ account: 'student@example.edu.cn', password: 'password123' })

    expect(post).toHaveBeenCalledWith('/api/auth/login', {
      account: 'student@example.edu.cn',
      password: 'password123',
    })
    expect(result.code).toBe(0)
    expect(result.data.token).toBe('mock-jwt-token')
    expect(result.data.userId).toBe(1)
  })

  it('应该成功注册', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: {
        id: 1,
        email: 'student@example.edu.cn',
        studentNo: '2024001',
        nickname: '小明',
        roles: ['USER'],
        banned: false,
        createdAt: '2024-01-01T00:00:00Z',
      },
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await register({
      email: 'student@example.edu.cn',
      studentNo: '2024001',
      password: 'password123',
      nickname: '小明',
    })

    expect(post).toHaveBeenCalledWith('/api/auth/register', {
      email: 'student@example.edu.cn',
      studentNo: '2024001',
      password: 'password123',
      nickname: '小明',
    })
    expect(result.code).toBe(0)
    expect(result.data.id).toBe(1)
    expect(result.data.email).toBe('student@example.edu.cn')
  })
})
