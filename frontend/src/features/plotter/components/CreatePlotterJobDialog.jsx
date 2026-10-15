import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { customersForExternalPlotter } from '../../commercial/presentation/customerCategory'
import {
  calculatePlotterTotalPreview,
  formatCommercialOrderStatusLabel,
  formatHistoricalPlotterOrderOption,
  formatPlotterJobTypeLabel,
  formatPlotterMoney,
  formatPlotterNumber,
  formatPlotterOrderLabel,
  isInternalPlotterJob,
  isWastePlotterJob,
  parseHistoricalOrderMonth,
  plotterOrdersForRegistration,
  withHistoricalPlotterRegistration,
} from '../presentation/plotterJobPresentation'
import { getHistoricalCommercialOrdersForPlotter } from '../services/plotterService'

function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function historicalOrderHelperText(parsedMonth, loading, count) {
  if (!parsedMonth) {
    return 'Elige el mes de confirmación para ver pedidos entregados o cerrados.'
  }
  if (loading) {
    return 'Buscando pedidos de ese mes…'
  }
  if (count === 0) {
    return 'No hay pedidos entregados o cerrados en ese mes.'
  }
  return 'Pedidos entregados o cerrados. El estado comercial no cambia. Un pedido puede tener varios trabajos.'
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
  historicalRegistration: false,
  historicalMonth: '',
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
  createdCustomerId = '',
  onCreateCustomer,
}) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [validationError, setValidationError] = useState('')
  const [historicalOrders, setHistoricalOrders] = useState([])
  const [historicalLookupError, setHistoricalLookupError] = useState('')
  const [historicalLookupLoading, setHistoricalLookupLoading] = useState(false)

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
    setHistoricalOrders([])
    setHistoricalLookupError('')
    setHistoricalLookupLoading(false)
  }, [open])

  useEffect(() => {
    if (!createdCustomerId) {
      return
    }
    setForm((current) => ({ ...current, customerId: createdCustomerId }))
  }, [createdCustomerId])

  const plotterCustomers = useMemo(
    () => customersForExternalPlotter(customers),
    [customers]
  )

  const historicalRegistration = isInternalPlotterJob(form.jobType) && form.historicalRegistration
  const parsedHistoricalMonth = parseHistoricalOrderMonth(form.historicalMonth)
  const selectableOrders = useMemo(
    () => plotterOrdersForRegistration(orders, historicalOrders, historicalRegistration),
    [orders, historicalOrders, historicalRegistration]
  )

  const selectedOrder = useMemo(
    () => selectableOrders.find((order) => order.orderId === form.orderId) || null,
    [selectableOrders, form.orderId]
  )

  const selectedRoll = useMemo(
    () =>
      (paperRolls || []).find((roll) => roll.inventoryItemId === form.paperInventoryItemId) ||
      null,
    [paperRolls, form.paperInventoryItemId]
  )

  const isInternal = isInternalPlotterJob(form.jobType)
  const isExternal = form.jobType === 'EXTERNAL'
  const isWaste = isWastePlotterJob(form.jobType)

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

  useEffect(() => {
    const year = parsedHistoricalMonth?.year
    const month = parsedHistoricalMonth?.month
    if (!open || !historicalRegistration || !year || !month) {
      return undefined
    }

    let cancelled = false
    setHistoricalLookupLoading(true)
    setHistoricalLookupError('')

    getHistoricalCommercialOrdersForPlotter({ year, month })
      .then((data) => {
        if (cancelled) {
          return
        }
        setHistoricalOrders(Array.isArray(data?.orders) ? data.orders : [])
      })
      .catch(() => {
        if (cancelled) {
          return
        }
        setHistoricalOrders([])
        setHistoricalLookupError('No se pudieron consultar los pedidos de ese mes.')
      })
      .finally(() => {
        if (!cancelled) {
          setHistoricalLookupLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [open, historicalRegistration, parsedHistoricalMonth?.year, parsedHistoricalMonth?.month])

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
        next.historicalRegistration = false
        next.historicalMonth = ''
        if (!next.creationDate) {
          next.creationDate = toIsoDate()
        }
      }
      if (field === 'historicalMonth') {
        next.orderId = ''
        next.customerId = ''
      }
      if (field === 'orderId') {
        const order = plotterOrdersForRegistration(
          orders,
          historicalOrders,
          isInternalPlotterJob(current.jobType) && current.historicalRegistration
        ).find((item) => item.orderId === value)
        next.customerId = order?.customerId || ''
      }
      return next
    })
    if (field === 'jobType' || field === 'historicalMonth') {
      setHistoricalOrders([])
      setHistoricalLookupError('')
      setHistoricalLookupLoading(false)
    }
    setValidationError('')
  }

  function updateHistoricalRegistration(enabled) {
    setHistoricalOrders([])
    setHistoricalLookupError('')
    setHistoricalLookupLoading(false)
    setForm((current) => ({
      ...current,
      historicalRegistration: enabled,
      historicalMonth: enabled ? current.historicalMonth : '',
      orderId: '',
      customerId: '',
      creationDate: enabled ? '' : toIsoDate(),
    }))
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
        historicalRegistration && !creationDate
          ? 'Indica la fecha real de impresión. Un registro atrasado no usa la fecha de hoy.'
          : 'Tipo de trabajo, fecha, rollo de papel y metros impresos son obligatorios.'
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

    let pricePerMeter = 0
    if (!isWaste) {
      const pricePerMeterRaw = form.pricePerMeter.trim()
      if (!pricePerMeterRaw) {
        setValidationError(
          isInternal
            ? 'El precio por metro es obligatorio para el servicio Plotter interno Magyen.'
            : 'El precio por metro es obligatorio para una venta Plotter externa.'
        )
        return
      }
      pricePerMeter = Number(pricePerMeterRaw)
      if (Number.isNaN(pricePerMeter) || (isInternal ? pricePerMeter <= 0 : pricePerMeter < 0)) {
        setValidationError(
          isInternal
            ? 'El precio por metro del servicio interno debe ser mayor que cero.'
            : 'El precio por metro debe ser un número mayor o igual a cero.'
        )
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
    onSubmit(withHistoricalPlotterRegistration({
      jobType,
      plotterJobId: form.plotterJobId || createPlotterJobId(),
      customerId: isExternal ? form.customerId : undefined,
      orderId: isInternal ? form.orderId : undefined,
      creationDate,
      paperInventoryItemId,
      printedMeters,
      pricePerMeter,
      observations: observations || null,
    }, historicalRegistration))
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Nuevo trabajo de plotter</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage || historicalLookupError) && (
            <Alert severity="error">{validationError || errorMessage || historicalLookupError}</Alert>
          )}

          <TextField
            select
            label="Tipo de trabajo"
            value={form.jobType}
            onChange={(event) => updateField('jobType', event.target.value)}
            fullWidth
            disabled={submitting}
            autoFocus
            helperText="Elige si este trabajo es un servicio interno Magyen, una venta externa o merma operativa."
          >
            <MenuItem value="INTERNAL_MAGYEN">
              {formatPlotterJobTypeLabel('INTERNAL_MAGYEN')}
            </MenuItem>
            <MenuItem value="EXTERNAL">{formatPlotterJobTypeLabel('EXTERNAL')}</MenuItem>
            <MenuItem value="WASTE">{formatPlotterJobTypeLabel('WASTE')}</MenuItem>
          </TextField>

          {isInternal && (
            <Alert severity="info">
              Servicio Plotter interno Magyen: no es una venta a cliente externo. Ingresa el
              precio por metro; el valor del servicio es metros × precio.
            </Alert>
          )}

          {isExternal && (
            <Alert severity="info">
              Venta Plotter externa. No crea una orden comercial ni producción.
            </Alert>
          )}

          {isWaste && (
            <Alert severity="info">
              Merma operativa: muestras, pruebas o impresiones fallidas. Consume papel,
              no exige cliente ni pedido y no genera cobro ni ingreso.
            </Alert>
          )}

          {isInternal && (
            <>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={form.historicalRegistration}
                    onChange={(event) => updateHistoricalRegistration(event.target.checked)}
                    disabled={submitting}
                  />
                }
                label="Registrar trabajo atrasado"
              />
              {historicalRegistration ? (
                <Alert severity="warning">
                  Registro atrasado de un trabajo que ya se imprimió. El pedido permanece
                  en su estado actual. Indica la fecha real de impresión; no se sustituye
                  por hoy. Un pedido puede tener varios trabajos de Plotter.
                </Alert>
              ) : null}
              {historicalRegistration && (
                <TextField
                  label="Mes de confirmación del pedido"
                  type="month"
                  value={form.historicalMonth}
                  onChange={(event) => updateField('historicalMonth', event.target.value)}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                  disabled={submitting}
                  helperText="Busca pedidos de meses anteriores sin cargar todo el histórico."
                />
              )}
              <TextField
                select
                label="Orden comercial asociada"
                value={form.orderId}
                onChange={(event) => updateField('orderId', event.target.value)}
                fullWidth
                disabled={
                  submitting
                  || (!historicalRegistration && loadingLookups)
                  || (historicalRegistration && (!parsedHistoricalMonth || historicalLookupLoading))
                }
                helperText={
                  historicalRegistration
                    ? historicalOrderHelperText(
                        parsedHistoricalMonth,
                        historicalLookupLoading,
                        selectableOrders.length
                      )
                    : !loadingLookups && selectableOrders.length === 0
                      ? 'No hay órdenes abiertas para asociar.'
                      : 'Solo órdenes abiertas: confirmadas, en producción o listas para entrega.'
                }
              >
                {selectableOrders.map((order) => (
                  <MenuItem key={order.orderId} value={order.orderId}>
                    {historicalRegistration
                      ? formatHistoricalPlotterOrderOption(order)
                      : `${formatPlotterOrderLabel(order)}${order.customerName ? ` — ${order.customerName}` : ''}`}
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
              {historicalRegistration && selectedOrder ? (
                <Typography variant="body2" color="text.secondary">
                  Estado actual: {formatCommercialOrderStatusLabel(selectedOrder.status)}.
                  Confirmación: {selectedOrder.confirmationDate || '—'}. El registro no reabre el pedido.
                </Typography>
              ) : null}
            </>
          )}

          {isExternal && (
            <>
              <TextField
                select
                label="Cliente"
                value={form.customerId}
                onChange={(event) => updateField('customerId', event.target.value)}
                fullWidth
                disabled={submitting || loadingLookups}
              >
                {(plotterCustomers || []).map((customer) => (
                  <MenuItem key={customer.customerId} value={customer.customerId}>
                    {customer.name}
                  </MenuItem>
                ))}
              </TextField>
              <Button
                type="button"
                variant="text"
                onClick={onCreateCustomer}
                disabled={submitting || !onCreateCustomer}
                sx={{ alignSelf: 'flex-start' }}
              >
                + Crear cliente Plotter
              </Button>
            </>
          )}

          {form.jobType && (
            <>
              <TextField
                label={historicalRegistration ? 'Fecha real de impresión' : 'Fecha del trabajo'}
                type="date"
                value={form.creationDate}
                onChange={(event) => updateField('creationDate', event.target.value)}
                InputLabelProps={{ shrink: true }}
                fullWidth
                disabled={submitting}
                required={historicalRegistration}
                helperText={
                  historicalRegistration
                    ? 'Fecha en que se imprimió el trabajo. No uses la fecha de hoy si el trabajo es de otro mes.'
                    : undefined
                }
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
                    {roll.paperRollNumber ? `${roll.paperRollNumber} — ${roll.name || roll.materialCode}` : (roll.name || roll.materialCode)} —{' '}
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

              {!isWaste && (
                <TextField
                  label="Precio por metro"
                  value={form.pricePerMeter}
                  onChange={(event) => updateField('pricePerMeter', event.target.value)}
                  fullWidth
                  disabled={submitting}
                  required
                  helperText={
                    isInternal
                      ? 'Precio variable del servicio interno Magyen. No es un precio de inventario.'
                      : 'Precio de venta al cliente externo.'
                  }
                  inputProps={{ inputMode: 'decimal' }}
                />
              )}

              {isInternal && (
                <Stack spacing={0.5}>
                  <Typography variant="body2" color="text.secondary">
                    Valor del servicio
                  </Typography>
                  <Typography variant="h6">
                    {salePreview === null ? '—' : formatPlotterMoney(salePreview)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Metros × precio por metro. El servidor calcula el valor definitivo.
                    {materialCostPreview !== null
                      ? ` Costo histórico del papel: ${formatPlotterMoney(materialCostPreview)} (no se duplica como compra).`
                      : ''}
                  </Typography>
                </Stack>
              )}

              {isWaste && (
                <Typography variant="body2" color="text.secondary">
                  La merma no genera valor de venta. El costo analítico de tinta se lee
                  de las compras del período, no de este trabajo.
                </Typography>
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
