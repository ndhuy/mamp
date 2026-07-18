import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, TextField,
} from '@mui/material'
import { updateSubmission } from '../api/submissions'
import { listSiteCategories, listRejectionCategories } from '../api/masterdata'
import { apiErrorMessage } from '../api/client'
import type { Submission, SubmissionStatus, SubmissionUpdatePayload } from '../api/types'
import { SUBMISSION_STATUS_LABELS, SUBMISSION_STATUS_OPTIONS } from '../labels'

interface Props {
  submission: Submission | null
  categoriesRequired: number
  onClose: () => void
}

const grid = { display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }

export function SubmissionDialog({ submission, categoriesRequired, onClose }: Props) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({
    status: 'NOT_SUBMITTED' as SubmissionStatus,
    primaryCategoryId: '', secondaryCategoryId: '', contributorAssetId: '', assetUrl: '',
    submittedDate: '', reviewedDate: '', rejectionCategoryId: '', rejectionDetail: '', notes: '',
  })

  const categories = useQuery({
    queryKey: ['site-categories', submission?.stockSiteId],
    queryFn: () => listSiteCategories(submission!.stockSiteId),
    enabled: !!submission,
  })
  const rejections = useQuery({ queryKey: ['rejection-categories'], queryFn: () => listRejectionCategories() })

  useEffect(() => {
    if (!submission) return
    setError(null)
    setForm({
      status: submission.status,
      primaryCategoryId: submission.primaryCategory?.id ?? '',
      secondaryCategoryId: submission.secondaryCategory?.id ?? '',
      contributorAssetId: submission.contributorAssetId ?? '',
      assetUrl: submission.assetUrl ?? '',
      submittedDate: submission.submittedDate ?? '',
      reviewedDate: submission.reviewedDate ?? '',
      rejectionCategoryId: submission.rejectionCategory?.id ?? '',
      rejectionDetail: submission.rejectionDetail ?? '',
      notes: submission.notes ?? '',
    })
  }, [submission])

  const set = <K extends keyof typeof form>(key: K, value: (typeof form)[K]) =>
    setForm((f) => ({ ...f, [key]: value }))

  const save = useMutation({
    mutationFn: () => {
      const payload: SubmissionUpdatePayload = {
        status: form.status,
        primaryCategoryId: form.primaryCategoryId || null,
        secondaryCategoryId: form.secondaryCategoryId || null,
        contributorAssetId: form.contributorAssetId || null,
        assetUrl: form.assetUrl || null,
        submittedDate: form.submittedDate || null,
        reviewedDate: form.reviewedDate || null,
        rejectionCategoryId: form.rejectionCategoryId || null,
        rejectionDetail: form.rejectionDetail || null,
        notes: form.notes || null,
      }
      return updateSubmission(submission!.id, payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['submissions', submission!.mediaId] })
      onClose()
    },
    onError: (e) => setError(apiErrorMessage(e, 'Could not update submission')),
  })

  const isRejected = form.status === 'REJECTED'

  return (
    <Dialog open={!!submission} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{submission?.stockSiteName} — Submission</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2.5} sx={{ pt: 0.5 }}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField select label="Status" value={form.status} onChange={(e) => set('status', e.target.value as SubmissionStatus)} fullWidth>
            {SUBMISSION_STATUS_OPTIONS.map((s) => <MenuItem key={s} value={s}>{SUBMISSION_STATUS_LABELS[s]}</MenuItem>)}
          </TextField>

          {categoriesRequired >= 1 && (
            <Box sx={grid}>
              <TextField select label="Primary Category" value={form.primaryCategoryId}
                onChange={(e) => set('primaryCategoryId', e.target.value)} fullWidth
                helperText="Required for this site">
                <MenuItem value="">— None —</MenuItem>
                {categories.data?.filter((c) => c.active).map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
              </TextField>
              {categoriesRequired === 2 && (
                <TextField select label="Secondary Category" value={form.secondaryCategoryId}
                  onChange={(e) => set('secondaryCategoryId', e.target.value)} fullWidth>
                  <MenuItem value="">— None —</MenuItem>
                  {categories.data?.filter((c) => c.active).map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
                </TextField>
              )}
            </Box>
          )}

          <Box sx={grid}>
            <TextField label="Submitted Date" type="date" value={form.submittedDate}
              onChange={(e) => set('submittedDate', e.target.value)} slotProps={{ inputLabel: { shrink: true } }} fullWidth />
            <TextField label="Reviewed Date" type="date" value={form.reviewedDate}
              onChange={(e) => set('reviewedDate', e.target.value)} slotProps={{ inputLabel: { shrink: true } }} fullWidth />
          </Box>

          <Box sx={grid}>
            <TextField label="Contributor Asset ID" value={form.contributorAssetId}
              onChange={(e) => set('contributorAssetId', e.target.value)} fullWidth />
            <TextField label="Asset URL" value={form.assetUrl}
              onChange={(e) => set('assetUrl', e.target.value)} fullWidth />
          </Box>

          {isRejected && (
            <>
              <TextField select label="Rejection Category" value={form.rejectionCategoryId}
                onChange={(e) => set('rejectionCategoryId', e.target.value)} fullWidth
                helperText="Required when rejected">
                <MenuItem value="">— None —</MenuItem>
                {rejections.data?.filter((r) => r.active).map((r) => <MenuItem key={r.id} value={r.id}>{r.name}</MenuItem>)}
              </TextField>
              <TextField label="Rejection Detail" value={form.rejectionDetail}
                onChange={(e) => set('rejectionDetail', e.target.value)} fullWidth multiline minRows={2}
                helperText="Required when rejected" />
            </>
          )}

          <TextField label="Notes" value={form.notes} onChange={(e) => set('notes', e.target.value)} fullWidth multiline minRows={2} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={() => { setError(null); save.mutate() }} disabled={save.isPending}>
          {save.isPending ? 'Saving…' : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
