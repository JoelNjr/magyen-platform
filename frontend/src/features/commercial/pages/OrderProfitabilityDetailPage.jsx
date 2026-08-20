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
  formatPlotterPhysicalPaperCost,
  formatPlotterProductionCost,
  formatProfitabilityMoney,
  formatProfitabilityResultMargin,
  formatProfitabilityResultMoney,
  getOrderProfitabilityStatusChipProps,
} from '../presentation/orderProfitabilityPresentation'
import { getOrderProfitability } from '../services/commercialService'
import PageHeader from '../../../layout/PageHeader'

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function ResultRow({ label, value, emphasize = false, tone }) {
  const valueColor =
    tone === 'positive'
      ? 'success.main'
      : tone === 'negative'
        ? 'error.main'
        : tone === 'revenue'
          ? 'success.main'
          : tone === 'cost'
            ? 'text.primary'
            : 'text.primary'

  return (
    <Stack direction="row" justifyContent="space-between" spacing={2}>
      <Typography color="text.secondary">{label}</Typography>
      <Typography
        sx={{
          fontWeight: emphasize ? 700 : 500,
          color: valueColor,
          textAlign: 'right',
        }}
      >
        {value}
      </Typography>
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
      <PageHeader
        title="Rentabilidad del pedido"
        subtitle={
          loading ? (
            <Skeleton width={280} />
          ) : (
            `${profitability?.orderNumber || '—'}${
              profitability?.description ? ` — ${profitability.description}` : ''
            }`
          )
        }
        actions={
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
        }
      />

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

          <Paper sx={{ p: 3, borderTop: 3, borderColor: 'success.main' }}>
            <Stack spacing={2}>
              <Typography variant="h5">Ingresos</Typography>
              <ResultRow
                label="Valor del pedido"
                value={formatProfitabilityMoney(profitability.orderValue)}
                emphasize
                tone="revenue"
              />
            </Stack>
          </Paper>

          <Paper sx={{ p: 3, borderTop: 3, borderColor: 'secondary.main' }}>
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
                tone="cost"
              />
              <ResultRow
                label="Mano de obra"
                value={formatLaborProductionCost(profitability)}
                tone="cost"
              />
              <ResultRow
                label="Servicio Plotter interno"
                value={formatPlotterProductionCost(profitability)}
                tone="cost"
              />
              <ResultRow
                label="Papel físico (histórico, no se suma otra vez)"
                value={formatPlotterPhysicalPaperCost(profitability)}
                tone="cost"
              />
            </Stack>
          </Paper>

          <Paper
            sx={{
              p: 3,
              borderTop: 3,
              borderColor:
                Number(profitability.directProfit) > 0
                  ? 'success.main'
                  : Number(profitability.directProfit) < 0
                    ? 'error.main'
                    : 'warning.main',
            }}
          >
            <Stack spacing={2}>
              <Typography variant="h5">Resultado</Typography>
              <ResultRow
                label="Costo total"
                value={formatProfitabilityResultMoney(
                  profitability.totalDirectCost,
                  profitability.profitabilityStatus
                )}
                tone="cost"
              />
              <Divider />
              <ResultRow
                label="Ganancia directa"
                value={formatProfitabilityResultMoney(
                  profitability.directProfit,
                  profitability.profitabilityStatus
                )}
                emphasize
                tone={
                  Number(profitability.directProfit) > 0
                    ? 'positive'
                    : Number(profitability.directProfit) < 0
                      ? 'negative'
                      : undefined
                }
              />
              <ResultRow
                label="Margen"
                value={formatProfitabilityResultMargin(
                  profitability.directMarginPercentage,
                  profitability.profitabilityStatus
                )}
                emphasize
                tone={
                  Number(profitability.directProfit) > 0
                    ? 'positive'
                    : Number(profitability.directProfit) < 0
                      ? 'negative'
                      : undefined
                }
              />
            </Stack>
          </Paper>
        </Stack>
      ) : null}
    </Stack>
  )
}

export default OrderProfitabilityDetailPage
