import { useEffect, useState } from 'react'
import { createProject, updateProject } from '../api/projects'
import { listUsers } from '../api/users'
import { PROJECT_STATUS, errorMessage } from '../utils/format'

export default function ProjectForm({ project, onSaved, onCancel }) {
  const [users, setUsers] = useState([])
  const [form, setForm] = useState({
    kod: project?.kod ?? '',
    ad: project?.ad ?? '',
    aciklama: project?.aciklama ?? '',
    musteri: project?.musteri ?? '',
    departman: project?.departman ?? '',
    butce: project?.butce ?? '',
    teslimTarihi: project?.teslimTarihi ?? '',
    sorumluPmId: project?.sorumluPmId ?? '',
    ekipLideriId: project?.ekipLideriId ?? '',
    durum: project?.durum ?? 'AKTIF',
  })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    listUsers().then(setUsers).catch(() => setError('Kullanıcı listesi yüklenemedi'))
  }, [])

  const pms = users.filter((u) => u.rol === 'PM')
  const leads = users.filter((u) => u.rol === 'LEAD')
  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    const payload = {
      ...form,
      butce: form.butce === '' ? null : Number(form.butce),
      teslimTarihi: form.teslimTarihi || null,
      sorumluPmId: form.sorumluPmId ? Number(form.sorumluPmId) : null,
      ekipLideriId: form.ekipLideriId ? Number(form.ekipLideriId) : null,
    }
    try {
      const saved = project ? await updateProject(project.id, payload) : await createProject(payload)
      onSaved(saved)
    } catch (err) {
      setError(errorMessage(err, 'Proje kaydedilemedi'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-grid">
        <label className="field">
          Proje kodu
          <input value={form.kod} onChange={set('kod')} placeholder="KLF-250" />
          <small>Boş bırakılırsa otomatik verilir.</small>
        </label>
        <label className="field">
          Proje adı
          <input value={form.ad} onChange={set('ad')} required />
        </label>
        <label className="field full">
          Açıklama
          <textarea value={form.aciklama} onChange={set('aciklama')} />
        </label>
        <label className="field">
          Departman
          <input value={form.departman} onChange={set('departman')} />
        </label>
        <label className="field">
          Müşteri
          <input value={form.musteri} onChange={set('musteri')} />
        </label>
        <label className="field">
          Bütçe (TL)
          <input type="number" min="0" step="1000" value={form.butce} onChange={set('butce')} />
        </label>
        <label className="field">
          Teslim tarihi
          <input type="date" value={form.teslimTarihi} onChange={set('teslimTarihi')} />
        </label>
        <label className="field">
          Proje yöneticisi
          <select value={form.sorumluPmId} onChange={set('sorumluPmId')} required>
            <option value="">Seçin</option>
            {pms.map((u) => <option key={u.id} value={u.id}>{u.adSoyad}</option>)}
          </select>
        </label>
        <label className="field">
          Ekip lideri
          <select value={form.ekipLideriId} onChange={set('ekipLideriId')}>
            <option value="">Atanmadı</option>
            {leads.map((u) => <option key={u.id} value={u.id}>{u.adSoyad}</option>)}
          </select>
        </label>
        <label className="field">
          Durum
          <select value={form.durum} onChange={set('durum')}>
            {Object.entries(PROJECT_STATUS).map(([key, label]) => <option key={key} value={key}>{label}</option>)}
          </select>
        </label>
      </div>

      {error && <div className="form-error" style={{ marginTop: 16 }}>{error}</div>}

      <div className="form-actions">
        <button type="button" className="secondary-button" onClick={onCancel}>Vazgeç</button>
        <button className="primary-button" disabled={submitting}>
          {submitting ? 'Kaydediliyor...' : project ? 'Değişiklikleri kaydet' : 'Projeyi oluştur'}
        </button>
      </div>
    </form>
  )
}
