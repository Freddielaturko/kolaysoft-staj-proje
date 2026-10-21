import { Pencil } from 'lucide-react'
import Badge from './Badge'
import { GENEL_DURUM, RISK } from '../utils/format'

export default function ReportView({ report, onEdit }) {
  const durum = GENEL_DURUM[report.genelDurum]
  const rows = [
    ['Bu hafta yapılanlar', report.yapilanlar, true],
    ['Gelecek hafta yapılacaklar', report.yapilacaklar, true],
    ['Risk ve engeller', report.riskEngelNotu, true],
    ['Bütçe durumu', report.butceDurumu],
    ['Ekip durumu', report.ekipDurumu],
    ['Canlıdaki task', report.canliTask],
    ['Genel not', report.genelNot],
  ]

  return (
    <div className="report-view">
      <div className="timeline-meta" style={{ marginBottom: 20 }}>
        <Badge tone={durum.tone}>{durum.label}</Badge>
        <Badge tone="info">Risk: {RISK[report.riskSeviyesi]}</Badge>
        <span className="muted">
          Hedef %{report.hedeflenenIlerleme} · Gerçekleşen %{report.gerceklesenIlerleme} · Revizyon {report.revizyon}
        </span>
      </div>
      <dl>
        {rows.map(([label, value, full]) => (
          <div key={label} className={full ? 'full' : ''}>
            <dt>{label}</dt>
            <dd>{value || '—'}</dd>
          </div>
        ))}
        <div className="full">
          <dt>Hazırlayan</dt>
          <dd>{report.olusturanUserAdSoyad}</dd>
        </div>
      </dl>
      {onEdit && (
        <div className="form-actions">
          <button className="secondary-button" onClick={onEdit}><Pencil size={14} /> Raporu düzenle</button>
        </div>
      )}
    </div>
  )
}
