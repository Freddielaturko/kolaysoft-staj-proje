import { useEffect, useState } from 'react'
import AppLayout from '../components/AppLayout'
import { listProjects } from '../api/projects'
import { getCtoDashboard } from '../api/dashboard'
import './CtoHome.css'

const GENEL_DURUM = ['YESIL', 'SARI', 'KIRMIZI']
const RISK_SEVIYESI = ['DUSUK', 'ORTA', 'YUKSEK']

const DURUM_LABEL = { YESIL: 'Yeşil', SARI: 'Sarı', KIRMIZI: 'Kırmızı' }
const RISK_LABEL = { DUSUK: 'Düşük', ORTA: 'Orta', YUKSEK: 'Yüksek' }

export default function CtoHome() {
  const [projects, setProjects] = useState([])
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filters, setFilters] = useState({ projectId: '', genelDurum: '', riskSeviyesi: '' })

  useEffect(() => {
    listProjects().catch(() => []).then((data) => setProjects(data || []))
  }, [])

  useEffect(() => {
    loadDashboard()
  }, [filters])

  async function loadDashboard() {
    setLoading(true)
    setError('')
    try {
      const data = await getCtoDashboard(filters)
      setReports(data)
    } catch {
      setError('Dashboard verileri yüklenemedi. Backend çalışıyor mu kontrol edin.')
    } finally {
      setLoading(false)
    }
  }

  const counts = {
    YESIL: reports.filter((r) => r.genelDurum === 'YESIL').length,
    SARI: reports.filter((r) => r.genelDurum === 'SARI').length,
    KIRMIZI: reports.filter((r) => r.genelDurum === 'KIRMIZI').length,
  }

  return (
    <AppLayout title="CTO Takip Paneli">
      {error && <div className="cto-error">{error}</div>}

      <div className="cto-summary">
        <SummaryCard label="Yeşil" count={counts.YESIL} tone="yesil" />
        <SummaryCard label="Sarı" count={counts.SARI} tone="sari" />
        <SummaryCard label="Kırmızı" count={counts.KIRMIZI} tone="kirmizi" />
        <SummaryCard label="Toplam Proje" count={reports.length} tone="notr" />
      </div>

      <div className="cto-filters">
        <select
          value={filters.projectId}
          onChange={(e) => setFilters({ ...filters, projectId: e.target.value })}
        >
          <option value="">Tüm Projeler</option>
          {projects.map((p) => (
            <option key={p.id} value={p.id}>{p.ad}</option>
          ))}
        </select>

        <select
          value={filters.genelDurum}
          onChange={(e) => setFilters({ ...filters, genelDurum: e.target.value })}
        >
          <option value="">Tüm Durumlar</option>
          {GENEL_DURUM.map((d) => (
            <option key={d} value={d}>{DURUM_LABEL[d]}</option>
          ))}
        </select>

        <select
          value={filters.riskSeviyesi}
          onChange={(e) => setFilters({ ...filters, riskSeviyesi: e.target.value })}
        >
          <option value="">Tüm Risk Seviyeleri</option>
          {RISK_SEVIYESI.map((r) => (
            <option key={r} value={r}>{RISK_LABEL[r]}</option>
          ))}
        </select>

        {(filters.projectId || filters.genelDurum || filters.riskSeviyesi) && (
          <button
            className="cto-filter-clear"
            onClick={() => setFilters({ projectId: '', genelDurum: '', riskSeviyesi: '' })}
          >
            Filtreleri Temizle
          </button>
        )}
      </div>

      {loading && <div className="cto-loading">Yükleniyor…</div>}

      {!loading && reports.length === 0 && (
        <div className="cto-empty">Filtreye uyan proje bulunamadı.</div>
      )}

      <div className="cto-grid">
        {reports.map((r) => (
          <ProjectCard key={r.id} report={r} />
        ))}
      </div>
    </AppLayout>
  )
}

function SummaryCard({ label, count, tone }) {
  return (
    <div className={`cto-summary-card cto-summary-${tone}`}>
      <div className="cto-summary-count">{count}</div>
      <div className="cto-summary-label">{label}</div>
    </div>
  )
}

function ProjectCard({ report: r }) {
  return (
    <div className={`cto-project-card cto-project-border-${r.genelDurum.toLowerCase()}`}>
      <div className="cto-project-header">
        <h3>{r.projectAd}</h3>
        <span className={`cto-badge cto-badge-durum-${r.genelDurum.toLowerCase()}`}>{DURUM_LABEL[r.genelDurum]}</span>
      </div>

      <div className="cto-progress-row">
        <div className="cto-progress-track">
          <div
            className="cto-progress-fill"
            style={{ width: `${r.gerceklesenIlerleme}%` }}
          />
        </div>
        <span className="cto-progress-label">{r.gerceklesenIlerleme}% / {r.hedeflenenIlerleme}% hedef</span>
      </div>

      <div className="cto-project-meta">
        <span className={`cto-badge cto-badge-risk-${r.riskSeviyesi.toLowerCase()}`}>Risk: {RISK_LABEL[r.riskSeviyesi]}</span>
        <span className="cto-project-week">Hafta: {r.raporHaftasi}</span>
      </div>

      {r.canliTask && <p className="cto-project-task"><strong>Canlı Task:</strong> {r.canliTask}</p>}
      {r.riskEngelNotu && <p className="cto-project-risk-note">{r.riskEngelNotu}</p>}

      <div className="cto-project-footer">Rapor: {r.olusturanUserAdSoyad}</div>
    </div>
  )
}
