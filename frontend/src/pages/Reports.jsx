import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Plus, SlidersHorizontal } from 'lucide-react'
import { listAllReports } from '../api/weeklyReports'
import { useAuth } from '../context/AuthContext'
import { usePortfolio } from '../context/PortfolioContext'
import Badge from '../components/Badge'
import Modal from '../components/Modal'
import ReportForm from '../components/ReportForm'
import ReportView from '../components/ReportView'
import { GENEL_DURUM, RISK, canWrite, errorMessage, formatDate } from '../utils/format'

export default function Reports() {
  const { user } = useAuth()
  const { projects, reload: reloadPortfolio } = usePortfolio()
  const [params, setParams] = useSearchParams()
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [projectFilter, setProjectFilter] = useState('all')
  const [statusFilter, setStatusFilter] = useState('all')
  const [selected, setSelected] = useState(null)
  const [editing, setEditing] = useState(null)

  const writable = canWrite(user)
  // Rapor yalnizca devam eden projelere girilir
  const reportable = projects.filter((p) => p.durum === 'AKTIF' || p.durum === 'BEKLEMEDE')
  const formOpen = writable && (params.get('yeni') === '1' || editing)
  const late = projects.filter((p) => p.raporGecikti)

  const load = () =>
    listAllReports()
      .then(setReports)
      .catch((err) => setError(errorMessage(err, 'Raporlar yüklenemedi')))
      .finally(() => setLoading(false))

  useEffect(() => {
    load()
  }, [])

  function closeForm() {
    setEditing(null)
    setParams({})
  }

  async function afterSave() {
    closeForm()
    await Promise.all([load(), reloadPortfolio()])
  }

  const filtered = reports.filter(
    (r) => (projectFilter === 'all' || r.projectId === Number(projectFilter)) && (statusFilter === 'all' || r.genelDurum === statusFilter),
  )

  if (loading) return <div className="loading">Raporlar yükleniyor...</div>

  return (
    <>
      {error && <div className="page-error">{error}</div>}

      {late.length > 0 && (
        <div className="page-note">
          Geçen haftanın raporu girilmemiş projeler: {late.map((p) => p.ad).join(', ')}
        </div>
      )}

      <section className="panel project-panel">
        <div className="panel-head table-title">
          <div>
            <p className="eyebrow blue">RAPORLAR / {reports.length} KAYIT</p>
            <h3>Haftalık raporlar</h3>
          </div>
          {writable && reportable.length > 0 && (
            <button className="primary-button" onClick={() => setParams({ yeni: '1' })}><Plus size={16} /> Rapor gir</button>
          )}
        </div>

        <div className="filter-bar">
          <div className="select-wrap">
            <SlidersHorizontal size={15} />
            <select value={projectFilter} onChange={(e) => setProjectFilter(e.target.value)}>
              <option value="all">Tüm projeler</option>
              {projects.map((p) => <option key={p.id} value={p.id}>{p.kod} · {p.ad}</option>)}
            </select>
          </div>
          <div className="select-wrap">
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="all">Tüm genel durumlar</option>
              {Object.entries(GENEL_DURUM).map(([key, d]) => <option key={key} value={key}>{d.label}</option>)}
            </select>
          </div>
        </div>

        <div className="table-scroll">
          <table>
            <thead>
              <tr><th>HAFTA</th><th>PROJE</th><th>GENEL DURUM</th><th>RİSK</th><th>İLERLEME</th><th>HAZIRLAYAN</th><th>REVİZYON</th></tr>
            </thead>
            <tbody>
              {filtered.map((r) => {
                const durum = GENEL_DURUM[r.genelDurum]
                return (
                  <tr key={r.id} className="clickable" onClick={() => setSelected(r)}>
                    <td><span className="date">{formatDate(r.raporHaftasi)}</span></td>
                    <td>
                      <div className="project-name">
                        <span className={`project-dot ${durum.tone}`}></span>
                        <span><strong>{r.projectAd}</strong><small>{r.projectKod}</small></span>
                      </div>
                    </td>
                    <td><Badge tone={durum.tone}>{durum.label}</Badge></td>
                    <td>{RISK[r.riskSeviyesi]}</td>
                    <td>
                      <div className="progress-cell">
                        <span>%{r.gerceklesenIlerleme}</span>
                        <div><i style={{ width: `${r.gerceklesenIlerleme}%` }} className={durum.tone}></i></div>
                        <small>hedef %{r.hedeflenenIlerleme}</small>
                      </div>
                    </td>
                    <td>{r.olusturanUserAdSoyad}</td>
                    <td>{r.revizyon}</td>
                  </tr>
                )
              })}
            </tbody>
          </table>
          {filtered.length === 0 && <div className="empty">Gösterilecek rapor yok.</div>}
        </div>
      </section>

      {selected && (
        <Modal title={`${selected.projectAd}`} eyebrow={`${formatDate(selected.raporHaftasi)} HAFTASI`} onClose={() => setSelected(null)} wide>
          <ReportView
            report={selected}
            onEdit={writable ? () => { setEditing(selected); setSelected(null) } : null}
          />
        </Modal>
      )}

      {formOpen && (
        <Modal title={editing ? 'Raporu düzenle' : 'Haftalık rapor'} eyebrow="RAPOR GİRİŞİ" onClose={closeForm} wide>
          <ReportForm projects={editing ? projects : reportable} initialReport={editing} onSaved={afterSave} onCancel={closeForm} />
        </Modal>
      )}
    </>
  )
}
