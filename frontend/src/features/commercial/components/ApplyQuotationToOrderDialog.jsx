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
import { QUOTATION_ORDER_PAYMENT_FLOOR_MESSAGE } from '../presentation/quotationOrderSynchronizationPresentation'

const currencyFormatter = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})

function formatCurrency(amount) {
  if (amount == null) {
    return '—'
  }
  return currencyFormatter.format(amount)
}

function formatSizes(sizes) {
  if (!sizes || sizes.length === 0) {
    return 'Sin tallas'
  }
  return sizes.map((size) => `${size.size}: ${size.quantity}`).join(', ')
}

function Section({ title, children }) {
  return (
    <Stack spacing={0.75}>
      <Typography variant="subtitle2">{title}</Typography>
      {children}
    </Stack>
  )
}

function ApplyQuotationToOrderDialog({
  open,
  preview,
  submitting,
  errorMessage,
  onClose,
  onConfirm,
}) {
  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  const paymentFloorBlocked = Boolean(preview?.paymentFloorViolation)
  const sizeBlocked = Boolean(preview?.sizeConstraintViolation)
  const confirmDisabled =
    submitting ||
    !preview?.applyAllowed ||
    paymentFloorBlocked ||
    sizeBlocked ||
    preview?.proposedTotal == null

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="md">
      <DialogTitle>Aplicar cambios a la orden</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 0.5 }}>
          {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
          {paymentFloorBlocked ? (
            <Alert severity="error">{QUOTATION_ORDER_PAYMENT_FLOOR_MESSAGE}</Alert>
          ) : null}
          {sizeBlocked ? (
            <Alert severity="error">
              La cantidad de la cotización quedaría por debajo de las tallas ya
              registradas en la orden. Ajuste las tallas en la orden antes de
              aplicar.
            </Alert>
          ) : null}

          <Section title="Ítems que se actualizarán">
            {preview?.matchedChanges?.length ? (
              preview.matchedChanges.map((change) => (
                <Typography key={change.orderItemId} variant="body2">
                  {change.productName}: {change.currentQuantity} → {change.proposedQuantity}
                  {' · '}
                  {formatCurrency(change.currentUnitPrice)} → {formatCurrency(change.proposedUnitPrice)}
                </Typography>
              ))
            ) : (
              <Typography variant="body2" color="text.secondary">
                Ninguno
              </Typography>
            )}
          </Section>

          <Section title="Ítems nuevos">
            {preview?.additions?.length ? (
              preview.additions.map((item) => (
                <Typography key={item.quotationItemId} variant="body2">
                  {item.productName}: {item.quantity} × {formatCurrency(item.unitPrice)}
                </Typography>
              ))
            ) : (
              <Typography variant="body2" color="text.secondary">
                Ninguno
              </Typography>
            )}
          </Section>

          <Section title="Ítems que se eliminarán de la orden">
            {preview?.orphanRemovals?.length ? (
              preview.orphanRemovals.map((item) => (
                <Typography key={item.orderItemId} variant="body2">
                  {item.productName}: {item.quantity} · {formatCurrency(item.subtotal)}
                  {' · '}
                  {formatSizes(item.sizes)}
                </Typography>
              ))
            ) : (
              <Typography variant="body2" color="text.secondary">
                Ninguno
              </Typography>
            )}
          </Section>

          <Section title="Ítems manuales que no se tocan">
            {preview?.manualsPreserved?.length ? (
              preview.manualsPreserved.map((item) => (
                <Typography key={item.orderItemId} variant="body2">
                  {item.productName}: {item.quantity} · {formatCurrency(item.subtotal)}
                </Typography>
              ))
            ) : (
              <Typography variant="body2" color="text.secondary">
                Ninguno
              </Typography>
            )}
          </Section>

          <Section title="Descuento">
            <Typography variant="body2">
              {formatCurrency(preview?.currentDiscount)} → {formatCurrency(preview?.proposedDiscount)}
            </Typography>
          </Section>

          <Section title="Totales">
            <Typography variant="body2">
              Total actual: {formatCurrency(preview?.currentTotal)}
            </Typography>
            <Typography variant="body2">
              Total propuesto: {formatCurrency(preview?.proposedTotal)}
            </Typography>
            <Typography variant="body2">
              Cobrado: {formatCurrency(preview?.collectedAmount)}
            </Typography>
            <Typography variant="body2">
              Saldo resultante: {formatCurrency(preview?.proposedOutstanding)}
            </Typography>
          </Section>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          type="button"
          variant="contained"
          onClick={onConfirm}
          disabled={confirmDisabled}
        >
          {submitting ? 'Aplicando…' : 'Aplicar a la orden'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ApplyQuotationToOrderDialog
