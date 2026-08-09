import { useEffect, useState } from 'react'
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
  Typography,
} from '@mui/material'
import { formatDisplayDate } from '../presentation/formatDisplayDate'

function CreateOrderFromQuotationDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  deliveryDate,
  salesperson,
}) {
  const [orderNumber, setOrderNumber] = useState('')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setOrderNumber('')
      setValidationError('')
    }
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    const trimmedOrderNumber = orderNumber.trim()

    if (!trimmedOrderNumber) {
      setValidationError('El número de orden es obligatorio.')
      return
    }

    setValidationError('')
    onSubmit({ orderNumber: trimmedOrderNumber })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Crear orden</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          <DialogContentText>
            Se creará una orden comercial a partir de esta cotización aprobada.
            Los productos y el total se tomarán del snapshot de la cotización.
          </DialogContentText>

          <Stack spacing={0.5}>
            <Typography variant="body2" color="text.secondary">
              Fecha de entrega
            </Typography>
            <Typography>{formatDisplayDate(deliveryDate)}</Typography>
          </Stack>

          <Stack spacing={0.5}>
            <Typography variant="body2" color="text.secondary">
              Vendedor
            </Typography>
            <Typography>{salesperson}</Typography>
          </Stack>

          <TextField
            label="Número de orden"
            value={orderNumber}
            onChange={(event) => setOrderNumber(event.target.value)}
            fullWidth
            required
            disabled={submitting}
            helperText="Identificador comercial de la orden. No se genera automáticamente."
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          type="button"
          variant="contained"
          onClick={handleSubmit}
          disabled={submitting}
        >
          {submitting ? 'Creando...' : 'Crear orden'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreateOrderFromQuotationDialog
