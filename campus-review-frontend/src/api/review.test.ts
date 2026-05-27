import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createReview, getReviewsByRestaurant, getMyReviews } from './review'
import { get, post } from './request'

// Mock 请求模块
vi.mock('./request', () => ({
  get: vi.fn(),
  post: vi.fn(),
}))

describe('评价 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该发布评价', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: {
        id: 1,
        restaurantId: 1,
        userId: 1,
        rating: 5,
        content: '非常好吃，服务也很好！',
        status: 'PENDING',
        createdAt: '2024-01-01T00:00:00Z',
      },
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await createReview({
      restaurantId: 1,
      rating: 5,
      content: '非常好吃，服务也很好！',
    })

    expect(post).toHaveBeenCalledWith('/api/reviews', {
      restaurantId: 1,
      rating: 5,
      content: '非常好吃，服务也很好！',
    })
    expect(result.code).toBe(0)
    expect(result.data.id).toBe(1)
    expect(result.data.status).toBe('PENDING')
  })

  it('应该获取餐馆评价列表', async () => {
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
          status: 'APPROVED',
          createdAt: '2024-01-01T00:00:00Z',
        },
      ],
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await getReviewsByRestaurant(1)

    expect(get).toHaveBeenCalledWith('/api/reviews', {
      params: { restaurantId: 1 },
    })
    expect(result.code).toBe(0)
    expect(Array.isArray(result.data)).toBe(true)
    expect(result.data[0].status).toBe('APPROVED')
  })

  it('应该获取我的评价', async () => {
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
          status: 'APPROVED',
          createdAt: '2024-01-01T00:00:00Z',
        },
      ],
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await getMyReviews()

    expect(get).toHaveBeenCalledWith('/api/reviews/me')
    expect(result.code).toBe(0)
    expect(result.data[0].id).toBe(1)
  })
})
