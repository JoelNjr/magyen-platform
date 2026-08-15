import httpClient from '../../../services/httpClient'

export async function getAdminUsers() {
  const response = await httpClient.get('/admin/users')
  return response.data
}

export async function createAdminUser({ username, password, role }) {
  const response = await httpClient.post('/admin/users', {
    username,
    password,
    role,
  })
  return response.data
}

export async function activateAdminUser(userId) {
  const response = await httpClient.patch(`/admin/users/${userId}/activate`)
  return response.data
}

export async function deactivateAdminUser(userId) {
  const response = await httpClient.patch(`/admin/users/${userId}/deactivate`)
  return response.data
}

export async function changeAdminUserRole(userId, role) {
  const response = await httpClient.patch(`/admin/users/${userId}/role`, { role })
  return response.data
}
