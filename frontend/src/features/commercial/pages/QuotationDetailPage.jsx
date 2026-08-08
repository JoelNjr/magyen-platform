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
  Typography,
} from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'
import { getQuotations } from '../services/commercialService'

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

function QuotationDetailPage() {
  const { quotationId } = useParams()
  const navigate = useNavigate()
  const [quotation, setQuotation] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [notFound, setNotFound] = useState(false)

  useEffect(() => {
    setLoading(true)
    setFailed(false)
    setNotFound(false)
    setQuotation(null)

    getQuotations()
      .then((data) => {
        const foundQuotation = data.quotations.find(
          (item) => item.quotationId === quotationId
        )

        if (foundQuotation) {
          setQuotation(foundQuotation)
        } else {
          setNotFound(true)
        }

        setLoading(false)
      })
      .catch(() => {
        setFailed(true)
        setLoading(false)
      })
  }, [quotationId])

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
              <Typography variant="h5">Productos</Typography>

              <Stack spacing={1.5} alignItems="center" sx={{ py: 3 }}>
                <Inventory2OutlinedIcon
                  color="action"
                  sx={{ fontSize: 48 }}
                />
                <Typography>No hay productos registrados.</Typography>
                <Typography color="text.secondary" textAlign="center">
                  Agrega productos para comenzar a construir esta cotización.
                </Typography>
                <Button variant="outlined" disabled startIcon={<AddIcon />}>
                  Agregar producto
                </Button>
              </Stack>

              <Divider />

              <Stack spacing={1}>
                <Typography sx={{ fontWeight: 'bold' }}>Resumen</Typography>
                <Typography>Subtotal: {formatCurrency(0)}</Typography>
                <Typography>Total: {formatCurrency(0)}</Typography>
              </Stack>
            </Stack>
          </Paper>
        </>
      )}
    </Stack>
  )
}

export default QuotationDetailPage
