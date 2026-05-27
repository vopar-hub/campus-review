import { post } from './request'

/**
 * 上传文件
 */
export function uploadFile(file: File, dir?: string) {
  const formData = new FormData()
  formData.append('file', file)
  if (dir) {
    formData.append('dir', dir)
  }

  return post<{
    url: string
    filename: string
    contentType: string
    size: number
  }>('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
