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
  Typography,
} from '@mui/material'
import { formatInventoryNumber } from '../presentation/inventoryStatusPresentation'

function UpdateInventoryMinimumStockDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  currentMinimumStock,
}) {
  const [minimumStock, setMinimumStock] = useState('')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setMinimumStock('')
      setValidationError('')
      return
    }

    setMinimumStock(
      currentMinimumStock === null || currentMinimumStock === undefined
        ? ''
        : String(currentMinimumStock)
    )
    setValidationError('')
  }, [open, currentMinimumStock])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    const trimmed = minimumStock.trim()

    if (trimmed === '') {
      setValidationError('')
      onSubmit(null)
      return
    }

    const parsed = Number(trimmed)

    if (Number.isNaN(parsed)) {
      setValidationError('El stock mínimo debe ser un número válido.')
      return
    }

    if (parsed < 0) {
      setValidationError('El stock mínimo no puede ser negativo.')
      return
    }

    setValidationError('')
    onSubmit(parsed)
  }

  const currentLabel =
    currentMinimumStock === null || currentMinimumStock === undefined
      ? 'No configurado'
      : formatInventoryNumber(currentMinimumStock)

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Configurar stock mínimo</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          <Typography variant="body2" color="text.secondary">
            Stock mínimo actual: {currentLabel}
          </Typography>

          <TextField
            label="Stock mínimo"
            value={minimumStock}
            onChange={(event) => {
              setMinimumStock(event.target.value)
              setValidationError('')
            }}
            fullWidth
            disabled={submitting}
            autoFocus
            helperText="Cero es válido. Dejar vacío deshabilita el monitoreo."
            inputProps={{ inputMode: 'decimal' }}
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
          {submitting ? 'Guardando...' : 'Guardar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default UpdateInventoryMinimumStockDialog
