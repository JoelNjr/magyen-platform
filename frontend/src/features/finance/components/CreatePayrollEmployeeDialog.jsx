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
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material'
import { toIsoDate } from '../presentation/financePresentation'

const EMPTY_FORM = {
  displayName: '',
  compensationType: 'FIXED_PAYROLL',
  fixedAmount: '',
  effectiveFrom: '',
  effectiveTo: '',
}

function CreatePayrollEmployeeDialog({
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
      effectiveFrom: toIsoDate(new Date()),
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

    const displayName = form.displayName.trim()
    if (!displayName || !form.compensationType) {
      setValidationError('Nombre y tipo de pago son obligatorios.')
      return
    }

    if (form.compensationType === 'PRODUCTION_BASED') {
      onSubmit({
        displayName,
        compensationType: 'PRODUCTION_BASED',
      })
      return
    }

    const fixedAmountRaw = form.fixedAmount.trim()
    if (!fixedAmountRaw || !form.effectiveFrom) {
      setValidationError(
        'Para pago fijo, valor fijo y vigencia desde son obligatorios.'
      )
      return
    }

    const fixedAmount = Number(fixedAmountRaw)
    if (Number.isNaN(fixedAmount) || fixedAmount <= 0) {
      setValidationError('El valor fijo debe ser un número mayor que cero.')
      return
    }

    if (form.effectiveTo && form.effectiveTo < form.effectiveFrom) {
      setValidationError('La vigencia hasta no puede ser anterior a desde.')
      return
    }

    onSubmit({
      displayName,
      compensationType: 'FIXED_PAYROLL',
      fixedAmount,
      effectiveFrom: form.effectiveFrom,
      effectiveTo: form.effectiveTo || null,
    })
  }

  const isFixed = form.compensationType === 'FIXED_PAYROLL'

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Crear empleado</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          <TextField
            label="Nombre"
            value={form.displayName}
            onChange={(event) => updateField('displayName', event.target.value)}
            required
            fullWidth
          />
          <Stack spacing={1}>
            <Typography variant="body2">Tipo de pago</Typography>
            <ToggleButtonGroup
              exclusive
              fullWidth
              value={form.compensationType}
              onChange={(_, value) => {
                if (value) {
                  updateField('compensationType', value)
                }
              }}
            >
              <ToggleButton value="FIXED_PAYROLL">Fijo</ToggleButton>
              <ToggleButton value="PRODUCTION_BASED">Por producción</ToggleButton>
            </ToggleButtonGroup>
          </Stack>
          {isFixed ? (
            <>
              <TextField
                label="Valor fijo"
                value={form.fixedAmount}
                onChange={(event) =>
                  updateField('fixedAmount', event.target.value)
                }
                required
                fullWidth
                inputProps={{ inputMode: 'decimal' }}
              />
              <TextField
                label="Vigencia desde"
                type="date"
                value={form.effectiveFrom}
                onChange={(event) =>
                  updateField('effectiveFrom', event.target.value)
                }
                required
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label="Vigencia hasta (opcional)"
                type="date"
                value={form.effectiveTo}
                onChange={(event) =>
                  updateField('effectiveTo', event.target.value)
                }
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
            </>
          ) : (
            <Alert severity="info">
              Este empleado podrá seleccionarse en Producción → Mano de obra.
              No genera nómina fija automática.
            </Alert>
          )}
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
          disabled={submitting}
        >
          {submitting ? 'Guardando...' : 'Crear empleado'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default CreatePayrollEmployeeDialog
