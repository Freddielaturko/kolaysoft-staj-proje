import { useEffect, useState } from 'react'
import { Plus } from 'lucide-react'
import { createUser, listUsers } from '../api/users'
import Modal from '../components/Modal'
import Badge from '../components/Badge'
import { ROLE_LABELS, errorMessage, initials } from '../utils/format'

const ROLE_TONE = { ADMIN: 'archive', CTO: 'info', PM: 'normal', LEAD: 'attention' }

export default function Users() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [formOpen, setFormOpen] = useState(false)

  const load = () =>
    listUsers()
      .then(setUsers)
      .catch(() => setError('Kullanıcılar yüklenemedi'))
      .finally(() => setLoading(false))

  useEffect(() => {
    load()
  }, [])

  if (loading) return <div className="loading">Kullanıcılar yükleniyor...</div>

  return (
    <>
      {error && <div className="page-error">{error}</div>}
      <section className="panel project-panel">
        <div className="panel-head table-title">
          <div>
            <p className="eyebrow blue">ERİŞİM / {users.length} KULLANICI</p>
            <h3>Kullanıcılar</h3>
          </div>
          <button className="primary-button" onClick={() => setFormOpen(true)}><Plus size={16} /> Kullanıcı ekle</button>
        </div>
        <div className="table-scroll">
          <table>
            <thead>
              <tr><th>AD SOYAD</th><th>E-POSTA</th><th>ROL</th></tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>
                    <div className="project-name">
                      <span className="avatar">{initials(u.adSoyad)}</span>
                      <strong>{u.adSoyad}</strong>
                    </div>
                  </td>
                  <td>{u.email}</td>
                  <td><Badge tone={ROLE_TONE[u.rol]}>{ROLE_LABELS[u.rol]}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {formOpen && (
        <Modal title="Yeni kullanıcı" eyebrow="ERİŞİM" onClose={() => setFormOpen(false)}>
          <UserForm
            onCancel={() => setFormOpen(false)}
            onSaved={() => {
              setFormOpen(false)
              load()
            }}
          />
        </Modal>
      )}
    </>
  )
}

function UserForm({ onSaved, onCancel }) {
  const [form, setForm] = useState({ adSoyad: '', email: '', sifre: '', rol: 'PM' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await createUser(form)
      onSaved()
    } catch (err) {
      setError(errorMessage(err, 'Kullanıcı oluşturulamadı'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-grid">
        <label className="field full">
          Ad soyad
          <input value={form.adSoyad} onChange={set('adSoyad')} required />
        </label>
        <label className="field">
          E-posta
          <input type="email" value={form.email} onChange={set('email')} required />
        </label>
        <label className="field">
          Rol
          <select value={form.rol} onChange={set('rol')}>
            {Object.entries(ROLE_LABELS).map(([key, label]) => <option key={key} value={key}>{label}</option>)}
          </select>
        </label>
        <label className="field full">
          Geçici şifre
          <input type="password" value={form.sifre} onChange={set('sifre')} minLength={6} required autoComplete="new-password" />
          <small>En az 6 karakter. Kullanıcıya ayrı bir kanaldan iletin.</small>
        </label>
      </div>
      {error && <div className="form-error" style={{ marginTop: 16 }}>{error}</div>}
      <div className="form-actions">
        <button type="button" className="secondary-button" onClick={onCancel}>Vazgeç</button>
        <button className="primary-button" disabled={submitting}>{submitting ? 'Kaydediliyor...' : 'Kullanıcıyı oluştur'}</button>
      </div>
    </form>
  )
}
