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
  Typography,
} from '@mui/material'
import { getDefaultGenerationRange } from '../presentation/financePresentation'

function GeneratePayrollPeriodsDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  result,
}) {
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setValidationError('')
      return
    }

    const defaults = getDefaultGenerationRange()
    setFromDate(defaults.fromDate)
    setToDate(defaults.toDate)
    setValidationError('')
  }, [open])

  function handleClose() {
    if (submitting) {
      return
    }
    onClose()
  }

  function handleSubmit() {
    if (submitting) {
      return
    }

    if (!fromDate || !toDate) {
      setValidationError('Desde y Hasta son obligatorios.')
      return
    }

    if (fromDate > toDate) {
      setValidationError('Desde no puede ser posterior a Hasta.')
      return
    }

    setValidationError('')
    onSubmit({ fromDate, toDate })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Generar nómina</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          <Alert severity="info">
            Genera períodos PENDING para empleados de nómina fija activos. No
            crea gastos en el ledger hasta que se pague cada período.
          </Alert>
          <TextField
            label="Desde"
            type="date"
            value={fromDate}
            onChange={(event) => {
              setFromDate(event.target.value)
              setValidationError('')
            }}
            fullWidth
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            label="Hasta"
            type="date"
            value={toDate}
            onChange={(event) => {
              setToDate(event.target.value)
              setValidationError('')
            }}
            fullWidth
            InputLabelProps={{ shrink: true }}
          />
          {result ? (
            <Alert severity="success">
              <Typography variant="body2">
                Creados: {result.created ?? 0}. Ya existentes:{' '}
                {result.alreadyExisting ?? 0}. Inactivos omitidos:{' '}
                {result.skippedInactive ?? 0}. Por producción omitidos:{' '}
                {result.skippedProductionBased ?? 0}.
              </Typography>
            </Alert>
          ) : null}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button type="button" onClick={handleClose} disabled={submitting}>
          Cerrar
        </Button>
        <Button
          type="button"
          variant="contained"
          onClick={handleSubmit}
          disabled={submitting}
        >
          {submitting ? 'Generando...' : 'Generar'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default GeneratePayrollPeriodsDialog
