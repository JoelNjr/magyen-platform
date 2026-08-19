import { useEffect, useMemo, useState } from 'react'
import PrintOutlinedIcon from '@mui/icons-material/PrintOutlined'
import {
  Alert,
  Button,
  MenuItem,
  Paper,
  Skeleton,
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
import { useNavigate } from 'react-router-dom'
import MetricCard from '../../home/components/MetricCard'
import { getCalendarMonthRange } from '../../finance/presentation/financePresentation'
import {
  formatPlotterDate,
  formatPlotterMoney,
  formatPlotterNumber,
  formatPlotterOrderLabel,
} from '../presentation/plotterJobPresentation'
import { getPlotterProfitability } from '../services/plotterService'

const headerCellSx = { fontWeight: 'bold', whiteSpace: 'nowrap' }

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function formatInkCost(summary) {
  return formatPlotterMoney(summary?.inkCost ?? 0)
}

function formatAnalyticalResult(summary) {
  if (!summary || summary.analyticalPlotterResult === null || summary.analyticalPlotterResult === undefined) {
    return '—'
  }
  return formatPlotterMoney(summary.analyticalPlotterResult)
}

function PlotterProfitabilityPage() {
  const navigate = useNavigate()
  const defaultRange = useMemo(() => getCalendarMonthRange(), [])
  const [fromDate, setFromDate] = useState(defaultRange.fromDate)
  const [toDate, setToDate] = useState(defaultRange.toDate)
  const [scope, setScope] = useState('ALL')
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  function loadSummary(nextFromDate = fromDate, nextToDate = toDate, nextScope = scope) {
    setLoading(true)
    setFailed(false)
    setErrorMessage('')

    getPlotterProfitability({
      fromDate: nextFromDate,
      toDate: nextToDate,
      scope: nextScope,
    })
      .then((data) => {
        setSummary(data)
        setLoading(false)
      })
      .catch((error) => {
        setSummary(null)
        setFailed(true)
        setErrorMessage(
          resolveApiErrorMessage(error, 'No fue posible cargar la rentabilidad del Plotter.')
        )
        setLoading(false)
      })
  }

  useEffect(() => {
    loadSummary(defaultRange.fromDate, defaultRange.toDate, 'ALL')
  }, [defaultRange])

  function handleApplyFilters() {
    loadSummary(fromDate, toDate, scope)
  }

  const internalOrders = Array.isArray(summary?.internalOrders)
    ? summary.internalOrders
    : []
  const paperValuationLabel = summary?.paperCostComplete
    ? 'Costos de papel valorizados'
    : 'Rentabilidad parcial — faltan costos por valorar'

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        justifyContent="space-between"
        alignItems={{ xs: 'stretch', sm: 'center' }}
      >
        <Stack spacing={0.5}>
          <Typography variant="h3">Rentabilidad del Plotter</Typography>
          <Typography variant="body2" color="text.secondary">
            Vista analítica. El ingreso interno representa el servicio Plotter Magyen; no
            es una venta a cliente externo. El gasto en papel son las compras de
            inventario del período, no los consumos de producción.
          </Typography>
        </Stack>
        <Button
          variant="outlined"
          onClick={() => navigate('/plotter')}
          sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
        >
          Ver trabajos
        </Button>
      </Stack>

      <Paper sx={{ p: 2 }}>
        <Stack
          direction={{ xs: 'column', md: 'row' }}
          spacing={2}
          alignItems={{ xs: 'stretch', md: 'center' }}
        >
          <TextField
            label="Desde"
            type="date"
            size="small"
            value={fromDate}
            onChange={(event) => setFromDate(event.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            label="Hasta"
            type="date"
            size="small"
            value={toDate}
            onChange={(event) => setToDate(event.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            select
            label="Alcance"
            size="small"
            value={scope}
            onChange={(event) => setScope(event.target.value)}
            sx={{ minWidth: 180 }}
          >
            <MenuItem value="ALL">Todos</MenuItem>
            <MenuItem value="EXTERNAL">Externos</MenuItem>
            <MenuItem value="INTERNAL">Internos</MenuItem>
            <MenuItem value="WASTE">Merma</MenuItem>
          </TextField>
          <Button variant="contained" onClick={handleApplyFilters} disabled={loading}>
            Consultar
          </Button>
        </Stack>
      </Paper>

      {failed ? (
        <Alert
          severity="error"
          action={
            <Button color="inherit" size="small" onClick={handleApplyFilters}>
              Reintentar
            </Button>
          }
        >
          {errorMessage}
        </Alert>
      ) : null}

      <Stack spacing={1}>
        <Typography variant="h5">Rentabilidad</Typography>
        <Typography variant="body2" color="text.secondary">
          Ingreso generado menos compras de papel y tinta del período. No es cobranza.
        </Typography>
      </Stack>

      <BoxGrid>
        <MetricCard
          title="Total de papel impreso"
          value={
            summary
              ? `${formatPlotterNumber(summary.totalPaperPrintedMeters)} m`
              : '—'
          }
          loading={loading}
        />
        <MetricCard
          title="Total generado — Externo"
          value={formatPlotterMoney(summary?.externalRevenue)}
          loading={loading}
        />
        <MetricCard
          title="Total generado — Interno Magyen"
          value={formatPlotterMoney(summary?.internalRevenue)}
          loading={loading}
        />
        <MetricCard
          title="Total generado — Combinado"
          value={formatPlotterMoney(summary?.combinedRevenue)}
          loading={loading}
        />
        <MetricCard
          title="Total gastado en papel"
          value={formatPlotterMoney(summary?.totalPaperCost)}
          loading={loading}
        />
        <MetricCard
          title="Total gastado en tintas"
          value={formatInkCost(summary)}
          loading={loading}
        />
        <MetricCard
          title="Resultado del Plotter"
          value={formatAnalyticalResult(summary)}
          loading={loading}
          emphasize={
            Number(summary?.analyticalPlotterResult) > 0
              ? 'positive'
              : Number(summary?.analyticalPlotterResult) < 0
                ? 'negative'
                : undefined
          }
        />
        <MetricCard
          title="Trabajos internos"
          value={summary?.internalJobCount ?? 0}
          loading={loading}
        />
        <MetricCard
          title="Trabajos externos"
          value={summary?.externalJobCount ?? 0}
          loading={loading}
        />
        <MetricCard
          title="Trabajos de merma"
          value={summary?.wasteJobCount ?? 0}
          loading={loading}
        />
        <MetricCard
          title="Metros de merma"
          value={
            summary
              ? `${formatPlotterNumber(summary.wastePrintedMeters)} m`
              : '—'
          }
          loading={loading}
        />
      </BoxGrid>

      <Stack spacing={1}>
        <Typography variant="h5">Cobranza de trabajos externos</Typography>
        <Typography variant="body2" color="text.secondary">
          Pagos registrados contra trabajos EXTERNAL del período. No crea
          transacciones de Finanzas al abrir este reporte. No incluye interno ni
          merma.
        </Typography>
      </Stack>

      <BoxGrid>
        <MetricCard
          title="Total generado"
          value={formatPlotterMoney(summary?.externalRevenue)}
          loading={loading}
        />
        <MetricCard
          title="Total pagado"
          value={formatPlotterMoney(summary?.externalPaidAmount)}
          loading={loading}
        />
        <MetricCard
          title="Saldo pendiente por cobrar"
          value={formatPlotterMoney(summary?.externalOutstandingAmount)}
          loading={loading}
        />
      </BoxGrid>

      {!failed && !loading && summary ? (
        <Alert severity={summary.paperCostComplete ? 'success' : 'warning'}>
          {paperValuationLabel}. Trabajos externos: {summary.externalJobCount}.
          Trabajos internos Magyen: {summary.internalJobCount}. Trabajos de merma:{' '}
          {summary.wasteJobCount}.
        </Alert>
      ) : null}

      <Paper sx={{ p: 3 }}>
        <Stack spacing={2}>
          <Typography variant="h5">Trabajos internos Magyen</Typography>
          <Typography variant="body2" color="text.secondary">
            Valor del servicio interno Magyen atribuido a pedidos. El gasto en papel del
            período son las compras de inventario, no estos consumos.
          </Typography>

          {loading ? (
            <Skeleton variant="rectangular" height={120} />
          ) : null}

          {!loading && !failed && internalOrders.length === 0 ? (
            <Stack spacing={1} alignItems="center" sx={{ py: 3 }}>
              <PrintOutlinedIcon color="disabled" sx={{ fontSize: 40 }} />
              <Typography color="text.secondary">
                No hay trabajos internos en el período seleccionado.
              </Typography>
            </Stack>
          ) : null}

          {!loading && !failed && internalOrders.length > 0 ? (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={headerCellSx}>Pedido</TableCell>
                    <TableCell sx={headerCellSx}>Cliente</TableCell>
                    <TableCell sx={headerCellSx}>Fecha</TableCell>
                    <TableCell align="right" sx={headerCellSx}>
                      Metros impresos
                    </TableCell>
                    <TableCell align="right" sx={headerCellSx}>
                      Valor del servicio
                    </TableCell>
                    <TableCell align="right" sx={headerCellSx}>
                      Papel físico (histórico)
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {internalOrders.map((item) => (
                    <TableRow key={item.plotterJobId}>
                      <TableCell>{formatPlotterOrderLabel(item)}</TableCell>
                      <TableCell>{item.customerName || '—'}</TableCell>
                      <TableCell>{formatPlotterDate(item.jobDate)}</TableCell>
                      <TableCell align="right">
                        {formatPlotterNumber(item.printedMeters)} m
                      </TableCell>
                      <TableCell align="right">
                        {formatPlotterMoney(item.serviceValue)}
                      </TableCell>
                      <TableCell align="right">
                        {item.paperCostValued
                          ? formatPlotterMoney(item.paperCost)
                          : 'Sin valorar'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : null}
        </Stack>
      </Paper>
    </Stack>
  )
}

function BoxGrid({ children }) {
  return (
    <Stack
      sx={{
        display: 'grid',
        gap: 2,
        gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: 'repeat(3, 1fr)' },
      }}
    >
      {children}
    </Stack>
  )
}

export default PlotterProfitabilityPage
