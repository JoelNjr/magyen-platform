import { formatInventoryNumber } from './inventoryStatusPresentation'

export const INVENTORY_MOVEMENT_TYPE_OPTIONS = [
  { value: 'IN', label: 'Entrada' },
  { value: 'OUT', label: 'Salida' },
  { value: 'ADJUSTMENT', label: 'Ajuste' },
]

export function formatInventoryMovementType(movementType) {
  switch (movementType) {
    case 'IN':
      return 'Entrada'
    case 'OUT':
      return 'Salida'
    case 'ADJUSTMENT':
      return 'Ajuste'
    default:
      return movementType || '—'
  }
}

export function formatInventoryMovementSourceType(sourceType) {
  switch (sourceType) {
    case 'MANUAL':
      return 'Manual'
    case 'PRODUCTION':
      return 'Producción'
    case 'PLOTTER':
      return 'Plotter'
    default:
      return sourceType || 'Manual'
  }
}

export function formatInventoryMovementSourceLabel(sourceType, sourceId) {
  const origin = formatInventoryMovementSourceType(sourceType)

  if (!sourceId || sourceType === 'MANUAL' || !sourceType) {
    return `Origen: ${origin}`
  }

  return `Origen: ${origin}`
}

export function formatInventoryMovementSourceId(sourceType, sourceId) {
  if (!sourceId || sourceType === 'MANUAL' || !sourceType) {
    return null
  }

  return String(sourceId)
}

/**
 * Preview-only stock calculation for UX. Backend remains authoritative.
 */
export function previewStockAfterMovement(currentStock, movementType, quantity) {
  const stock = Number(currentStock)
  const amount = Number(quantity)

  if (Number.isNaN(stock) || Number.isNaN(amount)) {
    return null
  }

  if (movementType === 'IN') {
    return stock + amount
  }

  if (movementType === 'OUT') {
    return stock - amount
  }

  if (movementType === 'ADJUSTMENT') {
    return stock + amount
  }

  return null
}

export function formatInventoryMovementQuantity(quantity, movementType) {
  const formatted = formatInventoryNumber(quantity)

  if (movementType === 'ADJUSTMENT') {
    const numericValue = Number(quantity)

    if (!Number.isNaN(numericValue) && numericValue > 0) {
      return `+${formatted}`
    }
  }

  return formatted
}

/**
 * Display-only datetime formatting for movement timestamps.
 */
export function formatInventoryMovementDateTime(value) {
  if (!value) {
    return '—'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return String(value)
  }

  return date.toLocaleString('es-CO', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
