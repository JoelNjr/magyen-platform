import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import ConfirmFinanceActionDialog from './ConfirmFinanceActionDialog'
import {
  PAYROLL_DEDUCTION_TYPE_OPTIONS,
  formatFinanceDate,
  formatFinanceMoney,
  getPayrollCompensationTypeLabel,
  getPayrollDeductionStatusLabel,
  getPayrollDeductionTypeLabel,
  resolveApiErrorMessage,
  toIsoDate,
} from '../presentation/financePresentation'
import {
  cancelPayrollDeduction,
  createPayrollDeduction,
  getPayrollEmployeeDeductions,
} from '../services/financeService'

const EMPTY_FORM = {
  type: 'LOAN',
  amount: '',
  deductionDate: '',
  description: '',
}

function PayrollEmployeeDeductionsDialog({
  open,
  employee,
  onClose,
  showSuccess,
}) {
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [summary, setSummary] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')
  const [cancelTarget, setCancelTarget] = useState(null)
  const [cancelling, setCancelling] = useState(false)
  const [cancelError, setCancelError] = useState('')

  useEffect(() => {
    if (!open || !employee) {
      setSummary(null)
      setErrorMessage('')
      setCreateError('')
      setForm(EMPTY_FORM)
      return
    }

    setForm({
      ...EMPTY_FORM,
      deductionDate: toIsoDate(new Date()),
    })
    loadDeductions(employee.employeeId)
  }, [open, employee])

  async function loadDeductions(employeeId) {
    setLoading(true)
    setErrorMessage('')
    try {
      const data = await getPayrollEmployeeDeductions(employeeId)
      setSummary(data)
    } catch (error) {
      setSummary(null)
      setErrorMessage(
        resolveApiErrorMessage(
          error,
          'No fue posible consultar las deducciones de este empleado.'
        )
      )
    } finally {
      setLoading(false)
    }
  }

  function handleClose() {
    if (creating || cancelling) {
      return
    }
    onClose()
  }

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    setCreateError('')
  }

  async function handleCreate() {
    if (!employee || creating) {
      return
    }

    const amountRaw = form.amount.trim()
    if (!form.type || !amountRaw || !form.deductionDate) {
      setCreateError('Tipo, monto y fecha son obligatorios.')
      return
    }

    const amount = Number(amountRaw)
    if (Number.isNaN(amount) || amount <= 0) {
      setCreateError('El monto debe ser un número mayor que cero.')
      return
    }

    setCreating(true)
    setCreateError('')
    try {
      await createPayrollDeduction(employee.employeeId, {
        type: form.type,
        amount,
        deductionDate: form.deductionDate,
        description: form.description.trim() || null,
      })
      setForm({
        ...EMPTY_FORM,
        type: form.type,
        deductionDate: toIsoDate(new Date()),
      })
      await loadDeductions(employee.employeeId)
      showSuccess?.('Descuento registrado.')
    } catch (error) {
      setCreateError(
        resolveApiErrorMessage(error, 'No fue posible registrar el descuento.')
      )
    } finally {
      setCreating(false)
    }
  }

  async function handleConfirmCancel() {
    if (!employee || !cancelTarget || cancelling) {
      return
    }

    setCancelling(true)
    setCancelError('')
    try {
      await cancelPayrollDeduction(employee.employeeId, cancelTarget.deductionId)
      setCancelTarget(null)
      await loadDeductions(employee.employeeId)
      showSuccess?.('Descuento cancelado.')
    } catch (error) {
      setCancelError(
        resolveApiErrorMessage(error, 'No fue posible cancelar el descuento.')
      )
    } finally {
      setCancelling(false)
    }
  }

  const canSell = employee?.compensationType === 'FIXED_PAYROLL'
  const canDoProduction = employee?.compensationType === 'PRODUCTION_BASED'
  const deductions = Array.isArray(summary?.deductions) ? summary.deductions : []

  return (
    <>
      <Dialog open={open} onClose={handleClose} fullWidth maxWidth="md">
        <DialogTitle>{employee?.displayName || 'Empleado'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Typography>
              Tipo de compensación:{' '}
              {getPayrollCompensationTypeLabel(employee?.compensationType)}
            </Typography>
            <Typography>
              Estado: {employee?.active ? 'Activo' : 'Inactivo'}
            </Typography>
            <Typography>Puede vender: {canSell ? 'Sí' : 'No'}</Typography>
            <Typography>
              Puede hacer producción: {canDoProduction ? 'Sí' : 'No'}
            </Typography>
            {canSell && employee?.fixedAmount != null ? (
              <Typography>
                Valor fijo: {formatFinanceMoney(employee.fixedAmount)}
              </Typography>
            ) : null}
            {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}
            <Typography variant="h6">Deducciones</Typography>
            <Typography>
              Descuentos activos: {summary?.activeCount ?? 0} — Total activo:{' '}
              {formatFinanceMoney(summary?.activeTotal ?? 0)}
            </Typography>
            {loading ? (
              <Typography color="text.secondary">Consultando...</Typography>
            ) : (
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Monto</TableCell>
                    <TableCell>Fecha</TableCell>
                    <TableCell>Descripción</TableCell>
                    <TableCell>Estado</TableCell>
                    <TableCell align="right">Acción</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {deductions.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6}>
                        <Typography color="text.secondary">
                          No hay descuentos registrados.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    deductions.map((deduction) => (
                      <TableRow key={deduction.deductionId}>
                        <TableCell>
                          {getPayrollDeductionTypeLabel(deduction.type)}
                        </TableCell>
                        <TableCell>
                          {formatFinanceMoney(deduction.amount)}
                        </TableCell>
                        <TableCell>
                          {formatFinanceDate(deduction.deductionDate)}
                        </TableCell>
                        <TableCell>{deduction.description || '—'}</TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            color={
                              deduction.status === 'ACTIVE'
                                ? 'success'
                                : 'default'
                            }
                            label={getPayrollDeductionStatusLabel(
                              deduction.status
                            )}
                          />
                        </TableCell>
                        <TableCell align="right">
                          {deduction.status === 'ACTIVE' ? (
                            <Button
                              size="small"
                              color="warning"
                              onClick={() => {
                                setCancelError('')
                                setCancelTarget(deduction)
                              }}
                            >
                              Cancelar descuento
                            </Button>
                          ) : (
                            '—'
                          )}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            )}

            <Typography variant="subtitle1">Registrar descuento</Typography>
            {createError ? <Alert severity="error">{createError}</Alert> : null}
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                select
                label="Tipo"
                value={form.type}
                onChange={(event) => updateField('type', event.target.value)}
                fullWidth
              >
                {PAYROLL_DEDUCTION_TYPE_OPTIONS.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="Monto"
                value={form.amount}
                onChange={(event) => updateField('amount', event.target.value)}
                fullWidth
                inputProps={{ inputMode: 'decimal' }}
              />
              <TextField
                label="Fecha"
                type="date"
                value={form.deductionDate}
                onChange={(event) =>
                  updateField('deductionDate', event.target.value)
                }
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
            </Stack>
            <TextField
              label="Descripción"
              value={form.description}
              onChange={(event) => updateField('description', event.target.value)}
              fullWidth
              multiline
              minRows={2}
            />
            <Button
              variant="contained"
              onClick={handleCreate}
              disabled={creating || loading}
              sx={{ alignSelf: 'flex-start' }}
            >
              {creating ? 'Guardando...' : 'Registrar descuento'}
            </Button>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button type="button" onClick={handleClose} disabled={creating}>
            Volver
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmFinanceActionDialog
        open={Boolean(cancelTarget)}
        title="Cancelar descuento"
        description={
          cancelTarget
            ? `¿Cancelar el descuento de ${formatFinanceMoney(cancelTarget.amount)} (${getPayrollDeductionTypeLabel(cancelTarget.type)})? El registro se conserva en el historial y deja de contar en el total activo.`
            : ''
        }
        confirmLabel="Cancelar descuento"
        submittingLabel="Cancelando..."
        confirmColor="warning"
        onClose={() => {
          if (!cancelling) {
            setCancelTarget(null)
            setCancelError('')
          }
        }}
        onConfirm={handleConfirmCancel}
        submitting={cancelling}
        errorMessage={cancelError}
      />
    </>
  )
}

export default PayrollEmployeeDeductionsDialog
