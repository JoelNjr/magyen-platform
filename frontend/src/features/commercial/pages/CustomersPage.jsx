import { useEffect, useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import PeopleOutlinedIcon from '@mui/icons-material/PeopleOutlined'
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
import CreateCustomerDialog from '../components/CreateCustomerDialog'
import UpdateCustomerDialog from '../components/UpdateCustomerDialog'
import {
  createCustomer,
  getCustomers,
  updateCustomer,
} from '../services/commercialService'

const headerCellSx = { fontWeight: 'bold' }
const SKELETON_ROW_COUNT = 4

function CustomersTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell sx={headerCellSx}>Cliente</TableCell>
        <TableCell align="right" sx={headerCellSx}>
          Acciones
        </TableCell>
      </TableRow>
    </TableHead>
  )
}

function CustomersPage() {
  const navigate = useNavigate()
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [creatingCustomer, setCreatingCustomer] = useState(false)
  const [createCustomerFailed, setCreateCustomerFailed] = useState(false)
  const [updateDialogOpen, setUpdateDialogOpen] = useState(false)
  const [selectedCustomer, setSelectedCustomer] = useState(null)
  const [updatingCustomer, setUpdatingCustomer] = useState(false)
  const [updateCustomerFailed, setUpdateCustomerFailed] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')
  const [successOpen, setSuccessOpen] = useState(false)

  useEffect(() => {
    setLoading(true)
    setFailed(false)

    getCustomers()
      .then((data) => {
        const nextCustomers = Array.isArray(data?.customers) ? data.customers : []
        setCustomers(nextCustomers)
        setLoading(false)
      })
      .catch(() => {
        setCustomers([])
        setFailed(true)
        setLoading(false)
      })
  }, [])

  function openCreateDialog() {
    if (creatingCustomer || updatingCustomer) {
      return
    }

    setCreateCustomerFailed(false)
    setCreateDialogOpen(true)
  }

  function handleCreateDialogClose() {
    if (creatingCustomer) {
      return
    }

    setCreateDialogOpen(false)
    setCreateCustomerFailed(false)
  }

  async function handleCreateCustomer(name) {
    setCreateCustomerFailed(false)
    setCreatingCustomer(true)

    try {
      await createCustomer({ name })

      try {
        const data = await getCustomers()
        const nextCustomers = Array.isArray(data?.customers) ? data.customers : []
        setCustomers(nextCustomers)
        setFailed(false)
      } catch {
        // Keep existing list if refresh fails; creation already succeeded.
      }

      setCreateDialogOpen(false)
      setSuccessMessage('Cliente creado correctamente.')
      setSuccessOpen(true)
    } catch {
      setCreateCustomerFailed(true)
    } finally {
      setCreatingCustomer(false)
    }
  }

  function openUpdateDialog(customer) {
    if (creatingCustomer || updatingCustomer) {
      return
    }

    setSelectedCustomer(customer)
    setUpdateCustomerFailed(false)
    setUpdateDialogOpen(true)
  }

  function handleUpdateDialogClose() {
    if (updatingCustomer) {
      return
    }

    setUpdateDialogOpen(false)
    setUpdateCustomerFailed(false)
    setSelectedCustomer(null)
  }

  async function handleUpdateCustomer(name) {
    if (!selectedCustomer?.customerId) {
      return
    }

    setUpdateCustomerFailed(false)
    setUpdatingCustomer(true)

    try {
      await updateCustomer(selectedCustomer.customerId, { name })

      try {
        const data = await getCustomers()
        const nextCustomers = Array.isArray(data?.customers) ? data.customers : []
        setCustomers(nextCustomers)
        setFailed(false)
      } catch {
        // Keep existing list if refresh fails; update already succeeded.
      }

      setUpdateDialogOpen(false)
      setSelectedCustomer(null)
      setSuccessMessage('Cliente actualizado correctamente.')
      setSuccessOpen(true)
    } catch {
      setUpdateCustomerFailed(true)
    } finally {
      setUpdatingCustomer(false)
    }
  }

  return (
    <>
      <Stack spacing={3}>
        <Button
          variant="outlined"
          onClick={() => navigate('/commercial')}
          sx={{ alignSelf: 'flex-start' }}
        >
          Volver
        </Button>

        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          justifyContent="space-between"
          alignItems={{ xs: 'stretch', sm: 'center' }}
        >
          <Typography variant="h3">Clientes</Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreateDialog}
            disabled={loading || creatingCustomer || updatingCustomer}
            sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}
          >
            Nuevo cliente
          </Button>
        </Stack>

        {loading && (
          <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
            <Table>
              <CustomersTableHead />
              <TableBody>
                {Array.from({ length: SKELETON_ROW_COUNT }).map((_, index) => (
                  <TableRow key={`customer-skeleton-${index}`}>
                    <TableCell>
                      <Skeleton width="60%" />
                      <Skeleton width="30%" sx={{ mt: 0.5 }} />
                    </TableCell>
                    <TableCell align="right">
                      <Skeleton width={72} sx={{ ml: 'auto' }} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {!loading && failed && (
          <Alert severity="error">No fue posible obtener los clientes.</Alert>
        )}

        {!loading && !failed && customers.length === 0 && (
          <Paper sx={{ p: { xs: 3, sm: 4 } }}>
            <Stack spacing={2} alignItems="center" sx={{ py: 2 }}>
              <PeopleOutlinedIcon color="action" sx={{ fontSize: 48 }} />
              <Typography variant="h6">No hay clientes registrados</Typography>
              <Typography color="text.secondary" textAlign="center">
                Crea tu primer cliente para comenzar a usarlo en cotizaciones.
              </Typography>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={openCreateDialog}
              >
                Nuevo cliente
              </Button>
            </Stack>
          </Paper>
        )}

        {!loading && !failed && customers.length > 0 && (
          <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
            <Table>
              <CustomersTableHead />
              <TableBody>
                {customers.map((customer) => (
                  <TableRow key={customer.customerId} hover>
                    <TableCell>
                      <Typography variant="body1">{customer.name}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        ID: {customer.customerId}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Button
                        type="button"
                        size="small"
                        onClick={() => openUpdateDialog(customer)}
                        disabled={creatingCustomer || updatingCustomer}
                      >
                        Editar
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Stack>

      <CreateCustomerDialog
        open={createDialogOpen}
        onClose={handleCreateDialogClose}
        onCreated={handleCreateCustomer}
        submitting={creatingCustomer}
        error={createCustomerFailed}
      />

      <UpdateCustomerDialog
        open={updateDialogOpen}
        onClose={handleUpdateDialogClose}
        customer={selectedCustomer}
        onUpdated={handleUpdateCustomer}
        submitting={updatingCustomer}
        error={updateCustomerFailed}
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
    </>
  )
}

export default CustomersPage
