import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Alert, Autocomplete, Box, Button, Card, CardContent, Checkbox, CircularProgress,
  Divider, FormControlLabel, MenuItem, Stack, Tab, Tabs, TextField, Typography,
} from '@mui/material'
import ImageIcon from '@mui/icons-material/Image'
import { createMedia, getMedia, updateMedia } from '../api/media'
import { listConcepts, createConcept } from '../api/concepts'
import { listCaptureDevices, listLenses } from '../api/masterdata'
import { uploadThumbnail } from '../api/uploads'
import { apiErrorMessage } from '../api/client'
import { thumbnailUrl } from '../config'
import type { Concept, MediaRequestPayload } from '../api/types'
import {
  STORAGE_TYPE_LABELS, STORAGE_TYPE_OPTIONS, WORKFLOW_STATUS_LABELS, WORKFLOW_STATUS_OPTIONS,
} from '../labels'

const schema = z.object({
  title: z.string().min(1, 'Title is required').max(120, 'Max 120 characters'),
  mediaType: z.enum(['PHOTO', 'FOOTAGE']),
  workflowStatus: z.string(),
  contentUsageType: z.string(),
  captureDeviceId: z.string(),
  lensId: z.string(),
  captureDate: z.string(),
  location: z.string(),
  storageType: z.string(),
  originalFilePath: z.string(),
  exportFilePath: z.string(),
  thumbnailKey: z.string(),
  aiGenerated: z.boolean(),
  description: z.string(),
  notes: z.string(),
  editorialCaption: z.string(),
  eventDate: z.string(),
  editorialLocation: z.string(),
  eventSubjectName: z.string(),
  editorialNotes: z.string(),
  concepts: z.array(z.any()),
  keywords: z.array(z.string()),
})
type FormValues = z.infer<typeof schema>

const EMPTY: FormValues = {
  title: '', mediaType: 'PHOTO', workflowStatus: 'DRAFT', contentUsageType: '',
  captureDeviceId: '', lensId: '', captureDate: '', location: '', storageType: '',
  originalFilePath: '', exportFilePath: '', thumbnailKey: '', aiGenerated: false,
  description: '', notes: '', editorialCaption: '', eventDate: '', editorialLocation: '',
  eventSubjectName: '', editorialNotes: '', concepts: [], keywords: [],
}

function TabPanel({ active, children }: { active: boolean; children: React.ReactNode }) {
  return <Box sx={{ display: active ? 'block' : 'none', pt: 3 }}>{children}</Box>
}

const grid = { display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2.5 }

