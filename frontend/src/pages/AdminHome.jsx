import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { listUsers, createUser } from '../api/users'
import { listProjects, createProject } from '../api/projects'
import './AdminHome.css'

const ROLES = ['ADMIN', 'PM', 'CTO']
const PROJECT_STATUSES = ['AKTIF', 'BEKLEMEDE', 'TAMAMLANDI', 'IPTAL']

export default function AdminHome() {
  const [users, setUsers] = useState([])
  const [projects, setProjects] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadAll()
  }, [])

  async function loadAll() {
    setLoading(true)
    setError('')
    try {
      const [usersData, projectsData] = await Promise.all([listUsers(), listProjects()])
      setUsers(usersData)
      setProjects(projectsData)
    } catch (err) {
      setError('Veriler yüklenemedi. Backend çalışıyor mu kontrol edin.')
    } finally {
      setLoading(false)
    }
  }

  const pmUsers = users.filter((u) => u.rol === 'PM')

  return (
    <AppLayout title="Admin Paneli">
      {error && <div className="admin-error">{error}</div>}

      <div className="admin-grid">
        <UserSection users={users} loading={loading} onCreated={loadAll} />
        <ProjectSection projects={projects} pmUsers={pmUsers} loading={loading} onCreated={loadAll} />
      </div>
    </AppLayout>
  )
}

function UserSection({ users, loading, onCreated }) {
  const [form, setForm] = useState({ adSoyad: '', email: '', sifre: '', rol: 'PM' })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setFormError('')
    setSubmitting(true)
    try {
      await createUser(form)
      setForm({ adSoyad: '', email: '', sifre: '', rol: 'PM' })
      onCreated()
    } catch (err) {
      setFormError(err.response?.data?.error || 'Kullanıcı oluşturulamadı.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="admin-card">
      <h2>Kullanıcılar</h2>

      <form className="admin-form" onSubmit={handleSubmit}>
        <input
          placeholder="Ad Soyad"
          value={form.adSoyad}
          onChange={(e) => setForm({ ...form, adSoyad: e.target.value })}
          required
        />
        <input
          type="email"
          placeholder="Email"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          required
        />
        <input
          type="password"
          placeholder="Şifre (en az 6 karakter)"
          value={form.sifre}
          onChange={(e) => setForm({ ...form, sifre: e.target.value })}
          required
          minLength={6}
        />
        <select value={form.rol} onChange={(e) => setForm({ ...form, rol: e.target.value })}>
          {ROLES.map((r) => (
            <option key={r} value={r}>{r}</option>
          ))}
        </select>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Ekleniyor…' : 'Kullanıcı Ekle'}
        </button>
        {formError && <div className="admin-form-error">{formError}</div>}
      </form>

      <table className="admin-table">
        <thead>
          <tr>
            <th>Ad Soyad</th>
            <th>Email</th>
            <th>Rol</th>
          </tr>
        </thead>
        <tbody>
          {loading && (
            <tr><td colSpan={3} className="admin-table-empty">Yükleniyor…</td></tr>
          )}
          {!loading && users.length === 0 && (
            <tr><td colSpan={3} className="admin-table-empty">Henüz kullanıcı yok.</td></tr>
          )}
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.adSoyad}</td>
              <td>{u.email}</td>
              <td><span className={`admin-badge admin-badge-${u.rol.toLowerCase()}`}>{u.rol}</span></td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}

function ProjectSection({ projects, pmUsers, loading, onCreated }) {
  const [form, setForm] = useState({ ad: '', musteri: '', sorumluPmId: '', durum: 'AKTIF' })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setFormError('')

    if (!form.sorumluPmId) {
      setFormError('Sorumlu proje yöneticisi seçilmelidir.')
      return
    }

    setSubmitting(true)
    try {
      await createProject({ ...form, sorumluPmId: Number(form.sorumluPmId) })
      setForm({ ad: '', musteri: '', sorumluPmId: '', durum: 'AKTIF' })
      onCreated()
    } catch (err) {
      setFormError(err.response?.data?.error || 'Proje oluşturulamadı.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="admin-card">
      <h2>Projeler</h2>

      <form className="admin-form" onSubmit={handleSubmit}>
        <input
          placeholder="Proje Adı"
          value={form.ad}
          onChange={(e) => setForm({ ...form, ad: e.target.value })}
          required
        />
        <input
          placeholder="Müşteri (opsiyonel)"
          value={form.musteri}
          onChange={(e) => setForm({ ...form, musteri: e.target.value })}
        />
        <select
          value={form.sorumluPmId}
          onChange={(e) => setForm({ ...form, sorumluPmId: e.target.value })}
          required
        >
          <option value="">Sorumlu PM seçin…</option>
          {pmUsers.map((pm) => (
            <option key={pm.id} value={pm.id}>{pm.adSoyad}</option>
          ))}
        </select>
        <select value={form.durum} onChange={(e) => setForm({ ...form, durum: e.target.value })}>
          {PROJECT_STATUSES.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
        <button type="submit" disabled={submitting || pmUsers.length === 0}>
          {submitting ? 'Ekleniyor…' : 'Proje Ekle'}
        </button>
        {pmUsers.length === 0 && !loading && (
          <div className="admin-form-hint">Proje ekleyebilmek için önce bir PM kullanıcısı oluşturun.</div>
        )}
        {formError && <div className="admin-form-error">{formError}</div>}
      </form>

      <table className="admin-table">
        <thead>
          <tr>
            <th>Proje</th>
            <th>Müşteri</th>
            <th>Sorumlu PM</th>
            <th>Durum</th>
          </tr>
        </thead>
        <tbody>
          {loading && (
            <tr><td colSpan={4} className="admin-table-empty">Yükleniyor…</td></tr>
          )}
          {!loading && projects.length === 0 && (
            <tr><td colSpan={4} className="admin-table-empty">Henüz proje yok.</td></tr>
          )}
          {projects.map((p) => (
            <tr key={p.id}>
              <td>{p.ad}</td>
              <td>{p.musteri || '—'}</td>
              <td>{p.sorumluPmAdSoyad}</td>
              <td><span className={`admin-badge admin-badge-status-${p.durum.toLowerCase()}`}>{p.durum}</span></td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}
