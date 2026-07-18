import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import type { TokenResponse, User } from './types'

const ACCESS_KEY = 'msamp.accessToken'
const REFRESH_KEY = 'msamp.refreshToken'
const USER_KEY = 'msamp.user'

export const tokenStore = {
  get access() {
    return localStorage.getItem(ACCESS_KEY)
  },
  get refresh() {
    return localStorage.getItem(REFRESH_KEY)
  },
  get user(): User | null {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? (JSON.parse(raw) as User) : null
  },
  set(tokens: TokenResponse) {
    localStorage.setItem(ACCESS_KEY, tokens.accessToken)
    localStorage.setItem(REFRESH_KEY, tokens.refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(tokens.user))
  },
  clear() {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
    localStorage.removeItem(USER_KEY)
  },
}

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStore.access
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Callback the AuthProvider registers so a failed refresh can reset app state.
let onAuthFailure: (() => void) | null = null
export function setOnAuthFailure(cb: () => void) {
  onAuthFailure = cb
}

let refreshing: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = tokenStore.refresh
  if (!refreshToken) return null
  try {
    const { data } = await axios.post<TokenResponse>('/api/auth/refresh', { refreshToken })
    tokenStore.set(data)
    return data.accessToken
  } catch {
    return null
  }
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean }
    const url = original?.url ?? ''
    const isAuthCall = url.includes('/auth/login') || url.includes('/auth/refresh')

    if (error.response?.status === 401 && original && !original._retry && !isAuthCall) {
      original._retry = true
      refreshing = refreshing ?? refreshAccessToken()
      const newToken = await refreshing
      refreshing = null
      if (newToken) {
        original.headers.Authorization = `Bearer ${newToken}`
        return api(original)
      }
      tokenStore.clear()
      onAuthFailure?.()
    }
    return Promise.reject(error)
  },
)

/** Extracts a human-readable message from an API error. */
export function apiErrorMessage(error: unknown, fallback = 'Something went wrong'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined
    return data?.message ?? error.message ?? fallback
  }
  return fallback
}
