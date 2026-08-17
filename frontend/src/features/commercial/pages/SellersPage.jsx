import { useEffect, useState } from 'react'
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined'
import {
  Alert,
  Button,
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
import { useNavigate } from 'react-router-dom'
import { getSellers } from '../services/commercialService'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

function SellersTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Empleado vendedor</TableCell>
        <TableCell sx={headerCellSx}>Estado</TableCell>
      </TableRow>
    </TableHead>
  )
}

function SellersPage() {
  const navigate = useNavigate()
  const [sellers, setSellers] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    setLoading(true)
    setFailed(false)

    getSellers()
      .then((data) => {
        const nextSellers = Array.isArray(data?.sellers) ? data.sellers : []
        setSellers(nextSellers)
        setLoading(false)
      })
      .catch(() => {
        setSellers([])
        setFailed(true)
        setLoading(false)
      })
  }, [])

  return (
    <Stack spacing={3}>
      <Button
        type="button"
        variant="text"
        onClick={() => navigate('/commercial')}
        sx={{ alignSelf: 'flex-start' }}
      >
        Volver a comercial
      </Button>

      <Typography variant="h3">Vendedores</Typography>
      <Typography color="text.secondary">
        Los vendedores son empleados de Finanzas con pago fijo. Esta pantalla
        solo muestra quién puede vender; no es un catálogo independiente.
      </Typography>

      {loading && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <SellersTableHead />
            <TableBody>
              {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                <TableRow key={`seller-skeleton-${index}`}>
                  <TableCell>
                    <Skeleton width="50%" />
                  </TableCell>
                  <TableCell>
                    <Skeleton width="20%" />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {!loading && failed && (
        <Alert severity="error">No fue posible obtener los vendedores.</Alert>
      )}

      {!loading && !failed && sellers.length === 0 && (
        <Paper sx={{ p: { xs: 3, sm: 4 } }}>
          <Stack spacing={2} alignItems="center" sx={{ py: 2 }}>
            <BadgeOutlinedIcon color="action" sx={{ fontSize: 48 }} />
            <Typography variant="h6">
              No hay empleados con pago fijo disponibles para seleccionar como
              vendedor.
            </Typography>
            <Typography color="text.secondary" textAlign="center">
              Créalo en Finanzas → Empleados.
            </Typography>
            <Button variant="outlined" onClick={() => navigate('/finance')}>
              Ir a Finanzas → Empleados
            </Button>
          </Stack>
        </Paper>
      )}

      {!loading && !failed && sellers.length > 0 && (
        <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
          <Table>
            <SellersTableHead />
            <TableBody>
              {sellers.map((seller) => (
                <TableRow key={seller.sellerId} hover>
                  <TableCell>
                    <Typography variant="body1">{seller.name}</Typography>
                  </TableCell>
                  <TableCell>{seller.active ? 'Activo' : 'Inactivo'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Stack>
  )
}

export default SellersPage
