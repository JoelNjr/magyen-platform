import { useCallback, useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { AuthContext } from './AuthContext'
import { getAuthenticatedUser, login as requestLogin } from './services/authService'
import {
  clearAuthenticationState,
  getAccessToken,
  getStoredIdentity,
  persistAuthentication,
} from './services/authStorage'
import {
  resetUnauthorizedRedirect,
  setUnauthorizedHandler,
} from './services/unauthorizedHandler'
import { resolveSafeInternalPath } from './presentation/authPresentation'

function toIdentity(source) {
  if (!source) {
    return null
  }

  return {
    userId: source.userId,
    username: source.username,
    role: source.role,
  }
}

export function AuthProvider({ children }) {
  const navigate = useNavigate()
  const location = useLocation()
  const [status, setStatus] = useState('initializing')
  const [identity, setIdentity] = useState(null)

  const logout = useCallback(() => {
    clearAuthenticationState()
    resetUnauthorizedRedirect()
    setIdentity(null)
    setStatus('unauthenticated')
    if (location.pathname !== '/login') {
      navigate('/login', { replace: true })
    }
  }, [location.pathname, navigate])

  useEffect(() => {
    setUnauthorizedHandler(() => {
      clearAuthenticationState()
      setIdentity(null)
      setStatus('unauthenticated')
      if (location.pathname !== '/login') {
        navigate('/login', {
          replace: true,
          state: { from: `${location.pathname}${location.search}` },
        })
      }
    })

    return () => setUnauthorizedHandler(null)
  }, [location.pathname, location.search, navigate])

  useEffect(() => {
    let cancelled = false

    async function restoreSession() {
      const accessToken = getAccessToken()
      if (!accessToken) {
        if (!cancelled) {
          setIdentity(null)
          setStatus('unauthenticated')
        }
        return
      }

      try {
        const authenticatedUser = await getAuthenticatedUser()
        if (cancelled) {
          return
        }

        const restoredIdentity = toIdentity(authenticatedUser)
        persistAuthentication({
          accessToken,
          ...restoredIdentity,
        })
        setIdentity(restoredIdentity)
        setStatus('authenticated')
        resetUnauthorizedRedirect()
      } catch (error) {
        if (cancelled) {
          return
        }

        if (error?.response?.status === 401) {
          clearAuthenticationState()
          setIdentity(null)
          setStatus('unauthenticated')
          return
        }

        const storedIdentity = getStoredIdentity()
        if (storedIdentity) {
          setIdentity(storedIdentity)
          setStatus('authenticated')
          return
        }

        setIdentity(null)
        setStatus('unauthenticated')
      }
    }

    restoreSession()

    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback(
    async ({ username, password }) => {
      const result = await requestLogin({ username, password })
      const nextIdentity = toIdentity(result)

      persistAuthentication({
        accessToken: result.accessToken,
        ...nextIdentity,
      })
      setIdentity(nextIdentity)
      setStatus('authenticated')
      resetUnauthorizedRedirect()

      const destination = resolveSafeInternalPath(location.state?.from, nextIdentity)
      navigate(destination, { replace: true })
    },
    [location.state, navigate]
  )

  const refreshIdentity = useCallback(async () => {
    const accessToken = getAccessToken()
    if (!accessToken) {
      return
    }

    const authenticatedUser = await getAuthenticatedUser()
    const nextIdentity = toIdentity(authenticatedUser)
    persistAuthentication({
      accessToken,
      ...nextIdentity,
    })
    setIdentity(nextIdentity)
    setStatus('authenticated')
  }, [])

  const value = useMemo(
    () => ({
      status,
      identity,
      isAuthenticated: status === 'authenticated',
      login,
      logout,
      refreshIdentity,
    }),
    [identity, login, logout, refreshIdentity, status]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
