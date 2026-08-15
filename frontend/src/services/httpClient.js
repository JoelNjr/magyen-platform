import axios from 'axios'
import {
  clearAuthenticationState,
  getAccessToken,
  isLoginRequestUrl,
} from '../features/auth/services/authStorage'
import { notifyUnauthorized } from '../features/auth/services/unauthorizedHandler'

const httpClient = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
})

httpClient.interceptors.request.use((config) => {
  if (isLoginRequestUrl(config.url)) {
    return config
  }

  const accessToken = getAccessToken()
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }

  return config
})

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const requestUrl = error.config?.url

    if (status === 401 && !isLoginRequestUrl(requestUrl)) {
      clearAuthenticationState()
      notifyUnauthorized()
    }

    return Promise.reject(error)
  }
)

export default httpClient
