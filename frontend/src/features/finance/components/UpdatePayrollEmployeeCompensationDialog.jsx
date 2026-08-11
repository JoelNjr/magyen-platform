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

function UpdatePayrollEmployeeCompensationDialog({
  open,
  employee,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
}) {
  const [displayName, setDisplayName] = useState('')
  const [fixedAmount, setFixedAmount] = useState('')
  const [effectiveFrom, setEffectiveFrom] = useState('')
  const [effectiveTo, setEffectiveTo] = useState('')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open || !employee) {
      setValidationError('')
      return
    }

    setDisplayName(employee.displayName || '')
    setFixedAmount(
      employee.fixedAmount === null || employee.fixedAmount === undefined
        ? ''
        : String(employee.fixedAmount)
    )
    setEffectiveFrom(employee.effectiveFrom || '')
    setEffectiveTo(employee.effectiveTo || '')
    setValidationError('')
  }, [open, employee])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  function handleSubmit() {
    if (submitting || !employee) {
      return
    }

    const name = displayName.trim()
    if (!name) {
      setValidationError('El nombre es obligatorio.')
      return
    }

    if (employee.compensationType === 'PRODUCTION_BASED') {
      onSubmit({ displayName: name })
      return
    }

    const amountRaw = fixedAmount.trim()
    if (!amountRaw || !effectiveFrom) {
      setValidationError('Valor fijo y vigencia desde son obligatorios.')
      return
    }

    const amount = Number(amountRaw)
    if (Number.isNaN(amount) || amount <= 0) {
      setValidationError('El valor fijo debe ser un número mayor que cero.')
      return
    }

    if (effectiveTo && effectiveTo < effectiveFrom) {
      setValidationError('La vigencia hasta no puede ser anterior a desde.')
      return
    }

    onSubmit({
      displayName: name,
      fixedAmount: amount,
      effectiveFrom,
      effectiveTo: effectiveTo || null,
    })
  }

  const isFixed = employee?.compensationType === 'FIXED_PAYROLL'

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Editar compensación</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          {isFixed ? (
            <Alert severity="info">
              Los períodos ya generados conservan su monto histórico. El nuevo
              valor aplica solo a períodos futuros.
            </Alert>
          ) : null}
          <TextField
            label="Nombre"
            value={displayName}
            onChange={(event) => {
              setDisplayName(event.target.value)
              setValidationError('')
            }}
            required
            fullWidth
          />
          {isFixed ? (
            <>
              <TextField
                label="Valor fijo"
                value={fixedAmount}
                onChange={(event) => {
                  setFixedAmount(event.target.value)
                  setValidationError('')
                }}
                required
                fullWidth
                inputProps={{ inputMode: 'decimal' }}
              />
              <TextField
                label="Vigencia desde"
                type="date"
                value={effectiveFrom}
                onChange={(event) => {
                  setEffectiveFrom(event.target.value)
                  setValidationError('')
                }}
                required
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label="Vigencia hasta (opcional)"
                type="date"
                value={effectiveTo}
                onChange={(event) => {
                  setEffectiveTo(event.target.value)
                  setValidationError('')
                }}
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
            </>
          ) : (
            <Alert severity="info">
              Este empleado es por producción; no tiene valor fijo de nómina.
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
          {submitting ? 'Guardando...' : 'Guardar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default UpdatePayrollEmployeeCompensationDialog
