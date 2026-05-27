import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getCurrentUser, updateProfile } from './user'
import { get, put } from './request'

// Mock 请求模块
vi.mock('./request', () => ({
  get: vi.fn(),
  put: vi.fn(),
}))

describe('用户 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该获取当前用户信息', async () => {
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
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await getCurrentUser()

    expect(get).toHaveBeenCalledWith('/api/users/me')
    expect(result.code).toBe(0)
    expect(result.data.id).toBe(1)
    expect(result.data.nickname).toBe('小明')
    expect(result.data.roles).toEqual(['USER'])
  })

  it('应该更新用户资料', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: {
        id: 1,
        email: 'student@example.edu.cn',
        studentNo: '2024001',
        nickname: '新昵称',
        roles: ['USER'],
        banned: false,
        createdAt: '2024-01-01T00:00:00Z',
      },
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(put).mockResolvedValue(mockResponse)

    const result = await updateProfile({ nickname: '新昵称' })

    expect(put).toHaveBeenCalledWith('/api/users/me', { nickname: '新昵称' })
    expect(result.code).toBe(0)
    expect(result.data.nickname).toBe('新昵称')
  })
})
