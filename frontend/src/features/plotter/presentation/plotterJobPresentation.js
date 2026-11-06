export function formatPlotterStatusLabel(status) {
  switch (status) {
    case 'REGISTERED':
      return 'Registrado'
    case 'IN_PROGRESS':
      return 'En progreso'
    case 'COMPLETED':
      return 'Completado'
    case 'CANCELLED':
      return 'Cancelado'
    default:
      return status || '—'
  }
}

export function getPlotterStatusChipProps(status) {
  switch (status) {
    case 'REGISTERED':
      return { label: formatPlotterStatusLabel(status), color: 'default' }
    case 'IN_PROGRESS':
      return { label: formatPlotterStatusLabel(status), color: 'info' }
    case 'COMPLETED':
      return { label: formatPlotterStatusLabel(status), color: 'success' }
    case 'CANCELLED':
      return { label: formatPlotterStatusLabel(status), color: 'error' }
    default:
      return { label: formatPlotterStatusLabel(status), color: 'default' }
  }
}

export function formatPlotterNumber(value, { minimumFractionDigits = 2, maximumFractionDigits = 4 } = {}) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }

  const numericValue = Number(value)

  if (Number.isNaN(numericValue)) {
    return String(value)
  }

  return numericValue.toLocaleString('es-CO', {
    minimumFractionDigits,
    maximumFractionDigits,
  })
}

export function formatPlotterMoney(value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }

  const numericValue = Number(value)

  if (Number.isNaN(numericValue)) {
    return String(value)
  }

  return `$${numericValue.toLocaleString('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`
}

export function formatPlotterPendingMonthLabel(year, month) {
  const monthIndex = Number(month) - 1
  const labels = [
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
  if (!year || monthIndex < 0 || monthIndex > 11) {
    return '—'
  }
  return `${labels[monthIndex]} ${year}`
}

export function formatPlotterDate(value) {
  if (!value) {
    return '—'
  }

  const date = new Date(`${value}T00:00:00`)

  if (Number.isNaN(date.getTime())) {
    return String(value)
  }

  return date.toLocaleDateString('es-CO')
}

export function formatPlotterJobTypeLabel(jobType) {
  switch (jobType) {
    case 'INTERNAL_MAGYEN':
      return 'Servicio Plotter interno Magyen'
    case 'EXTERNAL':
      return 'Venta Plotter externa'
    case 'WASTE':
      return 'Merma'
    default:
      return jobType || '—'
  }
}

export function getPlotterJobTypeChipProps(jobType) {
  const label = formatPlotterJobTypeLabel(jobType)
  switch (jobType) {
    case 'INTERNAL_MAGYEN':
      return { label, color: 'secondary' }
    case 'EXTERNAL':
      return { label, color: 'info' }
    case 'WASTE':
      return { label, color: 'warning' }
    default:
      return { label, color: 'default' }
  }
}

export function isInternalPlotterJob(jobType) {
  return jobType === 'INTERNAL_MAGYEN'
}

export function isWastePlotterJob(jobType) {
  return jobType === 'WASTE'
}

export function isExternalPlotterJob(jobType) {
  return jobType === 'EXTERNAL'
}

export function canRegisterExternalPlotterPayment() {
  return false
}

export function canOpenExternalPlotterAccount(job) {
  return isExternalPlotterJob(job?.jobType) && Boolean(job?.customerId)
}

export function isExternalPlotterPaymentComplete() {
  return false
}

export function groupPlotterJobsByWorkDate(jobs) {
  const groups = []
  for (const job of jobs || []) {
    const date = job?.creationDate || ''
    const current = groups[groups.length - 1]
    if (!current || current.date !== date) {
      groups.push({ date, jobs: [job] })
    } else {
      current.jobs.push(job)
    }
  }
  return groups
}

export function isPlotterDateOnlyCorrection(job, payload) {
  if (!job || !payload || payload.creationDate === job.creationDate) {
    return false
  }
  const sameRoll = payload.paperInventoryItemId === job.paperInventoryItemId
  const sameMeters = Number(payload.printedMeters) === Number(job.printedMeters)
  const samePrice = Number(payload.pricePerMeter || 0) === Number(job.pricePerMeter || 0)
  const sameValue =
    job.jobType !== 'EXTERNAL' || Number(payload.commercialValue) === Number(job.totalAmount)
  const sameNotes = (payload.observations || '') === (job.observations || '')
  return sameRoll && sameMeters && samePrice && sameValue && sameNotes
}

export function parseHistoricalOrderMonth(value) {
  if (typeof value !== 'string' || !/^\d{4}-\d{2}$/.test(value)) {
    return null
  }

  const [yearText, monthText] = value.split('-')
  const year = Number(yearText)
  const month = Number(monthText)
  if (!Number.isInteger(year) || year < 1 || !Number.isInteger(month) || month < 1 || month > 12) {
    return null
  }

  return { year, month }
}

export function isEligibleHistoricalPlotterOrderStatus(status) {
  return status === 'DELIVERED' || status === 'CLOSED'
}

export function plotterOrdersForRegistration(openOrders, historicalOrders, historicalRegistration) {
  if (historicalRegistration) {
    return (historicalOrders || []).filter((order) =>
      isEligibleHistoricalPlotterOrderStatus(order?.status)
    )
  }

  return (openOrders || []).filter(
    (order) => !isEligibleHistoricalPlotterOrderStatus(order?.status)
  )
}

export function formatCommercialOrderStatusLabel(status) {
  switch (status) {
    case 'CONFIRMED':
      return 'Confirmada'
    case 'IN_PRODUCTION':
      return 'En producción'
    case 'READY_FOR_DELIVERY':
      return 'Lista para entrega'
    case 'DELIVERED':
      return 'Entregada'
    case 'CLOSED':
      return 'Cerrada'
    default:
      return status || '—'
  }
}

export function formatHistoricalPlotterOrderOption(order) {
  const identity = formatPlotterOrderLabel(order)
  const customer = order?.customerName || 'Sin cliente'
  const date = formatPlotterDate(order?.confirmationDate)
  const status = formatCommercialOrderStatusLabel(order?.status)
  return `${identity} — ${customer} — ${date} — ${status}`
}

export function withHistoricalPlotterRegistration(payload, enabled) {
  if (!enabled) {
    return payload
  }

  return {
    ...payload,
    historicalRegistration: true,
  }
}

export function formatPlotterOrderLabel(jobOrOrder) {
  const orderNumber = jobOrOrder?.orderNumber
  if (!orderNumber) {
    return '—'
  }
  const description = jobOrOrder?.orderDescription || jobOrOrder?.description
  return description ? `#${orderNumber} — ${description}` : `#${orderNumber}`
}

export function formatPlotterCustomerLabel(job, fallbackName) {
  return job?.customerName || fallbackName || '—'
}

export function calculatePlotterTotalPreview(printedMeters, pricePerMeter) {
  const meters = Number(printedMeters)
  const price = Number(pricePerMeter)

  if (Number.isNaN(meters) || Number.isNaN(price) || meters <= 0 || price < 0) {
    return null
  }

  return meters * price
}
