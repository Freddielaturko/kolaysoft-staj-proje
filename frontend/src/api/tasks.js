import apiClient from './client'

export async function listTasks(projectId) {
  const { data } = await apiClient.get(`/projects/${projectId}/tasks`)
  return data
}

export async function createTask(projectId, payload) {
  const { data } = await apiClient.post(`/projects/${projectId}/tasks`, payload)
  return data
}

export async function updateTask(projectId, taskId, payload) {
  const { data } = await apiClient.put(`/projects/${projectId}/tasks/${taskId}`, payload)
  return data
}
