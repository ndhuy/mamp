import { useEffect, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, TextField,
} from '@mui/material'
import { createStockSite, updateStockSite } from '../api/masterdata'
import { apiErrorMessage } from '../api/client'
import type { StockSite } from '../api/types'

interface Props {
  open: boolean
  site: StockSite | null
  onClose: () => void
}

const EMPTY = { name: '', website: '', dashboardUrl: '', notes: '', displayOrder: '0', categoriesRequired: '0' }
const grid = { display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }

export function StockSiteFormDialog({ open, site, onClose }: Props) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setError(null)
    setForm(site ? {
      name: site.name, website: site.website ?? '', dashboardUrl: site.dashboardUrl ?? '',
      notes: site.notes ?? '', displayOrder: site.displayOrder.toString(),
      categoriesRequired: site.categoriesRequired.toString(),
    } : EMPTY)
  }, [open, site])

  const set = <K extends keyof typeof form>(k: K, v: (typeof form)[K]) => setForm((f) => ({ ...f, [k]: v }))

  const save = useMutation({
    mutationFn: () => {
      const payload = {
        name: form.name.trim(), website: form.website || null, dashboardUrl: form.dashboardUrl || null,
        notes: form.notes || null, displayOrder: Number(form.displayOrder) || 0,
        categoriesRequired: Number(form.categoriesRequired) || 0,
      }
      return site ? updateStockSite(site.id, payload) : createStockSite(payload)
    },
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['stock-sites'] }); onClose() },
    onError: (e) => setError(apiErrorMessage(e, 'Could not save stock site')),
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{site ? 'Edit Stock Site' : 'Add Stock Site'}</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2.5} sx={{ pt: 0.5 }}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField label="Site Name *" value={form.name} onChange={(e) => set('name', e.target.value)} fullWidth />
          <TextField label="Website" value={form.website} onChange={(e) => set('website', e.target.value)} fullWidth />
          <TextField label="Contributor Dashboard URL" value={form.dashboardUrl} onChange={(e) => set('dashboardUrl', e.target.value)} fullWidth />
          <Box sx={grid}>
            <TextField label="Display Order" type="number" value={form.displayOrder} onChange={(e) => set('displayOrder', e.target.value)} fullWidth />
            <TextField select label="Categories Required" value={form.categoriesRequired}
              onChange={(e) => set('categoriesRequired', e.target.value)} fullWidth
              helperText="Per submission">
              <MenuItem value="0">0 — none</MenuItem>
              <MenuItem value="1">1 — primary</MenuItem>
              <MenuItem value="2">2 — primary + secondary</MenuItem>
            </TextField>
          </Box>
          <TextField label="Notes" value={form.notes} onChange={(e) => set('notes', e.target.value)} fullWidth multiline minRows={2} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" disabled={!form.name.trim() || save.isPending} onClick={() => { setError(null); save.mutate() }}>
          {save.isPending ? 'Saving…' : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
