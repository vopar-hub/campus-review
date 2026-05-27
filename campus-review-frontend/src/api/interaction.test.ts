import { describe, it, expect, vi, beforeEach } from 'vitest'
import { like, unlike, favorite, unfavorite, getInteractionCount } from './interaction'
import { get, post } from './request'

// Mock 请求模块
vi.mock('./request', () => ({
  get: vi.fn(),
  post: vi.fn(),
}))

describe('互动 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该点赞', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: null,
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await like({ targetType: 'REVIEW', targetId: 1 })

    expect(post).toHaveBeenCalledWith('/api/interactions/like', {
      targetType: 'REVIEW',
      targetId: 1,
    })
    expect(result.code).toBe(0)
  })

  it('应该取消点赞', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: null,
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await unlike({ targetType: 'REVIEW', targetId: 1 })

    expect(post).toHaveBeenCalledWith('/api/interactions/unlike', {
      targetType: 'REVIEW',
      targetId: 1,
    })
    expect(result.code).toBe(0)
  })

  it('应该收藏', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: null,
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await favorite({ targetType: 'RESTAURANT', targetId: 1 })

    expect(post).toHaveBeenCalledWith('/api/interactions/favorite', {
      targetType: 'RESTAURANT',
      targetId: 1,
    })
    expect(result.code).toBe(0)
  })

  it('应该取消收藏', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: null,
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await unfavorite({ targetType: 'RESTAURANT', targetId: 1 })

    expect(post).toHaveBeenCalledWith('/api/interactions/unfavorite', {
      targetType: 'RESTAURANT',
      targetId: 1,
    })
    expect(result.code).toBe(0)
  })

  it('应该获取互动统计', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: {
        targetType: 'REVIEW',
        targetId: 1,
        likeCount: 10,
        favoriteCount: 5,
      },
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await getInteractionCount('REVIEW', 1)

    expect(get).toHaveBeenCalledWith('/api/interactions/count', {
      params: { targetType: 'REVIEW', targetId: 1 },
    })
    expect(result.code).toBe(0)
    expect(result.data.likeCount).toBe(10)
    expect(result.data.favoriteCount).toBe(5)
  })
})
