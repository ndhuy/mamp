import { useEffect, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, TextField,
} from '@mui/material'
import { createLens, updateLens } from '../api/masterdata'
import { apiErrorMessage } from '../api/client'
import type { Lens, LensTypeValue } from '../api/types'
import { LENS_TYPE_LABELS, LENS_TYPE_OPTIONS } from '../labels'

interface Props {
  open: boolean
  lens: Lens | null
  onClose: () => void
}

const EMPTY = { brand: '', model: '', mount: '', lensType: '', minFocalLength: '', maxFocalLength: '', maxAperture: '', notes: '' }
const grid = { display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }

export function LensFormDialog({ open, lens, onClose }: Props) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setError(null)
    setForm(lens ? {
      brand: lens.brand, model: lens.model, mount: lens.mount ?? '', lensType: lens.lensType ?? '',
      minFocalLength: lens.minFocalLength?.toString() ?? '', maxFocalLength: lens.maxFocalLength?.toString() ?? '',
      maxAperture: lens.maxAperture ?? '', notes: lens.notes ?? '',
    } : EMPTY)
  }, [open, lens])

  const set = <K extends keyof typeof form>(k: K, v: (typeof form)[K]) => setForm((f) => ({ ...f, [k]: v }))
  const num = (s: string) => (s.trim() === '' ? null : Number(s))

  const save = useMutation({
    mutationFn: () => {
      const payload = {
        brand: form.brand.trim(), model: form.model.trim(), mount: form.mount || null,
        lensType: (form.lensType || null) as LensTypeValue | null,
        minFocalLength: num(form.minFocalLength), maxFocalLength: num(form.maxFocalLength),
        maxAperture: form.maxAperture || null, notes: form.notes || null,
      }
      return lens ? updateLens(lens.id, payload) : createLens(payload)
    },
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['lenses'] }); onClose() },
    onError: (e) => setError(apiErrorMessage(e, 'Could not save lens')),
  })

  const valid = form.brand.trim() && form.model.trim()

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{lens ? 'Edit Lens' : 'Add Lens'}</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2.5} sx={{ pt: 0.5 }}>
          {error && <Alert severity="error">{error}</Alert>}
          <Box sx={grid}>
            <TextField label="Brand *" value={form.brand} onChange={(e) => set('brand', e.target.value)} fullWidth />
            <TextField label="Model *" value={form.model} onChange={(e) => set('model', e.target.value)} fullWidth />
          </Box>
          <Box sx={grid}>
            <TextField select label="Lens Type" value={form.lensType} onChange={(e) => set('lensType', e.target.value)} fullWidth>
              <MenuItem value="">— Not set —</MenuItem>
              {LENS_TYPE_OPTIONS.map((t) => <MenuItem key={t} value={t}>{LENS_TYPE_LABELS[t]}</MenuItem>)}
            </TextField>
            <TextField label="Mount" value={form.mount} onChange={(e) => set('mount', e.target.value)} fullWidth />
          </Box>
          <Box sx={grid}>
            <TextField label="Min Focal Length (mm)" type="number" value={form.minFocalLength}
              onChange={(e) => set('minFocalLength', e.target.value)} fullWidth />
            <TextField label="Max Focal Length (mm)" type="number" value={form.maxFocalLength}
              onChange={(e) => set('maxFocalLength', e.target.value)} fullWidth />
          </Box>
          <TextField label="Max Aperture" value={form.maxAperture} onChange={(e) => set('maxAperture', e.target.value)} fullWidth />
          <TextField label="Notes" value={form.notes} onChange={(e) => set('notes', e.target.value)} fullWidth multiline minRows={2} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" disabled={!valid || save.isPending} onClick={() => { setError(null); save.mutate() }}>
          {save.isPending ? 'Saving…' : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
