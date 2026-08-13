import apiClient from './client'

export async function listUsers() {
  const { data } = await apiClient.get('/admin/users')
  return data
}

export async function createUser(payload) {
  const { data } = await apiClient.post('/admin/users', payload)
  return data
}
