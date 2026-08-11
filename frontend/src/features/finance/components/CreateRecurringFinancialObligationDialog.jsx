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
} from '@mui/material'
import {
  FREQUENCY_OPTIONS,
  OBLIGATION_TYPE_OPTIONS,
  toIsoDate,
} from '../presentation/financePresentation'

const EMPTY_FORM = {
  name: '',
  type: 'SERVICE',
  expectedAmount: '',
  frequency: 'MONTHLY',
  dueDay: '',
  startDate: '',
  endDate: '',
  description: '',
  observation: '',
}

function CreateRecurringFinancialObligationDialog({
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
      return
    }

    setForm({
      ...EMPTY_FORM,
      startDate: toIsoDate(new Date()),
    })
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    setValidationError('')
  }

  function handleSubmit() {
    if (submitting) {
      return
    }

    const name = form.name.trim()
    const expectedAmountRaw = form.expectedAmount.trim()
    const dueDayRaw = form.dueDay.trim()
    const description = form.description.trim()
    const observation = form.observation.trim()

    if (!name || !form.type || !expectedAmountRaw || !form.frequency || !form.startDate) {
      setValidationError(
        'Nombre, tipo, monto, frecuencia y fecha de inicio son obligatorios.'
      )
      return
    }

    const expectedAmount = Number(expectedAmountRaw)
    if (Number.isNaN(expectedAmount) || expectedAmount <= 0) {
      setValidationError('El monto debe ser un número mayor que cero.')
      return
    }

    let dueDay = null
    if (dueDayRaw) {
      dueDay = Number(dueDayRaw)
      if (!Number.isInteger(dueDay) || dueDay < 1 || dueDay > 31) {
        setValidationError('El día de vencimiento debe ser un entero entre 1 y 31.')
        return
      }
    }

    if (form.endDate && form.endDate < form.startDate) {
      setValidationError('La fecha fin no puede ser anterior a la fecha de inicio.')
      return
    }

    onSubmit({
      name,
      type: form.type,
      expectedAmount,
      frequency: form.frequency,
      dueDay,
      startDate: form.startDate,
      endDate: form.endDate || null,
      description: description || null,
      observation: observation || null,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Crear obligación</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          <Alert severity="info">
            Crear una obligación no genera pagos automáticamente. Use Generar
            pagos cuando lo necesite.
          </Alert>
          <TextField
            label="Nombre"
            value={form.name}
            onChange={(event) => updateField('name', event.target.value)}
            fullWidth
            disabled={submitting}
          />
          <TextField
            select
            label="Tipo"
            value={form.type}
            onChange={(event) => updateField('type', event.target.value)}
            fullWidth
            disabled={submitting}
          >
            {OBLIGATION_TYPE_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Monto esperado"
            value={form.expectedAmount}
            onChange={(event) => updateField('expectedAmount', event.target.value)}
            fullWidth
            disabled={submitting}
          />
          <TextField
            select
            label="Frecuencia"
            value={form.frequency}
            onChange={(event) => updateField('frequency', event.target.value)}
            fullWidth
            disabled={submitting}
          >
            {FREQUENCY_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Día de vencimiento"
            value={form.dueDay}
            onChange={(event) => updateField('dueDay', event.target.value)}
            fullWidth
            disabled={submitting}
            helperText="Opcional según frecuencia"
          />
          <TextField
            label="Fecha inicio"
            type="date"
            value={form.startDate}
            onChange={(event) => updateField('startDate', event.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
            disabled={submitting}
          />
          <TextField
            label="Fecha fin"
            type="date"
            value={form.endDate}
            onChange={(event) => updateField('endDate', event.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
            disabled={submitting}
          />
          <TextField
            label="Descripción"
            value={form.description}
            onChange={(event) => updateField('description', event.target.value)}
            fullWidth
            multiline
            minRows={2}
            disabled={submitting}
          />
          <TextField
            label="Observación"
            value={form.observation}
            onChange={(event) => updateField('observation', event.target.value)}
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
          onClick={handleSubmit}
          disabled={submitting}
        >
          {submitting ? 'Creando...' : 'Crear obligación'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreateRecurringFinancialObligationDialog
