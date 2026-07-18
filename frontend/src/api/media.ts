import { api } from './client'
import type { MediaDetail, MediaRequestPayload, MediaSummary, Page } from './types'

export interface MediaFilters {
  q?: string
  mediaType?: string
  workflowStatus?: string
  contentUsageType?: string
  captureDeviceId?: string
  smart?: string
  deleted?: boolean
}

export interface MediaListParams extends MediaFilters {
  page?: number
  size?: number
  sort?: string
}

export async function listMedia(params: MediaListParams): Promise<Page<MediaSummary>> {
  const query: Record<string, string | number | boolean> = {
    page: params.page ?? 0,
    size: params.size ?? 20,
    sort: params.sort ?? 'createdAt,desc',
  }
  // Only send filter params that have a value.
  const filterKeys: (keyof MediaFilters)[] = [
    'q', 'mediaType', 'workflowStatus', 'contentUsageType', 'captureDeviceId', 'smart', 'deleted',
  ]
  for (const key of filterKeys) {
    const value = params[key]
    if (value !== undefined && value !== '' && value !== false) query[key] = value
  }
  const { data } = await api.get<Page<MediaSummary>>('/media', { params: query })
  return data
}

export async function getMedia(id: string): Promise<MediaDetail> {
  const { data } = await api.get<MediaDetail>(`/media/${id}`)
  return data
}

export async function createMedia(payload: MediaRequestPayload): Promise<MediaDetail> {
  const { data } = await api.post<MediaDetail>('/media', payload)
  return data
}

export async function updateMedia(id: string, payload: MediaRequestPayload): Promise<MediaDetail> {
  const { data } = await api.put<MediaDetail>(`/media/${id}`, payload)
  return data
}

export async function deleteMedia(id: string): Promise<void> {
  await api.delete(`/media/${id}`)
}

export async function restoreMedia(id: string): Promise<void> {
  await api.post(`/media/${id}/restore`)
}
