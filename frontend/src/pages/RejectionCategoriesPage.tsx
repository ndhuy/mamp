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
import { listRejectionCategories, setRejectionCategoryActive } from '../api/masterdata'
import type { RejectionCategory } from '../api/types'
import { useAuth } from '../auth/AuthProvider'
import { RejectionCategoryFormDialog } from '../components/RejectionCategoryFormDialog'

export function RejectionCategoriesPage() {
  const { isAdmin } = useAuth()
  const queryClient = useQueryClient()
  const { data, isLoading } = useQuery({
    queryKey: ['rejection-categories', 'all'],
    queryFn: () => listRejectionCategories(isAdmin),
  })
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<RejectionCategory | null>(null)

  const toggle = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => setRejectionCategoryActive(id, active),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['rejection-categories'] }),
  })

  return (
    <Stack spacing={2}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h6">Rejection Categories</Typography>
        {isAdmin && <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditing(null); setDialogOpen(true) }}>Add Category</Button>}
      </Box>
      <Paper variant="outlined">
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ '& th': { bgcolor: '#f8fafc', fontWeight: 700 } }}>
                <TableCell>Name</TableCell>
                <TableCell align="center">Status</TableCell>
                {isAdmin && <TableCell align="right">Actions</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading && <TableRow><TableCell colSpan={3} align="center" sx={{ py: 4 }}>Loading…</TableCell></TableRow>}
              {data?.map((r) => (
                <TableRow key={r.id} hover>
                  <TableCell><Typography sx={{ fontWeight: 600 }}>{r.name}</Typography></TableCell>
                  <TableCell align="center">
                    <Chip size="small" color={r.active ? 'success' : 'default'} label={r.active ? 'Active' : 'Inactive'} />
                  </TableCell>
                  {isAdmin && (
                    <TableCell align="right">
                      <Tooltip title="Edit">
                        <IconButton size="small" onClick={() => { setEditing(r); setDialogOpen(true) }}><EditOutlinedIcon fontSize="small" /></IconButton>
                      </Tooltip>
                      <Tooltip title={r.active ? 'Deactivate' : 'Activate'}>
                        <IconButton size="small" onClick={() => toggle.mutate({ id: r.id, active: !r.active })}>
                          {r.active ? <ToggleOnIcon color="success" /> : <ToggleOffIcon />}
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
      <RejectionCategoryFormDialog open={dialogOpen} category={editing} onClose={() => setDialogOpen(false)} />
    </Stack>
  )
}
