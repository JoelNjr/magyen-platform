import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  Typography,
} from '@mui/material'

function DeactivateInventoryMaterialDialog({
  open,
  material,
  onClose,
  onConfirm,
  submitting = false,
  errorMessage = '',
}) {
  const remainingStock = Number(material?.aggregatedStock)
  const hasRemainingStock = Number.isFinite(remainingStock) && remainingStock > 0

  return (
    <Dialog open={open} onClose={submitting ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>Eliminar material del inventario activo</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Typography>
            El material se quitará del inventario activo. Las compras, el gasto
            financiero y el historial de producción o Plotter no se eliminan.
          </Typography>
          {hasRemainingStock ? (
            <Typography color="error">
              No se puede desactivar mientras quede stock físico. El stock
              agregado actual es {material.aggregatedStock}.
            </Typography>
          ) : null}
          {errorMessage ? (
            <Typography color="error">{errorMessage}</Typography>
          ) : null}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          color="error"
          variant="contained"
          onClick={onConfirm}
          disabled={submitting || hasRemainingStock}
        >
          {submitting ? 'Eliminando…' : 'Eliminar del inventario activo'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default DeactivateInventoryMaterialDialog
