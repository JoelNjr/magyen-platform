import { Fragment, useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Chip,
  Collapse,
  IconButton,
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
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown'
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../../../layout/PageHeader'
import EmptyState from '../../home/components/EmptyState'
import {
  formatPlotterDate,
  formatPlotterMoney,
  formatPlotterPendingMonthLabel,
  formatPlotterStatusLabel,
} from '../presentation/plotterJobPresentation'
import { getPlotterPendingBalances } from '../services/plotterService'

const headerCellSx = { fontWeight: 'bold' }

function PlotterPendingBalancesPage() {
  const navigate = useNavigate()
  const [balances, setBalances] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [expandedMonthKey, setExpandedMonthKey] = useState('')
  const [expandedCustomerKey, setExpandedCustomerKey] = useState('')

  useEffect(() => {
    let active = true
    setLoading(true)
    setFailed(false)
    getPlotterPendingBalances()
      .then((data) => {
        if (active) {
          setBalances(data)
          const firstMonth = Array.isArray(data?.months) ? data.months[0] : null
          setExpandedMonthKey(firstMonth ? `${firstMonth.year}-${firstMonth.month}` : '')
          setLoading(false)
        }
      })
      .catch(() => {
        if (active) {
          setBalances(null)
          setFailed(true)
          setLoading(false)
        }
      })
    return () => {
      active = false
    }
  }, [])

  const months = Array.isArray(balances?.months) ? balances.months : []
  const negativeBalances = Array.isArray(balances?.negativeBalances)
    ? balances.negativeBalances
    : []

  return (
    <Stack spacing={3}>
      <Button
        variant="outlined"
        onClick={() => navigate('/plotter')}
        sx={{ alignSelf: 'flex-start' }}
      >
        Volver a trabajos
      </Button>

      <PageHeader title="Saldos pendientes" />

      <Alert severity="info">
        Saldo pendiente de trabajos externos de Plotter, agrupado por la fecha
        del trabajo. No depende del mes seleccionado en el listado. Los trabajos
        internos, la merma y los trabajos ya pagados no aparecen como deuda.
      </Alert>

      {loading && <Skeleton variant="rounded" height={180} />}

      {!loading && failed && (
        <Alert severity="error">No fue posible cargar los saldos pendientes.</Alert>
      )}

      {!loading && !failed && balances && (
        <>
          <Paper sx={{ p: 2 }}>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={3}
              sx={{ justifyContent: 'space-between' }}
            >
              <SummaryItem label="Trabajos abiertos" value={balances.openJobCount} />
              <SummaryItem label="Clientes" value={balances.customerCount} />
              <SummaryItem
                label="Facturado"
                value={formatPlotterMoney(balances.externalBilledAmount)}
              />
              <SummaryItem
                label="Pagado"
                value={formatPlotterMoney(balances.externalPaidAmount)}
              />
              <SummaryItem
                label="Pendiente"
                value={formatPlotterMoney(balances.outstandingAmount)}
              />
            </Stack>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
              Facturado y pagado incluyen todos los trabajos externos. Pendiente
              suma solo los saldos mayores que cero.
            </Typography>
          </Paper>

          {months.length === 0 ? (
            <EmptyState
              title="No hay saldos pendientes"
              message="Los trabajos externos están saldados o todavía no hay ventas de Plotter."
            />
          ) : (
            <Stack spacing={2}>
              {months.map((month) => {
                const monthKey = `${month.year}-${month.month}`
                const monthExpanded = expandedMonthKey === monthKey
                return (
                  <Paper key={monthKey} sx={{ overflow: 'hidden' }}>
                    <Stack
                      direction="row"
                      spacing={1}
                      sx={{ alignItems: 'center', px: 1, py: 1 }}
                    >
                      <IconButton
                        size="small"
                        aria-label={monthExpanded ? 'Ocultar mes' : 'Ver mes'}
                        onClick={() => {
                          setExpandedMonthKey(monthExpanded ? '' : monthKey)
                          setExpandedCustomerKey('')
                        }}
                      >
                        {monthExpanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                      </IconButton>
                      <Typography variant="h6" sx={{ flexGrow: 1 }}>
                        {formatPlotterPendingMonthLabel(month.year, month.month)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Total pendiente del mes {formatPlotterMoney(month.outstandingAmount)}
                      </Typography>
                    </Stack>
                    <Collapse in={monthExpanded} timeout="auto" unmountOnExit>
                      <TableContainer sx={{ overflowX: 'auto' }}>
                        <Table size="small">
                          <TableHead>
                            <TableRow>
                              <TableCell sx={headerCellSx} />
                              <TableCell sx={headerCellSx}>Cliente</TableCell>
                              <TableCell align="right" sx={headerCellSx}>
                                Trabajos abiertos
                              </TableCell>
                              <TableCell align="right" sx={headerCellSx}>
                                Facturado
                              </TableCell>
                              <TableCell align="right" sx={headerCellSx}>
                                Pagado
                              </TableCell>
                              <TableCell align="right" sx={headerCellSx}>
                                Pendiente
                              </TableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {(month.customers || []).map((customer) => {
                              const customerKey = `${monthKey}-${customer.customerId}`
                              const expanded = expandedCustomerKey === customerKey
                              return (
                                <Fragment key={customerKey}>
                                  <TableRow hover>
                                    <TableCell>
                                      <IconButton
                                        size="small"
                                        aria-label={expanded ? 'Ocultar trabajos' : 'Ver trabajos'}
                                        onClick={() =>
                                          setExpandedCustomerKey(expanded ? '' : customerKey)
                                        }
                                      >
                                        {expanded ? (
                                          <KeyboardArrowUpIcon />
                                        ) : (
                                          <KeyboardArrowDownIcon />
                                        )}
                                      </IconButton>
                                    </TableCell>
                                    <TableCell>{customer.customerName || '—'}</TableCell>
                                    <TableCell align="right">{customer.openJobCount}</TableCell>
                                    <TableCell align="right">
                                      {formatPlotterMoney(customer.billedAmount)}
                                    </TableCell>
                                    <TableCell align="right">
                                      {formatPlotterMoney(customer.paidAmount)}
                                    </TableCell>
                                    <TableCell align="right">
                                      {formatPlotterMoney(customer.outstandingAmount)}
                                    </TableCell>
                                  </TableRow>
                                  <TableRow>
                                    <TableCell
                                      colSpan={6}
                                      sx={{ py: 0, borderBottom: expanded ? undefined : 0 }}
                                    >
                                      <Collapse in={expanded} timeout="auto" unmountOnExit>
                                        <Stack spacing={1} sx={{ py: 2, pl: 2 }}>
                                          <Typography variant="subtitle2">
                                            {customer.customerName} · Pendiente{' '}
                                            {formatPlotterMoney(customer.outstandingAmount)}
                                          </Typography>
                                          <Table size="small">
                                            <TableHead>
                                              <TableRow>
                                                <TableCell>Trabajo</TableCell>
                                                <TableCell>Fecha</TableCell>
                                                <TableCell align="right">Total</TableCell>
                                                <TableCell align="right">Pagado</TableCell>
                                                <TableCell align="right">Pendiente</TableCell>
                                                <TableCell>Estado</TableCell>
                                              </TableRow>
                                            </TableHead>
                                            <TableBody>
                                              {(customer.jobs || []).map((job) => (
                                                <TableRow key={job.plotterJobId}>
                                                  <TableCell>
                                                    <Button
                                                      size="small"
                                                      onClick={() =>
                                                        navigate(`/plotter/jobs/${job.plotterJobId}`)
                                                      }
                                                    >
                                                      {job.plotterJobId}
                                                    </Button>
                                                  </TableCell>
                                                  <TableCell>
                                                    {formatPlotterDate(job.creationDate)}
                                                  </TableCell>
                                                  <TableCell align="right">
                                                    {formatPlotterMoney(job.totalAmount)}
                                                  </TableCell>
                                                  <TableCell align="right">
                                                    {formatPlotterMoney(job.paidAmount)}
                                                  </TableCell>
                                                  <TableCell align="right">
                                                    {formatPlotterMoney(job.outstandingAmount)}
                                                  </TableCell>
                                                  <TableCell>
                                                    <Chip
                                                      size="small"
                                                      label={formatPlotterStatusLabel(job.status)}
                                                    />
                                                  </TableCell>
                                                </TableRow>
                                              ))}
                                            </TableBody>
                                          </Table>
                                        </Stack>
                                      </Collapse>
                                    </TableCell>
                                  </TableRow>
                                </Fragment>
                              )
                            })}
                            <TableRow>
                              <TableCell />
                              <TableCell sx={headerCellSx}>Total pendiente del mes</TableCell>
                              <TableCell align="right" sx={headerCellSx}>
                                {month.openJobCount}
                              </TableCell>
                              <TableCell align="right" sx={headerCellSx}>
                                {formatPlotterMoney(month.billedAmount)}
                              </TableCell>
                              <TableCell align="right" sx={headerCellSx}>
                                {formatPlotterMoney(month.paidAmount)}
                              </TableCell>
                              <TableCell align="right" sx={headerCellSx}>
                                {formatPlotterMoney(month.outstandingAmount)}
                              </TableCell>
                            </TableRow>
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </Collapse>
                  </Paper>
                )
              })}
            </Stack>
          )}

          {negativeBalances.length > 0 && (
            <Stack spacing={1}>
              <Typography variant="h6">Saldos negativos</Typography>
              <Alert severity="warning">
                Estos trabajos tienen pagos mayores que el total. No se suman
                como deuda y no se modifican automáticamente.
              </Alert>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Cliente</TableCell>
                      <TableCell>Trabajo</TableCell>
                      <TableCell>Fecha</TableCell>
                      <TableCell align="right">Total</TableCell>
                      <TableCell align="right">Pagado</TableCell>
                      <TableCell align="right">Diferencia</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {negativeBalances.map((item) => (
                      <TableRow key={item.plotterJobId}>
                        <TableCell>{item.customerName || '—'}</TableCell>
                        <TableCell>{item.plotterJobId}</TableCell>
                        <TableCell>{formatPlotterDate(item.creationDate)}</TableCell>
                        <TableCell align="right">{formatPlotterMoney(item.totalAmount)}</TableCell>
                        <TableCell align="right">{formatPlotterMoney(item.paidAmount)}</TableCell>
                        <TableCell align="right">
                          {formatPlotterMoney(item.outstandingAmount)}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Stack>
          )}
        </>
      )}
    </Stack>
  )
}

function SummaryItem({ label, value }) {
  return (
    <Stack spacing={0.5}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="h6">{value}</Typography>
    </Stack>
  )
}

export default PlotterPendingBalancesPage
