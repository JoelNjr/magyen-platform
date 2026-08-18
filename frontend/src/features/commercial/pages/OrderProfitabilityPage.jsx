import { useEffect, useState } from 'react'
import ReceiptLongOutlinedIcon from '@mui/icons-material/ReceiptLongOutlined'
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
import { useNavigate } from 'react-router-dom'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import {
  formatLaborProductionCost,
  formatMaterialProductionCost,
  formatPlotterProductionCost,
  formatProfitabilityMoney,
  formatProfitabilityResultMargin,
  formatProfitabilityResultMoney,
  getOrderProfitabilityStatusChipProps,
} from '../presentation/orderProfitabilityPresentation'
import { getOrderProfitabilityList } from '../services/commercialService'

const headerCellSx = { fontWeight: 'bold', whiteSpace: 'nowrap' }
const SKELETON_ROW_COUNT = 4

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function ProfitabilityTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Pedido</TableCell>
        <TableCell sx={headerCellSx}>Cliente</TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Valor
        </TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Materiales
        </TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Mano de obra
        </TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Plotter
        </TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Costo total
        </TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Ganancia
        </TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Margen
        </TableCell>
        <TableCell align="center" sx={headerCellSx}>
          Estado
        </TableCell>
      </TableRow>
    </TableHead>
  )
}

function formatOrderLabel(order) {
  const number = order.orderNumber || '—'
  if (!order.description) {
    return number
  }
  return `${number} — ${order.description}`
}

function OrderProfitabilityPage() {
  const navigate = useNavigate()
  const [orders, setOrders] = useState([])
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  function loadProfitability() {
    setLoading(true)
    setFailed(false)
    setErrorMessage('')

    getOrderProfitabilityList()
      .then((data) => {
        setOrders(Array.isArray(data?.orders) ? data.orders : [])
        setSummary(data)
        setLoading(false)
      })
      .catch((error) => {
        setOrders([])
        setSummary(null)
        setFailed(true)
        setErrorMessage(
          resolveApiErrorMessage(error, 'No fue posible cargar la rentabilidad individual.')
        )
        setLoading(false)
      })
  }

  useEffect(() => {
    loadProfitability()
  }, [])

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        justifyContent="space-between"
        alignItems={{ xs: 'stretch', sm: 'center' }}
      >
        <Stack spacing={0.5}>
          <Typography variant="h3">Rentabilidad individual</Typography>
          <Typography variant="body2" color="text.secondary">
            Costos de producción del pedido: materiales, mano de obra y papel de
            Plotter interno. No es un gasto de Finanzas ni incluye saldo por cobrar.
          </Typography>
        </Stack>
        <Button
          variant="outlined"
          onClick={() => navigate('/commercial/orders')}
          sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
        >
          Ver órdenes
        </Button>
      </Stack>

      {failed ? (
        <Alert
          severity="error"
          action={
            <Button color="inherit" size="small" onClick={loadProfitability}>
              Reintentar
            </Button>
          }
        >
          {errorMessage}
        </Alert>
      ) : null}

      {!failed && summary ? (
        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
          {loading ? (
            <Skeleton width={280} height={32} />
          ) : (
            <>
              <Chip
                size="small"
                variant="outlined"
                label={`Pedidos evaluados: ${summary.evaluatedOrderCount ?? 0}`}
              />
              <Chip
                size="small"
                color="success"
                label={`Completos: ${summary.completeOrderCount ?? 0}`}
              />
              <Chip
                size="small"
                color="warning"
                label={`Parciales: ${summary.partiallyUnvaluedOrderCount ?? 0}`}
              />
              <Chip
                size="small"
                color="info"
                label={`Sin datos: ${summary.noCostDataOrderCount ?? 0}`}
              />
            </>
          )}
        </Stack>
      ) : null}

      {loading && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <ProfitabilityTableHead />
            <TableBody>
              {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                <TableRow key={`skeleton-${index}`}>
                  {Array.from({ length: 10 }).map((__, cellIndex) => (
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

      {!loading && !failed && orders.length === 0 && (
        <Paper sx={{ p: 4 }}>
          <Stack spacing={1} alignItems="center">
            <ReceiptLongOutlinedIcon color="disabled" sx={{ fontSize: 40 }} />
            <Typography variant="h6">Sin pedidos para evaluar</Typography>
            <Typography color="text.secondary" align="center">
              No hay órdenes confirmadas, en producción o entregadas con
              información de rentabilidad.
            </Typography>
          </Stack>
        </Paper>
      )}

      {!loading && !failed && orders.length > 0 && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <ProfitabilityTableHead />
            <TableBody>
              {orders.map((order) => {
                const statusChip = getOrderProfitabilityStatusChipProps(
                  order.profitabilityStatus
                )
                return (
                  <TableRow
                    key={order.orderId}
                    hover
                    sx={{ cursor: 'pointer' }}
                    onClick={() =>
                      navigate(`/commercial/orders/${order.orderId}/profitability`)
                    }
                  >
                    <TableCell>
                      <Stack spacing={0.25}>
                        <Typography variant="body2">{formatOrderLabel(order)}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          Entrega: {formatDisplayDate(order.promisedDeliveryDate) || '—'}
                        </Typography>
                      </Stack>
                    </TableCell>
                    <TableCell>{order.customerName || '—'}</TableCell>
                    <TableCell align="right">
                      {formatProfitabilityMoney(order.orderValue)}
                    </TableCell>
                    <TableCell align="right">
                      {formatMaterialProductionCost(order)}
                    </TableCell>
                    <TableCell align="right">
                      {formatLaborProductionCost(order)}
                    </TableCell>
                    <TableCell align="right">
                      {formatPlotterProductionCost(order)}
                    </TableCell>
                    <TableCell align="right">
                      {formatProfitabilityResultMoney(
                        order.totalDirectCost,
                        order.profitabilityStatus
                      )}
                    </TableCell>
                    <TableCell align="right">
                      {formatProfitabilityResultMoney(
                        order.directProfit,
                        order.profitabilityStatus
                      )}
                    </TableCell>
                    <TableCell align="right">
                      {formatProfitabilityResultMargin(
                        order.directMarginPercentage,
                        order.profitabilityStatus
                      )}
                    </TableCell>
                    <TableCell align="center">
                      <Chip size="small" {...statusChip} />
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

export default OrderProfitabilityPage
