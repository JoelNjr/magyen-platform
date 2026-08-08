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

function AddQuotationItemDialog({ open, onClose, onSubmit, submitting, error }) {
  const [productName, setProductName] = useState('')
  const [fabric, setFabric] = useState('')
  const [color, setColor] = useState('')
  const [quantity, setQuantity] = useState('')
  const [unitPrice, setUnitPrice] = useState('')

  useEffect(() => {
    if (!open) {
      setProductName('')
      setFabric('')
      setColor('')
      setQuantity('')
      setUnitPrice('')
    }
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    onSubmit({
      productName,
      fabric,
      color,
      quantity: Number(quantity),
      unitPrice: Number(unitPrice),
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Agregar producto</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {error && (
            <Alert severity="error">
              No fue posible agregar el producto.
            </Alert>
          )}

          <TextField
            label="Producto"
            value={productName}
            onChange={(event) => setProductName(event.target.value)}
            fullWidth
            disabled={submitting}
          />

          <TextField
            label="Tela"
            value={fabric}
            onChange={(event) => setFabric(event.target.value)}
            fullWidth
            disabled={submitting}
          />

          <TextField
            label="Color"
            value={color}
            onChange={(event) => setColor(event.target.value)}
            fullWidth
            disabled={submitting}
          />

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
          onClick={handleSubmit}
          disabled={submitting}
        >
          {submitting ? 'Agregando...' : 'Agregar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AddQuotationItemDialog
