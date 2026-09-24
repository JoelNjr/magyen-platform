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
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import {
  toIsoDate,
  validateDeliveryDate,
} from '../presentation/orderLifecyclePresentation'

function DeliverOrderDialog({
  open,
  confirmationDate,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
}) {
  const [deliveryDate, setDeliveryDate] = useState('')
  const [validationError, setValidationError] = useState('')
  const today = toIsoDate()

  useEffect(() => {
    if (!open) {
      setDeliveryDate('')
      setValidationError('')
      return
    }
    setDeliveryDate(today)
  }, [open, today])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  function handleSubmit() {
    if (submitting) {
      return
    }
    const validation = validateDeliveryDate(deliveryDate, confirmationDate, today)
    if (validation) {
      setValidationError(validation)
      return
    }
    onSubmit(deliveryDate)
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="xs">
      <DialogTitle>Registrar entrega</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Fecha de confirmación: {formatDisplayDate(confirmationDate) || '—'}
          </Typography>
          <TextField
            label="Fecha de entrega"
            type="date"
            value={deliveryDate}
            onChange={(event) => {
              setDeliveryDate(event.target.value)
              setValidationError('')
            }}
            slotProps={{
              inputLabel: { shrink: true },
              htmlInput: {
                min: confirmationDate || undefined,
                max: today,
              },
            }}
            required
            fullWidth
          />
          {validationError ? <Alert severity="warning">{validationError}</Alert> : null}
          {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button variant="contained" onClick={handleSubmit} disabled={submitting}>
          {submitting ? 'Registrando entrega…' : 'Registrar entrega'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default DeliverOrderDialog
