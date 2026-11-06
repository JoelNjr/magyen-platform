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
import { formatFinanceDate, formatFinanceMoney } from '../presentation/financePresentation'
import {
  defaultObligationPaymentDate,
  isSelectableObligationPaymentDate,
  obligationPeriodLabel,
} from '../presentation/obligationPaymentPresentation.js'

function PayObligationOccurrenceDialog({
  open,
  occurrence,
  onClose,
  onConfirm,
  submitting,
  errorMessage,
}) {
  const [paymentDate, setPaymentDate] = useState(() => defaultObligationPaymentDate())

  useEffect(() => {
    if (open) {
      setPaymentDate(defaultObligationPaymentDate())
    }
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  const periodLabel = occurrence ? obligationPeriodLabel(occurrence.dueDate) : '—'
  const paymentDateValid = isSelectableObligationPaymentDate(paymentDate)

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Marcar como pagado</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
          <Typography variant="body2" color="text.secondary">
            {occurrence
              ? `${occurrence.obligationName}. Monto ${formatFinanceMoney(occurrence.expectedAmount)}.`
              : ''}
          </Typography>
          <Typography>
            Período de la obligación: {periodLabel}
          </Typography>
          <Typography>
            Vencimiento: {formatFinanceDate(occurrence?.dueDate)}
          </Typography>
          <TextField
            label="Fecha real de pago"
            type="date"
            value={paymentDate}
            onChange={(event) => setPaymentDate(event.target.value)}
            fullWidth
            required
            helperText="El gasto queda en el mes de esta fecha. Puede ser un mes anterior."
            InputLabelProps={{ shrink: true }}
            disabled={submitting}
          />
          <Typography variant="body2">
            Fecha real de pago seleccionada: {paymentDateValid ? formatFinanceDate(paymentDate) : '—'}
          </Typography>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Volver
        </Button>
        <Button
          type="button"
          variant="contained"
          onClick={() => onConfirm(paymentDate)}
          disabled={submitting || !paymentDateValid}
        >
          {submitting ? 'Pagando...' : 'Marcar como pagado'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default PayObligationOccurrenceDialog
