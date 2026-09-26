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
import { calendarMonthBoundsFromInput, yearMonthInputValue } from '../presentation/financePresentation'

function GeneratePayrollPeriodsDialog({
  open,
  onClose,
  onSubmit,
  submitting,
  errorMessage,
  result,
}) {
  const [fromMonth, setFromMonth] = useState('')
  const [toMonth, setToMonth] = useState('')
  const [validationError, setValidationError] = useState('')

  useEffect(() => {
    if (!open) {
      setValidationError('')
      return
    }

    const currentMonth = yearMonthInputValue()
    setFromMonth(currentMonth)
    setToMonth(currentMonth)
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

    if (!fromMonth || !toMonth) {
      setValidationError('El mes inicial y el mes final son obligatorios.')
      return
    }

    if (fromMonth > toMonth) {
      setValidationError('El mes inicial no puede ser posterior al mes final.')
      return
    }

    setValidationError('')
    const fromDate = calendarMonthBoundsFromInput(fromMonth).fromDate
    const toDate = calendarMonthBoundsFromInput(toMonth).toDate
    onSubmit({ fromDate, toDate })
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Generar nómina mensual</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          <Alert severity="info">
            Crea un periodo por mes, del día 1 al último día. El pago corresponde
            a la nómina completa de ese mes. Generar no crea un gasto en caja.
          </Alert>
          <TextField
            label="Mes inicial"
            type="month"
            value={fromMonth}
            onChange={(event) => {
              setFromMonth(event.target.value)
              setValidationError('')
            }}
            fullWidth
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            label="Mes final"
            type="month"
            value={toMonth}
            onChange={(event) => {
              setToMonth(event.target.value)
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
