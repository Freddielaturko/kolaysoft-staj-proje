import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { ArrowUpRight } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { errorMessage } from '../utils/format'

// Canli demoda (VITE_DEMO_MODE=true) rol butonlari gorunuyor ve form ornek hesapla dolu geliyor.
// Gercek kurulumda bu blok tamamen kapali kaliyor.
const DEMO = import.meta.env.VITE_DEMO_MODE === 'true'
const DEMO_PASSWORD = import.meta.env.VITE_DEMO_PASSWORD || 'demo123'
const DEMO_ROLES = [
  ['cto', 'CTO'],
  ['pm', 'Proje Yöneticisi'],
  ['lead', 'Ekip Lideri'],
  ['admin', 'Admin'],
]

export default function Login() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState(DEMO ? 'cto@kolaysoft.demo' : '')
  const [sifre, setSifre] = useState(DEMO ? DEMO_PASSWORD : '')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/" replace />

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(email, sifre)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err.response ? errorMessage(err, 'Giriş yapılamadı') : 'Sunucuya ulaşılamadı. Birkaç saniye sonra tekrar deneyin.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-shell">
      <section className="login-art">
        <div className="brand-mark">K</div>
        <p className="eyebrow">PROJE OPERASYONLARI / {new Date().getFullYear()}</p>
        <h1>
          Projelerin gerçek
          <br />
          <span>ritmini görün.</span>
        </h1>
        <p className="login-copy">
          Haftalık raporları tek bir akışta toplayın, riskleri ve gecikmeleri karar vermeniz gereken anda görün.
        </p>
        <div className="login-stat">
          <strong>4</strong>
          <span>rol, tek görünüm<br />CTO, PM, ekip lideri ve admin</span>
        </div>
      </section>

      <section className="login-card">
        <div className="mobile-brand">
          <div className="brand-mark">K</div>
          <span>Kolaysoft Ops</span>
        </div>
        <p className="eyebrow">HOŞ GELDİNİZ</p>
        <h2>Çalışma alanınıza giriş yapın</h2>
        <p className="muted">{DEMO ? 'Demo hesabınızla devam edin.' : 'Hesabınız yöneticiniz tarafından oluşturulur.'}</p>

        <form onSubmit={handleSubmit}>
          <label>
            E-posta
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="username" required />
          </label>
          <label>
            Şifre
            <input type="password" value={sifre} onChange={(e) => setSifre(e.target.value)} autoComplete="current-password" required />
          </label>
          {error && <div className="form-error">{error}</div>}
          <button className="primary-button wide" disabled={submitting}>
            {submitting ? 'Giriş yapılıyor...' : 'Giriş yap'} <ArrowUpRight size={17} />
          </button>
        </form>

        {DEMO && (
          <div className="demo-roles">
            <span>Demo rolü seçin</span>
            <div>
              {DEMO_ROLES.map(([key, label]) => {
                const demoEmail = `${key}@kolaysoft.demo`
                return (
                  <button
                    key={key}
                    type="button"
                    className={email === demoEmail ? 'selected' : ''}
                    onClick={() => {
                      setEmail(demoEmail)
                      setSifre(DEMO_PASSWORD)
                    }}
                  >
                    {label}
                  </button>
                )
              })}
            </div>
          </div>
        )}
      </section>
    </main>
  )
}
