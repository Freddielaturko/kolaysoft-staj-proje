import apiClient from './client'

export async function getSummary() {
  const { data } = await apiClient.get('/dashboard/summary')
  return data
}

export async function listAlerts() {
  const { data } = await apiClient.get('/alerts')
  return data
}
