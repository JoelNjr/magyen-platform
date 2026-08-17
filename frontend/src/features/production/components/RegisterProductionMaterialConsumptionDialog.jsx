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
import { formatInventoryConsumptionOption, formatStockWithUnit } from '../../inventory/presentation/inventoryStatusPresentation'

const EMPTY_FORM = {
  inventoryItemId: '',
  quantity: '',
  observation: '',
}

function RegisterProductionMaterialConsumptionDialog({
  open,
  inventoryItems,
  itemsLoading,
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
      return
    }

    setForm({
      ...EMPTY_FORM,
      inventoryItemId: inventoryItems[0]?.inventoryItemId || '',
    })
  }, [open, inventoryItems])

  const selectedItem = useMemo(
    () =>
      inventoryItems.find((item) => item.inventoryItemId === form.inventoryItemId) ||
      null,
    [inventoryItems, form.inventoryItemId]
  )

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
    if (submitting) {
      return
    }

    const quantity = Number(form.quantity)
    const unitOfMeasure = selectedItem?.unitOfMeasure

    if (!form.inventoryItemId || !unitOfMeasure || form.quantity === '') {
      setValidationError('Material y cantidad son obligatorios.')
      return
    }

    if (Number.isNaN(quantity) || quantity <= 0) {
      setValidationError('La cantidad debe ser mayor que cero.')
      return
    }

    const available = Number(selectedItem?.stock)
    if (!Number.isNaN(available) && quantity > available) {
      setValidationError(
        'No hay stock suficiente. El inventario no permite dejar el material en negativo.'
      )
      return
    }

    onSubmit({
      inventoryItemId: form.inventoryItemId,
      quantity,
      unitOfMeasure,
      observation: form.observation.trim() || null,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Registrar consumo</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          {!itemsLoading && inventoryItems.length === 0 ? (
            <Alert severity="warning">
              No hay materiales de inventario disponibles.
            </Alert>
          ) : null}
          <TextField
            select
            label="Material"
            value={form.inventoryItemId}
            onChange={(event) => updateField('inventoryItemId', event.target.value)}
            required
            fullWidth
            disabled={!inventoryItems.length || submitting}
          >
            {inventoryItems.map((item) => (
              <MenuItem key={item.inventoryItemId} value={item.inventoryItemId}>
                {formatInventoryConsumptionOption(item)}
              </MenuItem>
            ))}
          </TextField>
          {selectedItem ? (
            <Typography variant="body2" color="text.secondary">
              Stock disponible:{' '}
              {formatStockWithUnit(selectedItem.stock, selectedItem.unitOfMeasure)}
            </Typography>
          ) : null}
          <TextField
            label="Cantidad"
            value={form.quantity}
            onChange={(event) => updateField('quantity', event.target.value)}
            required
            fullWidth
            disabled={submitting}
            inputProps={{ inputMode: 'decimal' }}
            helperText={
              selectedItem
                ? `Unidad: ${selectedItem.unitOfMeasure}. El costo lo calcula el inventario.`
                : 'El costo lo calcula el inventario.'
            }
          />
          <TextField
            label="Observación"
            value={form.observation}
            onChange={(event) => updateField('observation', event.target.value)}
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
          disabled={submitting || !inventoryItems.length}
        >
          {submitting ? 'Registrando...' : 'Registrar consumo'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default RegisterProductionMaterialConsumptionDialog
