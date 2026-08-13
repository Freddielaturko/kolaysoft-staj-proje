import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { listProjects } from '../api/projects'
import { listWeeklyReports, saveWeeklyReport } from '../api/weeklyReports'
import { listTasks, createTask, updateTask } from '../api/tasks'
import './PmHome.css'

const GENEL_DURUM = ['YESIL', 'SARI', 'KIRMIZI']
const RISK_SEVIYESI = ['DUSUK', 'ORTA', 'YUKSEK']
const TASK_DURUM = ['BEKLEMEDE', 'DEVAM_EDIYOR', 'TAMAMLANDI', 'IPTAL']

function thisMonday() {
  const d = new Date()
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  const monday = new Date(d.setDate(diff))
  return monday.toISOString().slice(0, 10)
}

export default function PmHome() {
  const [projects, setProjects] = useState([])
  const [selectedProjectId, setSelectedProjectId] = useState(null)
  const [loadingProjects, setLoadingProjects] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    listProjects()
      .then((data) => {
        setProjects(data)
        if (data.length > 0) setSelectedProjectId(data[0].id)
      })
      .catch(() => setError('Projeler yüklenemedi. Backend çalışıyor mu kontrol edin.'))
      .finally(() => setLoadingProjects(false))
  }, [])

  const selectedProject = projects.find((p) => p.id === selectedProjectId)

  return (
    <AppLayout title="Proje Yöneticisi Paneli">
      {error && <div className="pm-error">{error}</div>}

      {!loadingProjects && projects.length === 0 && (
        <div className="pm-empty">Size atanmış bir proje bulunmuyor. Admin'den proje ataması bekleyin.</div>
      )}

      {projects.length > 0 && (
        <>
          {projects.length > 1 && (
            <div className="pm-project-selector">
              <label>Proje:</label>
              <select
                value={selectedProjectId}
                onChange={(e) => setSelectedProjectId(Number(e.target.value))}
              >
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>{p.ad}</option>
                ))}
              </select>
            </div>
          )}

          {selectedProject && <ProjectWorkspace project={selectedProject} />}
        </>
      )}
    </AppLayout>
  )
}

function ProjectWorkspace({ project }) {
  const [reports, setReports] = useState([])
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [project.id])

  async function loadData() {
    setLoading(true)
    try {
      const [reportsData, tasksData] = await Promise.all([
        listWeeklyReports(project.id),
        listTasks(project.id),
      ])
      setReports(reportsData)
      setTasks(tasksData)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="pm-workspace">
      <WeeklyReportSection projectId={project.id} reports={reports} loading={loading} onSaved={loadData} />
      <TaskSection projectId={project.id} tasks={tasks} loading={loading} onChanged={loadData} />
    </div>
  )
}

