import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createRestaurant, getRestaurant, searchRestaurants } from './restaurant'
import { get, post } from './request'

// Mock 请求模块
vi.mock('./request', () => ({
  get: vi.fn(),
  post: vi.fn(),
}))

describe('餐馆 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该创建餐馆', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: {
        id: 1,
        name: '第一食堂',
        campus: '南湖校区',
        address: '校园北区 1 号楼',
        description: '提供各式家常菜',
        coverImageUrl: 'https://example.com/image.jpg',
        createdAt: '2024-01-01T00:00:00Z',
      },
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await createRestaurant({
      name: '第一食堂',
      campus: '南湖校区',
      address: '校园北区 1 号楼',
      description: '提供各式家常菜',
      coverImageUrl: 'https://example.com/image.jpg',
    })

    expect(post).toHaveBeenCalledWith('/api/restaurants', {
      name: '第一食堂',
      campus: '南湖校区',
      address: '校园北区 1 号楼',
      description: '提供各式家常菜',
      coverImageUrl: 'https://example.com/image.jpg',
    })
    expect(result.code).toBe(0)
    expect(result.data.id).toBe(1)
    expect(result.data.name).toBe('第一食堂')
  })

  it('应该获取餐馆详情', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: {
        id: 1,
        name: '第一食堂',
        campus: '南湖校区',
        address: '校园北区 1 号楼',
        description: '提供各式家常菜',
        coverImageUrl: 'https://example.com/image.jpg',
        createdAt: '2024-01-01T00:00:00Z',
      },
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await getRestaurant(1)

    expect(get).toHaveBeenCalledWith('/api/restaurants/1')
    expect(result.code).toBe(0)
    expect(result.data.id).toBe(1)
  })

  it('应该搜索餐馆', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: [
        {
          id: 1,
          name: '第一食堂',
          campus: '南湖校区',
          address: '校园北区 1 号楼',
          description: '提供各式家常菜',
          coverImageUrl: 'https://example.com/image.jpg',
          createdAt: '2024-01-01T00:00:00Z',
        },
      ],
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await searchRestaurants({ name: '食堂', campus: '南湖校区' })

    expect(get).toHaveBeenCalledWith('/api/restaurants', {
      params: { name: '食堂', campus: '南湖校区' },
    })
    expect(result.code).toBe(0)
    expect(Array.isArray(result.data)).toBe(true)
    expect(result.data.length).toBe(1)
  })
})
