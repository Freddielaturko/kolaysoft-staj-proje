import apiClient from './client'

export async function listProjects() {
  const { data } = await apiClient.get('/projects')
  return data
}

export async function createProject(payload) {
  const { data } = await apiClient.post('/admin/projects', payload)
  return data
}
