import { FormControl, FormHelperText, InputLabel, MenuItem, Select } from '@mui/material'

export default function CatalogSelect({
  label,
  value,
  onChange,
  options,
  disabled,
  required = false,
  helperText,
}) {
  const selectId = `catalog-select-${label}`
  return (
    <FormControl fullWidth disabled={disabled} required={required}>
      <InputLabel id={`${selectId}-label`}>{label}</InputLabel>
      <Select
        labelId={`${selectId}-label`}
        id={selectId}
        label={label}
        value={value ?? ''}
        onChange={(event) => onChange(event.target.value)}
      >
        {!required ? (
          <MenuItem value="">
            <em>Sin seleccionar</em>
          </MenuItem>
        ) : null}
        {options.map((option) => (
          <MenuItem key={String(option.value)} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </Select>
      {helperText ? <FormHelperText>{helperText}</FormHelperText> : null}
    </FormControl>
  )
}
