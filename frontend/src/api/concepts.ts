import { api } from './client'
import type { Concept } from './types'

export async function listConcepts(): Promise<Concept[]> {
  const { data } = await api.get<Concept[]>('/concepts')
  return data
}

export async function createConcept(name: string, description?: string): Promise<Concept> {
  const { data } = await api.post<Concept>('/concepts', { name, description })
  return data
}
