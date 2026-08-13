import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import './AppLayout.css'

export default function AppLayout({ title, children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-layout">
      <header className="app-header">
        <div className="app-header-title">{title}</div>
        <div className="app-header-user">
          <span>{user?.adSoyad} <span className="app-header-role">{user?.rol}</span></span>
          <button onClick={handleLogout}>Çıkış Yap</button>
        </div>
      </header>
      <main className="app-main">{children}</main>
    </div>
  )
}
