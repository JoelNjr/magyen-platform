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
import Autocomplete from '@mui/material/Autocomplete'
import { getOrders } from '../../commercial/services/commercialService'
import {
  associatedOrderSubmitValue,
  formatAssociatedOrderLabel,
} from '../presentation/associatedOrderPresentation'
import {
  EXPENSE_CATEGORY_OPTIONS,
  INCOME_CATEGORY_OPTIONS,
  TRANSACTION_TYPE_OPTIONS,
  toIsoDate,
} from '../presentation/financePresentation'

const EMPTY_FORM = {
  type: 'EXPENSE',
  amount: '',
  transactionDate: '',
  category: 'SERVICES',
  description: '',
  observation: '',
  orderId: '',
}

const NO_ORDER = { orderId: '', customerName: '', orderNumber: '' }

function RegisterFinancialTransactionDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
}) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [validationError, setValidationError] = useState('')
  const [orderSearch, setOrderSearch] = useState('')
  const [orderOptions, setOrderOptions] = useState([])
  const [loadingOrders, setLoadingOrders] = useState(false)

  useEffect(() => {
    if (!open) {
      setForm(EMPTY_FORM)
      setValidationError('')
      return
    }

    setForm({
      ...EMPTY_FORM,
      transactionDate: toIsoDate(new Date()),
    })
    setOrderSearch('')
    setOrderOptions([])
  }, [open])

  useEffect(() => {
    if (!open || form.type !== 'EXPENSE') {
      return undefined
    }

    let active = true
    const timer = setTimeout(() => {
      setLoadingOrders(true)
      getOrders({
        search: orderSearch.trim(),
        acceptsDirectCost: true,
        limit: 20,
      })
        .then((data) => {
          if (!active) {
            return
          }
          setOrderOptions(Array.isArray(data?.orders) ? data.orders : [])
        })
        .catch(() => {
          if (active) {
            setOrderOptions([])
          }
        })
        .finally(() => {
          if (active) {
            setLoadingOrders(false)
          }
        })
    }, 300)

    return () => {
      active = false
      clearTimeout(timer)
    }
  }, [open, form.type, orderSearch])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  function updateField(field, value) {
    setForm((current) => {
      const next = { ...current, [field]: value }
      if (field === 'type') {
        next.category = value === 'INCOME' ? 'SALES' : 'SERVICES'
        if (value !== 'EXPENSE') {
          next.orderId = ''
        }
      }
      return next
    })
    setValidationError('')
  }

  function handleSubmit() {
    if (submitting) {
      return
    }

    const amountRaw = form.amount.trim()
    const category = form.category.trim()
    const description = form.description.trim()
    const observation = form.observation.trim()

    if (!form.type || !amountRaw || !form.transactionDate || !category) {
      setValidationError('Tipo, monto, fecha y categoría son obligatorios.')
      return
    }

    const amount = Number(amountRaw)
    if (Number.isNaN(amount) || amount <= 0) {
      setValidationError('El monto debe ser un número mayor que cero.')
      return
    }

    onSubmit({
      type: form.type,
      amount,
      transactionDate: form.transactionDate,
      category,
      description: description || null,
      observation: observation || null,
      sourceType: 'MANUAL',
      sourceId: null,
      orderId: associatedOrderSubmitValue(form.orderId, form.type),
    })
  }

  const categoryOptions =
    form.type === 'INCOME' ? INCOME_CATEGORY_OPTIONS : EXPENSE_CATEGORY_OPTIONS
  const selectedOrder =
    orderOptions.find((order) => order.orderId === form.orderId) ||
    (form.orderId ? { orderId: form.orderId, orderNumber: form.orderId } : NO_ORDER)
  const orderChoices = [NO_ORDER, ...orderOptions.filter((order) => order.orderId)]

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
            value={form.type}
            onChange={(event) => updateField('type', event.target.value)}
            fullWidth
            disabled={submitting}
          >
            {TRANSACTION_TYPE_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Monto"
            value={form.amount}
            onChange={(event) => updateField('amount', event.target.value)}
            fullWidth
            disabled={submitting}
          />
          <TextField
            label="Fecha"
            type="date"
            value={form.transactionDate}
            onChange={(event) => updateField('transactionDate', event.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
            disabled={submitting}
          />
          <TextField
            select
            label="Categoría"
            value={form.category}
            onChange={(event) => updateField('category', event.target.value)}
            fullWidth
            disabled={submitting}
          >
            {categoryOptions.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
          {form.type === 'EXPENSE' ? (
            <Autocomplete
              fullWidth
              options={orderChoices}
              value={selectedOrder}
              loading={loadingOrders}
              disabled={submitting}
              onChange={(_, order) => updateField('orderId', order?.orderId || '')}
              onInputChange={(_, value, reason) => {
                if (reason === 'input') {
                  setOrderSearch(value)
                }
              }}
              getOptionLabel={(option) => formatAssociatedOrderLabel(option)}
              isOptionEqualToValue={(option, selected) =>
                (option?.orderId || '') === (selected?.orderId || '')
              }
              noOptionsText="No hay pedidos para asociar"
              loadingText="Buscando pedidos..."
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Pedido asociado (opcional)"
                  helperText="Sin pedido no cambia la rentabilidad. Un gasto asociado entra en Costos otros."
                />
              )}
            />
          ) : (
            <TextField
              label="Pedido asociado (opcional)"
              value="Sin pedido"
              fullWidth
              disabled
              helperText="Solo un gasto puede asociarse a un pedido."
            />
          )}
          <TextField
            label="Descripción"
            value={form.description}
            onChange={(event) => updateField('description', event.target.value)}
            fullWidth
            multiline
            minRows={2}
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
          disabled={submitting}
        >
          {submitting ? 'Registrando...' : 'Registrar movimiento'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default RegisterFinancialTransactionDialog
