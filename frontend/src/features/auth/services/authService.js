import httpClient from '../../../services/httpClient'

export async function login({ username, password }) {
  const response = await httpClient.post('/auth/login', { username, password })
  return response.data
}

export async function getAuthenticatedUser() {
  const response = await httpClient.get('/auth/me')
  return response.data
}