function WeeklyReportSection({ projectId, reports, loading, onSaved }) {
  const [form, setForm] = useState({
    raporHaftasi: thisMonday(),
    hedeflenenIlerleme: 0,
    gerceklesenIlerleme: 0,
    genelDurum: 'YESIL',
    riskSeviyesi: 'DUSUK',
    canliTask: '',
    yapilanlar: '',
    yapilacaklar: '',
    riskEngelNotu: '',
    genelNot: '',
  })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const [savedMsg, setSavedMsg] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setFormError('')
    setSavedMsg('')
    setSubmitting(true)
    try {
      await saveWeeklyReport(projectId, form)
      setSavedMsg('Rapor kaydedildi.')
      onSaved()
    } catch (err) {
      setFormError(err.response?.data?.error || 'Rapor kaydedilemedi.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="pm-card">
      <h2>Haftalık Rapor</h2>

      <form className="pm-report-form" onSubmit={handleSubmit}>
        <div className="pm-form-row">
          <label>
            Rapor Haftası
            <input
              type="date"
              value={form.raporHaftasi}
              onChange={(e) => setForm({ ...form, raporHaftasi: e.target.value })}
              required
            />
          </label>
          <label>
            Hedeflenen İlerleme (%)
            <input
              type="number" min={0} max={100}
              value={form.hedeflenenIlerleme}
              onChange={(e) => setForm({ ...form, hedeflenenIlerleme: Number(e.target.value) })}
              required
            />
          </label>
          <label>
            Gerçekleşen İlerleme (%)
            <input
              type="number" min={0} max={100}
              value={form.gerceklesenIlerleme}
              onChange={(e) => setForm({ ...form, gerceklesenIlerleme: Number(e.target.value) })}
              required
            />
          </label>
        </div>

        <div className="pm-form-row">
          <label>
            Genel Durum
            <select value={form.genelDurum} onChange={(e) => setForm({ ...form, genelDurum: e.target.value })}>
              {GENEL_DURUM.map((d) => <option key={d} value={d}>{d}</option>)}
            </select>
          </label>
          <label>
            Risk Seviyesi
            <select value={form.riskSeviyesi} onChange={(e) => setForm({ ...form, riskSeviyesi: e.target.value })}>
              {RISK_SEVIYESI.map((r) => <option key={r} value={r}>{r}</option>)}
            </select>
          </label>
          <label>
            Canlı Task
            <input
              value={form.canliTask}
              onChange={(e) => setForm({ ...form, canliTask: e.target.value })}
              placeholder="Şu an üzerinde çalışılan iş"
            />
          </label>
        </div>

        <label>
          Bu Hafta Yapılanlar
          <textarea rows={2} value={form.yapilanlar} onChange={(e) => setForm({ ...form, yapilanlar: e.target.value })} />
        </label>
        <label>
          Gelecek Hafta Yapılacaklar
          <textarea rows={2} value={form.yapilacaklar} onChange={(e) => setForm({ ...form, yapilacaklar: e.target.value })} />
        </label>
        <label>
          Risk / Engel Notu
          <textarea rows={2} value={form.riskEngelNotu} onChange={(e) => setForm({ ...form, riskEngelNotu: e.target.value })} />
        </label>
        <label>
          Genel Not
          <textarea rows={2} value={form.genelNot} onChange={(e) => setForm({ ...form, genelNot: e.target.value })} />
        </label>

        {formError && <div className="pm-form-error">{formError}</div>}
        {savedMsg && <div className="pm-form-success">{savedMsg}</div>}

        <button type="submit" disabled={submitting}>
          {submitting ? 'Kaydediliyor…' : 'Raporu Kaydet'}
        </button>
      </form>

      <h3 className="pm-subheading">Geçmiş Raporlar</h3>
      <table className="pm-table">
        <thead>
          <tr>
            <th>Hafta</th>
            <th>İlerleme</th>
            <th>Durum</th>
            <th>Risk</th>
          </tr>
        </thead>
        <tbody>
          {loading && <tr><td colSpan={4} className="pm-table-empty">Yükleniyor…</td></tr>}
          {!loading && reports.length === 0 && <tr><td colSpan={4} className="pm-table-empty">Henüz rapor yok.</td></tr>}
          {reports.map((r) => (
            <tr key={r.id}>
              <td>{r.raporHaftasi}</td>
              <td>{r.gerceklesenIlerleme}% / {r.hedeflenenIlerleme}%</td>
              <td><span className={`pm-badge pm-badge-durum-${r.genelDurum.toLowerCase()}`}>{r.genelDurum}</span></td>
              <td><span className={`pm-badge pm-badge-risk-${r.riskSeviyesi.toLowerCase()}`}>{r.riskSeviyesi}</span></td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}

function TaskSection({ projectId, tasks, loading, onChanged }) {
  const [form, setForm] = useState({ baslik: '', sorumlu: '', durum: 'BEKLEMEDE', planlananTarih: '' })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setFormError('')
    setSubmitting(true)
    try {
      await createTask(projectId, form)
      setForm({ baslik: '', sorumlu: '', durum: 'BEKLEMEDE', planlananTarih: '' })
      onChanged()
    } catch (err) {
      setFormError(err.response?.data?.error || 'İş kalemi eklenemedi.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleStatusChange(task, newDurum) {
    try {
      await updateTask(projectId, task.id, { ...task, durum: newDurum })
      onChanged()
    } catch {
      // sessizce yut, liste yenilenmeyince kullanici zaten fark eder
    }
  }

  return (
    <section className="pm-card">
      <h2>İş Kalemleri</h2>

      <form className="pm-task-form" onSubmit={handleSubmit}>
        <input
          placeholder="Başlık"
          value={form.baslik}
          onChange={(e) => setForm({ ...form, baslik: e.target.value })}
          required
        />
        <input
          placeholder="Sorumlu"
          value={form.sorumlu}
          onChange={(e) => setForm({ ...form, sorumlu: e.target.value })}
        />
        <input
          type="date"
          value={form.planlananTarih}
          onChange={(e) => setForm({ ...form, planlananTarih: e.target.value })}
        />
        <select value={form.durum} onChange={(e) => setForm({ ...form, durum: e.target.value })}>
          {TASK_DURUM.map((d) => <option key={d} value={d}>{d}</option>)}
        </select>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Ekleniyor…' : 'Ekle'}
        </button>
        {formError && <div className="pm-form-error">{formError}</div>}
      </form>

      <table className="pm-table">
        <thead>
          <tr>
            <th>Başlık</th>
            <th>Sorumlu</th>
            <th>Planlanan Tarih</th>
            <th>Durum</th>
          </tr>
        </thead>
        <tbody>
          {loading && <tr><td colSpan={4} className="pm-table-empty">Yükleniyor…</td></tr>}
          {!loading && tasks.length === 0 && <tr><td colSpan={4} className="pm-table-empty">Henüz iş kalemi yok.</td></tr>}
          {tasks.map((t) => (
            <tr key={t.id}>
              <td>{t.baslik}</td>
              <td>{t.sorumlu || '—'}</td>
              <td>{t.planlananTarih || '—'}</td>
              <td>
                <select
                  className={`pm-task-status-select pm-badge-task-${t.durum.toLowerCase()}`}
                  value={t.durum}
                  onChange={(e) => handleStatusChange(t, e.target.value)}
                >
                  {TASK_DURUM.map((d) => <option key={d} value={d}>{d}</option>)}
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}
