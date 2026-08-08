import { useEffect, useState } from 'react'
import {
  Chip,
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
import { Link as RouterLink } from 'react-router-dom'
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

function formatDate(value) {
  if (!value) {
    return ''
  }

  const [year, month, day] = value.split('-')
  return `${day}/${month}/${year}`
}

function getStatusChipProps(status) {
  switch (status) {
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

function QuotationsPage() {
  const [quotations, setQuotations] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    getQuotations()
      .then((data) => {
        setQuotations(data.quotations)
        setLoading(false)
      })
      .catch(() => {
        setFailed(true)
        setLoading(false)
      })
  }, [])

  if (loading) {
    return <Typography>Cargando cotizaciones...</Typography>
  }

  if (failed) {
    return <Typography>No fue posible obtener las cotizaciones.</Typography>
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h3">Cotizaciones</Typography>

      <TableContainer component={Paper}>
        <Table>
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
              <TableCell align="center" sx={headerCellSx}>
                Total
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {quotations.map((quotation) => {
              const statusChip = getStatusChipProps(quotation.status)

              return (
                <TableRow key={quotation.quotationId}>
                  <TableCell>
                    <RouterLink
                      to={`/commercial/quotations/${quotation.quotationId}`}
                    >
                      {quotation.quotationId}
                    </RouterLink>
                  </TableCell>
                  <TableCell>{quotation.customerId}</TableCell>
                  <TableCell>{formatDate(quotation.creationDate)}</TableCell>
                  <TableCell>{formatDate(quotation.deliveryDate)}</TableCell>
                  <TableCell align="center">
                    <Chip
                      label={statusChip.label}
                      color={statusChip.color}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{quotation.salesperson}</TableCell>
                  <TableCell align="right">
                    {formatCurrency(quotation.totalAmount)}
                  </TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  )
}

export default QuotationsPage
