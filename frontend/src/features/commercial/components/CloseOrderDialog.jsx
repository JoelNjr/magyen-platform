import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  Typography,
} from '@mui/material'

function CloseOrderDialog({
  open,
  orderNumber,
  totalAmount,
  totalPaid,
  paymentsFailed,
  formatCurrency,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
}) {
  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="xs">
      <DialogTitle>Cerrar pedido</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          <Typography>
            El pedido {orderNumber || ''} pasará a cerrado. Esta acción no registra un pago.
          </Typography>
          <Typography variant="body2" color="text.secondary">
            El cierre exige que los pagos ya registrados cubran el total del pedido.
          </Typography>
          <Typography>
            Total del pedido: {formatCurrency(totalAmount)}
          </Typography>
          <Typography>
            Total pagado: {paymentsFailed ? 'No disponible' : formatCurrency(totalPaid)}
          </Typography>
          {paymentsFailed ? (
            <Alert severity="warning">
              No se pudo mostrar la cobranza. El servidor sigue siendo quien autoriza el cierre.
            </Alert>
          ) : null}
          {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button variant="contained" onClick={onSubmit} disabled={submitting}>
          {submitting ? 'Cerrando…' : 'Cerrar pedido'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CloseOrderDialog
