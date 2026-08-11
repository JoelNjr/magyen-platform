import { useEffect, useMemo, useState } from 'react'
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
import { formatProductionMaterialCost } from '../presentation/productionCostPresentation'

const EMPTY_FORM = {
  operatorEmployeeId: '',
  workDate: '',
  operation: '',
  quantity: '',
  unitOfMeasure: 'UNIT',
  unitRate: '',
  observation: '',
}

function toIsoDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function RegisterProductionLaborDialog({
  open,
  operators,
  operatorsLoading,
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
      workDate: toIsoDate(),
      operatorEmployeeId: operators[0]?.employeeId || '',
    })
  }, [open, operators])

  const previewTotal = useMemo(() => {
    const quantity = Number(form.quantity)
    const unitRate = Number(form.unitRate)
    if (
      Number.isNaN(quantity) ||
      Number.isNaN(unitRate) ||
      quantity <= 0 ||
      unitRate < 0
    ) {
      return null
    }
    return Math.round(quantity * unitRate * 100) / 100
  }, [form.quantity, form.unitRate])

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

    if (!operators.length) {
      setValidationError('No hay operarios disponibles para producción.')
      return
    }

    const operation = form.operation.trim()
    const unitOfMeasure = form.unitOfMeasure.trim()
    const quantity = Number(form.quantity)
    const unitRate = Number(form.unitRate)

    if (
      !form.operatorEmployeeId ||
      !form.workDate ||
      !operation ||
      !unitOfMeasure ||
      form.quantity === '' ||
      form.unitRate === ''
    ) {
      setValidationError(
        'Operario, fecha, operación, cantidad, unidad y tarifa son obligatorios.'
      )
      return
    }

    if (Number.isNaN(quantity) || quantity <= 0) {
      setValidationError('La cantidad debe ser mayor que cero.')
      return
    }

    if (Number.isNaN(unitRate) || unitRate < 0) {
      setValidationError('La tarifa no puede ser negativa.')
      return
    }

    if (quantity * unitRate <= 0) {
      setValidationError('El total a pagar debe ser mayor que cero.')
      return
    }

    onSubmit({
      operatorEmployeeId: form.operatorEmployeeId,
      workDate: form.workDate,
      operation,
      quantity,
      unitOfMeasure,
      unitRate,
      observation: form.observation.trim() || null,
    })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Registrar mano de obra</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          {!operatorsLoading && operators.length === 0 ? (
            <Alert severity="warning">
              No hay operarios disponibles para producción.
            </Alert>
          ) : null}
          <TextField
            select
            label="Operario"
            value={form.operatorEmployeeId}
            onChange={(event) =>
              updateField('operatorEmployeeId', event.target.value)
            }
            required
            fullWidth
            disabled={!operators.length}
          >
            {operators.map((operator) => (
              <MenuItem key={operator.employeeId} value={operator.employeeId}>
                {operator.displayName}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Fecha"
            type="date"
            value={form.workDate}
            onChange={(event) => updateField('workDate', event.target.value)}
            required
            fullWidth
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            label="Operación"
            value={form.operation}
            onChange={(event) => updateField('operation', event.target.value)}
            required
            fullWidth
            placeholder="Ej. Confección"
          />
          <TextField
            label="Cantidad"
            value={form.quantity}
            onChange={(event) => updateField('quantity', event.target.value)}
            required
            fullWidth
            inputProps={{ inputMode: 'decimal' }}
          />
          <TextField
            label="Unidad"
            value={form.unitOfMeasure}
            onChange={(event) =>
              updateField('unitOfMeasure', event.target.value)
            }
            required
            fullWidth
          />
          <TextField
            label="Tarifa"
            value={form.unitRate}
            onChange={(event) => updateField('unitRate', event.target.value)}
            required
            fullWidth
            inputProps={{ inputMode: 'decimal' }}
          />
          <TextField
            label="Observación"
            value={form.observation}
            onChange={(event) => updateField('observation', event.target.value)}
            fullWidth
            multiline
            minRows={2}
          />
          <Typography variant="subtitle1">
            Total a pagar:{' '}
            {formatProductionMaterialCost(previewTotal) ?? '—'}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            El total definitivo lo calcula el servidor.
          </Typography>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Volver
        </Button>
        <Button
          type="button"
          variant="contained"
          onClick={handleSubmit}
          disabled={submitting || !operators.length}
        >
          {submitting ? 'Guardando...' : 'Registrar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default RegisterProductionLaborDialog
