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
import {
  formatFinanceDate,
  formatFinanceMoney,
  getCalendarMonthRange,
  getPayrollCompensationTypeLabel,
  resolveApiErrorMessage,
} from '../presentation/financePresentation'
import { getPayrollEmployeeProductionEarnings } from '../services/financeService'

function formatQuantity(value) {
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return '0'
  }
  return new Intl.NumberFormat('es-CO', {
    maximumFractionDigits: 4,
  }).format(amount)
}

function PayrollEmployeeProductionEarningsDialog({
  open,
  employee,
  onClose,
}) {
  const defaultRange = getCalendarMonthRange()
  const [fromDate, setFromDate] = useState(defaultRange.fromDate)
  const [toDate, setToDate] = useState(defaultRange.toDate)
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [earnings, setEarnings] = useState(null)

  useEffect(() => {
    if (!open || !employee) {
      setEarnings(null)
      setErrorMessage('')
      return
    }

    const range = getCalendarMonthRange()
    setFromDate(range.fromDate)
    setToDate(range.toDate)
  }, [open, employee])

  useEffect(() => {
    if (!open || !employee || !fromDate || !toDate) {
      return
    }

    let cancelled = false
    setLoading(true)
    setErrorMessage('')
    getPayrollEmployeeProductionEarnings(employee.employeeId, { fromDate, toDate })
      .then((data) => {
        if (!cancelled) {
          setEarnings(data)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setEarnings(null)
          setErrorMessage(
            resolveApiErrorMessage(
              error,
              'No fue posible consultar la mano de obra de este empleado.'
            )
          )
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [open, employee, fromDate, toDate])

  function handleClose() {
    onClose()
  }

  const productionApplicable = earnings?.productionLaborApplicable === true

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>
        {employee?.displayName || 'Empleado'}
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Typography>
            Tipo de pago:{' '}
            {getPayrollCompensationTypeLabel(employee?.compensationType)}
          </Typography>
          {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
          {earnings && !productionApplicable ? (
            <Alert severity="info">
              Este empleado recibe pago fijo y no registra mano de obra por
              producción.
            </Alert>
          ) : null}
          {productionApplicable || (!earnings && employee?.compensationType === 'PRODUCTION_BASED') ? (
            <>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <TextField
                  label="Desde"
                  type="date"
                  value={fromDate}
                  onChange={(event) => setFromDate(event.target.value)}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                />
                <TextField
                  label="Hasta"
                  type="date"
                  value={toDate}
                  onChange={(event) => setToDate(event.target.value)}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                />
              </Stack>
              {loading ? (
                <Typography color="text.secondary">Consultando...</Typography>
              ) : earnings && productionApplicable ? (
                <Stack spacing={0.5}>
                  <Typography variant="subtitle1">
                    {formatFinanceDate(earnings.fromDate)} –{' '}
                    {formatFinanceDate(earnings.toDate)}
                  </Typography>
                  <Typography>Trabajos: {earnings.laborWorkCount}</Typography>
                  <Typography>
                    Unidades: {formatQuantity(earnings.totalQuantity)}
                  </Typography>
                  <Typography>
                    Generado: {formatFinanceMoney(earnings.totalCalculatedAmount)}
                  </Typography>
                  <Typography>
                    Pagado: {formatFinanceMoney(earnings.totalPaidAmount)}
                  </Typography>
                  <Typography>
                    Pendiente: {formatFinanceMoney(earnings.totalPendingAmount)}
                  </Typography>
                </Stack>
              ) : null}
            </>
          ) : null}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button type="button" onClick={handleClose}>
          Volver
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default PayrollEmployeeProductionEarningsDialog
