import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, IconButton, Paper,
  Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Tooltip, Typography,
} from '@mui/material'
import BlockIcon from '@mui/icons-material/Block'
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutlined'
import KeyOutlinedIcon from '@mui/icons-material/KeyOutlined'
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined'
import PersonOutlineIcon from '@mui/icons-material/PersonOutlined'
import { listUsers, resetUserPassword, setUserRole, setUserStatus } from '../api/users'
import { apiErrorMessage } from '../api/client'
import type { UserAdmin } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function UsersPage() {
  const { user: currentUser } = useAuth()
  const queryClient = useQueryClient()
  const { data, isLoading } = useQuery({ queryKey: ['admin-users'], queryFn: listUsers })
  const [resetTarget, setResetTarget] = useState<UserAdmin | null>(null)
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['admin-users'] })

  const status = useMutation({
    mutationFn: ({ id, next }: { id: string; next: 'ACTIVE' | 'DISABLED' }) => setUserStatus(id, next),
    onSuccess: invalidate,
    onError: (e) => setError(apiErrorMessage(e, 'Could not update status')),
  })
  const role = useMutation({
    mutationFn: ({ id, next }: { id: string; next: 'USER' | 'ADMIN' }) => setUserRole(id, next),
    onSuccess: invalidate,
    onError: (e) => setError(apiErrorMessage(e, 'Could not update role')),
  })
  const reset = useMutation({
    mutationFn: () => resetUserPassword(resetTarget!.id, newPassword),
    onSuccess: () => { setResetTarget(null); setNewPassword(''); setError(null) },
    onError: (e) => setError(apiErrorMessage(e, 'Could not reset password')),
  })

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="h6">Users</Typography>
        <Typography variant="body2" color="text.secondary">Manage accounts, roles, and access.</Typography>
      </Box>
      {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}

      <Paper variant="outlined">
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ '& th': { bgcolor: '#f8fafc', fontWeight: 700 } }}>
                <TableCell>Username</TableCell>
                <TableCell>Email</TableCell>
                <TableCell align="center">Role</TableCell>
                <TableCell align="center">Status</TableCell>
                <TableCell>Created</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading && <TableRow><TableCell colSpan={6} align="center" sx={{ py: 4 }}>Loading…</TableCell></TableRow>}
              {data?.map((u) => {
                const isSelf = u.id === currentUser?.id
                const disabled = u.status === 'DISABLED'
                return (
                  <TableRow key={u.id} hover>
                    <TableCell>
                      <Typography sx={{ fontWeight: 600 }}>
                        {u.username}{isSelf && <Chip size="small" label="You" sx={{ ml: 1 }} />}
                      </Typography>
                    </TableCell>
                    <TableCell>{u.email}</TableCell>
                    <TableCell align="center">
                      <Chip size="small" color={u.role === 'ADMIN' ? 'primary' : 'default'} label={u.role === 'ADMIN' ? 'Admin' : 'User'} />
                    </TableCell>
                    <TableCell align="center">
                      <Chip size="small" color={disabled ? 'error' : 'success'} label={disabled ? 'Disabled' : 'Active'} />
                    </TableCell>
                    <TableCell>{new Date(u.createdAt).toLocaleDateString()}</TableCell>
                    <TableCell align="right">
                      <Tooltip title={u.role === 'ADMIN' ? 'Make User' : 'Make Admin'}>
                        <span>
                          <IconButton size="small" disabled={isSelf && u.role === 'ADMIN'}
                            onClick={() => role.mutate({ id: u.id, next: u.role === 'ADMIN' ? 'USER' : 'ADMIN' })}>
                            {u.role === 'ADMIN' ? <PersonOutlineIcon fontSize="small" /> : <AdminPanelSettingsOutlinedIcon fontSize="small" />}
                          </IconButton>
                        </span>
                      </Tooltip>
                      <Tooltip title={disabled ? 'Reactivate' : 'Disable'}>
                        <span>
                          <IconButton size="small" color={disabled ? 'success' : 'error'} disabled={isSelf && !disabled}
                            onClick={() => status.mutate({ id: u.id, next: disabled ? 'ACTIVE' : 'DISABLED' })}>
                            {disabled ? <CheckCircleOutlineIcon fontSize="small" /> : <BlockIcon fontSize="small" />}
                          </IconButton>
                        </span>
                      </Tooltip>
                      <Tooltip title="Reset password">
                        <IconButton size="small" onClick={() => { setResetTarget(u); setNewPassword(''); setError(null) }}>
                          <KeyOutlinedIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Dialog open={!!resetTarget} onClose={() => setResetTarget(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Reset password — {resetTarget?.username}</DialogTitle>
        <DialogContent dividers>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Set a new password for this user. Their active sessions will be signed out.
          </Typography>
          <TextField label="New password" type="password" value={newPassword} fullWidth autoFocus
            onChange={(e) => setNewPassword(e.target.value)} helperText="At least 8 characters" />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setResetTarget(null)}>Cancel</Button>
          <Button variant="contained" disabled={newPassword.length < 8 || reset.isPending} onClick={() => reset.mutate()}>
            {reset.isPending ? 'Resetting…' : 'Reset Password'}
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  )
}
