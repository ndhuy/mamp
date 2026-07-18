// Public base URL for objects in the media bucket (MinIO in dev).
// Override with VITE_MEDIA_BASE_URL for other environments.
export const MEDIA_BASE_URL =
  (import.meta.env.VITE_MEDIA_BASE_URL as string | undefined) ?? 'http://localhost:9000/msamp-media'

export function thumbnailUrl(key?: string | null): string | undefined {
  if (!key) return undefined
  if (key.startsWith('http://') || key.startsWith('https://')) return key
  return `${MEDIA_BASE_URL}/${key}`
}
