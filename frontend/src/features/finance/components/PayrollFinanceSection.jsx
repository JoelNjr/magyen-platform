import { useCallback, useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined'
import {
  Alert,
  Button,
  Chip,
  Paper,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import ConfirmFinanceActionDialog from './ConfirmFinanceActionDialog'
import CreatePayrollEmployeeDialog from './CreatePayrollEmployeeDialog'
import GeneratePayrollPeriodsDialog from './GeneratePayrollPeriodsDialog'
import PayrollEmployeeDeductionsDialog from './PayrollEmployeeDeductionsDialog'
import PayrollEmployeeProductionEarningsDialog from './PayrollEmployeeProductionEarningsDialog'
import UpdatePayrollEmployeeCompensationDialog from './UpdatePayrollEmployeeCompensationDialog'
import {
  formatFinanceDate,
  formatFinanceMoney,
  formatPayrollPeriodRange,
  getOccurrenceStatusChipColor,
  getOccurrenceStatusLabel,
  getPayrollCompensationTypeLabel,
  resolveApiErrorMessage,
} from '../presentation/financePresentation'
import {
  activatePayrollEmployee,
  cancelPayrollPeriod,
  createPayrollEmployee,
  deactivatePayrollEmployee,
  generatePayrollPeriods,
  getPayrollEmployees,
  getPayrollPeriods,
  payPayrollPeriod,
  updatePayrollEmployeeCompensation,
} from '../services/financeService'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 3

function LoadingRows({ columns }) {
  return Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
    <TableRow key={`payroll-skeleton-${index}`}>
      {Array.from({ length: columns }).map((__, cellIndex) => (
        <TableCell key={`payroll-skeleton-cell-${cellIndex}`}>
          <Skeleton variant="text" />
        </TableCell>
      ))}
    </TableRow>
  ))
}

function EmptyRow({ columns, message }) {
  return (
    <TableRow>
      <TableCell colSpan={columns}>
        <Paper variant="outlined" sx={{ p: 3, textAlign: 'center' }}>
          <Stack spacing={1} alignItems="center">
            <BadgeOutlinedIcon color="disabled" fontSize="large" />
            <Typography color="text.secondary">{message}</Typography>
          </Stack>
        </Paper>
      </TableCell>
    </TableRow>
  )
}

