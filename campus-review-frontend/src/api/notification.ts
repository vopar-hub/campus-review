import { get, post } from './request'
import type { MessageDTO, SendMessageRequest } from '@/types'

/**
 * 发送消息
 */
export function sendMessage(data: SendMessageRequest) {
  return post<MessageDTO>('/api/notifications/send', data)
}

/**
 * 获取收件箱消息列表
 */
export function getInbox() {
  return get<MessageDTO[]>('/api/notifications/inbox')
}

/**
 * 标记消息为已读
 */
export function markAsRead(id: number) {
  return post<void>(`/api/notifications/${id}/read`)
}
