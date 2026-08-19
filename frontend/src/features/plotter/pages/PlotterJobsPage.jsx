import { useCallback, useEffect, useMemo, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import PrintOutlinedIcon from '@mui/icons-material/PrintOutlined'
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
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { getCustomers, getOrders } from '../../commercial/services/commercialService'
import { getInventoryItems, getPlotterPaperRolls } from '../../inventory/services/inventoryService'
import CreatePlotterJobDialog from '../components/CreatePlotterJobDialog'
import RegisterPlotterPaymentDialog from '../components/RegisterPlotterPaymentDialog'
import {
  canRegisterExternalPlotterPayment,
  formatPlotterCustomerLabel,
  formatPlotterDate,
  formatPlotterJobTypeLabel,
  formatPlotterMoney,
  formatPlotterNumber,
  formatPlotterOrderLabel,
  getPlotterStatusChipProps,
  isExternalPlotterPaymentComplete,
} from '../presentation/plotterJobPresentation'
import { createPlotterJob, getPlotterJobs, registerPlotterPayment } from '../services/plotterService'
import MonthPeriodNavigator from '../../../shared/period/MonthPeriodNavigator'
import { formatMonthPeriodLabel, getCalendarMonthRange } from '../../../shared/period/monthPeriod'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4
const TABLE_COLUMN_COUNT = 11

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function PlotterJobsTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Tipo</TableCell>
        <TableCell sx={headerCellSx}>Cliente</TableCell>
        <TableCell sx={headerCellSx}>Orden</TableCell>
        <TableCell sx={headerCellSx}>Fecha</TableCell>
        <TableCell sx={headerCellSx}>Rollo</TableCell>
        <TableCell sx={headerCellSx}>Metros</TableCell>
        <TableCell sx={headerCellSx}>Total</TableCell>
        <TableCell sx={headerCellSx}>Pagado</TableCell>
        <TableCell sx={headerCellSx}>Saldo</TableCell>
        <TableCell align="center" sx={headerCellSx}>
          Estado
        </TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Acciones
        </TableCell>
      </TableRow>
    </TableHead>
  )
}

