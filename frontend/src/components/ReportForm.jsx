import { useEffect, useState } from 'react'
import { listWeeklyReports, saveWeeklyReport } from '../api/weeklyReports'
import { GENEL_DURUM, RISK, errorMessage, mondayOf, parseDate } from '../utils/format'

const EMPTY = {
  hedeflenenIlerleme: 0,
  gerceklesenIlerleme: 0,
  genelDurum: 'YESIL',
  riskSeviyesi: 'DUSUK',
  canliTask: '',
  yapilanlar: '',
  yapilacaklar: '',
  riskEngelNotu: '',
  genelNot: '',
  butceDurumu: '',
  ekipDurumu: '',
}

function fromReport(r) {
  const form = { ...EMPTY }
  Object.keys(EMPTY).forEach((key) => {
    if (r[key] != null) form[key] = r[key]
  })
  return form
}

// Ayni proje ve hafta icin rapor zaten varsa formu onunla dolduruyorum;
// backend bu durumda yeni kayit acmak yerine mevcut raporu guncelleyip revizyonu artiriyor.
export default function ReportForm({ projects, initialProjectId, initialReport, onSaved, onCancel }) {
  const [projectId, setProjectId] = useState(initialReport?.projectId ?? initialProjectId ?? projects[0]?.id ?? '')
  const [week, setWeek] = useState(initialReport?.raporHaftasi ?? mondayOf())
  const [form, setForm] = useState(initialReport ? fromReport(initialReport) : EMPTY)
  const [existing, setExisting] = useState([])
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!projectId) return
    listWeeklyReports(projectId).then(setExisting).catch(() => setExisting([]))
  }, [projectId])

  const match = existing.find((r) => r.raporHaftasi === week)

  useEffect(() => {
    if (initialReport) return
    if (match) {
      setForm(fromReport(match))
    } else {
      // Yeni haftada hedefi son raporun ilerlemesinden baslatmak giris yapani hizlandiriyor
      const last = existing[0]
      setForm({ ...EMPTY, hedeflenenIlerleme: last?.gerceklesenIlerleme ?? 0, gerceklesenIlerleme: last?.gerceklesenIlerleme ?? 0 })
    }
  }, [match?.id, existing, initialReport])

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const saved = await saveWeeklyReport(projectId, {
        ...form,
        raporHaftasi: week,
        hedeflenenIlerleme: Number(form.hedeflenenIlerleme),
        gerceklesenIlerleme: Number(form.gerceklesenIlerleme),
      })
      onSaved(saved)
    } catch (err) {
      setError(errorMessage(err, 'Rapor kaydedilemedi'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-grid">
        <label className="field">
          Proje
          <select
            value={projectId}
            onChange={(e) => setProjectId(Number(e.target.value))}
            disabled={Boolean(initialReport) || projects.length < 2}
          >
            {projects.map((p) => (
              <option key={p.id} value={p.id}>{p.kod} · {p.ad}</option>
            ))}
          </select>
        </label>
        <label className="field">
          Rapor haftası
          <input type="date" value={week} onChange={(e) => e.target.value && setWeek(mondayOf(parseDate(e.target.value)))} disabled={Boolean(initialReport)} required />
          <small>Seçtiğin tarih o haftanın pazartesisine yuvarlanır.</small>
        </label>

        <label className="field">
          Hedeflenen ilerleme (%)
          <input type="number" min="0" max="100" value={form.hedeflenenIlerleme} onChange={set('hedeflenenIlerleme')} required />
        </label>
        <label className="field">
          Gerçekleşen ilerleme (%)
          <input type="number" min="0" max="100" value={form.gerceklesenIlerleme} onChange={set('gerceklesenIlerleme')} required />
        </label>

        <label className="field">
          Genel durum
          <select value={form.genelDurum} onChange={set('genelDurum')}>
            {Object.entries(GENEL_DURUM).map(([key, d]) => <option key={key} value={key}>{d.label}</option>)}
          </select>
        </label>
        <label className="field">
          Risk seviyesi
          <select value={form.riskSeviyesi} onChange={set('riskSeviyesi')}>
            {Object.entries(RISK).map(([key, label]) => <option key={key} value={key}>{label}</option>)}
          </select>
        </label>

        <label className="field">
          Bütçe durumu
          <input value={form.butceDurumu} onChange={set('butceDurumu')} placeholder="Plan dahilinde" />
        </label>
        <label className="field">
          Ekip durumu
          <input value={form.ekipDurumu} onChange={set('ekipDurumu')} placeholder="İyi" />
        </label>

        <label className="field full">
          Bu hafta yapılanlar
          <textarea value={form.yapilanlar} onChange={set('yapilanlar')} />
        </label>
        <label className="field full">
          Gelecek hafta yapılacaklar
          <textarea value={form.yapilacaklar} onChange={set('yapilacaklar')} />
        </label>
        <label className="field full">
          Risk ve engeller
          <textarea value={form.riskEngelNotu} onChange={set('riskEngelNotu')} />
          <small>Her satır dashboard'da ayrı bir risk maddesi olarak görünür.</small>
        </label>
        <label className="field">
          Canlıdaki task
          <input value={form.canliTask} onChange={set('canliTask')} />
        </label>
        <label className="field">
          Genel not
          <input value={form.genelNot} onChange={set('genelNot')} />
        </label>
      </div>

      {match && !initialReport && (
        <div className="form-success" style={{ marginTop: 16 }}>
          Bu hafta için rapor zaten girilmiş. Kaydettiğinde revizyon {match.revizyon + 1} olarak güncellenecek.
        </div>
      )}
      {error && <div className="form-error" style={{ marginTop: 16 }}>{error}</div>}

      <div className="form-actions">
        <button type="button" className="secondary-button" onClick={onCancel}>Vazgeç</button>
        <button className="primary-button" disabled={submitting || !projectId}>
          {submitting ? 'Kaydediliyor...' : 'Raporu kaydet'}
        </button>
      </div>
    </form>
  )
}
