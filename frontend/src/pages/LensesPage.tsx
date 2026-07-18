import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Box, Button, Chip, IconButton, Paper, Stack, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Tooltip, Typography,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import ToggleOnIcon from '@mui/icons-material/ToggleOn'
import ToggleOffIcon from '@mui/icons-material/ToggleOff'
import { listLenses, setLensActive } from '../api/masterdata'
import type { Lens } from '../api/types'
import { LENS_TYPE_LABELS } from '../labels'
import { useAuth } from '../auth/AuthProvider'
import { LensFormDialog } from '../components/LensFormDialog'

function focalRange(l: Lens): string {
  if (l.minFocalLength && l.maxFocalLength) {
    return l.minFocalLength === l.maxFocalLength ? `${l.minFocalLength}mm` : `${l.minFocalLength}-${l.maxFocalLength}mm`
  }
  return l.minFocalLength ? `${l.minFocalLength}mm` : '—'
}

export function LensesPage() {
  const { isAdmin } = useAuth()
  const queryClient = useQueryClient()
  const { data, isLoading } = useQuery({ queryKey: ['lenses', 'all'], queryFn: () => listLenses(isAdmin) })
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<Lens | null>(null)

  const toggle = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => setLensActive(id, active),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['lenses'] }),
  })

  return (
    <Stack spacing={2}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h6">Lenses</Typography>
        {isAdmin && <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditing(null); setDialogOpen(true) }}>Add Lens</Button>}
      </Box>
      <Paper variant="outlined">
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ '& th': { bgcolor: '#f8fafc', fontWeight: 700 } }}>
                <TableCell>Brand</TableCell>
                <TableCell>Model</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Focal Length</TableCell>
                <TableCell>Max Aperture</TableCell>
                <TableCell align="center">Status</TableCell>
                {isAdmin && <TableCell align="right">Actions</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading && <TableRow><TableCell colSpan={7} align="center" sx={{ py: 4 }}>Loading…</TableCell></TableRow>}
              {!isLoading && data?.length === 0 && (
                <TableRow><TableCell colSpan={7} align="center" sx={{ py: 6, color: 'text.secondary' }}>No lenses yet.</TableCell></TableRow>
              )}
              {data?.map((l) => (
                <TableRow key={l.id} hover>
                  <TableCell><Typography sx={{ fontWeight: 600 }}>{l.brand}</Typography></TableCell>
                  <TableCell>{l.model}</TableCell>
                  <TableCell>{l.lensType ? LENS_TYPE_LABELS[l.lensType] : '—'}</TableCell>
                  <TableCell>{focalRange(l)}</TableCell>
                  <TableCell>{l.maxAperture ?? '—'}</TableCell>
                  <TableCell align="center">
                    <Chip size="small" color={l.active ? 'success' : 'default'} label={l.active ? 'Active' : 'Inactive'} />
                  </TableCell>
                  {isAdmin && (
                    <TableCell align="right">
                      <Tooltip title="Edit">
                        <IconButton size="small" onClick={() => { setEditing(l); setDialogOpen(true) }}><EditOutlinedIcon fontSize="small" /></IconButton>
                      </Tooltip>
                      <Tooltip title={l.active ? 'Deactivate' : 'Activate'}>
                        <IconButton size="small" onClick={() => toggle.mutate({ id: l.id, active: !l.active })}>
                          {l.active ? <ToggleOnIcon color="success" /> : <ToggleOffIcon />}
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  )}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
      <LensFormDialog open={dialogOpen} lens={editing} onClose={() => setDialogOpen(false)} />
    </Stack>
  )
}
