import { useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import MagyenLogo from '../../../layout/MagyenLogo'
import { magyenColors } from '../../../theme/magyenColors'
import { loginEntranceAnimation } from '../../../theme/magyenMotion'
import { useAuth } from '../AuthContext'
import { resolveLoginErrorMessage } from '../presentation/authPresentation'

function LoginPage() {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    setErrorMessage('')
    setSubmitting(true)

    try {
      await login({ username: username.trim(), password })
    } catch (error) {
      setErrorMessage(resolveLoginErrorMessage(error))
    } finally {
      setPassword('')
      setSubmitting(false)
    }
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: { xs: 2, sm: 3 },
        bgcolor: magyenColors.charcoal.main,
      }}
    >
      <Paper
        variant="outlined"
        sx={{
          width: '100%',
          maxWidth: 420,
          p: { xs: 3, sm: 4 },
          borderTop: `3px solid ${magyenColors.gold.main}`,
          boxShadow: '0 12px 32px rgba(0, 0, 0, 0.28)',
          ...loginEntranceAnimation,
        }}
      >
        <Stack spacing={3} component="form" onSubmit={handleSubmit}>
          <Stack spacing={1.5} alignItems="center" textAlign="center">
            <MagyenLogo size={88} />
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700 }}>
                Magyen
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Confecciones Magyen
              </Typography>
            </Box>
            <Box
              sx={{
                width: 48,
                height: 3,
                bgcolor: 'primary.main',
                borderRadius: 1,
              }}
            />
            <Typography variant="h6">Iniciar sesión</Typography>
          </Stack>

          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

          <TextField
            label="Usuario"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            autoComplete="username"
            autoFocus
            fullWidth
            disabled={submitting}
          />

          <TextField
            label="Contraseña"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
            fullWidth
            disabled={submitting}
          />

          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? 'Iniciando sesión...' : 'Iniciar sesión'}
          </Button>
        </Stack>
      </Paper>
    </Box>
  )
}

export default LoginPage
