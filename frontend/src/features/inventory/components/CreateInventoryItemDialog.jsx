import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material'
import {
  INVENTORY_MATERIAL_TYPE_OPTIONS,
  INVENTORY_UNIT_OPTIONS,
} from '../presentation/inventoryStatusPresentation'

const EMPTY_FORM = {
  code: '',
  name: '',
  category: '',
  description: '',
  materialType: 'OTHER',
  plotterPaperRoll: false,
  unitOfMeasure: 'METER',
  stock: '',
  minimumStock: '',
  unitCost: '',
}

function CreateInventoryItemDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
}) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setForm(EMPTY_FORM)
      setValidationError('')
    }
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function updateField(field, value) {
    setForm((current) => {
      const next = { ...current, [field]: value }

      if (field === 'materialType' && value !== 'PAPER') {
        next.plotterPaperRoll = false
      }

      if (field === 'plotterPaperRoll' && value) {
        next.materialType = 'PAPER'
        next.unitOfMeasure = 'METER'
        if (!next.category.trim()) {
          next.category = 'PAPER'
        }
      }

      return next
    })
    setValidationError('')
  }

  function handleSubmit() {
    if (submitting) {
      return
    }

    const code = form.code.trim()
    const name = form.name.trim()
    const category = form.category.trim()
    const description = form.description.trim()
    const materialType = form.materialType
    const plotterPaperRoll = Boolean(form.plotterPaperRoll)
    const unitOfMeasure = plotterPaperRoll ? 'METER' : form.unitOfMeasure
    const stockRaw = form.stock.trim()
    const minimumStockRaw = form.minimumStock.trim()
    const unitCostRaw = form.unitCost.trim()

    if (!code || !name || !category || !unitOfMeasure || !stockRaw || !materialType) {
      setValidationError(
        'Código, nombre, categoría, tipo, unidad y stock inicial son obligatorios.'
      )
      return
    }

    if (plotterPaperRoll && materialType !== 'PAPER') {
      setValidationError('Un rollo de papel para Plotter debe ser tipo Papel.')
      return
    }

    const stock = Number(stockRaw)

    if (Number.isNaN(stock)) {
      setValidationError('El stock inicial debe ser un número válido.')
      return
    }

    if (stock < 0) {
      setValidationError('El stock inicial no puede ser negativo.')
      return
    }

    let minimumStock = null

    if (minimumStockRaw !== '') {
      minimumStock = Number(minimumStockRaw)

      if (Number.isNaN(minimumStock)) {
        setValidationError('El stock mínimo debe ser un número válido.')
        return
      }

      if (minimumStock < 0) {
        setValidationError('El stock mínimo no puede ser negativo.')
        return
      }
    }

    let unitCost = null

    if (unitCostRaw !== '') {
      unitCost = Number(unitCostRaw)

      if (Number.isNaN(unitCost)) {
        setValidationError('El costo unitario debe ser un número válido.')
        return
      }

      if (unitCost < 0) {
        setValidationError('El costo unitario no puede ser negativo.')
        return
      }
    }

    setValidationError('')
    onSubmit({
      code,
      name,
      category,
      unitOfMeasure,
      stock,
      minimumStock,
      description: description || null,
      unitCost,
      materialType,
      plotterPaperRoll,
    })
  }

  const isPlotterPaperRoll = Boolean(form.plotterPaperRoll)

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Nuevo material</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          <TextField
            label="Código"
            value={form.code}
            onChange={(event) => updateField('code', event.target.value)}
            fullWidth
            disabled={submitting}
            autoFocus
          />

          <TextField
            label="Nombre"
            value={form.name}
            onChange={(event) => updateField('name', event.target.value)}
            fullWidth
            disabled={submitting}
          />

          <TextField
            select
            label="Tipo de material"
            value={form.materialType}
            onChange={(event) => updateField('materialType', event.target.value)}
            fullWidth
            disabled={submitting}
          >
            {INVENTORY_MATERIAL_TYPE_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>

          {form.materialType === 'PAPER' && (
            <FormControlLabel
              control={
                <Switch
                  checked={isPlotterPaperRoll}
                  onChange={(event) =>
                    updateField('plotterPaperRoll', event.target.checked)
                  }
                  disabled={submitting}
                />
              }
              label="Rollo de papel para Plotter"
            />
          )}

          {isPlotterPaperRoll && (
            <Alert severity="info">
              El número de rollo (RP-XXX) se genera automáticamente. La unidad queda
              fijada en metros.
            </Alert>
          )}

          <TextField
            label="Categoría"
            value={form.category}
            onChange={(event) => updateField('category', event.target.value)}
            fullWidth
            disabled={submitting}
          />

          <TextField
            label="Descripción"
            value={form.description}
            onChange={(event) => updateField('description', event.target.value)}
            fullWidth
            disabled={submitting}
            multiline
            minRows={2}
          />

          <TextField
            select
            label="Unidad de medida"
            value={isPlotterPaperRoll ? 'METER' : form.unitOfMeasure}
            onChange={(event) => updateField('unitOfMeasure', event.target.value)}
            fullWidth
            disabled={submitting || isPlotterPaperRoll}
          >
            {INVENTORY_UNIT_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label={isPlotterPaperRoll ? 'Metros iniciales' : 'Stock inicial'}
            value={form.stock}
            onChange={(event) => updateField('stock', event.target.value)}
            fullWidth
            disabled={submitting}
            inputProps={{ inputMode: 'decimal' }}
          />

          <TextField
            label="Stock mínimo (opcional)"
            value={form.minimumStock}
            onChange={(event) => updateField('minimumStock', event.target.value)}
            fullWidth
            disabled={submitting}
            helperText="Dejar vacío deshabilita el monitoreo de stock bajo."
            inputProps={{ inputMode: 'decimal' }}
          />

          <TextField
            label={
              isPlotterPaperRoll
                ? 'Costo por metro (opcional)'
                : 'Costo unitario (opcional)'
            }
            value={form.unitCost}
            onChange={(event) => updateField('unitCost', event.target.value)}
            fullWidth
            disabled={submitting}
            helperText="Valoración interna del material. No es precio de venta."
            inputProps={{ inputMode: 'decimal' }}
          />

          {isPlotterPaperRoll && (
            <Typography variant="caption" color="text.secondary">
              El número de rollo no se ingresa manualmente.
            </Typography>
          )}
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
          {submitting ? 'Creando...' : 'Crear material'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreateInventoryItemDialog
