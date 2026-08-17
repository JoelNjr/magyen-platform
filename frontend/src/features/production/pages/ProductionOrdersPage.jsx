import { useEffect, useState } from 'react'
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
  Typography,
} from '@mui/material'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import { resolveProductionBusinessLabel } from '../presentation/resolveProductionBusinessLabel'
import {
  getProductionOrderStatusChipProps,
  getProductionPriorityChipProps,
} from '../presentation/productionStatusPresentation'
import { getProductionOrders } from '../services/productionService'

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
  const [productionOrders, setProductionOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    getProductionOrders()
      .then((data) => {
        setProductionOrders(data.productionOrders ?? [])
        setLoading(false)
      })
      .catch(() => {
        setFailed(true)
        setLoading(false)
      })
  }, [])

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        justifyContent="space-between"
        alignItems={{ xs: 'stretch', sm: 'center' }}
      >
        <Typography variant="h3">Órdenes de producción</Typography>
        <Button
          type="button"
          variant="outlined"
          onClick={() => navigate('/production/operators')}
          sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
        >
          Operarios
        </Button>
      </Stack>

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
        <Paper sx={{ p: { xs: 3, sm: 4 } }}>
          <Stack spacing={2} alignItems="center" sx={{ py: 2 }}>
            <PrecisionManufacturingOutlinedIcon
              color="action"
              sx={{ fontSize: 48 }}
            />
            <Typography variant="h6">
              No hay órdenes de producción registradas.
            </Typography>
            <Typography color="text.secondary" textAlign="center">
              Las órdenes de producción aparecerán aquí cuando existan en el
              sistema.
            </Typography>
          </Stack>
        </Paper>
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