export function MediaFormPage() {
  const { id } = useParams()
  const isEdit = !!id
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [tab, setTab] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)

  const devices = useQuery({ queryKey: ['capture-devices'], queryFn: () => listCaptureDevices() })
  const lenses = useQuery({ queryKey: ['lenses'], queryFn: () => listLenses() })
  const concepts = useQuery({ queryKey: ['concepts'], queryFn: () => listConcepts() })
  const mediaQuery = useQuery({ queryKey: ['media', id], queryFn: () => getMedia(id!), enabled: isEdit })

  const { control, register, handleSubmit, reset, watch, setValue, formState: { errors, isSubmitting } } =
    useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: EMPTY })

  const handleThumbnailUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setUploading(true)
    setError(null)
    try {
      const result = await uploadThumbnail(file)
      setValue('thumbnailKey', result.key, { shouldDirty: true })
    } catch (err) {
      setError(apiErrorMessage(err, 'Thumbnail upload failed'))
    } finally {
      setUploading(false)
    }
  }

  useEffect(() => {
    const m = mediaQuery.data
    if (!m) return
    reset({
      title: m.title,
      mediaType: m.mediaType,
      workflowStatus: m.workflowStatus,
      contentUsageType: m.contentUsageType ?? '',
      captureDeviceId: m.captureDevice?.id ?? '',
      lensId: m.lens?.id ?? '',
      captureDate: m.captureDate ?? '',
      location: m.location ?? '',
      storageType: m.storageType ?? '',
      originalFilePath: m.originalFilePath ?? '',
      exportFilePath: m.exportFilePath ?? '',
      thumbnailKey: m.thumbnailKey ?? '',
      aiGenerated: m.aiGenerated,
      description: m.description ?? '',
      notes: m.notes ?? '',
      editorialCaption: m.editorialCaption ?? '',
      eventDate: m.eventDate ?? '',
      editorialLocation: m.editorialLocation ?? '',
      eventSubjectName: m.eventSubjectName ?? '',
      editorialNotes: m.editorialNotes ?? '',
      concepts: m.concepts.map((c) => ({ id: c.id, name: c.name, description: null, active: true })),
      keywords: m.keywords,
    })
  }, [mediaQuery.data, reset])

  const save = useMutation({
    mutationFn: async (values: FormValues) => {
      const conceptIds = await resolveConceptIds(values.concepts as (Concept | string)[])
      const payload: MediaRequestPayload = {
        title: values.title.trim(),
        mediaType: values.mediaType,
        workflowStatus: values.workflowStatus as MediaRequestPayload['workflowStatus'],
        contentUsageType: (values.contentUsageType || null) as MediaRequestPayload['contentUsageType'],
        captureDeviceId: values.captureDeviceId || null,
        lensId: values.lensId || null,
        captureDate: values.captureDate || null,
        location: values.location || null,
        storageType: (values.storageType || null) as MediaRequestPayload['storageType'],
        originalFilePath: values.originalFilePath || null,
        exportFilePath: values.exportFilePath || null,
        thumbnailKey: values.thumbnailKey || null,
        aiGenerated: values.aiGenerated,
        description: values.description || null,
        notes: values.notes || null,
        editorialCaption: values.editorialCaption || null,
        eventDate: values.eventDate || null,
        editorialLocation: values.editorialLocation || null,
        eventSubjectName: values.eventSubjectName || null,
        editorialNotes: values.editorialNotes || null,
        conceptIds,
        keywords: values.keywords,
      }
      return isEdit ? updateMedia(id!, payload) : createMedia(payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['media'] })
      navigate('/media')
    },
    onError: (e) => setError(apiErrorMessage(e, 'Could not save media')),
  })

  async function resolveConceptIds(items: (Concept | string)[]): Promise<string[]> {
    const ids: string[] = []
    for (const item of items) {
      if (typeof item === 'string') {
        const name = item.trim()
        if (!name) continue
        const existing = concepts.data?.find((c) => c.name.toLowerCase() === name.toLowerCase())
        ids.push(existing ? existing.id : (await createConcept(name)).id)
      } else {
        ids.push(item.id)
      }
    }
    return ids
  }

  const onSubmit = (values: FormValues) => { setError(null); save.mutate(values) }
  const isEditorial = watch('contentUsageType') === 'EDITORIAL'

  if (isEdit && mediaQuery.isLoading) {
    return <Box sx={{ display: 'grid', placeItems: 'center', py: 10 }}><CircularProgress /></Box>
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Stack spacing={2}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography variant="h6">{isEdit ? `Edit ${mediaQuery.data?.code ?? ''}` : 'Add Media'}</Typography>
            <Typography variant="body2" color="text.secondary">
              {isEdit ? 'Update the asset details.' : 'Create a new photo or footage record.'}
            </Typography>
          </Box>
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate('/media')}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={isSubmitting || save.isPending}>
              {save.isPending ? 'Saving…' : 'Save Media'}
            </Button>
          </Stack>
        </Box>

        {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}

        <Card>
          <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ px: 2, borderBottom: '1px solid #e2e8f0' }}>
            <Tab label="General" />
            <Tab label="Metadata" />
            <Tab label="Keywords" />
          </Tabs>
          <CardContent>
            {/* GENERAL */}
            <TabPanel active={tab === 0}>
              <Box sx={grid}>
                <TextField label="Title *" fullWidth {...register('title')}
                  error={!!errors.title} helperText={errors.title?.message} />
                <Controller name="mediaType" control={control} render={({ field }) => (
                  <TextField select label="Media Type *" fullWidth {...field}>
                    <MenuItem value="PHOTO">Photo</MenuItem>
                    <MenuItem value="FOOTAGE">Footage</MenuItem>
                  </TextField>
                )} />
                <Controller name="captureDeviceId" control={control} render={({ field }) => (
                  <TextField select label="Capture Device *" fullWidth {...field}>
                    <MenuItem value="">— None —</MenuItem>
                    {devices.data?.map((d) => <MenuItem key={d.id} value={d.id}>{d.brand} {d.model}</MenuItem>)}
                  </TextField>
                )} />
                <Controller name="lensId" control={control} render={({ field }) => (
                  <TextField select label="Lens (optional)" fullWidth {...field}>
                    <MenuItem value="">— None —</MenuItem>
                    {lenses.data?.map((l) => <MenuItem key={l.id} value={l.id}>{l.brand} {l.model}</MenuItem>)}
                  </TextField>
                )} />
                <TextField label="Capture Date" type="date" fullWidth slotProps={{ inputLabel: { shrink: true } }}
                  {...register('captureDate')} />
                <Controller name="contentUsageType" control={control} render={({ field }) => (
                  <TextField select label="Content Usage Type" fullWidth {...field}>
                    <MenuItem value="">— Not set —</MenuItem>
                    <MenuItem value="COMMERCIAL">Commercial</MenuItem>
                    <MenuItem value="EDITORIAL">Editorial</MenuItem>
                  </TextField>
                )} />
                <Controller name="workflowStatus" control={control} render={({ field }) => (
                  <TextField select label="Workflow Status" fullWidth {...field}>
                    {WORKFLOW_STATUS_OPTIONS.map((s) => (
                      <MenuItem key={s} value={s}>{WORKFLOW_STATUS_LABELS[s]}</MenuItem>
                    ))}
                  </TextField>
                )} />
                <Controller name="concepts" control={control} render={({ field }) => (
                  <Autocomplete
                    multiple freeSolo options={concepts.data ?? []}
                    getOptionLabel={(o) => (typeof o === 'string' ? o : o.name)}
                    isOptionEqualToValue={(o, v) =>
                      typeof o !== 'string' && typeof v !== 'string' ? o.id === v.id : o === v}
                    value={field.value as Concept[]}
                    onChange={(_, v) => field.onChange(v)}
                    renderInput={(p) => <TextField {...p} label="Concepts" placeholder="Add concept" />}
                  />
                )} />
              </Box>
              <Divider sx={{ my: 3 }} />
              <Box sx={grid}>
                <Controller name="storageType" control={control} render={({ field }) => (
                  <TextField select label="Storage Type" fullWidth {...field}>
                    <MenuItem value="">— Not set —</MenuItem>
                    {STORAGE_TYPE_OPTIONS.map((s) => <MenuItem key={s} value={s}>{STORAGE_TYPE_LABELS[s]}</MenuItem>)}
                  </TextField>
                )} />
                <Box>
                  <Typography variant="caption" color="text.secondary">Thumbnail</Typography>
                  <Stack direction="row" spacing={2} sx={{ alignItems: 'center', mt: 0.5 }}>
                    {watch('thumbnailKey')
                      ? <Box component="img" src={thumbnailUrl(watch('thumbnailKey'))}
                          sx={{ width: 72, height: 72, objectFit: 'cover', borderRadius: 2, border: '1px solid #e2e8f0' }} />
                      : <Box sx={{ width: 72, height: 72, borderRadius: 2, bgcolor: '#f1f5f9', display: 'grid', placeItems: 'center', color: '#94a3b8' }}>
                          <ImageIcon />
                        </Box>}
                    <Stack spacing={0.5}>
                      <Button component="label" variant="outlined" size="small" disabled={uploading}>
                        {uploading ? 'Uploading…' : 'Upload image'}
                        <input type="file" hidden accept="image/*" onChange={handleThumbnailUpload} />
                      </Button>
                      {watch('thumbnailKey') && (
                        <Button size="small" color="error" onClick={() => setValue('thumbnailKey', '', { shouldDirty: true })}>
                          Remove
                        </Button>
                      )}
                    </Stack>
                  </Stack>
                </Box>
                <TextField label="Original File Path" fullWidth {...register('originalFilePath')} />
                <TextField label="Export File Path" fullWidth {...register('exportFilePath')} />
              </Box>
              <Controller name="aiGenerated" control={control} render={({ field }) => (
                <FormControlLabel sx={{ mt: 2 }} control={
                  <Checkbox checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />
                } label="This media contains AI-generated content" />
              )} />
            </TabPanel>

            {/* METADATA */}
            <TabPanel active={tab === 1}>
              <Stack spacing={2.5}>
                <TextField label="Description" fullWidth multiline minRows={3} {...register('description')} />
                <Box sx={grid}>
                  <TextField label="Location" fullWidth {...register('location')} />
                </Box>
                <TextField label="Internal Notes" fullWidth multiline minRows={2} {...register('notes')} />
                {isEditorial && (
                  <>
                    <Divider textAlign="left"><Typography variant="subtitle2">Editorial details</Typography></Divider>
                    <TextField label="Editorial Caption" fullWidth multiline minRows={2} {...register('editorialCaption')} />
                    <Box sx={grid}>
                      <TextField label="Event Date" type="date" fullWidth slotProps={{ inputLabel: { shrink: true } }}
                        {...register('eventDate')} />
                      <TextField label="Editorial Location" fullWidth {...register('editorialLocation')} />
                      <TextField label="Event / Subject Name" fullWidth {...register('eventSubjectName')} />
                    </Box>
                    <TextField label="Editorial Notes" fullWidth multiline minRows={2} {...register('editorialNotes')} />
                  </>
                )}
              </Stack>
            </TabPanel>

            {/* KEYWORDS */}
            <TabPanel active={tab === 2}>
              <Controller name="keywords" control={control} render={({ field }) => (
                <Autocomplete
                  multiple freeSolo options={[] as string[]} value={field.value}
                  onChange={(_, v) => field.onChange(v)}
                  renderInput={(p) => (
                    <TextField {...p} label="Keywords" placeholder="Type a keyword and press Enter" />
                  )}
                />
              )} />
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                Keywords are normalized (trimmed, lowercased) and de-duplicated per your account.
              </Typography>
            </TabPanel>
          </CardContent>
        </Card>
      </Stack>
    </form>
  )
}
