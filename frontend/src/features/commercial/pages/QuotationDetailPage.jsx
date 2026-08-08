import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import {
  Button,
  Chip,
  Divider,
  Grid,
  Paper,
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
import { addQuotationItem, getQuotation } from '../services/commercialService'

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
    case 'REJECTED':
      return { label: 'Rechazada', color: 'error' }
    default:
      return { label: 'Estado desconocido', color: 'default' }
  }
}

function DetailField({ label, children }) {
  return (
    <Stack spacing={0.5}>
      <Typography sx={{ fontWeight: 'bold' }}>{label}</Typography>
      {children}
    </Stack>
  )
}

const headerCellSx = { fontWeight: 'bold' }

function QuotationDetailPage() {
  const { quotationId } = useParams()
  const navigate = useNavigate()
  const [quotation, setQuotation] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [notFound, setNotFound] = useState(false)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [submittingItem, setSubmittingItem] = useState(false)
  const [addItemError, setAddItemError] = useState(false)

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
  }, [quotationId])

  async function handleAddItem(payload) {
    setAddItemError(false)
    setSubmittingItem(true)

    try {
      await addQuotationItem(quotation.quotationId, payload)
      const refreshedQuotation = await getQuotation(quotation.quotationId)
      setQuotation(refreshedQuotation)
      setDialogOpen(false)
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

  return (
    <Stack spacing={3}>
      <Button
        variant="outlined"
        onClick={() => navigate('/commercial')}
        sx={{ alignSelf: 'flex-start' }}
      >
        Volver
      </Button>

      <Typography variant="h4">Detalle de Cotización</Typography>

      {loading && <Typography>Cargando cotización...</Typography>}

      {!loading && failed && (
        <Typography>No fue posible obtener la cotización.</Typography>
      )}

      {!loading && !failed && notFound && (
        <Typography>Cotización no encontrada.</Typography>
      )}

      {!loading && !failed && quotation && (
        <>
          <Paper sx={{ p: 3 }}>
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Cliente">
                  <Typography>{quotation.customerId}</Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Estado">
                  <Chip
                    label={getStatusChipProps(quotation.status).label}
                    color={getStatusChipProps(quotation.status).color}
                    size="small"
                    sx={{ alignSelf: 'flex-start' }}
                  />
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Fecha creación">
                  <Typography>{quotation.creationDate}</Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Fecha entrega">
                  <Typography>{quotation.deliveryDate}</Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Vendedor">
                  <Typography>{quotation.salesperson}</Typography>
                </DetailField>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <DetailField label="Observaciones">
                  <Typography>{quotation.observations}</Typography>
                </DetailField>
              </Grid>
            </Grid>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Stack spacing={3}>
              <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
              >
                <Typography variant="h5">Productos</Typography>
                {quotation.items.length > 0 && (
                  <Button
                    variant="outlined"
                    disabled={quotation.status !== 'DRAFT'}
                    startIcon={<AddIcon />}
                    onClick={() => {
                      setAddItemError(false)
                      setDialogOpen(true)
                    }}
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
                    disabled={quotation.status !== 'DRAFT'}
                    startIcon={<AddIcon />}
                    onClick={() => {
                      setAddItemError(false)
                      setDialogOpen(true)
                    }}
                  >
                    Agregar producto
                  </Button>
                </Stack>
              ) : (
                <TableContainer>
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
                      {quotation.items.map((item) => (
                        <TableRow key={item.itemId}>
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

              <Stack spacing={1}>
                <Typography sx={{ fontWeight: 'bold' }}>Resumen</Typography>
                <Typography>
                  Subtotal: {formatCurrency(quotation.totalAmount)}
                </Typography>
                <Typography>
                  Total: {formatCurrency(quotation.totalAmount)}
                </Typography>
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
        </>
      )}
    </Stack>
  )
}

export default QuotationDetailPage
