import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined'
import {
  Alert,
  Button,
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
import { useNavigate } from 'react-router-dom'
import CreateSellerDialog from '../components/CreateSellerDialog'
import { createSeller, getSellers } from '../services/commercialService'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

function SellersTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Vendedor</TableCell>
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
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [creatingSeller, setCreatingSeller] = useState(false)
  const [createSellerFailed, setCreateSellerFailed] = useState(false)
  const [successOpen, setSuccessOpen] = useState(false)

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

  function openCreateDialog() {
    if (creatingSeller) {
      return
    }

    setCreateSellerFailed(false)
    setCreateDialogOpen(true)
  }

  function handleCreateDialogClose() {
    if (creatingSeller) {
      return
    }

    setCreateDialogOpen(false)
    setCreateSellerFailed(false)
  }

  async function handleCreateSeller(name) {
    setCreateSellerFailed(false)
    setCreatingSeller(true)

    try {
      const createdSeller = await createSeller({ name })
      setSellers((current) => [...current, createdSeller])
      setCreateDialogOpen(false)
      setSuccessOpen(true)
    } catch {
      setCreateSellerFailed(true)
    } finally {
      setCreatingSeller(false)
    }
  }

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

      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        justifyContent="space-between"
        alignItems={{ xs: 'stretch', sm: 'center' }}
      >
        <Typography variant="h3">Vendedores</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={openCreateDialog}
          disabled={loading || creatingSeller}
          sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
        >
          Nuevo vendedor
        </Button>
      </Stack>

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
            <Typography variant="h6">No hay vendedores registrados</Typography>
            <Typography color="text.secondary" textAlign="center">
              Crea el vendedor interno que vendió cada cotización u orden.
            </Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={openCreateDialog}
            >
              Nuevo vendedor
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

      <CreateSellerDialog
        open={createDialogOpen}
        onClose={handleCreateDialogClose}
        onCreated={handleCreateSeller}
        submitting={creatingSeller}
        error={createSellerFailed}
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
          Vendedor creado correctamente.
        </Alert>
      </Snackbar>
    </Stack>
  )
}

export default SellersPage
