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

function GenerateOccurrencesDialog({
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
      <DialogTitle>Generar pagos</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {(validationError || errorMessage) && (
            <Alert severity="error">{validationError || errorMessage}</Alert>
          )}
          <Alert severity="info">
            Genera ocurrencias PENDING para obligaciones activas en el rango
            indicado. No crea movimientos financieros.
          </Alert>
          <TextField
            label="Desde"
            type="date"
            value={fromDate}
            onChange={(event) => {
              setFromDate(event.target.value)
              setValidationError('')
            }}
            InputLabelProps={{ shrink: true }}
            fullWidth
            disabled={submitting}
          />
          <TextField
            label="Hasta"
            type="date"
            value={toDate}
            onChange={(event) => {
              setToDate(event.target.value)
              setValidationError('')
            }}
            InputLabelProps={{ shrink: true }}
            fullWidth
            disabled={submitting}
          />
          {result ? (
            <Stack spacing={0.5}>
              <Typography variant="subtitle2">Resultado</Typography>
              <Typography variant="body2">
                Creadas: {result.occurrencesCreated}
              </Typography>
              <Typography variant="body2">
                Ya existentes: {result.occurrencesAlreadyExisting}
              </Typography>
              <Typography variant="body2">
                Obligaciones evaluadas: {result.obligationsEvaluated}
              </Typography>
              <Typography variant="body2">
                Omitidas (inactivas): {result.occurrencesSkippedInactive}
              </Typography>
              <Typography variant="body2">
                Omitidas (fuera de vigencia):{' '}
                {result.occurrencesSkippedOutsideValidity}
              </Typography>
            </Stack>
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

export default GenerateOccurrencesDialog
