import { describe, it, expect, beforeEach, vi } from 'vitest'
import { getToken, setToken, removeToken, getUserId, setUserId } from './storage'
import Cookies from 'js-cookie'

// Mock js-cookie
vi.mock('js-cookie', () => ({
  default: {
    set: vi.fn(),
    get: vi.fn(),
    remove: vi.fn(),
  },
}))

describe('storage 工具', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('token 操作', () => {
    it('应该设置和获取 token', () => {
      ;(Cookies.get as ReturnType<typeof vi.fn>).mockReturnValue('test-token-123')

      setToken('test-token-123')
      expect(Cookies.set).toHaveBeenCalledWith('campus_review_token', 'test-token-123', { expires: 1, secure: false, sameSite: 'Lax' })

      expect(getToken()).toBe('test-token-123')
    })

    it('应该删除 token', () => {
      removeToken()
      expect(Cookies.remove).toHaveBeenCalledWith('campus_review_token')
      expect(Cookies.remove).toHaveBeenCalledWith('campus_review_user_id')
    })

    it('应该返回 undefined 当 token 不存在', () => {
      ;(Cookies.get as ReturnType<typeof vi.fn>).mockReturnValue(undefined)
      expect(getToken()).toBeUndefined()
    })
  })

  describe('userId 操作', () => {
    it('应该设置和获取 userId', () => {
      ;(Cookies.get as ReturnType<typeof vi.fn>).mockReturnValue('123')

      setUserId(123)
      expect(Cookies.set).toHaveBeenCalledWith('campus_review_user_id', '123', { expires: 7, secure: false, sameSite: 'Lax' })

      expect(getUserId()).toBe(123)
    })

    it('应该返回 undefined 当 userId 不存在', () => {
      ;(Cookies.get as ReturnType<typeof vi.fn>).mockReturnValue(undefined)
      expect(getUserId()).toBeUndefined()
    })
  })
})
