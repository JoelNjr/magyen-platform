import { useCallback, useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import AccountBalanceWalletOutlinedIcon from '@mui/icons-material/AccountBalanceWalletOutlined'
import EventRepeatOutlinedIcon from '@mui/icons-material/EventRepeatOutlined'
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Paper,
  Skeleton,
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import ConfirmFinanceActionDialog from '../components/ConfirmFinanceActionDialog'
import CreateRecurringFinancialObligationDialog from '../components/CreateRecurringFinancialObligationDialog'
import GenerateOccurrencesDialog from '../components/GenerateOccurrencesDialog'
import PayrollFinanceSection from '../components/PayrollFinanceSection'
import RegisterFinancialTransactionDialog from '../components/RegisterFinancialTransactionDialog'
import UpdateRecurringFinancialObligationDialog from '../components/UpdateRecurringFinancialObligationDialog'
import {
  formatFinanceDate,
  formatFinanceMoney,
  getCalendarMonthRange,
  getOccurrenceStatusChipColor,
  getOccurrenceStatusLabel,
  getFrequencyLabel,
  getObligationTypeLabel,
  getPreviousCalendarMonthRange,
  getSourceTypeLabel,
  getTransactionTypeChipColor,
  getTransactionTypeLabel,
  resolveApiErrorMessage,
} from '../presentation/financePresentation'
import {
  cancelObligationOccurrence,
  createRecurringFinancialObligation,
  deactivateRecurringFinancialObligation,
  generateObligationOccurrences,
  getFinancialPeriodSummary,
  getFinancialTransactions,
  getOverdueObligationOccurrences,
  getPendingObligationOccurrences,
  getRecurringFinancialObligations,
  getUpcomingObligationOccurrences,
  payObligationOccurrence,
  registerFinancialTransaction,
  updateRecurringFinancialObligation,
} from '../services/financeService'
import PageHeader from '../../../layout/PageHeader'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 3

function SectionHeader({ title, actions }) {
  return (
    <Stack
      direction={{ xs: 'column', sm: 'row' }}
      spacing={2}
      justifyContent="space-between"
      alignItems={{ xs: 'stretch', sm: 'center' }}
    >
      <Typography variant="h5">{title}</Typography>
      {actions}
    </Stack>
  )
}

function EmptyState({ icon, message }) {
  return (
    <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
      <Stack spacing={1} alignItems="center">
        {icon}
        <Typography color="text.secondary">{message}</Typography>
      </Stack>
    </Paper>
  )
}

function LoadingRows({ columns }) {
  return Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
    <TableRow key={`skeleton-${index}`}>
      {Array.from({ length: columns }).map((__, cellIndex) => (
        <TableCell key={`skeleton-cell-${cellIndex}`}>
          <Skeleton variant="text" />
        </TableCell>
      ))}
    </TableRow>
  ))
}

