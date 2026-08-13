import apiClient from './client'

export async function login(email, sifre) {
  const { data } = await apiClient.post('/auth/login', { email, sifre })
  return data
}
