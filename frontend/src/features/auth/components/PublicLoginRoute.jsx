import { Navigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import AuthLoadingScreen from './AuthLoadingScreen'
import LoginPage from '../pages/LoginPage'

function PublicLoginRoute() {
  const { status } = useAuth()

  if (status === 'initializing') {
    return <AuthLoadingScreen message="Verificando sesión..." />
  }

  if (status === 'authenticated') {
    return <Navigate to="/home" replace />
  }

  return <LoginPage />
}

export default PublicLoginRoute
