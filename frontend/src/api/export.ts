import { api } from './client'
import type { MediaFilters } from './media'

/** Downloads the filtered media set as an .xlsx file (respects the current filters). */
export async function exportMediaXlsx(filters: MediaFilters): Promise<void> {
  const params: Record<string, string | boolean> = {}
  const keys: (keyof MediaFilters)[] = [
    'q', 'mediaType', 'workflowStatus', 'contentUsageType', 'captureDeviceId', 'smart', 'deleted',
  ]
  for (const key of keys) {
    const value = filters[key]
    if (value !== undefined && value !== '' && value !== false) params[key] = value
  }

  const response = await api.get('/export/media.xlsx', { params, responseType: 'blob' })
  const url = URL.createObjectURL(response.data as Blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'media-export.xlsx'
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}
