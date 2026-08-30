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
} from '@mui/material'

const EMPTY_FORM = {
  category: 'OTHER',
  description: '',
  amount: '',
  incurredDate: '',
}

function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function RegisterProductionAdditionalCostDialog({
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
      incurredDate: toIsoDate(),
    })
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  function handleSubmit(event) {
    event.preventDefault()
    const description = form.description.trim()
    const amount = Number(form.amount)
    if (!description) {
      setValidationError('La descripción es obligatoria para OTROS.')
      return
    }
    if (Number.isNaN(amount) || amount <= 0) {
      setValidationError('El valor debe ser mayor que cero.')
      return
    }
    if (!form.incurredDate) {
      setValidationError('La fecha es obligatoria.')
      return
    }
    setValidationError('')
    onSubmit({
      category: 'OTHER',
      description,
      amount,
      incurredDate: form.incurredDate,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <form onSubmit={handleSubmit}>
        <DialogTitle>Registrar costo OTROS</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Categoría"
              value="OTROS"
              disabled
              fullWidth
            />
            <TextField
              label="Descripción"
              value={form.description}
              onChange={(event) =>
                setForm((current) => ({ ...current, description: event.target.value }))
              }
              required
              fullWidth
              multiline
              minRows={2}
            />
            <TextField
              label="Valor"
              type="number"
              value={form.amount}
              onChange={(event) =>
                setForm((current) => ({ ...current, amount: event.target.value }))
              }
              required
              fullWidth
              inputProps={{ min: 0, step: '0.01' }}
            />
            <TextField
              label="Fecha"
              type="date"
              value={form.incurredDate}
              onChange={(event) =>
                setForm((current) => ({ ...current, incurredDate: event.target.value }))
              }
              required
              fullWidth
              InputLabelProps={{ shrink: true }}
            />
            {validationError ? <Alert severity="warning">{validationError}</Alert> : null}
            {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} disabled={submitting}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? 'Registrando...' : 'Registrar'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}

export default RegisterProductionAdditionalCostDialog
