import { api } from './client'
import type {
  CaptureDevice, CaptureDevicePayload, Lens, LensPayload, RejectionCategory, RejectionCategoryPayload,
  SiteCategory, SiteCategoryPayload, StockSite, StockSitePayload,
} from './types'

export async function listLenses(includeInactive = false): Promise<Lens[]> {
  const { data } = await api.get<Lens[]>('/lenses', { params: { includeInactive } })
  return data
}

export async function listSiteCategories(siteId: string, includeInactive = false): Promise<SiteCategory[]> {
  const { data } = await api.get<SiteCategory[]>(`/stock-sites/${siteId}/categories`, { params: { includeInactive } })
  return data
}

export async function listStockSites(includeInactive = false): Promise<StockSite[]> {
  const { data } = await api.get<StockSite[]>('/stock-sites', { params: { includeInactive } })
  return data
}

export async function listCaptureDevices(includeInactive = false): Promise<CaptureDevice[]> {
  const { data } = await api.get<CaptureDevice[]>('/capture-devices', { params: { includeInactive } })
  return data
}

export async function listRejectionCategories(includeInactive = false): Promise<RejectionCategory[]> {
  const { data } = await api.get<RejectionCategory[]>('/rejection-categories', { params: { includeInactive } })
  return data
}

// --- Capture devices (admin writes) ---
export const createCaptureDevice = (p: CaptureDevicePayload) =>
  api.post<CaptureDevice>('/capture-devices', p).then((r) => r.data)
export const updateCaptureDevice = (id: string, p: CaptureDevicePayload) =>
  api.put<CaptureDevice>(`/capture-devices/${id}`, p).then((r) => r.data)
export const setCaptureDeviceActive = (id: string, active: boolean) =>
  api.patch<CaptureDevice>(`/capture-devices/${id}/active`, { active }).then((r) => r.data)

// --- Lenses ---
export const createLens = (p: LensPayload) => api.post<Lens>('/lenses', p).then((r) => r.data)
export const updateLens = (id: string, p: LensPayload) => api.put<Lens>(`/lenses/${id}`, p).then((r) => r.data)
export const setLensActive = (id: string, active: boolean) =>
  api.patch<Lens>(`/lenses/${id}/active`, { active }).then((r) => r.data)

// --- Stock sites ---
export const createStockSite = (p: StockSitePayload) => api.post<StockSite>('/stock-sites', p).then((r) => r.data)
export const updateStockSite = (id: string, p: StockSitePayload) =>
  api.put<StockSite>(`/stock-sites/${id}`, p).then((r) => r.data)
export const setStockSiteActive = (id: string, active: boolean) =>
  api.patch<StockSite>(`/stock-sites/${id}/active`, { active }).then((r) => r.data)

// --- Site categories ---
export const createSiteCategory = (siteId: string, p: SiteCategoryPayload) =>
  api.post<SiteCategory>(`/stock-sites/${siteId}/categories`, p).then((r) => r.data)
export const updateSiteCategory = (categoryId: string, p: SiteCategoryPayload) =>
  api.put<SiteCategory>(`/stock-sites/categories/${categoryId}`, p).then((r) => r.data)
export const setSiteCategoryActive = (categoryId: string, active: boolean) =>
  api.patch<SiteCategory>(`/stock-sites/categories/${categoryId}/active`, { active }).then((r) => r.data)

// --- Rejection categories ---
export const createRejectionCategory = (p: RejectionCategoryPayload) =>
  api.post<RejectionCategory>('/rejection-categories', p).then((r) => r.data)
export const updateRejectionCategory = (id: string, p: RejectionCategoryPayload) =>
  api.put<RejectionCategory>(`/rejection-categories/${id}`, p).then((r) => r.data)
export const setRejectionCategoryActive = (id: string, active: boolean) =>
  api.patch<RejectionCategory>(`/rejection-categories/${id}/active`, { active }).then((r) => r.data)
