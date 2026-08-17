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
  formatPlotterJobTypeLabel,
  formatPlotterMoney,
  formatPlotterNumber,
  formatPlotterOrderLabel,
  isInternalPlotterJob,
} from '../presentation/plotterJobPresentation'

function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function createPlotterJobId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `plotter-${Date.now()}`
}

const EMPTY_FORM = {
  jobType: '',
  customerId: '',
  orderId: '',
  creationDate: '',
  paperInventoryItemId: '',
  printedMeters: '',
  pricePerMeter: '',
  observations: '',
  plotterJobId: '',
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
      plotterJobId: createPlotterJobId(),
    })
  }, [open])

  const selectedOrder = useMemo(
    () => (orders || []).find((order) => order.orderId === form.orderId) || null,
    [orders, form.orderId]
  )

  const selectedRoll = useMemo(
    () =>
      (paperRolls || []).find((roll) => roll.inventoryItemId === form.paperInventoryItemId) ||
      null,
    [paperRolls, form.paperInventoryItemId]
  )

  const isInternal = isInternalPlotterJob(form.jobType)
  const isExternal = form.jobType === 'EXTERNAL'

  const salePreview = useMemo(
    () => calculatePlotterTotalPreview(form.printedMeters, form.pricePerMeter),
    [form.printedMeters, form.pricePerMeter]
  )

  const materialCostPreview = useMemo(() => {
    const meters = Number(form.printedMeters)
    const unitCost = Number(selectedRoll?.unitCost)
    if (Number.isNaN(meters) || meters <= 0 || Number.isNaN(unitCost) || unitCost < 0) {
      return null
    }
    return meters * unitCost
  }, [form.printedMeters, selectedRoll])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  function updateField(field, value) {
    setForm((current) => {
      const next = { ...current, [field]: value }
      if (field === 'jobType') {
        next.customerId = ''
        next.orderId = ''
        next.pricePerMeter = ''
      }
      if (field === 'orderId') {
        const order = (orders || []).find((item) => item.orderId === value)
        next.customerId = order?.customerId || ''
      }
      return next
    })
    setValidationError('')
  }

  function handleSubmit() {
    if (submitting) {
      return
    }

    const jobType = form.jobType
    const creationDate = form.creationDate
    const paperInventoryItemId = form.paperInventoryItemId
    const printedMetersRaw = form.printedMeters.trim()
    const observations = form.observations.trim()

    if (!jobType || !paperInventoryItemId || !printedMetersRaw || !creationDate) {
      setValidationError(
        'Tipo de trabajo, fecha, rollo de papel y metros impresos son obligatorios.'
      )
      return
    }

    if (isInternal && !form.orderId) {
      setValidationError('Selecciona la orden comercial de Magyen.')
      return
    }

    if (isExternal && !form.customerId) {
      setValidationError('Selecciona el cliente externo.')
      return
    }

    const printedMeters = Number(printedMetersRaw)
    if (Number.isNaN(printedMeters) || printedMeters <= 0) {
      setValidationError('Los metros impresos deben ser un número mayor que cero.')
      return
    }

    let pricePerMeter = null
    if (isExternal) {
      const pricePerMeterRaw = form.pricePerMeter.trim()
      if (!pricePerMeterRaw) {
        setValidationError('El precio por metro es obligatorio para un cliente externo.')
        return
      }
      pricePerMeter = Number(pricePerMeterRaw)
      if (Number.isNaN(pricePerMeter) || pricePerMeter < 0) {
        setValidationError('El precio por metro debe ser un número mayor o igual a cero.')
        return
      }
    }

    const available = Number(selectedRoll?.stock)
    if (selectedRoll && !Number.isNaN(available) && printedMeters > available) {
      setValidationError(
        `Los metros impresos exceden el stock disponible del rollo (${formatPlotterNumber(available)} m).`
      )
      return
    }

    setValidationError('')
    onSubmit({
      jobType,
      plotterJobId: form.plotterJobId || createPlotterJobId(),
      customerId: isExternal ? form.customerId : undefined,
      orderId: isInternal ? form.orderId : undefined,
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
            label="Tipo de trabajo"
            value={form.jobType}
            onChange={(event) => updateField('jobType', event.target.value)}
            fullWidth
            disabled={submitting}
            autoFocus
            helperText="Elige si este trabajo es para una orden de Magyen o para un cliente externo."
          >
            <MenuItem value="INTERNAL_MAGYEN">
              {formatPlotterJobTypeLabel('INTERNAL_MAGYEN')}
            </MenuItem>
            <MenuItem value="EXTERNAL">{formatPlotterJobTypeLabel('EXTERNAL')}</MenuItem>
          </TextField>

          {isInternal && (
            <Alert severity="info">
              Este trabajo es para una orden de Magyen. El papel se registra una sola vez
              desde Plotter.
            </Alert>
          )}

          {isExternal && (
            <Alert severity="info">
              Este trabajo es para un cliente externo. No crea una orden comercial ni
              producción.
            </Alert>
          )}

          {isInternal && (
            <>
              <TextField
                select
                label="Orden comercial"
                value={form.orderId}
                onChange={(event) => updateField('orderId', event.target.value)}
                fullWidth
                disabled={submitting || loadingLookups}
              >
                {(orders || []).map((order) => (
                  <MenuItem key={order.orderId} value={order.orderId}>
                    {formatPlotterOrderLabel(order)}
                    {order.customerName ? ` — ${order.customerName}` : ''}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="Cliente"
                value={selectedOrder?.customerName || '—'}
                fullWidth
                InputProps={{ readOnly: true }}
                helperText="Se toma de la orden comercial."
              />
            </>
          )}

          {isExternal && (
            <TextField
              select
              label="Cliente"
              value={form.customerId}
              onChange={(event) => updateField('customerId', event.target.value)}
              fullWidth
              disabled={submitting || loadingLookups}
            >
              {(customers || []).map((customer) => (
                <MenuItem key={customer.customerId} value={customer.customerId}>
                  {customer.name}
                </MenuItem>
              ))}
            </TextField>
          )}

          {form.jobType && (
            <>
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
                label="Papel de inventario"
                value={form.paperInventoryItemId}
                onChange={(event) => updateField('paperInventoryItemId', event.target.value)}
                fullWidth
                disabled={submitting || loadingLookups}
                helperText={
                  (paperRolls || []).length === 0
                    ? 'No hay rollos de papel Plotter disponibles.'
                    : 'Selecciona el rollo real de inventario. Unidad: metro.'
                }
              >
                {(paperRolls || []).map((roll) => (
                  <MenuItem key={roll.inventoryItemId} value={roll.inventoryItemId}>
                    {roll.name || roll.paperRollNumber || roll.materialCode} —{' '}
                    {formatPlotterNumber(roll.stock)} m
                    {roll.unitCost != null && roll.unitCost !== ''
                      ? ` — ${formatPlotterMoney(roll.unitCost)}/m`
                      : ''}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                label="Cantidad (metros)"
                value={form.printedMeters}
                onChange={(event) => updateField('printedMeters', event.target.value)}
                fullWidth
                disabled={submitting}
                inputProps={{ inputMode: 'decimal' }}
              />

              {isExternal && (
                <TextField
                  label="Precio por metro"
                  value={form.pricePerMeter}
                  onChange={(event) => updateField('pricePerMeter', event.target.value)}
                  fullWidth
                  disabled={submitting}
                  inputProps={{ inputMode: 'decimal' }}
                />
              )}

              {isInternal && (
                <Stack spacing={0.5}>
                  <Typography variant="body2" color="text.secondary">
                    Costo de material (inventario)
                  </Typography>
                  <Typography variant="h6">
                    {materialCostPreview === null
                      ? 'Sin valoración histórica'
                      : formatPlotterMoney(materialCostPreview)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    No es una venta. El costo lo toma el inventario al consumir el papel.
                  </Typography>
                </Stack>
              )}

              {isExternal && (
                <Stack spacing={0.5}>
                  <Typography variant="body2" color="text.secondary">
                    Total cobrado (vista previa)
                  </Typography>
                  <Typography variant="h6">
                    {salePreview === null ? '—' : formatPlotterMoney(salePreview)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    El total definitivo lo calcula el servidor.
                  </Typography>
                </Stack>
              )}

              <TextField
                label="Observaciones"
                value={form.observations}
                onChange={(event) => updateField('observations', event.target.value)}
                fullWidth
                disabled={submitting}
                multiline
                minRows={2}
              />
            </>
          )}
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={submitting || loadingLookups || !form.jobType}
        >
          {submitting ? 'Guardando…' : 'Registrar trabajo'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreatePlotterJobDialog
