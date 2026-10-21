import { useEffect, useRef, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
  AlertTriangle,
  Bell,
  ChevronRight,
  CircleUserRound,
  ClipboardList,
  FolderKanban,
  LayoutDashboard,
  LogOut,
  Menu,
  Users,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { usePortfolio } from '../context/PortfolioContext'
import { ROLE_LABELS, initials } from '../utils/format'

function pageTitle(pathname) {
  if (/^\/projeler\/\d+/.test(pathname)) return 'Proje detayı'
  if (pathname.startsWith('/projeler')) return 'Projeler'
  if (pathname.startsWith('/raporlar')) return 'Haftalık raporlar'
  if (pathname.startsWith('/kullanicilar')) return 'Kullanıcılar'
  return 'Dashboard'
}

export default function AppLayout() {
  const { user, logout } = useAuth()
  const { projects, alerts } = usePortfolio()
  const location = useLocation()
  const navigate = useNavigate()
  const [mobileOpen, setMobileOpen] = useState(false)
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const notificationsRef = useRef(null)

  const lateReports = projects.filter((p) => p.raporGecikti).length
  const title = pageTitle(location.pathname)

  const items = [
    ['/', 'Dashboard', LayoutDashboard],
    ['/projeler', 'Projeler', FolderKanban],
    ['/raporlar', 'Haftalık Raporlar', ClipboardList, lateReports],
  ]
  if (user.rol === 'ADMIN') items.push(['/kullanicilar', 'Kullanıcılar', Users])

  useEffect(() => {
    setMobileOpen(false)
    setNotificationsOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!notificationsOpen) return
    const onClick = (e) => {
      if (!notificationsRef.current?.contains(e.target)) setNotificationsOpen(false)
    }
    document.addEventListener('mousedown', onClick)
    return () => document.removeEventListener('mousedown', onClick)
  }, [notificationsOpen])

  return (
    <div className="app-shell">
      <div className={mobileOpen ? 'sidebar-overlay open' : 'sidebar-overlay'} onClick={() => setMobileOpen(false)} />
      <aside className={mobileOpen ? 'sidebar open' : 'sidebar'}>
        <div className="side-brand">
          <div className="brand-mark small">K</div>
          <div>
            <strong>Kolaysoft</strong>
            <small>Proje Durum Sistemi</small>
          </div>
        </div>
        <nav>
          {items.map(([to, label, Icon, count]) => (
            <NavLink key={to} to={to} end={to === '/'} className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
              <Icon size={18} />
              <span>{label}</span>
              {count > 0 && <b title="Raporu geciken proje">{count}</b>}
            </NavLink>
          ))}
        </nav>
        <div className="side-bottom">
          <div className="user-mini">
            <div className="avatar">{initials(user.adSoyad)}</div>
            <div>
              <strong>{user.adSoyad}</strong>
              <small>{ROLE_LABELS[user.rol]}</small>
            </div>
          </div>
          <button className="logout" onClick={logout}>
            <LogOut size={16} /> Çıkış yap
          </button>
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <button className="mobile-menu" onClick={() => setMobileOpen(!mobileOpen)} aria-label="Menü">
            <Menu size={20} />
          </button>
          <div>
            <p className="breadcrumb">ÇALIŞMA ALANI / <strong>{title.toLocaleUpperCase('tr-TR')}</strong></p>
            <h1>{title}</h1>
          </div>
          <div className="top-actions" ref={notificationsRef}>
            <button className="icon-button" onClick={() => setNotificationsOpen(!notificationsOpen)} aria-label="Bildirimler">
              <Bell size={19} />
              {alerts.length > 0 && <i></i>}
            </button>
            <div className="top-user">
              <CircleUserRound size={20} />
              <span>{user.adSoyad}</span>
            </div>

            {notificationsOpen && (
              <div className="notifications">
                <p className="eyebrow red">BİLDİRİMLER / {alerts.length}</p>
                {alerts.length === 0 && <div className="empty">Aksiyon gerektiren bir durum yok.</div>}
                {alerts.slice(0, 6).map((a) => (
                  <button key={a.id} className="alert-row" onClick={() => navigate(`/projeler/${a.projectId}`)}>
                    <span className={`alert-icon ${a.seviye === 'KRITIK' ? 'critical' : 'attention'}`}>
                      <AlertTriangle size={15} />
                    </span>
                    <span>
                      <strong>{a.projectAd}</strong>
                      <small>{a.mesaj}</small>
                    </span>
                    <ChevronRight size={15} />
                  </button>
                ))}
              </div>
            )}
          </div>
        </header>
        <div className="content">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
