import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  Skeleton,
  Stack,
  Typography,
} from '@mui/material'
import { formatFinanceMoney, resolveApiErrorMessage } from '../presentation/financePresentation'
import { getPayrollEmployeeFinancialSummary } from '../services/financeService'

function SummaryRow({ label, value }) {
  return (
    <Stack direction="row" justifyContent="space-between" spacing={2}>
      <Typography color="text.secondary">{label}</Typography>
      <Typography sx={{ fontWeight: 600 }}>{value}</Typography>
    </Stack>
  )
}

function compensationTypeLabel(type) {
  if (type === 'FIXED_PAYROLL') {
    return 'Sueldo fijo'
  }
  if (type === 'PRODUCTION_BASED') {
    return 'Pago por producción'
  }
  return type || '—'
}

function PayrollEmployeeFinancialSummaryDialog({ open, employee, onClose }) {
  const [loading, setLoading] = useState(false)
  const [failed, setFailed] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [summary, setSummary] = useState(null)

  useEffect(() => {
    if (!open || !employee) {
      setSummary(null)
      setFailed(false)
      setErrorMessage('')
      return
    }

    let cancelled = false
    setLoading(true)
    setFailed(false)
    setErrorMessage('')
    getPayrollEmployeeFinancialSummary(employee.employeeId)
      .then((data) => {
        if (!cancelled) {
          setSummary(data)
          setLoading(false)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setSummary(null)
          setFailed(true)
          setErrorMessage(
            resolveApiErrorMessage(
              error,
              'No fue posible cargar el resumen del empleado.'
            )
          )
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [open, employee])

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{employee?.displayName || 'Empleado'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {failed ? (
            <Alert
              severity="error"
              action={
                <Button
                  color="inherit"
                  size="small"
                  onClick={() => {
                    if (!employee) {
                      return
                    }
                    setFailed(false)
                    setLoading(true)
                    getPayrollEmployeeFinancialSummary(employee.employeeId)
                      .then((data) => {
                        setSummary(data)
                        setLoading(false)
                      })
                      .catch((error) => {
                        setFailed(true)
                        setErrorMessage(
                          resolveApiErrorMessage(
                            error,
                            'No fue posible cargar el resumen del empleado.'
                          )
                        )
                        setLoading(false)
                      })
                  }}
                >
                  Reintentar
                </Button>
              }
            >
              {errorMessage}
            </Alert>
          ) : null}

          {loading ? (
            <Stack spacing={1}>
              <Skeleton variant="text" width="50%" />
              <Skeleton variant="rectangular" height={120} />
            </Stack>
          ) : null}

          {!loading && !failed && summary ? (
            <Stack spacing={2}>
              <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                <Chip
                  size="small"
                  label={`Tipo: ${compensationTypeLabel(summary.compensationType)}`}
                />
                <Chip
                  size="small"
                  color={summary.active ? 'success' : 'default'}
                  label={summary.active ? 'Activo' : 'Inactivo'}
                />
              </Stack>

              <Alert severity="info">
                Comisión y producción son acumulado / pendiente de liquidación.
                No son un gasto de Finanzas todavía y no representan un salario neto.
              </Alert>

              {summary.sellerCommissionApplicable ? (
                <Stack spacing={1}>
                  <Typography variant="subtitle1">Ventas y comisión</Typography>
                  <SummaryRow
                    label="Ventas realizadas"
                    value={summary.numberOfEligibleOrders ?? 0}
                  />
                  <SummaryRow
                    label="Total vendido"
                    value={formatFinanceMoney(summary.totalSales)}
                  />
                  <SummaryRow
                    label="Comisión acumulada (5%)"
                    value={formatFinanceMoney(summary.accumulatedCommission)}
                  />
                </Stack>
              ) : null}

              {summary.productionLaborApplicable ? (
                <Stack spacing={1}>
                  <Typography variant="subtitle1">Producción</Typography>
                  <SummaryRow
                    label="Producción generada"
                    value={formatFinanceMoney(summary.productionGenerated)}
                  />
                  <SummaryRow
                    label="Producción pagada"
                    value={formatFinanceMoney(summary.productionPaid)}
                  />
                  <SummaryRow
                    label="Producción pendiente"
                    value={formatFinanceMoney(summary.productionPending)}
                  />
                </Stack>
              ) : null}

              <Divider />

              <Stack spacing={1}>
                <Typography variant="subtitle1">Deducciones</Typography>
                <SummaryRow
                  label="Deducciones activas"
                  value={summary.activeDeductionCount ?? 0}
                />
                <SummaryRow
                  label="Total de deducciones activas"
                  value={formatFinanceMoney(summary.activeDeductionTotal)}
                />
              </Stack>
            </Stack>
          ) : null}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button type="button" onClick={onClose}>
          Volver
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default PayrollEmployeeFinancialSummaryDialog
