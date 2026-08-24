import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
} from '@mui/material'

function RemoveQuotationItemDialog({
  open,
  onClose,
  onConfirm,
  submitting,
  errorMessage,
  productName,
}) {
  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Eliminar producto</DialogTitle>

      <DialogContent>
        <Stack spacing={2}>
          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}
          <DialogContentText>
            Se eliminará {productName ? `“${productName}”` : 'este producto'} de
            la cotización. Esta acción no se puede deshacer.
          </DialogContentText>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          type="button"
          variant="contained"
          color="error"
          onClick={onConfirm}
          disabled={submitting}
        >
          {submitting ? 'Eliminando...' : 'Eliminar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default RemoveQuotationItemDialog
