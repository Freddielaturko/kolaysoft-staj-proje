import apiClient from './client'

export async function listProjects() {
  const { data } = await apiClient.get('/projects')
  return data
}

export async function getProject(id) {
  const { data } = await apiClient.get(`/projects/${id}`)
  return data
}

export async function createProject(payload) {
  const { data } = await apiClient.post('/admin/projects', payload)
  return data
}

export async function updateProject(id, payload) {
  const { data } = await apiClient.put(`/admin/projects/${id}`, payload)
  return data
}
