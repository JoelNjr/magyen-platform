const moneyFormatter = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})

export const TRANSACTION_TYPE_OPTIONS = [
  { value: 'INCOME', label: 'Ingreso' },
  { value: 'EXPENSE', label: 'Gasto' },
]

export const OBLIGATION_TYPE_OPTIONS = [
  { value: 'SERVICE', label: 'Servicio' },
  { value: 'PAYROLL', label: 'Nómina' },
  { value: 'CREDIT', label: 'Crédito' },
  { value: 'OTHER', label: 'Otro' },
]

export const FREQUENCY_OPTIONS = [
  { value: 'WEEKLY', label: 'Semanal' },
  { value: 'BIWEEKLY', label: 'Quincenal' },
  { value: 'MONTHLY', label: 'Mensual' },
  { value: 'YEARLY', label: 'Anual' },
]

export const INCOME_CATEGORY_OPTIONS = [
  { value: 'SALES', label: 'Ventas' },
  { value: 'PLOTTER_REVENUE', label: 'Ingreso Plotter' },
  { value: 'OTHER_INCOME', label: 'Otro ingreso' },
]

export const EXPENSE_CATEGORY_OPTIONS = [
  { value: 'MATERIALS', label: 'Materiales' },
  { value: 'PAPER', label: 'Papel' },
  { value: 'INK', label: 'Tinta' },
  { value: 'DTF', label: 'DTF' },
  { value: 'EMBROIDERY', label: 'Bordado' },
  { value: 'SERVICES', label: 'Servicios' },
  { value: 'PAYROLL', label: 'Nómina' },
  { value: 'PRODUCTION_PAYMENT', label: 'Pago producción' },
  { value: 'CREDIT_PAYMENT', label: 'Pago crédito' },
  { value: 'TRANSPORT', label: 'Transporte' },
  { value: 'MAINTENANCE', label: 'Mantenimiento' },
  { value: 'SOFTWARE', label: 'Software' },
  { value: 'ADVERTISING', label: 'Publicidad' },
  { value: 'OTHER_EXPENSE', label: 'Otro gasto' },
]

export function formatFinanceMoney(value) {
  const amount = Number(value)
  if (Number.isNaN(amount)) {
    return '$0'
  }
  return moneyFormatter.format(amount)
}

export function formatFinanceDate(value) {
  if (!value) {
    return '—'
  }

  const raw = String(value)
  if (/^\d{4}-\d{2}-\d{2}$/.test(raw)) {
    const [year, month, day] = raw.split('-')
    return `${day}/${month}/${year}`
  }

  const date = new Date(raw)
  if (Number.isNaN(date.getTime())) {
    return raw
  }

  return date.toLocaleDateString('es-CO')
}

export function toIsoDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function getCalendarMonthRange(referenceDate = new Date()) {
  const year = referenceDate.getFullYear()
  const month = referenceDate.getMonth()
  const fromDate = toIsoDate(new Date(year, month, 1))
  const toDate = toIsoDate(new Date(year, month + 1, 0))
  return { fromDate, toDate }
}

export function getPreviousCalendarMonthRange(referenceDate = new Date()) {
  const year = referenceDate.getFullYear()
  const month = referenceDate.getMonth()
  return getCalendarMonthRange(new Date(year, month - 1, 1))
}

export function getDefaultGenerationRange(referenceDate = new Date()) {
  const fromDate = toIsoDate(referenceDate)
  const to = new Date(referenceDate)
  to.setDate(to.getDate() + 30)
  return { fromDate, toDate: toIsoDate(to) }
}

export function getTransactionTypeLabel(type) {
  if (type === 'INCOME') return 'Ingreso'
  if (type === 'EXPENSE') return 'Gasto'
  return type || '—'
}

export function getObligationTypeLabel(type) {
  const found = OBLIGATION_TYPE_OPTIONS.find((option) => option.value === type)
  return found?.label || type || '—'
}

export function getFrequencyLabel(frequency) {
  const found = FREQUENCY_OPTIONS.find((option) => option.value === frequency)
  return found?.label || frequency || '—'
}

export function getOccurrenceStatusLabel(status) {
  if (status === 'PENDING') return 'Pendiente'
  if (status === 'PAID') return 'Pagado'
  if (status === 'CANCELLED') return 'Cancelado'
  return status || '—'
}

export function getPayrollCompensationTypeLabel(type) {
  if (type === 'FIXED_PAYROLL') return 'Fijo'
  if (type === 'PRODUCTION_BASED') return 'Por producción'
  return type || '—'
}

export const PAYROLL_DEDUCTION_TYPE_OPTIONS = [
  { value: 'LOAN', label: 'Préstamo' },
  { value: 'ADVANCE', label: 'Anticipo' },
  { value: 'OTHER', label: 'Otro descuento' },
]

export function getPayrollDeductionTypeLabel(type) {
  const found = PAYROLL_DEDUCTION_TYPE_OPTIONS.find((option) => option.value === type)
  return found?.label || type || '—'
}

export function getPayrollDeductionStatusLabel(status) {
  if (status === 'ACTIVE') return 'Activo'
  if (status === 'CANCELLED') return 'Cancelado'
  return status || '—'
}

export function formatPayrollPeriodRange(periodStart, periodEnd) {
  return `${formatFinanceDate(periodStart)} – ${formatFinanceDate(periodEnd)}`
}

