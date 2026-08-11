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
}

function RegisterFinancialTransactionDialog({
  open,
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
      transactionDate: toIsoDate(new Date()),
    })
  }, [open])

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
    })
  }

  const categoryOptions =
    form.type === 'INCOME' ? INCOME_CATEGORY_OPTIONS : EXPENSE_CATEGORY_OPTIONS

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
