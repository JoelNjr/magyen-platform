import { useEffect, useMemo, useState } from 'react'
import { Alert, Button, MenuItem, Paper, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../../../layout/PageHeader'
import EmptyState from '../../home/components/EmptyState'
import { getCustomers } from '../../commercial/services/commercialService'
import { externalPlotterCustomerOptions } from '../presentation/plotterJobFilters'
import { formatPlotterMoney } from '../presentation/plotterJobPresentation'
import { getPlotterCustomerAccounts } from '../services/plotterService'

const headerCellSx = { fontWeight: 'bold' }

function PlotterCustomerAccountsPage() {
  const navigate = useNavigate()
  const [customers, setCustomers] = useState([])
  const [accounts, setAccounts] = useState([])
  const [customerFilter, setCustomerFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  const plotterCustomers = useMemo(
    () => externalPlotterCustomerOptions(customers),
    [customers]
  )

  useEffect(() => {
    let active = true
    getCustomers()
      .then((data) => {
        if (active) {
          setCustomers(Array.isArray(data?.customers) ? data.customers : [])
        }
      })
      .catch(() => {
        if (active) {
          setCustomers([])
        }
      })
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    let active = true
    setLoading(true)
    setFailed(false)
    getPlotterCustomerAccounts({ customerId: customerFilter })
      .then((data) => {
        if (active) {
          setAccounts(Array.isArray(data?.customers) ? data.customers : [])
          setLoading(false)
        }
      })
      .catch(() => {
        if (active) {
          setFailed(true)
          setAccounts([])
          setLoading(false)
        }
      })
    return () => {
      active = false
    }
  }, [customerFilter])

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Cuentas de Plotter"
        actions={
          <Button onClick={() => navigate('/plotter')}>Volver a trabajos</Button>
        }
      />
      <Alert severity="info">
        Cada trabajo externo genera un débito con su valor comercial. Los pagos
        se registran sobre la cuenta del cliente y no sobre un trabajo.
      </Alert>
      <TextField
        select
        label="Cliente"
        value={customerFilter}
        onChange={(event) => setCustomerFilter(event.target.value)}
        sx={{ maxWidth: 360 }}
      >
        <MenuItem value="">Todos</MenuItem>
        {plotterCustomers.map((customer) => (
          <MenuItem key={customer.customerId} value={customer.customerId}>
            {customer.name}
          </MenuItem>
        ))}
      </TextField>
      {loading && <Skeleton variant="rounded" height={180} />}
      {!loading && failed && (
        <Alert severity="error">No fue posible cargar las cuentas de Plotter.</Alert>
      )}
      {!loading && !failed && accounts.length === 0 && (
        <EmptyState
          title="Sin cuentas de Plotter"
          message="Las cuentas aparecen cuando existe un trabajo externo o un pago del cliente."
        />
      )}
      {!loading && !failed && accounts.length > 0 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={headerCellSx}>Cliente</TableCell>
                <TableCell align="right" sx={headerCellSx}>Trabajos</TableCell>
                <TableCell align="right" sx={headerCellSx}>Débitos</TableCell>
                <TableCell align="right" sx={headerCellSx}>Pagos</TableCell>
                <TableCell align="right" sx={headerCellSx}>Saldo</TableCell>
                <TableCell align="right" sx={headerCellSx}>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {accounts.map((account) => (
                <TableRow key={account.customerId} hover>
                  <TableCell>{account.customerName || '—'}</TableCell>
                  <TableCell align="right">{account.jobCount}</TableCell>
                  <TableCell align="right">{formatPlotterMoney(account.debitTotal)}</TableCell>
                  <TableCell align="right">{formatPlotterMoney(account.paymentTotal)}</TableCell>
                  <TableCell align="right">{formatPlotterMoney(account.outstandingAmount)}</TableCell>
                  <TableCell align="right">
                    <Button
                      size="small"
                      onClick={() => navigate(`/plotter/customer-accounts/${account.customerId}`)}
                    >
                      Ver cuenta
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Stack>
  )
}

export default PlotterCustomerAccountsPage
