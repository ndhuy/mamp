import { useEffect, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, TextField,
} from '@mui/material'
import { createCaptureDevice, updateCaptureDevice } from '../api/masterdata'
import { apiErrorMessage } from '../api/client'
import type { CaptureDevice, DeviceType } from '../api/types'
import { DEVICE_TYPE_LABELS, DEVICE_TYPE_OPTIONS } from '../labels'

interface Props {
  open: boolean
  device: CaptureDevice | null
  onClose: () => void
}

const EMPTY = { brand: '', model: '', deviceType: 'INTERCHANGEABLE_LENS' as DeviceType, mount: '', serialNumber: '', notes: '' }
const grid = { display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }

export function DeviceFormDialog({ open, device, onClose }: Props) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setError(null)
    setForm(device ? {
      brand: device.brand, model: device.model, deviceType: device.deviceType,
      mount: device.mount ?? '', serialNumber: device.serialNumber ?? '', notes: device.notes ?? '',
    } : EMPTY)
  }, [open, device])

  const set = <K extends keyof typeof form>(k: K, v: (typeof form)[K]) => setForm((f) => ({ ...f, [k]: v }))

  const save = useMutation({
    mutationFn: () => {
      const payload = {
        brand: form.brand.trim(), model: form.model.trim(), deviceType: form.deviceType,
        mount: form.mount || null, serialNumber: form.serialNumber || null, notes: form.notes || null,
      }
      return device ? updateCaptureDevice(device.id, payload) : createCaptureDevice(payload)
    },
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['capture-devices'] }); onClose() },
    onError: (e) => setError(apiErrorMessage(e, 'Could not save device')),
  })

  const valid = form.brand.trim() && form.model.trim()

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{device ? 'Edit Capture Device' : 'Add Capture Device'}</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2.5} sx={{ pt: 0.5 }}>
          {error && <Alert severity="error">{error}</Alert>}
          <Box sx={grid}>
            <TextField label="Brand *" value={form.brand} onChange={(e) => set('brand', e.target.value)} fullWidth />
            <TextField label="Model *" value={form.model} onChange={(e) => set('model', e.target.value)} fullWidth />
          </Box>
          <TextField select label="Device Type *" value={form.deviceType}
            onChange={(e) => set('deviceType', e.target.value as DeviceType)} fullWidth>
            {DEVICE_TYPE_OPTIONS.map((t) => <MenuItem key={t} value={t}>{DEVICE_TYPE_LABELS[t]}</MenuItem>)}
          </TextField>
          <Box sx={grid}>
            <TextField label="Mount" value={form.mount} onChange={(e) => set('mount', e.target.value)} fullWidth />
            <TextField label="Serial Number" value={form.serialNumber} onChange={(e) => set('serialNumber', e.target.value)} fullWidth />
          </Box>
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
