import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { listProjects } from '../api/projects'
import { getSummary, listAlerts } from '../api/dashboard'

// Proje listesi, uyarilar ve ozet birden fazla ekranda (menu rozeti, bildirimler, dashboard) kullaniliyor.
// Tek yerden yukleyip rapor kaydi gibi islemlerden sonra reload() ile hepsini birlikte tazeliyorum.
const PortfolioContext = createContext(null)

export function PortfolioProvider({ children }) {
  const [projects, setProjects] = useState([])
  const [alerts, setAlerts] = useState([])
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const reload = useCallback(async () => {
    try {
      const [p, a, s] = await Promise.all([listProjects(), listAlerts(), getSummary()])
      setProjects(p)
      setAlerts(a)
      setSummary(s)
      setError('')
    } catch {
      setError('Veriler yüklenemedi. Sunucu uyanıyor olabilir, birkaç saniye sonra tekrar deneyin.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    reload()
  }, [reload])

  return (
    <PortfolioContext.Provider value={{ projects, alerts, summary, loading, error, reload }}>
      {children}
    </PortfolioContext.Provider>
  )
}

export function usePortfolio() {
  const ctx = useContext(PortfolioContext)
  if (!ctx) throw new Error('usePortfolio, PortfolioProvider icinde kullanilmali')
  return ctx
}
