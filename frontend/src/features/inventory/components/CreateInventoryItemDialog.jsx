import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import {
  INVENTORY_MATERIAL_TYPE_OPTIONS,
  INVENTORY_UNIT_OPTIONS,
} from '../presentation/inventoryStatusPresentation'

const EMPTY_FORM = {
  materialType: '',
  name: '',
  category: '',
  description: '',
  unitOfMeasure: 'METER',
  stock: '',
  minimumStock: '',
  purchaseQuantity: '',
  purchaseUnitCost: '',
  purchaseTotalCost: '',
  purchaseDate: '',
}

function todayIsoDate() {
  return new Date().toISOString().slice(0, 10)
}

function createPurchaseId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return undefined
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
    } else {
      setForm((current) => ({
        ...current,
        purchaseDate: current.purchaseDate || todayIsoDate(),
      }))
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

      if (field === 'materialType') {
        if (value === 'PAPER') {
          next.unitOfMeasure = 'METER'
          if (!next.category.trim()) {
            next.category = 'PAPER'
          }
          if (!next.purchaseQuantity.trim()) {
            next.purchaseQuantity = '1'
          }
        } else if (value === 'FABRIC') {
          next.unitOfMeasure = 'METER'
          if (!next.category.trim()) {
            next.category = 'FABRIC'
          }
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

    const materialType = form.materialType
    if (!materialType) {
      setValidationError('El tipo de material es obligatorio.')
      return
    }

    const isPaper = materialType === 'PAPER'
    const isFabric = materialType === 'FABRIC'
    const name = form.name.trim()
    const category = form.category.trim()
    const description = form.description.trim()
    const unitOfMeasure = isPaper || isFabric ? 'METER' : form.unitOfMeasure
    const minimumStockRaw = form.minimumStock.trim()

    if (!isPaper && !name) {
      setValidationError('El nombre del material es obligatorio.')
      return
    }

    if (!isPaper && (!category || !unitOfMeasure)) {
      setValidationError('La categoría y la unidad de medida son obligatorias.')
      return
    }

    let minimumStock = null
    if (minimumStockRaw !== '') {
      minimumStock = Number(minimumStockRaw)
      if (Number.isNaN(minimumStock) || minimumStock < 0) {
        setValidationError('El stock mínimo debe ser un número válido no negativo.')
        return
      }
    }

    if (isPaper) {
      const stockRaw = form.stock.trim()
      const quantityRaw = form.purchaseQuantity.trim() || '1'
      const pricePerRollRaw = form.purchaseUnitCost.trim()
      const purchaseDate = form.purchaseDate.trim()
      if (!stockRaw) {
        setValidationError('Los metros iniciales del rollo son obligatorios.')
        return
      }
      const stock = Number(stockRaw)
      if (Number.isNaN(stock) || stock <= 0) {
        setValidationError('Los metros iniciales deben ser un número mayor que cero.')
        return
      }
      const quantity = Number(quantityRaw)
      if (Number.isNaN(quantity) || quantity <= 0) {
        setValidationError('La cantidad de rollos debe ser un número mayor que cero.')
        return
      }
      if (!pricePerRollRaw) {
        setValidationError('El precio de adquisición por rollo es obligatorio.')
        return
      }
      const pricePerRoll = Number(pricePerRollRaw)
      if (Number.isNaN(pricePerRoll) || pricePerRoll <= 0) {
        setValidationError('El precio de adquisición por rollo debe ser mayor que cero.')
        return
      }
      if (!purchaseDate) {
        setValidationError('La fecha de compra es obligatoria.')
        return
      }

      setValidationError('')
      onSubmit({
        name: name || null,
        category: category || 'PAPER',
        unitOfMeasure: 'METER',
        stock,
        minimumStock,
        description: description || null,
        materialType,
        plotterPaperRoll: true,
        acquisition: {
          purchaseId: createPurchaseId(),
          quantity,
          unitCost: pricePerRoll,
          purchaseDate,
        },
      })
      return
    }

    const quantityRaw = form.purchaseQuantity.trim()
    const purchaseDate = form.purchaseDate.trim()
    if (!quantityRaw || !purchaseDate) {
      setValidationError('La cantidad adquirida y la fecha de compra son obligatorias.')
      return
    }
    const quantity = Number(quantityRaw)
    if (Number.isNaN(quantity) || quantity <= 0) {
      setValidationError('La cantidad adquirida debe ser un número mayor que cero.')
      return
    }

    let acquisition
    if (isFabric) {
      const unitCostRaw = form.purchaseUnitCost.trim()
      if (!unitCostRaw) {
        setValidationError('El costo de compra por metro es obligatorio.')
        return
      }
      const unitCost = Number(unitCostRaw)
      if (Number.isNaN(unitCost) || unitCost <= 0) {
        setValidationError('El costo por metro debe ser un número mayor que cero.')
        return
      }
      acquisition = {
        purchaseId: createPurchaseId(),
        quantity,
        unitCost,
        purchaseDate,
      }
    } else {
      const totalCostRaw = form.purchaseTotalCost.trim()
      if (!totalCostRaw) {
        setValidationError('El costo total de adquisición es obligatorio.')
        return
      }
      const totalCost = Number(totalCostRaw)
      if (Number.isNaN(totalCost) || totalCost <= 0) {
        setValidationError('El costo total de adquisición debe ser un número mayor que cero.')
        return
      }
      acquisition = {
        purchaseId: createPurchaseId(),
        quantity,
        totalCost,
        purchaseDate,
      }
    }

    setValidationError('')
    onSubmit({
      name,
      category,
      unitOfMeasure,
      stock: 0,
      minimumStock,
      description: description || null,
      materialType,
      plotterPaperRoll: false,
      acquisition,
    })
  }

  const isPaper = form.materialType === 'PAPER'
  const isFabric = form.materialType === 'FABRIC'
  const isOtherMaterial = Boolean(form.materialType) && !isPaper
  const typeSelected = Boolean(form.materialType)

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Nuevo material</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          <TextField
            select
            label="Tipo de material"
            value={form.materialType}
            onChange={(event) => updateField('materialType', event.target.value)}
            fullWidth
            disabled={submitting}
            autoFocus
            required
          >
            <MenuItem value="" disabled>
              Seleccione un tipo
            </MenuItem>
            {INVENTORY_MATERIAL_TYPE_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>

          {isPaper && (
            <Alert severity="info">
              El código de material es compartido por todos los rollos de papel. El número de
              rollo (RP-XXX) se genera automáticamente. Aquí se registra el precio de
              adquisición por rollo, no un precio de venta de Plotter.
            </Alert>
          )}

          {typeSelected && !isPaper && (
            <TextField
              label={isFabric ? 'Identidad de la tela' : 'Nombre'}
              value={form.name}
              onChange={(event) => updateField('name', event.target.value)}
              fullWidth
              disabled={submitting}
              required
            />
          )}

          {typeSelected && (
            <TextField
              label="Categoría"
              value={form.category}
              onChange={(event) => updateField('category', event.target.value)}
              fullWidth
              disabled={submitting}
            />
          )}

          {typeSelected && (
            <TextField
              label="Descripción"
              value={form.description}
              onChange={(event) => updateField('description', event.target.value)}
              fullWidth
              disabled={submitting}
              multiline
              minRows={2}
            />
          )}

          {isOtherMaterial && !isFabric && (
            <TextField
              select
              label="Unidad de medida"
              value={form.unitOfMeasure}
              onChange={(event) => updateField('unitOfMeasure', event.target.value)}
              fullWidth
              disabled={submitting}
            >
              {INVENTORY_UNIT_OPTIONS.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </TextField>
          )}

          {isPaper && (
            <>
              <TextField
                label="Metros iniciales del rollo"
                value={form.stock}
                onChange={(event) => updateField('stock', event.target.value)}
                fullWidth
                disabled={submitting}
                required
                inputProps={{ inputMode: 'decimal' }}
              />
              <TextField
                label="Cantidad de rollos"
                value={form.purchaseQuantity}
                onChange={(event) => updateField('purchaseQuantity', event.target.value)}
                fullWidth
                disabled={submitting}
                required
                helperText="Cantidad de rollos de esta compra. El ítem creado es un rollo físico (RP-XXX)."
                inputProps={{ inputMode: 'decimal' }}
              />
              <TextField
                label="Precio de adquisición por rollo"
                value={form.purchaseUnitCost}
                onChange={(event) => updateField('purchaseUnitCost', event.target.value)}
                fullWidth
                disabled={submitting}
                required
                helperText="Precio pagado al proveedor por cada rollo. El gasto es cantidad × este precio."
                inputProps={{ inputMode: 'decimal' }}
              />
              <TextField
                label="Fecha de compra"
                type="date"
                value={form.purchaseDate}
                onChange={(event) => updateField('purchaseDate', event.target.value)}
                fullWidth
                disabled={submitting}
                InputLabelProps={{ shrink: true }}
                required
              />
            </>
          )}

          {isFabric && (
            <>
              <TextField
                label="Metros adquiridos"
                value={form.purchaseQuantity}
                onChange={(event) => updateField('purchaseQuantity', event.target.value)}
                fullWidth
                disabled={submitting}
                required
                inputProps={{ inputMode: 'decimal' }}
              />
              <TextField
                label="Costo de compra por metro"
                value={form.purchaseUnitCost}
                onChange={(event) => updateField('purchaseUnitCost', event.target.value)}
                fullWidth
                disabled={submitting}
                required
                inputProps={{ inputMode: 'decimal' }}
              />
            </>
          )}

          {isOtherMaterial && !isFabric && (
            <>
              <TextField
                label="Cantidad adquirida"
                value={form.purchaseQuantity}
                onChange={(event) => updateField('purchaseQuantity', event.target.value)}
                fullWidth
                disabled={submitting}
                required
                inputProps={{ inputMode: 'decimal' }}
              />
              <TextField
                label="Costo total de adquisición"
                value={form.purchaseTotalCost}
                onChange={(event) => updateField('purchaseTotalCost', event.target.value)}
                fullWidth
                disabled={submitting}
                required
                helperText="Desembolso pagado por Magyen. No es un costo por metro."
                inputProps={{ inputMode: 'decimal' }}
              />
            </>
          )}

          {isOtherMaterial && (
            <TextField
              label="Fecha de compra"
              type="date"
              value={form.purchaseDate}
              onChange={(event) => updateField('purchaseDate', event.target.value)}
              fullWidth
              disabled={submitting}
              InputLabelProps={{ shrink: true }}
              required
            />
          )}

          {typeSelected && (
            <TextField
              label="Stock mínimo (opcional)"
              value={form.minimumStock}
              onChange={(event) => updateField('minimumStock', event.target.value)}
              fullWidth
              disabled={submitting}
              helperText="Dejar vacío deshabilita el monitoreo de stock bajo."
              inputProps={{ inputMode: 'decimal' }}
            />
          )}

          {isPaper && (
            <Typography variant="caption" color="text.secondary">
              Precio de adquisición por rollo. El valor de venta o servicio de Plotter se
              registra al crear el trabajo, no aquí.
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
          disabled={submitting || !typeSelected}
        >
          {submitting ? 'Creando...' : 'Crear material'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreateInventoryItemDialog
