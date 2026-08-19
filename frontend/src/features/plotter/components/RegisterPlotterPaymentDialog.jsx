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
import { formatPlotterMoney } from '../presentation/plotterJobPresentation'

function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const EMPTY_FORM = {
  amount: '',
  paymentDate: '',
  observations: '',
}

function RegisterPlotterPaymentDialog({
  open,
  totalAmount,
  paidAmount,
  outstandingAmount,
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
      paymentDate: toIsoDate(),
    })
  }, [open])

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

    const amountRaw = form.amount.trim()
    const observations = form.observations.trim()

    if (!amountRaw || !form.paymentDate) {
      setValidationError('Monto y fecha son obligatorios.')
      return
    }

    const amount = Number(amountRaw)
    if (Number.isNaN(amount) || amount <= 0) {
      setValidationError('El monto debe ser un número mayor que cero.')
      return
    }

    const outstanding = Number(outstandingAmount)
    if (!Number.isNaN(outstanding) && amount > outstanding) {
      setValidationError(
        `El monto no puede superar el saldo pendiente (${formatPlotterMoney(outstandingAmount)}).`
      )
      return
    }

    onSubmit({
      amount,
      paymentDate: form.paymentDate,
      observations: observations || null,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Registrar pago</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          <Stack spacing={0.5}>
            <Typography variant="body2" color="text.secondary">
              Total del trabajo: {formatPlotterMoney(totalAmount)}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Total pagado: {formatPlotterMoney(paidAmount)}
            </Typography>
            <Typography variant="body2">
              Saldo pendiente: {formatPlotterMoney(outstandingAmount)}
            </Typography>
          </Stack>
          <TextField
            label="Valor a registrar"
            value={form.amount}
            onChange={(event) => updateField('amount', event.target.value)}
            fullWidth
            disabled={submitting}
          />
          <TextField
            label="Fecha del pago"
            type="date"
            value={form.paymentDate}
            onChange={(event) => updateField('paymentDate', event.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
            disabled={submitting}
          />
          <TextField
            label="Observación"
            value={form.observations}
            onChange={(event) => updateField('observations', event.target.value)}
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
          {submitting ? 'Registrando...' : 'Registrar pago'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default RegisterPlotterPaymentDialog
