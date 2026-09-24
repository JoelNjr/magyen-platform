import { useState } from 'react'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material'
import { toIsoDate } from '../presentation/financePresentation'

function SellerCommissionPaymentDialog({
  open,
  description,
  onClose,
  onConfirm,
  submitting,
  errorMessage,
}) {
  const [paymentDate, setPaymentDate] = useState(() => toIsoDate(new Date()))

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Pagar comisión</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
          <DialogContentText>{description}</DialogContentText>
          <TextField
            label="Fecha de pago"
            type="date"
            value={paymentDate}
            onChange={(event) => setPaymentDate(event.target.value)}
            fullWidth
            InputLabelProps={{ shrink: true }}
            disabled={submitting}
          />
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
          disabled={submitting || !paymentDate}
        >
          {submitting ? 'Pagando...' : 'Pagar comisión'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default SellerCommissionPaymentDialog
