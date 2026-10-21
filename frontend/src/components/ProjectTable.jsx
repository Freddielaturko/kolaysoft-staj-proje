import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronRight, Search, SlidersHorizontal } from 'lucide-react'
import Badge from './Badge'
import { HEALTH, formatDate } from '../utils/format'

export default function ProjectTable({ projects, title, action }) {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [health, setHealth] = useState('all')

  const q = query.toLocaleLowerCase('tr-TR')
  const filtered = projects.filter((p) => {
    const haystack = [p.ad, p.kod, p.sorumluPmAdSoyad, p.ekipLideriAdSoyad, p.departman]
      .filter(Boolean)
      .join(' ')
      .toLocaleLowerCase('tr-TR')
    return haystack.includes(q) && (health === 'all' || p.saglik === health)
  })

  return (
    <section className="panel project-panel">
      <div className="panel-head table-title">
        <div>
          <p className="eyebrow blue">PORTFÖY / {projects.length} PROJE</p>
          <h3>{title}</h3>
        </div>
        {action}
      </div>

      <div className="filter-bar">
        <div className="search">
          <Search size={16} />
          <input
            placeholder="Proje, yönetici veya departman ara..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
        <div className="select-wrap">
          <SlidersHorizontal size={15} />
          <select value={health} onChange={(e) => setHealth(e.target.value)}>
            <option value="all">Tüm durumlar</option>
            {Object.entries(HEALTH).map(([key, h]) => (
              <option key={key} value={key}>{h.label}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>PROJE</th>
              <th>DURUM</th>
              <th>İLERLEME</th>
              <th>TESLİM</th>
              <th>YÖNETİCİ</th>
              <th>SON RAPOR</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((p) => {
              const h = HEALTH[p.saglik] || HEALTH.NORMAL
              return (
                <tr key={p.id} className="clickable" onClick={() => navigate(`/projeler/${p.id}`)}>
                  <td>
                    <div className="project-name">
                      <span className={`project-dot ${h.tone}`}></span>
                      <span>
                        <strong>{p.ad}</strong>
                        <small>{p.kod}{p.departman && ` · ${p.departman}`}</small>
                      </span>
                    </div>
                  </td>
                  <td><Badge tone={h.tone}>{h.label}</Badge></td>
                  <td>
                    <div className="progress-cell">
                      <span>%{p.ilerleme}</span>
                      <div><i style={{ width: `${p.ilerleme}%` }} className={h.tone}></i></div>
                    </div>
                  </td>
                  <td>
                    <span className={p.saglik === 'KRITIK' ? 'late-date' : 'date'}>{formatDate(p.teslimTarihi)}</span>
                  </td>
                  <td>{p.sorumluPmAdSoyad}</td>
                  <td>
                    {p.sonRaporHaftasi ? formatDate(p.sonRaporHaftasi) : '—'}
                    {p.raporGecikti && <span className="late-tag"> · gecikti</span>}
                  </td>
                  <td><ChevronRight size={16} /></td>
                </tr>
              )
            })}
          </tbody>
        </table>
        {filtered.length === 0 && (
          <div className="empty">
            {projects.length === 0 ? 'Henüz görüntülenecek proje yok.' : 'Bu filtrelere uyan proje bulunamadı.'}
          </div>
        )}
      </div>
    </section>
  )
}
