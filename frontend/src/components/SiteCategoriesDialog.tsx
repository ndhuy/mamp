import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Alert, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, IconButton, MenuItem,
  Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Tooltip, Typography,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import ToggleOnIcon from '@mui/icons-material/ToggleOn'
import ToggleOffIcon from '@mui/icons-material/ToggleOff'
import { createSiteCategory, listSiteCategories, setSiteCategoryActive } from '../api/masterdata'
import { apiErrorMessage } from '../api/client'
import type { StockSite } from '../api/types'

interface Props {
  site: StockSite | null
  onClose: () => void
}

export function SiteCategoriesDialog({ site, onClose }: Props) {
  const queryClient = useQueryClient()
  const [name, setName] = useState('')
  const [parentId, setParentId] = useState('')
  const [error, setError] = useState<string | null>(null)

  const categories = useQuery({
    queryKey: ['site-categories', site?.id],
    queryFn: () => listSiteCategories(site!.id, true),
    enabled: !!site,
  })

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['site-categories', site?.id] })

  const add = useMutation({
    mutationFn: () => createSiteCategory(site!.id, { name: name.trim(), parentId: parentId || null }),
    onSuccess: () => { setName(''); setParentId(''); setError(null); invalidate() },
    onError: (e) => setError(apiErrorMessage(e, 'Could not add category')),
  })
  const toggle = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => setSiteCategoryActive(id, active),
    onSuccess: invalidate,
  })

  const activeParents = categories.data?.filter((c) => c.active) ?? []

  return (
    <Dialog open={!!site} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{site?.name} — Categories</DialogTitle>
      <DialogContent dividers>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} sx={{ mb: 2 }}>
          <TextField size="small" label="New category" value={name} onChange={(e) => setName(e.target.value)} sx={{ flex: 1 }} />
          <TextField select size="small" label="Parent" value={parentId} onChange={(e) => setParentId(e.target.value)} sx={{ minWidth: 150 }}>
            <MenuItem value="">— None —</MenuItem>
            {activeParents.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
          </TextField>
          <Button variant="contained" startIcon={<AddIcon />} disabled={!name.trim() || add.isPending} onClick={() => add.mutate()}>
            Add
          </Button>
        </Stack>

        <Table size="small">
          <TableHead>
            <TableRow sx={{ '& th': { fontWeight: 700 } }}>
              <TableCell>Name</TableCell>
              <TableCell>Parent</TableCell>
              <TableCell align="center">Status</TableCell>
              <TableCell align="right">Active</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {categories.data?.length === 0 && (
              <TableRow><TableCell colSpan={4} align="center" sx={{ py: 3, color: 'text.secondary' }}>No categories yet.</TableCell></TableRow>
            )}
            {categories.data?.map((c) => (
              <TableRow key={c.id}>
                <TableCell>{c.name}</TableCell>
                <TableCell>{c.parentName ?? '—'}</TableCell>
                <TableCell align="center">
                  <Chip size="small" color={c.active ? 'success' : 'default'} label={c.active ? 'Active' : 'Inactive'} />
                </TableCell>
                <TableCell align="right">
                  <Tooltip title={c.active ? 'Deactivate' : 'Activate'}>
                    <IconButton size="small" onClick={() => toggle.mutate({ id: c.id, active: !c.active })}>
                      {c.active ? <ToggleOnIcon color="success" /> : <ToggleOffIcon />}
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
          Inactive categories stay on historical submissions but can't be selected for new ones.
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  )
}
