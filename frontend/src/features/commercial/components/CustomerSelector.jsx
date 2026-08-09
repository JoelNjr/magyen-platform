import Autocomplete from '@mui/material/Autocomplete'
import TextField from '@mui/material/TextField'

function CustomerSelector({
  customers,
  value,
  onChange,
  loading = false,
  error = false,
  disabled = false,
  required = false,
}) {
  const safeCustomers = Array.isArray(customers) ? customers : []
  const selectedCustomer =
    safeCustomers.find((customer) => customer.customerId === value) || null

  return (
    <Autocomplete
      fullWidth
      options={safeCustomers}
      value={selectedCustomer}
      loading={loading}
      disabled={disabled || loading || error}
      disableClearable={required}
      onChange={(_, customer) => {
        onChange(customer ? customer.customerId : '')
      }}
      getOptionLabel={(option) => option?.name || ''}
      isOptionEqualToValue={(option, selected) =>
        option?.customerId === selected?.customerId
      }
      noOptionsText="No hay clientes disponibles"
      loadingText="Cargando clientes..."
      renderInput={(params) => (
        <TextField
          {...params}
          label="Cliente"
          required={required}
          error={error}
          helperText={
            error ? 'No fue posible cargar los clientes.' : undefined
          }
        />
      )}
    />
  )
}

export default CustomerSelector
