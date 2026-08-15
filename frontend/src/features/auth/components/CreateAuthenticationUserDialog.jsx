import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from '@mui/material'

const EMPTY_FORM = {
  username: '',
  password: '',
  role: 'OPERATOR',
}

function CreateAuthenticationUserDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
}) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setForm(EMPTY_FORM)
      setValidationError('')
    }
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    setValidationError('')
  }

  function handleSubmit() {
    if (!form.username.trim()) {
      setValidationError('El usuario es obligatorio.')
      return
    }
    if (!form.password) {
      setValidationError('La contraseña es obligatoria.')
      return
    }
    if (form.password.length < 8) {
      setValidationError('La contraseña debe tener al menos 8 caracteres.')
      return
    }

    onSubmit({
      username: form.username.trim(),
      password: form.password,
      role: form.role,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Nuevo usuario</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(errorMessage || validationError) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          <TextField
            label="Usuario"
            value={form.username}
            onChange={(event) => updateField('username', event.target.value)}
            fullWidth
            disabled={submitting}
            autoFocus
          />
          <TextField
            label="Contraseña"
            type="password"
            value={form.password}
            onChange={(event) => updateField('password', event.target.value)}
            fullWidth
            disabled={submitting}
            autoComplete="new-password"
          />
          <TextField
            select
            label="Rol"
            value={form.role}
            onChange={(event) => updateField('role', event.target.value)}
            fullWidth
            disabled={submitting}
          >
            <MenuItem value="OPERATOR">Operador</MenuItem>
            <MenuItem value="ADMIN">Administrador</MenuItem>
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          type="button"
          variant="contained"
          onClick={handleSubmit}
          disabled={submitting}
        >
          Crear
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreateAuthenticationUserDialog
