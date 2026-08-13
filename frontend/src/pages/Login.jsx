import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import './Login.css'

const ROLE_HOME = {
  ADMIN: '/admin',
  PM: '/pm',
  CTO: '/cto',
}

export default function Login() {
  const [email, setEmail] = useState('')
  const [sifre, setSifre] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await login(email, sifre)
      navigate(ROLE_HOME[user.rol] || '/')
    } catch (err) {
      setError(err.response?.data?.error || 'Giris basarisiz. Bilgilerinizi kontrol edin.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-panel">
        <div className="login-brand">
          <div className="login-brand-mark">PD</div>
          <div>
            <h1>Proje Durum Sistemi</h1>
            <p>Haftalık raporlama ve CTO takip paneli</p>
          </div>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="ad.soyad@kolaysoft.com.tr"
              required
              autoFocus
            />
          </label>

          <label>
            Şifre
            <input
              type="password"
              value={sifre}
              onChange={(e) => setSifre(e.target.value)}
              placeholder="••••••••"
              required
            />
          </label>

          {error && <div className="login-error">{error}</div>}

          <button type="submit" disabled={loading}>
            {loading ? 'Giriş yapılıyor…' : 'Giriş Yap'}
          </button>
        </form>
      </div>
    </div>
  )
}
