import Autocomplete from '@mui/material/Autocomplete'
import TextField from '@mui/material/TextField'

function SellerSelector({
  sellers,
  value,
  onChange,
  loading = false,
  error = false,
  disabled = false,
  required = false,
}) {
  const safeSellers = Array.isArray(sellers) ? sellers.filter((seller) => seller.active) : []
  const selectedSeller =
    safeSellers.find((seller) => seller.sellerId === value) || null
  const empty = !loading && !error && safeSellers.length === 0

  return (
    <Autocomplete
      fullWidth
      options={safeSellers}
      value={selectedSeller}
      loading={loading}
      disabled={disabled || loading || error}
      disableClearable={required}
      onChange={(_, seller) => {
        onChange(seller ? seller.sellerId : '')
      }}
      getOptionLabel={(option) => option?.name || ''}
      isOptionEqualToValue={(option, selected) =>
        option?.sellerId === selected?.sellerId
      }
      noOptionsText="No hay empleados con pago fijo disponibles para seleccionar como vendedor."
      loadingText="Cargando vendedores..."
      renderInput={(params) => (
        <TextField
          {...params}
          label="Empleado vendedor / Vendedor"
          required={required}
          error={error}
          helperText={
            error
              ? 'No fue posible cargar los vendedores.'
              : empty
                ? 'No hay empleados con pago fijo disponibles para seleccionar como vendedor. Créalo en Finanzas → Empleados.'
                : 'Seleccione un empleado. No escriba el nombre a mano.'
          }
        />
      )}
    />
  )
}

export default SellerSelector
