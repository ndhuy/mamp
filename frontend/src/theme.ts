import { createTheme } from '@mui/material/styles'

// Palette tuned to the mockups: deep-navy sidebar, blue accent, light content area.
export const SIDEBAR_BG = '#0f2745'
export const SIDEBAR_BG_HOVER = '#1b3a63'
export const SIDEBAR_ACTIVE = '#2563eb'

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#2563eb' },
    secondary: { main: '#7c3aed' },
    success: { main: '#16a34a' },
    warning: { main: '#d97706' },
    error: { main: '#dc2626' },
    background: { default: '#f1f5f9', paper: '#ffffff' },
    text: { primary: '#0f172a', secondary: '#64748b' },
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: '"Inter", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
    h5: { fontWeight: 700 },
    h6: { fontWeight: 700 },
    subtitle2: { fontWeight: 600 },
  },
  components: {
    MuiPaper: { styleOverrides: { root: { backgroundImage: 'none' } } },
    MuiButton: { defaultProps: { disableElevation: true }, styleOverrides: { root: { textTransform: 'none', fontWeight: 600 } } },
    MuiCard: { styleOverrides: { root: { border: '1px solid #e2e8f0' } } },
  },
})
