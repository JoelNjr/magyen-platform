import {
  formatFinanceDate,
  formatFinanceMoney,
  getCalendarMonthRange,
  getPreviousCalendarMonthRange,
  resolveApiErrorMessage,
} from '../../finance/presentation/financePresentation'
import {
  getProductionOrderStatusChipProps,
  getProductionPriorityChipProps,
} from '../../production/presentation/productionStatusPresentation'
import { resolveProductionBusinessLabel } from '../../production/presentation/resolveProductionBusinessLabel'
import { getObligationTypeLabel } from '../../finance/presentation/financePresentation'

export {
  formatFinanceDate,
  formatFinanceMoney,
  getCalendarMonthRange,
  getPreviousCalendarMonthRange,
  resolveApiErrorMessage,
  getProductionPriorityChipProps,
  getObligationTypeLabel,
  resolveProductionBusinessLabel,
}

/**
 * Etiquetas de estado de producción para Home (español operativo).
 * No altera enums del backend ni el módulo Production.
 */
export function getHomeProductionStatusChipProps(status) {
  const base = getProductionOrderStatusChipProps(status)
  if (status === 'IN_PROGRESS') {
    return { ...base, label: 'En proceso' }
  }
  if (status === 'PLANNED') {
    return { ...base, label: 'Planificada' }
  }
  if (status === 'CREATED') {
    return { ...base, label: 'Creada' }
  }
  if (status === 'COMPLETED') {
    return { ...base, label: 'Completada' }
  }
  return base
}

/**
 * Evita duplicar en UI los rollos ya mostrados en "Rollos de papel".
 * No recalcula stock bajo: solo filtra presentación.
 */
export function filterGeneralInventoryAlertItems(inventoryItems, paperRollItems) {
  const items = Array.isArray(inventoryItems) ? inventoryItems : []
  const paperIds = new Set(
    (Array.isArray(paperRollItems) ? paperRollItems : [])
      .map((item) => item?.inventoryItemId)
      .filter(Boolean)
      .map(String)
  )
  return items.filter((item) => {
    if (item?.paperRollNumber) {
      return false
    }
    if (item?.inventoryItemId && paperIds.has(String(item.inventoryItemId))) {
      return false
    }
    return true
  })
}

export function formatHomeStock(value) {
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return '—'
  }
  return amount.toLocaleString('es-CO', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4,
  })
}

export function formatHomeMargin(value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return '—'
  }
  return `${amount.toLocaleString('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} %`
}

export function getProfitabilityStatusLabel(status) {
  if (status === 'COMPLETE') return 'Completa'
  if (status === 'PARTIALLY_UNVALUED') return 'Parcialmente valorizada'
  if (status === 'NO_COST_DATA') return 'Sin datos de costo'
  return status || '—'
}

/**
 * Presentación visual del compromiso según campos del backend.
 * No recalcula overdue: usa item.overdue y daysUntilDue.
 */
export function getCommitmentUrgencyChipProps(item) {
  if (!item) {
    return { label: '—', color: 'default' }
  }
  if (item.overdue) {
    const days = item.daysOverdue
    return {
      label: days != null ? `Vencido (${days} d)` : 'Vencido',
      color: 'error',
    }
  }
  if (item.daysUntilDue === 0) {
    return { label: 'Vence hoy', color: 'warning' }
  }
  if (item.daysUntilDue != null) {
    return {
      label: `Próximo (${item.daysUntilDue} d)`,
      color: 'info',
    }
  }
  return { label: 'Pendiente', color: 'default' }
}

export function formatCustomerLabel(item) {
  const name = item?.customerName
  if (typeof name === 'string' && name.trim()) {
    return name.trim()
  }
  return '—'
}
