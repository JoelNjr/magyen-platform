import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material'

function CreateSellerDialog({ open, onClose, onCreated, submitting, error }) {
  const [name, setName] = useState('')
  const [nameError, setNameError] = useState(false)

  useEffect(() => {
    if (!open) {
      setName('')
      setNameError(false)
    }
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    const trimmedName = name.trim()

    if (!trimmedName) {
      setNameError(true)
      return
    }

    onCreated(trimmedName)
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Nuevo vendedor</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {error && (
            <Alert severity="error">
              No fue posible crear el vendedor.
            </Alert>
          )}

          <TextField
            label="Nombre del vendedor"
            value={name}
            onChange={(event) => {
              setName(event.target.value)
              setNameError(false)
            }}
            fullWidth
            disabled={submitting}
            error={nameError}
            helperText={
              nameError
                ? 'El nombre del vendedor es obligatorio.'
                : 'Persona interna de Magyen. Quedará asociada a las cotizaciones y órdenes.'
            }
            autoFocus
          />
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
          {submitting ? 'Creando...' : 'Crear vendedor'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreateSellerDialog
