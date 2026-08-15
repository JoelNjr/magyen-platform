import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import AuthLoadingScreen from './AuthLoadingScreen'

function ProtectedRoute({ children }) {
  const { status } = useAuth()
  const location = useLocation()

  if (status === 'initializing') {
    return <AuthLoadingScreen message="Verificando sesión..." />
  }

  if (status !== 'authenticated') {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: `${location.pathname}${location.search}` }}
      />
    )
  }

  return children
}

export default ProtectedRoute
