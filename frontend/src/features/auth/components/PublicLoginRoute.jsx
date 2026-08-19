import { Navigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import { resolveDefaultAuthenticatedPath } from '../presentation/authPresentation'
import AuthLoadingScreen from './AuthLoadingScreen'
import LoginPage from '../pages/LoginPage'

function PublicLoginRoute() {
  const { status, identity } = useAuth()

  if (status === 'initializing') {
    return <AuthLoadingScreen message="Verificando sesión..." />
  }

  if (status === 'authenticated') {
    return <Navigate to={resolveDefaultAuthenticatedPath(identity)} replace />
  }

  return <LoginPage />
}

export default PublicLoginRoute
