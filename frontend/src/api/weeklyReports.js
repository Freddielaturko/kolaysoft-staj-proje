import apiClient from './client'

export async function listWeeklyReports(projectId) {
  const { data } = await apiClient.get(`/projects/${projectId}/weekly-reports`)
  return data
}

export async function saveWeeklyReport(projectId, payload) {
  const { data } = await apiClient.post(`/projects/${projectId}/weekly-reports`, payload)
  return data
}
