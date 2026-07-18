import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { DashboardPage } from './pages/DashboardPage'
import { MediaListPage } from './pages/MediaListPage'
import { MediaDetailPage } from './pages/MediaDetailPage'
import { MediaFormPage } from './pages/MediaFormPage'
import { StockSitesPage } from './pages/StockSitesPage'
import { DevicesPage } from './pages/DevicesPage'
import { LensesPage } from './pages/LensesPage'
import { RejectionCategoriesPage } from './pages/RejectionCategoriesPage'
import { UsersPage } from './pages/UsersPage'
import { useAuth } from './auth/AuthProvider'

export default function App() {
  const { isAuthenticated } = useAuth()

  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/register" element={isAuthenticated ? <Navigate to="/" replace /> : <RegisterPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/media" element={<MediaListPage />} />
          <Route path="/media/new" element={<MediaFormPage />} />
          <Route path="/media/:id" element={<MediaDetailPage />} />
          <Route path="/media/:id/edit" element={<MediaFormPage />} />
          <Route path="/stock-sites" element={<StockSitesPage />} />
          <Route path="/devices" element={<DevicesPage />} />
          <Route path="/lenses" element={<LensesPage />} />
          <Route path="/rejection-categories" element={<RejectionCategoriesPage />} />
          <Route path="/users" element={<UsersPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