function PayrollFinanceSection({
  fromDate,
  toDate,
  onFinanceChanged,
  showSuccess,
  showError,
}) {
  const [employees, setEmployees] = useState([])
  const [employeesLoading, setEmployeesLoading] = useState(true)
  const [employeesFailed, setEmployeesFailed] = useState(false)

  const [periods, setPeriods] = useState([])
  const [periodsLoading, setPeriodsLoading] = useState(true)
  const [periodsFailed, setPeriodsFailed] = useState(false)

  const [createOpen, setCreateOpen] = useState(false)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')

  const [editEmployee, setEditEmployee] = useState(null)
  const [updating, setUpdating] = useState(false)
  const [updateError, setUpdateError] = useState('')

  const [generateOpen, setGenerateOpen] = useState(false)
  const [generating, setGenerating] = useState(false)
  const [generateError, setGenerateError] = useState('')
  const [generateResult, setGenerateResult] = useState(null)

  const [payTarget, setPayTarget] = useState(null)
  const [paying, setPaying] = useState(false)
  const [payError, setPayError] = useState('')

  const [cancelTarget, setCancelTarget] = useState(null)
  const [cancelling, setCancelling] = useState(false)
  const [cancelError, setCancelError] = useState('')

  const [togglingEmployeeId, setTogglingEmployeeId] = useState(null)
  const [earningsEmployee, setEarningsEmployee] = useState(null)
  const [deductionsEmployee, setDeductionsEmployee] = useState(null)

  const loadEmployees = useCallback(async () => {
    setEmployeesLoading(true)
    setEmployeesFailed(false)
    try {
      const data = await getPayrollEmployees()
      setEmployees(Array.isArray(data?.employees) ? data.employees : [])
    } catch {
      setEmployees([])
      setEmployeesFailed(true)
    } finally {
      setEmployeesLoading(false)
    }
  }, [])

  const loadPeriods = useCallback(async () => {
    setPeriodsLoading(true)
    setPeriodsFailed(false)
    try {
      const data = await getPayrollPeriods()
      setPeriods(Array.isArray(data?.periods) ? data.periods : [])
    } catch {
      setPeriods([])
      setPeriodsFailed(true)
    } finally {
      setPeriodsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadEmployees()
    loadPeriods()
  }, [loadEmployees, loadPeriods])

  async function handleCreateEmployee(payload) {
    if (creating) {
      return
    }

    setCreating(true)
    setCreateError('')
    try {
      await createPayrollEmployee(payload)
      setCreateOpen(false)
      await loadEmployees()
      showSuccess('Empleado de nómina creado.')
    } catch (error) {
      setCreateError(
        resolveApiErrorMessage(error, 'No fue posible crear el empleado.')
      )
    } finally {
      setCreating(false)
    }
  }

  async function handleUpdateCompensation(payload) {
    if (!editEmployee || updating) {
      return
    }

    setUpdating(true)
    setUpdateError('')
    try {
      await updatePayrollEmployeeCompensation(editEmployee.employeeId, payload)
      setEditEmployee(null)
      await loadEmployees()
      showSuccess('Compensación actualizada.')
    } catch (error) {
      setUpdateError(
        resolveApiErrorMessage(error, 'No fue posible actualizar la compensación.')
      )
    } finally {
      setUpdating(false)
    }
  }

  async function handleToggleActive(employee) {
    if (togglingEmployeeId) {
      return
    }

    setTogglingEmployeeId(employee.employeeId)
    try {
      if (employee.active) {
        await deactivatePayrollEmployee(employee.employeeId)
        showSuccess(`Empleado desactivado: ${employee.displayName}.`)
      } else {
        await activatePayrollEmployee(employee.employeeId)
        showSuccess(`Empleado activado: ${employee.displayName}.`)
      }
      await loadEmployees()
    } catch (error) {
      showError(
        resolveApiErrorMessage(
          error,
          'No fue posible cambiar el estado del empleado.'
        )
      )
    } finally {
      setTogglingEmployeeId(null)
    }
  }

  async function handleGenerate(payload) {
    if (generating) {
      return
    }

    setGenerating(true)
    setGenerateError('')
    try {
      const result = await generatePayrollPeriods(payload)
      setGenerateResult(result)
      await loadPeriods()
      showSuccess(
        `Nómina generada: ${result.created ?? 0} período(s) creado(s).`
      )
    } catch (error) {
      setGenerateError(
        resolveApiErrorMessage(error, 'No fue posible generar la nómina.')
      )
    } finally {
      setGenerating(false)
    }
  }

  async function handleConfirmPay() {
    if (!payTarget || paying) {
      return
    }

    setPaying(true)
    setPayError('')
    try {
      await payPayrollPeriod(payTarget.periodId)
      setPayTarget(null)
      await Promise.all([loadPeriods(), onFinanceChanged?.(fromDate, toDate)])
      showSuccess(`Nómina pagada: ${payTarget.employeeDisplayName}.`)
    } catch (error) {
      setPayError(
        resolveApiErrorMessage(error, 'No fue posible pagar la nómina.')
      )
    } finally {
      setPaying(false)
    }
  }

  async function handleConfirmCancel() {
    if (!cancelTarget || cancelling) {
      return
    }

    setCancelling(true)
    setCancelError('')
    try {
      await cancelPayrollPeriod(cancelTarget.periodId)
      setCancelTarget(null)
      await loadPeriods()
      showSuccess('Período de nómina cancelado.')
    } catch (error) {
      setCancelError(
        resolveApiErrorMessage(error, 'No fue posible cancelar el período.')
      )
    } finally {
      setCancelling(false)
    }
  }

  return (
    <>
      <Stack spacing={2}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          justifyContent="space-between"
          alignItems={{ xs: 'stretch', sm: 'center' }}
        >
          <Typography variant="h5">Empleados</Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              setCreateError('')
              setCreateOpen(true)
            }}
          >
            Crear empleado
          </Button>
        </Stack>
        {employeesFailed ? (
          <Alert severity="error">No fue posible cargar los empleados.</Alert>
        ) : null}
        {!employeesFailed && (
          <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={headerCellSx}>Nombre</TableCell>
                  <TableCell sx={headerCellSx}>Tipo de compensación</TableCell>
                  <TableCell sx={headerCellSx}>Valor fijo</TableCell>
                  <TableCell sx={headerCellSx}>Puede vender</TableCell>
                  <TableCell sx={headerCellSx}>Puede hacer producción</TableCell>
                  <TableCell sx={headerCellSx}>Estado</TableCell>
                  <TableCell align="right" sx={headerCellSx}>
                    Acciones
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {employeesLoading ? (
                  <LoadingRows columns={7} />
                ) : employees.length === 0 ? (
                  <EmptyRow
                    columns={7}
                    message="No hay empleados de nómina registrados."
                  />
                ) : (
                  employees.map((employee) => (
                    <TableRow key={employee.employeeId}>
                      <TableCell>
                        <Typography fontWeight={600}>
                          {employee.displayName}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Stack spacing={0.5}>
                          <Typography>
                            {getPayrollCompensationTypeLabel(
                              employee.compensationType
                            )}
                          </Typography>
                        </Stack>
                      </TableCell>
                      <TableCell>
                        {employee.compensationType === 'FIXED_PAYROLL'
                          ? formatFinanceMoney(employee.fixedAmount)
                          : '—'}
                      </TableCell>
                      <TableCell>
                        {employee.compensationType === 'FIXED_PAYROLL'
                          ? 'Sí'
                          : 'No'}
                      </TableCell>
                      <TableCell>
                        {employee.compensationType === 'PRODUCTION_BASED'
                          ? 'Sí'
                          : 'No'}
                      </TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          color={employee.active ? 'success' : 'default'}
                          label={employee.active ? 'Activo' : 'Inactivo'}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Stack
                          direction="row"
                          spacing={1}
                          justifyContent="flex-end"
                        >
                          <Button
                            size="small"
                            onClick={() => setDeductionsEmployee(employee)}
                          >
                            Deducciones
                          </Button>
                          <Button
                            size="small"
                            onClick={() => setEarningsEmployee(employee)}
                          >
                            Ver mano de obra
                          </Button>
                          <Button
                            size="small"
                            onClick={() => {
                              setUpdateError('')
                              setEditEmployee(employee)
                            }}
                          >
                            Editar
                          </Button>
                          <Button
                            size="small"
                            disabled={
                              togglingEmployeeId === employee.employeeId
                            }
                            onClick={() => handleToggleActive(employee)}
                          >
                            {employee.active ? 'Desactivar' : 'Activar'}
                          </Button>
                        </Stack>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Stack>

      <Stack spacing={2}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          justifyContent="space-between"
          alignItems={{ xs: 'stretch', sm: 'center' }}
        >
          <Typography variant="h5">Nómina</Typography>
          <Button
            variant="outlined"
            onClick={() => {
              setGenerateError('')
              setGenerateResult(null)
              setGenerateOpen(true)
            }}
          >
            Generar nómina
          </Button>
        </Stack>
        <Typography variant="body2" color="text.secondary">
          Generar un período no crea gasto de caja. Solo al pagar se registra el
          movimiento financiero.
        </Typography>
        {periodsFailed ? (
          <Alert severity="error">
            No fue posible cargar los períodos de nómina.
          </Alert>
        ) : null}
        {!periodsFailed && (
          <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={headerCellSx}>Empleado</TableCell>
                  <TableCell sx={headerCellSx}>Periodo</TableCell>
                  <TableCell sx={headerCellSx}>Fecha prevista</TableCell>
                  <TableCell sx={headerCellSx}>Monto</TableCell>
                  <TableCell sx={headerCellSx}>Estado</TableCell>
                  <TableCell align="right" sx={headerCellSx}>
                    Acción
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {periodsLoading ? (
                  <LoadingRows columns={6} />
                ) : periods.length === 0 ? (
                  <EmptyRow
                    columns={6}
                    message="No hay períodos de nómina. Genere nómina para empleados fijos."
                  />
                ) : (
                  periods.map((period) => (
                    <TableRow key={period.periodId}>
                      <TableCell>
                        <Typography fontWeight={600}>
                          {period.employeeDisplayName || '—'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        {formatPayrollPeriodRange(
                          period.periodStart,
                          period.periodEnd
                        )}
                      </TableCell>
                      <TableCell>
                        {formatFinanceDate(period.expectedPaymentDate)}
                      </TableCell>
                      <TableCell>
                        {formatFinanceMoney(period.amountSnapshot)}
                      </TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          color={getOccurrenceStatusChipColor(period.status)}
                          label={getOccurrenceStatusLabel(period.status)}
                        />
                      </TableCell>
                      <TableCell align="right">
                        {period.status === 'PENDING' ? (
                          <Stack
                            direction="row"
                            spacing={1}
                            justifyContent="flex-end"
                          >
                            <Button
                              size="small"
                              variant="contained"
                              onClick={() => {
                                setPayError('')
                                setPayTarget(period)
                              }}
                            >
                              Pagar
                            </Button>
                            <Button
                              size="small"
                              color="warning"
                              onClick={() => {
                                setCancelError('')
                                setCancelTarget(period)
                              }}
                            >
                              Cancelar
                            </Button>
                          </Stack>
                        ) : (
                          '—'
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Stack>

      <CreatePayrollEmployeeDialog
        open={createOpen}
        onClose={() => {
          if (!creating) {
            setCreateOpen(false)
          }
        }}
        onSubmit={handleCreateEmployee}
        submitting={creating}
        errorMessage={createError}
      />

      <PayrollEmployeeProductionEarningsDialog
        open={Boolean(earningsEmployee)}
        employee={earningsEmployee}
        onClose={() => setEarningsEmployee(null)}
      />

      <PayrollEmployeeDeductionsDialog
        open={Boolean(deductionsEmployee)}
        employee={deductionsEmployee}
        onClose={() => setDeductionsEmployee(null)}
        showSuccess={showSuccess}
      />

      <UpdatePayrollEmployeeCompensationDialog
        open={Boolean(editEmployee)}
        employee={editEmployee}
        onClose={() => {
          if (!updating) {
            setEditEmployee(null)
          }
        }}
        onSubmit={handleUpdateCompensation}
        submitting={updating}
        errorMessage={updateError}
      />

      <GeneratePayrollPeriodsDialog
        open={generateOpen}
        onClose={() => {
          if (!generating) {
            setGenerateOpen(false)
          }
        }}
        onSubmit={handleGenerate}
        submitting={generating}
        errorMessage={generateError}
        result={generateResult}
      />

      <ConfirmFinanceActionDialog
        open={Boolean(payTarget)}
        title="Pagar nómina"
        description={
          payTarget
            ? `Empleado: ${payTarget.employeeDisplayName}. Periodo: ${formatPayrollPeriodRange(payTarget.periodStart, payTarget.periodEnd)}. Monto: ${formatFinanceMoney(payTarget.amountSnapshot)}.`
            : ''
        }
        confirmLabel="Pagar nómina"
        submittingLabel="Pagando..."
        onClose={() => {
          if (!paying) {
            setPayTarget(null)
            setPayError('')
          }
        }}
        onConfirm={handleConfirmPay}
        submitting={paying}
        errorMessage={payError}
      />

      <ConfirmFinanceActionDialog
        open={Boolean(cancelTarget)}
        title="Cancelar período de nómina"
        description={
          cancelTarget
            ? `¿Cancelar el período pendiente de "${cancelTarget.employeeDisplayName}" (${formatPayrollPeriodRange(cancelTarget.periodStart, cancelTarget.periodEnd)})?`
            : ''
        }
        confirmLabel="Cancelar período"
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

export default PayrollFinanceSection
