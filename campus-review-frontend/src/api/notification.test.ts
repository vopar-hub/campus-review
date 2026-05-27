import { describe, it, expect, vi, beforeEach } from 'vitest'
import { sendMessage, getInbox, markAsRead } from './notification'
import { get, post } from './request'

// Mock 请求模块
vi.mock('./request', () => ({
  get: vi.fn(),
  post: vi.fn(),
}))

describe('通知 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该发送消息', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: {
        id: 1,
        toUserId: 1,
        title: '审核通知',
        content: '您的评价已通过审核',
        read: false,
        createdAt: '2024-01-01T00:00:00Z',
      },
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await sendMessage({
      toUserId: 1,
      title: '审核通知',
      content: '您的评价已通过审核',
    })

    expect(post).toHaveBeenCalledWith('/api/notifications/send', {
      toUserId: 1,
      title: '审核通知',
      content: '您的评价已通过审核',
    })
    expect(result.code).toBe(0)
    expect(result.data.id).toBe(1)
  })

  it('应该获取收件箱消息列表', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: [
        {
          id: 1,
          toUserId: 1,
          title: '审核通知',
          content: '您的评价已通过审核',
          read: false,
          createdAt: '2024-01-01T00:00:00Z',
        },
        {
          id: 2,
          toUserId: 1,
          title: '点赞通知',
          content: '您的评价被点赞了',
          read: true,
          createdAt: '2024-01-02T00:00:00Z',
        },
      ],
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(get).mockResolvedValue(mockResponse)

    const result = await getInbox()

    expect(get).toHaveBeenCalledWith('/api/notifications/inbox')
    expect(result.code).toBe(0)
    expect(result.data.length).toBe(2)
    expect(result.data[0].read).toBe(false)
  })

  it('应该标记消息已读', async () => {
    const mockResponse = {
      code: 0,
      message: 'success',
      data: null,
      requestId: 'test-req-id',
      timestamp: Date.now(),
    }
    vi.mocked(post).mockResolvedValue(mockResponse)

    const result = await markAsRead(1)

    expect(post).toHaveBeenCalledWith('/api/notifications/1/read')
    expect(result.code).toBe(0)
  })
})
