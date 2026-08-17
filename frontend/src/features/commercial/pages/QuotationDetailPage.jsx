import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
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
import { useNavigate, useParams } from 'react-router-dom'
import AddQuotationItemDialog from '../components/AddQuotationItemDialog'
import ApproveQuotationDialog from '../components/ApproveQuotationDialog'
import CreateOrderFromQuotationDialog from '../components/CreateOrderFromQuotationDialog'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import { formatQuotationNumber } from '../presentation/formatQuotationNumber'
import {
  buildCustomerNameMap,
  resolveCustomerName,
} from '../presentation/resolveCustomerName'
import {
  addQuotationItem,
  approveQuotation,
  createOrder,
  getCustomers,
  getQuotation,
} from '../services/commercialService'

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
      return { label: 'Estado desconocido', color: 'default' }
  }
}

function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
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

const headerCellSx = { fontWeight: 'bold' }

function QuotationDetailLoadingSkeleton() {
  return (
    <>
      <Stack spacing={1}>
        <Skeleton width={160} height={24} />
        <Stack direction="row" spacing={1.5} alignItems="center">
          <Skeleton width={140} height={40} />
          <Skeleton width={80} height={28} sx={{ borderRadius: 4 }} />
        </Stack>
      </Stack>

      <Paper sx={{ p: 3 }}>
        <Grid container spacing={3}>
          {Array.from({ length: 5 }).map((_, index) => (
            <Grid key={`detail-skeleton-field-${index}`} size={{ xs: 12, md: 6 }}>
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
          <Skeleton width={120} height={32} />
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

function QuotationDetailPage() {
  const { quotationId } = useParams()
  const navigate = useNavigate()
  const [quotation, setQuotation] = useState(null)
  const [customerNameById, setCustomerNameById] = useState({})
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [notFound, setNotFound] = useState(false)

  const [dialogOpen, setDialogOpen] = useState(false)
  const [submittingItem, setSubmittingItem] = useState(false)
  const [addItemError, setAddItemError] = useState(false)

  const [approveDialogOpen, setApproveDialogOpen] = useState(false)
  const [approving, setApproving] = useState(false)
  const [approveError, setApproveError] = useState('')

  const [createOrderDialogOpen, setCreateOrderDialogOpen] = useState(false)
  const [creatingOrder, setCreatingOrder] = useState(false)
  const [createOrderError, setCreateOrderError] = useState('')

  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')

  const pageBusy = submittingItem || approving || creatingOrder

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    setNotFound(false)
    setQuotation(null)

    getQuotation(quotationId)
      .then((data) => {
        setQuotation(data)
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

    getCustomers()
      .then((data) => {
        setCustomerNameById(buildCustomerNameMap(data?.customers))
      })
      .catch(() => {
        setCustomerNameById({})
      })
  }, [quotationId])

  async function refreshQuotation() {
    const refreshedQuotation = await getQuotation(quotation.quotationId)
    setQuotation(refreshedQuotation)
  }

  async function handleAddItem(payload) {
    if (submittingItem) {
      return
    }

    setAddItemError(false)
    setSubmittingItem(true)

    try {
      await addQuotationItem(quotation.quotationId, payload)
      await refreshQuotation()
      setDialogOpen(false)
      setSuccessMessage('Producto agregado correctamente.')
      setSuccessOpen(true)
    } catch {
      setAddItemError(true)
    } finally {
      setSubmittingItem(false)
    }
  }

  function handleDialogClose() {
    if (submittingItem) {
      return
    }

    setDialogOpen(false)
    setAddItemError(false)
  }

  function openAddDialog() {
    if (pageBusy) {
      return
    }

    setAddItemError(false)
    setDialogOpen(true)
  }

  function openApproveDialog() {
    if (pageBusy || quotation.status !== 'DRAFT') {
      return
    }

    setApproveError('')
    setApproveDialogOpen(true)
  }

  function closeApproveDialog() {
    if (approving) {
      return
    }

    setApproveDialogOpen(false)
    setApproveError('')
  }

  async function handleApproveConfirm() {
    if (approving) {
      return
    }

    setApproveError('')
    setApproving(true)

    try {
      await approveQuotation(quotation.quotationId)
      await refreshQuotation()
      setApproveDialogOpen(false)
      setSuccessMessage('Cotización aprobada correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setApproveError(
        resolveApiErrorMessage(error, 'No fue posible aprobar la cotización.')
      )
    } finally {
      setApproving(false)
    }
  }

  function openCreateOrderDialog() {
    if (pageBusy || quotation.status !== 'APPROVED' || quotation.orderId) {
      return
    }

    setCreateOrderError('')
    setCreateOrderDialogOpen(true)
  }

  function closeCreateOrderDialog() {
    if (creatingOrder) {
      return
    }

    setCreateOrderDialogOpen(false)
    setCreateOrderError('')
  }

  async function handleCreateOrderSubmit({ orderNumber, description, confirmationDate }) {
    if (creatingOrder) {
      return
    }

    setCreateOrderError('')
    setCreatingOrder(true)

    try {
      const createdOrder = await createOrder({
        quotationId: quotation.quotationId,
        orderNumber,
        description,
        confirmationDate,
        deliveryDate: quotation.deliveryDate,
        observations: quotation.observations,
      })

      setCreateOrderDialogOpen(false)
      navigate(`/commercial/orders/${createdOrder.orderId}`, {
        state: {
          created: true,
          orderNumber: createdOrder.orderNumber,
        },
      })
    } catch (error) {
      if (error?.response?.status === 409) {
        setCreateOrderError('Esta cotización ya tiene una orden asociada.')
      } else {
        setCreateOrderError(
          resolveApiErrorMessage(error, 'No fue posible crear la orden.')
        )
      }
    } finally {
      setCreatingOrder(false)
    }
  }

  return (
    <Stack spacing={3}>
      <Button
        variant="outlined"
        onClick={() => navigate('/commercial')}
        sx={{ alignSelf: 'flex-start' }}
      >
        Volver
      </Button>

      {loading && <QuotationDetailLoadingSkeleton />}

      {!loading && failed && (
        <>
          <Typography variant="h4">Detalle de Cotización</Typography>
          <Typography>No fue posible obtener la cotización.</Typography>
        </>
      )}

      {!loading && !failed && notFound && (
        <>
          <Typography variant="h4">Detalle de Cotización</Typography>
          <Typography>Cotización no encontrada.</Typography>
        </>
      )}

      {!loading && !failed && quotation && (
        <>
          <Stack
            direction={{ xs: 'column', md: 'row' }}
            spacing={1.5}
            justifyContent="space-between"
            alignItems={{ xs: 'stretch', md: 'flex-start' }}
          >
            <Stack spacing={1}>
              <Typography variant="body2" color="text.secondary">
                Detalle de Cotización
              </Typography>
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={1.5}
                alignItems={{ xs: 'flex-start', sm: 'center' }}
              >
                <Typography variant="h4">
                  Cotización {formatQuotationNumber(quotation.quotationNumber)}
                </Typography>
                <Chip
                  label={getStatusChipProps(quotation.status).label}
                  color={getStatusChipProps(quotation.status).color}
                  size="small"
                />
              </Stack>
            </Stack>

            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={1.5}
              alignItems={{ xs: 'stretch', sm: 'center' }}
            >
              {quotation.status === 'DRAFT' && (
                <Button
                  variant="contained"
                  disabled={pageBusy}
                  onClick={openApproveDialog}
                >
                  Aprobar cotización
                </Button>
              )}
              {quotation.status === 'APPROVED' && !quotation.orderId && (
                <Button
                  variant="contained"
                  disabled={pageBusy}
                  onClick={openCreateOrderDialog}
                >
                  Crear orden
                </Button>
              )}
              {quotation.status === 'APPROVED' && quotation.orderId && (
                <Stack
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={1.5}
                  alignItems={{ xs: 'stretch', sm: 'center' }}
                >
                  <Chip label="Orden creada" color="success" size="small" />
                  <Button
                    variant="outlined"
                    disabled={pageBusy}
                    onClick={() =>
                      navigate(`/commercial/orders/${quotation.orderId}`)
                    }
                  >
                    Ver orden
                  </Button>
                </Stack>
              )}
            </Stack>
          </Stack>

          <Paper sx={{ p: 3 }}>
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Cliente">
                  <Typography>
                    {resolveCustomerName(
                      quotation.customerId,
                      customerNameById
                    )}
                  </Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Vendedor">
                  <Typography>{quotation.sellerName || '—'}</Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Fecha creación">
                  <Typography>
                    {formatDisplayDate(quotation.creationDate)}
                  </Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Fecha entrega">
                  <Typography>
                    {formatDisplayDate(quotation.deliveryDate)}
                  </Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12 }}>
                <DetailField label="Observaciones">
                  <Typography>{quotation.observations}</Typography>
                </DetailField>
              </Grid>
            </Grid>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Stack spacing={3}>
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={1.5}
                justifyContent="space-between"
                alignItems={{ xs: 'stretch', sm: 'center' }}
              >
                <Typography variant="h5">Productos</Typography>
                {quotation.items.length > 0 && (
                  <Button
                    variant="outlined"
                    disabled={quotation.status !== 'DRAFT' || pageBusy}
                    startIcon={<AddIcon />}
                    onClick={openAddDialog}
                  >
                    Agregar producto
                  </Button>
                )}
              </Stack>

              {quotation.items.length === 0 ? (
                <Stack spacing={1.5} alignItems="center" sx={{ py: 3 }}>
                  <Inventory2OutlinedIcon
                    color="action"
                    sx={{ fontSize: 48 }}
                  />
                  <Typography>No hay productos registrados.</Typography>
                  <Typography color="text.secondary" textAlign="center">
                    Agrega productos para comenzar a construir esta cotización.
                  </Typography>
                  <Button
                    variant="outlined"
                    disabled={quotation.status !== 'DRAFT' || pageBusy}
                    startIcon={<AddIcon />}
                    onClick={openAddDialog}
                  >
                    Agregar producto
                  </Button>
                </Stack>
              ) : (
                <TableContainer sx={{ overflowX: 'auto' }}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell sx={headerCellSx}>Producto</TableCell>
                        <TableCell sx={headerCellSx}>Tela</TableCell>
                        <TableCell sx={headerCellSx}>Color de tela / base</TableCell>
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
                      {quotation.items.map((item) => (
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
              )}

              <Divider />

              <Stack spacing={1.5}>
                <Typography sx={{ fontWeight: 'bold' }}>Resumen</Typography>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                >
                  <Typography color="text.secondary">Subtotal</Typography>
                  <Typography color="text.secondary">
                    {formatCurrency(quotation.totalAmount)}
                  </Typography>
                </Stack>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                >
                  <Typography variant="h6" sx={{ fontWeight: 700 }}>
                    TOTAL
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: 700 }}>
                    {formatCurrency(quotation.totalAmount)}
                  </Typography>
                </Stack>
              </Stack>
            </Stack>
          </Paper>

          <AddQuotationItemDialog
            open={dialogOpen}
            onClose={handleDialogClose}
            onSubmit={handleAddItem}
            submitting={submittingItem}
            error={addItemError}
          />

          <ApproveQuotationDialog
            open={approveDialogOpen}
            onClose={closeApproveDialog}
            onConfirm={handleApproveConfirm}
            submitting={approving}
            errorMessage={approveError}
          />

          <CreateOrderFromQuotationDialog
            open={createOrderDialogOpen}
            onClose={closeCreateOrderDialog}
            onSubmit={handleCreateOrderSubmit}
            submitting={creatingOrder}
            errorMessage={createOrderError}
            quotationDate={quotation.creationDate}
            deliveryDate={quotation.deliveryDate}
            sellerName={quotation.sellerName}
          />
        </>
      )}

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

export default QuotationDetailPage
