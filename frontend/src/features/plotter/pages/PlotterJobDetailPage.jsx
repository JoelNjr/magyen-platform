import { useCallback, useEffect, useState } from 'react'
import PaymentsOutlinedIcon from '@mui/icons-material/PaymentsOutlined'
import PrintOutlinedIcon from '@mui/icons-material/PrintOutlined'
import {
  Alert,
  Button,
  Chip,
  Grid,
  Link,
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
  Typography,
} from '@mui/material'
import { Link as RouterLink, useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { isAdmin } from '../../auth/presentation/authPresentation'
import { getCustomers } from '../../commercial/services/commercialService'
import {
  getInventoryItem,
  getInventoryMovements,
} from '../../inventory/services/inventoryService'
import EditPlotterJobDialog from '../components/EditPlotterJobDialog'
import {
  canOpenExternalPlotterAccount,
  formatPlotterCustomerLabel,
  formatPlotterDate,
  formatPlotterMoney,
  formatPlotterNumber,
  formatPlotterOrderLabel,
  getPlotterJobTypeChipProps,
  getPlotterStatusChipProps,
  isPlotterDateOnlyCorrection,
  isExternalPlotterJob,
  isInternalPlotterJob,
  isWastePlotterJob,
} from '../presentation/plotterJobPresentation'
import { formatPlotterMachineLabel } from '../presentation/plotterMachinePresentation'
import {
  cancelPlotterJob,
  correctPlotterJobDate,
  editPlotterJob,
  getPlotterJob,
  getPlotterPayments,
} from '../services/plotterService'
import PageHeader from '../../../layout/PageHeader'

function DetailField({ label, children }) {
  return (
    <Stack spacing={0.5}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      {children}
    </Stack>
  )
}

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function PlotterJobDetailPage() {
  const { plotterJobId } = useParams()
  const navigate = useNavigate()
  const { identity } = useAuth()
  const admin = isAdmin(identity)

  const [job, setJob] = useState(null)
  const [payments, setPayments] = useState([])
  const [customerName, setCustomerName] = useState('')
  const [paperLabel, setPaperLabel] = useState('')
  const [legacyPaper, setLegacyPaper] = useState(false)
  const [materialCost, setMaterialCost] = useState(null)
  const [consumedMeters, setConsumedMeters] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [notFound, setNotFound] = useState(false)
  const [paymentsFailed, setPaymentsFailed] = useState(false)

  const [editOpen, setEditOpen] = useState(false)
  const [savingJob, setSavingJob] = useState(false)
  const [editError, setEditError] = useState('')
  const [actionError, setActionError] = useState('')
  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  const loadDetail = useCallback(async () => {
    setLoading(true)
    setFailed(false)
    setNotFound(false)
    setPaymentsFailed(false)
    setLegacyPaper(false)
    setMaterialCost(null)
    setConsumedMeters(null)

    try {
      const [plotterJob, customersData, paymentsData] = await Promise.all([
        getPlotterJob(plotterJobId),
        getCustomers().catch(() => ({ customers: [] })),
        getPlotterPayments(plotterJobId).catch(() => null),
      ])

      setJob(plotterJob)

      if (paymentsData) {
        setPayments(Array.isArray(paymentsData.payments) ? paymentsData.payments : [])
      } else {
        setPayments([])
        setPaymentsFailed(true)
      }

      const customers = Array.isArray(customersData?.customers)
        ? customersData.customers
        : []
      const matchedCustomer = customers.find(
        (customer) => customer.customerId === plotterJob.customerId
      )
      setCustomerName(
        formatPlotterCustomerLabel(plotterJob, matchedCustomer?.name)
      )

      try {
        const inventoryItem = await getInventoryItem(plotterJob.paperInventoryItemId)
        if (inventoryItem?.plotterPaperRoll && inventoryItem?.paperRollNumber) {
          setPaperLabel(inventoryItem.paperRollNumber)
          setLegacyPaper(false)
        } else {
          setPaperLabel(
            inventoryItem?.name ||
              inventoryItem?.materialCode ||
              plotterJob.paperInventoryItemId
          )
          setLegacyPaper(true)
        }

        const movementsData = await getInventoryMovements(
          plotterJob.paperInventoryItemId
        )
        const movements = Array.isArray(movementsData?.movements)
          ? movementsData.movements
          : []
        const matchingMovement = movements.find(
          (movement) =>
            movement.sourceType === 'PLOTTER' &&
            movement.sourceId === plotterJob.plotterJobId
        )

        if (matchingMovement) {
          setConsumedMeters(matchingMovement.quantity)
          setMaterialCost(matchingMovement.totalCost)
        }
      } catch {
        setPaperLabel(plotterJob.paperInventoryItemId)
        setLegacyPaper(true)
      }

      setLoading(false)
    } catch (error) {
      if (error?.response?.status === 400 || error?.response?.status === 404) {
        setNotFound(true)
      } else {
        setFailed(true)
      }
      setJob(null)
      setLoading(false)
    }
  }, [plotterJobId])

  useEffect(() => {
    loadDetail()
  }, [loadDetail])

  async function handleEditJob(payload) {
    if (savingJob || !job) {
      return
    }
    setSavingJob(true)
    setEditError('')
    try {
      if (isPlotterDateOnlyCorrection(job, payload)) {
        await correctPlotterJobDate(job.plotterJobId, payload.creationDate)
      } else {
        await editPlotterJob(job.plotterJobId, payload)
      }
      setEditOpen(false)
      await loadDetail()
      setSuccessMessage('Trabajo de plotter corregido.')
      setSuccessOpen(true)
    } catch (error) {
      setEditError(resolveApiErrorMessage(error, 'No fue posible corregir el trabajo.'))
    } finally {
      setSavingJob(false)
    }
  }

  async function handleCancelJob() {
    if (!job || job.status === 'CANCELLED') {
      return
    }
    const confirmed = window.confirm(
      'Anular este trabajo revierte el consumo de papel y el débito pendiente. Los pagos ya recibidos se conservan.'
    )
    if (!confirmed) {
      return
    }
    setActionError('')
    try {
      const result = await cancelPlotterJob(job.plotterJobId)
      if (result?.deleted) {
        navigate('/plotter')
        return
      }
      await loadDetail()
      setSuccessMessage('Trabajo de plotter anulado.')
      setSuccessOpen(true)
    } catch (error) {
      setActionError(resolveApiErrorMessage(error, 'No fue posible anular el trabajo.'))
    }
  }

  if (loading) {
    return (
      <Stack spacing={3}>
        <Skeleton variant="text" width={280} height={48} />
        <Paper sx={{ p: 3 }}>
          <Skeleton variant="rectangular" height={180} />
        </Paper>
      </Stack>
    )
  }

  if (notFound) {
    return (
      <Stack spacing={2}>
        <Alert severity="warning">No se encontró el trabajo de plotter.</Alert>
        <Button onClick={() => navigate('/plotter')}>Volver a Plotter</Button>
      </Stack>
    )
  }

  if (failed || !job) {
    return (
      <Stack spacing={2}>
        <Alert severity="error">
          No fue posible cargar el detalle del trabajo de plotter.
        </Alert>
        <Button onClick={() => navigate('/plotter')}>Volver a Plotter</Button>
      </Stack>
    )
  }

  const statusChip = getPlotterStatusChipProps(job.status)
  const outstanding = Number(job.outstandingAmount ?? 0)
  const internalJob = isInternalPlotterJob(job.jobType)
  const wasteJob = isWastePlotterJob(job.jobType)
  const externalJob = isExternalPlotterJob(job.jobType)
  const cancelled = job.status === 'CANCELLED'

  return (
    <>
      <Stack spacing={3}>
        <PageHeader
          title="Trabajo de Plotter"
          subtitle={<Chip size="small" {...getPlotterJobTypeChipProps(job.jobType)} />}
          actions={
            <Stack direction="row" spacing={1}>
              <Button onClick={() => navigate('/plotter')}>Volver</Button>
              {admin && !cancelled && (
                <Button
                  onClick={() => {
                    setEditError('')
                    setEditOpen(true)
                  }}
                >
                  Editar
                </Button>
              )}
              {admin && !cancelled && (
                <Button color="warning" onClick={handleCancelJob}>
                  Anular
                </Button>
              )}
            </Stack>
          }
        />

        <Paper sx={{ p: 3 }}>
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={4}>
              <DetailField label="Tipo de trabajo">
                <Chip size="small" {...getPlotterJobTypeChipProps(job.jobType)} />
              </DetailField>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <DetailField label="Cliente">
                <Typography>{customerName}</Typography>
              </DetailField>
            </Grid>
            {internalJob && (
              <Grid item xs={12} sm={6} md={4}>
                <DetailField label="Orden">
                  <Typography>{formatPlotterOrderLabel(job)}</Typography>
                </DetailField>
              </Grid>
            )}
            <Grid item xs={12} sm={6} md={4}>
              <DetailField label="Fecha">
                <Typography>{formatPlotterDate(job.creationDate)}</Typography>
              </DetailField>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <DetailField label="Estado">
                <Chip size="small" {...statusChip} />
              </DetailField>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <DetailField label="Máquina">
                <Typography>
                  {formatPlotterMachineLabel(job.plotterMachineId, job.machineName)}
                </Typography>
              </DetailField>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <DetailField label="Rollo utilizado">
                <Typography>
                  {paperLabel}
                  {legacyPaper ? ' (histórico / legado)' : ''}
                </Typography>
                {!legacyPaper && (
                  <Link
                    component={RouterLink}
                    to={`/inventory/${job.paperInventoryItemId}`}
                    underline="hover"
                  >
                    Ver inventario
                  </Link>
                )}
              </DetailField>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <DetailField label="Metros impresos">
                <Typography>{formatPlotterNumber(job.printedMeters)} m</Typography>
              </DetailField>
            </Grid>
            {!internalJob && !wasteJob && (
              <>
                <Grid item xs={12} sm={6} md={4}>
                  <DetailField label="Precio por metro">
                    <Typography>{formatPlotterMoney(job.pricePerMeter)}</Typography>
                  </DetailField>
                </Grid>
                <Grid item xs={12} sm={6} md={4}>
                  <DetailField label="Total cobrado">
                    <Typography variant="h6">
                      {formatPlotterMoney(job.totalAmount)}
                    </Typography>
                  </DetailField>
                </Grid>
              </>
            )}
            {internalJob && (
              <Grid item xs={12}>
                <Alert severity="info">
                  Este trabajo es una operación de material de producción, no una
                  venta. El papel se registra una sola vez.
                </Alert>
              </Grid>
            )}
            {wasteJob && (
              <Grid item xs={12}>
                <Alert severity="info">
                  Este trabajo registra merma de papel. No genera cobro a cliente
                  ni ingreso de Finanzas.
                </Alert>
              </Grid>
            )}
            <Grid item xs={12}>
              <DetailField label="Observaciones">
                <Typography>{job.observations || '—'}</Typography>
              </DetailField>
            </Grid>
          </Grid>
        </Paper>

        {externalJob && (
        <Paper sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={2}
              justifyContent="space-between"
              alignItems={{ xs: 'stretch', sm: 'center' }}
            >
              <Stack direction="row" spacing={1} alignItems="center">
                <PaymentsOutlinedIcon color="action" />
                <Typography variant="h6">Resumen de pago</Typography>
              </Stack>
              {canOpenExternalPlotterAccount(job) && (
                <Button
                  variant="contained"
                  onClick={() => navigate(`/plotter/customer-accounts/${job.customerId}`)}
                >
                  Ver cuenta
                </Button>
              )}
            </Stack>

            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <DetailField label="Valor del trabajo">
                  <Typography variant="h6">
                    {formatPlotterMoney(job.totalAmount)}
                  </Typography>
                </DetailField>
              </Grid>
              <Grid item xs={12} sm={4}>
                <DetailField label="Pagado">
                  <Typography variant="h6" color="success.main">
                    {formatPlotterMoney(job.paidAmount)}
                  </Typography>
                </DetailField>
              </Grid>
              <Grid item xs={12} sm={4}>
                <DetailField label="Saldo pendiente">
                  <Typography
                    variant="h6"
                    color={outstanding > 0 ? 'warning.main' : 'text.primary'}
                  >
                    {formatPlotterMoney(job.outstandingAmount)}
                  </Typography>
                </DetailField>
              </Grid>
            </Grid>

            {paymentsFailed ? (
              <Alert severity="error">No fue posible cargar el historial de pagos.</Alert>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 'bold' }}>Fecha</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>Monto</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>Observación</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {payments.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={3}>
                          <Typography color="text.secondary" sx={{ py: 2 }}>
                            No hay pagos registrados.
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ) : (
                      payments.map((payment) => (
                        <TableRow key={payment.paymentId}>
                          <TableCell>
                            {formatPlotterDate(payment.paymentDate)}
                          </TableCell>
                          <TableCell>
                            {formatPlotterMoney(payment.amount)}
                          </TableCell>
                          <TableCell>{payment.observations || '—'}</TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Stack>
        </Paper>
        )}

        <Paper sx={{ p: 3 }}>
          <Stack spacing={1.5}>
            <Stack direction="row" spacing={1} alignItems="center">
              <PrintOutlinedIcon color="action" />
              <Typography variant="h6">Consumo de material</Typography>
            </Stack>
            {legacyPaper && !materialCost && consumedMeters === null ? (
              <Alert severity="info">
                Este trabajo histórico no tiene consumo de inventario asociado. Los
                trabajos nuevos consumen el rollo de papel seleccionado.
              </Alert>
            ) : (
              <Grid container spacing={2}>
                <Grid item xs={12} sm={4}>
                  <DetailField label="Rollo">
                    <Typography>{paperLabel}</Typography>
                  </DetailField>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <DetailField label="Consumido">
                    <Typography>
                      {formatPlotterNumber(consumedMeters ?? job.printedMeters)} m
                    </Typography>
                  </DetailField>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <DetailField label="Costo material">
                    <Typography>
                      {materialCost === null || materialCost === undefined
                        ? 'Sin valoración histórica'
                        : formatPlotterMoney(materialCost)}
                    </Typography>
                  </DetailField>
                </Grid>
              </Grid>
            )}
          </Stack>
        </Paper>
      </Stack>

      {actionError && <Alert severity="error">{actionError}</Alert>}
      <EditPlotterJobDialog
        open={editOpen}
        job={job}
        onClose={() => {
          if (!savingJob) {
            setEditOpen(false)
            setEditError('')
          }
        }}
        onSubmit={handleEditJob}
        submitting={savingJob}
        errorMessage={editError}
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
    </>
  )
}

export default PlotterJobDetailPage
