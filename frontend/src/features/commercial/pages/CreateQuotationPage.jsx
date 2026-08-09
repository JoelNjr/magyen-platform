import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Paper,
  Snackbar,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import CreateCustomerDialog from '../components/CreateCustomerDialog'
import CustomerSelector from '../components/CustomerSelector'
import {
  createCustomer,
  createQuotation,
  getCustomers,
} from '../services/commercialService'

function CreateQuotationPage() {
  const navigate = useNavigate()
  const [customers, setCustomers] = useState([])
  const [customersLoading, setCustomersLoading] = useState(true)
  const [customersFailed, setCustomersFailed] = useState(false)
  const [customerId, setCustomerId] = useState('')
  const [deliveryDate, setDeliveryDate] = useState('')
  const [salesperson, setSalesperson] = useState('')
  const [observations, setObservations] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [failed, setFailed] = useState(false)
  const [customerRequiredError, setCustomerRequiredError] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [creatingCustomer, setCreatingCustomer] = useState(false)
  const [createCustomerFailed, setCreateCustomerFailed] = useState(false)
  const [customersRefreshFailed, setCustomersRefreshFailed] = useState(false)
  const [customerCreatedOpen, setCustomerCreatedOpen] = useState(false)

  useEffect(() => {
    setCustomersLoading(true)
    setCustomersFailed(false)

    getCustomers()
      .then((data) => {
        const nextCustomers = Array.isArray(data?.customers) ? data.customers : []
        setCustomers(nextCustomers)
        setCustomersLoading(false)
      })
      .catch(() => {
        setCustomers([])
        setCustomersFailed(true)
        setCustomersLoading(false)
      })
  }, [])

  function openCreateCustomerDialog() {
    if (creatingCustomer || customersLoading) {
      return
    }

    setCreateCustomerFailed(false)
    setCustomersRefreshFailed(false)
    setCreateDialogOpen(true)
  }

  function handleCreateCustomerDialogClose() {
    if (creatingCustomer) {
      return
    }

    setCreateDialogOpen(false)
    setCreateCustomerFailed(false)
  }

  async function handleCreateCustomer(name) {
    setCreateCustomerFailed(false)
    setCustomersRefreshFailed(false)
    setCreatingCustomer(true)

    try {
      const createdCustomer = await createCustomer({ name })

      try {
        const data = await getCustomers()
        const nextCustomers = Array.isArray(data?.customers) ? data.customers : []
        setCustomers(nextCustomers)
        setCustomersFailed(false)
      } catch {
        setCustomersRefreshFailed(true)
      }

      setCustomerId(createdCustomer.customerId)
      setCustomerRequiredError(false)
      setCreateDialogOpen(false)
      setCustomerCreatedOpen(true)
    } catch {
      setCreateCustomerFailed(true)
    } finally {
      setCreatingCustomer(false)
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFailed(false)
    setCustomerRequiredError(false)

    if (!customerId) {
      setCustomerRequiredError(true)
      return
    }

    setSubmitting(true)

    const payload = {
      customerId,
      deliveryDate,
      salesperson,
      observations,
    }

    try {
      await createQuotation(payload)
      navigate('/commercial', { state: { created: true } })
    } catch {
      setFailed(true)
      setSubmitting(false)
    }
  }

  return (
    <>
      <Paper
        sx={{
          p: 4,
          maxWidth: 640,
          mx: 'auto',
        }}
      >
        <form onSubmit={handleSubmit}>
          <Stack spacing={3}>
            <Typography variant="h4">Nueva Cotización</Typography>

            {failed && (
              <Alert severity="error">No fue posible crear la cotización.</Alert>
            )}

            {customerRequiredError && (
              <Alert severity="error">Debes seleccionar un cliente.</Alert>
            )}

            {customersRefreshFailed && (
              <Alert severity="warning">
                El cliente se creó, pero no fue posible actualizar el listado.
              </Alert>
            )}

            <Stack spacing={1}>
              <CustomerSelector
                customers={customers}
                value={customerId}
                onChange={(selectedCustomerId) => {
                  setCustomerId(selectedCustomerId)
                  setCustomerRequiredError(false)
                }}
                loading={customersLoading}
                error={customersFailed}
                disabled={submitting || creatingCustomer}
                required
              />

              <Button
                type="button"
                variant="text"
                onClick={openCreateCustomerDialog}
                disabled={customersLoading || submitting || creatingCustomer}
                sx={{ alignSelf: 'flex-start' }}
              >
                + Nuevo cliente
              </Button>
            </Stack>

            <TextField
              label="Fecha de entrega"
              type="date"
              value={deliveryDate}
              onChange={(event) => setDeliveryDate(event.target.value)}
              fullWidth
              disabled={submitting}
              slotProps={{ inputLabel: { shrink: true } }}
            />

            <TextField
              label="Vendedor"
              value={salesperson}
              onChange={(event) => setSalesperson(event.target.value)}
              fullWidth
              disabled={submitting}
            />

            <TextField
              label="Observaciones"
              value={observations}
              onChange={(event) => setObservations(event.target.value)}
              fullWidth
              multiline
              minRows={3}
              disabled={submitting}
            />

            <Stack direction="row" spacing={2} sx={{ justifyContent: 'flex-end' }}>
              <Button
                type="button"
                variant="outlined"
                disabled={submitting}
                onClick={() => navigate('/commercial')}
              >
                Cancelar
              </Button>
              <Button type="submit" variant="contained" disabled={submitting}>
                {submitting ? 'Guardando...' : 'Guardar'}
              </Button>
            </Stack>
          </Stack>
        </form>
      </Paper>

      <CreateCustomerDialog
        open={createDialogOpen}
        onClose={handleCreateCustomerDialogClose}
        onCreated={handleCreateCustomer}
        submitting={creatingCustomer}
        error={createCustomerFailed}
      />

      <Snackbar
        open={customerCreatedOpen}
        autoHideDuration={4000}
        onClose={() => setCustomerCreatedOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          severity="success"
          variant="filled"
          onClose={() => setCustomerCreatedOpen(false)}
        >
          Cliente creado correctamente.
        </Alert>
      </Snackbar>
    </>
  )
}

export default CreateQuotationPage
