import { useEffect, useState } from 'react'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import {
  Alert,
  Button,
  Chip,
  Divider,
  Grid,
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
import { Link as RouterLink, useLocation, useNavigate, useParams } from 'react-router-dom'
import CreateProductionOrderDialog from '../components/CreateProductionOrderDialog'
import ManageOrderItemProductSpecificationDialog from '../components/ManageOrderItemProductSpecificationDialog'
import ManageOrderItemSizesDialog from '../components/ManageOrderItemSizesDialog'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import { getOrderStatusChipProps } from '../presentation/orderStatusPresentation'
import {
  buildCustomerNameMap,
  resolveCustomerName,
} from '../presentation/resolveCustomerName'
import {
  getCustomers,
  getOrder,
  getOrderProfitability,
} from '../services/commercialService'
import {
  createProductionOrderFromOrder,
  getProductionOrders,
} from '../../production/services/productionService'

const currencyFormatter = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})

function formatCurrency(amount) {
  if (amount === null || amount === undefined || Number.isNaN(Number(amount))) {
    return '—'
  }
  return currencyFormatter.format(amount)
}

function formatMarginPercentage(value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return '—'
  }
  return `${amount.toLocaleString('es-CO', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })} %`
}

function getProfitabilityStatusLabel(status) {
  if (status === 'COMPLETE') return 'Costos directos completos'
  if (status === 'PARTIALLY_UNVALUED') {
    return 'Hay consumos de materiales sin costo configurado'
  }
  if (status === 'NO_COST_DATA') return 'Sin costos registrados'
  return status || '—'
}

function getProfitabilityStatusSeverity(status) {
  if (status === 'COMPLETE') return 'success'
  if (status === 'PARTIALLY_UNVALUED') return 'warning'
  if (status === 'NO_COST_DATA') return 'info'
  return 'info'
}

function formatAcknowledgment(value) {
  return value ? 'Sí' : 'No'
}

function formatYesNo(value) {
  return value ? 'Sí' : 'No'
}

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}

function findLinkedProductionOrderId(productionOrdersPayload, commercialOrderId) {
  const productionOrders = Array.isArray(productionOrdersPayload?.productionOrders)
    ? productionOrdersPayload.productionOrders
    : []

  const linkedProductionOrder = productionOrders.find(
    (productionOrder) => productionOrder.orderId === commercialOrderId
  )

  return linkedProductionOrder?.productionOrderId ?? null
}

function isDuplicateProductionOrderError(error) {
  const status = error?.response?.status
  const message = String(error?.response?.data?.message || '').toLowerCase()

  return (
    status === 409 ||
    (status === 400 &&
      (message.includes('already exists') ||
        message.includes('ya existe una orden de producción')))
  )
}

function sumRegisteredSizes(sizes) {
  if (!Array.isArray(sizes)) {
    return 0
  }

  return sizes.reduce((sum, sizeEntry) => sum + Number(sizeEntry.quantity || 0), 0)
}

function DetailField({ label, children }) {
  return (
    <Stack spacing={0.5}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      {children}
    </Stack>
  )
}

function ProductSpecificationSection({ item, onEdit }) {
  const specification = item.productSpecification

  return (
    <Stack spacing={1.5}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={1.5}
        justifyContent="space-between"
        alignItems={{ xs: 'flex-start', sm: 'center' }}
      >
        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
          Especificaciones
        </Typography>
        <Button
          type="button"
          variant="outlined"
          size="small"
          onClick={() => onEdit(item)}
        >
          Editar especificaciones
        </Button>
      </Stack>

      {!specification ? (
        <Typography color="text.secondary">
          Sin especificación registrada.
        </Typography>
      ) : (
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 6 }}>
            <DetailField label="Tipo de prenda">
              <Typography>{specification.garmentType || '—'}</Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <DetailField label="Cuello">
              <Typography>{specification.collarType || '—'}</Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <DetailField label="Manga">
              <Typography>{specification.sleeveType || '—'}</Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <DetailField label="Variante">
              <Typography>{specification.garmentVariant || '—'}</Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <DetailField label="Sublimación">
              <Typography>
                {formatYesNo(specification.sublimationRequired)}
              </Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <DetailField label="Bordado">
              <Typography>
                {formatYesNo(specification.embroideryRequired)}
              </Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <DetailField label="DTF">
              <Typography>{formatYesNo(specification.dtfRequired)}</Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <DetailField label="Incluye nombres">
              <Typography>{formatYesNo(specification.includesNames)}</Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <DetailField label="Incluye números">
              <Typography>
                {formatYesNo(specification.includesNumbers)}
              </Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <DetailField label="Incluye logos">
              <Typography>{formatYesNo(specification.includesLogos)}</Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <DetailField label="Notas de decoración">
              <Typography>{specification.decorationNotes || '—'}</Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <DetailField label="Notas de personalización">
              <Typography>
                {specification.personalizationNotes || '—'}
              </Typography>
            </DetailField>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <DetailField label="Observaciones del ítem">
              <Typography>{specification.itemObservations || '—'}</Typography>
            </DetailField>
          </Grid>
        </Grid>
      )}
    </Stack>
  )
}

