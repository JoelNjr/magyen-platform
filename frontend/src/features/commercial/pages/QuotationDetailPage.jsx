import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import {
  Alert,
  Button,
  Chip,
  Divider,
  Grid,
  IconButton,
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
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'
import AddQuotationItemDialog from '../components/AddQuotationItemDialog'
import ApproveQuotationDialog from '../components/ApproveQuotationDialog'
import CreateOrderFromQuotationDialog from '../components/CreateOrderFromQuotationDialog'
import RemoveQuotationItemDialog from '../components/RemoveQuotationItemDialog'
import { formatDisplayDate } from '../presentation/formatDisplayDate'
import { formatQuotationNumber } from '../presentation/formatQuotationNumber'
import {
  buildCustomerNameMap,
  resolveCustomerName,
} from '../presentation/resolveCustomerName'
import {
  addQuotationItem,
  applyQuotationDiscount,
  approveQuotation,
  createOrder,
  downloadQuotationPdf,
  getCustomers,
  getQuotation,
  removeQuotationItem,
  updateQuotationItem,
} from '../services/commercialService'
import PageHeader, { BrandAccentLine } from '../../../layout/PageHeader'
import {
  QUOTATION_PDF_ACTION_LABEL,
  resolveBlobApiErrorMessage,
} from '../presentation/commercialDocumentDownload'

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

function mergeQuotationItemUpdatePayload(existingItem, payload) {
  const existingSpec = existingItem?.productSpecification || {}
  const formSpec = payload.productSpecification || {}
  return {
    ...payload,
    productSpecification: {
      garmentType: formSpec.garmentType || null,
      collarType: formSpec.collarType || null,
      sleeveType: formSpec.sleeveType || null,
      cuffRequired:
        formSpec.cuffRequired === undefined ? null : formSpec.cuffRequired,
      sublimationRequired: Boolean(existingSpec.sublimationRequired),
      embroideryRequired: Boolean(existingSpec.embroideryRequired),
      dtfRequired: Boolean(existingSpec.dtfRequired),
      decorationNotes: existingSpec.decorationNotes || null,
      includesNames: Boolean(existingSpec.includesNames),
      includesNumbers: Boolean(existingSpec.includesNumbers),
      includesLogos: Boolean(existingSpec.includesLogos),
      personalizationNotes: existingSpec.personalizationNotes || null,
      itemObservations: existingSpec.itemObservations || null,
    },
  }
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
  const [editingItem, setEditingItem] = useState(null)
  const [submittingItem, setSubmittingItem] = useState(false)
  const [itemFormError, setItemFormError] = useState(false)

  const [itemToRemove, setItemToRemove] = useState(null)
  const [removingItem, setRemovingItem] = useState(false)
  const [removeItemError, setRemoveItemError] = useState('')

  const [approveDialogOpen, setApproveDialogOpen] = useState(false)
  const [approving, setApproving] = useState(false)
  const [approveError, setApproveError] = useState('')

  const [createOrderDialogOpen, setCreateOrderDialogOpen] = useState(false)
  const [creatingOrder, setCreatingOrder] = useState(false)
  const [createOrderError, setCreateOrderError] = useState('')

  const [successOpen, setSuccessOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')
  const [generatingPdf, setGeneratingPdf] = useState(false)
  const [pdfError, setPdfError] = useState('')
  const [discountInput, setDiscountInput] = useState('')
  const [applyingDiscount, setApplyingDiscount] = useState(false)
  const [discountError, setDiscountError] = useState('')

  const pageBusy =
    submittingItem ||
    removingItem ||
    approving ||
    creatingOrder ||
    generatingPdf ||
    applyingDiscount

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    setNotFound(false)
    setQuotation(null)

    getQuotation(quotationId)
      .then((data) => {
        setQuotation(data)
        setDiscountInput(
          data?.discountAmount == null ? '0' : String(data.discountAmount)
        )
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
    setDiscountInput(
      refreshedQuotation?.discountAmount == null
        ? '0'
        : String(refreshedQuotation.discountAmount)
    )
  }

  async function handleApplyDiscount() {
    if (pageBusy || quotation.status !== 'DRAFT') {
      return
    }
    const amount = Number(discountInput)
    if (Number.isNaN(amount) || amount < 0) {
      setDiscountError('El descuento no puede ser negativo.')
      return
    }
    setDiscountError('')
    setApplyingDiscount(true)
    try {
      await applyQuotationDiscount(quotation.quotationId, { discountAmount: amount })
      await refreshQuotation()
      setSuccessMessage('Descuento actualizado.')
      setSuccessOpen(true)
    } catch (error) {
      setDiscountError(
        resolveApiErrorMessage(error, 'No fue posible aplicar el descuento.')
      )
    } finally {
      setApplyingDiscount(false)
    }
  }

  async function handleSubmitItem(payload) {
    if (submittingItem) {
      return
    }

    setItemFormError(false)
    setSubmittingItem(true)

    try {
      if (editingItem) {
        await updateQuotationItem(
          quotation.quotationId,
          editingItem.itemId,
          mergeQuotationItemUpdatePayload(editingItem, payload)
        )
        setSuccessMessage('Producto actualizado correctamente.')
      } else {
        await addQuotationItem(quotation.quotationId, payload)
        setSuccessMessage('Producto agregado correctamente.')
      }
      await refreshQuotation()
      setDialogOpen(false)
      setEditingItem(null)
      setSuccessOpen(true)
    } catch {
      setItemFormError(true)
    } finally {
      setSubmittingItem(false)
    }
  }

  function handleDialogClose() {
    if (submittingItem) {
      return
    }

    setDialogOpen(false)
    setEditingItem(null)
    setItemFormError(false)
  }

  function openAddDialog() {
    if (pageBusy || quotation.status !== 'DRAFT') {
      return
    }

    setEditingItem(null)
    setItemFormError(false)
    setDialogOpen(true)
  }

  function openEditDialog(item) {
    if (pageBusy || quotation.status !== 'DRAFT') {
      return
    }

    setEditingItem(item)
    setItemFormError(false)
    setDialogOpen(true)
  }

  function openRemoveDialog(item) {
    if (pageBusy || quotation.status !== 'DRAFT') {
      return
    }

    setRemoveItemError('')
    setItemToRemove(item)
  }

  function closeRemoveDialog() {
    if (removingItem) {
      return
    }

    setItemToRemove(null)
    setRemoveItemError('')
  }

  async function handleRemoveItemConfirm() {
    if (removingItem || !itemToRemove) {
      return
    }

    setRemoveItemError('')
    setRemovingItem(true)

    try {
      await removeQuotationItem(quotation.quotationId, itemToRemove.itemId)
      await refreshQuotation()
      setItemToRemove(null)
      setSuccessMessage('Producto eliminado correctamente.')
      setSuccessOpen(true)
    } catch (error) {
      setRemoveItemError(
        resolveApiErrorMessage(error, 'No fue posible eliminar el producto.')
      )
    } finally {
      setRemovingItem(false)
    }
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

  async function handleCreateOrderSubmit({ description, confirmationDate }) {
    if (creatingOrder) {
      return
    }

    setCreateOrderError('')
    setCreatingOrder(true)

    try {
      const createdOrder = await createOrder({
        quotationId: quotation.quotationId,
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

  async function handleGeneratePdf() {
    if (pageBusy || !quotation) {
      return
    }

    setPdfError('')
    setGeneratingPdf(true)

    try {
      await downloadQuotationPdf(quotation.quotationId)
    } catch (error) {
      setPdfError(
        await resolveBlobApiErrorMessage(
          error,
          'No fue posible generar el PDF de la cotización.'
        )
      )
    } finally {
      setGeneratingPdf(false)
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
          <PageHeader title="Detalle de Cotización" />
          <Typography>No fue posible obtener la cotización.</Typography>
        </>
      )}

      {!loading && !failed && notFound && (
        <>
          <PageHeader title="Detalle de Cotización" />
          <Typography>Cotización no encontrada.</Typography>
        </>
      )}

      {!loading && !failed && quotation && (
        <>
          {pdfError ? (
            <Alert severity="error" onClose={() => setPdfError('')}>
              {pdfError}
            </Alert>
          ) : null}
          <Stack
            direction={{ xs: 'column', md: 'row' }}
            spacing={1.5}
            justifyContent="space-between"
            alignItems={{ xs: 'stretch', md: 'flex-start' }}
          >
            <Stack spacing={1}>
              <BrandAccentLine />
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
              <Button
                variant="outlined"
                disabled={pageBusy}
                onClick={handleGeneratePdf}
              >
                {generatingPdf ? 'Generando PDF…' : QUOTATION_PDF_ACTION_LABEL}
              </Button>
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
                        <TableCell sx={headerCellSx}>Tela principal</TableCell>
                        <TableCell sx={headerCellSx}>Tela secundaria</TableCell>
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
                        {quotation.status === 'DRAFT' && (
                          <TableCell align="right" sx={headerCellSx}>
                            Acciones
                          </TableCell>
                        )}
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {quotation.items.map((item) => (
                        <TableRow key={item.itemId} hover>
                          <TableCell>{item.productName}</TableCell>
                          <TableCell>{item.fabric}</TableCell>
                          <TableCell>{item.secondaryFabric || '—'}</TableCell>
                          <TableCell>{item.color}</TableCell>
                          <TableCell align="right">{item.quantity}</TableCell>
                          <TableCell align="right">
                            {formatCurrency(item.unitPrice)}
                          </TableCell>
                          <TableCell align="right">
                            {formatCurrency(item.subtotal)}
                          </TableCell>
                          {quotation.status === 'DRAFT' && (
                            <TableCell align="right">
                              <Stack
                                direction="row"
                                spacing={0.5}
                                justifyContent="flex-end"
                              >
                                <Tooltip title="Editar">
                                  <span>
                                    <IconButton
                                      aria-label="Editar producto"
                                      size="small"
                                      disabled={pageBusy}
                                      onClick={() => openEditDialog(item)}
                                    >
                                      <EditOutlinedIcon fontSize="small" />
                                    </IconButton>
                                  </span>
                                </Tooltip>
                                <Tooltip title="Eliminar">
                                  <span>
                                    <IconButton
                                      aria-label="Eliminar producto"
                                      size="small"
                                      disabled={pageBusy}
                                      onClick={() => openRemoveDialog(item)}
                                    >
                                      <DeleteOutlinedIcon fontSize="small" />
                                    </IconButton>
                                  </span>
                                </Tooltip>
                              </Stack>
                            </TableCell>
                          )}
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
                    {formatCurrency(quotation.subtotalAmount ?? quotation.totalAmount)}
                  </Typography>
                </Stack>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                >
                  <Typography color="text.secondary">Descuento</Typography>
                  <Typography color="text.secondary">
                    {formatCurrency(quotation.discountAmount ?? 0)}
                  </Typography>
                </Stack>
                {quotation.status === 'DRAFT' ? (
                  <Stack
                    direction={{ xs: 'column', sm: 'row' }}
                    spacing={1.5}
                    alignItems={{ sm: 'center' }}
                  >
                    <TextField
                      label="Descuento sobre el total"
                      type="number"
                      size="small"
                      value={discountInput}
                      onChange={(event) => setDiscountInput(event.target.value)}
                      disabled={pageBusy}
                      inputProps={{ min: 0, step: '0.01' }}
                      sx={{ maxWidth: { sm: 220 } }}
                    />
                    <Button
                      variant="outlined"
                      onClick={handleApplyDiscount}
                      disabled={pageBusy}
                    >
                      {applyingDiscount ? 'Aplicando...' : 'Aplicar descuento'}
                    </Button>
                  </Stack>
                ) : null}
                {discountError ? <Alert severity="error">{discountError}</Alert> : null}
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
            onSubmit={handleSubmitItem}
            submitting={submittingItem}
            error={itemFormError}
            item={editingItem}
          />

          <RemoveQuotationItemDialog
            open={Boolean(itemToRemove)}
            onClose={closeRemoveDialog}
            onConfirm={handleRemoveItemConfirm}
            submitting={removingItem}
            errorMessage={removeItemError}
            productName={itemToRemove?.productName}
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
            quotationNumber={quotation.quotationNumber}
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
