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

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function emptyFormState() {
  return {
    garmentType: '',
    collarType: '',
    sleeveType: '',
    garmentVariant: '',
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

function toFormState(specification) {
  const source = specification ?? {}

  return {
    garmentType: source.garmentType ?? '',
    collarType: source.collarType ?? '',
    sleeveType: source.sleeveType ?? '',
    garmentVariant: source.garmentVariant ?? '',
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
      garmentVariant: form.garmentVariant.trim() || null,
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

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="md">
      <DialogTitle>Editar especificaciones</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

          <Stack spacing={0.5}>
            <Typography variant="h6">{orderItem.productName}</Typography>
            <Typography variant="body2" color="text.secondary">
              Cantidad: {orderItem.quantity}
            </Typography>
          </Stack>

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
            <TextField
              label="Tipo de prenda"
              value={form.garmentType}
              onChange={(event) => updateField('garmentType', event.target.value)}
              fullWidth
              disabled={submitting}
            />
            <TextField
              label="Tipo de cuello"
              value={form.collarType}
              onChange={(event) => updateField('collarType', event.target.value)}
              fullWidth
              disabled={submitting}
            />
          </Stack>

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
            <TextField
              label="Tipo de manga"
              value={form.sleeveType}
              onChange={(event) => updateField('sleeveType', event.target.value)}
              fullWidth
              disabled={submitting}
            />
            <TextField
              label="Variante"
              value={form.garmentVariant}
              onChange={(event) =>
                updateField('garmentVariant', event.target.value)
              }
              fullWidth
              disabled={submitting}
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
          disabled={submitting}
        >
          {submitting ? 'Guardando...' : 'Guardar especificaciones'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ManageOrderItemProductSpecificationDialog
