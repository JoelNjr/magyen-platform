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

const PRIORITY_OPTIONS = [
  { value: 'LOW', label: 'Baja' },
  { value: 'NORMAL', label: 'Normal' },
  { value: 'HIGH', label: 'Alta' },
  { value: 'URGENT', label: 'Urgente' },
]

function PlanProductionOrderDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  initialValues,
}) {
  const [plannedStartDate, setPlannedStartDate] = useState('')
  const [plannedEndDate, setPlannedEndDate] = useState('')
  const [priority, setPriority] = useState('NORMAL')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setPlannedStartDate('')
      setPlannedEndDate('')
      setPriority('NORMAL')
      setValidationError('')
      return
    }

    setPlannedStartDate(initialValues?.plannedStartDate || '')
    setPlannedEndDate(initialValues?.plannedEndDate || '')
    setPriority(initialValues?.priority || 'NORMAL')
    setValidationError('')
  }, [open, initialValues])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    if (!plannedStartDate || !plannedEndDate) {
      setValidationError('Las fechas de inicio y fin son obligatorias.')
      return
    }

    if (plannedEndDate < plannedStartDate) {
      setValidationError(
        'La fecha de fin planificada no puede ser anterior a la de inicio.'
      )
      return
    }

    if (!priority) {
      setValidationError('La prioridad es obligatoria.')
      return
    }

    setValidationError('')
    onSubmit({
      plannedStartDate,
      plannedEndDate,
      priority,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Planificar orden de producción</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          <TextField
            label="Inicio planificado"
            type="date"
            value={plannedStartDate}
            onChange={(event) => {
              setPlannedStartDate(event.target.value)
              setValidationError('')
            }}
            fullWidth
            disabled={submitting}
            InputLabelProps={{ shrink: true }}
          />

          <TextField
            label="Fin planificado"
            type="date"
            value={plannedEndDate}
            onChange={(event) => {
              setPlannedEndDate(event.target.value)
              setValidationError('')
            }}
            fullWidth
            disabled={submitting}
            InputLabelProps={{ shrink: true }}
          />

          <TextField
            select
            label="Prioridad"
            value={priority}
            onChange={(event) => {
              setPriority(event.target.value)
              setValidationError('')
            }}
            fullWidth
            disabled={submitting}
          >
            {PRIORITY_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
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
          {submitting ? 'Guardando...' : 'Planificar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default PlanProductionOrderDialog
