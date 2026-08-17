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
  calculatePlotterTotalPreview,
  formatPlotterMoney,
  formatPlotterNumber,
} from '../presentation/plotterJobPresentation'

function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const EMPTY_FORM = {
  customerId: '',
  orderId: '',
  creationDate: '',
  paperInventoryItemId: '',
  printedMeters: '',
  pricePerMeter: '',
  observations: '',
}

function CreatePlotterJobDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  customers,
  orders,
  paperRolls,
  loadingLookups,
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
      creationDate: toIsoDate(),
    })
  }, [open])

  const totalPreview = useMemo(
    () => calculatePlotterTotalPreview(form.printedMeters, form.pricePerMeter),
    [form.printedMeters, form.pricePerMeter]
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

    const customerId = form.customerId
    const orderId = form.orderId || null
    const creationDate = form.creationDate
    const paperInventoryItemId = form.paperInventoryItemId
    const printedMetersRaw = form.printedMeters.trim()
    const pricePerMeterRaw = form.pricePerMeter.trim()
    const observations = form.observations.trim()

    if (!customerId || !paperInventoryItemId || !printedMetersRaw || !pricePerMeterRaw || !creationDate) {
      setValidationError(
        'Cliente, fecha, rollo de papel, metros impresos y precio por metro son obligatorios.'
      )
      return
    }

    const printedMeters = Number(printedMetersRaw)
    const pricePerMeter = Number(pricePerMeterRaw)

    if (Number.isNaN(printedMeters) || printedMeters <= 0) {
      setValidationError('Los metros impresos deben ser un número mayor que cero.')
      return
    }

    if (Number.isNaN(pricePerMeter) || pricePerMeter < 0) {
      setValidationError('El precio por metro debe ser un número mayor o igual a cero.')
      return
    }

    const selectedRoll = (paperRolls || []).find(
      (roll) => roll.inventoryItemId === paperInventoryItemId
    )
    const available = Number(selectedRoll?.stock)

    if (selectedRoll && !Number.isNaN(available) && printedMeters > available) {
      setValidationError(
        `Los metros impresos exceden el stock disponible del rollo (${formatPlotterNumber(available)} m).`
      )
      return
    }

    setValidationError('')
    onSubmit({
      customerId,
      orderId,
      creationDate,
      paperInventoryItemId,
      printedMeters,
      pricePerMeter,
      observations: observations || null,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Nuevo trabajo de plotter</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          <TextField
            select
            label="Cliente"
            value={form.customerId}
            onChange={(event) => updateField('customerId', event.target.value)}
            fullWidth
            disabled={submitting || loadingLookups}
            autoFocus
          >
            {(customers || []).map((customer) => (
              <MenuItem key={customer.customerId} value={customer.customerId}>
                {customer.name}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            select
            label="Orden comercial"
            value={form.orderId}
            onChange={(event) => updateField('orderId', event.target.value)}
            fullWidth
            disabled={submitting || loadingLookups}
            helperText="Opcional. Atribuye este trabajo a una orden comercial sin crear un ingreso automático."
          >
            <MenuItem value="">Sin orden asociada</MenuItem>
            {(orders || []).map((order) => (
              <MenuItem key={order.orderId} value={order.orderId}>
                {order.orderNumber}
                {order.customerName ? ` — ${order.customerName}` : ''}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Fecha del trabajo"
            type="date"
            value={form.creationDate}
            onChange={(event) => updateField('creationDate', event.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
            disabled={submitting}
          />

          <TextField
            select
            label="Seleccionar rollo de papel"
            value={form.paperInventoryItemId}
            onChange={(event) => updateField('paperInventoryItemId', event.target.value)}
            fullWidth
            disabled={submitting || loadingLookups}
            helperText={
              (paperRolls || []).length === 0
                ? 'No hay rollos de papel Plotter disponibles.'
                : 'Solo se listan rollos elegibles (PAPER / METER).'
            }
          >
            {(paperRolls || []).map((roll) => (
              <MenuItem key={roll.inventoryItemId} value={roll.inventoryItemId}>
                {roll.paperRollNumber || roll.materialCode} —{' '}
                {formatPlotterNumber(roll.stock)} m disponibles
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Metros impresos"
            value={form.printedMeters}
            onChange={(event) => updateField('printedMeters', event.target.value)}
            fullWidth
            disabled={submitting}
            inputProps={{ inputMode: 'decimal' }}
          />

          <TextField
            label="Precio por metro"
            value={form.pricePerMeter}
            onChange={(event) => updateField('pricePerMeter', event.target.value)}
            fullWidth
            disabled={submitting}
            inputProps={{ inputMode: 'decimal' }}
          />

          <Stack spacing={0.5}>
            <Typography variant="body2" color="text.secondary">
              Total cobrado (vista previa)
            </Typography>
            <Typography variant="h6">
              {totalPreview === null ? '—' : formatPlotterMoney(totalPreview)}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              El total definitivo lo calcula el servidor.
            </Typography>
          </Stack>

          <TextField
            label="Observaciones"
            value={form.observations}
            onChange={(event) => updateField('observations', event.target.value)}
            fullWidth
            disabled={submitting}
            multiline
            minRows={2}
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={submitting || loadingLookups}
        >
          {submitting ? 'Guardando…' : 'Registrar trabajo'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreatePlotterJobDialog
