import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getHotRestaurants } from './ranking'
import { get } from './request'

// Mock 请求模块
vi.mock('./request', () => ({
  get: vi.fn(),
}))

describe('排行榜 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该获取热门餐馆排行榜（默认前 10）', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: [
        {
          rank: 1,
          restaurantId: 1,
          restaurantName: '第一食堂',
          score: 98.5,
        },
        {
          rank: 2,
          restaurantId: 2,
          restaurantName: '第二食堂',
          score: 95.0,
        },
      ],
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await getHotRestaurants()

    expect(get).toHaveBeenCalledWith('/api/rankings/hot-restaurants', {
      params: { topN: 10 },
    })
    expect(result.code).toBe(0)
    expect(result.data.length).toBe(2)
    expect(result.data[0].rank).toBe(1)
    expect(result.data[0].score).toBe(98.5)
  })

  it('应该获取指定数量的排行榜', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: [
        {
          rank: 1,
          restaurantId: 1,
          restaurantName: '第一食堂',
          score: 98.5,
        },
      ],
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await getHotRestaurants(1)

    expect(get).toHaveBeenCalledWith('/api/rankings/hot-restaurants', {
      params: { topN: 1 },
    })
    expect(result.code).toBe(0)
  })
})
