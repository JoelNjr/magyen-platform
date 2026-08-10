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
import { formatUnitCostLabel } from '../presentation/inventoryStatusPresentation'

function UpdateInventoryUnitCostDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  currentUnitCost,
  unitOfMeasure,
}) {
  const [unitCost, setUnitCost] = useState('')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setUnitCost('')
      setValidationError('')
      return
    }

    setUnitCost(
      currentUnitCost === null || currentUnitCost === undefined
        ? ''
        : String(currentUnitCost)
    )
    setValidationError('')
  }, [open, currentUnitCost])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    const trimmed = unitCost.trim()

    if (trimmed === '') {
      setValidationError('')
      onSubmit(null)
      return
    }

    const parsed = Number(trimmed)

    if (Number.isNaN(parsed)) {
      setValidationError('El costo unitario debe ser un número válido.')
      return
    }

    if (parsed < 0) {
      setValidationError('El costo unitario no puede ser negativo.')
      return
    }

    setValidationError('')
    onSubmit(parsed)
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Configurar costo unitario</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          <Typography variant="body2" color="text.secondary">
            Costo unitario actual:{' '}
            {formatUnitCostLabel(currentUnitCost, unitOfMeasure)}
          </Typography>

          <TextField
            label="Costo unitario"
            value={unitCost}
            onChange={(event) => {
              setUnitCost(event.target.value)
              setValidationError('')
            }}
            fullWidth
            disabled={submitting}
            autoFocus
            helperText="Cero es válido. Dejar vacío indica que no está configurado."
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

export default UpdateInventoryUnitCostDialog
