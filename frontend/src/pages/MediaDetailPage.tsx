import { useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Box, Button, Card, CardContent, Chip, CircularProgress, Divider, IconButton, MenuItem, Paper,
  Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Tooltip, Typography,
} from '@mui/material'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlined'
import AddIcon from '@mui/icons-material/Add'
import { getMedia } from '../api/media'
import { addTargetSite, deleteSubmission, listSubmissions } from '../api/submissions'
import { listStockSites } from '../api/masterdata'
import { apiErrorMessage } from '../api/client'
import { thumbnailUrl } from '../config'
import type { Submission } from '../api/types'
import { SubmissionDialog } from '../components/SubmissionDialog'
import { SUBMISSION_STATUS_COLORS, SUBMISSION_STATUS_LABELS, WORKFLOW_STATUS_LABELS } from '../labels'

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary">{label}</Typography>
      <Typography variant="body2" sx={{ fontWeight: 500 }}>{children ?? '—'}</Typography>
    </Box>
  )
}

export function MediaDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [siteToAdd, setSiteToAdd] = useState('')
  const [editing, setEditing] = useState<Submission | null>(null)
  const [error, setError] = useState<string | null>(null)

  const media = useQuery({ queryKey: ['media', id], queryFn: () => getMedia(id!), enabled: !!id })
  const submissions = useQuery({ queryKey: ['submissions', id], queryFn: () => listSubmissions(id!), enabled: !!id })
  const sites = useQuery({ queryKey: ['stock-sites'], queryFn: () => listStockSites() })

  const targetedIds = useMemo(() => new Set(submissions.data?.map((s) => s.stockSiteId)), [submissions.data])
  const availableSites = sites.data?.filter((s) => !targetedIds.has(s.id)) ?? []
  const categoriesRequiredFor = (stockSiteId: string) =>
    sites.data?.find((s) => s.id === stockSiteId)?.categoriesRequired ?? 0

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['submissions', id] })

  const add = useMutation({
    mutationFn: () => addTargetSite(id!, siteToAdd),
    onSuccess: () => { setSiteToAdd(''); setError(null); invalidate() },
    onError: (e) => setError(apiErrorMessage(e, 'Could not add target site')),
  })
  const remove = useMutation({
    mutationFn: (submissionId: string) => deleteSubmission(submissionId),
    onSuccess: invalidate,
  })

  if (media.isLoading) {
    return <Box sx={{ display: 'grid', placeItems: 'center', py: 10 }}><CircularProgress /></Box>
  }
  const m = media.data
  if (!m) return <Typography>Media not found.</Typography>

  return (
    <Stack spacing={2}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
          <IconButton onClick={() => navigate('/media')}><ArrowBackIcon /></IconButton>
          <Box>
            <Typography variant="h6">{m.title}</Typography>
            <Typography variant="body2" color="text.secondary">{m.code}</Typography>
          </Box>
        </Stack>
        <Button variant="outlined" startIcon={<EditOutlinedIcon />} onClick={() => navigate(`/media/${m.id}/edit`)}>
          Edit
        </Button>
      </Box>

      {/* Overview */}
      <Card>
        <CardContent>
          <Typography variant="subtitle2" gutterBottom>Overview</Typography>
          {m.thumbnailKey && (
            <Box
              component="img"
              src={thumbnailUrl(m.thumbnailKey)}
              sx={{ width: '100%', maxWidth: 320, height: 200, objectFit: 'cover', borderRadius: 2, border: '1px solid #e2e8f0', mb: 2 }}
            />
          )}
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr 1fr', md: 'repeat(4, 1fr)' }, gap: 2.5, mt: 1 }}>
            <Field label="Type">{m.mediaType === 'PHOTO' ? 'Photo' : 'Footage'}</Field>
            <Field label="Workflow Status">
              <Chip size="small" label={WORKFLOW_STATUS_LABELS[m.workflowStatus]} />
            </Field>
            <Field label="Content Usage">{m.contentUsageType ? (m.contentUsageType === 'COMMERCIAL' ? 'Commercial' : 'Editorial') : '—'}</Field>
            <Field label="Capture Date">{m.captureDate ?? '—'}</Field>
            <Field label="Capture Device">{m.captureDevice?.name ?? '—'}</Field>
            <Field label="Lens">{m.lens?.name ?? '—'}</Field>
            <Field label="Concepts">
              {m.concepts.length ? m.concepts.map((c) => c.name).join(', ') : '—'}
            </Field>
            <Field label="Keywords">{m.keywords.length}</Field>
          </Box>
          {m.keywords.length > 0 && (
            <>
              <Divider sx={{ my: 2 }} />
              <Stack direction="row" spacing={0.5} sx={{ flexWrap: 'wrap', gap: 0.5 }}>
                {m.keywords.map((k) => <Chip key={k} size="small" variant="outlined" label={k} />)}
              </Stack>
            </>
          )}
        </CardContent>
      </Card>

      {/* Target Sites */}
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
            <Typography variant="subtitle2">Target Sites &amp; Submissions</Typography>
          </Box>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} sx={{ mb: 2 }}>
            <TextField select size="small" label="Add target site" value={siteToAdd}
              onChange={(e) => setSiteToAdd(e.target.value)} sx={{ minWidth: 240 }}
              disabled={availableSites.length === 0}>
              {availableSites.map((s) => <MenuItem key={s.id} value={s.id}>{s.name}</MenuItem>)}
            </TextField>
            <Button variant="contained" startIcon={<AddIcon />} disabled={!siteToAdd || add.isPending}
              onClick={() => add.mutate()}>
              Add
            </Button>
          </Stack>
          {error && <Typography color="error" variant="body2" sx={{ mb: 1 }}>{error}</Typography>}

          <Paper variant="outlined">
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ '& th': { bgcolor: '#f8fafc', fontWeight: 700 } }}>
                    <TableCell>Site</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Categories</TableCell>
                    <TableCell>Submitted</TableCell>
                    <TableCell>Reviewed</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {submissions.data?.length === 0 && (
                    <TableRow><TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                      No target sites yet. Add one above.
                    </TableCell></TableRow>
                  )}
                  {submissions.data?.map((s) => (
                    <TableRow key={s.id} hover>
                      <TableCell sx={{ fontWeight: 600 }}>{s.stockSiteName}</TableCell>
                      <TableCell>
                        <Chip size="small" color={SUBMISSION_STATUS_COLORS[s.status]} label={SUBMISSION_STATUS_LABELS[s.status]} />
                      </TableCell>
                      <TableCell>
                        {[s.primaryCategory?.name, s.secondaryCategory?.name].filter(Boolean).join(', ') || '—'}
                      </TableCell>
                      <TableCell>{s.submittedDate ?? '—'}</TableCell>
                      <TableCell>{s.reviewedDate ?? '—'}</TableCell>
                      <TableCell align="right">
                        <Tooltip title="Edit submission">
                          <IconButton size="small" onClick={() => setEditing(s)}><EditOutlinedIcon fontSize="small" /></IconButton>
                        </Tooltip>
                        <Tooltip title="Remove target site">
                          <IconButton size="small" color="error" onClick={() => remove.mutate(s.id)}>
                            <DeleteOutlineIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </CardContent>
      </Card>

      <SubmissionDialog
        submission={editing}
        categoriesRequired={editing ? categoriesRequiredFor(editing.stockSiteId) : 0}
        onClose={() => setEditing(null)}
      />
    </Stack>
  )
}
