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

function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function CreateOrderFromQuotationDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  quotationDate,
  deliveryDate,
  sellerName,
}) {
  const [orderNumber, setOrderNumber] = useState('')
  const [description, setDescription] = useState('')
  const [confirmationDate, setConfirmationDate] = useState('')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setOrderNumber('')
      setDescription('')
      setConfirmationDate('')
      setValidationError('')
      return
    }

    setConfirmationDate(toIsoDate())
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    const trimmedOrderNumber = orderNumber.trim()

    if (!trimmedOrderNumber || !confirmationDate) {
      setValidationError('El número de orden y la fecha de confirmación son obligatorios.')
      return
    }

    setValidationError('')
    onSubmit({
      orderNumber: trimmedOrderNumber,
      description: description.trim() || null,
      confirmationDate,
    })
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
              Fecha de cotización
            </Typography>
            <Typography>{formatDisplayDate(quotationDate)}</Typography>
          </Stack>

          <Stack spacing={0.5}>
            <Typography variant="body2" color="text.secondary">
              Fecha de entrega
            </Typography>
            <Typography>{formatDisplayDate(deliveryDate)}</Typography>
          </Stack>

          <TextField
            label="Fecha de confirmación"
            type="date"
            value={confirmationDate}
            onChange={(event) => setConfirmationDate(event.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
            required
            disabled={submitting}
            helperText="Debe ser igual o posterior a la cotización e igual o anterior a la entrega."
          />

          <Stack spacing={0.5}>
            <Typography variant="body2" color="text.secondary">
              Vendedor
            </Typography>
            <Typography>{sellerName || '—'}</Typography>
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

          <TextField
            label="Descripción del pedido"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            fullWidth
            disabled={submitting}
            helperText="Explica de qué se trata el pedido. No reemplaza el número de orden."
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
