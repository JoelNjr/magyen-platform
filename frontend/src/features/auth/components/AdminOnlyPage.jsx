import { Typography } from '@mui/material'
import { useAuth } from '../AuthContext'
import { isAdmin } from '../presentation/authPresentation'

function AdminOnlyPage({ children }) {
  const { identity } = useAuth()

  if (!isAdmin(identity)) {
    return (
      <Typography variant="h6" component="p">
        Sin permisos
      </Typography>
    )
  }

  return children
}

export default AdminOnlyPage
