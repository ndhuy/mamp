import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createColumnHelper, flexRender, getCoreRowModel, useReactTable,
} from '@tanstack/react-table'
import {
  Avatar, Box, Button, Card, CardActionArea, CardContent, Chip, FormControlLabel, IconButton,
  InputAdornment, MenuItem, Paper, Stack, Switch, Table, TableBody, TableCell, TableContainer,
  TableHead, TablePagination, TableRow, TextField, ToggleButton, ToggleButtonGroup, Tooltip, Typography,
} from '@mui/material'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import AddIcon from '@mui/icons-material/Add'
import SearchIcon from '@mui/icons-material/Search'
import FileDownloadOutlinedIcon from '@mui/icons-material/FileDownloadOutlined'
import ViewListIcon from '@mui/icons-material/ViewList'
import GridViewIcon from '@mui/icons-material/GridView'
import ImageIcon from '@mui/icons-material/Image'
import { deleteMedia, listMedia, type MediaFilters } from '../api/media'
import { exportMediaXlsx } from '../api/export'
import { thumbnailUrl } from '../config'
import type { MediaSummary } from '../api/types'
import { WORKFLOW_STATUS_COLORS, WORKFLOW_STATUS_LABELS, WORKFLOW_STATUS_OPTIONS } from '../labels'

const columnHelper = createColumnHelper<MediaSummary>()

const SMART_FILTERS = [
  { value: 'READY', label: 'Ready for Upload' },
  { value: 'MISSING_THUMBNAIL', label: 'Missing thumbnail' },
  { value: 'MISSING_KEYWORDS', label: 'Missing keywords' },
  { value: 'NO_TARGET_SITE', label: 'No target site' },
  { value: 'NOT_SUBMITTED_ANY', label: 'Not submitted anywhere' },
  { value: 'IN_REVIEW', label: 'Currently in review' },
  { value: 'ACCEPTED_ANY', label: 'Accepted on a site' },
  { value: 'REJECTED_ANY', label: 'Rejected on a site' },
]

