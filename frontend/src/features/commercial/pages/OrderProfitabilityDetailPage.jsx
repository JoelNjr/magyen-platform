import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Chip,
  Divider,
  Paper,
  Skeleton,
  Stack,
  Typography,
} from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'
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
import { getOrderProfitability } from '../services/commercialService'

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function ResultRow({ label, value, emphasize = false }) {
  return (
    <Stack direction="row" justifyContent="space-between" spacing={2}>
      <Typography color="text.secondary">{label}</Typography>
      <Typography sx={{ fontWeight: emphasize ? 700 : 500 }}>{value}</Typography>
    </Stack>
  )
}

function OrderProfitabilityDetailPage() {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const [profitability, setProfitability] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  function loadProfitability() {
    if (!orderId) {
      return
    }
    setLoading(true)
    setFailed(false)
    setErrorMessage('')

    getOrderProfitability(orderId)
      .then((data) => {
        setProfitability(data)
        setLoading(false)
      })
      .catch((error) => {
        setProfitability(null)
        setFailed(true)
        setErrorMessage(
          resolveApiErrorMessage(error, 'No fue posible cargar la rentabilidad del pedido.')
        )
        setLoading(false)
      })
  }

  useEffect(() => {
    if (!orderId) {
      return
    }

    let cancelled = false
    setLoading(true)
    setFailed(false)
    setErrorMessage('')

    getOrderProfitability(orderId)
      .then((data) => {
        if (cancelled) {
          return
        }
        setProfitability(data)
        setLoading(false)
      })
      .catch((error) => {
        if (cancelled) {
          return
        }
        setProfitability(null)
        setFailed(true)
        setErrorMessage(
          resolveApiErrorMessage(error, 'No fue posible cargar la rentabilidad del pedido.')
        )
        setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [orderId])

  const statusChip = getOrderProfitabilityStatusChipProps(
    profitability?.profitabilityStatus
  )

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        justifyContent="space-between"
        alignItems={{ xs: 'stretch', sm: 'center' }}
      >
        <Stack spacing={0.5}>
          <Typography variant="h3">Rentabilidad del pedido</Typography>
          {loading ? (
            <Skeleton width={280} />
          ) : (
            <Typography variant="body2" color="text.secondary">
              {profitability?.orderNumber || '—'}
              {profitability?.description ? ` — ${profitability.description}` : ''}
            </Typography>
          )}
        </Stack>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
          <Button
            variant="outlined"
            onClick={() => navigate('/commercial/orders/profitability')}
          >
            Volver al listado
          </Button>
          <Button
            variant="outlined"
            onClick={() => navigate(`/commercial/orders/${orderId}`)}
          >
            Ver pedido
          </Button>
        </Stack>
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

      {loading ? (
        <Paper sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Skeleton variant="text" width="40%" />
            <Skeleton variant="rectangular" height={180} />
          </Stack>
        </Paper>
      ) : null}

      {!loading && !failed && profitability ? (
        <Stack spacing={3}>
          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            <Chip size="small" {...statusChip} />
            <Chip
              size="small"
              variant="outlined"
              label={`Cliente: ${profitability.customerName || '—'}`}
            />
            <Chip
              size="small"
              variant="outlined"
              label={`Entrega: ${formatDisplayDate(profitability.promisedDeliveryDate) || '—'}`}
            />
          </Stack>

          <Paper sx={{ p: 3 }}>
            <Stack spacing={2}>
              <Typography variant="h5">Ingresos</Typography>
              <ResultRow
                label="Valor del pedido"
                value={formatProfitabilityMoney(profitability.orderValue)}
                emphasize
              />
            </Stack>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Stack spacing={2}>
              <Stack spacing={0.5}>
                <Typography variant="h5">Costos de producción</Typography>
                <Typography variant="body2" color="text.secondary">
                  Atribución de costo de producción. No es un gasto de Finanzas.
                </Typography>
              </Stack>
              <ResultRow
                label="Materiales"
                value={formatMaterialProductionCost(profitability)}
              />
              <ResultRow
                label="Mano de obra"
                value={formatLaborProductionCost(profitability)}
              />
              <ResultRow
                label="Papel / Plotter"
                value={formatPlotterProductionCost(profitability)}
              />
            </Stack>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Stack spacing={2}>
              <Typography variant="h5">Resultado</Typography>
              <ResultRow
                label="Costo total"
                value={formatProfitabilityResultMoney(
                  profitability.totalDirectCost,
                  profitability.profitabilityStatus
                )}
              />
              <Divider />
              <ResultRow
                label="Ganancia directa"
                value={formatProfitabilityResultMoney(
                  profitability.directProfit,
                  profitability.profitabilityStatus
                )}
                emphasize
              />
              <ResultRow
                label="Margen"
                value={formatProfitabilityResultMargin(
                  profitability.directMarginPercentage,
                  profitability.profitabilityStatus
                )}
                emphasize
              />
            </Stack>
          </Paper>
        </Stack>
      ) : null}
    </Stack>
  )
}

export default OrderProfitabilityDetailPage
