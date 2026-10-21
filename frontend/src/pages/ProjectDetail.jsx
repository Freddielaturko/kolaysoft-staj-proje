import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { AlertTriangle, CheckCircle2, ChevronLeft, Pencil, Plus } from 'lucide-react'
import { getProject } from '../api/projects'
import { listWeeklyReports } from '../api/weeklyReports'
import { createTask, listTasks, updateTask } from '../api/tasks'
import { useAuth } from '../context/AuthContext'
import { usePortfolio } from '../context/PortfolioContext'
import Badge from '../components/Badge'
import Modal from '../components/Modal'
import ReportForm from '../components/ReportForm'
import ReportView from '../components/ReportView'
import ProjectForm from '../components/ProjectForm'
import {
  GENEL_DURUM,
  HEALTH,
  PROJECT_STATUS,
  TASK_STATUS,
  canWrite,
  daysUntil,
  errorMessage,
  formatDate,
  money,
} from '../utils/format'

export default function ProjectDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const { reload: reloadPortfolio } = usePortfolio()
  const [project, setProject] = useState(null)
  const [reports, setReports] = useState([])
  const [tasks, setTasks] = useState([])
  const [error, setError] = useState('')
  const [modal, setModal] = useState(null)

  const load = useCallback(async () => {
    try {
      const [p, r, t] = await Promise.all([getProject(id), listWeeklyReports(id), listTasks(id)])
      setProject(p)
      setReports(r)
      setTasks(t)
    } catch (err) {
      setError(err.response?.status === 403 ? 'Bu projeye erişim yetkiniz yok.' : errorMessage(err, 'Proje yüklenemedi'))
    }
  }, [id])

  useEffect(() => {
    load()
  }, [load])

  async function afterChange() {
    setModal(null)
    await Promise.all([load(), reloadPortfolio()])
  }

  if (error) return <><BackLink /><div className="page-error">{error}</div></>
  if (!project) return <div className="loading">Proje yükleniyor...</div>

  const health = HEALTH[project.saglik] || HEALTH.NORMAL
  const writable = canWrite(user)
  const kalan = daysUntil(project.teslimTarihi)
  const finished = project.durum === 'TAMAMLANDI' || project.durum === 'ARSIV' || project.durum === 'IPTAL'

  let deadlineNote = 'Teslim tarihi girilmedi'
  if (finished) deadlineNote = PROJECT_STATUS[project.durum]
  else if (kalan != null && kalan < 0) deadlineNote = `${-kalan} gün gecikti`
  else if (kalan != null) deadlineNote = project.saglik === 'KRITIK' ? `${kalan} gün kaldı, gecikme riski var` : `${kalan} gün kaldı`

  return (
    <>
      <BackLink />

      <section className="detail-head">
        <div>
          <div className="code-label">{project.kod} / PROJE DETAYI</div>
          <h2>{project.ad}</h2>
          {project.aciklama && <p className="muted">{project.aciklama}</p>}
          <div className="team-line">
            <span>Proje yöneticisi: <strong>{project.sorumluPmAdSoyad}</strong></span>
            <span>Ekip lideri: <strong>{project.ekipLideriAdSoyad || 'Atanmadı'}</strong></span>
            {project.musteri && <span>Müşteri: <strong>{project.musteri}</strong></span>}
          </div>
        </div>
        <div className="detail-actions">
          <Badge tone={health.tone}>{health.label}</Badge>
          {user.rol === 'ADMIN' && (
            <button className="secondary-button" onClick={() => setModal({ type: 'project' })}><Pencil size={14} /> Düzenle</button>
          )}
          {writable && (
            <button className="primary-button" onClick={() => setModal({ type: 'report-form' })}><Plus size={16} /> Rapor gir</button>
          )}
        </div>
      </section>

      <div className="detail-kpis">
        <div>
          <span>İlerleme</span>
          <strong>%{project.ilerleme}</strong>
          <div className="big-progress"><i style={{ width: `${project.ilerleme}%` }} className={health.tone}></i></div>
        </div>
        <div>
          <span>Teslim tarihi</span>
          <strong>{formatDate(project.teslimTarihi)}</strong>
          <small className={!finished && kalan != null && (kalan < 0 || project.saglik === 'KRITIK') ? 'late-tag' : ''}>{deadlineNote}</small>
        </div>
        <div>
          <span>Proje bütçesi</span>
          <strong>{money(project.butce)}</strong>
          <small>{project.departman || '—'}</small>
        </div>
      </div>

      <div className="detail-grid">
        <section className="panel">
          <div className="panel-head">
            <div>
              <p className="eyebrow blue">ZAMAN ÇİZELGESİ</p>
              <h3>Haftalık ilerleme</h3>
            </div>
            {project.raporGecikti && <Badge tone="attention">Rapor gecikti</Badge>}
          </div>
          {reports.length === 0 && <div className="empty">Bu proje için henüz haftalık rapor girilmedi.</div>}
          <div className="timeline">
            {reports.map((r, i) => {
              const durum = GENEL_DURUM[r.genelDurum]
              return (
                <button className="timeline-item" key={r.id} onClick={() => setModal({ type: 'report', report: r })}>
                  <span className={i === 0 ? 'timeline-dot current' : 'timeline-dot'}></span>
                  <div>
                    <div className="timeline-meta">
                      <strong>{formatDate(r.raporHaftasi)} haftası</strong>
                      <Badge tone={durum.tone}>{durum.label}</Badge>
                    </div>
                    <p>{r.yapilanlar || 'Yapılanlar girilmedi.'}</p>
                    <small>
                      Hedef %{r.hedeflenenIlerleme} · Gerçekleşen %{r.gerceklesenIlerleme} · Revizyon {r.revizyon} · {r.olusturanUserAdSoyad}
                    </small>
                  </div>
                </button>
              )
            })}
          </div>
        </section>

        <section className="panel risk-box">
          <p className="eyebrow red">RİSK VE AKSİYONLAR</p>
          <h3>Takip edilmesi gerekenler</h3>
          {project.riskler.map((risk) => (
            <div className="risk-item" key={risk}>
              <AlertTriangle size={16} />
              <span>{risk}</span>
            </div>
          ))}
          {project.raporGecikti && (
            <div className="risk-item">
              <AlertTriangle size={16} />
              <span>Geçen haftanın raporu girilmedi</span>
            </div>
          )}
          {project.riskler.length === 0 && !project.raporGecikti && (
            <div className="safe-state"><CheckCircle2 size={21} /> Kritik risk bulunmuyor</div>
          )}
        </section>
      </div>

      <TaskPanel projectId={project.id} tasks={tasks} writable={writable} onChanged={load} />

      {modal?.type === 'report' && (
        <Modal title={`${formatDate(modal.report.raporHaftasi)} haftası`} eyebrow={`${project.kod} / HAFTALIK RAPOR`} onClose={() => setModal(null)} wide>
          <ReportView report={modal.report} onEdit={writable ? () => setModal({ type: 'report-form', report: modal.report }) : null} />
        </Modal>
      )}
      {modal?.type === 'report-form' && (
        <Modal title={modal.report ? 'Raporu düzenle' : 'Haftalık rapor'} eyebrow={project.kod} onClose={() => setModal(null)} wide>
          <ReportForm projects={[project]} initialProjectId={project.id} initialReport={modal.report} onSaved={afterChange} onCancel={() => setModal(null)} />
        </Modal>
      )}
      {modal?.type === 'project' && (
        <Modal title="Projeyi düzenle" eyebrow={project.kod} onClose={() => setModal(null)} wide>
          <ProjectForm project={project} onSaved={afterChange} onCancel={() => setModal(null)} />
        </Modal>
      )}
    </>
  )
}

