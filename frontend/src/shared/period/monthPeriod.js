const MONTH_LABELS = [
  'Enero',
  'Febrero',
  'Marzo',
  'Abril',
  'Mayo',
  'Junio',
  'Julio',
  'Agosto',
  'Septiembre',
  'Octubre',
  'Noviembre',
  'Diciembre',
]

export function toIsoDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function getCalendarMonthRange(referenceDate = new Date()) {
  const year = referenceDate.getFullYear()
  const month = referenceDate.getMonth()
  return {
    fromDate: toIsoDate(new Date(year, month, 1)),
    toDate: toIsoDate(new Date(year, month + 1, 0)),
    year,
    month: month + 1,
  }
}

export function shiftCalendarMonth(fromDate, deltaMonths) {
  const current = parseIsoDate(fromDate) || new Date()
  return getCalendarMonthRange(new Date(current.getFullYear(), current.getMonth() + deltaMonths, 1))
}

export function monthPeriodFromYearMonth(year, month) {
  return getCalendarMonthRange(new Date(year, month - 1, 1))
}

export function formatMonthPeriodLabel(fromDate) {
  const date = parseIsoDate(fromDate)
  if (!date) {
    return '—'
  }
  return `${MONTH_LABELS[date.getMonth()]} ${date.getFullYear()}`
}

export function getSelectableYears(referenceYear = new Date().getFullYear()) {
  const years = []
  for (let year = referenceYear - 4; year <= referenceYear + 1; year += 1) {
    years.push(year)
  }
  return years
}

export function getMonthOptions() {
  return MONTH_LABELS.map((label, index) => ({
    value: index + 1,
    label,
  }))
}

function parseIsoDate(value) {
  if (!value) {
    return null
  }
  const date = new Date(`${value}T00:00:00`)
  return Number.isNaN(date.getTime()) ? null : date
}
