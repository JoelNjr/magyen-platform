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
import CatalogSelect from './CatalogSelect'
import { toSelectOptions, useCommercialCatalogs } from './useCommercialCatalogs'

function AddQuotationItemDialog({ open, onClose, onSubmit, submitting, error }) {
  const { catalogs, loading: catalogsLoading, failed: catalogsFailed } =
    useCommercialCatalogs()
  const [productName, setProductName] = useState('')
  const [fabric, setFabric] = useState('')
  const [color, setColor] = useState('')
  const [quantity, setQuantity] = useState('')
  const [unitPrice, setUnitPrice] = useState('')
  const [garmentType, setGarmentType] = useState('')
  const [collarType, setCollarType] = useState('')
  const [sleeveType, setSleeveType] = useState('')
  const [cuffRequired, setCuffRequired] = useState('')

  useEffect(() => {
    if (!open) {
      setProductName('')
      setFabric('')
      setColor('')
      setQuantity('')
      setUnitPrice('')
      setGarmentType('')
      setCollarType('')
      setSleeveType('')
      setCuffRequired('')
    }
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    const productSpecification =
      garmentType || collarType || sleeveType || cuffRequired !== ''
        ? {
            garmentType: garmentType || null,
            collarType: collarType || null,
            sleeveType: sleeveType || null,
            cuffRequired:
              cuffRequired === '' ? null : cuffRequired === 'true',
          }
        : undefined

    onSubmit({
      productName,
      fabric,
      color,
      quantity: Number(quantity),
      unitPrice: Number(unitPrice),
      productSpecification,
    })
  }

  const fieldsDisabled = submitting || catalogsLoading

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
          {catalogsFailed && (
            <Alert severity="warning">
              No fue posible cargar los catálogos comerciales.
            </Alert>
          )}

          <TextField
            label="Producto"
            value={productName}
            onChange={(event) => setProductName(event.target.value)}
            fullWidth
            disabled={fieldsDisabled}
          />

          <CatalogSelect
            label="Tela"
            value={fabric}
            onChange={setFabric}
            options={catalogs.fabrics}
            disabled={fieldsDisabled}
            required
          />

          <TextField
            label="Color de tela / base"
            value={color}
            onChange={(event) => setColor(event.target.value)}
            fullWidth
            disabled={fieldsDisabled}
            helperText="Color de la tela, no del diseño. En productos sublimados use Blanco."
          />

          <CatalogSelect
            label="Tipo de prenda"
            value={garmentType}
            onChange={setGarmentType}
            options={catalogs.garmentTypes}
            disabled={fieldsDisabled}
          />
          <CatalogSelect
            label="Tipo de cuello"
            value={collarType}
            onChange={setCollarType}
            options={catalogs.collarTypes}
            disabled={fieldsDisabled}
          />
          <CatalogSelect
            label="Tipo de manga"
            value={sleeveType}
            onChange={setSleeveType}
            options={catalogs.sleeveTypes}
            disabled={fieldsDisabled}
          />
          <CatalogSelect
            label="Lleva puño"
            value={cuffRequired}
            onChange={setCuffRequired}
            options={toSelectOptions(catalogs.cuffOptions)}
            disabled={fieldsDisabled}
          />

          <TextField
            label="Cantidad"
            type="number"
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
            fullWidth
            disabled={fieldsDisabled}
          />

          <TextField
            label="Precio unitario"
            type="number"
            value={unitPrice}
            onChange={(event) => setUnitPrice(event.target.value)}
            fullWidth
            disabled={fieldsDisabled}
            helperText="El total de la línea se calcula automáticamente: cantidad × precio unitario."
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
          disabled={fieldsDisabled || !fabric}
        >
          {submitting ? 'Agregando...' : 'Agregar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AddQuotationItemDialog
