import { api } from './client'
import type { TokenResponse, User } from './types'

export async function login(identifier: string, password: string): Promise<TokenResponse> {
  const { data } = await api.post<TokenResponse>('/auth/login', { identifier, password })
  return data
}

export async function register(input: {
  email: string
  username: string
  password: string
  confirmPassword: string
}): Promise<TokenResponse> {
  const { data } = await api.post<TokenResponse>('/auth/register', input)
  return data
}

export async function fetchMe(): Promise<User> {
  const { data } = await api.get<User>('/auth/me')
  return data
}

export async function logout(refreshToken: string): Promise<void> {
  await api.post('/auth/logout', { refreshToken })
}
