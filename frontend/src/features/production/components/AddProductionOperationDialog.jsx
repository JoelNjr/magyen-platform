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
import { formatProductionOperationType } from '../presentation/productionStatusPresentation'

const OPERATION_TYPE_OPTIONS = [
  'CUTTING',
  'CALENDERING',
  'SUBLIMATION',
  'SEWING',
  'QUALITY_CONTROL',
]

function AddProductionOperationDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  usedTypes = [],
}) {
  const [type, setType] = useState('')
  const [plannedStartDate, setPlannedStartDate] = useState('')
  const [plannedEndDate, setPlannedEndDate] = useState('')
  const [observations, setObservations] = useState('')
  const [validationError, setValidationError] = useState('')

  const availableTypes = OPERATION_TYPE_OPTIONS.filter(
    (option) => !usedTypes.includes(option)
  )

  useEffect(() => {
    if (!open) {
      setType('')
      setPlannedStartDate('')
      setPlannedEndDate('')
      setObservations('')
      setValidationError('')
      return
    }

    const nextAvailableTypes = OPERATION_TYPE_OPTIONS.filter(
      (option) => !usedTypes.includes(option)
    )

    setType(nextAvailableTypes[0] || '')
    setPlannedStartDate('')
    setPlannedEndDate('')
    setObservations('')
    setValidationError('')
  }, [open, usedTypes])

  function handleClose() {
    if (submitting) {
      return
    }

    onClose()
  }

  function handleSubmit() {
    if (!type) {
      setValidationError('El tipo de operación es obligatorio.')
      return
    }

    if (
      plannedStartDate &&
      plannedEndDate &&
      plannedEndDate < plannedStartDate
    ) {
      setValidationError(
        'La fecha de fin planificada no puede ser anterior a la de inicio.'
      )
      return
    }

    setValidationError('')
    onSubmit({
      type,
      plannedStartDate: plannedStartDate || null,
      plannedEndDate: plannedEndDate || null,
      observations: observations.trim() || null,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Agregar operación</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}

          {availableTypes.length === 0 ? (
            <Alert severity="warning">
              Esta orden ya tiene todos los tipos de operación disponibles.
            </Alert>
          ) : (
            <>
              <TextField
                select
                label="Tipo"
                value={type}
                onChange={(event) => {
                  setType(event.target.value)
                  setValidationError('')
                }}
                fullWidth
                disabled={submitting}
              >
                {availableTypes.map((option) => (
                  <MenuItem key={option} value={option}>
                    {formatProductionOperationType(option)}
                  </MenuItem>
                ))}
              </TextField>

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
                label="Observaciones"
                value={observations}
                onChange={(event) => setObservations(event.target.value)}
                fullWidth
                multiline
                minRows={2}
                disabled={submitting}
              />
            </>
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
          disabled={submitting || availableTypes.length === 0}
        >
          {submitting ? 'Guardando...' : 'Agregar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AddProductionOperationDialog
