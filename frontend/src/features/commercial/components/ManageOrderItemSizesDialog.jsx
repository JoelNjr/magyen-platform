import { useEffect, useMemo, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { replaceOrderItemSizes } from '../services/commercialService'

function createRowId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

function normalizeSizeKey(size) {
  return String(size ?? '')
    .trim()
    .toUpperCase()
}

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function ManageOrderItemSizesDialog({ open, onClose, orderId, orderItem, onSaved }) {
  const [rows, setRows] = useState([])
  const [submitting, setSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [validationError, setValidationError] = useState('')

  const totalQuantity = orderItem?.quantity ?? 0

  useEffect(() => {
    if (!open || !orderItem) {
      return
    }

    const existingSizes = Array.isArray(orderItem.sizes) ? orderItem.sizes : []

    setRows(
      existingSizes.map((sizeEntry) => ({
        rowId: createRowId(),
        size: sizeEntry.size ?? '',
        quantity: String(sizeEntry.quantity ?? ''),
      }))
    )
    setSubmitting(false)
    setErrorMessage('')
    setValidationError('')
  }, [open, orderItem])

  const registeredQuantity = useMemo(() => {
    return rows.reduce((sum, row) => {
      const quantity = Number(row.quantity)
      if (!Number.isFinite(quantity) || quantity <= 0) {
        return sum
      }
      return sum + quantity
    }, 0)
  }, [rows])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleAddRow() {
    setRows((current) => [
      ...current,
      { rowId: createRowId(), size: '', quantity: '' },
    ])
    setValidationError('')
    setErrorMessage('')
  }

  function handleRemoveRow(rowId) {
    setRows((current) => current.filter((row) => row.rowId !== rowId))
    setValidationError('')
    setErrorMessage('')
  }

  function handleSizeChange(rowId, value) {
    setRows((current) =>
      current.map((row) => (row.rowId === rowId ? { ...row, size: value } : row))
    )
    setValidationError('')
    setErrorMessage('')
  }

  function handleQuantityChange(rowId, value) {
    setRows((current) =>
      current.map((row) =>
        row.rowId === rowId ? { ...row, quantity: value } : row
      )
    )
    setValidationError('')
    setErrorMessage('')
  }

  function validateRows() {
    if (rows.length === 0) {
      return { valid: true, sizes: [] }
    }

    const seenSizes = new Set()
    const sizes = []
    let total = 0

    for (const row of rows) {
      const trimmedSize = String(row.size ?? '').trim()
      if (!trimmedSize) {
        return {
          valid: false,
          message: 'La talla no puede estar vacía.',
        }
      }

      const sizeKey = normalizeSizeKey(trimmedSize)
      if (seenSizes.has(sizeKey)) {
        return {
          valid: false,
          message: `La talla "${trimmedSize}" está duplicada.`,
        }
      }
      seenSizes.add(sizeKey)

      const quantity = Number(row.quantity)
      if (!Number.isInteger(quantity) || quantity <= 0) {
        return {
          valid: false,
          message: 'La cantidad de cada talla debe ser un número entero mayor que cero.',
        }
      }

      total += quantity
      sizes.push({
        size: trimmedSize,
        quantity,
      })
    }

    if (total > totalQuantity) {
      return {
        valid: false,
        message: `La cantidad de tallas no puede superar las ${totalQuantity} unidades.`,
      }
    }

    return { valid: true, sizes }
  }

  const validationResult = validateRows()
  const canSave = validationResult.valid && !submitting
  const liveValidationMessage =
    !validationResult.valid && rows.length > 0 ? validationResult.message : ''
  const displayedError = validationError || errorMessage || liveValidationMessage

  async function handleSave() {
    const result = validateRows()
    if (!result.valid) {
      setValidationError(result.message)
      return
    }

    setSubmitting(true)
    setValidationError('')
    setErrorMessage('')

    try {
      await replaceOrderItemSizes(orderId, orderItem.itemId, result.sizes)
      onSaved()
    } catch (error) {
      setErrorMessage(
        resolveApiErrorMessage(
          error,
          'No fue posible actualizar las tallas.'
        )
      )
      setSubmitting(false)
    }
  }

  if (!orderItem) {
    return null
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Gestionar tallas</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {displayedError && <Alert severity="error">{displayedError}</Alert>}

          <Stack spacing={0.5}>
            <Typography variant="h6">{orderItem.productName}</Typography>
            <Typography variant="body2" color="text.secondary">
              Cantidad total: {totalQuantity}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {registeredQuantity} / {totalQuantity} registradas
            </Typography>
          </Stack>

          {rows.length === 0 ? (
            <Typography color="text.secondary">
              No hay tallas registradas. Agrega filas para capturar la distribución.
            </Typography>
          ) : (
            <Stack spacing={1.5}>
              {rows.map((row) => (
                <Stack
                  key={row.rowId}
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={1.5}
                  alignItems={{ xs: 'stretch', sm: 'center' }}
                >
                  <TextField
                    label="Talla"
                    value={row.size}
                    onChange={(event) =>
                      handleSizeChange(row.rowId, event.target.value)
                    }
                    fullWidth
                    disabled={submitting}
                  />
                  <TextField
                    label="Cantidad"
                    type="number"
                    value={row.quantity}
                    onChange={(event) =>
                      handleQuantityChange(row.rowId, event.target.value)
                    }
                    fullWidth
                    disabled={submitting}
                    inputProps={{ min: 1, step: 1 }}
                  />
                  <IconButton
                    aria-label="Eliminar"
                    onClick={() => handleRemoveRow(row.rowId)}
                    disabled={submitting}
                    sx={{ alignSelf: { xs: 'flex-end', sm: 'center' } }}
                  >
                    <DeleteOutlinedIcon />
                  </IconButton>
                </Stack>
              ))}
            </Stack>
          )}

          <Button
            type="button"
            startIcon={<AddIcon />}
            onClick={handleAddRow}
            disabled={submitting}
            sx={{ alignSelf: 'flex-start' }}
          >
            Agregar talla
          </Button>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Cancelar
        </Button>
        <Button
          type="button"
          variant="contained"
          onClick={handleSave}
          disabled={!canSave}
        >
          {submitting ? 'Guardando...' : 'Guardar tallas'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ManageOrderItemSizesDialog
