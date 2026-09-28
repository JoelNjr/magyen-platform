import { useEffect, useMemo, useState } from 'react'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import {
  Alert,
  Button,
  FormControl,
  MenuItem,
  Paper,
  Select,
  Skeleton,
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import DeliverOrderDialog from '../components/DeliverOrderDialog'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import {
  applyOrderStatusToList,
  canSubmitOrderStatusChange,
  hasNextOrderStatus,
  orderStatusChangeErrorMessage,
  orderStatusChangeOptions,
  orderStatusChangeSuccessMessage,
  resolveOrderStatusSelection,
} from '../presentation/orderListStatusChange'
import {
  buildCustomerNameMap,
  resolveCustomerName,
} from '../presentation/resolveCustomerName'
import {
  closeOrder,
  deliverOrder,
  getCustomers,
  getOrders,
  markOrderReadyForDelivery,
  startOrderProduction,
} from '../services/commercialService'
import MonthPeriodNavigator from '../../../shared/period/MonthPeriodNavigator'
import { formatMonthPeriodLabel, getCalendarMonthRange } from '../../../shared/period/monthPeriod'
import PageHeader from '../../../layout/PageHeader'
import EmptyState from '../../home/components/EmptyState'

const currencyFormatter = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})

function formatCurrency(amount) {
  return currencyFormatter.format(amount)
}

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

function OrdersTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Número de orden</TableCell>
        <TableCell sx={headerCellSx}>Descripción</TableCell>
        <TableCell sx={headerCellSx}>Cliente</TableCell>
        <TableCell align="center" sx={headerCellSx}>
          Estado
        </TableCell>
        <TableCell sx={headerCellSx}>Fecha de confirmación</TableCell>
        <TableCell sx={headerCellSx}>Vendedor</TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Total
        </TableCell>
      </TableRow>
    </TableHead>
  )
}

