export const INVENTORY_UNIT_OPTIONS = [
  { value: 'UNIT', label: 'Unidad' },
  { value: 'METER', label: 'Metro' },
  { value: 'KILOGRAM', label: 'Kilogramo' },
  { value: 'LITER', label: 'Litro' },
  { value: 'ROLL', label: 'Rollo' },
]

export const INVENTORY_MATERIAL_TYPE_OPTIONS = [
  { value: 'FABRIC', label: 'Tela' },
  { value: 'PAPER', label: 'Papel' },
  { value: 'INK', label: 'Tinta' },
  { value: 'THREAD', label: 'Hilo' },
  { value: 'DTF', label: 'DTF' },
  { value: 'OTHER', label: 'Otro' },
]

export function formatMaterialTypeLabel(materialType) {
  const option = INVENTORY_MATERIAL_TYPE_OPTIONS.find(
    (item) => item.value === materialType
  )
  return option?.label || materialType || '—'
}

export function getInventoryStockStatusChipProps(lowStock) {
  if (lowStock) {
    return { label: 'Stock bajo', color: 'error' }
  }

  return { label: 'Disponible', color: 'success' }
}

export function formatUnitOfMeasureShortLabel(unitOfMeasure) {
  switch (unitOfMeasure) {
    case 'UNIT':
      return 'u'
    case 'METER':
      return 'm'
    case 'KILOGRAM':
      return 'kg'
    case 'LITER':
      return 'L'
    case 'ROLL':
      return 'rollo'
    default:
      return unitOfMeasure || ''
  }
}

export function formatUnitOfMeasureLabel(unitOfMeasure) {
  const option = INVENTORY_UNIT_OPTIONS.find((item) => item.value === unitOfMeasure)
  return option?.label || unitOfMeasure || '—'
}

export function formatInventoryNumber(value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }

  const numericValue = Number(value)

  if (Number.isNaN(numericValue)) {
    return String(value)
  }

  return numericValue.toLocaleString('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  })
}

export function formatStockWithUnit(stock, unitOfMeasure) {
  const quantity = formatInventoryNumber(stock)
  const unitLabel = formatUnitOfMeasureShortLabel(unitOfMeasure)

  if (!unitLabel) {
    return quantity
  }

  return `${quantity} ${unitLabel}`
}

export function getInventoryMaterialTitle(item) {
  if (!item) {
    return 'Material'
  }

  if (item.plotterPaperRoll && item.paperRollNumber) {
    return `Rollo ${item.paperRollNumber}`
  }

  const description = typeof item.description === 'string' ? item.description.trim() : ''
  if (description) {
    return description
  }

  return item.name || item.materialCode || 'Material'
}

export function formatInventoryMoney(value) {
  if (value === null || value === undefined || value === '') {
    return null
  }

  const numericValue = Number(value)

  if (Number.isNaN(numericValue)) {
    return String(value)
  }

  return numericValue.toLocaleString('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

export function formatUnitCostLabel(unitCost, unitOfMeasure) {
  const formatted = formatInventoryMoney(unitCost)

  if (formatted === null) {
    return 'No configurado'
  }

  const unitLabel = formatUnitOfMeasureShortLabel(unitOfMeasure)
  return unitLabel ? `$${formatted} / ${unitLabel}` : `$${formatted}`
}
