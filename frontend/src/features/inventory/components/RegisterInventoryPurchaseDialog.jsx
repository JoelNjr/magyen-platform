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
  formatStockWithUnit,
  formatUnitCostLabel,
} from '../presentation/inventoryStatusPresentation'

function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function newPurchaseId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function RegisterInventoryPurchaseDialog({
  open,
  items = [],
  lockedItem = null,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
}) {
  const [purchaseId, setPurchaseId] = useState('')
  const [inventoryItemId, setInventoryItemId] = useState('')
  const [quantity, setQuantity] = useState('')
  const [unitCost, setUnitCost] = useState('')
  const [purchaseDate, setPurchaseDate] = useState('')
  const [observation, setObservation] = useState('')
  const [validationError, setValidationError] = useState('')
  const selectableItems = useMemo(
    () => (lockedItem ? [lockedItem] : items),
    [lockedItem, items]
  )

  useEffect(() => {
    if (!open) {
      return
    }

    const selected = lockedItem ?? items[0] ?? null
    setPurchaseId(newPurchaseId())
    setInventoryItemId(selected?.inventoryItemId || '')
    setQuantity('')
    setUnitCost(selected?.unitCost != null ? String(selected.unitCost) : '')
    setPurchaseDate(toIsoDate())
    setObservation('')
    setValidationError('')
  }, [open, lockedItem, items])

  const selectedItem = useMemo(
    () =>
      selectableItems.find((item) => item.inventoryItemId === inventoryItemId) ||
      lockedItem ||
      null,
    [selectableItems, inventoryItemId, lockedItem]
  )

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  function handleSubmit() {
    const parsedQuantity = Number(quantity)
    const parsedUnitCost = Number(unitCost)

    if (!inventoryItemId || quantity === '' || unitCost === '' || !purchaseDate) {
      setValidationError('Material, cantidad, costo unitario y fecha son obligatorios.')
      return
    }

    if (Number.isNaN(parsedQuantity) || parsedQuantity <= 0) {
      setValidationError('La cantidad debe ser mayor que cero.')
      return
    }

    if (Number.isNaN(parsedUnitCost) || parsedUnitCost <= 0) {
      setValidationError('El costo unitario debe ser mayor que cero.')
      return
    }

    onSubmit({
      inventoryItemId,
      purchaseId,
      quantity: parsedQuantity,
      unitCost: parsedUnitCost,
      purchaseDate,
      observation: observation.trim() || null,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Registrar entrada de material</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          <Typography variant="body2" color="text.secondary">
            Esta entrada aumenta el stock y registra el gasto de la compra. El
            consumo posterior no vuelve a generar un gasto.
          </Typography>
          {selectableItems.length === 0 ? (
            <Alert severity="warning">
              Primero cree el material en inventario.
            </Alert>
          ) : (
            <TextField
              select
              label="Material"
              value={inventoryItemId}
              onChange={(event) => setInventoryItemId(event.target.value)}
              required
              fullWidth
              disabled={Boolean(lockedItem) || submitting}
            >
              {selectableItems.map((item) => (
                <MenuItem key={item.inventoryItemId} value={item.inventoryItemId}>
                  {item.name || item.materialCode} —{' '}
                  {formatStockWithUnit(item.stock, item.unitOfMeasure)}
                </MenuItem>
              ))}
            </TextField>
          )}
          {selectedItem ? (
            <Typography variant="body2" color="text.secondary">
              Stock actual:{' '}
              {formatStockWithUnit(selectedItem.stock, selectedItem.unitOfMeasure)}
              . Costo actual:{' '}
              {formatUnitCostLabel(
                selectedItem.unitCost,
                selectedItem.unitOfMeasure
              )}
              .
            </Typography>
          ) : null}
          <TextField
            label="Cantidad"
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
            required
            fullWidth
            disabled={submitting}
            inputProps={{ inputMode: 'decimal' }}
          />
          <TextField
            label="Costo unitario"
            value={unitCost}
            onChange={(event) => setUnitCost(event.target.value)}
            required
            fullWidth
            disabled={submitting}
            inputProps={{ inputMode: 'decimal' }}
            helperText="El total de la compra lo calcula el sistema."
          />
          <TextField
            label="Fecha"
            type="date"
            value={purchaseDate}
            onChange={(event) => setPurchaseDate(event.target.value)}
            InputLabelProps={{ shrink: true }}
            required
            fullWidth
            disabled={submitting}
          />
          <TextField
            label="Observaciones"
            value={observation}
            onChange={(event) => setObservation(event.target.value)}
            fullWidth
            multiline
            minRows={2}
            disabled={submitting}
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
          disabled={submitting || selectableItems.length === 0}
        >
          {submitting ? 'Registrando...' : 'Registrar entrada'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default RegisterInventoryPurchaseDialog
