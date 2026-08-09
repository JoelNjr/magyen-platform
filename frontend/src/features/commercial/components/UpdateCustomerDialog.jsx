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

function UpdateCustomerDialog({
  open,
  onClose,
  customer,
  onUpdated,
  submitting,
  error,
}) {
  const [name, setName] = useState('')
  const [nameError, setNameError] = useState(false)

  useEffect(() => {
    if (!open) {
      setName('')
      setNameError(false)
      return
    }

    setName(customer?.name || '')
    setNameError(false)
  }, [open, customer])

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

    onUpdated(trimmedName)
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Editar cliente</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {error && (
            <Alert severity="error">
              No fue posible actualizar el cliente.
            </Alert>
          )}

          <TextField
            label="Nombre del cliente"
            value={name}
            onChange={(event) => {
              setName(event.target.value)
              setNameError(false)
            }}
            fullWidth
            disabled={submitting}
            error={nameError}
            helperText={
              nameError ? 'El nombre del cliente es obligatorio.' : undefined
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
          {submitting ? 'Guardando...' : 'Guardar cambios'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default UpdateCustomerDialog
