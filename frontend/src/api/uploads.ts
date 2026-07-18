import { api } from './client'

export interface UploadResult {
  key: string
  url: string
}

export async function uploadThumbnail(file: File): Promise<UploadResult> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.post<UploadResult>('/uploads/thumbnail', form)
  return data
}
