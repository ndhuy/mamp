import { api } from './client'
import type { Role, UserAdmin, UserStatus } from './types'

export async function listUsers(): Promise<UserAdmin[]> {
  const { data } = await api.get<UserAdmin[]>('/admin/users')
  return data
}

export const setUserStatus = (id: string, status: UserStatus) =>
  api.patch<UserAdmin>(`/admin/users/${id}/status`, { status }).then((r) => r.data)

export const setUserRole = (id: string, role: Role) =>
  api.patch<UserAdmin>(`/admin/users/${id}/role`, { role }).then((r) => r.data)

export const resetUserPassword = (id: string, newPassword: string) =>
  api.post(`/admin/users/${id}/reset-password`, { newPassword })
