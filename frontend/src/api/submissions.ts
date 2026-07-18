import { api } from './client'
import type { Submission, SubmissionUpdatePayload } from './types'

export async function listSubmissions(mediaId: string): Promise<Submission[]> {
  const { data } = await api.get<Submission[]>(`/media/${mediaId}/submissions`)
  return data
}

export async function addTargetSite(mediaId: string, stockSiteId: string): Promise<Submission> {
  const { data } = await api.post<Submission>(`/media/${mediaId}/submissions`, { stockSiteId })
  return data
}

export async function updateSubmission(id: string, payload: SubmissionUpdatePayload): Promise<Submission> {
  const { data } = await api.put<Submission>(`/submissions/${id}`, payload)
  return data
}

export async function deleteSubmission(id: string): Promise<void> {
  await api.delete(`/submissions/${id}`)
}