function OrderItemSizesSection({ item, onManage }) {
  const sizes = Array.isArray(item.sizes) ? item.sizes : []
  const registeredQuantity = sumRegisteredSizes(sizes)
  const hasSizes = sizes.length > 0
  const buttonLabel = hasSizes ? 'Gestionar tallas' : 'Registrar tallas'

  return (
    <Stack spacing={1.5}>
      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
        Tallas
      </Typography>

      {!hasSizes ? (
        <Typography color="text.secondary">Tallas pendientes</Typography>
      ) : (
        <Stack spacing={0.5}>
          {sizes.map((sizeEntry) => (
            <Typography key={`${item.itemId}-${sizeEntry.size}`}>
              {sizeEntry.size}: {sizeEntry.quantity}
            </Typography>
          ))}
        </Stack>
      )}

      <Typography variant="body2" color="text.secondary">
        Tallas registradas: {registeredQuantity} / {item.quantity}
      </Typography>

      <Button
        type="button"
        variant="outlined"
        size="small"
        onClick={() => onManage(item)}
        sx={{ alignSelf: 'flex-start' }}
      >
        {buttonLabel}
      </Button>
    </Stack>
  )
}

const headerCellSx = { fontWeight: 'bold' }

function OrderDetailLoadingSkeleton() {
  return (
    <>
      <Stack spacing={1}>
        <Skeleton width={120} height={24} />
        <Stack direction="row" spacing={1.5} alignItems="center">
          <Skeleton width={160} height={40} />
          <Skeleton width={100} height={28} sx={{ borderRadius: 4 }} />
        </Stack>
      </Stack>

      <Paper sx={{ p: 3 }}>
        <Grid container spacing={3}>
          {Array.from({ length: 6 }).map((_, index) => (
            <Grid key={`order-detail-skeleton-${index}`} size={{ xs: 12, md: 6 }}>
              <Stack spacing={0.5}>
                <Skeleton width={100} height={20} />
                <Skeleton width="70%" height={28} />
              </Stack>
            </Grid>
          ))}
        </Grid>
      </Paper>

      <Paper sx={{ p: 3 }}>
        <Stack spacing={3}>
          <Skeleton width={140} height={32} />
          <Skeleton variant="rounded" height={120} />
          <Divider />
          <Stack spacing={1.5}>
            <Skeleton width={90} height={24} />
            <Skeleton width="100%" height={24} />
            <Skeleton width="100%" height={32} />
          </Stack>
        </Stack>
      </Paper>
    </>
  )
}