function BackLink() {
  return (
    <Link className="back-button" to="/projeler">
      <ChevronLeft size={16} /> Projelere dön
    </Link>
  )
}

const TASK_TONE = { BEKLEMEDE: 'archive', DEVAM_EDIYOR: 'info', TAMAMLANDI: 'normal', IPTAL: 'critical' }

function TaskPanel({ projectId, tasks, writable, onChanged }) {
  const [formOpen, setFormOpen] = useState(false)
  const [error, setError] = useState('')

  async function changeStatus(task, durum) {
    setError('')
    try {
      await updateTask(projectId, task.id, {
        ...task,
        durum,
        tamamlananTarih: durum === 'TAMAMLANDI' ? task.tamamlananTarih || new Date().toISOString().slice(0, 10) : task.tamamlananTarih,
      })
      onChanged()
    } catch (err) {
      setError(errorMessage(err, 'İş kalemi güncellenemedi'))
    }
  }

  const done = tasks.filter((t) => t.durum === 'TAMAMLANDI').length

  return (
    <section className="panel task-panel">
      <div className="panel-head">
        <div>
          <p className="eyebrow blue">İŞ KALEMLERİ / {done} / {tasks.length} TAMAMLANDI</p>
          <h3>Görevler</h3>
        </div>
        {writable && <button className="secondary-button" onClick={() => setFormOpen(true)}><Plus size={14} /> İş kalemi ekle</button>}
      </div>
      {error && <div className="form-error" style={{ margin: '0 22px 12px' }}>{error}</div>}
      <div className="table-scroll">
        <table>
          <thead>
            <tr><th>İŞ KALEMİ</th><th>SORUMLU</th><th>PLANLANAN</th><th>TAMAMLANAN</th><th>DURUM</th></tr>
          </thead>
          <tbody>
            {tasks.map((t) => (
              <tr key={t.id}>
                <td className="task-title">
                  <strong>{t.baslik}</strong>
                  {t.aciklama && <small>{t.aciklama}</small>}
                </td>
                <td>{t.sorumlu || '—'}</td>
                <td><span className="date">{formatDate(t.planlananTarih)}</span></td>
                <td><span className="date">{formatDate(t.tamamlananTarih)}</span></td>
                <td>
                  {writable ? (
                    <select value={t.durum} onChange={(e) => changeStatus(t, e.target.value)}>
                      {Object.entries(TASK_STATUS).map(([key, label]) => <option key={key} value={key}>{label}</option>)}
                    </select>
                  ) : (
                    <Badge tone={TASK_TONE[t.durum]}>{TASK_STATUS[t.durum]}</Badge>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {tasks.length === 0 && <div className="empty">Bu projeye henüz iş kalemi eklenmedi.</div>}
      </div>

      {formOpen && (
        <Modal title="Yeni iş kalemi" onClose={() => setFormOpen(false)}>
          <TaskForm
            projectId={projectId}
            onCancel={() => setFormOpen(false)}
            onSaved={() => {
              setFormOpen(false)
              onChanged()
            }}
          />
        </Modal>
      )}
    </section>
  )
}

function TaskForm({ projectId, onSaved, onCancel }) {
  const [form, setForm] = useState({ baslik: '', aciklama: '', sorumlu: '', durum: 'BEKLEMEDE', planlananTarih: '', not: '' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await createTask(projectId, { ...form, planlananTarih: form.planlananTarih || null })
      onSaved()
    } catch (err) {
      setError(errorMessage(err, 'İş kalemi eklenemedi'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-grid">
        <label className="field full">
          Başlık
          <input value={form.baslik} onChange={set('baslik')} required />
        </label>
        <label className="field full">
          Açıklama
          <textarea value={form.aciklama} onChange={set('aciklama')} />
        </label>
        <label className="field">
          Sorumlu
          <input value={form.sorumlu} onChange={set('sorumlu')} />
        </label>
        <label className="field">
          Planlanan tarih
          <input type="date" value={form.planlananTarih} onChange={set('planlananTarih')} />
        </label>
        <label className="field">
          Durum
          <select value={form.durum} onChange={set('durum')}>
            {Object.entries(TASK_STATUS).map(([key, label]) => <option key={key} value={key}>{label}</option>)}
          </select>
        </label>
        <label className="field">
          Not
          <input value={form.not} onChange={set('not')} />
        </label>
      </div>
      {error && <div className="form-error" style={{ marginTop: 16 }}>{error}</div>}
      <div className="form-actions">
        <button type="button" className="secondary-button" onClick={onCancel}>Vazgeç</button>
        <button className="primary-button" disabled={submitting}>{submitting ? 'Kaydediliyor...' : 'Ekle'}</button>
      </div>
    </form>
  )
}
