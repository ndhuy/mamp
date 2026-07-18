import { useState, type ReactNode } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import {
  AppBar, Avatar, Box, Divider, Drawer, IconButton, List, ListItemButton, ListItemIcon,
  ListItemText, Menu, MenuItem, Toolbar, Typography,
} from '@mui/material'
import DashboardIcon from '@mui/icons-material/Dashboard'
import PhotoLibraryIcon from '@mui/icons-material/PhotoLibrary'
import StorefrontIcon from '@mui/icons-material/Storefront'
import CameraAltIcon from '@mui/icons-material/CameraAlt'
import CameraIcon from '@mui/icons-material/Camera'
import BlockIcon from '@mui/icons-material/Block'
import PeopleAltIcon from '@mui/icons-material/PeopleAlt'
import LogoutIcon from '@mui/icons-material/Logout'
import { useAuth } from '../auth/AuthProvider'
import { SIDEBAR_ACTIVE, SIDEBAR_BG, SIDEBAR_BG_HOVER } from '../theme'

const DRAWER_WIDTH = 248

interface NavItem {
  label: string
  to: string
  icon: ReactNode
  adminOnly?: boolean
}

const NAV: NavItem[] = [
  { label: 'Dashboard', to: '/', icon: <DashboardIcon /> },
  { label: 'Media', to: '/media', icon: <PhotoLibraryIcon /> },
  { label: 'Stock Sites', to: '/stock-sites', icon: <StorefrontIcon /> },
  { label: 'Camera / Devices', to: '/devices', icon: <CameraAltIcon /> },
  { label: 'Lenses', to: '/lenses', icon: <CameraIcon /> },
  { label: 'Rejection Categories', to: '/rejection-categories', icon: <BlockIcon /> },
  { label: 'Users', to: '/users', icon: <PeopleAltIcon />, adminOnly: true },
]

export function AppLayout() {
  const { user, isAdmin, logout } = useAuth()
  const location = useLocation()
  const [anchor, setAnchor] = useState<null | HTMLElement>(null)

  const current = NAV.find((n) => (n.to === '/' ? location.pathname === '/' : location.pathname.startsWith(n.to)))

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <Drawer
        variant="permanent"
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
            bgcolor: SIDEBAR_BG,
            color: '#cbd5e1',
            border: 'none',
          },
        }}
      >
        <Box sx={{ px: 2.5, py: 2.5 }}>
          <Typography variant="h6" sx={{ color: '#fff', lineHeight: 1.2 }}>
            Stock Media
          </Typography>
          <Typography variant="body2" sx={{ color: '#94a3b8' }}>
            Manager
          </Typography>
        </Box>
        <Divider sx={{ borderColor: '#1e3a5f' }} />
        <List sx={{ px: 1.5, py: 1 }}>
          {NAV.filter((n) => !n.adminOnly || isAdmin).map((item) => (
            <ListItemButton
              key={item.to}
              component={NavLink}
              to={item.to}
              end={item.to === '/'}
              sx={{
                borderRadius: 2,
                mb: 0.5,
                color: '#cbd5e1',
                '& .MuiListItemIcon-root': { color: '#94a3b8', minWidth: 40 },
                '&:hover': { bgcolor: SIDEBAR_BG_HOVER },
                '&.active': {
                  bgcolor: SIDEBAR_ACTIVE,
                  color: '#fff',
                  '& .MuiListItemIcon-root': { color: '#fff' },
                },
              }}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} slotProps={{ primary: { sx: { fontSize: 14, fontWeight: 600 } } }} />
            </ListItemButton>
          ))}
        </List>
        <Box sx={{ mt: 'auto', p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Avatar sx={{ width: 36, height: 36, bgcolor: SIDEBAR_ACTIVE }}>
            {user?.username?.[0]?.toUpperCase() ?? '?'}
          </Avatar>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="body2" noWrap sx={{ color: '#fff', fontWeight: 600 }}>
              {user?.username}
            </Typography>
            <Typography variant="caption" sx={{ color: '#94a3b8' }}>
              {isAdmin ? 'Administrator' : 'Contributor'}
            </Typography>
          </Box>
        </Box>
      </Drawer>

      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <AppBar
          position="sticky"
          elevation={0}
          sx={{ bgcolor: '#fff', color: 'text.primary', borderBottom: '1px solid #e2e8f0' }}
        >
          <Toolbar>
            <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700 }}>
              {current?.label ?? 'Dashboard'}
            </Typography>
            <IconButton onClick={(e) => setAnchor(e.currentTarget)} size="small">
              <Avatar sx={{ width: 34, height: 34, bgcolor: 'primary.main' }}>
                {user?.username?.[0]?.toUpperCase() ?? '?'}
              </Avatar>
            </IconButton>
            <Menu anchorEl={anchor} open={!!anchor} onClose={() => setAnchor(null)}>
              <MenuItem disabled sx={{ opacity: 1 }}>
                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>{user?.username}</Typography>
                  <Typography variant="caption" color="text.secondary">{user?.email}</Typography>
                </Box>
              </MenuItem>
              <Divider />
              <MenuItem onClick={() => { setAnchor(null); logout() }}>
                <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
                Sign out
              </MenuItem>
            </Menu>
          </Toolbar>
        </AppBar>

        <Box component="main" sx={{ p: 3, flexGrow: 1 }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  )
}
