import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Button,
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
import { useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { isAdmin } from '../../auth/presentation/authPresentation'
import PageHeader from '../../../layout/PageHeader'
import RegisterPlotterPaymentDialog from '../components/RegisterPlotterPaymentDialog'
import {
  formatPlotterDate,
  formatPlotterMoney,
  formatPlotterNumber,
} from '../presentation/plotterJobPresentation'
import { formatPlotterMachineLabel } from '../presentation/plotterMachinePresentation'
import {
  canSubmitPlotterPayment,
  createPlotterPaymentRegistrationKey,
  registrationKeyAfterPaymentFailure,
} from '../presentation/plotterPaymentSubmission'
import {
  getPlotterCustomerAccount,
  registerPlotterCustomerPayment,
} from '../services/plotterService'

const headerCellSx = { fontWeight: 'bold' }

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function PlotterCustomerAccountDetailPage() {
  const { customerId } = useParams()
  const navigate = useNavigate()
  const { identity } = useAuth()
  const admin = isAdmin(identity)
  const [account, setAccount] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [paymentOpen, setPaymentOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [paymentError, setPaymentError] = useState('')
  const [successOpen, setSuccessOpen] = useState(false)
  const registrationKey = useRef(null)

  const loadAccount = useCallback(async () => {
    setLoading(true)
    setFailed(false)
    try {
      setAccount(await getPlotterCustomerAccount(customerId))
    } catch {
      setAccount(null)
      setFailed(true)
    } finally {
      setLoading(false)
    }
  }, [customerId])

  useEffect(() => {
    loadAccount()
  }, [loadAccount])

  const movements = useMemo(() => {
    const rows = Array.isArray(account?.movements) ? account.movements : []
    return rows.filter((movement) => {
      if (fromDate && movement.movementDate < fromDate) {
        return false
      }
      if (toDate && movement.movementDate > toDate) {
        return false
      }
      return true
    })
  }, [account, fromDate, toDate])

  function openPayment() {
    try {
      registrationKey.current = createPlotterPaymentRegistrationKey()
    } catch {
      setPaymentError('No fue posible preparar el registro del pago.')
      return
    }
    setPaymentError('')
    setPaymentOpen(true)
  }

  async function handlePayment(payload) {
    if (!canSubmitPlotterPayment(submitting)) {
      return
    }
    const registrationKeyValue = registrationKey.current
    if (!registrationKeyValue) {
      setPaymentError('No fue posible preparar el registro del pago.')
      return
    }
    setSubmitting(true)
    setPaymentError('')
    try {
      await registerPlotterCustomerPayment(customerId, {
        ...payload,
        registrationKey: registrationKeyValue,
      })
      setPaymentOpen(false)
      registrationKey.current = null
      await loadAccount()
      setSuccessOpen(true)
    } catch (error) {
      registrationKey.current = registrationKeyAfterPaymentFailure(registrationKeyValue)
      setPaymentError(
        resolveApiErrorMessage(error, 'No fue posible registrar el pago de la cuenta.')
      )
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return <Skeleton variant="rounded" height={220} />
  }

  if (failed || !account) {
    return (
      <Stack spacing={2}>
        <Alert severity="error">No fue posible cargar la cuenta del cliente.</Alert>
        <Button onClick={() => navigate('/plotter/customer-accounts')}>Volver</Button>
      </Stack>
    )
  }

  return (
    <>
      <Stack spacing={3}>
        <PageHeader
          title={account.customerName || 'Cuenta de Plotter'}
          subtitle="Cuenta corriente"
          actions={
            <Stack direction="row" spacing={1}>
              <Button onClick={() => navigate('/plotter/customer-accounts')}>Volver</Button>
              {admin && (
                <Button variant="contained" onClick={openPayment}>
                  Registrar pago
                </Button>
              )}
            </Stack>
          }
        />
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <Paper sx={{ p: 2, flex: 1 }}>
            <Typography variant="body2" color="text.secondary">Débitos de trabajos</Typography>
            <Typography variant="h6">{formatPlotterMoney(account.debitTotal)}</Typography>
          </Paper>
          <Paper sx={{ p: 2, flex: 1 }}>
            <Typography variant="body2" color="text.secondary">Pagos recibidos</Typography>
            <Typography variant="h6">{formatPlotterMoney(account.paymentTotal)}</Typography>
          </Paper>
          <Paper sx={{ p: 2, flex: 1 }}>
            <Typography variant="body2" color="text.secondary">Saldo pendiente</Typography>
            <Typography variant="h6">{formatPlotterMoney(account.outstandingAmount)}</Typography>
          </Paper>
        </Stack>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <TextField
            label="Desde"
            type="date"
            value={fromDate}
            onChange={(event) => setFromDate(event.target.value)}
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            label="Hasta"
            type="date"
            value={toDate}
            onChange={(event) => setToDate(event.target.value)}
            InputLabelProps={{ shrink: true }}
          />
        </Stack>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={headerCellSx}>Fecha</TableCell>
                <TableCell sx={headerCellSx}>Movimiento</TableCell>
                <TableCell sx={headerCellSx}>Detalle</TableCell>
                <TableCell align="right" sx={headerCellSx}>Valor</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {movements.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4}>
                    <Typography color="text.secondary" sx={{ py: 2 }}>
                      No hay movimientos en el rango seleccionado.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                movements.map((movement) => (
                  <TableRow key={`${movement.movementType}-${movement.plotterJobId || movement.paymentId}`}>
                    <TableCell>{formatPlotterDate(movement.movementDate)}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={movement.movementType === 'CUSTOMER_PAYMENT' ? 'Pago' : 'Trabajo'}
                        color={movement.movementType === 'CUSTOMER_PAYMENT' ? 'success' : 'info'}
                      />
                    </TableCell>
                    <TableCell>
                      <Stack spacing={0.25}>
                        <Typography>{movement.description || '—'}</Typography>
                        {movement.movementType === 'JOB_DEBIT' && (
                          <Typography variant="body2" color="text.secondary">
                            {formatPlotterNumber(movement.printedMeters)} m ·{' '}
                            {formatPlotterMachineLabel(movement.plotterMachineId, movement.machineName)}
                          </Typography>
                        )}
                        {movement.plotterJobId && (
                          <Button
                            size="small"
                            sx={{ alignSelf: 'flex-start', px: 0 }}
                            onClick={() => navigate(`/plotter/jobs/${movement.plotterJobId}`)}
                          >
                            Ver trabajo
                          </Button>
                        )}
                      </Stack>
                    </TableCell>
                    <TableCell align="right">{formatPlotterMoney(movement.amount)}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Stack>
      <RegisterPlotterPaymentDialog
        open={paymentOpen}
        title="Registrar pago de la cuenta"
        totalLabel="Total de trabajos"
        paidLabel="Pagos recibidos"
        totalAmount={account.debitTotal}
        paidAmount={account.paymentTotal}
        outstandingAmount={account.outstandingAmount}
        registrationKey={registrationKey.current}
        onClose={() => {
          if (!submitting) {
            setPaymentOpen(false)
          }
        }}
        onSubmit={handlePayment}
        submitting={submitting}
        errorMessage={paymentError}
      />
      <Snackbar
        open={successOpen}
        autoHideDuration={4000}
        onClose={() => setSuccessOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity="success" variant="filled" onClose={() => setSuccessOpen(false)}>
          Pago registrado en la cuenta del cliente.
        </Alert>
      </Snackbar>
    </>
  )
}

export default PlotterCustomerAccountDetailPage