function PlotterJobsPage() {
  const navigate = useNavigate()
  const initialPeriod = useMemo(() => getCalendarMonthRange(), [])
  const [period, setPeriod] = useState(initialPeriod)
  const [jobs, setJobs] = useState([])
  const [customers, setCustomers] = useState([])
  const [orders, setOrders] = useState([])
  const [paperRolls, setPaperRolls] = useState([])
  const [inventoryLookup, setInventoryLookup] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadingLookups, setLoadingLookups] = useState(true)
  const [failed, setFailed] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')
  const [paymentJob, setPaymentJob] = useState(null)
  const [registeringPayment, setRegisteringPayment] = useState(false)
  const [paymentError, setPaymentError] = useState('')
  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  const customerNameById = useMemo(() => {
    const map = new Map()
    customers.forEach((customer) => {
      map.set(customer.customerId, customer.name)
    })
    return map
  }, [customers])

  const paperLabelById = useMemo(() => {
    const map = new Map()
    inventoryLookup.forEach((item) => {
      if (item.paperRollNumber) {
        map.set(item.inventoryItemId, item.paperRollNumber)
      } else {
        map.set(
          item.inventoryItemId,
          item.name || item.materialCode || item.inventoryItemId
        )
      }
    })
    return map
  }, [inventoryLookup])

  const loadJobs = useCallback(async () => {
    setLoading(true)
    setFailed(false)

    try {
      const data = await getPlotterJobs({
        fromDate: period.fromDate,
        toDate: period.toDate,
      })
      setJobs(Array.isArray(data?.jobs) ? data.jobs : [])
      setLoading(false)
    } catch {
      setJobs([])
      setFailed(true)
      setLoading(false)
    }
  }, [period.fromDate, period.toDate])

  async function loadLookups() {
    setLoadingLookups(true)

    try {
      const [customersData, ordersData, rollsData, inventoryData] = await Promise.all([
        getCustomers(),
        getOrders(),
        getPlotterPaperRolls(),
        getInventoryItems(),
      ])
      setCustomers(
        Array.isArray(customersData?.customers) ? customersData.customers : []
      )
      setOrders(Array.isArray(ordersData?.orders) ? ordersData.orders : [])
      setPaperRolls(Array.isArray(rollsData?.items) ? rollsData.items : [])
      setInventoryLookup(
        Array.isArray(inventoryData?.items) ? inventoryData.items : []
      )
    } catch {
      setCustomers([])
      setOrders([])
      setPaperRolls([])
      setInventoryLookup([])
    } finally {
      setLoadingLookups(false)
    }
  }

  useEffect(() => {
    loadJobs()
  }, [loadJobs])

  useEffect(() => {
    loadLookups()
  }, [])

  function openCreateDialog() {
    if (creating) {
      return
    }

    setCreateError('')
    setCreateDialogOpen(true)
  }

  function handleCreateDialogClose() {
    if (creating) {
      return
    }

    setCreateDialogOpen(false)
    setCreateError('')
  }

  async function handleCreatePlotterJob(payload) {
    if (creating) {
      return
    }

    setCreateError('')
    setCreating(true)

    try {
      await createPlotterJob(payload)
      await loadJobs()
      setCreateDialogOpen(false)
      setSuccessMessage('Trabajo de plotter registrado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setCreateError(
        resolveApiErrorMessage(error, 'No fue posible registrar el trabajo de plotter.')
      )
    } finally {
      setCreating(false)
    }
  }

  function openPaymentDialog(job) {
    if (registeringPayment || !canRegisterExternalPlotterPayment(job)) {
      return
    }
    setPaymentError('')
    setPaymentJob(job)
  }

  function handlePaymentDialogClose() {
    if (registeringPayment) {
      return
    }
    setPaymentJob(null)
    setPaymentError('')
  }

  async function handleRegisterPayment(payload) {
    if (registeringPayment || !paymentJob) {
      return
    }

    setPaymentError('')
    setRegisteringPayment(true)

    try {
      await registerPlotterPayment(paymentJob.plotterJobId, payload)
      await loadJobs()
      setPaymentJob(null)
      setSuccessMessage('Pago de Plotter registrado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setPaymentError(
        resolveApiErrorMessage(error, 'No fue posible registrar el pago.')
      )
    } finally {
      setRegisteringPayment(false)
    }
  }

  return (
    <>
      <Stack spacing={3}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          justifyContent="space-between"
          alignItems={{ xs: 'stretch', sm: 'center' }}
        >
          <Typography variant="h3">Plotter</Typography>
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={1.5}
            sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
          >
            <Button
              variant="outlined"
              onClick={() => navigate('/plotter/profitability')}
              disabled={loading}
            >
              Ver rentabilidad
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={openCreateDialog}
              disabled={loading || creating}
            >
              Nuevo trabajo
            </Button>
          </Stack>
        </Stack>

        <MonthPeriodNavigator
          fromDate={period.fromDate}
          disabled={loading}
          onPeriodChange={setPeriod}
        />

        {loading && (
          <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
            <Table>
              <PlotterJobsTableHead />
              <TableBody>
                {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                  <TableRow key={`skeleton-${index}`}>
                    {Array.from({ length: TABLE_COLUMN_COUNT }).map((__, cellIndex) => (
                      <TableCell key={`skeleton-cell-${cellIndex}`}>
                        <Skeleton variant="text" />
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {!loading && failed && (
          <Alert severity="error">
            No fue posible cargar los trabajos de plotter.
          </Alert>
        )}

        {!loading && !failed && jobs.length === 0 && (
          <Paper sx={{ p: 4 }}>
            <Stack spacing={1} alignItems="center">
              <PrintOutlinedIcon color="disabled" sx={{ fontSize: 40 }} />
              <Typography variant="h6">
                Sin trabajos de plotter en {formatMonthPeriodLabel(period.fromDate)}
              </Typography>
              <Typography color="text.secondary" align="center">
                Cambia de mes para ver el histórico o registra un nuevo trabajo.
              </Typography>
            </Stack>
          </Paper>
        )}

        {!loading && !failed && jobs.length > 0 && (
          <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
            <Table>
              <PlotterJobsTableHead />
              <TableBody>
                {jobs.map((job) => {
                  const statusChip = getPlotterStatusChipProps(job.status)
                  const customerLabel = formatPlotterCustomerLabel(
                    job,
                    customerNameById.get(job.customerId)
                  )
                  const paperLabel =
                    paperLabelById.get(job.paperInventoryItemId) ||
                    'Histórico / legado'

                  return (
                    <TableRow key={job.plotterJobId} hover>
                      <TableCell>{formatPlotterJobTypeLabel(job.jobType)}</TableCell>
                      <TableCell>{customerLabel}</TableCell>
                      <TableCell>{formatPlotterOrderLabel(job)}</TableCell>
                      <TableCell>{formatPlotterDate(job.creationDate)}</TableCell>
                      <TableCell>{paperLabel}</TableCell>
                      <TableCell>
                        {formatPlotterNumber(job.printedMeters)} m
                      </TableCell>
                      <TableCell>
                        {formatPlotterMoney(job.totalAmount)}
                      </TableCell>
                      <TableCell>
                        {formatPlotterMoney(job.paidAmount)}
                      </TableCell>
                      <TableCell>
                        {formatPlotterMoney(job.outstandingAmount)}
                      </TableCell>
                      <TableCell align="center">
                        <Chip size="small" {...statusChip} />
                      </TableCell>
                      <TableCell align="right">
                        <Stack
                          direction="row"
                          spacing={1}
                          justifyContent="flex-end"
                          flexWrap="wrap"
                          useFlexGap
                          sx={{ minWidth: 0 }}
                        >
                          <Button
                            size="small"
                            onClick={() =>
                              navigate(`/plotter/jobs/${job.plotterJobId}`)
                            }
                          >
                            Ver detalle
                          </Button>
                          {canRegisterExternalPlotterPayment(job) ? (
                            <Button
                              size="small"
                              variant="contained"
                              disabled={registeringPayment}
                              onClick={() => openPaymentDialog(job)}
                            >
                              Registrar pago
                            </Button>
                          ) : null}
                          {isExternalPlotterPaymentComplete(job) ? (
                            <Typography
                              variant="body2"
                              color="success.main"
                              sx={{ alignSelf: 'center' }}
                            >
                              Pago completado
                            </Typography>
                          ) : null}
                        </Stack>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Stack>

      <CreatePlotterJobDialog
        open={createDialogOpen}
        onClose={handleCreateDialogClose}
        onSubmit={handleCreatePlotterJob}
        submitting={creating}
        errorMessage={createError}
        customers={customers}
        orders={orders.map((order) => ({
          ...order,
          customerName: customerNameById.get(order.customerId) || '',
        }))}
        paperRolls={paperRolls}
        loadingLookups={loadingLookups}
      />

      <RegisterPlotterPaymentDialog
        open={Boolean(paymentJob)}
        totalAmount={paymentJob?.totalAmount}
        paidAmount={paymentJob?.paidAmount}
        outstandingAmount={paymentJob?.outstandingAmount}
        onClose={handlePaymentDialogClose}
        onSubmit={handleRegisterPayment}
        submitting={registeringPayment}
        errorMessage={paymentError}
      />

      <Snackbar
        open={successOpen}
        autoHideDuration={4000}
        onClose={() => setSuccessOpen(false)}
        message={successMessage}
      />
    </>
  )
}

export default PlotterJobsPage
