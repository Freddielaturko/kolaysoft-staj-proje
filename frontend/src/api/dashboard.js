import apiClient from './client'

export async function getCtoDashboard(filters = {}) {
  const params = {}
  if (filters.projectId) params.projectId = filters.projectId
  if (filters.raporHaftasi) params.raporHaftasi = filters.raporHaftasi
  if (filters.genelDurum) params.genelDurum = filters.genelDurum
  if (filters.riskSeviyesi) params.riskSeviyesi = filters.riskSeviyesi

  const { data } = await apiClient.get('/dashboard/cto', { params })
  return data
}
