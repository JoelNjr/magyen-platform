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
  inventoryItemId: '',
  quantity: '',
  unitOfMeasure: 'METER',
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
    const unitOfMeasure = form.unitOfMeasure.trim()

    if (!form.inventoryItemId || !unitOfMeasure || form.quantity === '') {
      setValidationError('Material, cantidad y unidad son obligatorios.')
      return
    }

    if (Number.isNaN(quantity) || quantity <= 0) {
      setValidationError('La cantidad debe ser mayor que cero.')
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
      <DialogTitle>Registrar consumo de material</DialogTitle>
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
                {item.name || item.materialCode}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Cantidad"
            value={form.quantity}
            onChange={(event) => updateField('quantity', event.target.value)}
            required
            fullWidth
            disabled={submitting}
            inputProps={{ inputMode: 'decimal' }}
          />
          <TextField
            label="Unidad"
            value={form.unitOfMeasure}
            onChange={(event) => updateField('unitOfMeasure', event.target.value)}
            required
            fullWidth
            disabled={submitting}
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
