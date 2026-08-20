import { useEffect, useMemo, useState } from 'react'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
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
} from '@mui/material'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import { getOrderStatusChipProps } from '../presentation/orderStatusPresentation'
import {
  buildCustomerNameMap,
  resolveCustomerName,
} from '../presentation/resolveCustomerName'
import { getCustomers, getOrders } from '../services/commercialService'
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
                const statusChip = getOrderStatusChipProps(order.status)

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
                      <Chip
                        label={statusChip.label}
                        color={statusChip.color}
                        size="small"
                      />
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
    </Stack>
  )
}

export default OrdersPage
