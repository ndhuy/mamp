import { useQuery } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { Box, Card, CardContent, Chip, Skeleton, Stack, Typography } from '@mui/material'
import PhotoLibraryIcon from '@mui/icons-material/PhotoLibrary'
import MovieIcon from '@mui/icons-material/Movie'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import SendIcon from '@mui/icons-material/Send'
import ThumbUpIcon from '@mui/icons-material/ThumbUp'
import ImageIcon from '@mui/icons-material/Image'
import {
  Area, AreaChart, Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart,
  ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts'
import { getDashboard } from '../api/dashboard'
import type { SubmissionStatus } from '../api/types'
import { SUBMISSION_STATUS_LABELS } from '../labels'
import { useAuth } from '../auth/AuthProvider'

const SERIES = '#2563eb'
const GOOD = '#16a34a'
const BAD = '#dc2626'
const AXIS = '#94a3b8'
const GRID = '#e2e8f0'

// Reserved status colors (green=good … red=critical) — not categorical series.
const STATUS_HEX: Record<SubmissionStatus, string> = {
  NOT_SUBMITTED: '#94a3b8',
  SUBMITTED: '#2563eb',
  IN_REVIEW: '#d97706',
  ACCEPTED: '#16a34a',
  REJECTED: '#dc2626',
  RESUBMIT_REQUIRED: '#f59e0b',
  RESUBMITTED: '#0891b2',
  REMOVED: '#64748b',
}

function StatCard({ label, value, icon, color }: { label: string; value: ReactNode; icon: ReactNode; color: string }) {
  return (
    <Card sx={{ flex: '1 1 180px' }}>
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Box sx={{ width: 46, height: 46, borderRadius: 2, display: 'grid', placeItems: 'center', bgcolor: `${color}1a`, color }}>
          {icon}
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="h5">{value}</Typography>
          <Typography variant="body2" color="text.secondary" noWrap>{label}</Typography>
        </Box>
      </CardContent>
    </Card>
  )
}

function ChartCard({ title, children, empty }: { title: string; children: ReactNode; empty?: boolean }) {
  return (
    <Card sx={{ flex: '1 1 420px', minWidth: 0 }}>
      <CardContent>
        <Typography variant="subtitle2" gutterBottom>{title}</Typography>
        <Box sx={{ height: 260 }}>
          {empty
            ? <Box sx={{ height: '100%', display: 'grid', placeItems: 'center', color: 'text.secondary' }}>No data yet</Box>
            : <ResponsiveContainer width="100%" height="100%">{children as React.ReactElement}</ResponsiveContainer>}
        </Box>
      </CardContent>
    </Card>
  )
}

export function DashboardPage() {
  const { user } = useAuth()
  const { data, isLoading } = useQuery({ queryKey: ['dashboard'], queryFn: getDashboard })

  const stat = (value: number | undefined) => (isLoading ? <Skeleton width={44} /> : (value ?? 0).toLocaleString())

  const submissionsPie = (data?.submissionsByStatus ?? []).map((c) => ({
    name: SUBMISSION_STATUS_LABELS[c.label as SubmissionStatus] ?? c.label,
    value: c.value,
    hex: STATUS_HEX[c.label as SubmissionStatus] ?? SERIES,
  }))
  const byMonth = (data?.mediaByMonth ?? []).map((c) => ({ name: c.label, value: c.value }))
  const byDevice = (data?.mediaByDevice ?? []).map((c) => ({ name: c.label, value: c.value }))
  const bySite = data?.acceptedRejectedBySite ?? []

  return (
    <Stack spacing={3}>
      <Box>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
          <Typography variant="h5">Welcome back, {user?.username}</Typography>
          {data?.systemWide && <Chip size="small" color="primary" label="System-wide" />}
        </Stack>
        <Typography variant="body2" color="text.secondary">
          {data?.systemWide ? 'Metrics across all users.' : 'Metrics for your portfolio.'}
        </Typography>
      </Box>

      {/* Stat cards */}
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
        <StatCard label="Total Media" color={SERIES} icon={<ImageIcon />} value={stat(data?.totalMedia)} />
        <StatCard label="Photos" color="#0891b2" icon={<PhotoLibraryIcon />} value={stat(data?.photos)} />
        <StatCard label="Footage" color="#7c3aed" icon={<MovieIcon />} value={stat(data?.footage)} />
        <StatCard label="Ready for Upload" color="#d97706" icon={<CheckCircleIcon />} value={stat(data?.readyForUpload)} />
        <StatCard label="Submissions" color={GOOD} icon={<SendIcon />} value={stat(data?.totalSubmissions)} />
        <StatCard label="Acceptance Rate" color={GOOD} icon={<ThumbUpIcon />}
          value={isLoading ? <Skeleton width={44} /> : `${data?.acceptanceRate ?? 0}%`} />
      </Box>

      {/* Charts */}
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
        <ChartCard title="Submissions by status" empty={submissionsPie.length === 0}>
          <PieChart>
            <Pie data={submissionsPie} dataKey="value" nameKey="name" innerRadius={55} outerRadius={90} paddingAngle={2}>
              {submissionsPie.map((d) => <Cell key={d.name} fill={d.hex} stroke="#fff" strokeWidth={2} />)}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ChartCard>

        <ChartCard title="Media created by month" empty={byMonth.length === 0}>
          <AreaChart data={byMonth} margin={{ top: 8, right: 12, left: -12, bottom: 0 }}>
            <defs>
              <linearGradient id="g" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={SERIES} stopOpacity={0.35} />
                <stop offset="100%" stopColor={SERIES} stopOpacity={0.02} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke={GRID} vertical={false} />
            <XAxis dataKey="name" tick={{ fontSize: 12, fill: AXIS }} tickLine={false} axisLine={{ stroke: GRID }} />
            <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: AXIS }} tickLine={false} axisLine={false} width={32} />
            <Tooltip />
            <Area type="monotone" dataKey="value" name="Media" stroke={SERIES} strokeWidth={2} fill="url(#g)" />
          </AreaChart>
        </ChartCard>

        <ChartCard title="Accepted vs rejected by site" empty={bySite.length === 0}>
          <BarChart data={bySite} margin={{ top: 8, right: 12, left: -12, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke={GRID} vertical={false} />
            <XAxis dataKey="site" tick={{ fontSize: 12, fill: AXIS }} tickLine={false} axisLine={{ stroke: GRID }} />
            <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: AXIS }} tickLine={false} axisLine={false} width={32} />
            <Tooltip />
            <Legend />
            <Bar dataKey="accepted" name="Accepted" fill={GOOD} radius={[4, 4, 0, 0]} />
            <Bar dataKey="rejected" name="Rejected" fill={BAD} radius={[4, 4, 0, 0]} />
          </BarChart>
        </ChartCard>

        <ChartCard title="Media by capture device" empty={byDevice.length === 0}>
          <BarChart data={byDevice} layout="vertical" margin={{ top: 8, right: 16, left: 8, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke={GRID} horizontal={false} />
            <XAxis type="number" allowDecimals={false} tick={{ fontSize: 12, fill: AXIS }} tickLine={false} axisLine={{ stroke: GRID }} />
            <YAxis type="category" dataKey="name" width={130} tick={{ fontSize: 12, fill: AXIS }} tickLine={false} axisLine={false} />
            <Tooltip />
            <Bar dataKey="value" name="Media" fill={SERIES} radius={[0, 4, 4, 0]} />
          </BarChart>
        </ChartCard>
      </Box>
    </Stack>
  )
}
