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
import { listCaptureDevices, setCaptureDeviceActive } from '../api/masterdata'
import type { CaptureDevice } from '../api/types'
import { DEVICE_TYPE_LABELS } from '../labels'
import { useAuth } from '../auth/AuthProvider'
import { DeviceFormDialog } from '../components/DeviceFormDialog'

export function DevicesPage() {
  const { isAdmin } = useAuth()
  const queryClient = useQueryClient()
  const { data, isLoading } = useQuery({
    queryKey: ['capture-devices', 'all'],
    queryFn: () => listCaptureDevices(isAdmin),
  })
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<CaptureDevice | null>(null)

  const toggle = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => setCaptureDeviceActive(id, active),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['capture-devices'] }),
  })

  const openAdd = () => { setEditing(null); setDialogOpen(true) }
  const openEdit = (d: CaptureDevice) => { setEditing(d); setDialogOpen(true) }

  return (
    <Stack spacing={2}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h6">Camera / Devices</Typography>
        {isAdmin && <Button variant="contained" startIcon={<AddIcon />} onClick={openAdd}>Add Device</Button>}
      </Box>
      <Paper variant="outlined">
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ '& th': { bgcolor: '#f8fafc', fontWeight: 700 } }}>
                <TableCell>Brand</TableCell>
                <TableCell>Model</TableCell>
                <TableCell>Device Type</TableCell>
                <TableCell align="center">Status</TableCell>
                {isAdmin && <TableCell align="right">Actions</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading && <TableRow><TableCell colSpan={5} align="center" sx={{ py: 4 }}>Loading…</TableCell></TableRow>}
              {data?.map((d) => (
                <TableRow key={d.id} hover>
                  <TableCell><Typography sx={{ fontWeight: 600 }}>{d.brand}</Typography></TableCell>
                  <TableCell>{d.model}</TableCell>
                  <TableCell>{DEVICE_TYPE_LABELS[d.deviceType]}</TableCell>
                  <TableCell align="center">
                    <Chip size="small" color={d.active ? 'success' : 'default'} label={d.active ? 'Active' : 'Inactive'} />
                  </TableCell>
                  {isAdmin && (
                    <TableCell align="right">
                      <Tooltip title="Edit">
                        <IconButton size="small" onClick={() => openEdit(d)}><EditOutlinedIcon fontSize="small" /></IconButton>
                      </Tooltip>
                      <Tooltip title={d.active ? 'Deactivate' : 'Activate'}>
                        <IconButton size="small" onClick={() => toggle.mutate({ id: d.id, active: !d.active })}>
                          {d.active ? <ToggleOnIcon color="success" /> : <ToggleOffIcon />}
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
      <DeviceFormDialog open={dialogOpen} device={editing} onClose={() => setDialogOpen(false)} />
    </Stack>
  )
}
