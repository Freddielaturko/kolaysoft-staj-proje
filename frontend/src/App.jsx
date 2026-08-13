import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import AdminHome from './pages/AdminHome'
import PmHome from './pages/PmHome'
import CtoHome from './pages/CtoHome'

const ROLE_HOME = {
  ADMIN: '/admin',
  PM: '/pm',
  CTO: '/cto',
}

function RootRedirect() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={ROLE_HOME[user.rol] || '/login'} replace />
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<RootRedirect />} />

      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/pm"
        element={
          <ProtectedRoute allowedRoles={['PM']}>
            <PmHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/cto"
        element={
          <ProtectedRoute allowedRoles={['CTO']}>
            <CtoHome />
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}

export default App
