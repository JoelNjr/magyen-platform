import { useEffect, useMemo, useState } from 'react'
import PrecisionManufacturingOutlinedIcon from '@mui/icons-material/PrecisionManufacturingOutlined'
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
import { resolveProductionBusinessLabel } from '../presentation/resolveProductionBusinessLabel'
import {
  getProductionOrderStatusChipProps,
  getProductionPriorityChipProps,
} from '../presentation/productionStatusPresentation'
import { getProductionOrders } from '../services/productionService'
import PageHeader from '../../../layout/PageHeader'
import EmptyState from '../../home/components/EmptyState'
import MonthPeriodNavigator from '../../../shared/period/MonthPeriodNavigator'
import { formatMonthPeriodLabel, getCalendarMonthRange } from '../../../shared/period/monthPeriod'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

function ProductionOrdersTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Orden de producción</TableCell>
        <TableCell sx={headerCellSx}>Orden comercial</TableCell>
        <TableCell sx={headerCellSx}>Descripción</TableCell>
        <TableCell sx={headerCellSx}>Cliente</TableCell>
        <TableCell sx={headerCellSx}>Fecha creación</TableCell>
        <TableCell align="center" sx={headerCellSx}>
          Estado
        </TableCell>
        <TableCell align="center" sx={headerCellSx}>
          Prioridad
        </TableCell>
        <TableCell sx={headerCellSx}>Inicio planificado</TableCell>
        <TableCell sx={headerCellSx}>Fin planificado</TableCell>
        <TableCell sx={headerCellSx}>Observaciones</TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Acciones
        </TableCell>
      </TableRow>
    </TableHead>
  )
}

function ProductionOrdersPage() {
  const navigate = useNavigate()
  const initialPeriod = useMemo(() => getCalendarMonthRange(), [])
  const [period, setPeriod] = useState(initialPeriod)
  const [productionOrders, setProductionOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    getProductionOrders({ fromDate: period.fromDate, toDate: period.toDate })
      .then((data) => {
        setProductionOrders(data.productionOrders ?? [])
        setLoading(false)
      })
      .catch(() => {
        setFailed(true)
        setLoading(false)
      })
  }, [period.fromDate, period.toDate])

  return (
    <Stack spacing={3}>
        <PageHeader title="Órdenes de producción" />

      <MonthPeriodNavigator
        fromDate={period.fromDate}
        disabled={loading}
        onPeriodChange={setPeriod}
      />

      {loading && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <ProductionOrdersTableHead />
            <TableBody>
              {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                <TableRow key={`production-order-skeleton-${index}`}>
                  <TableCell>
                    <Skeleton width={180} />
                  </TableCell>
                  <TableCell>
                    <Skeleton width={180} />
                  </TableCell>
                  <TableCell>
                    <Skeleton width={180} />
                  </TableCell>
                  <TableCell>
                    <Skeleton width={140} />
                  </TableCell>
                  <TableCell>
                    <Skeleton width={100} />
                  </TableCell>
                  <TableCell align="center">
                    <Skeleton
                      width={90}
                      height={28}
                      sx={{ mx: 'auto', borderRadius: 4 }}
                    />
                  </TableCell>
                  <TableCell align="center">
                    <Skeleton
                      width={70}
                      height={28}
                      sx={{ mx: 'auto', borderRadius: 4 }}
                    />
                  </TableCell>
                  <TableCell>
                    <Skeleton width={100} />
                  </TableCell>
                  <TableCell>
                    <Skeleton width={100} />
                  </TableCell>
                  <TableCell>
                    <Skeleton width="80%" />
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
          No fue posible obtener las órdenes de producción.
        </Alert>
      )}

      {!loading && !failed && productionOrders.length === 0 && (
        <EmptyState
          icon={
            <PrecisionManufacturingOutlinedIcon color="action" sx={{ fontSize: 48 }} />
          }
          title={`No hay órdenes de producción en ${formatMonthPeriodLabel(period.fromDate)}.`}
          message="Cambia de mes para ver el histórico."
        />
      )}

      {!loading && !failed && productionOrders.length > 0 && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <ProductionOrdersTableHead />
            <TableBody>
              {productionOrders.map((productionOrder) => {
                const statusChip = getProductionOrderStatusChipProps(
                  productionOrder.status
                )
                const priorityChip = getProductionPriorityChipProps(
                  productionOrder.priority
                )
                const detailPath = `/production/orders/${productionOrder.productionOrderId}`

                return (
                  <TableRow key={productionOrder.productionOrderId} hover>
                    <TableCell>
                      <RouterLink to={detailPath}>
                        {resolveProductionBusinessLabel(productionOrder.orderNumber)}
                      </RouterLink>
                    </TableCell>
                    <TableCell>
                      {productionOrder.orderId ? (
                        <RouterLink
                          to={`/commercial/orders/${productionOrder.orderId}`}
                        >
                          {resolveProductionBusinessLabel(productionOrder.orderNumber)}
                        </RouterLink>
                        ) : (
                        '—'
                      )}
                    </TableCell>
                    <TableCell>
                      {productionOrder.orderDescription || '—'}
                    </TableCell>
                    <TableCell>
                      {resolveProductionBusinessLabel(productionOrder.customerName)}
                    </TableCell>
                    <TableCell>
                      {formatDisplayDate(productionOrder.creationDate)}
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={statusChip.label}
                        color={statusChip.color}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={priorityChip.label}
                        color={priorityChip.color}
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      {formatDisplayDate(productionOrder.plannedStartDate)}
                    </TableCell>
                    <TableCell>
                      {formatDisplayDate(productionOrder.plannedEndDate)}
                    </TableCell>
                    <TableCell>
                      {productionOrder.observations || '—'}
                    </TableCell>
                    <TableCell align="right">
                      <Button
                        size="small"
                        variant="outlined"
                        onClick={() => navigate(detailPath)}
                      >
                        Ver detalle
                      </Button>
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

export default ProductionOrdersPage
