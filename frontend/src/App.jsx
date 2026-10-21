import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import AppLayout from './components/AppLayout'
import { PortfolioProvider } from './context/PortfolioContext'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Projects from './pages/Projects'
import ProjectDetail from './pages/ProjectDetail'
import Reports from './pages/Reports'
import Users from './pages/Users'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        element={
          <ProtectedRoute>
            <PortfolioProvider>
              <AppLayout />
            </PortfolioProvider>
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="projeler" element={<Projects />} />
        <Route path="projeler/:id" element={<ProjectDetail />} />
        <Route path="raporlar" element={<Reports />} />
        <Route
          path="kullanicilar"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <Users />
            </ProtectedRoute>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
