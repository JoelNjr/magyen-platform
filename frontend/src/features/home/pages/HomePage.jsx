import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import AccountBalanceWalletOutlinedIcon from '@mui/icons-material/AccountBalanceWalletOutlined'
import PrecisionManufacturingOutlinedIcon from '@mui/icons-material/PrecisionManufacturingOutlined'
import ReceiptLongOutlinedIcon from '@mui/icons-material/ReceiptLongOutlined'
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined'
import {
  Alert,
  Box,
  Button,
  Chip,
  Link,
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
import EmptyState from '../components/EmptyState'
import MetricCard from '../components/MetricCard'
import SectionHeader from '../components/SectionHeader'
import { getHomeDashboard } from '../services/homeService'
import {
  filterGeneralInventoryAlertItems,
  formatCustomerId,
  formatFinanceDate,
  formatFinanceMoney,
  formatHomeMargin,
  formatHomeStock,
  getCalendarMonthRange,
  getCommitmentUrgencyChipProps,
  getHomeProductionStatusChipProps,
  getObligationTypeLabel,
  getPreviousCalendarMonthRange,
  getProductionPriorityChipProps,
  resolveApiErrorMessage,
  resolveProductionBusinessLabel,
} from '../presentation/homePresentation'

const headerCellSx = { fontWeight: 'bold', whiteSpace: 'nowrap' }
const SKELETON_ROW_COUNT = 3

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

function CounterChip({ label, value, color = 'default', emphasized = false }) {
  return (
    <Chip
      label={`${label}: ${value}`}
      color={color}
      variant={color === 'default' ? 'outlined' : 'filled'}
      size={emphasized ? 'medium' : 'small'}
      sx={emphasized ? { fontWeight: 700 } : undefined}
    />
  )
}

function HomePage() {
  const initialMonth = getCalendarMonthRange()

  const [fromDate, setFromDate] = useState(initialMonth.fromDate)
  const [toDate, setToDate] = useState(initialMonth.toDate)
  const [periodError, setPeriodError] = useState('')

  const [dashboard, setDashboard] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const loadDashboard = useCallback(async (rangeFrom, rangeTo) => {
    setLoading(true)
    setFailed(false)
    setErrorMessage('')
    try {
      const data = await getHomeDashboard({
        fromDate: rangeFrom,
        toDate: rangeTo,
      })
      setDashboard(data)
    } catch (error) {
      setDashboard(null)
      setFailed(true)
      setErrorMessage(
        resolveApiErrorMessage(
          error,
          'No fue posible cargar la información del inicio.'
        )
      )
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadDashboard(initialMonth.fromDate, initialMonth.toDate)
    // Solo carga inicial con el mes calendario actual.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadDashboard])

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
    loadDashboard(nextFrom, nextTo)
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

  const financial = dashboard?.financialSummary
  const receivables = dashboard?.receivables
  const completedReceivables = dashboard?.completedReceivables
  const commitments = dashboard?.commitments
  const inventoryAlerts = dashboard?.inventoryAlerts
  const paperRollAlerts = dashboard?.paperRollAlerts
  const production = dashboard?.productionSummary
  const profitability = dashboard?.profitabilitySummary

  const receivableItems = Array.isArray(receivables?.items) ? receivables.items : []
  const completedReceivableItems = Array.isArray(completedReceivables?.items)
    ? completedReceivables.items
    : []
  const commitmentItems = Array.isArray(commitments?.items) ? commitments.items : []
  const paperItems = Array.isArray(paperRollAlerts?.items) ? paperRollAlerts.items : []
  const productionItems = Array.isArray(production?.items) ? production.items : []

  const generalInventoryItems = useMemo(
    () =>
      filterGeneralInventoryAlertItems(
        inventoryAlerts?.items,
        paperRollAlerts?.items
      ),
    [inventoryAlerts?.items, paperRollAlerts?.items]
  )

  const netEmphasize =
    financial == null
      ? undefined
      : Number(financial.netResult) > 0
        ? 'positive'
        : Number(financial.netResult) < 0
          ? 'negative'
          : undefined

  const directResultEmphasize =
    profitability == null
      ? undefined
      : Number(profitability.totalDirectProfit) > 0
        ? 'positive'
        : Number(profitability.totalDirectProfit) < 0
          ? 'negative'
          : undefined

  return (
    <Stack spacing={4}>
      <Box>
        <Typography variant="h3" component="h1" fontWeight={700} gutterBottom>
          Inicio
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Prioridad operativa: producción, rentabilidad y obligaciones primero.
          El período financiero solo afecta el resumen de ingresos y gastos.
        </Typography>
      </Box>

      {failed ? (
        <Alert
          severity="error"
          action={
            <Button color="inherit" size="small" onClick={() => applyPeriod(fromDate, toDate)}>
              Reintentar
            </Button>
          }
        >
          {errorMessage || 'No fue posible cargar la información del inicio.'}
        </Alert>
      ) : null}

      {/* 1 — Production (Priority 1) */}
      <Paper
        variant="outlined"
        sx={{
          p: { xs: 2, md: 3 },
          borderColor: 'warning.light',
          bgcolor: (theme) =>
            theme.palette.mode === 'light' ? 'rgba(237, 108, 2, 0.04)' : 'transparent',
        }}
      >
        <Stack spacing={2}>
          <SectionHeader
            title="Producción"
            priority="primary"
            subtitle="Estado operativo actual de las órdenes de producción."
            actions={
              <Button component={RouterLink} to="/production" variant="contained" size="small">
                Ver producción
              </Button>
            }
          />
          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            {loading ? (
              <Skeleton width={320} height={36} />
            ) : failed ? null : (
              <>
                <CounterChip label="Total" value={production?.totalOrders ?? 0} />
                <CounterChip label="Creadas" value={production?.createdCount ?? 0} />
                <CounterChip
                  label="Planificadas"
                  value={production?.plannedCount ?? 0}
                  color="info"
                />
                <CounterChip
                  label="En proceso"
                  value={production?.inProgressCount ?? 0}
                  color="warning"
                  emphasized
                />
                <CounterChip
                  label="Completadas"
                  value={production?.completedCount ?? 0}
                  color="success"
                />
              </>
            )}
          </Stack>
          {loading ? (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableBody>
                  <LoadingRows columns={7} />
                </TableBody>
              </Table>
            </TableContainer>
          ) : failed ? null : productionItems.length === 0 ? (
            <EmptyState
              icon={<PrecisionManufacturingOutlinedIcon color="disabled" fontSize="large" />}
              message="No hay órdenes de producción activas."
            />
          ) : (
            <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={headerCellSx}>Producción</TableCell>
                    <TableCell sx={headerCellSx}>Orden comercial</TableCell>
                    <TableCell sx={headerCellSx}>Descripción</TableCell>
                    <TableCell sx={headerCellSx}>Cliente</TableCell>
                    <TableCell sx={headerCellSx}>Estado</TableCell>
                    <TableCell sx={headerCellSx}>Prioridad</TableCell>
                    <TableCell sx={headerCellSx}>Creación</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {productionItems.map((item) => {
                    const statusChip = getHomeProductionStatusChipProps(item.status)
                    const priorityChip = getProductionPriorityChipProps(item.priority)
                    const inProgress = item.status === 'IN_PROGRESS'
                    return (
                      <TableRow
                        key={item.productionOrderId}
                        hover
                        sx={
                          inProgress
                            ? {
                                bgcolor: (theme) =>
                                  theme.palette.mode === 'light'
                                    ? 'rgba(237, 108, 2, 0.08)'
                                    : 'action.selected',
                              }
                            : undefined
                        }
                      >
                        <TableCell>
                          <Link
                            component={RouterLink}
                            to={`/production/orders/${item.productionOrderId}`}
                            underline="hover"
                            fontWeight={inProgress ? 700 : 400}
                          >
                            {resolveProductionBusinessLabel(item.orderNumber)}
                          </Link>
                        </TableCell>
                        <TableCell>
                          {item.orderId ? (
                            <Link
                              component={RouterLink}
                              to={`/commercial/orders/${item.orderId}`}
                              underline="hover"
                            >
                              {resolveProductionBusinessLabel(item.orderNumber)}
                            </Link>
                          ) : (
                            '—'
                          )}
                        </TableCell>
                        <TableCell>
                          {item.orderDescription || '—'}
                        </TableCell>
                        <TableCell>
                          {resolveProductionBusinessLabel(item.customerName)}
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={statusChip.label}
                            color={statusChip.color}
                            sx={inProgress ? { fontWeight: 700 } : undefined}
                          />
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={priorityChip.label}
                            color={priorityChip.color}
                            variant="outlined"
                          />
                        </TableCell>
                        <TableCell>{formatFinanceDate(item.creationDate)}</TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Stack>
      </Paper>

      {/* 2 — Profitability (Priority 2) */}
      <Stack spacing={2}>
        <SectionHeader
          title="Rentabilidad"
          priority="secondary"
          subtitle="Rentabilidad directa de órdenes (no es ganancia neta)."
          actions={
            <Button component={RouterLink} to="/commercial/orders" size="small">
              Ver órdenes
            </Button>
          }
        />
        {loading ? (
          <Box
            sx={{
              display: 'grid',
              gap: 2,
              gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: 'repeat(4, 1fr)' },
            }}
          >
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={`prof-sk-${index}`} variant="rounded" height={88} />
            ))}
          </Box>
        ) : failed ? null : (profitability?.evaluatedOrderCount ?? 0) === 0 ? (
          <EmptyState
            icon={<ReceiptLongOutlinedIcon color="disabled" fontSize="large" />}
            message="No hay información de rentabilidad disponible."
          />
        ) : (
          <>
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
              <CounterChip
                label="Evaluadas"
                value={profitability.evaluatedOrderCount ?? 0}
              />
              <CounterChip
                label="Completas"
                value={profitability.completeOrderCount ?? 0}
                color="success"
              />
              <CounterChip
                label="Parcialmente valorizadas"
                value={profitability.partiallyUnvaluedOrderCount ?? 0}
                color="warning"
              />
              <CounterChip
                label="Sin datos de costo"
                value={profitability.noCostDataOrderCount ?? 0}
              />
              <CounterChip
                label="Costos sin valorizar"
                value={profitability.unvaluedCostCount ?? 0}
              />
            </Stack>
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
              <MetricCard
                title="Valor de órdenes (completas)"
                value={formatFinanceMoney(profitability.totalOrderValue)}
              />
              <MetricCard
                title="Costo directo total"
                value={formatFinanceMoney(profitability.totalDirectCost)}
              />
              <MetricCard
                title="Resultado directo"
                value={formatFinanceMoney(profitability.totalDirectProfit)}
                emphasize={directResultEmphasize}
              />
              <MetricCard
                title="Margen directo agregado"
                value={formatHomeMargin(profitability.averageMarginPercentage)}
              />
            </Box>
            <Typography variant="caption" color="text.secondary">
              Valores de rentabilidad directa suministrados por el backend. No se
              recalculan en el frontend.
            </Typography>
          </>
        )}
      </Stack>

      {/* 3 — Commitments + 4 — Paper rolls */}
      <Box
        sx={{
          display: 'grid',
          gap: 3,
          gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' },
        }}
      >
        <Stack spacing={2}>
          <SectionHeader
            title="Compromisos pendientes"
            priority="standard"
            subtitle="Obligaciones financieras actuales."
            actions={
              <Button component={RouterLink} to="/finance" size="small">
                Ir a Finanzas
              </Button>
            }
          />
          {!failed ? (
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
              {loading ? (
                <Skeleton width={220} height={32} />
              ) : (
                <>
                  <CounterChip
                    label="Total pendiente"
                    value={formatFinanceMoney(commitments?.totalPendingAmount)}
                  />
                  <CounterChip
                    label="Total vencido"
                    value={formatFinanceMoney(commitments?.totalOverdueAmount)}
                    color="error"
                  />
                  <CounterChip
                    label="Vencidos"
                    value={commitments?.overdueCount ?? 0}
                    color="error"
                  />
                  <CounterChip
                    label="Próximos"
                    value={commitments?.upcomingCount ?? 0}
                    color="info"
                  />
                </>
              )}
            </Stack>
          ) : null}
          {loading ? (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableBody>
                  <LoadingRows columns={5} />
                </TableBody>
              </Table>
            </TableContainer>
          ) : failed ? null : commitmentItems.length === 0 ? (
            <EmptyState
              icon={<AccountBalanceWalletOutlinedIcon color="disabled" fontSize="large" />}
              message="No hay compromisos pendientes."
            />
          ) : (
            <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={headerCellSx}>Nombre</TableCell>
                    <TableCell sx={headerCellSx}>Tipo</TableCell>
                    <TableCell sx={headerCellSx}>Vence</TableCell>
                    <TableCell sx={headerCellSx}>Estado</TableCell>
                    <TableCell sx={headerCellSx} align="right">
                      Monto
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {commitmentItems.map((item) => {
                    const urgency = getCommitmentUrgencyChipProps(item)
                    return (
                      <TableRow key={item.occurrenceId} hover>
                        <TableCell>{item.name || '—'}</TableCell>
                        <TableCell>{getObligationTypeLabel(item.type)}</TableCell>
                        <TableCell>{formatFinanceDate(item.dueDate)}</TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={urgency.label}
                            color={urgency.color}
                          />
                        </TableCell>
                        <TableCell align="right">
                          {formatFinanceMoney(item.expectedAmount)}
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Stack>

        <Stack spacing={2}>
          <SectionHeader
            title="Rollos de papel"
            priority="standard"
            subtitle="Insumo crítico del Plotter."
            actions={
              <Button component={RouterLink} to="/plotter" size="small">
                Ver Plotter
              </Button>
            }
          />
          {!failed && !loading ? (
            <CounterChip
              label="Rollos bajos"
              value={paperRollAlerts?.lowStockCount ?? 0}
              color="warning"
            />
          ) : null}
          {loading ? (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableBody>
                  <LoadingRows columns={3} />
                </TableBody>
              </Table>
            </TableContainer>
          ) : failed ? null : paperItems.length === 0 ? (
            <EmptyState
              icon={<WarningAmberOutlinedIcon color="disabled" fontSize="large" />}
              message="No hay rollos de papel con stock bajo."
            />
          ) : (
            <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={headerCellSx}>RP</TableCell>
                    <TableCell sx={headerCellSx} align="right">
                      Metros
                    </TableCell>
                    <TableCell sx={headerCellSx} align="right">
                      Mínimo
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paperItems.map((item) => (
                    <TableRow key={item.inventoryItemId} hover>
                      <TableCell>
                        <Link
                          component={RouterLink}
                          to={`/inventory/${item.inventoryItemId}`}
                          underline="hover"
                        >
                          {item.paperRollNumber || '—'}
                        </Link>
                      </TableCell>
                      <TableCell align="right">
                        {formatHomeStock(item.stock)} {item.unitOfMeasure || ''}
                      </TableCell>
                      <TableCell align="right">
                        {formatHomeStock(item.minimumStock)} {item.unitOfMeasure || ''}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Stack>
      </Box>

      {/* 5 — Receivables */}
      <Stack spacing={2}>
        <SectionHeader
          title="Cuentas por cobrar"
          priority="standard"
          subtitle="Cobros pendientes actuales (independientes del período financiero)."
          actions={
            <Button component={RouterLink} to="/commercial/orders" size="small">
              Ver órdenes
            </Button>
          }
        />
        {!failed ? (
          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            {loading ? (
              <Skeleton width={180} height={32} />
            ) : (
              <>
                <CounterChip
                  label="Pendiente"
                  value={formatFinanceMoney(receivables?.totalOutstandingAmount)}
                  color="warning"
                />
                <CounterChip
                  label="Órdenes"
                  value={receivables?.orderCount ?? 0}
                />
              </>
            )}
          </Stack>
        ) : null}
        {loading ? (
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableBody>
                <LoadingRows columns={3} />
              </TableBody>
            </Table>
          </TableContainer>
        ) : failed ? null : receivableItems.length === 0 ? (
          <EmptyState
            icon={<ReceiptLongOutlinedIcon color="disabled" fontSize="large" />}
            message="No hay cuentas por cobrar."
          />
        ) : (
          <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={headerCellSx}>Orden</TableCell>
                  <TableCell sx={headerCellSx}>Cliente</TableCell>
                  <TableCell sx={headerCellSx} align="right">
                    Pendiente
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {receivableItems.map((item) => (
                  <TableRow key={item.orderId} hover>
                    <TableCell>
                      <Link
                        component={RouterLink}
                        to={`/commercial/orders/${item.orderId}`}
                        underline="hover"
                      >
                        {item.orderNumber || item.orderId}
                      </Link>
                    </TableCell>
                    <TableCell title={item.customerId}>
                      {formatCustomerId(item.customerId)}
                    </TableCell>
                    <TableCell align="right">
                      {formatFinanceMoney(item.outstandingAmount)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Stack>

      {/* 5b — Completed order receivables */}
      <Stack spacing={2}>
        <SectionHeader
          title="Dinero por cobrar de pedidos completados"
          priority="standard"
          subtitle="Pedidos entregados o cerrados que todavía tienen saldo pendiente."
          actions={
            <Button component={RouterLink} to="/commercial/orders" size="small">
              Ver órdenes
            </Button>
          }
        />
        {!failed ? (
          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            {loading ? (
              <Skeleton width={180} height={32} />
            ) : (
              <>
                <CounterChip
                  label="Pendiente"
                  value={formatFinanceMoney(
                    completedReceivables?.totalOutstandingAmount
                  )}
                  color="warning"
                />
                <CounterChip
                  label="Pedidos"
                  value={completedReceivables?.orderCount ?? 0}
                />
              </>
            )}
          </Stack>
        ) : null}
        {loading ? (
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableBody>
                <LoadingRows columns={4} />
              </TableBody>
            </Table>
          </TableContainer>
        ) : failed ? null : completedReceivableItems.length === 0 ? (
          <EmptyState
            icon={<ReceiptLongOutlinedIcon color="disabled" fontSize="large" />}
            message="No hay pedidos completados con saldo pendiente."
          />
        ) : (
          <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={headerCellSx}>Orden</TableCell>
                  <TableCell sx={headerCellSx}>Cliente</TableCell>
                  <TableCell sx={headerCellSx}>Descripción</TableCell>
                  <TableCell sx={headerCellSx} align="right">
                    Pendiente
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {completedReceivableItems.map((item) => (
                  <TableRow key={item.orderId} hover>
                    <TableCell>
                      <Link
                        component={RouterLink}
                        to={`/commercial/orders/${item.orderId}`}
                        underline="hover"
                      >
                        {item.orderNumber || '—'}
                      </Link>
                    </TableCell>
                    <TableCell>
                      {item.customerName || '—'}
                    </TableCell>
                    <TableCell>{item.description || '—'}</TableCell>
                    <TableCell align="right">
                      {formatFinanceMoney(item.outstandingAmount)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Stack>

      {/* 6 — Financial period */}
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Stack spacing={2}>
          <SectionHeader
            title="Período financiero"
            priority="context"
            subtitle="Aplica únicamente al resumen financiero."
          />
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            alignItems={{ xs: 'stretch', sm: 'center' }}
            flexWrap="wrap"
            useFlexGap
          >
            <TextField
              label="Desde"
              type="date"
              size="small"
              value={fromDate}
              onChange={(event) => setFromDate(event.target.value)}
              InputLabelProps={{ shrink: true }}
              sx={{ minWidth: { xs: '100%', sm: 160 } }}
            />
            <TextField
              label="Hasta"
              type="date"
              size="small"
              value={toDate}
              onChange={(event) => setToDate(event.target.value)}
              InputLabelProps={{ shrink: true }}
              sx={{ minWidth: { xs: '100%', sm: 160 } }}
            />
            <Button variant="contained" onClick={() => applyPeriod(fromDate, toDate)}>
              Aplicar
            </Button>
            <Button onClick={() => handlePeriodPreset('current')}>Mes actual</Button>
            <Button onClick={() => handlePeriodPreset('previous')}>Mes anterior</Button>
          </Stack>
          {periodError ? <Alert severity="warning">{periodError}</Alert> : null}
          {!loading && !failed && dashboard?.fromDate && dashboard?.toDate ? (
            <Typography variant="caption" color="text.secondary">
              Resumen financiero: {formatFinanceDate(dashboard.fromDate)} –{' '}
              {formatFinanceDate(dashboard.toDate)}
            </Typography>
          ) : null}
        </Stack>
      </Paper>

      {/* 7 — Financial summary */}
      <Stack spacing={2}>
        <SectionHeader
          title="Resumen financiero"
          priority="context"
          subtitle="Ingresos, gastos y resultado del período seleccionado."
          actions={
            <Button component={RouterLink} to="/finance" size="small">
              Ir a Finanzas
            </Button>
          }
        />
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
          <MetricCard
            title="Ingresos"
            loading={loading}
            value={formatFinanceMoney(financial?.income)}
          />
          <MetricCard
            title="Gastos"
            loading={loading}
            value={formatFinanceMoney(financial?.expense)}
          />
          <MetricCard
            title="Resultado neto"
            loading={loading}
            value={formatFinanceMoney(financial?.netResult)}
            emphasize={netEmphasize}
          />
          <MetricCard
            title="Movimientos"
            loading={loading}
            value={financial?.transactionCount ?? '—'}
          />
        </Box>
      </Stack>

      {/* 8 — General inventory alerts */}
      <Stack spacing={2}>
        <SectionHeader
          title="Alertas de inventario"
          priority="context"
          subtitle="Materiales generales con stock bajo (los rollos de papel se muestran arriba)."
          actions={
            <Button component={RouterLink} to="/inventory" size="small">
              Ver inventario
            </Button>
          }
        />
        {!failed && !loading ? (
          <CounterChip
            label="Stock bajo"
            value={generalInventoryItems.length}
            color="warning"
          />
        ) : null}
        {loading ? (
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableBody>
                <LoadingRows columns={4} />
              </TableBody>
            </Table>
          </TableContainer>
        ) : failed ? null : generalInventoryItems.length === 0 ? (
          <EmptyState
            icon={<Inventory2OutlinedIcon color="disabled" fontSize="large" />}
            message="No hay materiales con stock bajo."
          />
        ) : (
          <TableContainer component={Paper} variant="outlined" sx={{ overflowX: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={headerCellSx}>Material</TableCell>
                  <TableCell sx={headerCellSx}>Código</TableCell>
                  <TableCell sx={headerCellSx} align="right">
                    Stock
                  </TableCell>
                  <TableCell sx={headerCellSx} align="right">
                    Mínimo
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {generalInventoryItems.map((item) => (
                  <TableRow key={item.inventoryItemId} hover>
                    <TableCell>
                      <Link
                        component={RouterLink}
                        to={`/inventory/${item.inventoryItemId}`}
                        underline="hover"
                      >
                        {item.name || '—'}
                      </Link>
                    </TableCell>
                    <TableCell>{item.materialCode || '—'}</TableCell>
                    <TableCell align="right">
                      {formatHomeStock(item.stock)} {item.unitOfMeasure || ''}
                    </TableCell>
                    <TableCell align="right">
                      {formatHomeStock(item.minimumStock)} {item.unitOfMeasure || ''}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Stack>
    </Stack>
  )
}

export default HomePage