function OrdersPage() {
  const navigate = useNavigate()
  const initialPeriod = useMemo(() => getCalendarMonthRange(), [])
  const [period, setPeriod] = useState(initialPeriod)
  const [orders, setOrders] = useState([])
  const [customerNameById, setCustomerNameById] = useState({})
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [changingOrderId, setChangingOrderId] = useState(null)
  const [statusError, setStatusError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [successOpen, setSuccessOpen] = useState(false)
  const [deliverTarget, setDeliverTarget] = useState(null)
  const [deliverError, setDeliverError] = useState('')

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    getOrders({ fromDate: period.fromDate, toDate: period.toDate })
      .then((data) => {
        setOrders(data.orders ?? [])
        setLoading(false)
      })
      .catch(() => {
        setFailed(true)
        setLoading(false)
      })

    getCustomers()
      .then((data) => {
        setCustomerNameById(buildCustomerNameMap(data?.customers))
      })
      .catch(() => {
        setCustomerNameById({})
      })
  }, [period.fromDate, period.toDate])

  function showStatusSuccess(status) {
    setStatusError('')
    setSuccessMessage(orderStatusChangeSuccessMessage(status))
    setSuccessOpen(true)
  }

  async function commitStatusChange(order, action) {
    if (action === 'start-production') {
      return startOrderProduction(order.orderId)
    }
    if (action === 'ready-for-delivery') {
      return markOrderReadyForDelivery(order.orderId)
    }
    if (action === 'close') {
      return closeOrder(order.orderId)
    }
    return null
  }

  async function handleStatusChange(order, selectedStatus) {
    if (!canSubmitOrderStatusChange(changingOrderId, order.orderId)) {
      return
    }
    const decision = resolveOrderStatusSelection(order.status, selectedStatus)
    if (decision.type === 'none') {
      return
    }
    if (decision.type === 'invalid') {
      setStatusError('Esa transición de estado no está permitida.')
      return
    }
    if (decision.type === 'requires-delivery-date') {
      setDeliverError('')
      setDeliverTarget(order)
      return
    }

    setStatusError('')
    setChangingOrderId(order.orderId)
    try {
      const updated = await commitStatusChange(order, decision.action)
      if (!updated?.status) {
        setStatusError('No fue posible confirmar el nuevo estado del pedido.')
        return
      }
      setOrders((current) =>
        applyOrderStatusToList(current, order.orderId, updated.status)
      )
      showStatusSuccess(updated.status)
    } catch (error) {
      setStatusError(
        orderStatusChangeErrorMessage(
          error,
          'No fue posible actualizar el estado del pedido.'
        )
      )
    } finally {
      setChangingOrderId(null)
    }
  }

  async function handleDeliverFromList(deliveryDate) {
    if (!deliverTarget || !canSubmitOrderStatusChange(changingOrderId, deliverTarget.orderId)) {
      return
    }
    setDeliverError('')
    setStatusError('')
    setChangingOrderId(deliverTarget.orderId)
    try {
      const updated = await deliverOrder(deliverTarget.orderId, deliveryDate)
      if (!updated?.status) {
        setDeliverError('No fue posible confirmar el nuevo estado del pedido.')
        return
      }
      const deliveredOrderId = deliverTarget.orderId
      setOrders((current) =>
        applyOrderStatusToList(current, deliveredOrderId, updated.status)
      )
      setDeliverTarget(null)
      showStatusSuccess(updated.status)
    } catch (error) {
      setDeliverError(
        orderStatusChangeErrorMessage(error, 'No fue posible registrar la entrega.')
      )
    } finally {
      setChangingOrderId(null)
    }
  }

  return (
    <Stack spacing={3}>
        <PageHeader
          title="Órdenes"
          actions={
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={1.5}
          sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
        >
          <Button
            variant="outlined"
            onClick={() => navigate('/commercial')}
          >
            Cotizaciones
          </Button>
          <Button
            variant="outlined"
            onClick={() => navigate('/commercial/customers')}
          >
            Clientes
          </Button>
          </Stack>
          }
        />

        <MonthPeriodNavigator
          fromDate={period.fromDate}
          disabled={loading}
          onPeriodChange={setPeriod}
        />

        {loading && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <OrdersTableHead />
            <TableBody>
              {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                <TableRow key={`order-skeleton-${index}`}>
                  <TableCell>
                    <Skeleton width={100} />
                  </TableCell>
                  <TableCell>
                    <Skeleton width="80%" />
                  </TableCell>
                  <TableCell>
                    <Skeleton width="80%" />
                  </TableCell>
                  <TableCell align="center">
                    <Skeleton
                      width={90}
                      height={28}
                      sx={{ mx: 'auto', borderRadius: 4 }}
                    />
                  </TableCell>
                  <TableCell>
                    <Skeleton width={100} />
                  </TableCell>
                  <TableCell>
                    <Skeleton width="70%" />
                  </TableCell>
                  <TableCell align="right">
                    <Skeleton width={90} sx={{ ml: 'auto' }} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {!loading && failed && (
        <Alert severity="error">
          No fue posible obtener las órdenes.
        </Alert>
      )}

      {statusError ? (
        <Alert severity="error" onClose={() => setStatusError('')}>
          {statusError}
        </Alert>
      ) : null}

      {!loading && !failed && orders.length === 0 && (
        <EmptyState
          icon={<Inventory2OutlinedIcon color="action" sx={{ fontSize: 48 }} />}
          title={`No hay órdenes en ${formatMonthPeriodLabel(period.fromDate)}`}
          message="Cambia de mes para ver el histórico. Las órdenes se crean desde una cotización aprobada."
          action={
            <Button variant="contained" onClick={() => navigate('/commercial')}>
              Ir a cotizaciones
            </Button>
          }
        />
      )}

      {!loading && !failed && orders.length > 0 && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <OrdersTableHead />
            <TableBody>
              {orders.map((order) => {
                const statusOptions = orderStatusChangeOptions(order.status)
                const statusBusy = changingOrderId === order.orderId

                return (
                  <TableRow key={order.orderId} hover>
                    <TableCell>
                      <RouterLink to={`/commercial/orders/${order.orderId}`}>
                        {order.orderNumber}
                      </RouterLink>
                    </TableCell>
                    <TableCell>{order.description || '—'}</TableCell>
                    <TableCell>
                      {order.customerName ||
                        resolveCustomerName(
                          order.customerId,
                          customerNameById
                        )}
                    </TableCell>
                    <TableCell align="center">
                      <FormControl size="small" sx={{ minWidth: 180 }}>
                        <Select
                          value={order.status}
                          disabled={Boolean(changingOrderId) || !hasNextOrderStatus(order.status)}
                          onChange={(event) => handleStatusChange(order, event.target.value)}
                          inputProps={{ 'aria-label': `Estado de ${order.orderNumber}` }}
                        >
                          {statusOptions.map((option) => (
                            <MenuItem key={option.status} value={option.status}>
                              {statusBusy && option.current ? 'Actualizando…' : option.label}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    </TableCell>
                    <TableCell>
                      {formatDisplayDate(order.confirmationDate)}
                    </TableCell>
                    <TableCell>{order.sellerName || '—'}</TableCell>
                    <TableCell align="right">
                      {formatCurrency(order.totalAmount)}
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <DeliverOrderDialog
        open={Boolean(deliverTarget)}
        confirmationDate={deliverTarget?.confirmationDate}
        onClose={() => {
          if (!changingOrderId) {
            setDeliverTarget(null)
            setDeliverError('')
          }
        }}
        onSubmit={handleDeliverFromList}
        submitting={Boolean(changingOrderId)}
        errorMessage={deliverError}
      />

      <Snackbar
        open={successOpen}
        autoHideDuration={4000}
        onClose={() => setSuccessOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          severity="success"
          variant="filled"
          onClose={() => setSuccessOpen(false)}
        >
          {successMessage}
        </Alert>
      </Snackbar>
    </Stack>
  )
}

export default OrdersPage
