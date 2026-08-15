const ACCESS_TOKEN_KEY = 'magyen.auth.accessToken'
const IDENTITY_KEY = 'magyen.auth.identity'

function readJson(value) {
  if (!value) {
    return null
  }

  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

export function getAccessToken() {
  return sessionStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getStoredIdentity() {
  const identity = readJson(sessionStorage.getItem(IDENTITY_KEY))
  if (!identity || !identity.userId || !identity.username || !identity.role) {
    return null
  }

  return {
    userId: identity.userId,
    username: identity.username,
    role: identity.role,
  }
}

export function persistAuthentication({ accessToken, userId, username, role }) {
  if (!accessToken) {
    throw new Error('Access token is required')
  }

  sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  sessionStorage.setItem(
    IDENTITY_KEY,
    JSON.stringify({
      userId,
      username,
      role,
    })
  )
}

export function clearAuthenticationState() {
  sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  sessionStorage.removeItem(IDENTITY_KEY)
}

export function isLoginRequestUrl(url) {
  return typeof url === 'string' && url.includes('/auth/login')
}