function OrderDetailPage() {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const [order, setOrder] = useState(null)
  const [customerNameById, setCustomerNameById] = useState({})
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [notFound, setNotFound] = useState(false)
  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')
  const [sizesDialogItem, setSizesDialogItem] = useState(null)
  const [specificationDialogItem, setSpecificationDialogItem] = useState(null)
  const [linkedProductionOrderId, setLinkedProductionOrderId] = useState(null)
  const [productionLookupFailed, setProductionLookupFailed] = useState(false)
  const [createProductionDialogOpen, setCreateProductionDialogOpen] =
    useState(false)
  const [creatingProductionOrder, setCreatingProductionOrder] = useState(false)
  const [createProductionError, setCreateProductionError] = useState('')

  const [profitability, setProfitability] = useState(null)
  const [profitabilityLoading, setProfitabilityLoading] = useState(true)
  const [profitabilityFailed, setProfitabilityFailed] = useState(false)
  const [profitabilityError, setProfitabilityError] = useState('')

  async function loadProfitability(commercialOrderId) {
    setProfitabilityLoading(true)
    setProfitabilityFailed(false)
    setProfitabilityError('')
    try {
      const data = await getOrderProfitability(commercialOrderId)
      setProfitability(data)
    } catch (error) {
      setProfitability(null)
      setProfitabilityFailed(true)
      setProfitabilityError(
        resolveApiErrorMessage(
          error,
          'No fue posible calcular la rentabilidad directa.'
        )
      )
    } finally {
      setProfitabilityLoading(false)
    }
  }

  async function loadLinkedProductionOrder(commercialOrderId) {
    try {
      const productionOrdersPayload = await getProductionOrders()
      setLinkedProductionOrderId(
        findLinkedProductionOrderId(productionOrdersPayload, commercialOrderId)
      )
      setProductionLookupFailed(false)
    } catch {
      setLinkedProductionOrderId(null)
      setProductionLookupFailed(true)
    }
  }

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    setNotFound(false)
    setOrder(null)
    setLinkedProductionOrderId(null)
    setProductionLookupFailed(false)
    setCreateProductionDialogOpen(false)
    setCreateProductionError('')
    setProfitability(null)
    setProfitabilityFailed(false)
    setProfitabilityError('')
    setProfitabilityLoading(true)

    getOrder(orderId)
      .then((data) => {
        setOrder(data)
        setLoading(false)
      })
      .catch((error) => {
        const status = error.response?.status

        if (status === 400 || status === 404) {
          setNotFound(true)
        } else {
          setFailed(true)
        }

        setLoading(false)
      })

    loadLinkedProductionOrder(orderId)
    loadProfitability(orderId)

    getCustomers()
      .then((data) => {
        setCustomerNameById(buildCustomerNameMap(data?.customers))
      })
      .catch(() => {
        setCustomerNameById({})
      })
  }, [orderId])

  useEffect(() => {
    if (!location.state?.created) {
      return
    }

    const orderNumber = location.state.orderNumber
    setSuccessMessage(
      orderNumber
        ? `Orden ${orderNumber} creada correctamente.`
        : 'Orden creada correctamente.'
    )
    setSuccessOpen(true)
    navigate(location.pathname, { replace: true, state: {} })
  }, [location.state, location.pathname, navigate])

  async function refreshOrderAfterSave(successMessageText) {
    try {
      const refreshedOrder = await getOrder(orderId)
      setOrder(refreshedOrder)
      await loadProfitability(orderId)
      setSuccessMessage(successMessageText)
      setSuccessOpen(true)
    } catch {
      setFailed(true)
    }
  }

  async function handleSizesSaved() {
    setSizesDialogItem(null)
    await refreshOrderAfterSave('Tallas actualizadas correctamente.')
  }

  async function handleSpecificationSaved() {
    setSpecificationDialogItem(null)
    await refreshOrderAfterSave('Especificaciones actualizadas correctamente.')
  }

  function openCreateProductionDialog() {
    if (creatingProductionOrder) {
      return
    }

    setCreateProductionError('')
    setCreateProductionDialogOpen(true)
  }

  function closeCreateProductionDialog() {
    if (creatingProductionOrder) {
      return
    }

    setCreateProductionDialogOpen(false)
    setCreateProductionError('')
  }

  async function handleCreateProductionOrder() {
    if (creatingProductionOrder || !order) {
      return
    }

    setCreateProductionError('')
    setCreatingProductionOrder(true)

    try {
      const createdProductionOrder = await createProductionOrderFromOrder(
        order.orderId
      )
      const productionOrderId = createdProductionOrder?.productionOrderId

      if (!productionOrderId) {
        setCreateProductionError(
          'La creación no devolvió el identificador de producción.'
        )
        return
      }

      setCreateProductionDialogOpen(false)
      setLinkedProductionOrderId(productionOrderId)
      navigate(`/production/orders/${productionOrderId}`)
    } catch (error) {
      if (isDuplicateProductionOrderError(error)) {
        setCreateProductionError(
          resolveApiErrorMessage(
            error,
            'Ya existe una orden de producción para esta orden comercial.'
          )
        )
        await loadLinkedProductionOrder(order.orderId)
      } else {
        setCreateProductionError(
          resolveApiErrorMessage(
            error,
            'No fue posible crear la orden de producción.'
          )
        )
      }
    } finally {
      setCreatingProductionOrder(false)
    }
  }

  const statusChip = order ? getOrderStatusChipProps(order.status) : null
  const items = order?.items ?? []
  const deliveryCommitment = order?.deliveryCommitment
  const paymentSummary = order?.paymentSummary
  const isConfirmedOrder = order?.status === 'CONFIRMED'
  const canCreateProductionOrder =
    isConfirmedOrder && !linkedProductionOrderId
  const showProductionSection =
    Boolean(order) && (isConfirmedOrder || Boolean(linkedProductionOrderId))

  return (
    <Stack spacing={3}>
      <Button
        variant="outlined"
        onClick={() => navigate('/commercial/orders')}
        sx={{ alignSelf: 'flex-start' }}
      >
        Volver
      </Button>

      {loading && <OrderDetailLoadingSkeleton />}

      {!loading && failed && (
        <>
          <Typography variant="h4">Detalle de Orden</Typography>
          <Alert severity="error">No fue posible obtener la orden.</Alert>
        </>
      )}

      {!loading && !failed && notFound && (
        <>
          <Typography variant="h4">Detalle de Orden</Typography>
          <Alert severity="warning">Orden no encontrada.</Alert>
        </>
      )}

      {!loading && !failed && order && (
        <>
          <Stack spacing={1}>
            <Typography variant="body2" color="text.secondary">
              Detalle de Orden
            </Typography>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={1.5}
              alignItems={{ xs: 'flex-start', sm: 'center' }}
            >
              <Typography variant="h4">{order.orderNumber}</Typography>
              <Chip
                label={statusChip.label}
                color={statusChip.color}
                size="small"
              />
            </Stack>
            <Typography variant="body2" color="text.secondary">
              ID: {order.orderId}
            </Typography>
          </Stack>

          <Paper sx={{ p: 3 }}>
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Cliente">
                  <Typography>
                    {resolveCustomerName(order.customerId, customerNameById)}
                  </Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Cotización de origen">
                  <Typography>
                    <RouterLink
                      to={`/commercial/quotations/${order.quotationId}`}
                    >
                      {order.quotationId}
                    </RouterLink>
                  </Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Fecha de confirmación">
                  <Typography>
                    {formatDisplayDate(order.confirmationDate)}
                  </Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Fecha de entrega">
                  <Typography>
                    {formatDisplayDate(
                      deliveryCommitment?.promisedDeliveryDate
                    )}
                  </Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Vendedor">
                  <Typography>{order.salesperson}</Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Observaciones de entrega">
                  <Typography>
                    {deliveryCommitment?.deliveryObservations || '—'}
                  </Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12 }}>
                <DetailField label="Observaciones">
                  <Typography>{order.observations || '—'}</Typography>
                </DetailField>
              </Grid>
            </Grid>
          </Paper>

          {showProductionSection && (
            <Paper sx={{ p: 3 }}>
              <Stack spacing={2}>
                <Typography variant="h5">Producción</Typography>

                {linkedProductionOrderId ? (
                  <>
                    <Typography>
                      Orden de producción creada
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Esta orden comercial ya tiene una orden de producción
                      asociada.
                    </Typography>
                    <Button
                      type="button"
                      variant="contained"
                      onClick={() =>
                        navigate(`/production/orders/${linkedProductionOrderId}`)
                      }
                      sx={{ alignSelf: 'flex-start' }}
                    >
                      Ver producción
                    </Button>
                  </>
                ) : (
                  <>
                    <Typography variant="body2" color="text.secondary">
                      La información de productos, especificaciones y tallas se
                      tomará de esta orden al momento de crear producción.
                    </Typography>
                    {productionLookupFailed && (
                      <Alert severity="warning">
                        No fue posible verificar si ya existe una orden de
                        producción asociada.
                      </Alert>
                    )}
                    {canCreateProductionOrder && (
                      <Button
                        type="button"
                        variant="contained"
                        onClick={openCreateProductionDialog}
                        sx={{ alignSelf: 'flex-start' }}
                      >
                        Crear orden de producción
                      </Button>
                    )}
                  </>
                )}
              </Stack>
            </Paper>
          )}

          <Paper sx={{ p: 3 }}>
            <Stack spacing={3}>
              <Typography variant="h5">Resumen de pago</Typography>
              <Grid container spacing={3}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Anticipo reconocido">
                    <Typography>
                      {formatAcknowledgment(
                        paymentSummary?.advanceAcknowledged
                      )}
                    </Typography>
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Pago final reconocido">
                    <Typography>
                      {formatAcknowledgment(
                        paymentSummary?.finalPaymentAcknowledged
                      )}
                    </Typography>
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Total comprometido">
                    <Typography>
                      {formatCurrency(paymentSummary?.committedTotal)}
                    </Typography>
                  </DetailField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <DetailField label="Saldo restante">
                    <Typography>
                      {formatCurrency(paymentSummary?.remainingBalance)}
                    </Typography>
                  </DetailField>
                </Grid>
              </Grid>
            </Stack>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Stack spacing={3}>
              <Stack spacing={1}>
                <Typography variant="h5">Rentabilidad</Typography>
                <Typography variant="body2" color="text.secondary">
                  Este cálculo considera únicamente ingresos del pedido y costos
                  directos actualmente registrados. No incluye gastos generales,
                  servicios, créditos, impuestos ni otros costos indirectos.
                </Typography>
              </Stack>

              {profitabilityLoading ? (
                <Stack spacing={1}>
                  <Skeleton variant="text" width="40%" />
                  <Skeleton variant="text" width="60%" />
                  <Skeleton variant="rectangular" height={80} />
                </Stack>
              ) : null}

              {profitabilityFailed ? (
                <Alert severity="warning">
                  No se puede calcular todavía.{' '}
                  {profitabilityError ||
                    'Intente recargar la página más tarde.'}
                </Alert>
              ) : null}

              {!profitabilityLoading && !profitabilityFailed && profitability ? (
                <>
                  <Alert
                    severity={getProfitabilityStatusSeverity(
                      profitability.profitabilityStatus
                    )}
                  >
                    {getProfitabilityStatusLabel(
                      profitability.profitabilityStatus
                    )}
                  </Alert>

                  <Grid container spacing={3}>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <DetailField label="Valor del pedido">
                        <Typography variant="h5">
                          {formatCurrency(profitability.orderValue)}
                        </Typography>
                      </DetailField>
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <DetailField label="Dinero recibido">
                        <Typography variant="h6">
                          {formatCurrency(profitability.collectedAmount)}
                        </Typography>
                      </DetailField>
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <DetailField label="Pendiente por cobrar">
                        <Typography variant="h6">
                          {formatCurrency(profitability.outstandingAmount)}
                        </Typography>
                      </DetailField>
                    </Grid>
                  </Grid>

                  <Divider />

                  <Stack spacing={1.5}>
                    <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                      Costos directos
                    </Typography>
                    <Typography>
                      Materiales: {formatCurrency(profitability.materialCost)}
                    </Typography>
                    <Typography>
                      Mano de obra: {formatCurrency(profitability.laborCost)}
                    </Typography>
                    <Typography>
                      Plotter:{' '}
                      {profitability.plotterCostAttributable
                        ? formatCurrency(profitability.plotterMaterialCost)
                        : 'No atribuible'}
                    </Typography>
                    <Typography sx={{ fontWeight: 600 }}>
                      Total costos directos:{' '}
                      {formatCurrency(profitability.totalDirectCost)}
                    </Typography>
                    {profitability.unvaluedMaterialConsumptionCount > 0 ? (
                      <Typography variant="body2" color="warning.main">
                        Consumos sin costo configurado:{' '}
                        {profitability.unvaluedMaterialConsumptionCount}
                      </Typography>
                    ) : null}
                  </Stack>

                  <Divider />

                  <Grid container spacing={3}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <DetailField label="Resultado directo">
                        <Typography variant="h5">
                          {formatCurrency(profitability.directProfit)}
                        </Typography>
                      </DetailField>
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <DetailField label="Margen directo">
                        <Typography variant="h5">
                          {formatMarginPercentage(
                            profitability.directMarginPercentage
                          )}
                        </Typography>
                      </DetailField>
                    </Grid>
                  </Grid>
                </>
              ) : null}
            </Stack>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Stack spacing={3}>
              <Typography variant="h5">Productos</Typography>

              {items.length === 0 ? (
                <Stack spacing={1.5} alignItems="center" sx={{ py: 3 }}>
                  <Inventory2OutlinedIcon
                    color="action"
                    sx={{ fontSize: 48 }}
                  />
                  <Typography>No hay productos en esta orden.</Typography>
                  <Typography color="text.secondary" textAlign="center">
                    El snapshot comercial no contiene líneas de producto.
                  </Typography>
                </Stack>
              ) : (
                <Stack spacing={3}>
                  <TableContainer sx={{ overflowX: 'auto' }}>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell sx={headerCellSx}>Producto</TableCell>
                          <TableCell sx={headerCellSx}>Tela</TableCell>
                          <TableCell sx={headerCellSx}>Color</TableCell>
                          <TableCell align="right" sx={headerCellSx}>
                            Cantidad
                          </TableCell>
                          <TableCell align="right" sx={headerCellSx}>
                            Precio unitario
                          </TableCell>
                          <TableCell align="right" sx={headerCellSx}>
                            Subtotal
                          </TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {items.map((item) => (
                          <TableRow key={item.itemId} hover>
                            <TableCell>{item.productName}</TableCell>
                            <TableCell>{item.fabric}</TableCell>
                            <TableCell>{item.color}</TableCell>
                            <TableCell align="right">{item.quantity}</TableCell>
                            <TableCell align="right">
                              {formatCurrency(item.unitPrice)}
                            </TableCell>
                            <TableCell align="right">
                              {formatCurrency(item.subtotal)}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>

                  {items.map((item) => (
                    <Paper
                      key={`item-detail-${item.itemId}`}
                      variant="outlined"
                      sx={{ p: 2.5 }}
                    >
                      <Stack spacing={2.5}>
                        <Stack spacing={0.5}>
                          <Typography variant="body2" color="text.secondary">
                            Producto
                          </Typography>
                          <Typography variant="h6">{item.productName}</Typography>
                          <Typography variant="body2" color="text.secondary">
                            Cantidad: {item.quantity}
                          </Typography>
                        </Stack>

                        <Divider />

                        <ProductSpecificationSection
                          item={item}
                          onEdit={setSpecificationDialogItem}
                        />

                        <Divider />

                        <OrderItemSizesSection
                          item={item}
                          onManage={setSizesDialogItem}
                        />
                      </Stack>
                    </Paper>
                  ))}
                </Stack>
              )}

              <Divider />

              <Stack spacing={1.5}>
                <Typography sx={{ fontWeight: 'bold' }}>Resumen</Typography>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                >
                  <Typography variant="h6" sx={{ fontWeight: 700 }}>
                    TOTAL
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: 700 }}>
                    {formatCurrency(order.totalAmount)}
                  </Typography>
                </Stack>
              </Stack>
            </Stack>
          </Paper>
        </>
      )}

      <ManageOrderItemSizesDialog
        open={Boolean(sizesDialogItem)}
        onClose={() => setSizesDialogItem(null)}
        orderId={orderId}
        orderItem={sizesDialogItem}
        onSaved={handleSizesSaved}
      />

      <ManageOrderItemProductSpecificationDialog
        open={Boolean(specificationDialogItem)}
        onClose={() => setSpecificationDialogItem(null)}
        orderId={orderId}
        orderItem={specificationDialogItem}
        onSaved={handleSpecificationSaved}
      />

      <CreateProductionOrderDialog
        open={createProductionDialogOpen}
        onClose={closeCreateProductionDialog}
        onConfirm={handleCreateProductionOrder}
        submitting={creatingProductionOrder}
        errorMessage={createProductionError}
      />

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
          {successMessage}
        </Alert>
      </Snackbar>
    </Stack>
  )
}

export default OrderDetailPage
