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
      return { label: formatPlotterStatusLabel(status), color: 'warning' }
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
    default:
      return jobType || '—'
  }
}

export function isInternalPlotterJob(jobType) {
  return jobType === 'INTERNAL_MAGYEN'
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
