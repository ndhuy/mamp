import { useEffect, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField } from '@mui/material'
import { createRejectionCategory, updateRejectionCategory } from '../api/masterdata'
import { apiErrorMessage } from '../api/client'
import type { RejectionCategory } from '../api/types'

interface Props {
  open: boolean
  category: RejectionCategory | null
  onClose: () => void
}

export function RejectionCategoryFormDialog({ open, category, onClose }: Props) {
  const queryClient = useQueryClient()
  const [name, setName] = useState('')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setError(null)
    setName(category?.name ?? '')
  }, [open, category])

  const save = useMutation({
    mutationFn: () => {
      const payload = { name: name.trim() }
      return category ? updateRejectionCategory(category.id, payload) : createRejectionCategory(payload)
    },
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['rejection-categories'] }); onClose() },
    onError: (e) => setError(apiErrorMessage(e, 'Could not save rejection category')),
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{category ? 'Edit Rejection Category' : 'Add Rejection Category'}</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2.5} sx={{ pt: 0.5 }}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField label="Name *" value={name} onChange={(e) => setName(e.target.value)} fullWidth autoFocus />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" disabled={!name.trim() || save.isPending} onClick={() => { setError(null); save.mutate() }}>
          {save.isPending ? 'Saving…' : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
