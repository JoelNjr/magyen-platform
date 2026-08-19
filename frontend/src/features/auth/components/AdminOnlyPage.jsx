import { Navigate } from 'react-router-dom'
import { Typography } from '@mui/material'
import { useAuth } from '../AuthContext'
import { isAdmin } from '../presentation/authPresentation'

function AdminOnlyPage({ children, redirectTo }) {
  const { identity } = useAuth()

  if (!isAdmin(identity)) {
    if (typeof redirectTo === 'string' && redirectTo.startsWith('/')) {
      return <Navigate to={redirectTo} replace />
    }

    return (
      <Typography variant="h6" component="p">
        Sin permisos
      </Typography>
    )
  }

  return children
}

export default AdminOnlyPage
