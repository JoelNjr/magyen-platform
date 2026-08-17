import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { updateOrderItemProductSpecification } from '../services/commercialService'
import CatalogSelect from './CatalogSelect'
import {
  toSelectOptions,
  useCommercialCatalogs,
  withCurrentOption,
} from './useCommercialCatalogs'

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function emptyFormState() {
  return {
    garmentType: '',
    collarType: '',
    sleeveType: '',
    cuffRequired: '',
    sublimationRequired: false,
    embroideryRequired: false,
    dtfRequired: false,
    decorationNotes: '',
    includesNames: false,
    includesNumbers: false,
    includesLogos: false,
    personalizationNotes: '',
    itemObservations: '',
  }
}

function toCuffSelectValue(value) {
  if (value === true) {
    return 'true'
  }
  if (value === false) {
    return 'false'
  }
  return ''
}

function toFormState(specification) {
  const source = specification ?? {}

  return {
    garmentType: source.garmentType ?? '',
    collarType: source.collarType ?? '',
    sleeveType: source.sleeveType ?? '',
    cuffRequired: toCuffSelectValue(source.cuffRequired),
    sublimationRequired: Boolean(source.sublimationRequired),
    embroideryRequired: Boolean(source.embroideryRequired),
    dtfRequired: Boolean(source.dtfRequired),
    decorationNotes: source.decorationNotes ?? '',
    includesNames: Boolean(source.includesNames),
    includesNumbers: Boolean(source.includesNumbers),
    includesLogos: Boolean(source.includesLogos),
    personalizationNotes: source.personalizationNotes ?? '',
    itemObservations: source.itemObservations ?? '',
  }
}

function ManageOrderItemProductSpecificationDialog({
  open,
  onClose,
  orderId,
  orderItem,
  onSaved,
}) {
  const { catalogs, loading: catalogsLoading, failed: catalogsFailed } =
    useCommercialCatalogs()
  const [form, setForm] = useState(emptyFormState)
  const [submitting, setSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    if (!open || !orderItem) {
      return
    }

    setForm(toFormState(orderItem.productSpecification))
    setSubmitting(false)
    setErrorMessage('')
  }, [open, orderItem])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    setErrorMessage('')
  }

  function toPayload() {
    return {
      garmentType: form.garmentType.trim() || null,
      collarType: form.collarType.trim() || null,
      sleeveType: form.sleeveType.trim() || null,
      cuffRequired: form.cuffRequired === '' ? null : form.cuffRequired === 'true',
      sublimationRequired: form.sublimationRequired,
      embroideryRequired: form.embroideryRequired,
      dtfRequired: form.dtfRequired,
      decorationNotes: form.decorationNotes.trim() || null,
      includesNames: form.includesNames,
      includesNumbers: form.includesNumbers,
      includesLogos: form.includesLogos,
      personalizationNotes: form.personalizationNotes.trim() || null,
      itemObservations: form.itemObservations.trim() || null,
    }
  }

  async function handleSave() {
    setSubmitting(true)
    setErrorMessage('')

    try {
      await updateOrderItemProductSpecification(
        orderId,
        orderItem.itemId,
        toPayload()
      )
      onSaved()
    } catch (error) {
      setErrorMessage(
        resolveApiErrorMessage(
          error,
          'No fue posible actualizar las especificaciones.'
        )
      )
      setSubmitting(false)
    }
  }

  if (!orderItem) {
    return null
  }

  const fieldsDisabled = submitting || catalogsLoading

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="md">
      <DialogTitle>Editar especificaciones</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}
          {catalogsFailed && (
            <Alert severity="warning">
              No fue posible cargar los catálogos comerciales.
            </Alert>
          )}

          <Stack spacing={0.5}>
            <Typography variant="h6">{orderItem.productName}</Typography>
            <Typography variant="body2" color="text.secondary">
              Cantidad: {orderItem.quantity}
            </Typography>
          </Stack>

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
            <CatalogSelect
              label="Tipo de prenda"
              value={form.garmentType}
              onChange={(value) => updateField('garmentType', value)}
              options={withCurrentOption(
                catalogs.garmentTypes,
                form.garmentType
              )}
              disabled={fieldsDisabled}
            />
            <CatalogSelect
              label="Tipo de cuello"
              value={form.collarType}
              onChange={(value) => updateField('collarType', value)}
              options={withCurrentOption(catalogs.collarTypes, form.collarType)}
              disabled={fieldsDisabled}
            />
          </Stack>

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
            <CatalogSelect
              label="Tipo de manga"
              value={form.sleeveType}
              onChange={(value) => updateField('sleeveType', value)}
              options={withCurrentOption(catalogs.sleeveTypes, form.sleeveType)}
              disabled={fieldsDisabled}
            />
            <CatalogSelect
              label="Lleva puño"
              value={form.cuffRequired}
              onChange={(value) => updateField('cuffRequired', value)}
              options={toSelectOptions(catalogs.cuffOptions)}
              disabled={fieldsDisabled}
            />
          </Stack>

          <Typography variant="subtitle2">Decoración</Typography>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
            <FormControlLabel
              control={
                <Checkbox
                  checked={form.sublimationRequired}
                  onChange={(event) =>
                    updateField('sublimationRequired', event.target.checked)
                  }
                  disabled={submitting}
                />
              }
              label="Sublimación"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={form.embroideryRequired}
                  onChange={(event) =>
                    updateField('embroideryRequired', event.target.checked)
                  }
                  disabled={submitting}
                />
              }
              label="Bordado"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={form.dtfRequired}
                  onChange={(event) =>
                    updateField('dtfRequired', event.target.checked)
                  }
                  disabled={submitting}
                />
              }
              label="DTF"
            />
          </Stack>

          <Typography variant="subtitle2">Personalización</Typography>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
            <FormControlLabel
              control={
                <Checkbox
                  checked={form.includesNames}
                  onChange={(event) =>
                    updateField('includesNames', event.target.checked)
                  }
                  disabled={submitting}
                />
              }
              label="Incluye nombres"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={form.includesNumbers}
                  onChange={(event) =>
                    updateField('includesNumbers', event.target.checked)
                  }
                  disabled={submitting}
                />
              }
              label="Incluye números"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={form.includesLogos}
                  onChange={(event) =>
                    updateField('includesLogos', event.target.checked)
                  }
                  disabled={submitting}
                />
              }
              label="Incluye logos"
            />
          </Stack>

          <TextField
            label="Notas de decoración"
            value={form.decorationNotes}
            onChange={(event) =>
              updateField('decorationNotes', event.target.value)
            }
            fullWidth
            multiline
            minRows={2}
            disabled={submitting}
          />

          <TextField
            label="Notas de personalización"
            value={form.personalizationNotes}
            onChange={(event) =>
              updateField('personalizationNotes', event.target.value)
            }
            fullWidth
            multiline
            minRows={2}
            disabled={submitting}
          />

          <TextField
            label="Observaciones del producto"
            value={form.itemObservations}
            onChange={(event) =>
              updateField('itemObservations', event.target.value)
            }
            fullWidth
            multiline
            minRows={2}
            disabled={submitting}
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
          onClick={handleSave}
          disabled={fieldsDisabled}
        >
          {submitting ? 'Guardando...' : 'Guardar especificaciones'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ManageOrderItemProductSpecificationDialog
