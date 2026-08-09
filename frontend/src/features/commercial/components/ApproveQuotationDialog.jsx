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

function ApproveQuotationDialog({
  open,
  onClose,
  onConfirm,
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
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Aprobar cotización</DialogTitle>

      <DialogContent>
        <Stack spacing={2}>
          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}
          <DialogContentText>
            Al aprobar, la cotización pasará de Borrador a Aprobada. Esta acción
            confirma la aceptación del cliente y habilita la creación de la
            orden comercial.
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
          onClick={onConfirm}
          disabled={submitting}
        >
          {submitting ? 'Aprobando...' : 'Aprobar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ApproveQuotationDialog