function FinancePage() {
  const initialMonth = getCalendarMonthRange()

  const [fromDate, setFromDate] = useState(initialMonth.fromDate)
  const [toDate, setToDate] = useState(initialMonth.toDate)
  const [periodError, setPeriodError] = useState('')

  const [summary, setSummary] = useState(null)
  const [summaryLoading, setSummaryLoading] = useState(true)
  const [summaryFailed, setSummaryFailed] = useState(false)

  const [pending, setPending] = useState([])
  const [pendingTotal, setPendingTotal] = useState(0)
  const [pendingLoading, setPendingLoading] = useState(true)
  const [pendingFailed, setPendingFailed] = useState(false)

  const [overdue, setOverdue] = useState([])
  const [overdueTotal, setOverdueTotal] = useState(0)
  const [overdueLoading, setOverdueLoading] = useState(true)
  const [overdueFailed, setOverdueFailed] = useState(false)

  const [upcoming, setUpcoming] = useState([])
  const [upcomingLoading, setUpcomingLoading] = useState(true)
  const [upcomingFailed, setUpcomingFailed] = useState(false)

  const [obligations, setObligations] = useState([])
  const [obligationsLoading, setObligationsLoading] = useState(true)
  const [obligationsFailed, setObligationsFailed] = useState(false)

  const [transactions, setTransactions] = useState([])
  const [transactionsLoading, setTransactionsLoading] = useState(true)
  const [transactionsFailed, setTransactionsFailed] = useState(false)

  const [payingOccurrenceId, setPayingOccurrenceId] = useState(null)
  const [cancelTarget, setCancelTarget] = useState(null)
  const [cancelling, setCancelling] = useState(false)
  const [cancelError, setCancelError] = useState('')

  const [generateOpen, setGenerateOpen] = useState(false)
  const [generating, setGenerating] = useState(false)
  const [generateError, setGenerateError] = useState('')
  const [generateResult, setGenerateResult] = useState(null)

  const [createObligationOpen, setCreateObligationOpen] = useState(false)
  const [creatingObligation, setCreatingObligation] = useState(false)
  const [createObligationError, setCreateObligationError] = useState('')

  const [editObligation, setEditObligation] = useState(null)
  const [updatingObligation, setUpdatingObligation] = useState(false)
  const [updateObligationError, setUpdateObligationError] = useState('')

  const [deactivateTarget, setDeactivateTarget] = useState(null)
  const [deactivating, setDeactivating] = useState(false)
  const [deactivateError, setDeactivateError] = useState('')

  const [registerOpen, setRegisterOpen] = useState(false)
  const [registering, setRegistering] = useState(false)
  const [registerError, setRegisterError] = useState('')

  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')
  const [errorOpen, setErrorOpen] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const showSuccess = useCallback((message) => {
    setSuccessMessage(message)
    setSuccessOpen(true)
  }, [])

  const showError = useCallback((message) => {
    setErrorMessage(message)
    setErrorOpen(true)
  }, [])

  const loadSummary = useCallback(async (rangeFrom, rangeTo) => {
    setSummaryLoading(true)
    setSummaryFailed(false)
    try {
      const data = await getFinancialPeriodSummary({
        fromDate: rangeFrom,
        toDate: rangeTo,
      })
      setSummary(data)
    } catch {
      setSummary(null)
      setSummaryFailed(true)
    } finally {
      setSummaryLoading(false)
    }
  }, [])

  const loadPending = useCallback(async () => {
    setPendingLoading(true)
    setPendingFailed(false)
    try {
      const data = await getPendingObligationOccurrences()
      setPending(Array.isArray(data?.occurrences) ? data.occurrences : [])
      setPendingTotal(data?.totalPendingAmount ?? 0)
    } catch {
      setPending([])
      setPendingTotal(0)
      setPendingFailed(true)
    } finally {
      setPendingLoading(false)
    }
  }, [])

  const loadOverdue = useCallback(async () => {
    setOverdueLoading(true)
    setOverdueFailed(false)
    try {
      const data = await getOverdueObligationOccurrences()
      setOverdue(Array.isArray(data?.occurrences) ? data.occurrences : [])
      setOverdueTotal(data?.totalOverdueAmount ?? 0)
    } catch {
      setOverdue([])
      setOverdueTotal(0)
      setOverdueFailed(true)
    } finally {
      setOverdueLoading(false)
    }
  }, [])

  const loadUpcoming = useCallback(async () => {
    setUpcomingLoading(true)
    setUpcomingFailed(false)
    try {
      const data = await getUpcomingObligationOccurrences(7)
      setUpcoming(Array.isArray(data?.occurrences) ? data.occurrences : [])
    } catch {
      setUpcoming([])
      setUpcomingFailed(true)
    } finally {
      setUpcomingLoading(false)
    }
  }, [])

  const loadObligations = useCallback(async () => {
    setObligationsLoading(true)
    setObligationsFailed(false)
    try {
      const data = await getRecurringFinancialObligations()
      setObligations(Array.isArray(data?.obligations) ? data.obligations : [])
    } catch {
      setObligations([])
      setObligationsFailed(true)
    } finally {
      setObligationsLoading(false)
    }
  }, [])

  const loadTransactions = useCallback(async () => {
    setTransactionsLoading(true)
    setTransactionsFailed(false)
    try {
      const data = await getFinancialTransactions()
      setTransactions(Array.isArray(data?.transactions) ? data.transactions : [])
    } catch {
      setTransactions([])
      setTransactionsFailed(true)
    } finally {
      setTransactionsLoading(false)
    }
  }, [])

  const reloadCommitmentViews = useCallback(async () => {
    await Promise.all([loadPending(), loadOverdue(), loadUpcoming()])
  }, [loadOverdue, loadPending, loadUpcoming])

  useEffect(() => {
    loadSummary(fromDate, toDate)
    loadPending()
    loadOverdue()
    loadUpcoming()
    loadObligations()
    loadTransactions()
    // Carga inicial únicamente; el resumen se recarga al aplicar período.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function applyPeriod(nextFrom, nextTo) {
    if (!nextFrom || !nextTo) {
      setPeriodError('Desde y Hasta son obligatorios.')
      return
    }
    if (nextFrom > nextTo) {
      setPeriodError('Desde no puede ser posterior a Hasta.')
      return
    }
    setPeriodError('')
    setFromDate(nextFrom)
    setToDate(nextTo)
    loadSummary(nextFrom, nextTo)
  }

  function handlePeriodPreset(kind) {
    if (kind === 'current') {
      const range = getCalendarMonthRange()
      applyPeriod(range.fromDate, range.toDate)
      return
    }
    if (kind === 'previous') {
      const range = getPreviousCalendarMonthRange()
      applyPeriod(range.fromDate, range.toDate)
    }
  }

  async function handlePayOccurrence(occurrence) {
    if (payingOccurrenceId) {
      return
    }

    setPayingOccurrenceId(occurrence.occurrenceId)
    try {
      await payObligationOccurrence(occurrence.occurrenceId)
      await Promise.all([
        reloadCommitmentViews(),
        loadSummary(fromDate, toDate),
        loadTransactions(),
      ])
      showSuccess(`Pago registrado: ${occurrence.obligationName}.`)
    } catch (error) {
      showError(
        resolveApiErrorMessage(error, 'No fue posible marcar el pago.')
      )
    } finally {
      setPayingOccurrenceId(null)
    }
  }

  async function handleConfirmCancel() {
    if (!cancelTarget || cancelling) {
      return
    }

    setCancelling(true)
    setCancelError('')
    try {
      await cancelObligationOccurrence(cancelTarget.occurrenceId)
      setCancelTarget(null)
      await Promise.all([
        reloadCommitmentViews(),
        loadSummary(fromDate, toDate),
      ])
      showSuccess('Ocurrencia cancelada.')
    } catch (error) {
      setCancelError(
        resolveApiErrorMessage(error, 'No fue posible cancelar la ocurrencia.')
      )
    } finally {
      setCancelling(false)
    }
  }

  async function handleGenerate(payload) {
    if (generating) {
      return
    }

    setGenerating(true)
    setGenerateError('')
    try {
      const result = await generateObligationOccurrences(payload)
      setGenerateResult(result)
      await reloadCommitmentViews()
      showSuccess(
        `Generación completada: ${result.occurrencesCreated} creadas.`
      )
    } catch (error) {
      setGenerateError(
        resolveApiErrorMessage(error, 'No fue posible generar los pagos.')
      )
    } finally {
      setGenerating(false)
    }
  }

  async function handleCreateObligation(payload) {
    if (creatingObligation) {
      return
    }

    setCreatingObligation(true)
    setCreateObligationError('')
    try {
      await createRecurringFinancialObligation(payload)
      setCreateObligationOpen(false)
      await loadObligations()
      showSuccess('Obligación creada. Use Generar pagos para crear ocurrencias.')
    } catch (error) {
      setCreateObligationError(
        resolveApiErrorMessage(error, 'No fue posible crear la obligación.')
      )
    } finally {
      setCreatingObligation(false)
    }
  }

  async function handleUpdateObligation(payload) {
    if (!editObligation || updatingObligation) {
      return
    }

    setUpdatingObligation(true)
    setUpdateObligationError('')
    try {
      await updateRecurringFinancialObligation(editObligation.obligationId, payload)
      setEditObligation(null)
      await loadObligations()
      showSuccess('Obligación actualizada.')
    } catch (error) {
      setUpdateObligationError(
        resolveApiErrorMessage(error, 'No fue posible actualizar la obligación.')
      )
    } finally {
      setUpdatingObligation(false)
    }
  }

  async function handleConfirmDeactivate() {
    if (!deactivateTarget || deactivating) {
      return
    }

    setDeactivating(true)
    setDeactivateError('')
    try {
      await deactivateRecurringFinancialObligation(deactivateTarget.obligationId)
      setDeactivateTarget(null)
      await Promise.all([loadObligations(), reloadCommitmentViews()])
      showSuccess('Obligación desactivada.')
    } catch (error) {
      setDeactivateError(
        resolveApiErrorMessage(error, 'No fue posible desactivar la obligación.')
      )
    } finally {
      setDeactivating(false)
    }
  }

  async function handleRegisterTransaction(payload) {
    if (registering) {
      return
    }

    setRegistering(true)
    setRegisterError('')
    try {
      await registerFinancialTransaction(payload)
      setRegisterOpen(false)
      await Promise.all([loadTransactions(), loadSummary(fromDate, toDate)])
      showSuccess('Movimiento registrado.')
    } catch (error) {
      setRegisterError(
        resolveApiErrorMessage(error, 'No fue posible registrar el movimiento.')
      )
    } finally {
      setRegistering(false)
    }
  }

  function renderOccurrenceActions(occurrence) {
    const isPaying = payingOccurrenceId === occurrence.occurrenceId
    return (
      <Stack direction="row" spacing={1} justifyContent="flex-end" flexWrap="wrap">
        <Button
          size="small"
          variant="contained"
          disabled={Boolean(payingOccurrenceId)}
          onClick={() => handlePayOccurrence(occurrence)}
        >
          {isPaying ? 'Pagando...' : 'Marcar como pagado'}
        </Button>
        <Button
          size="small"
          color="inherit"
          disabled={Boolean(payingOccurrenceId)}
          onClick={() => {
            setCancelError('')
            setCancelTarget(occurrence)
          }}
        >
          Cancelar
        </Button>
      </Stack>
    )
  }

  return (
    <>
      <Stack spacing={4}>
        <PageHeader
          title="Finanzas"
          actions={
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
            <Button
              variant="outlined"
              startIcon={<EventRepeatOutlinedIcon />}
              onClick={() => {
                setGenerateError('')
                setGenerateResult(null)
                setGenerateOpen(true)
              }}
            >
              Generar pagos
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => {
                setRegisterError('')
                setRegisterOpen(true)
              }}
            >
              Registrar movimiento
            </Button>
          </Stack>
          }
        />

        <Paper variant="outlined" sx={{ p: 2 }}>
          <Stack spacing={2}>
            <Typography variant="h6">Período</Typography>
            {periodError ? <Alert severity="error">{periodError}</Alert> : null}
            <Stack
              direction={{ xs: 'column', md: 'row' }}
              spacing={2}
              alignItems={{ md: 'center' }}
            >
              <TextField
                label="Desde"
                type="date"
                value={fromDate}
                onChange={(event) => setFromDate(event.target.value)}
                InputLabelProps={{ shrink: true }}
                size="small"
                sx={{ width: { xs: '100%', md: 'auto' }, minWidth: { md: 160 } }}
              />
              <TextField
                label="Hasta"
                type="date"
                value={toDate}
                onChange={(event) => setToDate(event.target.value)}
                InputLabelProps={{ shrink: true }}
                size="small"
                sx={{ width: { xs: '100%', md: 'auto' }, minWidth: { md: 160 } }}
              />
              <Button
                variant="contained"
                onClick={() => applyPeriod(fromDate, toDate)}
              >
                Aplicar
              </Button>
              <Button onClick={() => handlePeriodPreset('current')}>
                Mes actual
              </Button>
              <Button onClick={() => handlePeriodPreset('previous')}>
                Mes anterior
              </Button>
            </Stack>
          </Stack>
        </Paper>

        <Stack spacing={2}>
          <Typography variant="h5">Resumen del período</Typography>
          {summaryFailed ? (
            <Alert severity="error">
              No fue posible cargar el resumen del período.
            </Alert>
          ) : null}
          <Box
            sx={{
              display: 'grid',
              gap: 2,
              gridTemplateColumns: {
                xs: '1fr',
                sm: '1fr 1fr',
                md: 'repeat(4, 1fr)',
              },
            }}
          >
            {[
              {
                label: 'Ingresos',
                value: summaryLoading
                  ? null
                  : formatFinanceMoney(summary?.totalIncome),
                color: 'success.main',
              },
              {
                label: 'Gastos',
                value: summaryLoading
                  ? null
                  : formatFinanceMoney(summary?.totalExpense),
                color: 'error.main',
              },
              {
                label: 'Resultado neto',
                value: summaryLoading
                  ? null
                  : formatFinanceMoney(summary?.netResult),
                color: 'text.primary',
              },
              {
                label: 'Movimientos',
                value: summaryLoading ? null : String(summary?.transactionCount ?? 0),
                color: 'text.primary',
              },
            ].map((card) => (
              <Card key={card.label} variant="outlined" sx={{ height: '100%', borderLeft: 3, borderLeftColor: card.color }}>
                <CardContent>
                  <Typography color="text.secondary">{card.label}</Typography>
                  {summaryLoading ? (
                    <Skeleton width="60%" height={40} />
                  ) : (
                    <Typography variant="h5" sx={{ color: card.color, mt: 1 }}>
                      {card.value}
                    </Typography>
                  )}
                </CardContent>
              </Card>
            ))}
          </Box>
        </Stack>

        <Stack spacing={2}>
          <SectionHeader
            title="Pagos vencidos"
            actions={
              !overdueLoading && !overdueFailed ? (
                <Chip
                  color="error"
                  label={`Total: ${formatFinanceMoney(overdueTotal)}`}
                />
              ) : null
            }
          />
          {overdueFailed ? (
            <Alert severity="error">No fue posible cargar los pagos vencidos.</Alert>
          ) : null}
          {!overdueFailed && (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={headerCellSx}>Obligación</TableCell>
                    <TableCell sx={headerCellSx}>Vence</TableCell>
                    <TableCell sx={headerCellSx}>Monto</TableCell>
                    <TableCell sx={headerCellSx}>Días vencido</TableCell>
                    <TableCell align="right" sx={headerCellSx}>
                      Acciones
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {overdueLoading ? (
                    <LoadingRows columns={5} />
                  ) : overdue.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5}>
                        <EmptyState
                          icon={
                            <WarningAmberOutlinedIcon color="disabled" fontSize="large" />
                          }
                          message="No hay pagos vencidos."
                        />
                      </TableCell>
                    </TableRow>
                  ) : (
                    overdue.map((occurrence) => (
                      <TableRow
                        key={occurrence.occurrenceId}
                        sx={{ bgcolor: 'action.hover' }}
                      >
                        <TableCell>
                          <Stack spacing={0.5}>
                            <Typography fontWeight={600}>
                              {occurrence.obligationName}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              {getObligationTypeLabel(occurrence.obligationType)}
                            </Typography>
                          </Stack>
                        </TableCell>
                        <TableCell>{formatFinanceDate(occurrence.dueDate)}</TableCell>
                        <TableCell>
                          {formatFinanceMoney(occurrence.expectedAmount)}
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            color="error"
                            label={`${occurrence.daysOverdue ?? 0} día(s)`}
                          />
                        </TableCell>
                        <TableCell align="right">
                          {renderOccurrenceActions(occurrence)}
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
          <SectionHeader title="Próximos pagos" />
          {upcomingFailed ? (
            <Alert severity="error">No fue posible cargar los próximos pagos.</Alert>
          ) : null}
          {!upcomingFailed && (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={headerCellSx}>Obligación</TableCell>
                    <TableCell sx={headerCellSx}>Vence</TableCell>
                    <TableCell sx={headerCellSx}>Monto</TableCell>
                    <TableCell sx={headerCellSx}>Días</TableCell>
                    <TableCell align="right" sx={headerCellSx}>
                      Acciones
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {upcomingLoading ? (
                    <LoadingRows columns={5} />
                  ) : upcoming.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5}>
                        <EmptyState
                          icon={
                            <AccountBalanceWalletOutlinedIcon
                              color="disabled"
                              fontSize="large"
                            />
                          }
                          message="No hay pagos próximos en los siguientes 7 días."
                        />
                      </TableCell>
                    </TableRow>
                  ) : (
                    upcoming.map((occurrence) => (
                      <TableRow key={occurrence.occurrenceId}>
                        <TableCell>
                          <Stack spacing={0.5}>
                            <Typography fontWeight={600}>
                              {occurrence.obligationName}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              {getObligationTypeLabel(occurrence.obligationType)}
                            </Typography>
                          </Stack>
                        </TableCell>
                        <TableCell>{formatFinanceDate(occurrence.dueDate)}</TableCell>
                        <TableCell>
                          {formatFinanceMoney(occurrence.expectedAmount)}
                        </TableCell>
                        <TableCell>
                          {occurrence.daysUntilDue === 0
                            ? 'Hoy'
                            : `${occurrence.daysUntilDue} día(s)`}
                        </TableCell>
                        <TableCell align="right">
                          {renderOccurrenceActions(occurrence)}
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
          <SectionHeader
            title="Pagos pendientes"
            actions={
              !pendingLoading && !pendingFailed ? (
                <Chip
                  color="warning"
                  label={`Total pendiente: ${formatFinanceMoney(pendingTotal)}`}
                />
              ) : null
            }
          />
          {pendingFailed ? (
            <Alert severity="error">
              No fue posible cargar los pagos pendientes.
            </Alert>
          ) : null}
          {!pendingFailed && (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={headerCellSx}>Obligación</TableCell>
                    <TableCell sx={headerCellSx}>Tipo</TableCell>
                    <TableCell sx={headerCellSx}>Vence</TableCell>
                    <TableCell sx={headerCellSx}>Monto</TableCell>
                    <TableCell sx={headerCellSx}>Estado</TableCell>
                    <TableCell align="right" sx={headerCellSx}>
                      Acciones
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {pendingLoading ? (
                    <LoadingRows columns={6} />
                  ) : pending.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6}>
                        <EmptyState
                          icon={
                            <AccountBalanceWalletOutlinedIcon
                              color="disabled"
                              fontSize="large"
                            />
                          }
                          message="No hay pagos pendientes."
                        />
                      </TableCell>
                    </TableRow>
                  ) : (
                    pending.map((occurrence) => (
                      <TableRow key={occurrence.occurrenceId}>
                        <TableCell>
                          <Stack direction="row" spacing={1} alignItems="center">
                            <Typography fontWeight={600}>
                              {occurrence.obligationName}
                            </Typography>
                            {occurrence.overdue ? (
                              <Chip size="small" color="error" label="Vencido" />
                            ) : null}
                          </Stack>
                        </TableCell>
                        <TableCell>
                          {getObligationTypeLabel(occurrence.obligationType)}
                        </TableCell>
                        <TableCell>{formatFinanceDate(occurrence.dueDate)}</TableCell>
                        <TableCell>
                          {formatFinanceMoney(occurrence.expectedAmount)}
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            color={getOccurrenceStatusChipColor(occurrence.status)}
                            label={getOccurrenceStatusLabel(occurrence.status)}
                          />
                        </TableCell>
                        <TableCell align="right">
                          {renderOccurrenceActions(occurrence)}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Stack>

        <PayrollFinanceSection
          fromDate={fromDate}
          toDate={toDate}
          onFinanceChanged={async (rangeFrom, rangeTo) => {
            await Promise.all([
              loadSummary(rangeFrom, rangeTo),
              loadTransactions(),
            ])
          }}
          showSuccess={showSuccess}
          showError={showError}
        />

        <Stack spacing={2}>
          <SectionHeader
            title="Obligaciones recurrentes"
            actions={
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => {
                  setCreateObligationError('')
                  setCreateObligationOpen(true)
                }}
              >
                Crear obligación
              </Button>
            }
          />
          {obligationsFailed ? (
            <Alert severity="error">
              No fue posible cargar las obligaciones recurrentes.
            </Alert>
          ) : null}
          {!obligationsFailed && (
            <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={headerCellSx}>Nombre</TableCell>
                    <TableCell sx={headerCellSx}>Tipo</TableCell>
                    <TableCell sx={headerCellSx}>Monto</TableCell>
                    <TableCell sx={headerCellSx}>Frecuencia</TableCell>
                    <TableCell sx={headerCellSx}>Día</TableCell>
                    <TableCell sx={headerCellSx}>Inicio</TableCell>
                    <TableCell sx={headerCellSx}>Estado</TableCell>
                    <TableCell align="right" sx={headerCellSx}>
                      Acciones
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {obligationsLoading ? (
                    <LoadingRows columns={8} />
                  ) : obligations.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={8}>
                        <EmptyState
                          icon={
                            <EventRepeatOutlinedIcon color="disabled" fontSize="large" />
                          }
                          message="No hay obligaciones recurrentes registradas."
                        />
                      </TableCell>
                    </TableRow>
                  ) : (
                    obligations.map((obligation) => (
                      <TableRow key={obligation.obligationId}>
                        <TableCell>{obligation.name}</TableCell>
                        <TableCell>
                          {getObligationTypeLabel(obligation.type)}
                        </TableCell>
                        <TableCell>
                          {formatFinanceMoney(obligation.expectedAmount)}
                        </TableCell>
                        <TableCell>
                          {getFrequencyLabel(obligation.frequency)}
                        </TableCell>
                        <TableCell>{obligation.dueDay ?? '—'}</TableCell>
                        <TableCell>
                          {formatFinanceDate(obligation.startDate)}
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            color={obligation.active ? 'success' : 'default'}
                            label={obligation.active ? 'Activa' : 'Inactiva'}
                          />
                        </TableCell>
                        <TableCell align="right">
                          <Stack
                            direction="row"
                            spacing={1}
                            justifyContent="flex-end"
                            flexWrap="wrap"
                          >
                            <Button
                              size="small"
                              onClick={() => {
                                setUpdateObligationError('')
                                setEditObligation(obligation)
                              }}
                            >
                              Editar
                            </Button>
                            {obligation.active ? (
                              <Button
                                size="small"
                                color="warning"
                                onClick={() => {
                                  setDeactivateError('')
                                  setDeactivateTarget(obligation)
                                }}
                              >
                                Desactivar
                              </Button>
                            ) : null}
                          </Stack>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}
          <Alert severity="info">
            La reactivación de obligaciones no está expuesta aún por la API
            (solo desactivar). Las ocurrencias históricas no se eliminan.
          </Alert>
        </Stack>

        <Stack spacing={2}>
          <SectionHeader title="Movimientos financieros" />
          {transactionsFailed ? (
            <Alert severity="error">
              No fue posible cargar los movimientos financieros.
            </Alert>
          ) : null}
          {!transactionsFailed && (
            <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={headerCellSx}>Fecha</TableCell>
                    <TableCell sx={headerCellSx}>Tipo</TableCell>
                    <TableCell sx={headerCellSx}>Categoría</TableCell>
                    <TableCell sx={headerCellSx}>Descripción</TableCell>
                    <TableCell sx={headerCellSx}>Monto</TableCell>
                    <TableCell sx={headerCellSx}>Origen</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {transactionsLoading ? (
                    <LoadingRows columns={6} />
                  ) : transactions.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6}>
                        <EmptyState
                          icon={
                            <AccountBalanceWalletOutlinedIcon
                              color="disabled"
                              fontSize="large"
                            />
                          }
                          message="No hay movimientos registrados."
                        />
                      </TableCell>
                    </TableRow>
                  ) : (
                    transactions.map((transaction) => (
                      <TableRow key={transaction.transactionId}>
                        <TableCell>
                          {formatFinanceDate(transaction.transactionDate)}
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            color={getTransactionTypeChipColor(transaction.type)}
                            label={getTransactionTypeLabel(transaction.type)}
                          />
                        </TableCell>
                        <TableCell>{transaction.category}</TableCell>
                        <TableCell>{transaction.description || '—'}</TableCell>
                        <TableCell
                          sx={{
                            color:
                              transaction.type === 'INCOME'
                                ? 'success.main'
                                : 'error.main',
                            fontWeight: 600,
                          }}
                        >
                          {formatFinanceMoney(transaction.amount)}
                        </TableCell>
                        <TableCell>
                          {getSourceTypeLabel(transaction.sourceType)}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Stack>
      </Stack>

      <GenerateOccurrencesDialog
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

      <CreateRecurringFinancialObligationDialog
        open={createObligationOpen}
        onClose={() => {
          if (!creatingObligation) {
            setCreateObligationOpen(false)
          }
        }}
        onSubmit={handleCreateObligation}
        submitting={creatingObligation}
        errorMessage={createObligationError}
      />

      <UpdateRecurringFinancialObligationDialog
        open={Boolean(editObligation)}
        obligation={editObligation}
        onClose={() => {
          if (!updatingObligation) {
            setEditObligation(null)
          }
        }}
        onSubmit={handleUpdateObligation}
        submitting={updatingObligation}
        errorMessage={updateObligationError}
      />

      <RegisterFinancialTransactionDialog
        open={registerOpen}
        onClose={() => {
          if (!registering) {
            setRegisterOpen(false)
          }
        }}
        onSubmit={handleRegisterTransaction}
        submitting={registering}
        errorMessage={registerError}
      />

      <ConfirmFinanceActionDialog
        open={Boolean(cancelTarget)}
        title="Cancelar ocurrencia"
        description={
          cancelTarget
            ? `¿Cancelar el pago pendiente de "${cancelTarget.obligationName}" con vencimiento ${formatFinanceDate(cancelTarget.dueDate)}?`
            : ''
        }
        confirmLabel="Cancelar ocurrencia"
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

      <ConfirmFinanceActionDialog
        open={Boolean(deactivateTarget)}
        title="Desactivar obligación"
        description={
          deactivateTarget
            ? `¿Desactivar la obligación "${deactivateTarget.name}"? No se eliminarán ocurrencias históricas.`
            : ''
        }
        confirmLabel="Desactivar"
        submittingLabel="Desactivando..."
        confirmColor="warning"
        onClose={() => {
          if (!deactivating) {
            setDeactivateTarget(null)
            setDeactivateError('')
          }
        }}
        onConfirm={handleConfirmDeactivate}
        submitting={deactivating}
        errorMessage={deactivateError}
      />

      <Snackbar
        open={successOpen}
        autoHideDuration={4000}
        onClose={() => setSuccessOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          onClose={() => setSuccessOpen(false)}
          severity="success"
          variant="filled"
          sx={{ width: '100%' }}
        >
          {successMessage}
        </Alert>
      </Snackbar>

      <Snackbar
        open={errorOpen}
        autoHideDuration={5000}
        onClose={() => setErrorOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          onClose={() => setErrorOpen(false)}
          severity="error"
          variant="filled"
          sx={{ width: '100%' }}
        >
          {errorMessage}
        </Alert>
      </Snackbar>
    </>
  )
}

export default FinancePage
