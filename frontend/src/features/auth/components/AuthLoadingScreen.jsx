import { Box, CircularProgress, Typography } from '@mui/material'

function AuthLoadingScreen({ message = 'Cargando...' }) {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
      }}
    >
      <CircularProgress />
      <Typography color="text.secondary">{message}</Typography>
    </Box>
  )
}

export default AuthLoadingScreen
