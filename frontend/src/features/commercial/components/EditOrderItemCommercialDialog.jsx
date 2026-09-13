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
} from '@mui/material'

function EditOrderItemCommercialDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  item,
}) {
  const [quantity, setQuantity] = useState('')
  const [unitPrice, setUnitPrice] = useState('')

  useEffect(() => {
    if (!open) {
      setQuantity('')
      setUnitPrice('')
      return
    }

    setQuantity(item?.quantity == null ? '' : String(item.quantity))
    setUnitPrice(item?.unitPrice == null ? '' : String(item.unitPrice))
  }, [open, item])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Editar cantidad y precio</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
          <TextField
            label="Cantidad"
            type="number"
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
            fullWidth
            disabled={submitting}
          />
          <TextField
            label="Precio unitario"
            type="number"
            value={unitPrice}
            onChange={(event) => setUnitPrice(event.target.value)}
            fullWidth
            disabled={submitting}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          variant="contained"
          onClick={() =>
            onSubmit({
              quantity: Number(quantity),
              unitPrice: Number(unitPrice),
            })
          }
          disabled={submitting}
        >
          {submitting ? 'Guardando...' : 'Guardar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default EditOrderItemCommercialDialog
