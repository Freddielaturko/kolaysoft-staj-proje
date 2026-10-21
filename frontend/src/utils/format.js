export const ROLE_LABELS = {
  ADMIN: 'Admin',
  CTO: 'CTO',
  PM: 'Proje Yöneticisi',
  LEAD: 'Ekip Lideri',
}

export const HEALTH = {
  NORMAL: { label: 'Normal', tone: 'normal' },
  DIKKAT: { label: 'Dikkat', tone: 'attention' },
  KRITIK: { label: 'Kritik', tone: 'critical' },
  ARSIV: { label: 'Arşiv', tone: 'archive' },
}

export const PROJECT_STATUS = {
  AKTIF: 'Aktif',
  BEKLEMEDE: 'Beklemede',
  TAMAMLANDI: 'Tamamlandı',
  IPTAL: 'İptal',
  ARSIV: 'Arşiv',
}

export const GENEL_DURUM = {
  YESIL: { label: 'Yeşil', tone: 'normal' },
  SARI: { label: 'Sarı', tone: 'attention' },
  KIRMIZI: { label: 'Kırmızı', tone: 'critical' },
}

export const RISK = {
  DUSUK: 'Düşük',
  ORTA: 'Orta',
  YUKSEK: 'Yüksek',
}

export const TASK_STATUS = {
  BEKLEMEDE: 'Beklemede',
  DEVAM_EDIYOR: 'Devam ediyor',
  TAMAMLANDI: 'Tamamlandı',
  IPTAL: 'İptal',
}

const moneyFormat = new Intl.NumberFormat('tr-TR', {
  style: 'currency',
  currency: 'TRY',
  maximumFractionDigits: 0,
})

export function money(value) {
  return value == null ? '—' : moneyFormat.format(value)
}

// Backend LocalDate'i "2026-10-29" olarak donuyor; saat dilimi kaymasin diye parcalayip kuruyorum
export function parseDate(iso) {
  if (!iso) return null
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, m - 1, d)
}

export function formatDate(iso, options = { day: '2-digit', month: 'short', year: 'numeric' }) {
  const date = parseDate(iso)
  return date ? date.toLocaleDateString('tr-TR', options) : '—'
}

export function daysUntil(iso) {
  const date = parseDate(iso)
  if (!date) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return Math.round((date - today) / 86400000)
}

export function toIsoDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function mondayOf(date = new Date()) {
  const d = new Date(date)
  const day = d.getDay()
  d.setDate(d.getDate() - (day === 0 ? 6 : day - 1))
  return toIsoDate(d)
}

export function initials(name = '') {
  return name
    .split(' ')
    .filter(Boolean)
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
    .toLocaleUpperCase('tr-TR')
}

export function errorMessage(err, fallback = 'İşlem tamamlanamadı') {
  const data = err?.response?.data
  if (data?.alanlar) return Object.values(data.alanlar).join(', ')
  return data?.error || fallback
}

export function canWrite(user) {
  return user && (user.rol === 'PM' || user.rol === 'LEAD' || user.rol === 'ADMIN')
}
