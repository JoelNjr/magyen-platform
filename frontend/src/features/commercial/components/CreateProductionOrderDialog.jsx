import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
  Typography,
} from '@mui/material'

function CreateProductionOrderDialog({
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
      <DialogTitle>Crear orden de producción</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 0.5 }}>
          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

          <DialogContentText>
            Se creará una orden de producción con la información actual de esta
            orden comercial, incluyendo productos, especificaciones y tallas
            registradas.
          </DialogContentText>

          <Typography variant="body2" color="text.secondary">
            La información de productos, especificaciones y tallas se tomará de
            esta orden al momento de crear producción.
          </Typography>
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
          {submitting ? 'Creando...' : 'Crear orden de producción'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreateProductionOrderDialog
