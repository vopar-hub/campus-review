/**
 * 站内消息
 */
export interface MessageDTO {
  id: number
  toUserId: number
  title: string
  content: string
  read: boolean
  createdAt: string
}

/**
 * 发送消息请求
 */
export interface SendMessageRequest {
  toUserId: number
  title: string
  content: string
}
