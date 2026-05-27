import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getPendingReviews,
  approveReview,
  rejectReview,
  banUser,
  unbanUser,
} from './admin'
import { get, post } from './admin-request'

// Mock 管理员请求模块
vi.mock('./admin-request', () => ({
  get: vi.fn(),
  post: vi.fn(),
}))

describe('管理员 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该获取待审核评价列表', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: [
        {
          id: 1,
          restaurantId: 1,
          userId: 1,
          rating: 5,
          content: '非常好吃，服务也很好！',
          status: 'PENDING',
          createdAt: '2024-01-01T00:00:00Z',
        },
        {
          id: 2,
          restaurantId: 2,
          userId: 2,
          rating: 4,
          content: '味道不错，环境一般',
          status: 'PENDING',
          createdAt: '2024-01-02T00:00:00Z',
        },
      ],
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await getPendingReviews()

    expect(get).toHaveBeenCalledWith('/reviews/pending')
    expect(result.code).toBe(0)
    expect(Array.isArray(result.data)).toBe(true)
    expect(result.data.length).toBe(2)
    expect(result.data[0].id).toBe(1)
    expect(result.data[0].status).toBe('PENDING')
    expect(result.data[1].id).toBe(2)
  })

  it('应该通过评价', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: null,
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await approveReview(1)

    expect(post).toHaveBeenCalledWith('/reviews/1/approve')
    expect(result.code).toBe(0)
    expect(result.data).toBeNull()
  })

  it('应该驳回评价', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: null,
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await rejectReview(1)

    expect(post).toHaveBeenCalledWith('/reviews/1/reject')
    expect(result.code).toBe(0)
    expect(result.data).toBeNull()
  })

  it('应该封禁用户', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: null,
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await banUser(123)

    expect(post).toHaveBeenCalledWith('/users/123/ban')
    expect(result.code).toBe(0)
    expect(result.data).toBeNull()
  })

  it('应该解封用户', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: null,
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await unbanUser(123)

    expect(post).toHaveBeenCalledWith('/users/123/unban')
    expect(result.code).toBe(0)
    expect(result.data).toBeNull()
  })
})