export function isFullCalendarPayrollMonth(periodStart, periodEnd) {
  if (!periodStart || !periodEnd) {
    return false
  }
  const [yearText, monthText, dayText] = String(periodStart).split('-')
  const year = Number(yearText)
  const month = Number(monthText)
  const day = Number(dayText)
  if (!year || !month || day !== 1) {
    return false
  }
  const lastDay = new Date(Date.UTC(year, month, 0)).getUTCDate()
  const expectedEnd = `${yearText}-${monthText}-${String(lastDay).padStart(2, '0')}`
  return periodEnd === expectedEnd
}

export function formatPayrollPeriodLabel(periodStart, periodEnd) {
  if (!isFullCalendarPayrollMonth(periodStart, periodEnd)) {
    return formatPayrollPeriodRange(periodStart, periodEnd)
  }
  const [yearText, monthText] = String(periodStart).split('-')
  const label = new Intl.DateTimeFormat('es-CO', {
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(new Date(Date.UTC(Number(yearText), Number(monthText) - 1, 1)))
  return label.charAt(0).toUpperCase() + label.slice(1)
}

export function yearMonthInputValue(referenceDate = new Date()) {
  const year = referenceDate.getFullYear()
  const month = String(referenceDate.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

export function calendarMonthBoundsFromInput(yearMonth) {
  const [yearText, monthText] = String(yearMonth).split('-')
  const year = Number(yearText)
  const monthIndex = Number(monthText) - 1
  return getCalendarMonthRange(new Date(year, monthIndex, 1))
}

export function commissionSettlementLabel(status) {
  if (status === 'CALCULATED') return 'Comisión calculada'
  if (status === 'HISTORICAL') return 'Histórico / no pagable'
  if (status === 'PAID') return 'Comisión pagada'
  return '—'
}

export function canPaySellerCommission(seller) {
  if (!seller || seller.settlementStatus !== 'CALCULATED') {
    return false
  }
  const amount = Number(seller.accumulatedCommission)
  return Number.isFinite(amount) && amount > 0
}

export function sellerCommissionHasVariance(seller) {
  if (!seller || seller.settlementStatus !== 'PAID' || seller.paidCommissionSnapshot == null) {
    return false
  }
  return Number(seller.accumulatedCommission) !== Number(seller.paidCommissionSnapshot)
}

export function sellerCommissionPaymentDescription(seller, periodStart) {
  const monthLabel = commissionMonthLabel(periodStart)
  const amount = seller?.paidCommissionSnapshot ?? seller?.accumulatedCommission
  return `Vas a registrar el pago de la comisión de ${monthLabel} a ${seller?.displayName || 'el vendedor'} por ${formatFinanceMoney(amount)}. Esta acción registrará un gasto financiero de comisión y no registra un pago del cliente.`
}

export function commissionMonthLabel(periodStart) {
  if (!periodStart) {
    return 'este mes'
  }
  const [yearText, monthText] = String(periodStart).split('-')
  const date = new Date(Number(yearText), Number(monthText) - 1, 1)
  return date.toLocaleDateString('es-CO', { month: 'long', year: 'numeric' })
}

export function sellerCommissionAlreadyPaidMessage() {
  return 'La comisión de este vendedor para este mes ya fue pagada.'
}

export function sumCommissionAmounts(orders) {
  return (orders ?? []).reduce((total, line) => {
    const amount = Number(line.commissionAmount)
    return total + (Number.isFinite(amount) ? amount : 0)
  }, 0)
}

export function selectSellerCommission(sellers, employeeId) {
  if (!employeeId) {
    return null
  }
  return (sellers ?? []).find((seller) => seller.employeeId === employeeId) ?? null
}

export function getSourceTypeLabel(sourceType) {
  if (!sourceType) return '—'
  if (sourceType === 'MANUAL') return 'Manual'
  if (sourceType === 'RECURRING_OBLIGATION') return 'Obligación recurrente'
  if (sourceType === 'COMMERCIAL_ORDER') return 'Pago comercial'
  if (sourceType === 'PLOTTER') return 'Pago de Plotter'
  if (sourceType === 'PRODUCTION') return 'Producción'
  if (sourceType === 'PAYROLL') return 'Pago de nómina'
  if (sourceType === 'SELLER_COMMISSION') return 'Comisión de vendedor'
  if (sourceType === 'SERVICE') return 'Servicio'
  if (sourceType === 'CREDIT') return 'Crédito'
  if (sourceType === 'INVENTORY_PURCHASE') return 'Compra de inventario'
  if (sourceType === 'PLOTTER_INTERNAL_EXPENSE') return 'Servicio Plotter interno (gasto)'
  if (sourceType === 'PLOTTER_INTERNAL_INCOME') return 'Servicio Plotter interno (ingreso)'
  return sourceType
}

export function getTransactionTypeChipColor(type) {
  if (type === 'INCOME') return 'success'
  if (type === 'EXPENSE') return 'error'
  return 'default'
}

export function getOccurrenceStatusChipColor(status) {
  if (status === 'PENDING') return 'warning'
  if (status === 'PAID') return 'success'
  if (status === 'CANCELLED') return 'error'
  return 'default'
}

export function resolveApiErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || fallbackMessage
}
