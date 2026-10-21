import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { AlertTriangle, BarChart3, CheckCircle2, ChevronRight, ClipboardList, FolderKanban, Plus } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { usePortfolio } from '../context/PortfolioContext'
import ProjectTable from '../components/ProjectTable'
import { formatDate, parseDate } from '../utils/format'

function Delta({ value, suffix }) {
  if (value == null) return <small>Karşılaştırma için yeterli rapor yok</small>
  const tone = value > 0 ? 'up' : value < 0 ? 'down' : 'flat'
  const sign = value > 0 ? '+' : ''
  return <small><em className={tone}>{sign}{value} puan</em> {suffix}</small>
}

function monthLabel(iso) {
  return parseDate(iso).toLocaleDateString('tr-TR', { month: 'short' })
}

export default function Dashboard() {
  const { user } = useAuth()
  const { projects, alerts, summary, loading, error } = usePortfolio()
  const navigate = useNavigate()
  const [showAllAlerts, setShowAllAlerts] = useState(false)

  if (loading) return <div className="loading">Portföy yükleniyor...</div>
  if (error) return <div className="page-error">{error}</div>

  const firstName = user.adSoyad.split(' ')[0]
  const today = new Date().toLocaleDateString('tr-TR', { day: 'numeric', month: 'long', year: 'numeric' })
  const trend = summary.trend
  const lastPoint = [...trend].reverse().find((t) => t.ortalamaIlerleme != null)
  const reportRate = summary.buHaftaBeklenen ? Math.round((summary.buHaftaRaporlanan / summary.buHaftaBeklenen) * 100) : 0
  const visibleAlerts = showAllAlerts ? alerts : alerts.slice(0, 3)

  let action = null
  if (user.rol === 'ADMIN') {
    action = <button className="primary-button" onClick={() => navigate('/projeler?yeni=1')}><Plus size={17} /> Yeni proje</button>
  } else if (user.rol === 'PM' || user.rol === 'LEAD') {
    action = <button className="primary-button" onClick={() => navigate('/raporlar?yeni=1')}><Plus size={17} /> Haftalık rapor gir</button>
  }

  return (
    <>
      <section className="hero-row">
        <div>
          <p className="eyebrow blue">GENEL BAKIŞ / {today.toLocaleUpperCase('tr-TR')}</p>
          <h2>Kontrol sizde, <span>{firstName}.</span></h2>
          <p className="muted">Proje portföyünüzdeki değişimleri ve aksiyon gerektiren noktaları inceleyin.</p>
        </div>
        {action}
      </section>

      <section className="kpi-grid">
        <div className="kpi">
          <div className="kpi-top"><span>Aktif projeler</span><FolderKanban size={18} /></div>
          <strong>{summary.aktifProje}</strong>
          <small><em className={summary.dikkatProje ? 'down' : 'up'}>{summary.dikkatProje}</em> projede dikkat gerekiyor</small>
        </div>
        <div className="kpi">
          <div className="kpi-top"><span>Ortalama ilerleme</span><BarChart3 size={18} /></div>
          <strong>%{summary.ortalamaIlerleme}</strong>
          <Delta value={summary.ortalamaIlerlemeDegisim} suffix="geçen haftaya göre" />
        </div>
        <div className={summary.kritikProje ? 'kpi danger-kpi' : 'kpi'}>
          <div className="kpi-top"><span>Kritik durum</span><AlertTriangle size={18} /></div>
          <strong>{summary.kritikProje}</strong>
          <small>{summary.kritikProje ? <em className="down">Aksiyon gerekli</em> : <em className="up">Kritik proje yok</em>}</small>
        </div>
        <div className="kpi">
          <div className="kpi-top"><span>Bu hafta rapor</span><ClipboardList size={18} /></div>
          <strong>{summary.buHaftaRaporlanan}<small className="fraction">/{summary.buHaftaBeklenen}</small></strong>
          <small><em className={reportRate === 100 ? 'up' : 'flat'}>%{reportRate}</em> aktif proje raporlandı</small>
        </div>
      </section>

      <div className="dashboard-grid">
        <section className="panel alerts-panel">
          <div className="panel-head">
            <div>
              <p className="eyebrow red">ÖNCELİKLİ AKSİYONLAR</p>
              <h3>Uyarı merkezi <span className="count-dot">{alerts.length}</span></h3>
            </div>
            {alerts.length > 3 && (
              <button className="text-button" onClick={() => setShowAllAlerts(!showAllAlerts)}>
                {showAllAlerts ? 'Daha az göster' : 'Tümünü gör'} <ChevronRight size={15} />
              </button>
            )}
          </div>
          {alerts.length === 0 && (
            <div className="safe-state"><CheckCircle2 size={21} /> Aksiyon gerektiren bir durum yok</div>
          )}
          {visibleAlerts.map((a) => (
            <button className="alert-row" key={a.id} onClick={() => navigate(`/projeler/${a.projectId}`)}>
              <span className={`alert-icon ${a.seviye === 'KRITIK' ? 'critical' : 'attention'}`}>
                <AlertTriangle size={17} />
              </span>
              <span>
                <strong>{a.projectAd}</strong>
                <small>{a.mesaj}</small>
              </span>
              <ChevronRight size={16} />
            </button>
          ))}
        </section>

        <section className="panel pulse-panel">
          <div className="panel-head">
            <div>
              <p className="eyebrow blue">PORTFÖY NABZI</p>
              <h3>Haftalık ilerleme</h3>
            </div>
            <span className="live-dot">CANLI</span>
          </div>
          <div className="pulse-bars">
            {trend.map((t, i) => (
              <span
                key={t.hafta}
                title={`${formatDate(t.hafta)} haftası: ${t.ortalamaIlerleme == null ? 'rapor yok' : '%' + t.ortalamaIlerleme}`}
                style={{ height: `${t.ortalamaIlerleme ?? 0}%` }}
                className={t.ortalamaIlerleme == null ? 'missing' : i >= trend.length - 3 ? 'current' : ''}
              />
            ))}
          </div>
          <div className="pulse-labels">
            <span>{monthLabel(trend[0].hafta)}</span>
            <strong>%{lastPoint?.ortalamaIlerleme ?? 0} <small>son hafta ortalaması</small></strong>
            <span>{monthLabel(trend[trend.length - 1].hafta)}</span>
          </div>
        </section>
      </div>

      <ProjectTable
        projects={projects}
        title="Proje görünümü"
        action={<Link className="text-button" to="/projeler">Tüm projeler <ChevronRight size={15} /></Link>}
      />
    </>
  )
}
