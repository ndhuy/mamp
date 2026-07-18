import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Box, Button, Chip, IconButton, Link, Paper, Stack, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Tooltip, Typography,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import CategoryOutlinedIcon from '@mui/icons-material/CategoryOutlined'
import ToggleOnIcon from '@mui/icons-material/ToggleOn'
import ToggleOffIcon from '@mui/icons-material/ToggleOff'
import { listStockSites, setStockSiteActive } from '../api/masterdata'
import type { StockSite } from '../api/types'
import { useAuth } from '../auth/AuthProvider'
import { StockSiteFormDialog } from '../components/StockSiteFormDialog'
import { SiteCategoriesDialog } from '../components/SiteCategoriesDialog'

export function StockSitesPage() {
  const { isAdmin } = useAuth()
  const queryClient = useQueryClient()
  const { data, isLoading } = useQuery({ queryKey: ['stock-sites', 'all'], queryFn: () => listStockSites(isAdmin) })
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<StockSite | null>(null)
  const [categoriesSite, setCategoriesSite] = useState<StockSite | null>(null)

  const toggle = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => setStockSiteActive(id, active),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['stock-sites'] }),
  })

  return (
    <Stack spacing={2}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h6">Stock Sites</Typography>
        {isAdmin && <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditing(null); setDialogOpen(true) }}>Add Site</Button>}
      </Box>
      <Paper variant="outlined">
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ '& th': { bgcolor: '#f8fafc', fontWeight: 700 } }}>
                <TableCell>Name</TableCell>
                <TableCell>Website</TableCell>
                <TableCell align="center">Categories Required</TableCell>
                <TableCell align="center">Status</TableCell>
                {isAdmin && <TableCell align="right">Actions</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading && <TableRow><TableCell colSpan={5} align="center" sx={{ py: 4 }}>Loading…</TableCell></TableRow>}
              {data?.map((site) => (
                <TableRow key={site.id} hover>
                  <TableCell><Typography sx={{ fontWeight: 600 }}>{site.name}</Typography></TableCell>
                  <TableCell>
                    {site.website ? <Link href={site.website} target="_blank" rel="noreferrer">{site.website}</Link> : '—'}
                  </TableCell>
                  <TableCell align="center">{site.categoriesRequired}</TableCell>
                  <TableCell align="center">
                    <Chip size="small" color={site.active ? 'success' : 'default'} label={site.active ? 'Active' : 'Inactive'} />
                  </TableCell>
                  {isAdmin && (
                    <TableCell align="right">
                      <Tooltip title="Categories">
                        <IconButton size="small" onClick={() => setCategoriesSite(site)}><CategoryOutlinedIcon fontSize="small" /></IconButton>
                      </Tooltip>
                      <Tooltip title="Edit">
                        <IconButton size="small" onClick={() => { setEditing(site); setDialogOpen(true) }}><EditOutlinedIcon fontSize="small" /></IconButton>
                      </Tooltip>
                      <Tooltip title={site.active ? 'Deactivate' : 'Activate'}>
                        <IconButton size="small" onClick={() => toggle.mutate({ id: site.id, active: !site.active })}>
                          {site.active ? <ToggleOnIcon color="success" /> : <ToggleOffIcon />}
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
      <StockSiteFormDialog open={dialogOpen} site={editing} onClose={() => setDialogOpen(false)} />
      <SiteCategoriesDialog site={categoriesSite} onClose={() => setCategoriesSite(null)} />
    </Stack>
  )
}
