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

function CreateCustomerDialog({
  open,
  onClose,
  onCreated,
  submitting,
  error,
  lockedCategory = '',
}) {
  const [name, setName] = useState('')
  const [nameError, setNameError] = useState(false)
  const [category, setCategory] = useState('')
  const [categoryError, setCategoryError] = useState(false)

  useEffect(() => {
    if (!open) {
      setName('')
      setNameError(false)
      setCategory('')
      setCategoryError(false)
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
    const resolvedCategory = lockedCategory || category

    if (!trimmedName) {
      setNameError(true)
    }

    if (!resolvedCategory) {
      setCategoryError(true)
    }

    if (!trimmedName || !resolvedCategory) {
      return
    }

    onCreated(trimmedName, resolvedCategory)
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Nuevo cliente</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {error && (
            <Alert severity="error">
              No fue posible crear el cliente.
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

          {!lockedCategory && (
            <TextField
              select
              label="Grupo"
              value={category}
              onChange={(event) => {
                setCategory(event.target.value)
                setCategoryError(false)
              }}
              fullWidth
              disabled={submitting}
              error={categoryError}
              helperText={
                categoryError
                  ? 'Selecciona si el cliente es Magyen o Plotter.'
                  : 'Magyen se usa en cotizaciones. Plotter se usa en trabajos externos.'
              }
            >
              <MenuItem value="MAGYEN">Cliente Magyen</MenuItem>
              <MenuItem value="PLOTTER">Cliente Plotter</MenuItem>
            </TextField>
          )}
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
          {submitting ? 'Creando...' : 'Crear cliente'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreateCustomerDialog
