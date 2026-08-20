import { useEffect, useMemo, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
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
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import { formatQuotationNumber } from '../presentation/formatQuotationNumber'
import {
  buildCustomerNameMap,
  resolveCustomerName,
} from '../presentation/resolveCustomerName'
import { getCustomers, getQuotations } from '../services/commercialService'
import MonthPeriodNavigator from '../../../shared/period/MonthPeriodNavigator'
import { formatMonthPeriodLabel, getCalendarMonthRange } from '../../../shared/period/monthPeriod'
import PageHeader from '../../../layout/PageHeader'

const currencyFormatter = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})

function formatCurrency(amount) {
  return currencyFormatter.format(amount)
}

function getStatusChipProps(status) {
  switch (status) {
    case 'DRAFT':
      return { label: 'Borrador', color: 'default' }
    case 'APPROVED':
      return { label: 'Aprobada', color: 'success' }
    case 'PENDING':
      return { label: 'Pendiente', color: 'warning' }
    case 'REJECTED':
      return { label: 'Rechazada', color: 'error' }
    default:
      return { label: status, color: 'default' }
  }
}

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

function QuotationsTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Número</TableCell>
        <TableCell sx={headerCellSx}>Cliente</TableCell>
        <TableCell sx={headerCellSx}>Fecha creación</TableCell>
        <TableCell sx={headerCellSx}>Fecha entrega</TableCell>
        <TableCell align="center" sx={headerCellSx}>
          Estado
        </TableCell>
        <TableCell sx={headerCellSx}>Vendedor</TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Total
        </TableCell>
      </TableRow>
    </TableHead>
  )
}

function QuotationsPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const initialPeriod = useMemo(() => getCalendarMonthRange(), [])
  const [period, setPeriod] = useState(initialPeriod)
  const [quotations, setQuotations] = useState([])
  const [customerNameById, setCustomerNameById] = useState({})
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [successOpen, setSuccessOpen] = useState(false)

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    getQuotations({ fromDate: period.fromDate, toDate: period.toDate })
      .then((data) => {
        setQuotations(data.quotations)
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

  useEffect(() => {
    if (!location.state?.created) {
      return
    }

    setSuccessOpen(true)
    navigate(location.pathname, { replace: true, state: {} })
  }, [location.state, location.pathname, navigate])

  return (
    <>
      <Stack spacing={3}>
        <PageHeader
          title="Cotizaciones"
          actions={
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={1.5}
            sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
          >
            <Button
              variant="outlined"
              onClick={() => navigate('/commercial/orders')}
            >
              Órdenes
            </Button>
            <Button
              variant="outlined"
              onClick={() => navigate('/commercial/customers')}
            >
              Clientes
            </Button>
            <Button
              variant="outlined"
              onClick={() => navigate('/commercial/sellers')}
            >
              Vendedores
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => navigate('/commercial/new')}
            >
              Nueva cotización
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
              <QuotationsTableHead />
              <TableBody>
                {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                  <TableRow key={`quotation-skeleton-${index}`}>
                    <TableCell>
                      <Skeleton width={80} />
                    </TableCell>
                    <TableCell>
                      <Skeleton width="80%" />
                    </TableCell>
                    <TableCell>
                      <Skeleton width={100} />
                    </TableCell>
                    <TableCell>
                      <Skeleton width={100} />
                    </TableCell>
                    <TableCell align="center">
                      <Skeleton
                        width={80}
                        height={28}
                        sx={{ mx: 'auto', borderRadius: 4 }}
                      />
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
          <Typography>No fue posible obtener las cotizaciones.</Typography>
        )}

        {!loading && !failed && quotations.length === 0 && (
          <Paper sx={{ p: { xs: 3, sm: 4 } }}>
            <Stack spacing={2} alignItems="center" sx={{ py: 2 }}>
              <Inventory2OutlinedIcon color="action" sx={{ fontSize: 48 }} />
              <Typography variant="h6">
                No hay cotizaciones en {formatMonthPeriodLabel(period.fromDate)}
              </Typography>
              <Typography color="text.secondary" textAlign="center">
                Cambia de mes para ver el histórico o crea una nueva cotización.
              </Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => navigate('/commercial/new')}
              >
                Nueva cotización
              </Button>
            </Stack>
          </Paper>
        )}

        {!loading && !failed && quotations.length > 0 && (
          <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
            <Table>
              <QuotationsTableHead />
              <TableBody>
                {quotations.map((quotation) => {
                  const statusChip = getStatusChipProps(quotation.status)

                  return (
                    <TableRow key={quotation.quotationId} hover>
                      <TableCell>
                        <RouterLink
                          to={`/commercial/quotations/${quotation.quotationId}`}
                        >
                          {formatQuotationNumber(quotation.quotationNumber)}
                        </RouterLink>
                      </TableCell>
                      <TableCell>
                        {resolveCustomerName(
                          quotation.customerId,
                          customerNameById
                        )}
                      </TableCell>
                      <TableCell>
                        {formatDisplayDate(quotation.creationDate)}
                      </TableCell>
                      <TableCell>
                        {formatDisplayDate(quotation.deliveryDate)}
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          label={statusChip.label}
                          color={statusChip.color}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{quotation.sellerName || '—'}</TableCell>
                      <TableCell align="right">
                        {formatCurrency(quotation.totalAmount)}
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Stack>

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
          Cotización creada correctamente.
        </Alert>
      </Snackbar>
    </>
  )
}

export default QuotationsPage