export function MediaListPage() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [searchInput, setSearchInput] = useState('')
  const [filters, setFilters] = useState<MediaFilters>({})
  const [exporting, setExporting] = useState(false)
  const [view, setView] = useState<'table' | 'grid'>(
    () => (localStorage.getItem('msamp.mediaView') === 'grid' ? 'grid' : 'table'),
  )

  const changeView = (next: 'table' | 'grid' | null) => {
    if (!next) return
    setView(next)
    localStorage.setItem('msamp.mediaView', next)
  }

  const handleExport = async () => {
    setExporting(true)
    try {
      await exportMediaXlsx(filters)
    } finally {
      setExporting(false)
    }
  }

  // Debounce the search box into the filter set.
  useEffect(() => {
    const t = setTimeout(() => {
      setFilters((f) => ({ ...f, q: searchInput }))
      setPage(0)
    }, 350)
    return () => clearTimeout(t)
  }, [searchInput])

  const setFilter = (key: keyof MediaFilters, value: string | boolean) => {
    setFilters((f) => ({ ...f, [key]: value }))
    setPage(0)
  }

  const clearFilters = () => {
    setSearchInput('')
    setFilters({})
    setPage(0)
  }

  const hasActiveFilters =
    !!searchInput || !!filters.mediaType || !!filters.workflowStatus || !!filters.smart || !!filters.deleted

  const { data, isLoading, isError } = useQuery({
    queryKey: ['media', 'list', page, size, filters],
    queryFn: () => listMedia({ page, size, sort: 'createdAt,desc', ...filters }),
  })

  const remove = useMutation({
    mutationFn: (id: string) => deleteMedia(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['media'] }),
  })

  const columns = useMemo(
    () => [
      columnHelper.accessor('code', { header: 'ID', cell: (c) => <Typography variant="body2" sx={{ fontWeight: 600 }}>{c.getValue()}</Typography> }),
      columnHelper.display({
        id: 'thumb',
        header: '',
        cell: (c) => (
          <Avatar variant="rounded" src={thumbnailUrl(c.row.original.thumbnailKey)} sx={{ bgcolor: '#e2e8f0', color: '#94a3b8' }}>
            <ImageIcon />
          </Avatar>
        ),
      }),
      columnHelper.accessor('title', { header: 'Title' }),
      columnHelper.accessor('mediaType', {
        header: 'Type',
        cell: (c) => <Chip size="small" variant="outlined" label={c.getValue() === 'PHOTO' ? 'Photo' : 'Footage'} />,
      }),
      columnHelper.accessor('workflowStatus', {
        header: 'Status',
        cell: (c) => (
          <Chip size="small" color={WORKFLOW_STATUS_COLORS[c.getValue()]} label={WORKFLOW_STATUS_LABELS[c.getValue()]} />
        ),
      }),
      columnHelper.accessor('keywordCount', { header: 'Keywords', cell: (c) => c.getValue() }),
      columnHelper.accessor('createdAt', {
        header: 'Created',
        cell: (c) => new Date(c.getValue()).toLocaleDateString(),
      }),
      columnHelper.display({
        id: 'actions',
        header: '',
        cell: (c) => (
          <Stack direction="row" spacing={0.5} sx={{ justifyContent: 'flex-end' }}>
            <Tooltip title="Edit">
              <IconButton size="small" onClick={(e) => { e.stopPropagation(); navigate(`/media/${c.row.original.id}/edit`) }}>
                <EditOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Delete">
              <IconButton size="small" color="error" onClick={(e) => { e.stopPropagation(); remove.mutate(c.row.original.id) }}>
                <DeleteOutlineIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Stack>
        ),
      }),
    ],
    [remove, navigate],
  )

  const table = useReactTable({
    data: data?.content ?? [],
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
  })

  return (
    <Stack spacing={2}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h6">Media Library</Typography>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" startIcon={<FileDownloadOutlinedIcon />} onClick={handleExport} disabled={exporting}>
            {exporting ? 'Exporting…' : 'Export to Excel'}
          </Button>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/media/new')}>
            Add Media
          </Button>
        </Stack>
      </Box>

      <Paper variant="outlined" sx={{ p: 2 }}>
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, alignItems: 'center' }}>
          <TextField
            size="small"
            placeholder="Search title, ID, description, keyword…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            sx={{ minWidth: 280, flex: '1 1 280px' }}
            slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> } }}
          />
          <TextField select size="small" label="Type" value={filters.mediaType ?? ''}
            onChange={(e) => setFilter('mediaType', e.target.value)} sx={{ minWidth: 130 }}>
            <MenuItem value="">All</MenuItem>
            <MenuItem value="PHOTO">Photo</MenuItem>
            <MenuItem value="FOOTAGE">Footage</MenuItem>
          </TextField>
          <TextField select size="small" label="Status" value={filters.workflowStatus ?? ''}
            onChange={(e) => setFilter('workflowStatus', e.target.value)} sx={{ minWidth: 170 }}>
            <MenuItem value="">All statuses</MenuItem>
            {WORKFLOW_STATUS_OPTIONS.map((s) => <MenuItem key={s} value={s}>{WORKFLOW_STATUS_LABELS[s]}</MenuItem>)}
          </TextField>
          <TextField select size="small" label="Smart filter" value={filters.smart ?? ''}
            onChange={(e) => setFilter('smart', e.target.value)} sx={{ minWidth: 200 }}>
            <MenuItem value="">None</MenuItem>
            {SMART_FILTERS.map((s) => <MenuItem key={s.value} value={s.value}>{s.label}</MenuItem>)}
          </TextField>
          <FormControlLabel
            control={<Switch checked={!!filters.deleted} onChange={(e) => setFilter('deleted', e.target.checked)} />}
            label="Deleted"
          />
          {hasActiveFilters && <Button size="small" onClick={clearFilters}>Clear</Button>}
          <ToggleButtonGroup size="small" exclusive value={view} onChange={(_, v) => changeView(v)} sx={{ ml: 'auto' }}>
            <ToggleButton value="table" aria-label="table view"><ViewListIcon fontSize="small" /></ToggleButton>
            <ToggleButton value="grid" aria-label="grid view"><GridViewIcon fontSize="small" /></ToggleButton>
          </ToggleButtonGroup>
        </Box>
      </Paper>

      {view === 'table' && (
      <Paper variant="outlined">
        <TableContainer>
          <Table>
            <TableHead>
              {table.getHeaderGroups().map((hg) => (
                <TableRow key={hg.id} sx={{ '& th': { bgcolor: '#f8fafc', fontWeight: 700 } }}>
                  {hg.headers.map((h) => (
                    <TableCell key={h.id}>
                      {flexRender(h.column.columnDef.header, h.getContext())}
                    </TableCell>
                  ))}
                </TableRow>
              ))}
            </TableHead>
            <TableBody>
              {isLoading && (
                <TableRow><TableCell colSpan={columns.length} align="center" sx={{ py: 6, color: 'text.secondary' }}>Loading…</TableCell></TableRow>
              )}
              {isError && (
                <TableRow><TableCell colSpan={columns.length} align="center" sx={{ py: 6, color: 'error.main' }}>Failed to load media</TableCell></TableRow>
              )}
              {!isLoading && !isError && table.getRowModel().rows.length === 0 && (
                <TableRow>
                  <TableCell colSpan={columns.length} align="center" sx={{ py: 8 }}>
                    <Box sx={{ color: 'text.secondary' }}>
                      <Typography variant="body1" sx={{ fontWeight: 600 }}>No media yet</Typography>
                      <Typography variant="body2">Media assets you create will appear here.</Typography>
                    </Box>
                  </TableCell>
                </TableRow>
              )}
              {table.getRowModel().rows.map((row) => (
                <TableRow
                  key={row.id}
                  hover
                  sx={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/media/${row.original.id}`)}
                >
                  {row.getVisibleCells().map((cell) => (
                    <TableCell key={cell.id}>{flexRender(cell.column.columnDef.cell, cell.getContext())}</TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
      )}

      {view === 'grid' && (
        <Box>
          {isLoading && <Typography sx={{ py: 6, textAlign: 'center', color: 'text.secondary' }}>Loading…</Typography>}
          {isError && <Typography color="error" sx={{ py: 6, textAlign: 'center' }}>Failed to load media</Typography>}
          {!isLoading && !isError && (data?.content.length ?? 0) === 0 && (
            <Paper variant="outlined" sx={{ py: 8, textAlign: 'center', color: 'text.secondary' }}>
              <Typography variant="body1" sx={{ fontWeight: 600 }}>No media yet</Typography>
              <Typography variant="body2">Media assets you create will appear here.</Typography>
            </Paper>
          )}
          <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 2 }}>
            {data?.content.map((m) => (
              <Card key={m.id} variant="outlined">
                <CardActionArea onClick={() => navigate(`/media/${m.id}`)}>
                  <Box sx={{ height: 140, bgcolor: '#f1f5f9', display: 'grid', placeItems: 'center', color: '#94a3b8', overflow: 'hidden' }}>
                    {m.thumbnailKey
                      ? <Box component="img" src={thumbnailUrl(m.thumbnailKey)} sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      : <ImageIcon fontSize="large" />}
                  </Box>
                  <CardContent sx={{ pb: 1 }}>
                    <Typography variant="caption" color="text.secondary">{m.code}</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 600 }} noWrap>{m.title}</Typography>
                    <Stack direction="row" spacing={0.5} sx={{ mt: 1, flexWrap: 'wrap', gap: 0.5 }}>
                      <Chip size="small" variant="outlined" label={m.mediaType === 'PHOTO' ? 'Photo' : 'Footage'} />
                      <Chip size="small" color={WORKFLOW_STATUS_COLORS[m.workflowStatus]} label={WORKFLOW_STATUS_LABELS[m.workflowStatus]} />
                    </Stack>
                  </CardContent>
                </CardActionArea>
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', px: 1, pb: 1 }}>
                  <Tooltip title="Edit">
                    <IconButton size="small" onClick={() => navigate(`/media/${m.id}/edit`)}><EditOutlinedIcon fontSize="small" /></IconButton>
                  </Tooltip>
                  <Tooltip title="Delete">
                    <IconButton size="small" color="error" onClick={() => remove.mutate(m.id)}><DeleteOutlineIcon fontSize="small" /></IconButton>
                  </Tooltip>
                </Box>
              </Card>
            ))}
          </Box>
        </Box>
      )}

      <Paper variant="outlined">
        <TablePagination
          component="div"
          count={data?.totalElements ?? 0}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={size}
          onRowsPerPageChange={(e) => { setSize(parseInt(e.target.value, 10)); setPage(0) }}
          rowsPerPageOptions={[20, 50, 100]}
        />
      </Paper>
    </Stack>
  )
}
