import { useEffect, useMemo, useState } from 'react'
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
  Typography,
} from '@mui/material'
import {
  INVENTORY_MOVEMENT_TYPE_OPTIONS,
  previewStockAfterMovement,
} from '../presentation/inventoryMovementPresentation'
import {
  formatStockWithUnit,
  formatUnitOfMeasureLabel,
} from '../presentation/inventoryStatusPresentation'

function RegisterInventoryMovementDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  currentStock,
  unitOfMeasure,
}) {
  const [movementType, setMovementType] = useState('IN')
  const [quantity, setQuantity] = useState('')
  const [observation, setObservation] = useState('')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setMovementType('IN')
      setQuantity('')
      setObservation('')
      setValidationError('')
    }
  }, [open])

  const previewStock = useMemo(() => {
    const trimmed = quantity.trim()

    if (!trimmed) {
      return null
    }

    const parsed = Number(trimmed)

    if (Number.isNaN(parsed) || parsed === 0) {
      return null
    }

    if ((movementType === 'IN' || movementType === 'OUT') && parsed < 0) {
      return null
    }

    return previewStockAfterMovement(currentStock, movementType, parsed)
  }, [currentStock, movementType, quantity])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    const trimmedQuantity = quantity.trim()
    const trimmedObservation = observation.trim()

    if (!movementType) {
      setValidationError('El tipo de movimiento es obligatorio.')
      return
    }

    if (!trimmedQuantity) {
      setValidationError('La cantidad es obligatoria.')
      return
    }

    const parsedQuantity = Number(trimmedQuantity)

    if (Number.isNaN(parsedQuantity)) {
      setValidationError('La cantidad debe ser un número válido.')
      return
    }

    if (parsedQuantity === 0) {
      setValidationError('La cantidad no puede ser cero.')
      return
    }

    if ((movementType === 'IN' || movementType === 'OUT') && parsedQuantity < 0) {
      setValidationError('Para entrada o salida la cantidad debe ser positiva.')
      return
    }

    setValidationError('')
    onSubmit({
      movementType,
      quantity: parsedQuantity,
      unitOfMeasure: unitOfMeasure || null,
      observation: trimmedObservation || null,
      sourceType: 'MANUAL',
      sourceId: null,
    })
  }

  const previewLabel =
    movementType === 'ADJUSTMENT'
      ? 'Stock después del ajuste'
      : 'Stock después del movimiento'

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Registrar movimiento</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          <TextField
            select
            label="Tipo"
            value={movementType}
            onChange={(event) => {
              setMovementType(event.target.value)
              setValidationError('')
            }}
            fullWidth
            disabled={submitting}
            autoFocus
          >
            {INVENTORY_MOVEMENT_TYPE_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Cantidad"
            value={quantity}
            onChange={(event) => {
              setQuantity(event.target.value)
              setValidationError('')
            }}
            fullWidth
            disabled={submitting}
            helperText={
              movementType === 'ADJUSTMENT'
                ? 'Para ajuste use un valor positivo o negativo. No se convierte automáticamente.'
                : 'Debe ser una cantidad positiva.'
            }
            inputProps={{ inputMode: 'decimal' }}
          />

          <TextField
            label="Unidad"
            value={
              unitOfMeasure
                ? `${formatUnitOfMeasureLabel(unitOfMeasure)} (${unitOfMeasure})`
                : '—'
            }
            fullWidth
            disabled
          />

          <TextField
            label="Observación"
            value={observation}
            onChange={(event) => {
              setObservation(event.target.value)
              setValidationError('')
            }}
            fullWidth
            disabled={submitting}
            multiline
            minRows={2}
          />

          <Typography variant="body2" color="text.secondary">
            Stock actual: {formatStockWithUnit(currentStock, unitOfMeasure)}
          </Typography>

          {previewStock !== null && (
            <Alert severity="info">
              {previewLabel}: {formatStockWithUnit(previewStock, unitOfMeasure)}
            </Alert>
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
          {submitting ? 'Guardando...' : 'Registrar movimiento'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default RegisterInventoryMovementDialog
